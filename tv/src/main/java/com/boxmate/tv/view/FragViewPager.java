package com.boxmate.tv.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.datatype.Duration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.R;
import com.boxmate.tv.net.AppListDownloader;
import com.boxmate.tv.util.CommonUtil;

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

public class FragViewPager extends ViewPager {

	private final static int SPEED=177;
	public FragViewPager(Context context) {
		super(context);
	}

	public FragViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		
	}
	
	private void init(){
		Field mField;
		try {
			mField = ViewPager.class.getDeclaredField("mScroller");

			mField.setAccessible(true);
			FixedSpeedScroller mScroller = new FixedSpeedScroller(getContext(),
					new AccelerateInterpolator());

			try {
				mField.set(this, mScroller);
				mScroller.setmDuration(SPEED);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	

	



}
