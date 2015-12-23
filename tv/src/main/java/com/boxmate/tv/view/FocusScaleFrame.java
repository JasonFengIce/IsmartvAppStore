package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.anim;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.util.CommonUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class FocusScaleFrame extends LinearLayout {
	private ImageView btnCover;
	private RelativeLayout parentView;
	public FocusScaleFrame(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.init();
		
	}

	public FocusScaleFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
		this.init();
	}

	public FocusScaleFrame(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init Animation
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
			this.scaleToLarge();
			this.addCover();
		} else {
			//this.scaleToNormal();
			this.clearAnimation();
			this.btnCover.clearAnimation();
			this.removeCover();
		}
		getParent().requestLayout();
		invalidate();
	}
	private void addCover(){
		this.parentView = (RelativeLayout)getParent();
		this.parentView.addView(this.btnCover);
		this.btnCover.bringToFront();
		
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.btnCover.getLayoutParams();
		CommonUtil.setBorderParams(params, this);
		
		
		
		Animation ani = AnimationUtils.loadAnimation(this.getContext(), R.anim.scale_to_large);
		ani.setFillAfter(true);
		this.btnCover.startAnimation(ani);
	}
	private void removeCover(){
		this.parentView.removeView(this.btnCover);
	}
	private void scaleToLarge() {
		// TODO Auto-generated method stub
		Animation ani = AnimationUtils.loadAnimation(this.getContext(), R.anim.scale_to_large);
		ani.setFillAfter(true);
		this.startAnimation(ani);
	}
	private void scaleToNormal(){
		Animation ani = AnimationUtils.loadAnimation(this.getContext(), R.anim.scale_to_normal);
		ani.setFillAfter(true);
		this.startAnimation(ani);
	}

}
