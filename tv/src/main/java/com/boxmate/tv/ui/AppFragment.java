package com.boxmate.tv.ui;

import java.util.ArrayList;
import java.util.HashMap;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.FocusScaleButton;

import android.R.integer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Toast;

public class AppFragment extends Fragment implements
		MainActivity.HomeFragmentInterface {
	private int ads[] = { R.id.app_ad_1, R.id.app_ad_2, R.id.app_ad_3,
			R.id.app_ad_4, R.id.app_ad_5,R.id.app_ad_6};
	private boolean loadFlag=true;
	public int page;
	private View appView;
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		appView = inflater.inflate(R.layout.fragment_app, container,
				false);

		for (int i = 0; i < ads.length; i++) {
			((FocusScaleButton) appView.findViewById(ads[i])).page = page;
		}
		((FocusScaleButton) appView.findViewById(R.id.app_board)).page = page;
		((FocusScaleButton) appView.findViewById(R.id.app_new)).page = page;
		((FocusScaleButton) appView.findViewById(R.id.app_live)).page = page;
		((FocusScaleButton) appView.findViewById(R.id.app_education)).page = page;
		((FocusScaleButton) appView.findViewById(R.id.app_health)).page = page;
		((FocusScaleButton) appView.findViewById(R.id.app_tools)).page = page;
		
		loadData();
		return appView;
	}

	public void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();
	}

	@Override
	public void setPage(int page) {
		// TODO Auto-generated method stub
		this.page = page;
	}

	@Override
	public int getPage() {
		// TODO Auto-generated method stub
		return page;
	}
	
	@Override
	public void loadData() {
		
		if (loadFlag) {
			loadFlag=false;
			
			Log.i("首页加载","加载应用页");
			// 获取主页ad
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "get_ad_list_by_nameid");
			params.put("name_id", "shiyun_tool_remmand");
			String api = HttpCommon.buildApiUrl(params);

			HttpCommon.getApi(api, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					JSONObject homeadJsonObject;
					try {
						homeadJsonObject = new JSONObject(result);

						final JSONArray imageList = homeadJsonObject
								.getJSONArray("items");
						int adNum = Math.min(ads.length, imageList.length());
						boolean isRepeat=false;
						int count=0;
						for (int i = 0; i < imageList.length(); i++) {
							JSONObject jsonObj=imageList.getJSONObject(i);
							String pkg=jsonObj.getString("package");
							
							if (isRepeat) {
								if (!CommonUtil.isAppInstalled(getActivity(),
										pkg)) {
									continue;
								}
							} else {

								if (CommonUtil.isAppInstalled(getActivity(),
										pkg)) {

									if (i < (imageList.length() - 1)) {
										continue;
									} else {
										if (count < (adNum - 1)) {
											i = -1;
											isRepeat = true;
											continue;
										}
									}

								} else {
									if (i >= (imageList.length() - 1)) {
										i = -1;
										isRepeat = true;
									}
								}

							}
							
							int adId = ads[count];
							appView.findViewById(adId).setTag(
									"1-"
											+ jsonObj.getString("app_id"));
							FocusScaleButton focusScaleButton=((FocusScaleButton) appView.findViewById(adId));
									MainActivity.tb.display(focusScaleButton, jsonObj.getString("image_url_v2"));
							((FocusScaleButton) appView.findViewById(adId))
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View arg0) {
											String tag = (String) arg0.getTag();
											String[] ads = tag.split("-");
											Intent intent = new Intent(
													getActivity(), AppDetail.class);
											intent.putExtra("app_id", ads[1]);
											startActivity(intent);

										}
									});
							
							count++;
							if (count >= adNum) {
								break;
							}
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_board), "应用排行",
					"get_app_list_new_by_rank",RankType.RANK);
			

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_new), "最新应用",
					"get_app_list_new_by_rank",RankType.TIME);
			

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_live), "生活",
					"get_list_by_cid_rank&cid=" + Config.APP_CAT_LIVE,
					RankType.RANK);
			

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_education), "教育",
					"get_list_by_cid_rank&cid=" + Config.APP_CAT_EDUCATION,
					RankType.RANK);
			

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_health), "健康",
					"get_list_by_cid_rank&cid=" + Config.APP_CAT_HEALTH,
					RankType.RANK);
			

			CommonUtil.bindGoAppListEvent(getActivity(),
					appView.findViewById(R.id.app_tools), "工具",
					"get_list_by_cid_rank&cid=" + Config.APP_CAT_TOOLS,
					RankType.RANK);
			
		}
	
		
	}

	@Override
	public void focusControl(int direction) {
		
		if (appView==null) 
			return;
		
		switch (direction) {
		case 0:
			appView.findViewById(R.id.app_ad_1).requestFocus();
			break;
		case 1:
			appView.findViewById(R.id.app_ad_1).requestFocus();
			break;
		case 2:
			appView.findViewById(R.id.app_ad_6).requestFocus();
			break;
		}
		
		
	}
	
}
