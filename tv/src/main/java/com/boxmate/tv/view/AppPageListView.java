package com.boxmate.tv.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.view.TvRelativeLayout;

import com.boxmate.tv.R;
import com.boxmate.tv.net.AppListDownloader;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class AppPageListView extends RelativeLayout implements
		AppListDownloader.AppListDownloaderListener, AppPageView.OnAppListener {

	public AppListDownloader downloader = new AppListDownloader();
	public int curPage = 0;
	private ArrayList<AppPageView> pages = new ArrayList<AppPageView>();
	private int totalPage = 0;
	private int pageWidth;
	private int pageHeight;
	public AppPageListListener delegate;

	public interface AppPageListListener {
		public void onNewListLoaded(int pageTotal,
				ArrayList<HashMap<String, Object>> appList, int page);

		public void onPageChange(int fromPage, int toPage);

		public void onAppClick(int page, int position,
				HashMap<String, Object> app);
	}

	public AppPageListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public AppPageListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public AppPageListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public void init() {
		downloader.delegate = this;
		pageWidth = (int) getContext().getResources().getDimension(
				R.dimen.px1608);
		pageHeight = (int) getContext().getResources().getDimension(
				R.dimen.px796);
		mScroller = new Scroller(getContext());
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
		pages = new ArrayList<AppPageView>();
		curPage = 0;
		scrollTo(0, 0);
	}

	@Override
	public void newAppListLoaded(int page, String result) {
		// TODO Auto-generated method stub
		JSONObject resultJsonObject;
		try {
			resultJsonObject = new JSONObject(result);
			JSONArray newApplisJsonArray = resultJsonObject
					.getJSONArray("data_list");
			if (newApplisJsonArray.length() == 0) {
				return;
			}
			totalPage = resultJsonObject.getInt("page_num");

			ArrayList<HashMap<String, Object>> pageListDataArrayList = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < newApplisJsonArray.length(); i++) {
				JSONObject radioJson = (JSONObject) newApplisJsonArray.opt(i);
				HashMap<String, Object> appData = DataUtil
						.buildDataForShow(radioJson);
				pageListDataArrayList.add(appData);
			}

			delegate.onNewListLoaded(totalPage, pageListDataArrayList, page);

			AppPageView pageView = new AppPageView(getContext());
			pageView.delegate = this;
			pageView.setPage(page);
			pageView.setList(pageListDataArrayList);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					pageWidth, pageHeight);
			params.leftMargin = page * pageWidth;
			pageView.setLayoutParams(params);
			setFocusable(false);
			pages.add(pageView);

			ViewGroup.LayoutParams params2 = getLayoutParams();
			params2.width = pages.size()
					* (int) getResources().getDimension(R.dimen.screen_width);
			setLayoutParams(params2);
			addView(pageView);
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
		// TODO Auto-generated method stub

		Log.i("测试", "我获取焦点了");

		if (page > curPage) {
			// 加载右边的，
			if (pages.size() <= (page + 1)) {

				// 为了防止卡顿，暂停0.5ms再请求咯
				new Handler().postDelayed(new Runnable() {
					public void run() {
						downloader.setWantPage(page + 1);
					}
				}, 500);

			} else {
				try {
					addView(pages.get(page + 1));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		} else if (page < curPage) {
			if (page - 1 >= 0) {
				// 左边的
				try {
					this.addView(pages.get(page - 1));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		if (page != curPage) {
			// 处理不要的
			for (int i = page - 2; i >= 0; i--) {
				removeView(pages.get(i));
			}
			for (int i = page + 2; i < pages.size(); i++) {
				removeView(pages.get(i));
			}
			smoothScrollTo(page * pageWidth, 0);
			delegate.onPageChange(curPage, page);
			curPage = page;
		}
	}

	@Override
	public void setFirstApp(TvRelativeLayout frame) {
		// TODO Auto-generated method stub

	}

	// 平滑滚动
	private Scroller mScroller;

	// 调用此方法滚动到目标位置
	public void smoothScrollTo(int fx, int fy) {
		int dx = fx - mScroller.getFinalX();
		int dy = fy - mScroller.getFinalY();
		smoothScrollBy(dx, dy);
	}

	// 调用此方法设置滚动的相对偏移
	public void smoothScrollBy(int dx, int dy) {

		// 设置mScroller的滚动偏移量
		mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx,
				dy, 500);
		invalidate();// 这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
	}

	@Override
	public void computeScroll() {

		// 先判断mScroller滚动是否完成
		if (mScroller.computeScrollOffset()) {

			// 这里调用View的scrollTo()完成实际的滚动
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());

			// 必须调用该方法，否则不一定能看到滚动效果
			postInvalidate();
		}
		super.computeScroll();
	}

	@Override
	public void setOnPageChangeBeforePosition(int position) {
		// TODO Auto-generated method stub

	}

}
