package com.boxmate.tv.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.TvRelativeLayout;
import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.ui.AppDetail;
import com.boxmate.tv.ui.manage.UninstallActivity;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.view.StarList;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AppListAdapter extends PagerAdapter {

	private List<List<AppInfo>> pageList;
	private List<AppInfo> appList;
	private Context context;
	private int width, height, spaceHori, spaceVert, marginLeft;
	private LayoutInflater mInflater;
	private int marginTop;
	private int col = 3, row = 3;
	private int pageCount;
	public HashMap<String,ImageView> itemList = new HashMap<String, ImageView>();

	public int getPageCount() {
		return pageCount;
	}

	public AppListAdapter(Context context, List<AppInfo> appList) {

		this.context = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.appList = appList;
		pageList = new ArrayList<List<AppInfo>>();
		this.width = (int) context.getResources().getDimension(R.dimen.px490);
		this.height = (int) context.getResources().getDimension(R.dimen.px224);
		this.marginLeft = (int) context.getResources().getDimension(R.dimen.px40);
		this.spaceHori = (int) context.getResources()
				.getDimension(R.dimen.px10);
		this.spaceVert = (int) context.getResources()
				.getDimension(R.dimen.px10);

		formatList();

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

		pageView = createView(pageList.get(position), position + 1);
		((ViewPager) container).addView(pageView);

		return pageView;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	private void formatList() {

		List<AppInfo> page = new ArrayList<AppInfo>();
		for (int i = 0; i < appList.size(); i++) {
			page.add(appList.get(i));
			if (page.size() >= 9) {
				pageList.add(page);
				page = new ArrayList<AppInfo>();

			} else if (i == (appList.size() - 1)) {
				pageList.add(page);
				break;
			}

		}

		pageCount = pageList.size();
	}

	private RelativeLayout createView(List<AppInfo> imageList, final int pageNum) {

		RelativeLayout page = new RelativeLayout(context);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		if (imageList.size() < 4) {
			marginTop = (int) context.getResources()
					.getDimension(R.dimen.px380);
		} else {
			marginTop = (int) context.getResources()
					.getDimension(R.dimen.px200);
		}
		page.setLayoutParams(vlp);
		page.setClipChildren(false);
		page.setClipToPadding(false);

		for (int i = 0; i < imageList.size(); i++) {
			TvRelativeLayout item = (TvRelativeLayout) mInflater.inflate(
					R.layout.activity_install_list_item, null);

			int vId = pageNum * 10 + i;
			item.setId(vId);

			TvImageView tiv_icon = (TvImageView) item
					.findViewById(R.id.tiv_icon);
			ImageView iv_installed = (ImageView) item
					.findViewById(R.id.iv_installed);
			TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
			TextView tv_controll = (TextView) item.findViewById(R.id.tv_controll);
			StarList sl_star = (StarList) item.findViewById(R.id.sl_star);

			final AppInfo app = imageList.get(i);

			itemList.put(app.getPackagename(),iv_installed);
			
			tiv_icon.configImageUrl(app.getIconUrl());
			tv_title.setText(app.getAppname());
			tv_controll.setText(app.getCname());
			sl_star.setStar(app.getStar());
			
			if (CommonUtil.isAppInstalled(context, app.getPackagename())) {
				iv_installed.setVisibility(View.VISIBLE);
				iv_installed.setBackgroundResource(R.drawable.app_installed_cover);
			}

			if (i == 0) {
				RelativeLayout.LayoutParams appLpFirst = new RelativeLayout.LayoutParams(
						width, height);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpFirst.setMargins(marginLeft, marginTop, 0, 0);
				page.addView(item, appLpFirst);
			} else if (i < col) {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.setMargins(spaceHori, marginTop, 0, 0);
				page.addView(item, appLpRight);
			} else if (i % col == 0) {

				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.BELOW, vId - col);
				appLpRight.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpRight.setMargins(marginLeft, spaceVert, 0, 0);
				page.addView(item, appLpRight);
			} else {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.BELOW, vId - col);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.setMargins(spaceHori, spaceVert, 0, 0);
				page.addView(item, appLpRight);
			}

			item.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					
					Intent intent = new Intent(context, AppDetail.class);
					intent.putExtra("app_id", app.getId()+"");
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);

				}
			});

		}

		return page;
	}

}
