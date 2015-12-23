package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AppListBottomButton extends Button {
	public Boolean isFocus = false;
	public interface OnButtonFocusChangeListener{
		public void onButtonFocusChange(AppListBottomButton button,Boolean focus);
	}
	protected ImageView btnCover;
	protected RelativeLayout parentView;
	public AppListBottomButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.init();
		
	}

	public AppListBottomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
		this.init();
	}

	public AppListBottomButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init Animation
		this.init();
	}
	private void init(){
		
		this.btnCover = new ImageView(this.getContext());
		this.btnCover.setBackgroundResource(R.drawable.shape_line_bold_white);
	}
	
	public void focus(Boolean isFocus) {
		onFocusChanged(isFocus, 0, null);
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			if(!isFocus) {
				this.bringToFront();
				this.scaleToLarge();
				this.addCover();
			}
			isFocus = true;
		} else {
			if(isFocus) {
				this.scaleToNormal();
				this.btnCover.clearAnimation();
				this.removeCover();
			}
			isFocus = false;
		}
		
		try {
			((OnButtonFocusChangeListener)getContext()).onButtonFocusChange(this,focused);
		} catch (Exception e) {
		}
		
	}
	private void addCover(){
		
		if(this.btnCover==null) {
			
		}
		
		this.parentView = (RelativeLayout)getParent();
		this.parentView.addView(this.btnCover);
		this.btnCover.bringToFront();
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.btnCover.getLayoutParams();
		int bgWidth = (int)getResources().getDimension(R.dimen.px70);
		int bgHeight = (int)getResources().getDimension(R.dimen.px4);
		params.leftMargin = this.getLeft()+(getLayoutParams().width-bgWidth)/2;
		params.topMargin = this.getTop()+getLayoutParams().height+(int)getResources().getDimension(R.dimen.px3);
		params.width = bgWidth;
		params.height = bgHeight;
		
//		this.btnCover.layout(
//				coverLeft, 
//				coverTop, 
//				coverLeft+this.getWidth(), 
//				coverTop+this.getHeight()
//				);
//		
	}
	private void removeCover(){
		this.parentView.removeView(this.btnCover);
	}
	private void scaleToLarge() {
		
	}
	private void scaleToNormal(){
		
	}
	

}
