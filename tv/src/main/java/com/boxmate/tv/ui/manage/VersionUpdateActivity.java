package com.boxmate.tv.ui.manage;

import java.io.File;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;
import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvProgressBar;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.umeng.analytics.MobclickAgent;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class VersionUpdateActivity extends Activity {

	private final String TAG="VersionUpdateActivity";
	private AppDownloadManager manager;
	private TaskInfo taskInfo;
	private TvProgressBar tpb_download;
	private String apk_url;
	private TextView tv_version_name;
	private TextView tv_version_info;
	private TvButton tb_ignore;
	private TvButton tb_update;
	private RelativeLayout rl_root;
	private TextView tv_update_now;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_version_update);
		
		rl_root = (RelativeLayout) findViewById(R.id.rl_root);
		tpb_download = (TvProgressBar) findViewById(R.id.tpb_download);
		tv_version_name = ((TextView)findViewById(R.id.tv_version_name));
		tv_version_info = ((TextView)findViewById(R.id.tv_version_info));
		tv_update_now = ((TextView)findViewById(R.id.tv_update_now));
		
		tb_ignore = (TvButton) findViewById(R.id.tb_ignore);
		tb_update = (TvButton) findViewById(R.id.tb_update);
		
		//读取并设置背景
		//showTheme();
		
		Intent intent =  getIntent();
		String onlineVersionName = (String)intent.getStringExtra("online_version");
		//final String icon_url = (String)intent.getStringExtra("icon_url");
		apk_url = (String)intent.getStringExtra("apk_url");
		
		String updateInfo = intent.getStringExtra("update_info");
		
		tv_version_name.setText(String.format(
				getResources().getString(R.string.setting_update_version_name), onlineVersionName));
		tv_version_info.setText(updateInfo);
		
		tb_ignore.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				finish();
			}
		});
		
		
		tb_update.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tb_update.removeCover();
				rl_root.removeView(tv_version_info);
				rl_root.removeView(tv_version_name);
				rl_root.removeView(tb_ignore);
				rl_root.removeView(tb_update);
				tv_update_now.setVisibility(View.VISIBLE);
				tv_update_now.setText(getResources().getText(R.string.setting_update_now));
				tpb_download.setVisibility(View.VISIBLE);
				update();
			}
		});
		
		tb_update.requestFocus();
	}
	
	protected void onResume() {  
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
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void update(){
		TvHttp http=new TvHttp(getApplicationContext());
		final String tempPath = getFilesDir() + "/"
				+ DataUtil.md5(apk_url)
				+ ".temp.apk";
		final String filePath= getFilesDir() + "/"
				+ DataUtil.md5(apk_url)
				+ ".apk";
		http.download(apk_url, tempPath, new AjaxCallBack<File>() {
			
			@Override
			public void onLoading(long count, long current) {
				tpb_download.setProgress((int) (current * 100 / count));
				super.onLoading(count, current);
			}
			@Override
			public void onSuccess(File t) {
				
				t.renameTo(new File(filePath));
				//CommonUtil.installApkByFilePath(getApplicationContext(), t.getAbsolutePath());
				
				boolean success=false;
				try {
					Log.i(TAG, "安装包路径" + apk_url);
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setDataAndType(Uri.fromFile(new File(filePath)),
							"application/vnd.android.package-archive");
					startActivity(intent);
					success=true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (!success) {
					CommonUtil.deleteFile(getApplicationContext(), apk_url);
				}
				
				VersionUpdateActivity.this.finish();
				
				super.onSuccess(t);
			}
			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg) {
				CommonUtil.deleteFile(getApplicationContext(), apk_url);
				super.onFailure(t, errorNo, strMsg);
			}
		});
	}

	
//	private void showTheme(){
//		ThemeInfo currentTheme = DataUtil.readTheme(getApplicationContext());
//		if (currentTheme.getLocalFlag()) {
//			rl_root.setBackgroundResource(Integer.parseInt(currentTheme.getResId()));
//		}else{
//			Bitmap bmp=BitmapFactory.decodeFile(currentTheme.getResId());
//			rl_root.setBackgroundDrawable(new BitmapDrawable(bmp));
//		}
//		
//		
//	}
}
	
