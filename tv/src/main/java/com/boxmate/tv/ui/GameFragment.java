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

public class GameFragment extends Fragment implements
		MainActivity.HomeFragmentInterface {
	
	private int ads[] = { R.id.game_ad_1, R.id.game_ad_2,
			R.id.game_ad_3, R.id.game_ad_4,R.id.game_ad_5,R.id.game_ad_6};
	private boolean loadFlag=true;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		gameView = inflater.inflate(R.layout.fragment_game,
				container, false);
		
		for (int i = 0; i < ads.length; i++) {
			((FocusScaleButton) gameView.findViewById(ads[i])).page = page;
		}
		((FocusScaleButton) gameView.findViewById(R.id.game_board)).page = page;
		((FocusScaleButton) gameView.findViewById(R.id.game_new)).page = page;
		((FocusScaleButton) gameView.findViewById(R.id.game_table)).page = page;
		((FocusScaleButton) gameView.findViewById(R.id.game_iq)).page = page;
		((FocusScaleButton) gameView.findViewById(R.id.game_control)).page = page;
		((FocusScaleButton) gameView.findViewById(R.id.game_cat)).page = page;
		
		
		CommonUtil.bindGoAppListEvent(getActivity(),
				gameView.findViewById(R.id.game_board), "游戏排行",
				"get_game_list_by_rank", RankType.RANK);
		

		CommonUtil.bindGoAppListEvent(getActivity(),
				gameView.findViewById(R.id.game_new), "最新游戏",
				"get_game_list_by_rank", RankType.TIME);
		

		

		CommonUtil.bindGoAppListEvent(getActivity(),
				gameView.findViewById(R.id.game_table), "棋牌桌游",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_TABLE,
				RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(getActivity(),
				gameView.findViewById(R.id.game_iq), "益智休闲",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_IQ, RankType.RANK);
		

		gameView.findViewById(R.id.game_control).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(),
								ControlActivity.class);
						startActivity(intent);
					}
				});
		
	

		gameView.findViewById(R.id.game_cat).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(),
								MoreCatActivity.class);
						startActivity(intent);
					}
				});
		
		loadData();
		return gameView;
	}

	public void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();
	}

	public int page = 0;
	private View gameView;

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
			
			
			Log.i("首页加载","加载游戏页");
			// 获取主页ad
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "get_ad_list_by_nameid");
			params.put("name_id", "shiyun_game_remmand");
			String api = HttpCommon.buildApiUrl(params);

			HttpCommon.getApi(api, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					try {
						JSONObject homeadJsonObject = new JSONObject(result);

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
							gameView.findViewById(adId).setTag(
									"2-"
											+ jsonObj.getString("app_id"));
							FocusScaleButton focusScaleButton=((FocusScaleButton) gameView.findViewById(adId));
							MainActivity.tb.display(focusScaleButton,jsonObj.getString("image_url_v2"));
							((FocusScaleButton) gameView.findViewById(adId))
									.setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View arg0) {
											// TODO Auto-generated method stub
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


			
			
		}
		
		
	}

	@Override
	public void focusControl(int direction) {
		if (gameView==null) 
			return;
		switch (direction) {
		case 0:
			gameView.findViewById(R.id.game_ad_1).requestFocus();
			break;
		case 1:
			gameView.findViewById(R.id.game_ad_1).requestFocus();
			break;
		case 2:
			break;
		}
		
		
	}

}
