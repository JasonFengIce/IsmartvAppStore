package com.boxmate.tv.ui;

import java.util.ArrayList;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvBitmap;
import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.TvProgressBar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;

import com.boxmate.tv.LauncherActivity;
import com.boxmate.tv.R;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.entity.AppWebInfo;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.view.AppListBottomButton;
import com.boxmate.tv.view.StarList;
import com.umeng.analytics.MobclickAgent;

public class AppDetail extends Activity implements
		SecurityService.OnDownloadStatusChanaged,
		AppListBottomButton.OnButtonFocusChangeListener {

	private final static String TAG = "AppDetail";
	private int appId;
	private AppWebInfo appInfo = null;
	private HttpGet request = null;
	private TaskInfo taskInfo;
	private TvProgressBar tpb_download;
	public static int PREVIEW_STATUS = 0;
	public static int DESCRIPT_STATUS = 1;
	public static int RELATIVE_STATUS = 2;
	public Boolean bottomFocus = false;
	public int curPage = PREVIEW_STATUS;
	public Fragment curFragment;
	public Boolean rightFocus = false;
	private AppDownloadManager manager;
	private TvBitmap tb;

	public interface OnPressOkListener {
		public void onPress();

		public void onFocusChange(Boolean isFocus);
	}

	private Handler notiHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (tpb_download == null || taskInfo == null) {
				return;
			}

			if (msg.what == SecurityService.DOWNLOAD_STATUS_SUCCESS) {
				// 下载完成
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 进程遭强杀后返回，防数据库路径为空
		if (LauncherActivity.dbFilePath == null) {
			LauncherActivity.dbFilePath = getFilesDir().getAbsolutePath();
		}
		this.tb = TvBitmap.create(getApplicationContext());
		setContentView(R.layout.activity_app_detail);

		Intent intent = getIntent();
		appId = Integer.parseInt(intent.getStringExtra("app_id"));
		Intent intentService = new Intent(this, SecurityService.class);
		this.startService(intentService);

		manager = AppDownloadManager.getInstance();
		taskInfo = manager.getTaskInfoByAppId(appId);

		loadAppInfo();
	}

	@Override
	protected void onStart() {
		// 设定依赖
		SecurityService.delegate = this;
		super.onStart();
	}

	protected void onResume() {
		// 刷新状态 改为打开 或 安装
		try {
			fresh();
		} catch (Exception e) {
		}
		super.onResume();
		MobclickAgent.onResume(this);
	}

	private void fresh() {

		if (appInfo == null)
			return;

		TvButton tb_install = (TvButton) findViewById(R.id.tb_install);
		tpb_download = (TvProgressBar) findViewById(R.id.tpb_download);

		// 判断APP下载状态 解析错误 错误文件删除

		taskInfo = manager.getTaskInfoByAppId(appId);

		Log.i(TAG, "1=" + taskInfo.status);
		if (CommonUtil.isAppInstalled(this, appInfo.packageName)) {
			taskInfo.status = TaskInfo.INSTALLED;
			tpb_download.setProgress(100);
			//tb_install.setBackgroundResource(R.drawable.run_btn);
			tb_install.setText(getResources().getString(R.string.button_open));
		} else if (CommonUtil.isApkExist(getApplicationContext(),
				appInfo.downloadUrl)) {
			Log.i(TAG, "2=" + appInfo.toString());
			taskInfo.status = TaskInfo.SUCCESS;
			tpb_download.setProgress(100);
			//tb_install.setBackgroundResource(R.drawable.install_btn);
			tb_install.setText(getResources().getString(R.string.button_install_now));
		} else if (taskInfo.status == TaskInfo.WAITING
				|| taskInfo.status == TaskInfo.RUNNING) {
			//tb_install.setBackgroundResource(R.drawable.installing_btn);
			tb_install.setText(getResources().getString(R.string.button_install_cancel));
			tpb_download.setProgress(0);
			taskInfo.status = TaskInfo.RUNNING;
			// 启动轮询获取参数
		} else if (taskInfo.status == TaskInfo.SUCCESS) {
			//tb_install.setBackgroundResource(R.drawable.install_btn);
			tb_install.setText(getResources().getString(R.string.button_install_now));
			tpb_download.setProgress(100);
		} else {
			//tb_install.setBackgroundResource(R.drawable.install_btn);
			tb_install.setText(getResources().getString(R.string.button_install_now));
			tpb_download.setProgress(100);
		}
		Log.i(TAG, "2=" + taskInfo.status);
		tb_install.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (appInfo == null)
					return;// 防止加载空下载任务
				Log.i(TAG, "3=" + taskInfo.status);
				switch (taskInfo.status) {
				case TaskInfo.INSTALLED:
					tpb_download.setProgress(100);
					Intent intent = new Intent();
					intent = getPackageManager().getLaunchIntentForPackage(
							appInfo.packageName);
					if (intent != null) {
						startActivityForResult(intent, 0);
					}
					break;

				case TaskInfo.SUCCESS:
					tpb_download.setProgress(100);
					CommonUtil.installApk(appInfo.downloadUrl, AppDetail.this);
					break;

				case TaskInfo.WAITING:
				case TaskInfo.RUNNING:
				case TaskInfo.FAILED:
					try {
						Log.i(TAG, "停止下载");
						tpb_download.setProgress(100);
						manager.delTaskById(getApplicationContext(), taskInfo);
						fresh();
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case TaskInfo.INIT:
					tpb_download.setProgress(0);
					try {

						if (appInfo != null) {
							final TaskInfo ti = new TaskInfo();
							ti.setTaskId(appInfo.appId);
							ti.setTaskName(appInfo.title);
							ti.setProgress(0);
							ti.status = TaskInfo.WAITING;
							ti.setDownloadUrl(appInfo.downloadUrl);
							ti.setPackageName(appInfo.packageName);
							ti.iconUrl = appInfo.iconUrl;
							Log.i("taskInfo=", ti.toString());
							if (SecurityService.instance == null) {
								startService(new Intent(AppDetail.this,
										SecurityService.class));
							}
							SecurityService.instance.addTaskInQueue(ti);
							taskInfo = manager.getTaskInfoByAppId(ti
									.getTaskId());

							//v.setBackgroundResource(R.drawable.installing_btn);
							((TextView)v).setText(getResources().getString(R.string.button_install_cancel));

							String apiUrlString = Config.appBase
									+ "action=app_download_log&id="
									+ appInfo.appId;

							HttpCommon.getApi(apiUrlString,
									new HttpSuccessInterface() {
										@Override
										public void run(String result) {
											// TODO Auto-generated method stub

										}
									});

						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					break;

				}

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(TAG, "返回市场");
		// 防止路径为空
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void loadAppInfo() {

		String apiUrlString = Config.appBase + "action=get_app_info&id="
				+ appId;
		Log.i("详细也api", apiUrlString);

		request = HttpCommon.getApi(apiUrlString, new HttpSuccessInterface() {

			@Override
			public void run(String result) {
				try {
					JSONObject appInfoJsonObject = new JSONObject(result);

					appInfo = new AppWebInfo();
					appInfo.title = appInfoJsonObject.getString("title");
					appInfo.iconUrl = appInfoJsonObject.getString("icon_url");
					appInfo.versionCode = appInfoJsonObject
							.getInt("version_code");
					appInfo.packageName = appInfoJsonObject
							.getString("package");
					appInfo.downloadUrl = appInfoJsonObject
							.getString("download_url");
					appInfo.desc = appInfoJsonObject.getString("desc");
					appInfo.sourceQuality = appInfoJsonObject
							.getInt("official");
					JSONArray imagesJsonArray = appInfoJsonObject
							.getJSONArray("image_urls");

					int lastId = 0;

					RelativeLayout imgsContainer = (RelativeLayout) findViewById(R.id.image_list);

					OnFocusChangeListener focusChangeListener = new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View arg0, boolean isFocus) {
							// TODO Auto-generated method stub
							if (isFocus) {
								int[] location = new int[2];
								arg0.getLocationOnScreen(location);
								int left = location[0];
								int maxWidth = left + arg0.getWidth();

								HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.sreen_shot_list);

								if (maxWidth > scrollView.getWidth()) {
									scrollView.smoothScrollBy(maxWidth
											- scrollView.getWidth() + 100, 0);
								} else if (left < 0) {
									scrollView.smoothScrollTo(0, 0);
								}
							}
						}
					};

					final ArrayList<String> urlList = new ArrayList<String>();
					JSONArray imagesJson = appInfoJsonObject
							.getJSONArray("image_urls_big");
					for (int i = 0; i < imagesJson.length(); i++) {
						urlList.add(imagesJson.opt(i).toString());
					}

					OnClickListener clickListener = new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(AppDetail.this,
									ImageBrowserActivity.class);
							intent.putExtra("index", arg0.getId() - 1000);
							intent.putStringArrayListExtra("urllist", urlList);
							startActivity(intent);
						}
					};

					for (int i = 0; i < imagesJsonArray.length(); i++) {
						appInfo.imageList
								.add(imagesJsonArray.opt(i).toString());

						TvButton cursor = new TvButton(AppDetail.this);
						cursor.setScalable(false);
						cursor.setCursorRes(R.drawable.shape_rectangle_cursor);
						cursor.setBoarder((int) getResources()
									.getDimension(R.dimen.px2));
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								(int) getResources()
										.getDimension(R.dimen.px534),
								(int) getResources()
										.getDimension(R.dimen.px300));

						if (lastId > 0) {
							params.addRule(RelativeLayout.ALIGN_TOP, lastId);
							params.addRule(RelativeLayout.RIGHT_OF, lastId);
							params.leftMargin = (int) getResources()
									.getDimension(R.dimen.px12);
						} else {
							params.topMargin = (int) getResources()
									.getDimension(R.dimen.px2);
							params.leftMargin = (int) getResources()
									.getDimension(R.dimen.px12);
						}

						cursor.setLayoutParams(params);
						cursor.configImageUrl(imagesJsonArray.opt(i).toString());
						imgsContainer.addView(cursor);
						cursor.setOnFocusChangeListener(focusChangeListener);
						cursor.setOnClickListener(clickListener);
						lastId = i + 1000;
						cursor.setId(lastId);
					}

					// 添加一个空的空的
					RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
							(int) getResources().getDimension(R.dimen.px9),
							(int) getResources().getDimension(R.dimen.px300));
					ImageView empty = new ImageView(AppDetail.this);

					params.addRule(RelativeLayout.ALIGN_TOP, lastId);
					params.addRule(RelativeLayout.RIGHT_OF, lastId);
					empty.setLayoutParams(params);
					imgsContainer.addView(empty);

					((TextView) findViewById(R.id.desc_textview))
							.setText(appInfo.desc);

					if (appInfo.sourceQuality == 0) {
						findViewById(R.id.iv_sq_official).setVisibility(View.GONE);
					}

					findViewById(R.id.trl_desc).setOnClickListener(
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											getApplicationContext(),
											AppDetailDescActivity.class);
									intent.putExtra("descContent", appInfo.desc);
									AppDetail.this.startActivity(intent);
								}
							});

					appInfo.appId = appInfoJsonObject.getInt("id");
					((TextView) findViewById(R.id.app_detail_title_little))
							.setText(appInfo.title);

					((TextView) findViewById(R.id.app_detail_cat_little))
							.setText(appInfoJsonObject.getString("cat_title"));

					((StarList) findViewById(R.id.app_detail_star_list))
							.setStar(appInfoJsonObject.getInt("star"));

					int downloadCount = appInfoJsonObject
							.getInt("download_num");

					if (downloadCount > 10000) {
						((TextView) findViewById(R.id.app_detail_download_num))
								.setText(downloadCount / 10000 + "万+");
					} else {
						((TextView) findViewById(R.id.app_detail_download_num))
								.setText(downloadCount + "");
					}

					// 作者
					((TextView) findViewById(R.id.app_detail_author))
							.setText(appInfoJsonObject.getString("author"));

					// 版本
					((TextView) findViewById(R.id.app_detail_version_name))
							.setText(appInfoJsonObject
									.getString("version_name"));

					// 更新

					// 大小
					((TextView) findViewById(R.id.app_detail_size))
							.setText(CommonUtil.formatSize(appInfoJsonObject
									.getInt("size")));
					// 分级

//					// 控制图
//					((ImageView) findViewById(R.id.app_detail_controller))
//							.setImageResource(DataUtil
//									.getControllerRsid(appInfoJsonObject
//											.getInt("controller")));
					// 控制text
					((TextView) findViewById(R.id.app_detail_controller_name))
							.setText(DataUtil
									.getControllerName(appInfoJsonObject
											.getInt("controller")));

					TvImageView tiv_icon = ((TvImageView) findViewById(R.id.tiv_icon));
					tiv_icon.configImageUrl(appInfo.iconUrl);

					fresh();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	protected void onDestroy() {

		if (request != null) {
			request.abort();
		}

		super.onDestroy();
	}

	@Override
	public void onTaskUpdate(String packageName, int action) {

		try {
			switch (action) {
			case SecurityService.DOWNLOAD_STATUS_UPDATE:

				if (packageName.equals(taskInfo.getPackageName())) {
					tpb_download.setProgress(taskInfo.getProgress());
				}

				break;

			case SecurityService.DOWNLOAD_STATUS_SUCCESS:
				if (packageName.equals(taskInfo.getPackageName())) {
					taskInfo.status = TaskInfo.SUCCESS;
					fresh();
				}
				break;

			case SecurityService.DOWNLOAD_STATUS_FAILED:
				if (packageName.equals(taskInfo.getPackageName())) {
					CommonUtil.showBigTip(this,"网络错误,下载失败");
					taskInfo.status = TaskInfo.FAILED;
					fresh();
				}
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onButtonFocusChange(AppListBottomButton button, Boolean focus) {

	}

}
