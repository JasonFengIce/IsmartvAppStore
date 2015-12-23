package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.util.CommonUtil;

import android.R.integer;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FocusScaleButton extends Button {
	
	
	protected ImageView btnCover;
	protected RelativeLayout parentView;
	public int page = 0;
	
	public FocusScaleButton(Context context) {
		super(context);
		this.init();
	}

	public FocusScaleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	public FocusScaleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.init();
	}
	private void init(){
		
		this.btnCover = new ImageView(this.getContext());
		this.btnCover.setBackgroundResource(R.drawable.cursor);
		//this.getContext().getResources().getDrawable(R.drawable.border_selected);
		//this.btnCover.setBackground();
		
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
			//this.scaleToNormal(this.btnCover);
			//this.clearAnimation();
			//this.btnCover.clearAnimation();
			this.removeCover();
		}
	}
	private void addCover(){
		if(this.btnCover==null) {
			return ;
		}
		this.parentView = (RelativeLayout)getParent();
		if(this.parentView==null) {
			return ;
		}
		if(this.btnCover.getParent()==null) {
			this.parentView.addView(this.btnCover);
		} else {
			return;
		}
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
