package com.boxmate.tv.adapter;

import java.util.List;

import reco.frame.tv.TvBitmap;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppWebInfo;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.ui.home.ThemeDetailAcitivity;
import com.boxmate.tv.ui.home.ThemeListActivity;
import com.boxmate.tv.view.FocusScaleLinearLayout;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThemeDetailAdapter extends PagerAdapter {

	private List<List<AppWebInfo>> pageList;
	private Context context;
	private int width, height, space, margin;
	private LayoutInflater mInflater;
	private TvBitmap tb;
	

	public ThemeDetailAdapter(Context context, List<List<AppWebInfo>> pageList) {

		this.context = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.pageList = pageList;
		this.width = (int) context.getResources().getDimension(R.dimen.px298);
		this.height = (int) context.getResources().getDimension(R.dimen.px298);
		this.margin = (int) context.getResources().getDimension(R.dimen.px30);
		this.space = (int) context.getResources().getDimension(R.dimen.px44);
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

	private RelativeLayout createView(List<AppWebInfo> imageList,final int pageNum) {

		RelativeLayout page = new RelativeLayout(context);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		page.setLayoutParams(vlp);
		page.setPadding(0, 0, 0,margin);
		page.setClipChildren(false);
		page.setClipToPadding(false);

		for (int i = 0; i < imageList.size(); i++) {
			FocusScaleLinearLayout item = (FocusScaleLinearLayout) mInflater
					.inflate(R.layout.activity_theme_detail_item, null);
			
			int vId = pageNum*10 + i;
			item.setId(vId);
			ImageView iv=(ImageView) item.findViewById(R.id.iv_icon);
			TextView tv_name=(TextView) item.findViewById(R.id.tv_name);
			final AppWebInfo app = imageList.get(i);
			tv_name.setText(app.title);
			tb.display(iv, app.iconUrl);
			
			if (i==0) {
				RelativeLayout.LayoutParams appLpFirst = new RelativeLayout.LayoutParams(
						width, height);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpFirst.setMargins(margin, 0, 0, margin);
				page.addView(item, appLpFirst);
			} else {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.addRule(RelativeLayout.ALIGN_BOTTOM, vId - 1);
				appLpRight.setMargins(space, 0, 0, 0);
				page.addView(item, appLpRight);
			}

			item.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					jump(app);
				}
			});
			
			item.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View childView, int arg1, KeyEvent event) {
					int focusedId = childView.getId();

					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

					
						if (focusedId % 10 == 4) {
							try {
								ThemeDetailAcitivity.mainView.findViewById((pageNum + 1) * 10).requestFocus();

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

	private void jump(AppWebInfo app) {
		try {
			Intent intent = new Intent(context, AppDetail.class);
			intent.putExtra("app_id", app.appId+"");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
