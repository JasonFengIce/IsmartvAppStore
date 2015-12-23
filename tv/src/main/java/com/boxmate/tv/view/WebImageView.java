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

public class WebImageView extends ImageView {

	private static Boolean downloading = false;
	private static ArrayList<WebImageInfo> imageQueue = new ArrayList<WebImageInfo>();
	public Boolean showLoading = true;

	private static Handler onePicLoadedHandler = new Handler() {
		public void handleMessage(Message msg) {
			WebImageInfo imageViewInfo = (WebImageInfo) msg.obj;
			imageViewInfo.imageView.setImageDrawable(imageViewInfo.drawable);
			System.gc();
		}
	};

	private static void startDownload() {
		if (downloading) {
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				downloading = true;
				while (imageQueue.size() > 0) {
					WebImageInfo imageInfo = imageQueue.get(0);
					if (imageInfo != null) {

						Log.i("开始下载", imageInfo.url);
						Drawable drawable = CommonUtil.getDrawableByUrl(
								imageInfo.url, imageInfo.imageView.getContext());
						Log.i("下载完毕", imageInfo.url);

						if (drawable != null) {
							imageInfo.drawable = drawable;

							if (drawable != null) {
								Message message = onePicLoadedHandler
										.obtainMessage();
								message.obj = imageInfo;
								message.sendToTarget();
							}
						} else {
							if (imageInfo.tryNumberRemain > 0) {
								Log.i("开始重试", "还剩下" + imageInfo.tryNumberRemain
										+ "次," + imageInfo.url);
								imageInfo.tryNumberRemain--;
								imageQueue.add(imageInfo);// 重新加入队列
							}
						}
					}
					imageQueue.remove(imageInfo);
					// Log.i("当前队列",imageQueue.toString());
				}
				downloading = false;
			}
		}).start();
	}

	public WebImageView(Context context) {
		super(context);
	}

	public WebImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WebImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@SuppressLint("HandlerLeak")
	public void setImageUrl(final String url) {
		if (showLoading) {
			setImageResource(R.drawable.image_loading);
		}
		
		if (url == null) {
			Log.i("图片图片", "有个图片的url为空");
			return;
		}

		WebImageInfo webImageInfo = new WebImageInfo(this, url);
		imageQueue.add(webImageInfo);
		startDownload();
	}
}
