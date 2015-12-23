package com.boxmate.tv.ui;



import com.boxmate.tv.R;
import com.umeng.analytics.MobclickAgent;
import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class AppDetailDescActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_detail_desc);
		String desc=getIntent().getStringExtra("descContent");
	
		TextView tv_desc=(TextView) findViewById(R.id.tv_desc);
		tv_desc.setMovementMethod(new ScrollingMovementMethod());
		tv_desc.setText(desc);
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
