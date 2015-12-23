package com.boxmate.tv.adapter;

import java.util.List;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;


import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.component.TvBaseAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BoardGridAdapter extends TvBaseAdapter {

	
	private List<AppInfo> appList;
	private LayoutInflater inflater;
	
	public BoardGridAdapter(Context context,List<AppInfo> appList){
		this.inflater=LayoutInflater.from(context);
		this.appList=appList;
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
		ViewHolder holder=null;
		if (contentView==null) {
			contentView=inflater.inflate(R.layout.activity_board_grid_item, null);
			holder=new ViewHolder();
			holder.tv_rank=(TextView) contentView.findViewById(R.id.tv_rank);
			holder.tv_title=(TextView) contentView.findViewById(R.id.tv_title);
			holder.tiv_icon=(TvImageView) contentView.findViewById(R.id.tiv_icon);
			contentView.setTag(holder);
		}else{
			holder=(ViewHolder) contentView.getTag();
		}
		
		AppInfo app=appList.get(position);
		
		if (app.getId()!=-1) {
			holder.tv_rank.setText((position/3+1)+"");
			holder.tv_title.setText(app.getAppname());
			holder.tv_title.setTag(app.getId());
			holder.tiv_icon.configImageUrl(app.getIconUrl());
		}else{
			holder.tv_rank.setText("");
			holder.tv_title.setText("");
			holder.tv_title.setTag(app.getId());
			holder.tiv_icon.setVisibility(View.INVISIBLE);
			contentView.setVisibility(View.INVISIBLE);
		}
		
		
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

	
	static class ViewHolder{
		TextView tv_rank;
		TextView tv_title;
		TvImageView tiv_icon;
	}
}
