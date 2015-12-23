package com.boxmate.tv.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.view.TvRelativeLayout;

import com.boxmate.tv.R;
import com.boxmate.tv.adapter.AppViewPagerAdapter;
import com.boxmate.tv.net.AppListDownloader;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;

public class AppViewPagerView extends ViewPager implements
		AppListDownloader.AppListDownloaderListener, AppPageView.OnAppListener {
	public AppListDownloader downloader = new AppListDownloader();
	public int curPage = 0;
	private ArrayList<ArrayList<HashMap<String, Object>>> pages = new ArrayList<ArrayList<HashMap<String, Object>>>();
	private int totalPage = 0;
	public AppPageListListener delegate;
	public AppViewPagerAdapter adapter;
	public int pageChanageBeforPosition = 0;
	public Boolean firstShow = true;

	public interface AppPageListListener {
		public void onNewListLoaded(int pageTotal,
				ArrayList<HashMap<String, Object>> appList, int page);

		public void onPageChange(int fromPage, int toPage);

		public void onAppClick(int page, int position,
				HashMap<String, Object> app);

		public void showLeftPageTip(Boolean show);

		public void showRightPageTip(Boolean show);
	}

	public AppViewPagerView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public AppViewPagerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private FixedSpeedScroller mScroller;

	public void init() {
		downloader.delegate = this;

		Field mField;
		try {
			mField = ViewPager.class.getDeclaredField("mScroller");

			mField.setAccessible(true);
			mScroller = new FixedSpeedScroller(getContext(),
					new AccelerateInterpolator());

			try {
				mField.set(this, mScroller);
				mScroller.setmDuration(200);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void load(String url) {

		downloader.setUrl(url);
		reset();
		downloader.reset();
		downloader.setWantPage(1);
	}

	public void reset() {

		for (int i = 0; i < pages.size(); i++) {
			this.removeAllViews();
		}

		downloader.reset();
		pages = new ArrayList<ArrayList<HashMap<String, Object>>>();
		curPage = 0;
		scrollTo(0, 0);
		setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(final int page) {

				if (page > curPage) {
					// 加载右边的，
					if (pages.size() <= (page + 1)) {
						new Handler().postDelayed(new Runnable() {
							public void run() {
								downloader.setWantPage(page + 1);
							}

						}, 300);
					}
				}

				delegate.onPageChange(curPage, page);
				// 不是第一页
				if (page > 0) {
					delegate.showLeftPageTip(true);
				} else {
					delegate.showLeftPageTip(false);
				}

				if ((page + 1) < totalPage) {
					delegate.showRightPageTip(true);
				} else {
					delegate.showRightPageTip(false);
				}

				try {
					if (page > curPage) {
						((AppPageView) findViewById(page)).frameList.get(
								pageChanageBeforPosition / 3 * 3)
								.requestFocus();
					} else if (page < curPage) {
						((AppPageView) findViewById(page)).frameList.get(
								pageChanageBeforPosition + 2).requestFocus();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
				curPage = page;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void newAppListLoaded(int page, String result) {
		JSONObject resultJsonObject;
		try {
			resultJsonObject = new JSONObject(result);
			JSONArray newApplisJsonArray = resultJsonObject
					.getJSONArray("data_list");
			if (newApplisJsonArray.length() == 0) {
				return;
			}
			totalPage = resultJsonObject.getInt("page_num");

			// 新页面加载进来，又翻页
			if (totalPage > (curPage + 1)) {
				delegate.showRightPageTip(true);
			} else {
				delegate.showRightPageTip(false);
			}

			ArrayList<HashMap<String, Object>> pageListDataArrayList = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < newApplisJsonArray.length(); i++) {
				JSONObject radioJson = (JSONObject) newApplisJsonArray.opt(i);
				HashMap<String, Object> appData = DataUtil
						.buildDataForShow(radioJson);
				pageListDataArrayList.add(appData);
			}

			delegate.onNewListLoaded(totalPage, pageListDataArrayList, page);

			pages.add(pageListDataArrayList);

			if (page == 0) {
				adapter = new AppViewPagerAdapter(pages, getContext(), this);
				setAdapter(adapter);
				setCurrentItem(0);
			}
			if (adapter!=null) {
				adapter.notifyDataSetChanged();
			}
			
			// setOnPageChangeListener(new MyOnPageChangeListener());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void onAppClick(int page, int position, HashMap<String, Object> app) {
		// TODO Auto-generated method stub
		delegate.onAppClick(page, position, app);
	}

	@Override
	public void onAppFocus(final int page, int position) {
		firstShow = false;
	}

	@Override
	public void setFirstApp(TvRelativeLayout frame) {
		// TODO Auto-generated method stub
		if (firstShow) {
			frame.requestFocus();
		}
	}

	@Override
	public void setOnPageChangeBeforePosition(int position) {
		// TODO Auto-generated method stub
		pageChanageBeforPosition = position;
	}
}
