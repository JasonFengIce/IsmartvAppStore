package com.boxmate.tv.ui.tool;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvLoadingBar;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.string;
import com.boxmate.tv.entity.NetWorkSpeedInfo;
import com.boxmate.tv.util.CommonUtil;

import android.app.Activity;
import android.app.PendingIntent.CanceledException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ToolsSpeed extends Activity {

	private String url = "http://apk.reco.cn/ae48c28357b5b7f1ae2484ff4ad2254d.apk";
	private NetWorkSpeedInfo netSeed;
	private final int START_TEST = 0;
	private final int UPDATE_SPEED = 1;
	private final int UPDATE_STOP = 2;
	private final int UPDATE_DONE = 3;
	private long begin = 0;
	private final int DELAY = 700;
	private double speedCurrent = 0;
	private long speedAverage = 0;
	private long speedTotal = 0;
	private static boolean isRunning;
	private List<Double> list;
	private TextView tv_speed;
	private TextView tv_speed_result;
	private TextView tv_ip;
	private TextView tv_live_result;
	private TextView tv_score_result;
	private TextView tv_hd_result;
	private TextView tv_percent_suffix;
	private RelativeLayout rl_testing, rl_result;
	private TvButton tb_finish;
	private TvLoadingBar tlb_testing;

	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case START_TEST:
				startTest();
				break;
			case UPDATE_DONE:

				if (list != null&&list.size()>0) {

					for (Double item : list) {
						speedTotal += item;
					}

					speedAverage = speedTotal / list.size();
					rl_testing.setVisibility(View.GONE);
					tv_speed.setVisibility(View.GONE);
					rl_result.setVisibility(View.VISIBLE);
					DecimalFormat df = new DecimalFormat("##.0");
					
					if(speedAverage<1000){
						tv_speed_result.setText(getResources()
								.getString(R.string.tools_speed_result)+
								speedAverage + getResources()
								.getString(R.string.suffix_netspeed_k));
					}else{
						tv_speed_result.setText(getResources()
								.getString(R.string.tools_speed_result)+
								df.format(speedAverage/1000.0f) + getResources()
								.getString(R.string.suffix_netspeed_m));
					}
					

					speedGrade(speedAverage);

					tb_finish.setVisibility(View.VISIBLE);
					tb_finish.requestFocus();
					tb_finish.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							isRunning = false;
							ToolsSpeed.this.finish();

						}
					});

				}

				break;

			case UPDATE_STOP:
				isRunning = false;
				tlb_testing.stopAnim();
				Message doneMsg = mHandler.obtainMessage();
				doneMsg.what = UPDATE_DONE;
				mHandler.sendMessageDelayed(doneMsg, 700);
				break;

			case UPDATE_SPEED:
				
				DecimalFormat df = new DecimalFormat("##.0");
				NetWorkSpeedInfo speedTemp = (NetWorkSpeedInfo) msg.obj;
				speedCurrent = speedTemp.getSpeed();
				list.add(speedCurrent);
				//Log.i("speed=", speedCurrent + "---"+speedTemp.getSpeed());
				if (speedCurrent < 1) {
					speedCurrent = 1;
				}else if(speedCurrent<1000){
					tv_percent_suffix.setText(getResources()
							.getString(R.string.suffix_netspeed_k));
				}else{
					tv_percent_suffix.setText(getResources()
							.getString(R.string.suffix_netspeed_m));
					speedCurrent=speedCurrent/1000.0f;
				}
				
				
				
				tv_speed.setText(df.format(speedCurrent));
				break;
			}

		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_speed);
		tv_speed = (TextView) findViewById(R.id.tv_speed);
		tlb_testing = (TvLoadingBar) findViewById(R.id.tlb_testing);
		tv_speed_result = (TextView) findViewById(R.id.tv_speed_result);
		tv_ip = (TextView) findViewById(R.id.tv_ip);
		tv_live_result = (TextView) findViewById(R.id.tv_live_result);
		tv_hd_result = (TextView) findViewById(R.id.tv_hd_result);
		rl_testing = (RelativeLayout) findViewById(R.id.rl_testing);
		rl_result = (RelativeLayout) findViewById(R.id.rl_result);
		tv_score_result = (TextView) findViewById(R.id.tv_score_result);
		tb_finish = (TvButton) findViewById(R.id.tb_finish);
		tv_percent_suffix= (TextView) findViewById(R.id.tv_percent_suffix);

	}

	private void init() {
		isRunning = true;
		list = new ArrayList<Double>();
		netSeed = new NetWorkSpeedInfo();
		netSeed.setHadFinishBytes(0);
		tv_ip.setText(getResources().getString(R.string.tools_speed_ip)
				+ CommonUtil.getWifiInfo(getApplicationContext()));

	}

	@Override
	protected void onStart() {

		init();
		Message msg = mHandler.obtainMessage();
		msg.what = START_TEST;
		mHandler.sendMessageDelayed(msg, DELAY);
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			isRunning = false;
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void startTest() {

		new Thread() {

			public void run() {
				Log.i("开始", "**********开始  ReadFile*******");

				loadFile();
			};
		}.start();

		new Thread() {
			public void run() {
				Log.i("开始",
						netSeed.hadFinishBytes + "---"
								+ netSeed.getTotalBytes());
				while (netSeed.hadFinishBytes < netSeed.getTotalBytes()
						&& isRunning) {

					try {
						//Log.i("开始", "正在测速");
						sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					Message msg = mHandler.obtainMessage();
					msg.what = UPDATE_SPEED;
					msg.obj = netSeed;
					mHandler.sendMessage(msg);
				}

				if (netSeed.hadFinishBytes == netSeed.getTotalBytes()
						&& isRunning) {
					Log.i("开始", "完成");
					mHandler.sendEmptyMessage(UPDATE_STOP);

				}

			};
		}.start();

		// 定时结束
		Message msg = mHandler.obtainMessage();
		msg.what = UPDATE_STOP;
		mHandler.sendMessageDelayed(msg, 7 * 1000);
	}

	private void speedGrade(long speed) {
		int score = 0;
		// 计算等级
		if (speed < 5) {
			score = 0;
		} else if (speed > 5 && speed <= 32) {
			score = 1;
		} else if (speed > 32 && speed <= 64) {
			score = 2;
		} else if (speed > 64 && speed <= 128) {
			score = 3;
		} else if (speed > 128 && speed <= 256) {
			score = 4;
		} else if (speed > 256 && speed <= 512) {
			score = 5;
		} else if (speed > 512 && speed <= 1024) {
			score = 6;
		} else if (speed > 1024 && speed <= 1024 * 2) {
			score = 7;
		} else if (speed > 1024 * 2 && speed <= 1024 * 3) {
			score = 8;
		} else if (speed > 1024 * 3 && speed <= 1024 * 5) {
			score = 9;
		} else {
			score = 10;
		}

		if (score < 1) {
			tv_score_result.setText(String.format(getResources().getString(
					R.string.tools_speed_disconnect)));
		} else if (score < 10) {
			tv_score_result.setText(String.format(
					getResources().getString(R.string.tools_speed_score), score
							+ "0%"));
			SpannableStringBuilder ssb = new SpannableStringBuilder(
					tv_score_result.getText().toString());
			ForegroundColorSpan fcs = new ForegroundColorSpan(Color.WHITE);
			ssb.setSpan(fcs, 9, 13, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			tv_score_result.setText(ssb);
		} else {
			tv_score_result.setText(String.format(getResources().getString(
					R.string.tools_speed_unbelievaable)));

		}
		if (score < 4) {
			tv_live_result.setText(getResources().getString(
					R.string.tools_speed_delay)
					+ "");
		} else {
			tv_live_result.setText(getResources().getString(
					R.string.tools_speed_fluency)
					+ "");
		}
		if (score < 7) {
			tv_hd_result.setText(getResources().getString(
					R.string.tools_speed_delay)
					+ "");
		} else {
			tv_hd_result.setText(getResources().getString(
					R.string.tools_speed_fluency)
					+ "");
		}

	}

	private void loadFile() {

		int currentByte = 0;
		int fileLengh = 0;
		long startTime = 0;
		long intervalTime = 0;

		int byteCount = 0;

		URL urlDownload = null;
		URLConnection urlConnection = null;
		InputStream stream = null;

		try {
			urlDownload = new URL(url);
			urlConnection = urlDownload.openConnection();
			urlConnection.setConnectTimeout(15000);
			urlConnection.setReadTimeout(15000);
			fileLengh = urlConnection.getContentLength();
			stream = urlConnection.getInputStream();
			netSeed.setTotalBytes(fileLengh);
			// fileByte = new byte[fileLengh];
			byte[] buffer = new byte[1024];
			startTime = System.currentTimeMillis();
			while ((currentByte = stream.read(buffer)) != -1) {
				netSeed.hadFinishBytes += currentByte;
				intervalTime = System.currentTimeMillis() - startTime;
//				Log.e("正在下载", netSeed.hadFinishBytes + "---" + intervalTime
//						+ "--" + isRunning);

				if (intervalTime > 100) {
					netSeed.setSpeed(netSeed.getHadFinishBytes() / (intervalTime*1.0f) + 1);
				}

//				if (byteCount < fileLengh) {
//					// fileByte[byteCount++] = (byte) currentByte;
//				}

				if (!isRunning) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (stream != null) {
					stream.close();
				}

			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}


	}

}
