package com.boxmate.tv.background;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.SystemUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateCheckService extends Service {
	private String TAG = "UpdateCheckService";
	private ServiceBinder mBinder;
	public final static int NEW_VIDEO_LAUNCHER = 0;
	private List<String> runningVideoListBefore;
	private Boolean initRunningList;
	private final static int CIRCLE = 1 * 1000;
	private VideoObserver videoObserver;
	private HashMap<String, AppInfo> appList = new HashMap<String, AppInfo>();

	public class ServiceBinder extends Binder {
		public UpdateCheckService getService() {
			return UpdateCheckService.this;
		}
	}

	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
		String SYSTEM_REASON = "reason";
		String SYSTEM_HOME_KEY = "homekey";
		String SYSTEM_HOME_KEY_LONG = "recentapps";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_REASON);
				if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
					freshUpdate();
				} else if (TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate callded");

		if (CommonUtil.context == null) {
			CommonUtil.context = getApplicationContext();
		}

		AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, this.getClass());
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		long triggerAtTime = SystemClock.elapsedRealtime();
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				3 * 1000, pendingIntent);

		SharedPreferences sp = getSharedPreferences(Config.SETTING,
				Context.MODE_PRIVATE);
		if (sp.getBoolean(Config.SETTING_VIDEO_FLAG, false)) {
			startVideoObserver();
		}

		super.onCreate();

		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
		IntentFilter filter = new IntentFilter();
		filter.addAction(Config.INTENT_VIDEO_OBSERVER_OPEN);
		filter.addAction(Config.INTENT_VIDEO_OBSERVER_CLOSE);
		registerReceiver(mBroadcastReceiver, filter);

		// freshUpdate();
	}
	
	private void startVideoObserver(){
		videoObserver = VideoObserver.getInstance(getApplicationContext());
	}

	

	private Handler appLoadedHandler = new Handler() {
		public void handleMessage(Message msg) {

			HashMap<String, String> infHashMap = (HashMap<String, String>) msg.obj;

			String api = Config.appBase
					+ "action=get_app_list_by_package_names";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("packages", infHashMap
					.get("package")));
			params.add(new BasicNameValuePair("versions", infHashMap
					.get("version")));

			HttpCommon.postApi(api, params, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					JSONArray updateJsonArray;
					try {
						updateJsonArray = new JSONArray(result);

						ArrayList<HashMap<String, String>> updateList = new ArrayList<HashMap<String, String>>();

						for (int i = 0; i < updateJsonArray.length(); i++) {
							JSONObject appJson = (JSONObject) updateJsonArray
									.opt(i);
							HashMap<String, String> appData = new HashMap<String, String>();
							appData.put("title", appJson.getString("title"));
							appData.put("package", appJson.getString("package"));
							updateList.add(appData);
						}
						if (updateList.size() > 0) {
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	};

	public void freshUpdate() {
		
		
		
		
		final SharedPreferences spSetting=getSharedPreferences(Config.SETTING, Context.MODE_MULTI_PROCESS);
		Boolean appUpdateTipsOpen = spSetting.getBoolean(Config.SETTING_UPDATE_HINT_FLAG, true);
		if(!appUpdateTipsOpen) {
			Log.i(TAG,"未开启弹窗");
			return ;
		}

		final SharedPreferences sp = getSharedPreferences("update_check",
				MODE_PRIVATE);

		final int last_check_time = sp.getInt("last_check_time", 0);

		int cTime = (int) (new Date().getTime() / 1000);

		Log.i(TAG, "最后检查时间:" + last_check_time + ",锟斤拷前:" + cTime);

		if (cTime - last_check_time < (24 * 3600)) {
			return;
		} else {

			Editor editor = sp.edit();
			editor.putInt("last_check_time", cTime);
			editor.commit();

			if (last_check_time == 0) {
				return;
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				List<PackageInfo> packages = getPackageManager()
						.getInstalledPackages(0);
				String packagesString = "";
				String versionCodeString = "";
				appList = new HashMap<String, AppInfo>();
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo packageInfo = packages.get(i);
					AppInfo appInfo = new AppInfo();
					appInfo.appicon = packageInfo.applicationInfo
							.loadIcon(getPackageManager());
					appInfo.appname = packageInfo.applicationInfo.loadLabel(
							getPackageManager()).toString();
					appInfo.packagename = packageInfo.packageName;
					appInfo.versionName = packageInfo.versionName;
					appInfo.versionCode = packageInfo.versionCode;

					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {

						appList.put(appInfo.packagename, appInfo);
						packagesString += appInfo.packagename + "|";
						versionCodeString += appInfo.versionCode + "|";
					}

				}

				Message message = appLoadedHandler.obtainMessage();
				HashMap<String, String> infoHashMap = new HashMap<String, String>();
				infoHashMap.put("package", packagesString);
				infoHashMap.put("version", versionCodeString);
				message.obj = infoHashMap;
				message.sendToTarget();
			}
		}).start();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "alive");

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy callded");
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Config.INTENT_VIDEO_OBSERVER_OPEN)) {
				startVideoObserver();
			} else if (intent.getAction().equals(
					Config.INTENT_VIDEO_OBSERVER_CLOSE)) {
				if (videoObserver!=null) {
					videoObserver.stopVideoObserver();
				}
			}

		};
	};
}