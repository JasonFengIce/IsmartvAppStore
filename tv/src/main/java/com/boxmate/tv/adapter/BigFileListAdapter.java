package com.boxmate.tv.adapter;

import java.util.List;

import reco.frame.tv.view.TvMarqueeText;
import reco.frame.tv.view.component.TvBaseAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.BigFileInfo;
import com.boxmate.tv.util.CommonUtil;

public class BigFileListAdapter extends TvBaseAdapter {

	private Context context;
	private LayoutInflater mInflater;
	private List<BigFileInfo> apkList;
	private PackageManager pm;
	private int[] typeIconIds = { R.drawable.tools_filetype_video,
			R.drawable.tools_filetype_sound, R.drawable.tools_filetype_picture,
			R.drawable.tools_filetype_apk, R.drawable.tools_filetype_other };
	private int height;

	public BigFileListAdapter(Context context, List<BigFileInfo> apkList) {
		this.context = context;
		this.mInflater = LayoutInflater.from(context);
		this.apkList = apkList;
		this.pm = context.getPackageManager();
		this.height = (int) context.getResources().getDimension(R.dimen.px90);
	}

	@Override
	public int getCount() {
		return apkList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return apkList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(final int i, View view, ViewGroup arg2) {
		if (view == null) {
			view = mInflater.inflate(R.layout.tools_bigfile_item, null);
		}
		AbsListView.LayoutParams alp = new AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, height);
		view.setLayoutParams(alp);

		final BigFileInfo bigFile = apkList.get(i);
		ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
		TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
		final TvMarqueeText tv_path = (TvMarqueeText) view
				.findViewById(R.id.tmt_path);
		TextView tv_size = (TextView) view.findViewById(R.id.tv_size);
		tv_name.setText(bigFile.getName());
		tv_path.setText(bigFile.getPath());
		tv_size.setText(bigFile.getSizeString());
		iv_icon.setBackgroundResource(typeIconIds[bigFile.getType()]);
		final Button tb_delete = (Button) view.findViewById(R.id.tb_delete);

		tb_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CommonUtil.deleteFileByPath(context, bigFile.getPath());
				v.setAlpha(0.5F);
				v.setFocusable(false);
				tb_delete.setText(context.getResources().getString(
						R.string.tools_bigfile_deleted));
				// apkList.remove(bigFile);
				// notifyDataSetChanged();

			}
		});

		tb_delete.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean flag) {

				if (flag) {
					tv_path.startMarquee();
				} else {
					tv_path.stopMarquee();
				}

			}
		});

		return view;
	}

}
