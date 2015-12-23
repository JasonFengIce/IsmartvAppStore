package com.boxmate.tv.view;

import java.util.ArrayList;
import com.boxmate.tv.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

public class SudokuView extends RelativeLayout {

	private ArrayList<View> views = new ArrayList<View>();

	public void addBoxView(View view) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		int topOffset = (int) getContext().getResources().getDimension(
				R.dimen.px50);
		int leftOffset = (int) getContext().getResources().getDimension(
				R.dimen.px90);
		if (views.size() > 3) {
			params.addRule(RelativeLayout.BELOW, views.get(views.size() - 3)
					.getId());
			topOffset = (int) getResources().getDimension(R.dimen.px18);
		}
		if (views.size() % 3 != 0) {
			params.addRule(RelativeLayout.RIGHT_OF, views.get(views.size() - 1)
					.getId());
			leftOffset = (int) getResources().getDimension(R.dimen.px18);
		}
		params.setMargins(leftOffset, topOffset, 0, 0);
		addView(view);
	}

	public SudokuView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		initView();

	}

	public SudokuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView();
	}

	public SudokuView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub'
		initView();
	}

	public void initView() {

	}

}
