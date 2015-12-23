package com.boxmate.tv.background;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.boxmate.tv.R;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.SystemUtil;

public class VideoObserver {

	private String TAG = "VideoObserver";
	private static VideoObserver observer;
	private Context context;
	public final static int NEW_VIDEO_LAUNCHER = 0;
	private String topPkgBefore;
	private Boolean initRunningList;// 避免销毁重启出错
	private Boolean videoObserverFlag;// 用于关闭线程
	private final static int CIRCLE = 1 * 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case NEW_VIDEO_LAUNCHER:
				String pkg = (String) msg.obj;
				String appName = SystemUtil.getAppNameByPkg(context, pkg);
				Log.i(TAG, "appName=" + appName + "---" + initRunningList);
				// 判断当前启是否属于视频
				if (checkAppType(pkg)) {
					showSpeedToast(appName);
					videoSpeed(pkg);
				}
				break;

			}
		}
	};

	private VideoObserver(Context context) {
		this.context = context;
		this.videoObserverFlag = true;
		startVideoObserver();
	}

	public static VideoObserver getInstance(Context context) {
		if (observer == null) {
			synchronized (VideoObserver.class) {
				if (observer == null) {
					observer = new VideoObserver(context);
				}
			}
		}
		return observer;
	}

	/**
	 * 启动视频加速监听
	 */
	public void startVideoObserver() {
		Log.i(TAG, "正在启动视频启监听！！！");
		initRunningList = true;
		topPkgBefore = "";
		// 确保服务处于激活状态
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

				if (!videoObserverFlag) {
					return;
				}
				try {
					List<String> runningVideoList = SystemUtil
							.quaryAllRunningPkg(context,
									DataUtil.getAppList(context));
					// 判断是否是当天首次置顶 成功启动保存日期
					for (String pkg : runningVideoList) {

						if (SystemUtil.isTopActivity(context, pkg)&&checkPkg(pkg)) {
							// 计算上次启动时间差，小于1小时则跳出 大于1小时则加速
							// if
							// (SystemUtil.countHoursBeforeLastLauncher(context,
							// pkg)) {
							// Log.e(TAG, "7小时首次启动");
							// Message msg = mHandler.obtainMessage();
							// msg.what = NEW_VIDEO_LAUNCHER;
							// msg.obj = pkg;
							// mHandler.sendMessage(msg);
							// break;
							// }
							//topPkgBefore = pkg;
							
							Message msg = mHandler.obtainMessage();
							msg.what = NEW_VIDEO_LAUNCHER;
							msg.obj = pkg;
							mHandler.sendMessage(msg);
							break;
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, CIRCLE);
	}

	public void stopVideoObserver() {
		videoObserverFlag = false;
	}

	/**
	 * 杀死当前视频进程之外所有进程
	 * 
	 * @param pkg
	 */
	private void videoSpeed(final String pkg) {
		new Thread() {
			public void run() {

				try {
					SystemUtil.killRunningTask(context, pkg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();

	}

	/**
	 * 判断当前包名是否属于视频
	 * 
	 * @param pkg
	 * @return
	 */
	private Boolean checkAppType(String pkg) {
		try {

			List<String> videoPkgList = DataUtil.readStringList(context,
					Config.VIDEO_PKG_LIST);
			for (String videoPkg : videoPkgList) {
				if (pkg.equals(videoPkg)) {
					return true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 显示右下角加速提醒
	 * 
	 * @param appName
	 */
	private void showSpeedToast(String appName) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.toast_video_speed, null);
		TextView tv_appname = (TextView) layout.findViewById(R.id.tv_appname);
		tv_appname.setText(appName
				+ " "
				+ context.getResources().getString(
						R.string.tools_video_speed_hint));
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.RIGHT | Gravity.BOTTOM, 10, 10);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}
	
	/**
	 * 检查当前置顶包名与上次置顶包名雷同
	 * @param context
	 * @return
	 */
	public Boolean checkPkg(String pkg){
		
		try {
			SharedPreferences sp=context.getSharedPreferences(Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			String lastPkg=sp.getString(Config.LAST_TOP_PKG, "");
			Editor ed=sp.edit();
			//Log.e(TAG, "首次启动"+pkg+"---"+lastPkg);
			if (pkg.equals(lastPkg)) {
				return false;
			}else{
				ed.putString(Config.LAST_TOP_PKG, pkg);
				ed.commit();
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return false;
	}

}
