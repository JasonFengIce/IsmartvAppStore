package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.view.TvLoadingBar;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.adapter.ThemeDetailAdapter;
import com.boxmate.tv.adapter.ThemeListAdapter;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.AppWebInfo;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.entity.ThemeInfo;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.view.WebImageView;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ThemeDetailAcitivity extends Activity {

	private final static String TAG="ThemeDetailAcitivity";
	private final static int START_LOAD_BACKGROUND=0;
	private final static int LOAD_BACKGROUND_SUCCESS=1;
	private final static int LOAD_LIST_SUCCESS=2;
	private final static int LOAD_LIST_FAILED=3;
	public static View mainView;
	private ViewPager vp_applist;
	private ImageView right_tip;
	private ImageView left_tip;
	private String backgroundUrl;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case LOAD_BACKGROUND_SUCCESS:
				wiv_background.setImageUrl(backgroundUrl);
				loadThemeList();
				break;
			
			case LOAD_LIST_SUCCESS:

				final List<List<AppWebInfo>> pageList = (List<List<AppWebInfo>>) msg.obj;
				if (pageList!=null&&pageList.size()>1) {
					right_tip.setVisibility(View.VISIBLE);
				}
				vp_applist.setAdapter(new ThemeDetailAdapter(
						getApplicationContext(), pageList));
				vp_applist
						.setOnPageChangeListener(new OnPageChangeListener() {

							@Override
							public void onPageSelected(int position) {

								if (position > 0) {
									left_tip.setVisibility(View.VISIBLE);
								} else {
									left_tip.setVisibility(View.GONE);
								}

								if (position == (pageList.size() - 1)) {
									right_tip.setVisibility(View.GONE);
								} else {
									right_tip.setVisibility(View.VISIBLE);
								}
							}

							@Override
							public void onPageScrolled(int arg0, float arg1,
									int arg2) {
							}

							@Override
							public void onPageScrollStateChanged(int arg0) {
							}
						});
				tlb_loading.setVisibility(View.GONE);
				break;

			}
		};
	};
	private String action;
	private String nameId;
	private WebImageView wiv_background;
	private TvLoadingBar tlb_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater mInflater=LayoutInflater.from(this);
		mainView = mInflater.inflate(R.layout.activity_theme_detail, null);
		setContentView(mainView);
		vp_applist = (ViewPager) findViewById(R.id.vp_applist);
		wiv_background = (WebImageView) findViewById(R.id.wiv_background);
		wiv_background.showLoading=false;
		right_tip = (ImageView) findViewById(R.id.right_tip);
		left_tip = (ImageView) findViewById(R.id.left_tip);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		loadBackground();
	}
	
	private void loadBackground(){
		Intent intent = getIntent();
		String titleString = intent.getStringExtra("title");
		TextView titleTextView = (TextView)findViewById(R.id.tv_detail_title);
		titleTextView.setText(titleString);
		backgroundUrl = intent.getStringExtra("backgroundUrl");
		action = "get_app_list_by_subject_name_id";
		nameId = intent.getStringExtra("nameId");
		
		new Thread(){
			public void run() {
				CommonUtil.getDrawableByUrl(backgroundUrl, getApplicationContext());
				mHandler.sendEmptyMessage(LOAD_BACKGROUND_SUCCESS);
			};
		}.start();
	}

	private void loadThemeList() {

		try {
			
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", action);
			params.put("name_id", nameId);
			String api = HttpCommon.buildApiUrl(params);
			Log.i(TAG, "check api="+api);
			HttpCommon.getApi(api, new HttpSuccessInterface() {
				@Override
				public void run(String result) {
					try {
						//Log.e(TAG, result);
						List<List<AppWebInfo>> pageList = new ArrayList<List<AppWebInfo>>();
						List<AppWebInfo> appList=new ArrayList<AppWebInfo>();
						JSONObject jsonObj=new JSONObject(result);
						JSONArray jsonArray =jsonObj.getJSONArray("data_list");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject obj = (JSONObject) jsonArray.opt(i);
							AppWebInfo app=new AppWebInfo();
							app.title=obj.getString("title");
							app.iconUrl=obj.getString("icon_url");
							app.downloadUrl=obj.getString("download_url");
							app.catTitle=obj.getString("cat_title");
							app.appId=obj.getInt("id");
							app.star=obj.getInt("star");
							app.packageName=obj.getString("package");
							app.controller=obj.getInt("controller");
							app.cid=DataUtil.getControllerRsid(obj.getInt("controller"));
							app.cname=DataUtil.getControllerName(obj.getInt("controller"));
							appList.add(app);
							
							if (appList.size() >= 5) {
								pageList.add(appList);
								appList = new ArrayList<AppWebInfo>();

							} else if (i == (jsonArray.length() - 1)) {
								pageList.add(appList);
								break;
							}
							
						}
						Message msg = mHandler.obtainMessage();
						msg.what = LOAD_LIST_SUCCESS;
						msg.obj = pageList;
						mHandler.sendMessage(msg);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
