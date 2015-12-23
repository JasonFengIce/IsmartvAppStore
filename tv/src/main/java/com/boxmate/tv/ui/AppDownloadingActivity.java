package com.boxmate.tv.ui;

import reco.frame.tv.TvBitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.WebImageView;
import com.umeng.analytics.MobclickAgent;

public class AppDownloadingActivity extends Activity implements
		SecurityService.OnDownloadStatusChanaged {
	private String packageName = null;
	private SecurityService mService;
	private TaskInfo taskInfo;
	private Boolean isFreshInfo = false;
	private Boolean isWaitingInstalled = false;
	private ProgressBar progressBar;
	private TextView downloadedTextView;
	private TvBitmap tb;

	private void freshView() {
		if (taskInfo != null) {
			((TextView) findViewById(R.id.app_loading_title)).setText(taskInfo
					.getTaskName());
			WebImageView webImageView = ((WebImageView) findViewById(R.id.app_loading_icon));
			tb.display(webImageView, taskInfo.iconUrl);
		}
	}

	public void setPackageName(String name) {
		packageName = name;
	}

	protected void onResume() {
		super.onResume();
		if (isWaitingInstalled) {
			// 检查是否安装成功
		}
		isWaitingInstalled = true;
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tb =TvBitmap.create(getApplicationContext());

		setContentView(R.layout.activity_app_downloading);
		progressBar = (ProgressBar) findViewById(R.id.app_download_progress);
		downloadedTextView = (TextView) findViewById(R.id.app_download_info);
		Intent intent = getIntent();
		setPackageName(intent.getStringExtra("package"));
		int id = intent.getIntExtra("id", 0);
		final AppDownloadManager manager = AppDownloadManager.getInstance();
		taskInfo = manager.getTaskInfoByAppId(id);
		freshView();
		Intent serviceIntent = new Intent(this, SecurityService.class);
		startService(serviceIntent);
		findViewById(R.id.tb_cancel)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (taskInfo != null) {
							manager.delTaskById(getApplicationContext(),
									taskInfo);
							AppDownloadingActivity.this.finish();
						}
					}
				});
		findViewById(R.id.tb_back)
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						AppDownloadingActivity.this.finish();
					}
				});

	}

	@Override
	protected void onStart() {
		// 设定依赖
		SecurityService.delegate = this;
		super.onStart();
	}

	protected void onDestroy() {
		// mService.delegate = null;
		// unbindService(mServiceConn);
		super.onDestroy();
	}

	@Override
	public void onTaskUpdate(String packageName, int action) {
		try {
			switch (action) {
			case SecurityService.DOWNLOAD_STATUS_UPDATE:

				if (packageName.equals(taskInfo.getPackageName())) {
					Log.i("package状态发生改变", packageName + ":" + action);
					progressBar.setProgress(taskInfo.getProgress());
					float loaded = (float) taskInfo.getDownloadSize()
							/ (float) 1024 / (float) 1024;
					float total = (float) taskInfo.fileSize /(float) 1024 / (float) 1024;
				
					downloadedTextView.setText(String.format("%.1fM/%.1fM",
							loaded, total));
				}

				break;

			case SecurityService.DOWNLOAD_STATUS_SUCCESS:
				if (packageName.equals(taskInfo.getPackageName())) {
					finish();
				}

				break;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
