package com.boxmate.tv.view;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class WebImageInfo {

	public ImageView imageView;
	public String url;
	public Drawable drawable;
	public int tryNumberRemain = 3;// 重试三次放弃
	public Boolean noCache = false;

	public WebImageInfo(ImageView imageView, String url) {
		this.imageView = imageView;
		this.url = url;
	}

	public String toString() {
		return url;
	}
}
