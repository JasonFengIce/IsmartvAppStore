package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.util.CommonUtil;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class FocusScaleLinearLayout extends LinearLayout {
	private ImageView btnCover;
	private RelativeLayout parentView;
	public int page = 0;
	public FocusScaleLinearLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.init();
		
	}

	public FocusScaleLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
		this.init();
	}

	public FocusScaleLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init Animation
		this.init();
	}
	private void init(){
		
		this.btnCover = new ImageView(this.getContext());
		this.btnCover.setBackgroundResource(R.drawable.cursor);
		
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			this.bringToFront();
			this.scaleToLarge(this);
			this.addCover();
		} else {
			this.scaleToNormal(this);
			this.removeCover();
		}
	}
	private void addCover(){
		
		if(this.btnCover==null) {
			
		}
		
		this.parentView = (RelativeLayout)getParent();
		this.parentView.addView(this.btnCover);
		this.btnCover.bringToFront();
		
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.btnCover.getLayoutParams();
		CommonUtil.setBorderParams(params, this);
		this.scaleToLarge(this.btnCover);
	}
	private void removeCover(){
		this.parentView.removeView(this.btnCover);
	}
	
	private void scaleToLarge(View view) {
		
		ObjectAnimator oa=ObjectAnimator.ofFloat(view, "ScaleX", 1f, 1.1f);
		oa.setDuration(100);
		oa.start();
		ObjectAnimator oa2=ObjectAnimator.ofFloat(view, "ScaleY", 1f, 1.1f);
		oa2.setDuration(100);
		oa2.start();
	}
	private void scaleToNormal(View view){
		
		ObjectAnimator oa=ObjectAnimator.ofFloat(view, "ScaleX", 1.1f, 1f);
		oa.setDuration(100);
		oa.start();
		ObjectAnimator oa2=ObjectAnimator.ofFloat(view, "ScaleY", 1.1f, 1f);
		oa2.setDuration(100);
		oa2.start();
	}
	
	public void setImageUrl(final String url) {
		final Handler handler = new Handler(){
			public void handleMessage(Message msg) {
				Drawable drawable = (Drawable)msg.obj;
				setBackgroundDrawable(drawable);
				
			}
		};
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Drawable drawable = CommonUtil.getDrawableByUrl(url, getContext());
				Message msg = handler.obtainMessage();
				msg.obj = drawable;
				msg.sendToTarget();
				
			}
		}).start();
		
	}
	
}
