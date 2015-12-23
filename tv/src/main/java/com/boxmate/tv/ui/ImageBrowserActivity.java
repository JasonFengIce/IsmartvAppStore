package com.boxmate.tv.ui;

import java.util.ArrayList;

import reco.frame.tv.TvBitmap;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.view.WebImageView;

import android.os.Bundle;
import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class ImageBrowserActivity extends Activity {

	private int index = 0;
	private ArrayList<String> urlList = new ArrayList<String>();
	private WebImageView imageView;
	private TvBitmap tb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.tb=TvBitmap.create(getApplicationContext());
		
		setContentView(R.layout.activity_image_browser);

		Intent intent = getIntent();
		index = intent.getIntExtra("index", 0);
		urlList = intent.getStringArrayListExtra("urllist");
		imageView = (WebImageView) findViewById(R.id.imageview);
		tb.display(imageView, urlList.get(index));
		
		Log.i("url", urlList.toString());
		
		freshTip();
		
	}

	public void freshTip() {
		findViewById(R.id.right_tip).setVisibility(View.VISIBLE);
		findViewById(R.id.left_tip).setVisibility(View.VISIBLE);
		
		if(index==0) {
			findViewById(R.id.left_tip).setVisibility(View.INVISIBLE);
		}
		
		if(index==urlList.size()-1) {
			findViewById(R.id.right_tip).setVisibility(View.INVISIBLE);
		}
			
	}
	public boolean onKeyDown(int kCode, KeyEvent kEvent) {

		switch (kCode) {

		case KeyEvent.KEYCODE_DPAD_LEFT:
			
			index--;
			if (index == -1) {
				index = 0;
				break;
			} else {
				tb.display(imageView, urlList.get(index));
			}
			freshTip();
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			index++;
			if (index == urlList.size()) {
				index = urlList.size() - 1;
				break;
			} else {
				tb.display(imageView, urlList.get(index));
			}
			freshTip();
			return true;
		}

		return super.onKeyDown(kCode, kEvent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.image_browser, menu);
		return false;
	}

}
