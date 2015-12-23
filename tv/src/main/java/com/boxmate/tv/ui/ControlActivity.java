package com.boxmate.tv.ui;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.util.CommonUtil;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ControlActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
		
		
		

		CommonUtil.bindGoAppListEvent(this,
				findViewById(R.id.tb_remote), "遥控操作",
				"get_game_list_by_control&control=" + Config.CONTROL_REMOTE,
				RankType.RANK);

		CommonUtil.bindGoAppListEvent(this,
				findViewById(R.id.tb_handle), "手柄操作",
				"get_game_list_by_control&control=" + Config.CONTROL_HANDLE,
				RankType.RANK);

		CommonUtil.bindGoAppListEvent(this,
				findViewById(R.id.tb_mouse), "鼠标游戏",
				"get_game_list_by_control&control=" + Config.CONTROL_MOUSE,
				RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				findViewById(R.id.tb_mobile), "手机操作",
				"get_game_list_by_control&control=" + Config.CONTROL_PHONE,
				RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				findViewById(R.id.tb_body), "体感操作",
				"get_game_list_by_control&control=" + Config.CONTROL_BODY,
				RankType.RANK);
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.control, menu);
		return false;
	}
	
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
