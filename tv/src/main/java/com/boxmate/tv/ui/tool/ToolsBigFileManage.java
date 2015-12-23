package com.boxmate.tv.ui.tool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import reco.frame.tv.view.TvListView;

import com.boxmate.tv.R;
import com.boxmate.tv.adapter.BigFileListAdapter;
import com.boxmate.tv.entity.BigFileInfo;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ToolsBigFileManage extends Activity {

	private final String TAG = "ToolsBigFileManage";
	private final static int SEARCH_FINISH = 0;
	private final static int DELETE_SUCCESS = 1;
	private final static int SHOW_PATH = 2;
	private int DELAY = 1300, DELETE_DELAY = 700;
	public final static long BIG_FILE = 10 * 1024 * 1024;
	private List<BigFileInfo> bigfileList;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case SEARCH_FINISH:
				tv_path.setVisibility(View.GONE);
				tv_searching.setVisibility(View.GONE);
				iv_file.setVisibility(View.GONE);
				if (bigfileList != null && bigfileList.size() > 0) {
					BigFileListAdapter mAdapter = new BigFileListAdapter(
							getApplicationContext(), bigfileList);
					tlv_list.setAdapter(mAdapter);
				} else {
					tv_result.setText(getResources().getString(
							R.string.tools_bigfile_nofile));
				}
				break;
			case SHOW_PATH:
				String path = (String) msg.obj + "";
				if (path.length() > 31) {
					path = path.substring(0, 13) + "..."
							+ path.substring(path.length() - 17);
				}
				tv_path.setText(path);
				break;
			}
		};
	};

	private ImageView iv_file;
	private TvListView tlv_list;
	private TextView tv_result;
	private TextView tv_searching;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_bigfile);
		init();
		new Thread() {

			public void run() {
				searchBigFile(CommonUtil.getSDPath(), 0);
				//searchBigFile(getFilesDir().getPath(),0);
				Message msg = mHandler.obtainMessage();
				msg.what = SEARCH_FINISH;
				mHandler.sendMessageDelayed(msg, DELAY);
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

		return super.onKeyDown(keyCode, event);
	}

	private void init() {
		DELAY = 1700;
		tlv_list = (TvListView) findViewById(R.id.tlv_list);
		iv_file = (ImageView) findViewById(R.id.iv_file);
		tv_result = (TextView) findViewById(R.id.tv_result);
		tv_searching = (TextView) findViewById(R.id.tv_searching);
		tv_path = (TextView) findViewById(R.id.tv_path);
		bigfileList = new ArrayList<BigFileInfo>();

	}

	private int searchBigFile(String dir, int acc) {

		Log.i(TAG, "dir=" + dir);

		try {
			File[] fileList = new File(dir).listFiles();
			if (fileList == null) {
				return acc;
			}

			if (fileList.length > 50) {
				DELAY -= 300;
			}
			for (File file : fileList) {

				//Log.i(TAG, file.getAbsolutePath());
				if (file.isDirectory()) {
					searchBigFile(file.getAbsolutePath(), (acc + 1));
				} else if (file.length() > BIG_FILE) {
					BigFileInfo bigFile = checkType(file);
					if (bigFile != null) {
						bigfileList.add(bigFile);
					}
				}
				Message msg = mHandler.obtainMessage();
				msg.what = SHOW_PATH;
				msg.obj = file.getAbsolutePath();
				mHandler.sendMessage(msg);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return acc;
	}

	private String videoTypeSuffixs = "avi.dat.flv.mkv.rm.rmvb.mov.mp4";
	private String soundTypeSuffixs = "mp3.wav";
	private String pictureTypeSuffixs = "gif.jpg.jpeg.png";
	private TextView tv_path;

	private BigFileInfo checkType(File file) {
		BigFileInfo big = new BigFileInfo();
		try {
			big.setName(file.getName());
			big.setPath(file.getAbsolutePath());
			big.setSizeString(DataUtil.parseApkSize(file.length()));
			String fileName = file.getName();
			// 判断类型
			String suffix = fileName.substring(fileName.lastIndexOf("."));
			int type = 4;
			if (videoTypeSuffixs.contains(suffix)) {
				type = 0;
			} else if (soundTypeSuffixs.contains(suffix)) {
				type = 1;
			} else if (pictureTypeSuffixs.contains(suffix)) {
				type = 2;
			} else if (suffix.equals(".apk")) {
				type = 3;
			} else {
				type = 4;
			}
			big.setType(type);

			return big;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
