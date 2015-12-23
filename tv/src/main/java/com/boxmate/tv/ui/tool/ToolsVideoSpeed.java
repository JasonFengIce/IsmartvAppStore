package com.boxmate.tv.ui.tool;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.Config;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
public class ToolsVideoSpeed extends Activity {

	private ImageView iv_bg;
	private Boolean speedFlag;
	private Editor editor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//读取状态
		SharedPreferences sp=getSharedPreferences(Config.SETTING, MODE_MULTI_PROCESS);
		speedFlag=sp.getBoolean(Config.SETTING_VIDEO_FLAG, false);
		editor = sp.edit();
		setContentView(R.layout.tools_video);
		iv_bg = (ImageView) findViewById(R.id.iv_bg);
		if (speedFlag) {
			iv_bg.setBackgroundResource(R.drawable.tools_video_bg_close);
		}else{
			iv_bg.setBackgroundResource(R.drawable.tools_video_bg_open);
		}
		iv_bg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (speedFlag) {
					speedFlag=false;
					v.setBackgroundResource(R.drawable.tools_video_bg_open);
					editor.putBoolean(Config.SETTING_VIDEO_FLAG, false);
					sendBroadcast(new Intent(Config.INTENT_VIDEO_OBSERVER_CLOSE));
					
				}else{
					speedFlag=true;
					v.setBackgroundResource(R.drawable.tools_video_bg_close);
					editor.putBoolean(Config.SETTING_VIDEO_FLAG, true);
					sendBroadcast(new Intent(Config.INTENT_VIDEO_OBSERVER_OPEN));
				}
				editor.commit();
			}
		});
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode==KeyEvent.KEYCODE_BACK) {
			ToolsVideoSpeed.this.finish();
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
