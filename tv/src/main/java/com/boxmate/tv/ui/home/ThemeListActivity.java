package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import reco.frame.tv.view.TvLoadingBar;
import com.boxmate.tv.R;
import com.boxmate.tv.adapter.ThemeListAdapter;
import com.boxmate.tv.entity.ThemeInfo;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.umeng.analytics.MobclickAgent;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class ThemeListActivity extends Activity {

	public static View mainView;
	private final static int LOAD_SUCCESS = 0;
	private ViewPager vp_themelist;
	private ImageView right_tip;
	private ImageView left_tip;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case LOAD_SUCCESS:

				final List<List<ThemeInfo>> pageList = (List<List<ThemeInfo>>) msg.obj;
				if (pageList!=null&&pageList.size()>1) {
					right_tip.setVisibility(View.VISIBLE);
				}
				vp_themelist.setAdapter(new ThemeListAdapter(
						getApplicationContext(), pageList));
				vp_themelist
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
	private TvLoadingBar tlb_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater mInflater=LayoutInflater.from(this);
		mainView = mInflater.inflate(R.layout.activity_theme_list, null);
		setContentView(mainView);

		vp_themelist = (ViewPager) findViewById(R.id.vp_themelist);
		right_tip = (ImageView) findViewById(R.id.right_tip);
		left_tip = (ImageView) findViewById(R.id.left_tip);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		loadThemeList();

	}

	private void loadThemeList() {

		try {

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "get_subject_show_list");
			String api = HttpCommon.buildApiUrl(params);

			Log.i("check api", api);

			HttpCommon.getApi(api, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					
					JSONArray themeArray;
					try {
						List<List<ThemeInfo>> pageList = new ArrayList<List<ThemeInfo>>();
						themeArray = new JSONArray(result);
						List<ThemeInfo> themeList = new ArrayList<ThemeInfo>();
						for (int i = 0; i < themeArray.length(); i++) {

							JSONObject jsonObj = themeArray.getJSONObject(i);
							ThemeInfo theme = new ThemeInfo();
							theme.setName(jsonObj.getString("name_id"));
							theme.setTitle(jsonObj.getString("title"));
							theme.setImageUrl(jsonObj.getString("icon_url"));
							theme.setBackgroundUrl(jsonObj.getString("background_url"));
							themeList.add(theme);

							if (themeList.size() >= 5) {
								pageList.add(themeList);
								themeList = new ArrayList<ThemeInfo>();

							} else if (i == (themeArray.length() - 1)) {
								pageList.add(themeList);
								break;
							}

						}
						Message msg = mHandler.obtainMessage();
						msg.what = LOAD_SUCCESS;
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
