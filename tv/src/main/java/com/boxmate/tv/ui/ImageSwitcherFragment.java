package com.boxmate.tv.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.R.raw;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

import com.boxmate.tv.R;
import com.boxmate.tv.R.anim;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.WebImageView;
public class ImageSwitcherFragment extends Fragment {
	
	private ArrayList<String> urls;
	private int index = -1;
	private ImageSwitcher switcher;
	private Timer	timer;
	public int getIndex() {
		return this.index;
	}
	public void setUrls(ArrayList<String> urls) {
		this.urls = urls;
		autoLoopStart();
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image_switcher,
				container, false);

		switcher = (ImageSwitcher) view
				.findViewById(R.id.MainImageSwitcher);

		switcher.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.fade_in));
		switcher.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
				R.anim.fade_out));

		switcher.setFactory(new ViewFactory() {
			@Override
			public View makeView() {
				// TODO Auto-generated method stub
				System.gc();
				ImageView imageView = new ImageView(getActivity());
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
						(int) getResources().getDimension(R.dimen.px708),
						(int) getResources().getDimension(R.dimen.px466)));
				return imageView;
			}
		});
		
		return view;
	}
	
	public void startLoop() {
		//autoLoopStart();
	}
	
	public void stopLoop() {
//		if(timer!=null) {
//			timer.cancel();
//			timer = null;
//		}
		
	}
	public void killLoop() {
//		if(timer!=null) {
//			timer.cancel();
//			timer = null;
//		}
	}
	private Handler goHander = new Handler(){
		public void handleMessage(Message msg) {
			switcher.setImageDrawable((Drawable)msg.obj);
		}
	};
	public void autoLoopStart(){
		
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
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
		
		
//		
//		if(timer==null) {
//			timer = new Timer();
//			timer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					if(urls==null) {
//						return;
//					}
//					if(urls.size()>0) {
//						index++;
//						if(index>=urls.size()) {
//							index = 0;
//						}
//						
//						Message message = goHander.obtainMessage();
//						message.obj = WebImageView.getDrawableByUrl(urls.get(index), getActivity());
//						message.sendToTarget();
//					}
//				}
//			}, 0, 500000000);
//		}
		
	}
	
	
	public void  onDestroy() {
		super.onDestroy();
		//stopLoop();
	}
}
