package com.boxmate.tv.ui.manage;

import java.io.File;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import com.boxmate.tv.R;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.VersionInfo;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.tool.ToolsBigFileManage;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.SystemUtil;
import com.umeng.analytics.MobclickAgent;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SettingActivity extends Activity {

	private final static String TAG = "SettingActivity";
	private final static int DESK_SPEED_OPEN = 0;
	private final static int DESK_SPEED_CLOSE = 1;
	private VersionInfo version;
	private Boolean videoFlag, updateHintFlag, upgradeFlag;
	private Boolean adbFlag, adbIsOpen = false;// ADB是否可用 adb是否打开
	private Boolean isApkExist = false;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case DESK_SPEED_OPEN:
				SharedPreferences sp1 = getSharedPreferences(Config.SETTING,
						Context.MODE_PRIVATE);
				Editor editor1 = sp1.edit();
				editor1.putBoolean(Config.SETTING_DESK_SPEED_FLAG, true);
				editor1.commit();
				break;

			case DESK_SPEED_CLOSE:
				SharedPreferences sp2 = getSharedPreferences(Config.SETTING,
						Context.MODE_PRIVATE);
				Editor editor2 = sp2.edit();
				editor2.putBoolean(Config.SETTING_DESK_SPEED_FLAG, false);
				editor2.commit();
				break;
			}
		};
	};
	private String savePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_setting);
	}

	@Override
	protected void onStart() {
		init();
		checkFile();
		super.onStart();
	}

	@Override
	protected void onResume() {
		// 刷新状态
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void init() {

		tv_video_speed_flag = (TextView) findViewById(R.id.tv_video_speed_flag);
		tv_update_hint_flag = (TextView) findViewById(R.id.tv_update_hint_flag);
		tv_upgrade_flag = (TextView) findViewById(R.id.tv_upgrade_flag);

		// 视频加速
		videoFlag = DataUtil.readVideoSpeed(getApplicationContext());
		// 应用更新提示
		updateHintFlag = DataUtil.readUpdateHint(getApplicationContext());

		// 桌面加速工具
		isApkExist = false;
		savePath = getFilesDir().getAbsolutePath() + "/"
				+ Config.TV_ClEAN_APK_NAME;
		
		
		
		adbFlag = DataUtil.readAdbState(getApplicationContext());
		checkAdbState();


		if (videoFlag) {
			tv_video_speed_flag.setText(getResources().getString(
					R.string.setting_state_opened));
		} else {
			tv_video_speed_flag.setText(getResources().getString(
					R.string.setting_state_closed));
		}

		if (updateHintFlag) {
			tv_update_hint_flag.setText(getResources().getString(
					R.string.setting_state_opened));
		} else {
			tv_update_hint_flag.setText(getResources().getString(
					R.string.setting_state_closed));
		}

		findViewById(R.id.trl_video_speed).setOnClickListener(mClickListener);
		findViewById(R.id.trl_update_hint).setOnClickListener(mClickListener);
		findViewById(R.id.trl_upgrade).setOnClickListener(mClickListener);
		findViewById(R.id.trl_about).setOnClickListener(mClickListener);

		checkUpdate();

	}


	private void checkFile() {
		try {
			// 检查APK文件是否存在
			File file = new File(savePath);

			if (!file.exists()) {
				loadFile();
			} else {
				isApkExist = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取桌面清理APK
	 * 
	 * @return
	 */
	private void loadFile() {

		Log.i("t=", "正在联网获取桌面清理APK");
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "get_product_by_package");
		params.put("package", Config.TV_ClEAN_PKG);
		String api = HttpCommon.buildApiUrl(params);
		HttpCommon.getApi(api, new HttpSuccessInterface() {

			@Override
			public void run(String result) {
				try {
					JSONTokener jsonParser = new JSONTokener(result);
					JSONObject jsonObj = (JSONObject) jsonParser.nextValue();
					String apkUrl = jsonObj.getString("apk_url");

					new TvHttp(getApplicationContext()).download(apkUrl,
							savePath,
							new reco.frame.tv.http.AjaxCallBack<File>() {

								public void onSuccess(File t) {

									Log.i("t=", "成功获取桌面清理APK");
									isApkExist = true;
								};
							});

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

	private void removeDeskSpeed() {
		new Thread() {
			public void run() {
				if (adbFlag && adbIsOpen) {
					SystemUtil.adbDisConnect(Config.LOCAL_HOST);
					SystemUtil.adbConnect(Config.LOCAL_HOST);
					SystemUtil.uninstallApp(Config.TV_ClEAN_PKG,
							Config.LOCAL_HOST);
					mHandler.sendEmptyMessage(DESK_SPEED_CLOSE);
				} else {
					CommonUtil.uninstallApk(getApplicationContext(),
							Config.TV_ClEAN_PKG);
				}
			};
		}.start();

	}

	private void checkUpdate() {
		version = new VersionInfo();
		String pkName = getPackageName();
		try {
			final int versionCode = getPackageManager().getPackageInfo(pkName,
					0).versionCode;

			TvHttp http = new TvHttp(this);
			http.get(Config.UPDATE_URL + getPackageName(),
					new AjaxCallBack<Object>() {
						@Override
						public void onSuccess(Object t) {
							JSONObject infoJsonObject;
							try {
								// Log.e(TAG, t.toString());
								infoJsonObject = new JSONObject(t.toString());
								int onlineVersionCode = infoJsonObject
										.getInt("version_code");
								String onlineVersonName = infoJsonObject
										.getString("version_name");
								String desc = infoJsonObject.getString("desc");
								String apkUrl = infoJsonObject
										.getString("apk_url");
								Log.i("version", onlineVersionCode + "");
								if (onlineVersionCode > versionCode) {
									tv_upgrade_flag
											.setText(getResources()
													.getString(
															R.string.setting_upgrade_right_yes));
									version.setNewVersion(true);
								} else {
									tv_upgrade_flag
											.setText(getResources()
													.getString(
															R.string.setting_upgrade_right_no));
									version.setNewVersion(false);
								}
								version.setVersionName(onlineVersonName);
								version.setApkUrl(apkUrl);
								version.setDesc(desc);
							} catch (JSONException e) {
								e.printStackTrace();
							}
							super.onSuccess(t);
						}
					});

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void installDeskSpeed() {
		new Thread() {
			public void run() {
				if (adbFlag && adbIsOpen) {
					SystemUtil.adbDisConnect(Config.LOCAL_HOST);
					SystemUtil.adbConnect(Config.LOCAL_HOST);
					SystemUtil.installApp(getFilesDir() + "/"
							+ Config.TV_ClEAN_APK_NAME, Config.LOCAL_HOST);
					mHandler.sendEmptyMessage(DESK_SPEED_OPEN);
				} else {
					CommonUtil.installApkByFilePath(getApplicationContext(),
							getFilesDir() + "/" + Config.TV_ClEAN_APK_NAME);
				}
			};
		}.start();

	}

	/**
	 * 检查当前ADB
	 */
	private void checkAdbState() {
		new Thread() {
			public void run() {
				if (adbFlag) {
					SystemUtil.adbDisConnect(Config.LOCAL_HOST);
					if (SystemUtil.adbConnect(Config.LOCAL_HOST)) {
						adbIsOpen = true;
					}
				}
			};
		}.start();

	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {
			case R.id.trl_video_speed:

				if (videoFlag) {
					videoFlag = false;
					tv_video_speed_flag.setText(getResources().getString(
							R.string.setting_state_closed));
					sendBroadcast(new Intent(Config.INTENT_VIDEO_OBSERVER_CLOSE));

				} else {
					videoFlag = true;
					tv_video_speed_flag.setText(getResources().getString(
							R.string.setting_state_opened));
					sendBroadcast(new Intent(Config.INTENT_VIDEO_OBSERVER_OPEN));
				}
				DataUtil.saveVideoSpeed(getApplicationContext(), videoFlag);

				break;
			case R.id.trl_update_hint:

				if (updateHintFlag) {
					tv_update_hint_flag.setText(getResources().getString(
							R.string.setting_state_closed));
					updateHintFlag = false;
				} else {
					tv_update_hint_flag.setText(getResources().getString(
							R.string.setting_state_opened));
					updateHintFlag = true;
				}
				break;
			case R.id.trl_upgrade:
				if (version.isNewVersion()) {
					Intent update = new Intent(SettingActivity.this,
							VersionUpdateActivity.class);
					update.putExtra("online_version", version.getVersionName());
					update.putExtra("apk_url", version.getApkUrl());
					update.putExtra("update_info", version.getDesc());
					startActivity(update);
				}
				break;
			case R.id.trl_about:
				Intent about = new Intent(SettingActivity.this,
						AboutActivity.class);
				startActivity(about);
				break;

			}

		}
	};
	private TextView tv_video_speed_flag;
	private TextView tv_update_hint_flag;
	private TextView tv_upgrade_flag;

}
