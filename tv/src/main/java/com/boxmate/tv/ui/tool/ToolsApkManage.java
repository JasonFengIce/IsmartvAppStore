package com.boxmate.tv.ui.tool;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvListView;

import com.boxmate.tv.R;
import com.boxmate.tv.adapter.ApkListAdapter;
import com.boxmate.tv.entity.ApkInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.FileNameFilterCustom;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ToolsApkManage extends Activity {

	private final String TAG = "ToolsApkManage";
	private final static int SEARCH_FINISH = 0;
	private final static int DELETE_SUCCESS = 1;
	private final static int SHOW_PATH = 2;
	private int DELAY = 1300, DELETE_DELAY = 700;
	private List<ApkInfo> apkList;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SEARCH_FINISH:
				tv_path.setVisibility(View.GONE);
				tv_searching.setVisibility(View.GONE);
				iv_apk.setVisibility(View.GONE);
				if (apkList != null && apkList.size() > 0) {
					ApkListAdapter mAdapter = new ApkListAdapter(
							getApplicationContext(), apkList);
					tlv_apklist.setAdapter(mAdapter);
					tb_install.setVisibility(View.VISIBLE);
				} else {
					tv_result.setText(getResources().getString(
							R.string.tools_apk_noapk));
				}
				break;
			case DELETE_SUCCESS:
				tb_install.setVisibility(View.GONE);
				tlv_apklist.setVisibility(View.GONE);
				tv_result.setText(getResources().getString(
						R.string.tools_apk_noapk));
				break;
			case SHOW_PATH:
				String path = (String) msg.obj + "";
				if (path.length() > 31) {
					path = path.substring(0, 13) + "..."
							+ path.substring(path.length() - 17);
				}
				tv_path.setText(path);
				break;
			}
		};
	};

	private PackageManager pm;
	private TvListView tlv_apklist;
	private TvButton tb_install;
	private TextView tv_result;
	private final static String SUFFIX_APK = ".apk";
	private TextView tv_path;
	private TextView tv_searching;
	private ImageView iv_apk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_apk);
		init();
		new Thread() {

			public void run() {
				searchApk(CommonUtil.getSDPath(), 0);
				searchApk(getFilesDir().getPath(), 0);
				Message msg = mHandler.obtainMessage();
				msg.what = SEARCH_FINISH;
				mHandler.sendMessageDelayed(msg, DELAY);
			};
		}.start();

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);
	}

	private void init() {
		DELAY = 1700;
		pm = getPackageManager();
		tlv_apklist = (TvListView) findViewById(R.id.tlv_apklist);
		tv_searching = (TextView) findViewById(R.id.tv_searching);
		iv_apk = (ImageView) findViewById(R.id.iv_apk);
		tv_result = (TextView) findViewById(R.id.tv_result);
		tv_path = (TextView) findViewById(R.id.tv_path);
		apkList = new ArrayList<ApkInfo>();

		tb_install = (TvButton) findViewById(R.id.tb_install);
		tb_install.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new Thread() {
					public void run() {
						try {
							for (ApkInfo apk : apkList) {
								CommonUtil.deleteFileByPath(
										getApplicationContext(), apk.getPath());
							}
						} catch (ConcurrentModificationException e) {
							e.printStackTrace();
						}

						Message msg = mHandler.obtainMessage();
						msg.what = DELETE_SUCCESS;
						mHandler.sendMessageDelayed(msg, DELETE_DELAY);
					};
				}.start();

			}
		});
	}

	private int searchApk(String dir, int acc) {

		//Log.d(TAG, "dir=" + dir);

		try {
			File[] fileList = new File(dir).listFiles();
			if (fileList == null) {
				return acc;
			}

			if (fileList.length > 50) {
				DELAY -= 300;
			}
			for (File file : fileList) {

				//Log.i(TAG, file.getAbsolutePath());
				String filePath = file.getAbsolutePath();
				if (file.isDirectory()) {
					searchApk(filePath, (acc + 1));
				} else if (filePath.endsWith(SUFFIX_APK)) {
					ApkInfo apk = readApk(file.getAbsolutePath(), file.length());
					if (apk != null
							&& !AppDownloadManager.getInstance()
									.checkTaskInfoByPackageName(
											apk.getPackageName())) {
						apkList.add(apk);
					}
				}
				Message msg = mHandler.obtainMessage();
				msg.what = SHOW_PATH;
				msg.obj = file.getAbsolutePath();
				mHandler.sendMessage(msg);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return acc;
	}

	private ApkInfo readApk(String apkPath, long fileSize) {

		try {
			ApkInfo apk = new ApkInfo();
			PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			if (appInfo != null) {
				appInfo.sourceDir = apkPath;
				appInfo.publicSourceDir = apkPath;// 此处以确保资源正确读取
				apk.setName(pm.getApplicationLabel(appInfo).toString());
				apk.setPackageName(pkgInfo.packageName);
				apk.setPath(apkPath);
				apk.setVersion(pkgInfo.versionName);
				apk.setSizeString(DataUtil.parseApkSize(fileSize));
			}
			//Log.i(TAG, apk.toString());
			return apk;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
