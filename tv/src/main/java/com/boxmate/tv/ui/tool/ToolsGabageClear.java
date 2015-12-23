package com.boxmate.tv.ui.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import reco.frame.tv.view.TvButton;
import reco.frame.tv.view.TvProgressBar;

import com.boxmate.tv.R;
import com.boxmate.tv.util.CacheHelper;
import com.boxmate.tv.util.CacheHelper.SyncCallBack;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ToolsGabageClear extends Activity {

	private final String TAG = "ToolsGabageClear";
	private final static int SEARCH_UPDATE = 0;
	private final static int SEARCH_FINISH = 1;
	private final static int START_CLEAR = 2;
	private final static int SHOW_PROGRESS = 3;
	private final static int SHOW_SEARCH_FLASH = 9;
	private final static int SHOW_CLEAR_FLASH = 10;
	private final static int CLEAR_UPDATE = 4;
	private final static int CLEAR_UNINSTAll = 5;
	private final static int CLEAR_CACHE = 6;
	private final static int CLEAR_OTHER = 7;
	private final static int CLEAR_FINISH = 8;
	private final int PROGRESS_MAX = 87, PROGRESS_MIN = 11;
	private final int GABAGE_MIN = 1 * 1024 * 1024;// 小于此数则不清理
	private int PERIOD = 230, PERIOS_FLASH = 7, SCAN_SLEEP = 21,
			CLEAN_SLEEP = 30;
	private int clearMax;
	private static int currentProcess;
	private static long totalSize, clearSize, uninstallSize, cacheSize,
			otherSize;
	private int totalCount, clearCount;
	private List<GabageInfo> uninstallPathList, cachePathList, otherPathList;
	private TvProgressBar tpb_clear;
	private TextView tv_size, tv_size_suffix;
	private TextView tv_option;
	private static boolean searchFlag, searchFinish, clearFlag;
	private long systemTime;
	private TvButton tb_finish;
	private TextView tv_size_total;
	private TextView tv_size_history;
	private TextView tv_state, tv_path;
	private RelativeLayout rl_running, rl_result;
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SEARCH_UPDATE:
				String path = (String) msg.obj + "";
				if (totalSize > GABAGE_MIN && searchFlag) {

					if (totalSize < 1000 * 1000 * 1000) {
						tv_size_suffix.setText(getResources().getString(
								R.string.suffix_size_m));
					} else {
						tv_size_suffix.setText(getResources().getString(
								R.string.suffix_size_g));
					}

					tv_size.setText(parseApkSize(totalSize));
				}

				if (path.length() > 31) {
					path = path.substring(0, 13) + "..."
							+ path.substring(path.length() - 17);
				}else if(path.length()<10){
					path=getFilesDir().getAbsolutePath();
				}
				tv_path.setText(path);
				
				break;
			case SEARCH_FINISH:
				searchFlag = false;
				if (totalSize > GABAGE_MIN) {
					totalCount = uninstallPathList.size()
							+ cachePathList.size() + otherPathList.size() + 1;
					// 计算清理进度动画终点位置
					if (totalCount < 13) {
						clearMax = 13;
					} else if (totalCount < 31) {
						clearMax = 31;
					} else {
						clearMax = 71;
					}

					// 延时启动清理
					Message startClear = mHandler.obtainMessage();
					startClear.what = START_CLEAR;
					mHandler.sendMessageDelayed(startClear, 1100);

				} else {
					rl_running.setVisibility(View.GONE);
					rl_result.setVisibility(View.VISIBLE);
					tb_finish.setVisibility(View.VISIBLE);
					tb_finish.requestFocus();
					tv_size_total.setText(getResources().getString(
							R.string.tools_gc_without));
				}

				break;
			case START_CLEAR:
				tv_state.setText(getResources().getString(
						R.string.tools_gc_cleanning));
				tv_path.setText(getResources().getString(
						R.string.tools_gc_cache));
				clearFlag = true;
				startClear();
				break;
			case SHOW_SEARCH_FLASH:
				showFlash(0);
				break;
			case SHOW_CLEAR_FLASH:
				showFlash(1);
				break;
			case SHOW_PROGRESS:
				if (currentProcess < 1) {
					currentProcess = 0;
				}
				tpb_clear.setProgress(currentProcess);
				break;

			case CLEAR_UPDATE:
				clearCount++;
				// Log.i(TAG, totalCount + "---" + clearCount + "---" +
				// clearMax);
				currentProcess = 100 - (clearCount * clearMax / totalCount);
				if (currentProcess < 0) {
					currentProcess = 0;
				}
				tpb_clear.setProgress(currentProcess);
				long leftSize = totalSize - clearSize;

				if (leftSize < 1000 * 1000 * 1000) {
					tv_size_suffix.setText(getResources().getString(
							R.string.suffix_size_m));
				} else {
					tv_size_suffix.setText(getResources().getString(
							R.string.suffix_size_g));
				}
				tv_size.setText(parseApkSize(leftSize));

				break;
			case CLEAR_UNINSTAll:
				tv_path.setText(getResources().getString(
						R.string.tools_gc_uninstall));
				break;
			case CLEAR_CACHE:
				tv_path.setText(getResources().getString(
						R.string.tools_gc_cache));
				break;
			case CLEAR_OTHER:
				tv_path.setText(getResources().getString(
						R.string.tools_gc_other));
				break;
			case CLEAR_FINISH:
				rl_running.setVisibility(View.GONE);
				rl_result.setVisibility(View.VISIBLE);
				tb_finish.setVisibility(View.VISIBLE);
				tb_finish.requestFocus();
				showClearDetail();
				break;

			}
		};
	};
	private CacheHelper cacheHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_gc);
		rl_result = (RelativeLayout) findViewById(R.id.rl_result);
		rl_running = (RelativeLayout) findViewById(R.id.rl_running);
		tv_state = (TextView) findViewById(R.id.tv_state);
		tb_finish = (TvButton) findViewById(R.id.tb_finish);
		tv_path = (TextView) findViewById(R.id.tv_path);
		tpb_clear = (TvProgressBar) findViewById(R.id.tpb_clear);
		tv_size = (TextView) findViewById(R.id.tv_size);
		tv_size_suffix = (TextView) findViewById(R.id.tv_size_suffix);
		tv_option = (TextView) findViewById(R.id.tv_option);
		tv_size_total = (TextView) findViewById(R.id.tv_size_total);
		tv_size_history = (TextView) findViewById(R.id.tv_size_history);
		tv_option.setText(getResources().getString(R.string.tools_gc_gabage));
		cacheHelper = CacheHelper.create(getApplicationContext());

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
	protected void onStart() {
		init();
		new Thread() {
			public void run() {
				searchFinish = false;
				searchFlag = true;
				startSearch(CommonUtil.getSDPath());
				// // 清除本应用缓存
				searchFile(getCacheDir().getAbsolutePath(), 0);
				searchFinish = true;
			};
		}.start();

		showSearchProcess();
		super.onStart();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			searchFlag = false;
			ToolsGabageClear.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void init() {

		currentProcess = 0;
		searchFlag = true;
		totalSize = 0;
		clearSize = 0;
		otherSize = 0;
		clearCount = 0;
		totalCount = 1;
		uninstallSize = 0;
		cacheSize = 0;
		systemTime = System.currentTimeMillis();
		uninstallPathList = new ArrayList<GabageInfo>();
		cachePathList = new ArrayList<GabageInfo>();
		otherPathList = new ArrayList<GabageInfo>();

		tv_size.setText("0");
		tpb_clear.setProgress(1);

		tb_finish.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				searchFlag = false;
				// 关闭页面
				ToolsGabageClear.this.finish();

			}
		});

	}

	private Boolean startSearch(String dir) {
		Log.i(TAG, "dir=" + dir);
		try {
			// 读取SD卡文件映射
			Properties pro = new Properties();
			InputStream is = getApplicationContext().getAssets().open(
					"filepath_reflect.properties");
			pro.load(is);
			is.close();
			//反射获取已安装应用缓存
			List<String> appList = DataUtil.getAppList(getApplicationContext());
			
			for (String pkg : appList) {
				final String middle=pkg;
				Thread.sleep(SCAN_SLEEP);
				cacheHelper.queryPacakgeSize(pkg, new SyncCallBack() {
					

					@Override
					public void onDataLoaded(long codesize, long dataSize,
							long cacheSize) {
						String filePath="/data/data/"+middle+"/cache/";
						// 缓存
						cacheSize += cacheSize;
						totalSize += cacheSize;
						cachePathList.add(new GabageInfo(filePath, cacheSize));
						try {
							Thread.sleep(SCAN_SLEEP);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Message msg = mHandler.obtainMessage();
						msg.what = SEARCH_UPDATE;
						msg.obj = filePath;
						mHandler.sendMessage(msg);
						
					}
				});
			}
			
			File[] fileList = new File(dir).listFiles();

			for (File file : fileList) {
				// Log.i(TAG, file.getName());
				if (file.isDirectory()) {
					// Log.e(TAG, file.getName()+"");
					if (pro.containsKey(file.getName())
							&& !CommonUtil.isAppInstalled(
									getApplicationContext(),
									(String) pro.get(file.getName()))) {
						// 此处得到路径 于路径集中存在 但于SD卡中未存在，添入残留列表
						searchFileUninstall(file.getAbsolutePath(), 0);
					} else if (isUseless(file)) {// 超出一月未使用文件夹
						searchFileUninstall(file.getAbsolutePath(), 0);
					} else {
						// 此处为不可删除文件及文件夹路径
						searchFile(file.getAbsolutePath(), 0);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private int searchFile(String dir, int acc) {
		// Log.i(TAG, "dir=" + dir);
		if (!searchFlag)
			return acc;// 用以退出线程
		try {
			File[] fileList = new File(dir).listFiles();
			if (fileList == null) {
				// 空文件夹
				otherPathList.add(new GabageInfo(dir, 0));
				return acc;
			}

			for (File file : fileList) {
				if (!searchFlag)
					break;// 用以退出线程
				String filePath = file.getAbsolutePath();
				if (file.isDirectory()) {
					searchFile(filePath, (acc + 1));
					Message msg = mHandler.obtainMessage();
					msg.what = SEARCH_UPDATE;
					msg.obj = file.getAbsolutePath();
					mHandler.sendMessage(msg);
				} else {

					if (file.length() > ToolsBigFileManage.BIG_FILE) {
						continue;
					}

					if (filePath.contains("cache")
							|| filePath.endsWith(".cache")
							|| file.equals(".temp")) {
						// 缓存
						cacheSize += file.length();
						totalSize += file.length();
						cachePathList.add(new GabageInfo(filePath, file
								.length()));
						Thread.sleep(SCAN_SLEEP);
						Message msg = mHandler.obtainMessage();
						msg.what = SEARCH_UPDATE;
						msg.obj = file.getAbsolutePath();
						mHandler.sendMessage(msg);

					} else if (file.length() <= 0) {
						// 无效文件（长时未使用文件：大于一月，长度为0）
						otherPathList.add(new GabageInfo(filePath, file
								.length()));
						Thread.sleep(SCAN_SLEEP);
						Message msg = mHandler.obtainMessage();
						msg.what = SEARCH_UPDATE;
						msg.obj = file.getAbsolutePath();
						mHandler.sendMessage(msg);
					}
					// Log.i(TAG, file.getAbsolutePath() + "--" +
					// file.getName());

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return acc;
	}

	private int searchFileUninstall(String dir, int acc) {
		//Log.i(TAG, "uninstallDir=" + dir);
		if (!searchFlag)
			return acc;// 用以退出线程
		try {
			File[] fileList = new File(dir).listFiles();
			if (fileList == null) {
				// 空文件夹
				uninstallPathList.add(new GabageInfo(dir, 0));
				return acc;
			}
			for (File file : fileList) {
				if (!searchFlag)
					break;// 用以退出线程
				String filePath = file.getAbsolutePath();
				if (file.isDirectory()) {
					searchFileUninstall(filePath, (acc + 1));
				} else if (file.length() < ToolsBigFileManage.BIG_FILE) {// 文件长于此数则不添加
					// 缓存
					uninstallSize += file.length();
					totalSize += file.length();
					uninstallPathList.add(new GabageInfo(filePath, file
							.length()));
					Thread.sleep(SCAN_SLEEP);
					mHandler.sendEmptyMessage(SEARCH_UPDATE);

					// Log.i(TAG, filePath + "--" + file.getName());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return acc;
	}

	private void startClear() {
		
		
		// 最后一步 清理空文件夹？？
		new Thread() {
			public void run() {
				try {
					int sleep = countSleep(uninstallPathList.size());
					int delay = countDelay(uninstallPathList.size());
					if (delay != 0) {
						mHandler.sendEmptyMessage(CLEAR_UNINSTAll);
						Thread.sleep(delay);
					}
					for (GabageInfo gabage : uninstallPathList) {
						 CommonUtil.deleteFileByPath(getApplicationContext(),
						 gabage.getFilePath());
						clearSize += gabage.getFileSize();
						mHandler.sendEmptyMessage(CLEAR_UPDATE);
						Thread.sleep(sleep);
					}
					sleep = countSleep(cachePathList.size());
					delay = countDelay(cachePathList.size());
					if (delay != 0) {
						mHandler.sendEmptyMessage(CLEAR_CACHE);
						Thread.sleep(delay);
					}
					for (GabageInfo gabage : cachePathList) {
						 CommonUtil.deleteFileByPath(getApplicationContext(),
						 gabage.getFilePath());
						clearSize += gabage.getFileSize();
						mHandler.sendEmptyMessage(CLEAR_UPDATE);
						Thread.sleep(sleep);
					}

					sleep = countSleep(otherPathList.size());
					delay = countDelay(otherPathList.size());
					if (delay != 0) {
						mHandler.sendEmptyMessage(CLEAR_OTHER);
						Thread.sleep(delay);
					}

					for (GabageInfo gabage : otherPathList) {
						 CommonUtil.deleteFileByPath(getApplicationContext(),
						 gabage.getFilePath());
						clearSize += gabage.getFileSize();
						mHandler.sendEmptyMessage(CLEAR_UPDATE);
						Thread.sleep(sleep);
					}
					
					//反射清理所有应用缓存
					CacheHelper.create(getApplicationContext()).clearPackageCache();
					clearFlag = false;
					mHandler.sendEmptyMessage(SHOW_CLEAR_FLASH);
				} catch (Exception e) {
					e.printStackTrace();
				}

			};
		}.start();
	}

	/**
	 * 启动动画
	 */
	private void showSearchProcess() {

		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

				if (searchFlag) {
					try {
						if (currentProcess < PROGRESS_MAX) {
							currentProcess++;
						}

						if (searchFinish && currentProcess > PROGRESS_MIN) {
							searchFlag = false;
							mHandler.sendEmptyMessage(SHOW_SEARCH_FLASH);
							cancel();
						} else {
							mHandler.sendEmptyMessage(SHOW_PROGRESS);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					cancel();
				}
			}
		}, 0, PERIOD);

	}

	private void showFlash(final int state) {// 0为快进 1为快减
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {

				if (state == 0) {
					try {
						currentProcess++;
						if (currentProcess > 100) {
							// 动画加速
							Thread.sleep(700);
							mHandler.sendEmptyMessage(SEARCH_FINISH);
							cancel();
						} else {
							mHandler.sendEmptyMessage(SHOW_PROGRESS);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else if (state == 1) {
					try {
						currentProcess--;
						if (currentProcess < 0) {
							// 动画加速
							Thread.sleep(1700);
							mHandler.sendEmptyMessage(CLEAR_FINISH);
							cancel();
						} else {
							mHandler.sendEmptyMessage(SHOW_PROGRESS);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, PERIOS_FLASH);
	}

	private int countSleep(int listSize) {
		if (listSize < 3) {// 小于3个则睡眠700
			return 300;
		} else if (listSize < 10) {
			return 90;
		} else if (listSize < 30) {
			return 30;
		} else {
			return 20;
		}
	}

	private int countDelay(int listSize) {
		// 确保时间大于1秒

		if (listSize < 3) {// 小于3个则睡眠700
			return 0;
		} else if (listSize < 5) {// 小于3个则睡眠700
			return 900;
		} else if (listSize < 13) {
			return 700;
		} else {
			return 300;
		}
	}

	private Boolean isUseless(File file) {

		try {

			long lastModify = file.lastModified();
			int days = (int) ((systemTime - lastModify) / (24 * 60 * 60 * 1000));
			if (days > 31) {
				return true;
			}

			Log.i(TAG, "己有" + days + "天未使用");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private void showClearDetail() {
		// 显示清理细节
		String totalStr = parseApkSizeWithSuffix(totalSize);
		String historyStr = parseApkSizeWithSuffix(CommonUtil.readGagabeSize()
				+ totalSize);
		ForegroundColorSpan fcs = new ForegroundColorSpan(Color.WHITE);

		tv_size_total.setText(String.format(
				getResources().getString(R.string.tools_gc_clear_total),
				totalStr));
		SpannableStringBuilder ssb = new SpannableStringBuilder(tv_size_total
				.getText().toString());
		ssb.setSpan(fcs, 8, 9 + totalStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		tv_size_total.setText(ssb);

		tv_size_history.setText(String.format(
				getResources().getString(R.string.tools_gc_clear_history),
				historyStr));
		ssb = new SpannableStringBuilder(tv_size_history.getText().toString());
		ssb.setSpan(fcs, 8, 9 + historyStr.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		tv_size_history.setText(ssb);
		CommonUtil.saveGabageSize(getApplicationContext(), totalSize);
	}

	private String parseApkSize(long size) {
		DecimalFormat df = new DecimalFormat("#.0");
		String fileSizeString = "";
		if (size < 1024 * 1024) {
			fileSizeString = "0" + df.format((double) size / (1024 * 1024));
		} else if (size < 1024 * 1024 * 1024) {
			fileSizeString = df.format((double) size / (1024 * 1024));
		} else {
			fileSizeString = df.format((double) size / (1000 * 1000 * 1000));
		}
		return fileSizeString;
	}

	private String parseApkSizeWithSuffix(long size) {
		DecimalFormat df = new DecimalFormat("#.0");
		String fileSizeString = "";
		if (size < 1024 * 1024) {
			fileSizeString = "0" + df.format((double) size / (1024 * 1024))
					+ "MB";
		} else if (size < 1024 * 1024 * 1024) {
			fileSizeString = df.format((double) size / (1024 * 1024)) + "MB";
		} else {
			fileSizeString = df.format((double) size / (1000 * 1000 * 1000))
					+ "GB";
		}
		return fileSizeString;
	}

	class GabageInfo {
		private long fileSize;
		private String filePath;

		public GabageInfo(String path, long size) {
			this.filePath = path;
			this.fileSize = size;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		public String getFilePath() {
			return filePath;
		}

		public void setFilePath(String filePath) {
			this.filePath = filePath;
		}

	}
}
