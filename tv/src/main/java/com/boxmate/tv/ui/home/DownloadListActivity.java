package com.boxmate.tv.ui.home;

import java.util.ArrayList;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.adapter.DownloadingAppListAdapter;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.background.SecurityService.OnDownloadStatusChanaged;
import com.boxmate.tv.background.SecurityService.ServiceBinder;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.util.CommonUtil;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DownloadListActivity extends Activity implements
		SecurityService.OnDownloadStatusChanaged,
		DownloadingAppListAdapter.OnLineButtonClickListener {

	private ArrayList<TaskInfo> taskInfoList;
	private ListView lv_download;
	private DownloadingAppListAdapter adapter;
	private SecurityService mService;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_list);
		AppDownloadManager manager = AppDownloadManager.getInstance();
		taskInfoList = manager.getTaskList();
		lv_download = (ListView) findViewById(R.id.lv_download);
		lv_download.setItemsCanFocus(true);
		adapter = new DownloadingAppListAdapter(this);
		adapter.setList(taskInfoList);
		lv_download.setAdapter(adapter);

		SecurityService.delegate = this;
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConn);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.download_list, menu);
		return false;
	}
	
	public Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
			String packageName = (String) msg.obj;
			int action = msg.what;
			
			if(action==SecurityService.DOWNLOAD_STATUS_UPDATE) {
				AppDownloadManager manager = AppDownloadManager.getInstance();
				TaskInfo taskInfo = manager.getTaskInfoByPackageName(packageName);
				if(taskInfo!=null) {
					adapter.setProgressByTaskInfo(taskInfo);
				}
			} else {
				adapter.notifyDataSetChanged();
			}
		}
	};
	
	@Override
	public void onTaskUpdate(String packageName, int action) {
		// TODO Auto-generated method stub
		Message msgMessage = updateHandler.obtainMessage();
		msgMessage.obj = packageName;
		msgMessage.what = action;
		msgMessage.sendToTarget();
	}

	@Override
	public void onCancelClick(int position) {
		// TODO Auto-generated method stub
		onDeleteClick(position);
	}

	@Override
	public void onDeleteClick(int position) {
			if (taskInfoList.size()<1) {
				return;
			}
			TaskInfo taskInfo = taskInfoList.get(position);
			taskInfo.status = TaskInfo.CANCELED;
			taskInfoList.remove(taskInfo);
			AppDownloadManager manager = AppDownloadManager.getInstance();
			manager.delTaskById(getApplicationContext(),taskInfo);
			adapter.notifyDataSetChanged();
	
	}

	@Override
	public void onInstallClick(int position) {

		Toast toast = Toast.makeText(getApplicationContext(), "heheheh ", Toast.LENGTH_SHORT);
		toast.show();

		TaskInfo taskInfo = taskInfoList.get(position);

		Log.d("install",taskInfo.toString());
		CommonUtil.installApk(taskInfo.getDownloadUrl(), this);
	}
	
	
	public ServiceConnection mServiceConn = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder service) {
			// TODO Auto-generated method stub
			mService = ((SecurityService.ServiceBinder)service).getService();  
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			// TODO Auto-generated method stub
			mService = null;
		}  
	};
	
	protected void onResume() {
		super.onResume();

		Intent it = new Intent(this, SecurityService.class);
		bindService(it, mServiceConn, BIND_AUTO_CREATE);
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onRestartClick(int position) {
		// TODO Auto-generated method stub
		TaskInfo taskInfo = taskInfoList.get(position);
		onDeleteClick(position);
		
		taskInfo.status = TaskInfo.WAITING;
		
		
		if(SecurityService.instance!=null) {
			SecurityService.instance.addTaskInQueue(taskInfo);
		}
		adapter.notifyDataSetChanged();
		Log.i("list",taskInfoList.toString());
	}
}
