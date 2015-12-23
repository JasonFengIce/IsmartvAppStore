package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.view.TvGridView;
import reco.frame.tv.view.TvLoadingBar;
import reco.frame.tv.view.TvGridView.OnItemClickListener;

import com.boxmate.tv.R;
import com.boxmate.tv.adapter.BoardGridAdapter;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.util.NetUtil;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class BoardActivity extends Activity {

	private String TAG = "BoardActivity";
	private TvGridView tgv_board;
	private BoardGridAdapter adapter;
	private int boardCount = 15, listType = 3;
	private TvLoadingBar tlb_loading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_board);
		tgv_board = (TvGridView) findViewById(R.id.tgv_board);

		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		load();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void load() {

		TvHttp tvHttp = new TvHttp(this);
		tvHttp.addHeader(Config.Cookie, NetUtil.buildCommonCookie(this));
		tvHttp.get(Config.BOARD_URL, new AjaxCallBack<Object>() {
			@Override
			public void onSuccess(Object t) {
				try {
					List<AppInfo> appList = new ArrayList<AppInfo>();
					JSONObject jsonObject = new JSONObject(t.toString());

					JSONArray hotList = jsonObject.getJSONArray("top_hot_app");
					JSONArray newList = jsonObject.getJSONArray("top_new_app");
					JSONArray gameList = jsonObject
							.getJSONArray("top_hot_game");

					for (int i = 0; i < boardCount; i++) {

						int col = i % listType;
						int row = i / listType;
						AppInfo app = new AppInfo();
						JSONObject jObject = null;
						switch (col) {
						case 0:
							if (row < hotList.length()) {
								jObject = hotList.getJSONObject(row);
								app.setId(Integer.parseInt(jObject
										.getString("id")));
								app.setAppname(jObject.getString("title"));
								app.setIconUrl(jObject.getString("icon_url"));
							} else {
								app.setId(-1);
							}

							break;
						case 1:

							if (row < hotList.length()) {
								jObject = newList.getJSONObject(row);
								app.setId(Integer.parseInt(jObject
										.getString("id")));
								app.setAppname(jObject.getString("title"));
								app.setIconUrl(jObject.getString("icon_url"));
							} else {
								app.setId(-1);
							}

							break;
						case 2:
							if (row < hotList.length()) {
								jObject = gameList.getJSONObject(row);
								app.setId(Integer.parseInt(jObject
										.getString("id")));
								app.setAppname(jObject.getString("title"));
								app.setIconUrl(jObject.getString("icon_url"));
							} else {
								app.setId(-1);
							}

							break;
						}
						appList.add(app);

					}

					adapter = new BoardGridAdapter(getApplicationContext(),
							appList);
					tgv_board.setAdapter(adapter);

					tlb_loading.setVisibility(View.GONE);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				super.onSuccess(t);
			}

		});

		tgv_board.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(View item, int position) {
				TextView tv = (TextView) item.findViewById(R.id.tv_title);
				Intent intent = new Intent(getApplicationContext(),
						AppDetail.class);
				intent.putExtra("app_id", tv.getTag().toString());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BoardActivity.this.startActivity(intent);
			}
		});

	}
}
