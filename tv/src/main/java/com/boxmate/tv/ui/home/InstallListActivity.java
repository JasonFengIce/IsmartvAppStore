package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvLoadingBar;
import reco.frame.tv.view.TvRelativeLayout;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.adapter.AppListAdapter;
import com.boxmate.tv.adapter.UninstallListAdapter;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.background.SecurityService.OnDownloadStatusChanaged;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppListDownloader;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.util.NetUtil;
import com.boxmate.tv.view.AppListBottomButton;
import com.boxmate.tv.view.AppPageView;
import com.boxmate.tv.view.AppViewPagerView;
import com.boxmate.tv.view.FocusScaleFrame;
import com.boxmate.tv.view.AppListBottomButton.OnButtonFocusChangeListener;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InstallListActivity extends Activity {

	private List<AppInfo> appList;
	public AppListDownloader downloader = new AppListDownloader();
	private String action = "get_app_list_by_rank";
	private int rankType = RankType.RANK;
	private ViewPager vp_list;
	// private TextView tv_download_hint;
	private TvButton install_easy;
	private TvLoadingBar tlb_loading;
	private AppListAdapter adapter;
	public void setRankType(int rankType) {
		this.rankType = rankType;
	}

	public void onResume() {
		super.onResume();

		if(adapter!=null) {


			for (String key : adapter.itemList.keySet()) {
				ImageView bg = adapter.itemList.get(key);
				if(CommonUtil.checkPackageInstalled(key)) {
					bg.setVisibility(View.VISIBLE);
					bg.setBackgroundResource(R.drawable.app_installed_cover);
				}
			}
		}


		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_install_list);

		Intent intent = getIntent();
		rankType = intent.getIntExtra("rank_type", RankType.RANK);
		action = intent.getStringExtra("action");
		if (action == null) {
			action = "get_app_list_by_rank";
		}
		// tv_download_hint = (TextView) findViewById(R.id.tv_download_hint);
		install_easy = (TvButton) findViewById(R.id.install_easy);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		vp_list = (ViewPager) findViewById(R.id.vp_list);
		loadData();
	}

	private void loadData() {

		String url = Config.appBase + "action=" + action + "&rank_type="
				+ rankType;

		TvHttp tvHttp = new TvHttp(getApplicationContext());
		tvHttp.addHeader(Config.Cookie, NetUtil.buildCommonCookie(this));
		tvHttp.get(url, new AjaxCallBack<Object>() {

			@Override
			public void onSuccess(Object t) {

				JSONObject resultJsonObject;
				try {
					resultJsonObject = new JSONObject(t.toString());
					JSONArray jsonArray = resultJsonObject
							.getJSONArray("data_list");
					appList = new ArrayList<AppInfo>();

					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);

						AppInfo app = new AppInfo();
						app.setId(jsonObject.getInt("id"));
						app.setAppname(jsonObject.getString("title"));
						app.setIconUrl(jsonObject.getString("icon_url"));
						app.setApkUrl(jsonObject.getString("download_url"));
						app.setCatTitle(jsonObject.getString("cat_title"));
						app.setCname(DataUtil.getControllerName(jsonObject
								.getInt("controller")));
						app.setCsId(DataUtil.getControllerRsid(jsonObject
								.getInt("controller")));
						app.setPackagename(jsonObject.getString("package"));
						app.setControl(jsonObject.getInt("controller"));
						app.setStar(jsonObject.getInt("star"));
						appList.add(app);
					}

					show();

					// try {
					// install_easy.requestFocus();
					// } catch (Exception e) {
					// }
				} catch (JSONException e) {
					e.printStackTrace();
				}
				super.onSuccess(t);
			}
		});
	}

	private void show() {
		adapter = new AppListAdapter(getApplicationContext(),
				appList);
		vp_list.setAdapter(adapter);
		tlb_loading.setVisibility(View.GONE);


		install_easy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					if (SecurityService.instance == null) {
						startService(new Intent(InstallListActivity.this,
								SecurityService.class));
					}

					for (AppInfo app : appList) {
						String packageName = app.getPackagename();
						if (!CommonUtil.checkPackageInstalled(packageName)
								&& !SecurityService.instance
										.checkPackageExistInQueue(packageName)) {

							TaskInfo ti = new TaskInfo();
							ti.setTaskId(app.getId());
							ti.setTaskName(app.getAppname());
							ti.setProgress(0);
							ti.status = TaskInfo.WAITING;
							ti.setDownloadUrl(app.getApkUrl());
							ti.setPackageName(packageName);
							ti.iconUrl = app.getIconUrl();
							SecurityService.instance.addTaskInQueue(ti);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				install_easy.setText(getResources().getString(
						R.string.easy_download_hint));
				install_easy.setClickable(false);

				// v.setVisibility(View.GONE);
				// tv_download_hint.setVisibility(View.VISIBLE);

			}

		});
	}

	// @Override
	// public void onAppClick(int page, int position, HashMap<String, Object>
	// app) {
	//
	// Intent intent = new Intent(this, AppDetail.class);
	// intent.putExtra("app_id", app.get("app_id").toString());
	// startActivity(intent);
	//
	// }

}
