package com.boxmate.tv.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class HomeTopButton extends Button {
	public HomeTopButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.init();
		
	}

	public HomeTopButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
		this.init();
	}

	public HomeTopButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init Animation
		this.init();
	}
	private void init(){
	}
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			this.bringToFront();
			this.setAlpha(1f);
		} else {
			this.setAlpha(0.5f);
		}
	}
}
