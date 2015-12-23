package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.string;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.ui.AppListActivity;
import com.boxmate.tv.ui.MainActivity;
import com.boxmate.tv.ui.MainActivity.HomeFragmentInterface;
import com.boxmate.tv.ui.tool.ToolsFlush;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.FocusScaleButton;
import com.boxmate.tv.view.WebImageView;
import com.umeng.analytics.MobclickAgent;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class HomeFragment extends Fragment implements
		MainActivity.HomeFragmentInterface {
	public View homeView;
	private Boolean loadFlag = true;

	public interface HomeFragmentCreate {
		public void setHomeFirstFocusButton(Button button);
	}

	private int ads[] = { R.id.home_ad_1, R.id.home_ad_2, R.id.home_ad_3,
			R.id.home_ad_4, R.id.home_ad_5 };

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		homeView = inflater.inflate(R.layout.fragment_home, container, false);

		// 写page
		for (int i = 0; i < ads.length; i++) {
			((FocusScaleButton) homeView.findViewById(ads[i])).page = page;
		}
		// 写page
		((FocusScaleButton) homeView.findViewById(R.id.home_education)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_flush)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_search)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_board)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_game_remote)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_health)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_game_handle)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_education)).page = page;
		((FocusScaleButton) homeView.findViewById(R.id.home_install)).page = page;

		FocusScaleButton themeButton = (FocusScaleButton) homeView
				.findViewById(R.id.home_theme_list);
		themeButton.page = page;
		themeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getActivity(),
						ThemeListActivity.class);
				getActivity().startActivity(intent);

				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_home_themelist_click));
				} catch (Exception e) {
				}
			}
		});
		// 清理
		homeView.findViewById(R.id.home_flush).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(),
								ToolsFlush.class);
						getActivity().startActivity(intent);

						try {
							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_clear_click));
						} catch (Exception e) {
						}
					}
				});

		// 搜索分类
		homeView.findViewById(R.id.home_search).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(getActivity(),
								SearchActivity.class);
						getActivity().startActivity(intent);

						try {
							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_search_click));
						} catch (Exception e) {
						}
					}
				});
		//排行榜
		homeView.findViewById(R.id.home_board).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(getActivity(),
								BoardActivity.class);
						getActivity().startActivity(intent);

						try {
							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_board_click));
						} catch (Exception e) {
						}
					}
				});

		// 遥控游戏
//		CommonUtil.bindGoAppListEvent(getActivity(),
//				homeView.findViewById(R.id.home_game_remote), "遥控操作",
//				"get_game_list_by_control&control=" + Config.CONTROL_REMOTE,
//				RankType.RANK);
		((FocusScaleButton) homeView.findViewById(R.id.home_game_remote))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							// TODO Auto-generated method stub
							Intent intent = new Intent(getActivity(),
									AppListActivity.class);
							intent.putExtra("title", "遥控游戏");
							intent.putExtra("action",
									"get_app_list_by_subject_name_id&name_id="
											+ "shiyun_home_game_remote");
							intent.putExtra("rank_type", RankType.RANK);
							intent.putExtra("isSubject", 1);

							startActivity(intent);

							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_game_click));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		

	
		// 健康
		
//		CommonUtil.bindGoAppListEvent(getActivity(),
//				homeView.findViewById(R.id.home_health), "健康",
//				"get_list_by_cid_rank&cid=" + Config.APP_CAT_HEALTH,
//				RankType.RANK);
		((FocusScaleButton) homeView.findViewById(R.id.home_health))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							// TODO Auto-generated method stub
							Intent intent = new Intent(getActivity(),
									AppListActivity.class);
							intent.putExtra("title", "健康");
							intent.putExtra("action",
									"get_app_list_by_subject_name_id&name_id="
											+ "shiyun_home_health");
							intent.putExtra("rank_type", RankType.RANK);
							intent.putExtra("isSubject", 1);
							startActivity(intent);

							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_video_click));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		// 手柄游戏
		
//		CommonUtil.bindGoAppListEvent(getActivity(),
//				homeView.findViewById(R.id.home_game_handle), "手柄操作",
//				"get_game_list_by_control&control=" + Config.CONTROL_HANDLE,
//				RankType.RANK);
		((FocusScaleButton) homeView.findViewById(R.id.home_game_handle))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							// TODO Auto-generated method stub
							Intent intent = new Intent(getActivity(),
									AppListActivity.class);
							intent.putExtra("title", "手柄游戏");
							intent.putExtra("action",
									"get_app_list_by_subject_name_id&name_id="
											+ "shiyun_home_game_handle");
							intent.putExtra("rank_type", RankType.RANK);
							intent.putExtra("isSubject", 1);
							startActivity(intent);

							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_game_click));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		// 教育
		((FocusScaleButton) homeView.findViewById(R.id.home_education))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							// TODO Auto-generated method stub
							Intent intent = new Intent(getActivity(),
									AppListActivity.class);
							intent.putExtra("title", "教育");
							intent.putExtra("action",
									"get_app_list_by_subject_name_id&name_id="
											+ "shiyun_home_edu");
							intent.putExtra("rank_type", RankType.RANK);
							intent.putExtra("isSubject", 1);
							startActivity(intent);

							// 统计点击
							MobclickAgent
									.onEvent(
											getActivity(),
											getActivity()
													.getResources()
													.getString(
															R.string.count_home_education_click));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
		// 装机必备
		((FocusScaleButton) homeView.findViewById(R.id.home_install))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						try {
							Intent intent = new Intent(getActivity(),
									InstallListActivity.class);
							intent.putExtra("title", "装机必备");
							intent.putExtra("action",
									"get_app_list_by_subject_name_id&name_id="
											+ "shiyun_home_tool");
							intent.putExtra("rank_type", RankType.RANK);
							intent.putExtra("isSubject", 1);
							startActivity(intent);

							// 统计点击
							MobclickAgent.onEvent(
									getActivity(),
									getActivity().getResources().getString(
											R.string.count_home_install_click));

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

		loadData();
		return homeView;
	}

	public void initAd(int rid, HashMap<String, String> adDetail) {

	}

	@Override
	public void onStart() {
		// 首次安装 弹出强推页
		SharedPreferences setting = getActivity().getSharedPreferences(
				"Setting", Context.MODE_PRIVATE);
		if (setting.getBoolean("firstLaunch", true)) {
			Editor editor = setting.edit();
			editor.putBoolean("firstLaunch", false);
			editor.commit();
		}

		super.onStart();
	}

	public void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();

	}

	public void onDestroy() {

		super.onDestroy();

	}

	public int page = 0;

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
		if (!loadFlag) {
			return;
		}
		Log.i("首页", "加载首页");
		loadFlag = false;
		// 广告
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "get_ad_list_by_nameid");
		params.put("name_id", "shiyun_home_remmand");
		String api = HttpCommon.buildApiUrl(params);

		Log.i("api", api);

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
						//Log.e("t=", isRepeat+"---"+i+"---"+pkg+"--"+count+"----"+jsonObj.toString());
						int adId = ads[count];
						homeView.findViewById(adId).setTag(
								"0-"
										+ jsonObj.getString("app_id"));
						FocusScaleButton focusScaleButton=((FocusScaleButton) homeView.findViewById(adId));
						MainActivity.tb.display(focusScaleButton, jsonObj.getString("image_url_v2"));
						
						((FocusScaleButton) homeView.findViewById(adId))
								.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										try {
											String tag = (String) arg0.getTag();
											String[] ads = tag.split("-");
											Intent intent = new Intent(
													getActivity(),
													AppDetail.class);
											intent.putExtra("app_id", ads[1]);
											intent.putExtra("referer", "home");
											startActivity(intent);

											// 统计点击
											MobclickAgent
													.onEvent(
															getActivity(),
															getActivity()
																	.getResources()
																	.getString(
																			R.string.count_home_adlist_click));
										} catch (Exception e) {
										}

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

	@Override
	public void focusControl(int direction) {
		if (homeView==null) 
			return;
		switch (direction) {
		case 0:
			homeView.findViewById(R.id.home_search).requestFocus();
			break;
		case 1:
			try {
				homeView.findViewById(R.id.home_search).requestFocus();
			} catch (Exception e) {
				// TODO: handle exception
			}
			
			break;
		case 2:
			homeView.findViewById(R.id.home_ad_5).requestFocus();
			break;
		}
		
	}
}
