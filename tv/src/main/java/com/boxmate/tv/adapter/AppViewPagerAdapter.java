package com.boxmate.tv.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import reco.frame.tv.TvBitmap;

import com.boxmate.tv.view.AppPageView;
import com.boxmate.tv.view.AppPageView.OnAppListener;

import android.R.integer;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.support.v4.view.ViewPager;

public class AppViewPagerAdapter extends PagerAdapter {
	public List<ArrayList<HashMap<String, Object>>> mListViews;
	private Context context;
	private TvBitmap tb;
	public int lastPosition = 0;
	private AppPageView.OnAppListener delegate;

	public AppViewPagerAdapter(
			List<ArrayList<HashMap<String, Object>>> mListViews,
			Context context, AppPageView.OnAppListener delegate) {
		this.mListViews = mListViews;
		this.context = context;
		this.delegate = delegate;
		this.tb=TvBitmap.create(context);
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView((View) arg2);
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return mListViews.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {

		AppPageView pageView = new AppPageView(context);
		pageView.delegate = delegate;
		pageView.setPage(arg1);
		pageView.setList(mListViews.get(arg1));
		((ViewPager) arg0).addView(pageView);
		pageView.setId(arg1);
		// System.gc();
		return pageView;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}
}