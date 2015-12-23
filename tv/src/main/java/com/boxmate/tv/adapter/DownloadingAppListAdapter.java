package com.boxmate.tv.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.TvProgressBar;
import reco.frame.tv.view.TvSubButton;
import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.TaskInfo;

public class DownloadingAppListAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater layoutInflater;
	private ArrayList<TaskInfo> list;
	private HashMap<Integer, TvProgressBar> progressBarList = new HashMap<Integer, TvProgressBar>();
	private int itemWidth,itemHeight,spaceVert;

	public DownloadingAppListAdapter(Context context) {
		this.context = context;
		layoutInflater = LayoutInflater.from(context);
		this.itemHeight=(int) context.getResources().getDimension(R.dimen.px114);
	}

	public interface OnLineButtonClickListener {
		public void onCancelClick(int position);

		public void onDeleteClick(int position);

		public void onInstallClick(int position);

		public void onRestartClick(int position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	public void setList(ArrayList<TaskInfo> rlist) {
		list = rlist;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	public void setProgressByTaskInfo(TaskInfo taskInfo) {
		TvProgressBar bar = progressBarList.get(taskInfo.getTaskId());
		if (bar != null) {
			bar.setProgress(taskInfo.getProgress());
		}
	}

	@Override
	public View getView(final int i, View view, ViewGroup arg2) {
		if (view == null) {
			view = layoutInflater.inflate(R.layout.activity_download_list_item,
					arg2,false);
		}
		
		AbsListView.LayoutParams alp=new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,itemHeight);
		view.setLayoutParams(alp);
		final TaskInfo taskInfo = list.get(i);

		progressBarList.put(taskInfo.getTaskId(),
				(TvProgressBar) view.findViewById(R.id.tpb_download));

		((TextView) view.findViewById(R.id.download_list_title))
				.setText(taskInfo.getTaskName());
		((TvProgressBar) view.findViewById(R.id.tpb_download))
				.setProgress(taskInfo.getProgress());

		TvImageView tiv_icon = ((TvImageView) view.findViewById(R.id.tiv_icon));
		tiv_icon.configImageUrl(taskInfo.iconUrl);

		TvSubButton tsb_install = ((TvSubButton) view
				.findViewById(R.id.download_list_install_btn));

		if (taskInfo.status == TaskInfo.WAITING
				|| taskInfo.status == TaskInfo.RUNNING) {

			tsb_install.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					((OnLineButtonClickListener) context).onDeleteClick(i);
				}
			});
		} else if (taskInfo.status == TaskInfo.SUCCESS) {
			tsb_install.setText("安装");
			tsb_install.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((OnLineButtonClickListener) context).onInstallClick(i);
				}
			});
		} else if (taskInfo.status == TaskInfo.FAILED) {
			tsb_install.setText("重新下载");
			tsb_install.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((OnLineButtonClickListener) context).onRestartClick(i);
				}
			});
		}
		((TvSubButton) view.findViewById(R.id.download_list_del_btn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						((OnLineButtonClickListener) context).onDeleteClick(i);
					}
				});

		if (i == 1) {

			//tsb_install.requestFocus();
		}
		return view;
	}

}
