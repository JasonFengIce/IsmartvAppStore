package com.boxmate.tv.ui.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvLoadingBar;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.entity.PeripheralInfo;
import com.boxmate.tv.entity.ProcessInfo;
import com.boxmate.tv.util.SystemUtil;
import com.boxmate.tv.view.FocusScaleButton;
import com.umeng.analytics.MobclickAgent;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ToolsFlush extends Activity {

	private final static String TAG = "ToolsFlush";
	private final static int SEARCH_SUCCESS = 0;
	private final static int START_ANIMATION = 1;
	private final static int CLEAR_SHOW_NAME = 2;
	private final static int CLEAR_MEMORY_CHANGE = 3;
	private final static int CLEAR_MEMORY_FINISH = 4;
	private final static int SYSTEM_CLEAN = 5;
	private final static int STOP_ANIMATION = 6;
	private int tempProgress, currentProgress, leftProgress;
	private final static int SPEED = 477;
	private final static int DELAY = 700;
	private final static int COL = 7;// 列数
	private int period = 23, clearInterval, processTotalSize;
	private int clearIndex = 0;
	private boolean isPlus, isClearing;
	private TvLoadingBar tlb_searching;
	private TextView tv_percent;
	private TextView tv_app_name;
	private TextView tv_clear_hint;
	private int iconSize, rowSpace, marginTop, marginLeft;
	private PackageManager pm;
	private List<ProcessInfo> processList;
	private LayoutInflater mInflater;
	private TextView tv_clear_memory,tv_clear_memory_hint;
	private TextView tv_clear_count,tv_clear_count_hint;
	private RelativeLayout rl_result, rl_cleaning;
	private TvButton tb_finish;
	private long totalMemory;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {

			case START_ANIMATION:
				break;
			case STOP_ANIMATION:
				
				tlb_searching.stopAnim();
				break;
			case CLEAR_SHOW_NAME:

				// Log.e(TAG,
				// "clearIndex="+clearIndex+"---"+processList.size());
				if (clearIndex < processList.size()) {
					showAppName(clearIndex);
				}
				clearIndex++;
				break;
			case CLEAR_MEMORY_CHANGE:
				if (tempProgress < 10) {
					tv_percent.setText("0" + tempProgress);
				} else {
					tv_percent.setText(tempProgress + "");
				}
				break;

			case SYSTEM_CLEAN:

				isClearing = false;
				rl_cleaning.setVisibility(View.GONE);
				rl_result.setVisibility(View.VISIBLE);
				tb_finish.setVisibility(View.VISIBLE);
				tb_finish.requestFocus();
				tb_finish.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ToolsFlush.this.finish();

					}
				});
				tv_clear_hint.setText(getResources().getString(R.string.tools_flush_best));
				tv_clear_count_hint.setVisibility(View.GONE);
				tv_clear_memory_hint.setVisibility(View.GONE);

				break;

			case CLEAR_MEMORY_FINISH:
				isClearing = false;
				rl_cleaning.setVisibility(View.GONE);
				tb_finish.setVisibility(View.VISIBLE);
				tb_finish.requestFocus();
				tb_finish.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ToolsFlush.this.finish();

					}
				});
				rl_result.setVisibility(View.VISIBLE);
				int clearPercent = Math.abs(currentProgress - tempProgress) + 1;
				String clearMemory = formatSize(totalMemory * clearPercent
						/ 100);
				tv_clear_hint.setText(String.format(
						getResources().getString(R.string.tools_flush_hint),
						clearPercent
								+ getResources().getString(
										R.string.suffix_percent)));
				tv_clear_memory.setText(clearMemory);
				tv_clear_count.setText(processList.size()
						+ getResources().getString(
								R.string.tools_flush_clear_count_suffix));
				//
				// new Handler().postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				//
				// // ToolsFlush.this.finish();
				//
				// }
				// }, 1100);

				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_flush);
		pm = getPackageManager();
		tlb_searching = (TvLoadingBar) findViewById(R.id.tlb_searching);
		tv_percent = (TextView) findViewById(R.id.tv_percent);
		tv_clear_memory = (TextView) findViewById(R.id.tv_clear_memory);
		tv_clear_count = (TextView) findViewById(R.id.tv_clear_count);
		tv_clear_memory_hint = (TextView) findViewById(R.id.tv_clear_memory_hint);
		tv_clear_count_hint = (TextView) findViewById(R.id.tv_clear_count_hint);
		tv_app_name = (TextView) findViewById(R.id.tv_app_name);
		tv_clear_hint = (TextView) findViewById(R.id.tv_clear_hint);
		rl_result = (RelativeLayout) findViewById(R.id.rl_result);
		tb_finish = (TvButton) findViewById(R.id.tb_finish);
		rl_cleaning = (RelativeLayout) findViewById(R.id.rl_cleaning);

	}

	@Override
	protected void onStart() {
		initValues();
		init();
		super.onStart();
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
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			isClearing = false;
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void init() {

		// 于主进程获取所有运行进程

		processList = SystemUtil
				.queryAllRunningAppInfo(getApplicationContext());

		// 计算进程总内存
		for (ProcessInfo process : processList) {
			processTotalSize += process.getMemory();
			// Log.e(TAG,
			// process.getPackageName() + "--" + process.getProcessName());
		}
		// 获取可用内存当前百分比
		long availMemory = getAvailMemory();
		totalMemory = getTotalMemory();

		currentProgress = Math
				.abs((100 - (int) (availMemory * 100 / totalMemory)));
		leftProgress = Math
				.abs((100 - (int) ((availMemory + processTotalSize) * 100 / totalMemory)));

		// 假数
		if (leftProgress > 20) {
			leftProgress = leftProgress - 9;
		}

		// 计算图标出现间隔
		clearInterval = (currentProgress + leftProgress)
				/ (processList.size() + 1);
		if (clearInterval < 7) {
			clearInterval = 7;
		}

		if (currentProgress < 10) {
			tv_percent.setText(" " + currentProgress);
		} else {
			tv_percent.setText(currentProgress + "");
		}

		if (processList.size() < 1) {
			mHandler.sendEmptyMessage(SYSTEM_CLEAN);
			return;
		}

		// 延时启动旋转动画
		if (isClearing) {
			playClearAnimation();
			Message msg = mHandler.obtainMessage();
			msg.what = START_ANIMATION;
			mHandler.sendMessageDelayed(msg, DELAY);

		}

		// 异步杀死所有进程
		new Thread() {
			public void run() {
				try {
					SystemUtil.killRunningTask(getApplicationContext());
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
	}

	private void initValues() {
		mInflater = LayoutInflater.from(getApplicationContext());
		marginLeft = (int) getResources().getDimension(R.dimen.px17);
		rowSpace = (int) getResources().getDimension(R.dimen.px11);
		isClearing = true;
		isPlus = false;
		processList = new ArrayList<ProcessInfo>();

		// ValueAnimator transAnimatorX = ObjectAnimator.ofFloat(iv_circle,
		// "x", xBefore, x);
		// ValueAnimator transAnimatorY = ObjectAnimator.ofFloat(iv_circle,
		// "y", yBefore, y);

		// AnimatorSet animatorSet = new AnimatorSet();
		// animatorSet.play(rotateAnimator);
		// animatorSet.setDuration(SPEED);
		// animatorSet.setInterpolator(new DecelerateInterpolator(3));
	}

	/**
	 * 启动清理旋转动画
	 * 
	 * @param childView
	 */
	private void playClearAnimation() {

		tempProgress = currentProgress;
		isPlus = true;
		// 获得清理后的百分比
		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (!isClearing) {
					return;
				}
				try {
					// 启动清理内存
					Message msg = new Message();
					msg.what = CLEAR_MEMORY_CHANGE;
					if (isPlus) {
						msg.obj = tempProgress;
						mHandler.sendMessage(msg);
						tempProgress -= 1;
						isPlus = tempProgress <= 0 ? false : true;
					} else {

						tempProgress++;
						if (tempProgress >= leftProgress - 1) {
							if (clearIndex >= processList.size()) {
								msg.what = CLEAR_MEMORY_FINISH;
								msg.obj = tempProgress;
								mHandler.sendMessageDelayed(msg, DELAY);
								mHandler.sendEmptyMessage(STOP_ANIMATION);
								this.cancel();
								return;
							}

						} else {
							msg.obj = tempProgress;
							mHandler.sendMessage(msg);
						}

					}
					// 逐步消除进程图标
					if (tempProgress % clearInterval == 0) {
						mHandler.sendEmptyMessage(CLEAR_SHOW_NAME);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, DELAY, period);

	}

	private long getAvailMemory() {
		// 获取android当前可用内存大小
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; 当前系统的可用内存
		// return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
		return mi.availMem / 1024;// KB
	}

	private static long getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return Formatter.formatFileSize(context, initial_memory);//
		// Byte转换为KB，内存大小规格化
		return Math.abs(initial_memory / 1024);
	}

	private void showAppName(int clearIndex) {
		try {
			ProcessInfo process = processList.get(clearIndex);
			tv_app_name.setText(process.getAppName());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String formatSize(int size) {

		if (size < 1024) {
			return "7M";
		} else {
			String result = ((size / 1024) + 7) + "M";
			return result;
		}
	}

	private String formatSize(long size) {

		if (size < 1024) {
			return "7M";
		} else {
			String result = ((size / 1024) + 7) + "M";
			return result;
		}
	}
}
