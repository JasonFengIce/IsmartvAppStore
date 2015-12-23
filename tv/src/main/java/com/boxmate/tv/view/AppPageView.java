package com.boxmate.tv.view;

import java.util.ArrayList;

import java.util.HashMap;

import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.TvRelativeLayout;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.ui.AppPageFragment.OnAppListener;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.WebImageView;

import android.R.array;
import android.R.integer;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class AppPageView extends RelativeLayout {

	public int page;
	private ArrayList<HashMap<String, Object>> appList;
	public OnAppListener delegate;
	public ArrayList<TvRelativeLayout> frameList = new ArrayList<TvRelativeLayout>();

	public interface OnAppListener {
		public void onAppClick(int page, int position,
				HashMap<String, Object> app);

		public void onAppFocus(int page, int position);

		public void setFirstApp(TvRelativeLayout frame);

		public void setOnPageChangeBeforePosition(int position);
	}

	public AppPageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AppPageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AppPageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setList(ArrayList<HashMap<String, Object>> list) {
		this.appList = list;
		initView();
	}

	public void initView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int index = i * 3 + j;
				if (index >= appList.size()) {
					break;
				}

				int frameId = index + 100;

				TvRelativeLayout frame = (TvRelativeLayout) inflater.inflate(
						R.layout.activity_app_list_item, null);
				frameList.add(frame);
				frame.setTag(index);
				frame.setId(frameId);

				frame.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
						// TODO Auto-generated method stub

						int pos = (Integer) arg0.getTag();

						if (arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

							if ((pos + 1) % 3 == 0) {
								delegate.setOnPageChangeBeforePosition(pos);
							}
						} else if (arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
							if ((pos + 1) % 3 == 1) {
								delegate.setOnPageChangeBeforePosition(pos);
							}
						}

						return false;
					}
				});
				frame.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						// TODO Auto-generated method stub
						if (arg1) {
							delegate.onAppFocus(page, (Integer) arg0.getTag());
						}
					}
				});
				frame.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						HashMap<String, Object> appInfo = appList
								.get((Integer) arg0.getTag());
						// TODO Auto-generated method stub
						delegate.onAppClick(page, (Integer) arg0.getTag(),
								appInfo);
					}
				});

				int topOffset = (int) getContext().getResources().getDimension(
						R.dimen.px50);
				int leftOffset = (int) getContext().getResources()
						.getDimension(R.dimen.px90);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						(int) getContext().getResources().getDimension(
								R.dimen.px490), (int) getContext()
								.getResources().getDimension(R.dimen.px224));

				if (i != 0) {
					params.addRule(RelativeLayout.BELOW, frameId - 3);
				} else {
					params.addRule(RelativeLayout.ALIGN_TOP);
				}

				if (j != 0) {
					params.addRule(RelativeLayout.RIGHT_OF, frameId - 1);

				} else {
					params.addRule(RelativeLayout.ALIGN_LEFT);
				}

				if (i != 0) {
					topOffset = (int) getResources().getDimension(R.dimen.px10);
				}
				if (j != 0) {
					leftOffset = (int) getResources()
							.getDimension(R.dimen.px10);
				}

				params.setMargins(leftOffset, topOffset, 0, 0);

				frame.setLayoutParams(params);
				HashMap<String, Object> appInfo = appList.get(index);

				TvImageView tiv_icon = (TvImageView) frame.findViewById(R.id.tiv_icon);
				tiv_icon.configImageUrl(appInfo.get("icon_url").toString());
				
				
				TextView tv_title = (TextView) frame.findViewById(R.id.tv_title);
				tv_title.setText(appInfo.get("title").toString());
				
				
				TextView tv_controll = (TextView) frame.findViewById(R.id.tv_controll);
				tv_controll.setText(appInfo.get("cname").toString());
				
				
				StarList sl_star = (StarList)frame.findViewById(R.id.sl_star);
				
				sl_star.setStar(Integer.parseInt(appInfo.get("star").toString()));
				
				//判断是否安装
				ImageView iv_installed = (ImageView)frame.findViewById(R.id.iv_installed);
				
				
				if(CommonUtil.checkPackageInstalled(appInfo.get("package").toString())) {
					iv_installed.setVisibility(View.VISIBLE);
					iv_installed.setBackgroundResource(R.drawable.app_installed_cover);
				} 
				
				this.addView(frame);
				if ((page + i + j) == 0) {
					delegate.setFirstApp(frame);
				}
			}
		}
	}
}
