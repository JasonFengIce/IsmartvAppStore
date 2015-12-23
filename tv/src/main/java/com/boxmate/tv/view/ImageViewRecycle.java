package com.boxmate.tv.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


import com.boxmate.tv.R;
import com.boxmate.tv.util.CommonUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewRecycle extends ImageView {

	public ImageViewRecycle(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ImageViewRecycle(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImageViewRecycle(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImageDrawable(Drawable drawable) {

		super.setImageDrawable(drawable);
	}

}
