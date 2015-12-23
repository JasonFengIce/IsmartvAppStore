package com.boxmate.tv.adapter;

import java.util.List;

import reco.frame.tv.TvBitmap;
import reco.frame.tv.view.TvButton;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.entity.ThemeInfo;
import com.boxmate.tv.ui.AppListActivity;
import com.boxmate.tv.ui.home.ThemeDetailAcitivity;
import com.boxmate.tv.ui.home.ThemeListActivity;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.FocusScaleButton;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ThemeListAdapter extends PagerAdapter {

	private List<List<ThemeInfo>> pageList;
	private Context context;
	private int width, height, space, margin;
	private LayoutInflater mInflater;
	private TvBitmap tb;
	

	public ThemeListAdapter(Context context, List<List<ThemeInfo>> pageList) {

		this.context = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.pageList = pageList;
		this.width = (int) context.getResources().getDimension(R.dimen.px280);
		this.height = (int) context.getResources().getDimension(R.dimen.px430);
		this.margin = (int) context.getResources().getDimension(R.dimen.px30);
		this.space = (int) context.getResources().getDimension(R.dimen.px10);
		this.tb=TvBitmap.create(context);

	}

	@Override
	public int getCount() {
		return pageList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		View pageView = null;

		try {
			pageView = createView(pageList.get(position),position+1);
			((ViewPager) container).addView(pageView);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pageView;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	private RelativeLayout createView(List<ThemeInfo> imageList,final int pageNum) {

		RelativeLayout page = new RelativeLayout(context);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		page.setLayoutParams(vlp);
		page.setClipChildren(false);

		for (int i = 0; i < imageList.size(); i++) {
			TvButton scaleButton = (TvButton) mInflater
					.inflate(R.layout.activity_theme_list_item, null);
			int vId = pageNum*10 +i;
			scaleButton.setId(vId);
			final ThemeInfo theme = imageList.get(i);
			tb.display(scaleButton, theme.getImageUrl());
			
			if (i==0) {
				RelativeLayout.LayoutParams appLpFirst = new RelativeLayout.LayoutParams(
						width, height);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpFirst.setMargins(margin, margin, 0, 0);
				page.addView(scaleButton, appLpFirst);
			} else {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.addRule(RelativeLayout.ALIGN_TOP, vId - 1);
				appLpRight.setMargins(space, 0, 0, 0);
				page.addView(scaleButton, appLpRight);
			}

			scaleButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					jump(theme);
				}
			});
			
			scaleButton.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View childView, int arg1, KeyEvent event) {
					int focusedId = childView.getId();

					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

					
						if (focusedId % 10 == 4) {
							try {
								ThemeListActivity.mainView.findViewById((pageNum+1) * 10).requestFocus();

							} catch (Exception e) {
								e.printStackTrace();
							}

						}
						

					}

					return false;
				}
			});

		}

		return page;
	}

	private void jump(final ThemeInfo theme) {
		
		try {
			Intent intent = new Intent(context, ThemeDetailAcitivity.class);
			intent.putExtra("title", theme.getTitle());
			intent.putExtra("nameId", theme.getName());
			intent.putExtra("backgroundUrl", theme.getBackgroundUrl());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
