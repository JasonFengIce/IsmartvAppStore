package com.boxmate.tv.adapter;

import java.util.HashMap;
import java.util.List;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;

import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.component.TvBaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class SearchGridAdapter extends TvBaseAdapter {

	private List<AppInfo> appList;
	private LayoutInflater inflater;

	public SearchGridAdapter(Context context, List<AppInfo> appList) {
		this.inflater = LayoutInflater.from(context);
		this.appList = appList;
	}

	@Override
	public int getCount() {
		return appList.size();
	}

	@Override
	public Object getItem(int position) {
		return appList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View contentView, ViewGroup parent) {
		ViewHolder holder = null;
		if (contentView == null) {
			contentView = inflater.inflate(R.layout.activity_search_grid_item,
					null);
			holder = new ViewHolder();
			holder.tv_title = (TextView) contentView
					.findViewById(R.id.tv_title);
			holder.tiv_icon = (TvImageView) contentView
					.findViewById(R.id.tiv_icon);
			contentView.setTag(holder);
		} else {
			holder = (ViewHolder) contentView.getTag();
		}

		AppInfo app = appList.get(position);
		holder.tv_title.setText(app.getAppname());
		holder.tv_title.setTag(app.getId());
		holder.tiv_icon.configImageUrl(app.getIconUrl());

		return contentView;
	}

	public void addItem(AppInfo item) {
		appList.add(item);
	}

	public void clear() {
		appList.clear();
	}

	public void flush(List<AppInfo> appListNew) {
		appList = appListNew;
	}

	static class ViewHolder {
		TextView tv_title;
		TvImageView tiv_icon;
	}
}
