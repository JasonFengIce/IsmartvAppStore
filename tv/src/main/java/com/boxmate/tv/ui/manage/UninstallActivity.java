package com.boxmate.tv.ui.manage;

import java.util.List;
import reco.frame.tv.view.TvLoadingBar;
import com.boxmate.tv.R;
import com.boxmate.tv.adapter.UninstallListAdapter;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.util.DataUtil;
import com.umeng.analytics.MobclickAgent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class UninstallActivity extends Activity {
	private final static int LOAD_SUCCESS = 0;
	public static View mainView;
	private ViewPager vp_list;
	private ImageView right_tip;
	private ImageView left_tip;
	private List<AppInfo> appList;
	private TvLoadingBar tlb_loading;
	private LayoutInflater mInflater;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case LOAD_SUCCESS:

				
				final UninstallListAdapter adapter=new UninstallListAdapter(getApplicationContext(),appList);
				vp_list.setAdapter(adapter);
				vp_list.setOnPageChangeListener(new OnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {

						if (position > 0) {
							left_tip.setVisibility(View.VISIBLE);
						} else {
							left_tip.setVisibility(View.GONE);
						}

						if (position == (adapter.getPageCount() - 1)) {
							right_tip.setVisibility(View.GONE);
						} else {
							right_tip.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {
					}

					@Override
					public void onPageScrollStateChanged(int arg0) {
					}
				});
				if (adapter.getPageCount() > 1) {
					right_tip.setVisibility(View.VISIBLE);
				}
				tlb_loading.setVisibility(View.GONE);
				break;

			}
		};
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mInflater = LayoutInflater.from(this);
		mainView = mInflater.inflate(R.layout.activity_uninstall_list, null);
		setContentView(mainView);

		vp_list = (ViewPager) findViewById(R.id.vp_list);
		right_tip = (ImageView) findViewById(R.id.right_tip);
		left_tip = (ImageView) findViewById(R.id.left_tip);
		tlb_loading = (TvLoadingBar) findViewById(R.id.tlb_loading);
		loadList();
		left_tip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = vp_list.getCurrentItem();
				index = index - 1;
				if (index < 0) {
					return;
				}
				vp_list.setCurrentItem(index, true);
			}
		});
		right_tip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int index = vp_list.getCurrentItem();
				index = index + 1;
				vp_list.setCurrentItem(index, true);
			}
		});
	}

	private void loadList() {

		new Thread() {
			public void run() {

				appList = DataUtil
						.readInstalledAppList(getApplicationContext());
				Message msg = mHandler.obtainMessage();
				msg.what = LOAD_SUCCESS;
				mHandler.sendMessage(msg);
			};
		}.start();

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK) {
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
}
