package com.boxmate.tv.view;

import java.util.Map;

import reco.frame.tv.view.component.TvUtil;

import com.boxmate.tv.R;
import com.boxmate.tv.util.CommonUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TvKeyboard extends RelativeLayout {

	private OnKeyInputListner mKeyClickListner;
	private int textSizeFunction, textSizeKey, keyWidth, keyHeight, spaceHori,
			spaceVert;
	private int functionTypeWidth, functionClearWidth, functionDeleteWidth,
			functionHeight;
	private int paddingLeft, paddingTop;
	private int marginL1, marginL2, marginL3, marginT;
	private int deletePadding,deletePaddingRight;
	private int textColor, backgroundColor;
	private int col = 5;
	private String functionTypeNo, functionTypeLetter, functionClear,
			functionDelete;
	private SparseArray<Integer> letterIds, numberIds;

	public TvKeyboard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TvKeyboard(Context context) {
		this(context, null);
	}

	public TvKeyboard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		this.textSizeFunction = (int) getResources().getDimension(R.dimen.px36);
		this.textSizeKey = (int) getResources().getDimension(R.dimen.px36);
		this.keyWidth = (int) getResources().getDimension(R.dimen.px70);
		this.keyHeight = (int) getResources().getDimension(R.dimen.px70);
		this.functionTypeWidth = (int) getResources().getDimension(
				R.dimen.px190);
		this.functionClearWidth = (int) getResources().getDimension(
				R.dimen.px90);
		this.functionDeleteWidth = (int) getResources().getDimension(
				R.dimen.px96);
		this.functionHeight = (int) getResources().getDimension(R.dimen.px60);
		this.spaceHori = (int) getResources().getDimension(R.dimen.px20);
		this.spaceVert = (int) getResources().getDimension(R.dimen.px19);
		this.paddingLeft = (int) getResources().getDimension(R.dimen.px4);
		this.paddingTop = (int) getResources().getDimension(R.dimen.px42);
		this.marginL1 = (int) getResources().getDimension(R.dimen.px2);
		this.marginL2 = (int) getResources().getDimension(R.dimen.px233);
		this.marginL3 = (int) getResources().getDimension(R.dimen.px334);
		this.marginT = (int) getResources().getDimension(R.dimen.px92);
		this.deletePadding= (int) getResources().getDimension(R.dimen.px12);
		this.deletePaddingRight= (int) getResources().getDimension(R.dimen.px21);
		this.textColor = Color.WHITE;
		this.functionTypeNo = getResources().getString(
				R.string.search_keyboard_no);
		this.functionTypeLetter = getResources().getString(
				R.string.search_keyboard_letter);
		this.functionClear = getResources().getString(
				R.string.search_keyboard_clear);
		this.functionDelete = "functionDelete";

		letterIds = new SparseArray<Integer>();
		numberIds = new SparseArray<Integer>();

		init();
	}

	private void init() {

		buildFunction();
		buildLetters();
	}

	private void buildFunction() {
		for (int i = 0; i < 3; i++) {

			TextView key = new TextView(getContext());
			key.setFocusable(true);
			key.setTextColor(textColor);
			key.setTextSize(CommonUtil.Px2Dp(getContext(), textSizeFunction));
			key.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams rlp = null;
			int l = 0, t = 0;
			switch (i) {
			case 0:
				rlp = new RelativeLayout.LayoutParams(functionTypeWidth,
						functionHeight);
				key.setText(functionTypeNo);
				key.setTag(functionTypeLetter);
				l = paddingLeft + marginL1;
				break;
			case 1:
				rlp = new RelativeLayout.LayoutParams(functionClearWidth,
						functionHeight);
				key.setText(functionClear);
				key.setTag(functionClear);
				l = marginL2;
				break;
			case 2:
				ImageView delete = new ImageView(getContext());
				delete.setPadding(deletePadding, deletePadding, deletePaddingRight, deletePadding);
				rlp = new RelativeLayout.LayoutParams(functionDeleteWidth,
						functionHeight);
				delete.setImageResource(R.drawable.search_delete);
				delete.setTag(functionDelete);
				delete.setFocusable(true);
				l = marginL3;
				delete.setOnClickListener(mClickListner);
				delete.setOnFocusChangeListener(mFocusListener);
				rlp.setMargins(l, t, 0, 0);
				this.addView(delete, rlp);
				continue;
			}
			// 默认居中
			rlp.setMargins(l, t, 0, 0);
			key.setOnClickListener(mClickListner);
			key.setOnFocusChangeListener(mFocusListener);
			this.addView(key, rlp);
		}
	}

	private void buildLetters() {
		for (int i = 0; i < keyArray.length; i++) {
			TextView key = new TextView(getContext());
			key.setFocusable(true);
			key.setTextColor(textColor);
			key.setTextSize(CommonUtil.Px2Dp(getContext(), textSizeKey));
			key.setText(keyArray[i]);
			key.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
					keyWidth, keyHeight);
			// 默认居中
			int l = (spaceHori + keyWidth) * (i % col) + paddingLeft, t = (spaceVert + keyHeight)
					* (i / col) + marginT;
			rlp.setMargins(l, t, 0, 0);
			int id = TvUtil.buildId();
			letterIds.put(i, id);
			key.setId(id);
			key.setTag(keyArray[i]);
			key.setOnClickListener(mClickListner);
			key.setOnFocusChangeListener(mFocusListener);
			if (i==12) {
				key.requestFocus();
			}
			this.addView(key, rlp);

		}
	}

	private void buildNumbers() {
		for (int i = 0; i < noArray.length; i++) {
			TextView key = new TextView(getContext());
			key.setFocusable(true);
			key.setTextColor(textColor);
			key.setTextSize(CommonUtil.Px2Dp(getContext(), textSizeFunction));
			key.setText(noArray[i]);
			key.setTag(noArray[i]);
			key.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
					keyWidth, keyHeight);
			// 默认居中
			int l = (spaceHori + keyWidth) * (i % col) + paddingLeft, t = (spaceVert + keyHeight)
					* (i / col) + marginT;
			int id = TvUtil.buildId();
			numberIds.put(i, id);
			key.setId(id);
			key.setOnClickListener(mClickListner);
			key.setOnFocusChangeListener(mFocusListener);
			rlp.setMargins(l, t, 0, 0);
			this.addView(key, rlp);
		}
	}

	private String[] keyArray = new String[] { "A", "B", "C", "D", "E", "F",
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
			"T", "U", "V", "W", "X", "Y", "Z" };

	private String[] noArray = new String[] { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9" };

	private OnClickListener mClickListner = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String keyStr = v.getTag().toString();

			if (keyStr.equals(functionTypeLetter)) {

				for (int i = 0; i < letterIds.size(); i++) {
					View key = findViewById(letterIds.get(i));
					TvKeyboard.this.removeView(key);
				}
				v.setTag(functionTypeNo);
				buildNumbers();
				((TextView) v).setText(functionTypeLetter);

			} else if (keyStr.equals(functionTypeNo)) {
				for (int i = 0; i < numberIds.size(); i++) {
					View key = findViewById(numberIds.get(i));
					TvKeyboard.this.removeView(key);
				}
				v.setTag(functionTypeLetter);
				((TextView) v).setText(functionTypeNo);
				buildLetters();
			} else if (keyStr.equals(functionClear)) {
				// 清空
				if (mKeyClickListner != null)
					mKeyClickListner.onKeyClear();
			} else if (keyStr.equals(functionDelete)) {
				// 退格
				if (mKeyClickListner != null)
					mKeyClickListner.onKeyDelete();
			} else {
				// 传递
				if (mKeyClickListner != null)
					mKeyClickListner.onKeyInput(keyStr);
			}

		}
	};
	private OnFocusChangeListener mFocusListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				v.setBackgroundResource(R.drawable.shape_keyboard_cursor);
			} else {
				v.setBackgroundResource(0);
			}

		}
	};

	public void setOnKeyInputListener(OnKeyInputListner listener) {
		this.mKeyClickListner = listener;
	}

	public interface OnKeyInputListner {
		public void onKeyInput(String keyStr);

		public void onKeyDelete();

		public void onKeyClear();
	}

}
