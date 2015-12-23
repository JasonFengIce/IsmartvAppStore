package com.boxmate.tv.ui.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.view.TvGridView;
import reco.frame.tv.view.TvGridView.OnItemClickListener;
import reco.frame.tv.view.TvLoadingBar;
import com.boxmate.tv.R;
import com.boxmate.tv.adapter.SearchGridAdapter;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.NetUtil;
import com.boxmate.tv.view.TvKeyboard;
import com.boxmate.tv.view.TvKeyboard.OnKeyInputListner;
import com.umeng.analytics.MobclickAgent;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class SearchActivity extends Activity {

	private String TAG = "SearchActivity";
	private final int STATE_INIT = 0, STATE_SEARCHING = 1,
			STATE_SEARCH_FINISH = 2;
	private TvGridView tgv_search;
	private SearchGridAdapter adapter;
	private TvLoadingBar tlb_loading;
	private TvKeyboard tk_keyboard;
	private TextView tv_input, tv_search_result, tv_no_result;
	private String keyWord = "";
	private int hotListMax = 10;
	private int searchState = 0;
	private List<AppInfo> hotList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		tv_input = (TextView) findViewById(R.id.tv_input);
		tv_search_result = (TextView) findViewById(R.id.tv_search_result);
		tv_no_result = (TextView) findViewById(R.id.tv_no_result);
		tgv_search = (TvGridView) findViewById(R.id.tgv_search);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		tk_keyboard = (TvKeyboard) findViewById(R.id.tk_keyboard);
		bindKeyEvent();
		load();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void load() {
		tlb_loading.setVisibility(View.VISIBLE);
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "get_app_list_by_subject_name_id");
		params.put("name_id", "shiyun_search");
		String api = NetUtil.buildApiUrl(params);
		TvHttp tvHttp = new TvHttp(getApplicationContext());
		tvHttp.addHeader(Config.Cookie, NetUtil.buildCommonCookie(this));
		tvHttp.get(api, new AjaxCallBack<Object>() {

			@Override
			public void onSuccess(Object t) {
				Log.e(TAG, t.toString());
				try {

					hotList = new ArrayList<AppInfo>();
					JSONObject jsonObject = new JSONObject(t.toString());
					final JSONArray pushList = jsonObject
							.getJSONArray("data_list");
					for (int i = 0; i < pushList.length(); i++) {
						JSONObject jsonObj = pushList.getJSONObject(i);
						String pkg = jsonObj.getString("package");
						if (!CommonUtil
								.isAppInstalled(SearchActivity.this, pkg)) {

							AppInfo app = new AppInfo();
							app.setId(Integer.parseInt(jsonObj.getString("id")));
							app.setAppname(jsonObj.getString("title"));
							app.setIconUrl(jsonObj.getString("icon_url"));
							hotList.add(app);
							if (hotList.size() >= hotListMax) {
								break;
							}

						}

					}
					adapter = new SearchGridAdapter(getApplicationContext(),
							hotList);
					tgv_search.setAdapter(adapter);

				} catch (Exception e) {
					e.printStackTrace();
				}
				tlb_loading.setVisibility(View.GONE);
				searchState = STATE_SEARCH_FINISH;

				super.onSuccess(t);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				tlb_loading.setVisibility(View.GONE);
				searchState = STATE_SEARCH_FINISH;
				tv_no_result.setVisibility(View.VISIBLE);
				super.onFailure(t, errorNo, strMsg);
			}
		});

	}

	private void bindKeyEvent() {
		tk_keyboard.setOnKeyInputListener(new OnKeyInputListner() {

			@Override
			public void onKeyInput(String keyStr) {
				if (searchState == STATE_SEARCHING || keyWord.length() > 9) {
					return;
				}
				keyWord = keyWord + keyStr;
				tv_input.setText(keyWord);
				selectPost();
			}

			@Override
			public void onKeyDelete() {
				tv_no_result.setVisibility(View.GONE);

				if (searchState == STATE_SEARCHING) {
					return;
				}

				if (keyWord.length() > 0)
					keyWord = keyWord.substring(0, keyWord.length() - 1);
				tv_input.setText(keyWord);

				if (keyWord.length() > 0) {
					selectPost();
				} else if (hotList != null && hotList.size() > 0) {

					adapter = new SearchGridAdapter(getApplicationContext(),
							hotList);
					tgv_search.setAdapter(adapter);
				}

			}

			@Override
			public void onKeyClear() {

				tv_no_result.setVisibility(View.GONE);

				if (searchState == STATE_SEARCHING) {
					return;
				}
				keyWord = "";
				tv_input.setText("");
				if (hotList != null && hotList.size() > 0) {

					adapter = new SearchGridAdapter(getApplicationContext(),
							hotList);
					tgv_search.setAdapter(adapter);

					tv_search_result.setText(getResources().getString(
							R.string.search_result_hot));
				}

			}
		});

		tgv_search.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(View item, int position) {
				TextView tv = (TextView) item.findViewById(R.id.tv_title);
				Intent intent = new Intent(getApplicationContext(),
						AppDetail.class);
				intent.putExtra("app_id", tv.getTag().toString());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				SearchActivity.this.startActivity(intent);

			}
		});
	}

	private void selectPost() {

		tv_no_result.setVisibility(View.GONE);
		tgv_search.removeAllViews();
		tv_search_result.setText(getResources().getString(
				R.string.search_result));

		if (!keyWord.equals("")) {
			searchState = STATE_SEARCHING;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "search");
			params.put("per_num", "99");
			params.put("keyword", keyWord);
			String api = NetUtil.buildApiUrl(params);
			TvHttp tvHttp = new TvHttp(getApplicationContext());
			tvHttp.addHeader(Config.Cookie, NetUtil.buildCommonCookie(this));
			tvHttp.get(api, new AjaxCallBack<Object>() {

				@Override
				public void onSuccess(Object t) {
					try {
						List<AppInfo> appList = new ArrayList<AppInfo>();
						JSONObject jsonObject = new JSONObject(t.toString());
						final JSONArray pushList = jsonObject
								.getJSONArray("data_list");

						for (int i = 0; i < pushList.length(); i++) {
							JSONObject jsonObj = pushList.getJSONObject(i);
							AppInfo app = new AppInfo();
							app.setId(Integer.parseInt(jsonObj.getString("id")));
							app.setAppname(jsonObj.getString("title"));
							app.setIconUrl(jsonObj.getString("icon_url"));
							appList.add(app);

						}

						adapter = new SearchGridAdapter(
								getApplicationContext(), appList);
						tgv_search.setAdapter(adapter);

						if (appList.size() < 1) {
							tv_no_result.setVisibility(View.VISIBLE);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
					tlb_loading.setVisibility(View.GONE);
					searchState = STATE_SEARCH_FINISH;
					super.onSuccess(t);
				}

				@Override
				public void onFailure(Throwable t, int errorNo, String strMsg) {
					searchState = STATE_SEARCH_FINISH;
					tlb_loading.setVisibility(View.GONE);
					tv_no_result.setVisibility(View.VISIBLE);
					super.onFailure(t, errorNo, strMsg);
				}
			});

			tlb_loading.setVisibility(View.VISIBLE);
		} else {
			searchState = STATE_SEARCH_FINISH;
			tlb_loading.setVisibility(View.GONE);
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
