package com.boxmate.tv.view;

import java.util.ArrayList;

import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.drawable;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StarList extends LinearLayout {
	public ArrayList<ImageView> starList = new ArrayList<ImageView>();
	private int starNum = 0;
	public StarList(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	public StarList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public StarList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setStar(int num) {
		num = Math.min(num, 5);
		starNum = num;
		for(int i = 0;i < num; i++) {
			starList.get(i).setImageResource(R.drawable.star_white);
		}
		for(int i = num;i < 5; i++) {
			starList.get(i).setImageResource(R.drawable.star_hollow);
		}
	}
	
	public void init() {
		for (int i = 0; i < 5; i++) {
			ImageView imageView = new ImageView(getContext());
			starList.add(imageView);
			imageView.setImageResource(R.drawable.star_gray);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					(int)getContext().getResources().getDimension(R.dimen.px32),
					(int)getContext().getResources().getDimension(R.dimen.px31));
			params.rightMargin = (int)getContext().getResources().getDimension(R.dimen.px10);
			imageView.setLayoutParams(params);
			addView(imageView);
		}
	}
}
