package com.boxmate.tv.background;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.http.HttpHandler;

import com.boxmate.tv.LauncherActivity;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.SystemUtil;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class SecurityService extends Service {

	private TvHttp th;
	private String TAG = "SecurityService";
	private ArrayList<TaskInfo> mDownloadQueue;
	public static Boolean isDownloading;
	public static OnDownloadStatusChanaged delegate;

	public interface OnDownloadStatusChanaged {
		public void onTaskUpdate(String packageName, int action);
	}

	private final static int ALIVE = 0;
	private final static int CIRCLE = 1 * 1000;
	private static int alive = 0;

	// 通知类型
	public final static int DOWNLOAD_STATUS_UPDATE = 1;
	public final static int DOWNLOAD_STATUS_SUCCESS = 2;
	public final static int DOWNLOAD_STATUS_FAILED = 3;
	public final static int DOWNLOAD_STATUS_START = 4;
	public final static int DOWNLOAD_STATUS_INSTALLED = 5;
	public final static int NEW_VIDEO_LAUNCHER = 6;

	public final static String APP_ACTION_UPDATE = "com.app.action.update";
	public final static String APP_ACTION_FINISHED = "com.app.action.finished";
	public final static String APP_ACTION_FAILED = "com.app.action.failed";
	public final static String APP_ACTION_INSTALLED = "com.app.action.installed";
	private TaskInfo currenTaskInfo = null;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case ALIVE:// 心跳线程
				if (alive > 1000) {
					alive = 0;
				}
				Log.d(TAG, "alive=" + alive);
				alive++;
				break;
			case DOWNLOAD_STATUS_UPDATE:
				break;
			case DOWNLOAD_STATUS_SUCCESS:
				Log.i(TAG, "下载成功");
				try {
					TaskInfo taskTemp = (TaskInfo) msg.obj;
					CommonUtil.installApk(taskTemp.getDownloadUrl(),
							getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				}

				startDownload();
				break;
			case DOWNLOAD_STATUS_FAILED:

//				AppDownloadManager.getInstance().setStatusByTaskId(
//						TaskInfo.FAILED, currenTaskInfo.getTaskId());
				CommonUtil.deleteFile(getApplicationContext(),
						currenTaskInfo.getDownloadUrl());
				//currenTaskInfo.status = TaskInfo.FAILED;
				startDownload();
				break;
			case DOWNLOAD_STATUS_START:
				currenTaskInfo.status = TaskInfo.RUNNING;
				AppDownloadManager.getInstance().setStatusByTaskId(
						TaskInfo.RUNNING, currenTaskInfo.getTaskId());
				break;

			}
		}
	};

	public class ServiceBinder extends Binder {
		public SecurityService getService() {
			return SecurityService.this;
		}
	}


	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "守护服务启动。。。");
		//初始赋值
		isDownloading=false;
		CommonUtil.context = getApplicationContext();
		
		th = new TvHttp(SecurityService.this);
		instance = this;
		// 进程遭强杀后返回，防数据库路径为空
		if (LauncherActivity.dbFilePath == null) {
			LauncherActivity.dbFilePath = getFilesDir().getAbsolutePath();
		}
		keepAlive();
		// 继续未完成下载
		continueDownload();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDownloadQueue = null;
		Log.d(TAG, "onDestroy");
	}
	

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public TaskInfo getCurrentTaskInfo() {
		return currenTaskInfo;
	}

	public static SecurityService instance;

	// 删除安装包
	public static void packageInstalled(String packageName) {
		try {
			if (delegate != null) {
				delegate.onTaskUpdate(packageName, DOWNLOAD_STATUS_INSTALLED);
				TaskInfo taskInfo = AppDownloadManager.getInstance()
						.getTaskInfoByPackageName(packageName);
				AppDownloadManager.getInstance().delTaskByPackageName(
						packageName);
				CommonUtil.deleteFile(instance, taskInfo.getDownloadUrl());
				// 记录日志
				logPackageInstalled(taskInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void logPackageInstalled(final TaskInfo taskInfo) {
		if (taskInfo != null) {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "app_install_success");
			params.put("app_id", taskInfo.getTaskId() + "");

			String salt = "b2o3x4m5a6t7e";

			int t = (int) (new Date().getTime() / 1000);

			String signature = DataUtil.md5(taskInfo.getTaskId()+""+t+salt);
			params.put("t", t + "");
			params.put("signature", signature);

			String api = HttpCommon.buildApiUrl(params);

			HttpCommon.getApi(api, new HttpSuccessInterface() {
				@Override
				public void run(String result) {
					// TODO Auto-generated method stub
					Log.i("安装成功统计", result);
				}
			});
		}
	}

	public void continueDownload() {
		Log.i(TAG, "正在续传");
		try {
			//须判断是否有下载任务处于运行之中
			mDownloadQueue = new ArrayList<TaskInfo>();
			// 加载未完成列表
			List<TaskInfo> unfinishList = AppDownloadManager.getInstance()
					.getTaskList();
			if (unfinishList.size() > 0) {
				for (TaskInfo taskInfo : unfinishList) {
					if (taskInfo.status!=TaskInfo.SUCCESS) {
						CommonUtil.deleteFile(getApplicationContext(),
								taskInfo.getDownloadUrl());
						taskInfo.setProgress(0);
						taskInfo.status = TaskInfo.WAITING;
						addTaskInQueue(taskInfo);	
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Boolean checkPackageExistInQueue(String packageName) {
		// 判断是否在队列里
		Boolean exist = false;
		for (TaskInfo taskInfo : mDownloadQueue) {
			if (taskInfo.getPackageName().equals(packageName)) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	public Boolean addTaskInQueue(TaskInfo ti) {
		

		if (checkPackageExistInQueue(ti.getPackageName())) {// 判断若有正在运行中，则只加队列不开始
			return false;
		} else {
			Log.i("新增任务", ti.getPackageName());

			if (mDownloadQueue != null) {
				mDownloadQueue.add(ti);
				Log.d(TAG, "addTaskInQueue id = " + ti.getTaskId());
			}

			AppDownloadManager.getInstance().addTaskInfo(ti);

			try {
				if (mDownloadQueue.size() > 0) {
					startDownload();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}

	}

	public void setCurrentTaskStatus(int status) {
		try {
			currenTaskInfo.status = status;
			AppDownloadManager.getInstance().setStatusByTaskId(status,
					currenTaskInfo.getTaskId());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void startDownload() {
		
		if (mDownloadQueue != null && mDownloadQueue.size() > 0) {

			isDownloading=true;
			currenTaskInfo = mDownloadQueue.get(0);

			Log.i("正在下载", currenTaskInfo.toString());
			String downloadUrl = currenTaskInfo.getDownloadUrl();

			if (currenTaskInfo.status == TaskInfo.RUNNING) {
				// 若正在运行中 则返回，否则队列中下一位开启下载
				return;
			}

			if (CommonUtil.isApkExist(getApplicationContext(),
					currenTaskInfo.getDownloadUrl())) {// 若安装包已存在
				Log.i(TAG, "安装包已存在 直接安装");
				currenTaskInfo.setProgress(100);
				sendMsg(DOWNLOAD_STATUS_SUCCESS);
				setCurrentTaskStatus(TaskInfo.SUCCESS);
				mDownloadQueue.remove(currenTaskInfo);
				return;
			}

			String savePath = getFilesDir() + "/"
					+ DataUtil.md5(currenTaskInfo.getDownloadUrl()) + ".temp.apk";
			Log.i("savePath", savePath);
			HttpHandler<File> downloadHandler = th.download(downloadUrl,
					savePath, true, new AjaxCallBack<java.io.File>() {

						@Override
						public void onLoading(long count, long current) {

							Log.i("process=", (int) (current * 100 / count)
									+ "");
							currenTaskInfo
									.setProgress((int) (current * 100 / count));
							currenTaskInfo.setDownloadSize(current);
							currenTaskInfo.setFileSize(count);
							if (delegate != null) {
								delegate.onTaskUpdate(
										currenTaskInfo.getPackageName(),
										DOWNLOAD_STATUS_UPDATE);
							}
							super.onLoading(count, current);
						}

						@Override
						public void onSuccess(File t) {
							Log.i(currenTaskInfo.getPackageName(), "下载成功");
							

							if (delegate != null) {
								delegate.onTaskUpdate(
										currenTaskInfo.getPackageName(),
										DOWNLOAD_STATUS_SUCCESS);
							}
							setCurrentTaskStatus(TaskInfo.SUCCESS);
							mDownloadQueue.remove(currenTaskInfo);
							Log.i(currenTaskInfo.getPackageName(), "已自下载队列移除");
							
							//改名
							t.renameTo(new File(getFilesDir()+"/"+DataUtil.md5(currenTaskInfo.getDownloadUrl())+".apk"));
							sendMsg(DOWNLOAD_STATUS_SUCCESS);
							super.onSuccess(t);

						}

						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							Log.e(currenTaskInfo.getPackageName(), "下载失败");


							if (delegate != null) {
								delegate.onTaskUpdate(
										currenTaskInfo.getPackageName(),
										DOWNLOAD_STATUS_FAILED);
							}

							setCurrentTaskStatus(TaskInfo.FAILED);
							mDownloadQueue.remove(currenTaskInfo);
							sendMsg(DOWNLOAD_STATUS_FAILED);
						};

					});

			// 保存线程
			currenTaskInfo.downloadHandler = downloadHandler;
			// 任务状态设为运行中
			setCurrentTaskStatus(TaskInfo.RUNNING);

		}

	}

	private void sendMsg(int msgType) {
		Message msg = mHandler.obtainMessage(msgType);
		TaskInfo taskInfo = currenTaskInfo.clone();
		msg.obj = taskInfo;
		msg.sendToTarget();
	}

	public void cancelTaskById(int id) {
		
		try {
			Log.d(TAG, "cancelTaskById id = " + id);
			for (int i = 0; i < mDownloadQueue.size(); i++) {
				TaskInfo ti = mDownloadQueue.get(i);
				
				if (ti.getTaskId() == id) {
					if (ti.downloadHandler != null) {//关闭线程
						ti.downloadHandler.stop();
						ti.downloadHandler.cancel(true);
					}
					ti.status = TaskInfo.CANCELED;
					mDownloadQueue.remove(i);
					// 下一项开始下载
					startDownload();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}


	private void keepAlive() {
		// 确保服务处于激活状态
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					mHandler.sendEmptyMessage(ALIVE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, CIRCLE);
	}
}
