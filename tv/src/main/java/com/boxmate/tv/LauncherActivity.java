package com.boxmate.tv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvBitmap;

import com.boxmate.tv.R;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.MainActivity;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.SystemUtil;
import com.boxmate.tv.view.WebImageView;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LauncherActivity extends Activity {

	private final static String TAG = "LauncherActivity";
	private final static int ADB_INSTALL_SUCCESS = 0;
	public static String dbFilePath;
	private TvBitmap tb;
	private Boolean isDestroyed = false;
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case ADB_INSTALL_SUCCESS:
				DataUtil.saveAdbState(getApplicationContext(), true);
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getIntentData();
		init();
		if (Config.PAGE_INDEX != Config.PAGE_HOME) {
			jump();
		} else {
			setContentView(R.layout.activity_launcher);
			loadData();
		}
	}

	private void init() {

		// 初始赋值
		this.tb = TvBitmap.create(getApplicationContext());
		if (getCacheDir() != null) {
			tb.configDiskCachePath(getCacheDir().getPath());
		}
		dbFilePath = getFilesDir().getAbsolutePath();
		CommonUtil.context = getApplicationContext();
		// 启动服务
		Intent serviceIntent = new Intent(this, SecurityService.class);
		startService(serviceIntent);
		CommonUtil.loadInstalledApp();
		// 获取视频包名列表
		loadVideoPkgList();
	}

	private Boolean hasLastLauncher = true;

	private void loadData() {

		final SharedPreferences sp = getSharedPreferences("launcher_setting",
				MODE_PRIVATE);
		final String launcher_url = sp.getString("launcher_url", "none");
		Log.i("launcher_url", launcher_url);
		final WebImageView imageView = (WebImageView) findViewById(R.id.launcher_bg);
		imageView.showLoading = false;
		if (launcher_url.equals("none")) {
			hasLastLauncher = false;
			
			final  Handler drawableHandler = new Handler() {
				public void handleMessage(Message msg) {
					try {
						imageView.setImageDrawable((Drawable) msg.obj);
					} catch (Exception e) {
					}
				};
			};
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Drawable drawable = getResources().getDrawable(R.drawable.launcher_bg);
					Message msg = drawableHandler.obtainMessage();
					msg.obj = drawable;
					msg.sendToTarget();
				}
			}).start();
		} else {
			tb.displayHD(imageView, launcher_url);
		}
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "get_product_by_package");
		params.put("package", "com.boxmate.tv");
		String api = HttpCommon.buildApiUrl(params);
		Log.i("launcher", api);
		final Handler loadedHandler = new Handler() {
			public void handleMessage(Message msg) {
				String result = (String) msg.obj;
				if (result.equals("NetError")) {

					new Handler().postDelayed(new Runnable() {
						public void run() {
							imageView.setVisibility(View.GONE);
							findViewById(R.id.tv_disconnect).setVisibility(
									View.VISIBLE);

							new Handler().postDelayed(new Runnable() {
								public void run() {
									LauncherActivity.this.finish();
								}
							}, 3000);

						}
					}, 2000);

					return;
				}

				try {
					final JSONObject app = new JSONObject(result);
					Editor editor = sp.edit();
					editor.putString("launcher_url",
							app.getString("launcher_url"));
					editor.commit();

					if (!hasLastLauncher) {
						// imageView.setImageUrl(app.getString("launcher_url"));
					} else if (!app.getString("launcher_url").equals(
							launcher_url)) {

						new Thread(new Runnable() {
							public void run() {
								try {
									CommonUtil.getDrawableByUrl(
											app.getString("launcher_url"),
											LauncherActivity.this);
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}).start();
					}
					new Handler().postDelayed(new Runnable() {
						public void run() {
							jump();
						}

					}, 1000);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		};
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpGet request = new HttpGet(api);
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					HttpResponse response = httpClient.execute(request);
					String result = EntityUtils.toString(response.getEntity());
					Message msg = loadedHandler.obtainMessage();
					msg.obj = result;
					msg.sendToTarget();
				} catch (ClientProtocolException e) {
					Message msg = loadedHandler.obtainMessage();
					msg.obj = "NetError";
					msg.sendToTarget();
					e.printStackTrace();
					return;
				} catch (IOException e) {
					Message msg = loadedHandler.obtainMessage();
					msg.obj = "NetError";
					msg.sendToTarget();
					e.printStackTrace();
					return;
				}
			}
		}).start();

		try {

			SharedPreferences setting = getSharedPreferences(Config.SETTING,
					Context.MODE_PRIVATE);
			// 初始化配置
			if (setting.getBoolean(Config.SETTING_FIRST_LAUNCHER, true)) {
				Log.i(TAG, "初始化");
				firstLauncher();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getIntentData() {
		try {
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				String jumpParam = bundle.getString(Config.JUMP_PARAM);
				if (jumpParam == null || "".equals(jumpParam)) {
					Config.PAGE_INDEX = Config.PAGE_HOME;
				} else {
					for (int i = 0; i < Config.params.length; i++) {
						if (Config.params[i].equalsIgnoreCase(jumpParam)) {
							Config.PAGE_INDEX = i;
							break;
						}
					}
				}
				Log.i(TAG, "jumpParam=" + jumpParam + "");
			}

		} catch (Exception e) {
			Config.PAGE_INDEX = Config.PAGE_HOME;
			e.printStackTrace();
		}

	}

	private void jump() {

		if(isDestroyed) {
			return;
		}

		Intent mainIntent = new Intent(LauncherActivity.this,
				MainActivity.class);
		LauncherActivity.this.startActivity(mainIntent);
		LauncherActivity.this.finish();
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	/**
	 * 首次安装启动
	 */
	private void firstLauncher() {

		SharedPreferences sp = getSharedPreferences(Config.SETTING,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		// 判断设备是否允许后台进程
		if (SystemUtil.checkBackgroundAvailable()) {
			editor.putBoolean(Config.SETTING_BACKGROUND_FLAG, true);
		} else {
			editor.putBoolean(Config.SETTING_BACKGROUND_FLAG, false);
		}

		editor.putBoolean(Config.SETTING_FIRST_LAUNCHER, false);
		editor.commit();

	}


	private void loadVideoPkgList() {

		try {

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "get_common_video_package");
			ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
					getPackageName(), PackageManager.GET_META_DATA);
			String channel = appInfo.metaData.getString("UMENG_CHANNEL");
			params.put("channel", channel);
			String api = HttpCommon.buildApiUrl(params);
			Log.i("check api", api);
			HttpCommon.getApi(api, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					JSONArray pkgArray;
					try {
						List<String> pkgList = new ArrayList<String>();
						pkgArray = new JSONArray(result);
						for (int i = 0; i < pkgArray.length(); i++) {
							pkgList.add(pkgArray.getString(i));
						}
						DataUtil.saveStringList(getApplicationContext(),
								pkgList, Config.VIDEO_PKG_LIST);

					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}
	protected void onDestroy(){
		super.onDestroy();
		isDestroyed = true;

	}
}
