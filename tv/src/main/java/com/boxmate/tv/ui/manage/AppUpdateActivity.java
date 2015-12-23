package com.boxmate.tv.ui.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvLoadingBar;

import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.R.string;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.background.SecurityService.OnDownloadStatusChanaged;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.AppDownloadingActivity;
import com.boxmate.tv.ui.AppUpdateListFragment;
import com.boxmate.tv.ui.AppUpdateListFragment.OnAppListener;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.FocusScaleFrame;
import com.boxmate.tv.view.PageScrollView;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AppUpdateActivity extends Activity implements
		AppUpdateListFragment.OnAppListener,
		SecurityService.OnDownloadStatusChanaged {
	private FocusScaleFrame firstAppFrame;
	private ArrayList<HashMap<String, Object>> pageListDataArrayList = new ArrayList<HashMap<String, Object>>();
	private ArrayList<AppUpdateListFragment> fgs = new ArrayList<AppUpdateListFragment>();

	private AppDownloadManager appDownloadManager;
	private HttpPost request;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_list);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		tb_update = (TvButton) findViewById(R.id.tb_update);
		getAppList();

		SecurityService.delegate = this;

		appDownloadManager = AppDownloadManager.getInstance();
		tb_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (fgs.size() > 0) {

					for (HashMap<String, Object> data : pageListDataArrayList) {
						addTaskInfo(data);
					}

				} else {

				}
			}
		});

	}

	public Handler focusHandle = new Handler() {
		public void handleMessage(Message msg) {
			
			
			
			if (firstAppFrame != null) {
				Log.i("app", "radio");
				firstAppFrame.requestFocus();
			}
		}
	};

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

			request = HttpCommon.postApi(api, params,
					new HttpSuccessInterface() {

						@Override
						public void run(String result) {
							//Log.i("result", result);
							JSONArray updateJsonArray;
							try {
								updateJsonArray = new JSONArray(result);

								HashMap<String, Object> appData = null;

								if (updateJsonArray.length() == 0) {

									if (tlb_loading!=null) {
										tlb_loading.setVisibility(View.GONE);
									}
									tb_update.setVisibility(View.GONE);
									return;
								}

								for (int i = 0; i < updateJsonArray.length(); i++) {
									JSONObject appJson = (JSONObject) updateJsonArray
											.opt(i);
									appData = new HashMap<String, Object>();
									appData.put("title",
											appJson.getString("title"));
									appData.put("icon_url",
											appJson.getString("icon_url"));
									appData.put("cat_title",
											appJson.getString("cat_title"));
									appData.put("app_id", appJson.getInt("id"));
									appData.put("star", appJson.getInt("star"));
									appData.put("size", CommonUtil
											.formatSize(appJson.getInt("size")));
									appData.put("download_url",
											appJson.getString("download_url"));
									appData.put("package",
											appJson.getString("package"));
									appData.put("version_name",
											appJson.getString("version_name"));
									
									pageListDataArrayList.add(appData);
								}

								FragmentManager mFragmentManager = getFragmentManager();
								// 计算page
								int pageNum = (int) Math
										.ceil((float) pageListDataArrayList
												.size() / 9.0);
								for (int i = 0; i <= pageNum; i++) {
									int start = i * 9;
									int end = (start + 9) > pageListDataArrayList
											.size() ? pageListDataArrayList
											.size() : (start + 9);

									ArrayList<HashMap<String, Object>> pageList = new ArrayList<HashMap<String, Object>>();
									for (int j = start; j < end; j++) {
										pageList.add(pageListDataArrayList
												.get(j));
									}

									AppUpdateListFragment mTextFragmentOne = new AppUpdateListFragment();
									mTextFragmentOne.setPage(i);
									mTextFragmentOne.setList(pageList);
									FragmentTransaction fragmentTransactionHome = mFragmentManager
											.beginTransaction();
									fragmentTransactionHome.add(
											R.id.app_list_container_update,
											mTextFragmentOne);
									fragmentTransactionHome
											.commitAllowingStateLoss();
									fgs.add(mTextFragmentOne);
								}

								if (tlb_loading!=null) {
									tlb_loading.setVisibility(View.GONE);
								}

								Thread focusCallThread = new Thread(
										new Runnable() {
											@Override
											public void run() {
												// TODO Auto-generated method
												// stub
												Message message = focusHandle
														.obtainMessage();
												message.sendToTarget();
											}
										});
								focusCallThread.start();
								

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	};
	private int curPage = 0;
	private int curenttranslationX = 0;

	private void getAppList() {
		
		new Thread(new Runnable() {
			@Override
			public void run() {

				List<PackageInfo> packages = getPackageManager()
						.getInstalledPackages(0);

				String packagesString = "";
				String versionCodeString = "";
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo packageInfo = packages.get(i);
					
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						packagesString += packageInfo.packageName + "|";
						versionCodeString += packageInfo.versionCode+ "|";
					}
				}
				if(!isFinishing()) {
					Message message = appLoadedHandler.obtainMessage();
					HashMap<String, String> infoHashMap = new HashMap<String, String>();
					infoHashMap.put("package", packagesString);
					infoHashMap.put("version", versionCodeString);
					message.obj = infoHashMap;
					message.sendToTarget();
					
				}
			}
		}).start();
	}

	private void scrollPage(int toPage) {
		int pageWidth = (int) getResources().getDimension(R.dimen.px1608);
		curenttranslationX += (toPage - curPage) * pageWidth;
		PageScrollView appListContainerLayout = (PageScrollView) findViewById(R.id.app_list_container_update);
		appListContainerLayout.smoothScrollTo(curenttranslationX, 0);
	}

	public TaskInfo addTaskInfo(HashMap<String, Object> appInfo) {
		String packageName = appInfo.get("package").toString();
		TaskInfo taskInfo = appDownloadManager
				.getTaskInfoByPackageName(packageName);
		if (taskInfo != null) {
			if (taskInfo.status == TaskInfo.RUNNING
					|| taskInfo.status == TaskInfo.WAITING) {
				Toast.makeText(this,
						getResources().getString(R.string.updateing),
						Toast.LENGTH_SHORT).show();
				return null;
			}
			if (taskInfo.status == TaskInfo.SUCCESS) {
				CommonUtil.installApk(taskInfo.getDownloadUrl(),
						this);
				return null;
			}
		}

		TaskInfo ti = new TaskInfo();
		ti.setTaskId(Integer.parseInt(appInfo.get("app_id").toString()));
		ti.setTaskName(appInfo.get("title").toString());
		ti.setProgress(0);
		ti.status = TaskInfo.WAITING;
		ti.setDownloadUrl(appInfo.get("download_url").toString());
		ti.setPackageName(appInfo.get("package").toString());
		ti.iconUrl = appInfo.get("icon_url").toString();
		if (SecurityService.instance!=null) {
			SecurityService.instance.addTaskInQueue(ti);
		}
		freshStatus();
		return ti;
	}

	@Override
	public void onAppClick(int page, int position) {
		// TODO Auto-generated method stub
		HashMap<String, Object> appInfo = pageListDataArrayList.get(page * 9
				+ position);
		TaskInfo ti = addTaskInfo(appInfo);

		if (ti != null) {
			Intent intent = new Intent(AppUpdateActivity.this,
					AppDownloadingActivity.class);
			intent.putExtra("package", appInfo.get("package").toString());
			intent.putExtra("id", ti.getTaskId());
			startActivity(intent);
		}

		freshStatus();

	}


	public void onDestroy() {
		super.onDestroy();
		if (request != null) {
			request.abort();
			request = null;
		}
	}

	protected void onResume() {
		super.onResume();
		freshStatus();
		SecurityService.delegate = AppUpdateActivity.this;
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void refresh() {

		curenttranslationX = 0;
		curPage = 0;
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransactionHome = mFragmentManager
				.beginTransaction();
		for (int i = 0; i < fgs.size(); i++) {
			fragmentTransactionHome.remove(fgs.get(i));
		}

		fragmentTransactionHome.commitAllowingStateLoss();

		pageListDataArrayList = new ArrayList<HashMap<String, Object>>();
		fgs = new ArrayList<AppUpdateListFragment>();
		getAppList();
	}

	@Override
	public void onAppFocus(int page, int position) {
		scrollPage(page);
		curPage = page;
	}

	@Override
	public void setFirstApp(FocusScaleFrame frame) {
		// TODO Auto-generated method stub
		firstAppFrame = frame;
	}

	public String toString() {
		return "update";
	}

	public void freshStatus() {
		for (AppUpdateListFragment fragment : fgs) {
			fragment.updateStatus();
		}
	}

	private Handler freshHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (request == null) {

				return;
			}

			try {
				refresh();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	};
	private TvLoadingBar tlb_loading;
	private TvButton tb_update;

	@Override
	public void onTaskUpdate(String packageName, int action) {
		if (action == SecurityService.DOWNLOAD_STATUS_INSTALLED) {
			Message msgMessage = freshHandler.obtainMessage();
			msgMessage.obj = packageName;
			msgMessage.sendToTarget();
		}
	}
}
