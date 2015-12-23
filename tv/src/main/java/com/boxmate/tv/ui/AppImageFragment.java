package com.boxmate.tv.ui;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;
import android.widget.LinearLayout;

import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.WebImageView;
public class AppImageFragment extends Fragment implements AppDetail.OnPressOkListener {
	private ArrayList<String>urls = new ArrayList<String>();
	private int index = -1;
	private Timer timer;
	private ArrayList<ImageView>pointImageViews = new ArrayList<ImageView>();
	public void setUrls(ArrayList<String> urls) {
		this.urls = urls;
		
	}
	private ImageSwitcher switcher;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		LinearLayout appView =  (LinearLayout)inflater.inflate(R.layout.fragment_app_image, container,false);
		switcher = (ImageSwitcher) appView
				.findViewById(R.id.MainImageSwitcher);

//		switcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
//				R.anim.fade_in));
//		switcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
//				R.anim.fade_out));

		switcher.setFactory(new ViewFactory() {
			@Override
			public View makeView() {
				// TODO Auto-generated method stub
				System.gc();
				
				ImageView imageView = new ImageView(getActivity());
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
						ImageSwitcher.LayoutParams.MATCH_PARENT,
						ImageSwitcher.LayoutParams.MATCH_PARENT));
				return imageView;
			}
		});
		
		for(int i=0;i<urls.size();i++) {
			ImageView pointImageView = new ImageView(getActivity());
			pointImageView.setBackgroundResource(R.drawable.point_gray);
			LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
					(int) getResources().getDimension(R.dimen.px22),
					(int) getResources().getDimension(R.dimen.px22));
			params2.leftMargin = (int)(int) getResources().getDimension(R.dimen.px18);
			
			pointImageView.setLayoutParams(params2);
			LinearLayout pointsLayout = (LinearLayout)appView.findViewById(R.id.image_points);
			pointsLayout.addView(pointImageView);
			pointImageViews.add(pointImageView);
		}
		
		autoLoopStart();
		
		return appView;
	}
	
	
	public void onDestroy() {
		super.onDestroy();
		if(timer!=null) {
			timer.cancel();
		}
	}
	
	
	private Handler goHander = new Handler(){
		public void handleMessage(Message msg) {
			switcher.setImageDrawable((Drawable)msg.obj);
			for(int i=0;i<urls.size();i++) {
				pointImageViews.get(i).setImageResource(R.drawable.point_gray);
			}
			pointImageViews.get(index).setImageResource(R.drawable.point);
			
		}
	};
	public void autoLoopStart(){
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				if(urls.size()>0) {
					index++;
					if(index>=urls.size()) {
						index = 0;
					}
					Message message = goHander.obtainMessage();
					message.obj = CommonUtil.getDrawableByUrl(urls.get(index), getActivity());
					message.sendToTarget();
				}
			}
		}).start();
		
	}
	@Override
	public void onPress() {
		autoLoopStart();
		// TODO Auto-generated method stub
		
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				if(urls.size()>0) {
//					index++;
//					if(index>=urls.size()) {
//						index = 0;
//					}
//					Message message = goHander.obtainMessage();
//					message.obj = WebImageView.getDrawableByUrl(urls.get(index), getActivity());
//					message.sendToTarget();
//				}
//			}
//		}).start();
	}
	@Override
	public void onFocusChange(Boolean isFocus) {
		// TODO Auto-generated method stub
//		if(isFocus) {
//			if(timer!=null) {
//				timer.cancel();
//				timer = null;
//			}
//		} else {
//			autoLoopStart();
//		}
	}
	
}
