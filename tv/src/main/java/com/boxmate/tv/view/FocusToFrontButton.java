package com.boxmate.tv.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FocusToFrontButton extends Button {
	public FocusToFrontButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		
	}

	public FocusToFrontButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// init Animation
	}

	public FocusToFrontButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// init Animation
	}
	
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		if (focused) {
			this.bringToFront();
			this.getParent().requestLayout();
			invalidate();
		} else {
			
		}
	}
	
}
