package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.drawable;

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class FocusScaleShineButton extends Button {
	
	
	protected ImageView btnCover;
	protected RelativeLayout parentView;
	
	
	public FocusScaleShineButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.init();
		
	}

	public FocusScaleShineButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
		this.init();
	}

	public FocusScaleShineButton(Context context, AttributeSet attrs, int defStyle) {
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
			this.addCover();
			this.bringToFront();
			
			this.getParent().requestLayout();
			invalidate();
			
			this.scaleToLarge();
			
		} else {
			this.scaleToNormal();
			//this.clearAnimation();
			this.btnCover.clearAnimation();
			this.removeCover();
		}
	}
	private void addCover(){
		if(this.btnCover==null) {
			return;
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
		
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.btnCover.getLayoutParams();
		params.addRule(RelativeLayout.ALIGN_LEFT, this.getId());
		params.addRule(RelativeLayout.BELOW, this.getId());
		
		int width = this.getLayoutParams().width;
		int bgWidth = (int)getResources().getDimension(R.dimen.px80);
		int bgHeight = (int)getResources().getDimension(R.dimen.px4);
		int coverLeft = -1 * (bgWidth-width)/2;
		int coverTop = (int)getResources().getDimension(R.dimen.px7);
		params.leftMargin = coverLeft;
		params.topMargin = coverTop;
		
		params.width = bgWidth;
		params.height = bgHeight;
		
		
		
	}
	private void removeCover(){
		if (this.parentView!=null) {
			this.parentView.removeView(this.btnCover);
		}
	}
	private void scaleToLarge() {
		

	}
	private void scaleToNormal(){

	}

}
