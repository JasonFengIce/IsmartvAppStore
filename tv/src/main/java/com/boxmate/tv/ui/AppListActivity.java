package com.boxmate.tv.ui;

import java.util.ArrayList;
import java.util.HashMap;

import reco.frame.tv.view.TvLoadingBar;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.ui.home.SearchActivity;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.AppListBottomButton;
import com.boxmate.tv.view.AppViewPagerView;
import com.boxmate.tv.view.AppListBottomButton.OnButtonFocusChangeListener;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AppListActivity extends Activity implements
		AppListBottomButton.OnButtonFocusChangeListener,
		AppViewPagerView.AppPageListListener {

	private String action = "get_app_list_by_rank";
	private int rankType = RankType.RANK;
	private Boolean viewLoaded = false;
	private Boolean bottomFocus = true;
	private int bottomType = 1;

	private AppViewPagerView appPageListView;
	private TvLoadingBar tlb_loading;

	public void setRankType(int rankType) {
		this.rankType = rankType;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_list);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		// 标题
		Intent intent = getIntent();
		String titleString = intent.getStringExtra("title");
		int isSubject = intent.getIntExtra("isSubject", 0);
		if (isSubject == 1) {
			findViewById(R.id.app_list_bottom_containter).setVisibility(
					View.GONE);
			bottomFocus = false;
		}

		TextView titleTextView = (TextView) findViewById(R.id.app_list_title);
		titleTextView.setText(titleString);
		rankType = intent.getIntExtra("rank_type", RankType.RANK);
		bottomType = rankType;
		action = intent.getStringExtra("action");
		if (action == null) {
			action = "get_app_list_by_rank";
		}

		findViewById(R.id.app_list_bottom_containter).setOnFocusChangeListener(
				new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						if (arg1) {
							bottomFocus = true;
							getButtonByRankType(rankType).focus(true);
							
						} else {
							bottomFocus = false;
							getButtonByRankType(rankType).focus(false);
							
						}
					}
				});

		appPageListView = (AppViewPagerView) findViewById(R.id.app_page_container);
		appPageListView.delegate = this;

		findViewById(R.id.app_list_botton_top_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRankType(1);
				bottomType = 1;
			}
		});
		findViewById(R.id.app_list_botton_latest_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadRankType(2);
				bottomType = 2;
			}
		});
		findViewById(R.id.right_tip).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = appPageListView.getCurrentItem();
				index = index + 1;
				appPageListView.setCurrentItem(index, true);
			}
		});

		findViewById(R.id.left_tip).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = appPageListView.getCurrentItem();
				index = index - 1;
				if(index<0) {
					return;
				}
				appPageListView.setCurrentItem(index, true);
			}
		});
		findViewById(id.menu).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AppListActivity.this, SearchActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	public boolean onKeyDown(int kCode, KeyEvent kEvent) {

		switch (kCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (bottomFocus) {
				int toType = bottomType - 1;
				if (toType > 0) {
					loadRankType(toType);
					bottomType = toType;
				}
				return true;
			} else {
				break;
			}
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (bottomFocus) {
				int toType = bottomType + 1;
				if (toType <= 2) {
					loadRankType(toType);
					bottomType = toType;
				}
				return true;
			} else {
				break;
			}
		case KeyEvent.KEYCODE_MENU:
			Intent intent = new Intent(this, SearchActivity.class);
			startActivity(intent);
			finish();
			return true;
		}

		return super.onKeyDown(kCode, kEvent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_list, menu);
		return true;
	}

	@Override
	public void onButtonFocusChange(AppListBottomButton button, Boolean focus) {
		if (focus) {
			button.setAlpha(1.0f);
		} else {
			if (bottomFocus) {
				button.setAlpha(0.5f);
			}else{
				
			}
		}

	}

	public AppListBottomButton getButtonByRankType(int type) {
		if (type == RankType.RANK) {
			return (AppListBottomButton) findViewById(R.id.app_list_botton_top_btn);
		} else if (type == RankType.TIME) {
			return (AppListBottomButton) findViewById(R.id.app_list_botton_latest_btn);
		} else {
			return (AppListBottomButton) findViewById(R.id.app_list_botton_top_btn);
		}
	}

	public void loadFirstPage() {

		// 获取主页ad

		String apiUrlString = Config.appBase + "action=" + action
				+ "&rank_type=" + rankType;

		Log.i("api", apiUrlString);
		appPageListView.load(apiUrlString);
	}

	public void loadRankType(int type, Boolean force) {

		for (int i = 1; i <= 3; i++) {
			getButtonByRankType(i).focus(false);
		}

		getButtonByRankType(type).focus(true);

		int btnType = Integer.parseInt(getButtonByRankType(type).getTag()
				.toString());

		if ((btnType != rankType) || force) {

			rankType = btnType;
			loadFirstPage();
		} else {
			rankType = btnType;
		}

	}

	public void loadRankType(int type) {
		((TextView) findViewById(R.id.page_num)).setText("1");
		((TextView) findViewById(R.id.page_total_num)).setText("0");

		findViewById(R.id.right_tip).setVisibility(View.INVISIBLE);
		findViewById(R.id.left_tip).setVisibility(View.INVISIBLE);
		loadRankType(type, false);
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		if (hasFocus && !viewLoaded) {
			getButtonByRankType(RankType.RANK).setTag(RankType.RANK);
			getButtonByRankType(RankType.TIME).setTag(RankType.TIME);
			getButtonByRankType(RankType.WEEKRANK).setTag(RankType.WEEKRANK);
			loadRankType(rankType, true);
			viewLoaded = true;
		}
	}

	@Override
	public void onNewListLoaded(int pageTotal,
			ArrayList<HashMap<String, Object>> appList, int page) {
		try {
			((TextView) findViewById(R.id.page_total_num)).setText(pageTotal
					+ "");
			if (tlb_loading != null) {
				tlb_loading.setVisibility(View.GONE);
			}

		} catch (Exception e) {
		}

	}

	@Override
	public void onPageChange(int fromPage, int toPage) {
		// TODO Auto-generated method stub
		((TextView) findViewById(R.id.page_num)).setText(toPage + 1 + "");
	}

	@Override
	public void onAppClick(int page, int position, HashMap<String, Object> app) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, AppDetail.class);
		intent.putExtra("app_id", app.get("app_id").toString());
		startActivity(intent);
	}

	@Override
	public void showLeftPageTip(Boolean show) {
		// TODO Auto-generated method stub
		if (show) {
			findViewById(R.id.left_tip).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.left_tip).setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void showRightPageTip(Boolean show) {
		// TODO Auto-generated method stub
		if (show) {
			findViewById(R.id.right_tip).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.right_tip).setVisibility(View.INVISIBLE);
		}
	}
}
