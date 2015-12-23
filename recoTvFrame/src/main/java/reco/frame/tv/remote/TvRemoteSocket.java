package reco.frame.tv.remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.remote.UdpHelper.OnUdpListener;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.util.Log;

public class TvRemoteSocket {

	/**
	 * 开放端口
	 */
	private final static String TAG = "TvRemoteSocket";
	public static TvRemoteSocket mRemoteSocket;
	private RequestListenerReceiver requestReceiver;
	private RemoteListener remoteListener;
	private RemoteKeyListener remoteKeyListener;
	private TvHttp tvHttp;
	private Context mContext;
	private Handler handler;
	private HashMap<String, TaskInfo> taskList;

	public static TvRemoteSocket instance(Context context) {

		synchronized (TvRemoteSocket.class) {

			if (mRemoteSocket == null) {
				mRemoteSocket = new TvRemoteSocket(context);
			}

		}
		return mRemoteSocket;

	}

	public TvRemoteSocket(Context context) {
		tvHttp = new TvHttp(context);
		mContext = context;
		taskList = new HashMap<String, TaskInfo>();
		handler = new Handler();
	}

	/**
	 * 启动遥控服务端
	 * 
	 * @param udp
	 *            是否启动UDP监听
	 */
	public void startRemoteServer(boolean udp) {
		Log.i(TAG, "遥控服务启动");
		try {
			if (requestReceiver == null) {
				requestReceiver = new RequestListenerReceiver(
						new WebServiceHandler(), Remote.TV_SERVER_PORT);
				requestReceiver.setDaemon(false);
			}
			requestReceiver.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 通过upd建立连接
		if (udp) {
			startUdpServer();
		}

	}

	/**
	 * 启动UPD监听 收到客户端消息后 返回IP与端口
	 */
	private void startUdpServer() {
		final String ip = RemoteUtil.getIp(mContext);
		UdpHelper.instance().startListen(new OnUdpListener() {

			@Override
			public void onMessageReceiver(String msg) {
				if (msg.contains(Remote.TYPE_UDP_REQUEST)) {
					UdpHelper.instance().send(ip + "," + Remote.TV_SERVER_PORT);
				}

			}
		});

	}

	/**
	 * 关闭UDP监听
	 */
	public void closeUdpServer() {
		UdpHelper.instance().closeServer();
	}

	/**
	 * 下载
	 * 
	 * @param url
	 * @return
	 */
	private TaskInfo checkDownload(final String url) {

		try {
			TaskInfo task = taskList.get(RemoteUtil.md5(url));
			
			if (task==null) {
				return download(url);
			}else{
				
				if (!RemoteUtil.isFileExist(task.getFilePath())) {
					taskList.remove(RemoteUtil.md5(url));
					RemoteUtil.deleteFile(mContext, task.getDownloadUrl());
					return download(url);
				}else{
					return task;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new TaskInfo();
	}
	
	private TaskInfo download(final String url){
		final TaskInfo newTask = new TaskInfo();
		taskList.put(RemoteUtil.md5(url), newTask);
		String suffix = url.substring(url.lastIndexOf(".") + 1);
		final String tempPath = mContext.getFilesDir() + "/"
				+ RemoteUtil.md5(url) + ".temp." + suffix;
		final String savePath = mContext.getFilesDir() + "/"
				+ RemoteUtil.md5(url) + "." + suffix;
		newTask.setStatus(TaskInfo.RUNNING);
		newTask.setFilePath(tempPath);
		handler.post(new Runnable() {

			@Override
			public void run() {

				// Log.e(TAG, "newTask3=" + newTask.toString());
				tvHttp.download(url, tempPath, true,
						new AjaxCallBack<java.io.File>() {

							@Override
							public void onLoading(long count,
									long current) {
								int progress = (int) (current * 100 / count);
								newTask.setProgress(progress);
								Log.i("progress=", progress + "");
								super.onLoading(count, current);
							}

							@Override
							public void onSuccess(File t) {
								// 改名
								t.renameTo(new File(savePath));
								newTask.setStatus(TaskInfo.SUCCESS);
								newTask.setFilePath(savePath);
								// if (RemoteUtil.installApk(mContext,
								// savePath)) {
								// newTask.setStatus(TaskInfo.INSTALLED);
								// RemoteUtil.deleteFile(mContext, url);
								// }
								super.onSuccess(t);

							}

							public void onFailure(Throwable t,
									int errorNo, String strMsg) {
								Log.i(TAG, "下载失败");
								RemoteUtil.deleteFile(mContext, url);
								newTask.setStatus(TaskInfo.FAILED);
								taskList.remove(RemoteUtil.md5(url));
							};

						});

			}
		});

		return newTask;
	}

	public void setListener(RemoteListener listener) {
		this.remoteListener = listener;
	}

	public void setOnRemoteKeyListener(RemoteKeyListener listener) {
		this.remoteKeyListener = listener;
	}

	public interface RemoteListener {

		// public void OnRemoteEvent(int event);
		public Object OnRemoteReceive(int action, Object param);

	}

	public interface RemoteKeyListener {
		public void onRemoteKeyDown(int keyCode);
	}

	public interface RemoteDownloadCallback {
		public Object onUpdata(TaskInfo task);

		public Object onSuccess(TaskInfo task);

		public Object onFailure(TaskInfo task);

	}

	private void sendKeyCode(final int keyCode) {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(keyCode);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	class WebServiceHandler implements HttpRequestHandler {

		public WebServiceHandler() {
			super();
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String line = request.getRequestLine().toString();
			response.setStatusCode(HttpStatus.SC_OK);
			Log.i(TAG, line);

			if (line.contains(Remote.TYPE_KEY)) {
				final int keyCode = Integer.parseInt(line.substring(
						line.indexOf("param=") + 6, line.indexOf("HTTP") - 1));
				if (remoteKeyListener != null) {

					handler.post(new Runnable() {

						@Override
						public void run() {
							sendKeyCode(keyCode);
							remoteKeyListener.onRemoteKeyDown(keyCode);
						}
					});

				}
				return;
			}

			if (line.contains(Remote.TYPE_CHECK_SERVER_STATUS)) {
				StringEntity entity = new StringEntity("true", "UTF-8");
				response.setEntity(entity);
				return;
			}
			if (!line.contains("param=") || !line.contains("action=")) {
				return;
			}

			int action = Integer.parseInt(line.substring(
					line.indexOf("action=") + 7, line.indexOf("param=") - 1));
			String param = line.substring(line.indexOf("param=") + 6,
					line.indexOf("HTTP") - 1);
			param = URLDecoder.decode(param, "UTF-8");

			if (line.contains(Remote.TYPE_TEXT)) {
				Object responseObj = remoteListener.OnRemoteReceive(action,
						param);
				StringEntity entity = new StringEntity(responseObj + "",
						"UTF-8");
				response.setEntity(entity);
			} else if (line.contains(Remote.TYPE_IMAGE_GET)) {
				Object responseObj = remoteListener.OnRemoteReceive(action,
						param);
				if (responseObj == null) {
					responseObj = "null";
				}
				byte[] data = RemoteUtil.Bitmap2Bytes((Bitmap) responseObj);
				ByteArrayEntity byteEntity = new ByteArrayEntity(data);
				response.setEntity(byteEntity);
			} else if (line.contains(Remote.TYPE_IMAGE_POST)) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();

				byte[] data = EntityUtils.toByteArray(entity);

				String content = EncodingUtils.getString(data, "utf-8");
				Log.e(TAG, content);

				String matchValue = "Content-Transfer-Encoding: binary\r\n\r\n";
				int lastIndex = content.lastIndexOf(matchValue);

				int startingIndex = lastIndex + matchValue.length();

				String imageHeader = content.substring(0, startingIndex - 1);
				String suffix=".jpg";
//				if (content.contains("PNG")) {
//					suffix=".png";
//				}
				byte[] bytes = imageHeader.getBytes();
				int bytesLength = bytes.length;

				byte[] entityContent = new byte[data.length - bytesLength - 1];
				int index = 0;
				for (int i = bytesLength + 1; i < data.length; i++) {
					entityContent[index] = data[i];
					index++;
				}

				// 检测当前图片是否已存在
				String path = mContext.getFilesDir().getAbsolutePath()
						+"/"+ RemoteUtil.md5(imageHeader)+suffix;
				//若不存在则写入
				if (!RemoteUtil.isFileExist(path)) {
					FileOutputStream fout = new FileOutputStream(path);
					// 将字节写入文件
					fout.write(entityContent);
					fout.close();
				}

				remoteListener.OnRemoteReceive(action, path);
			} else if (line.contains(Remote.TYPE_DOWNLOAD)) {
				TaskInfo responseObj = checkDownload(param);
				Log.i(TAG, responseObj.toString());
				StringEntity entity = new StringEntity(
						RemoteUtil.objToJson(responseObj) + "", "UTF-8");
				response.setEntity(entity);
			}

		}

	}
}
