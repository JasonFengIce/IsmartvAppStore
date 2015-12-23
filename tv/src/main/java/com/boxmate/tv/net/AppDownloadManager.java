package com.boxmate.tv.net;

import java.util.ArrayList;

import com.boxmate.tv.LauncherActivity;
import com.boxmate.tv.background.SecurityService;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.util.CommonUtil;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AppDownloadManager {
	private final static String TAG = "AppDownloadManager";
	private static AppDownloadManager manager;
	private static SQLiteDatabase db;

	public static AppDownloadManager getInstance() {
		if (manager == null) {
			synchronized (AppDownloadManager.class) {
				if (manager == null) {
					manager = new AppDownloadManager();
				}
			}
		}
		return manager;
	}

	private ArrayList<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
	private static String TABLE_NAME = "taskInfoList";
	private static String createTable = "create table IF NOT EXISTS "
			+ TABLE_NAME + " (id integer primary key autoincrement, "
			+ " app_id integer, " + "title varchar not null,"
			+ "icon_url varchar not null ," + "package_name varchar not null,"
			+ "download_url varchar not null," + "status integer)";

	private AppDownloadManager() {
		Log.i(TAG, "db初始化");
		if (db == null) {
			db = getDb();
			if (db != null) {
				db.execSQL(createTable);
			}
		}

		try {
			loadList();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadList() {
		String sqlString = "select * from " + TABLE_NAME;
		Cursor cursor = db.rawQuery(sqlString, new String[] {});

		beginTranscation();
		while (cursor.moveToNext()) {
			TaskInfo taskInfo = new TaskInfo();
			taskInfo.setTaskId(cursor.getInt(cursor.getColumnIndex("app_id")));
			taskInfo.setTaskName(cursor.getString(cursor
					.getColumnIndex("title")));

			taskInfo.status = cursor.getInt(cursor.getColumnIndex("status"));
			if (taskInfo.status == TaskInfo.SUCCESS) {
				taskInfo.setProgress(100);
			} else {
				taskInfo.setProgress(0);
			}

			taskInfo.setDownloadUrl(cursor.getString(cursor
					.getColumnIndex("download_url")));
			taskInfo.setPackageName(cursor.getString(cursor
					.getColumnIndex("package_name")));
			taskInfo.iconUrl = cursor.getString(cursor
					.getColumnIndex("icon_url"));

			if (taskInfo.status == 1 || taskInfo.status == 0) {
				// 判断
				if (SecurityService.instance == null) {
					taskInfo.status = 4;
					setStatusByTaskIdCustom(4, taskInfo.getTaskId());
				} else {
					if (!SecurityService.instance
							.checkPackageExistInQueue(taskInfo.getPackageName())) {
						taskInfo.status = 4;
						setStatusByTaskIdCustom(4, taskInfo.getTaskId());
					}
				}
			}

			taskInfoList.add(taskInfo);
		}
		endTranscation();
		Log.i(TAG, "成功自数据库中读取下载队列");

	}

	public void addTaskInfo(TaskInfo taskInfo) {
		

		// 判断是否已经存在了。。。。
		if (replaceTaskInfoByTaskInfo(taskInfo)) {
			return;
		}
		try {
			//Log.i("taskInfo", taskInfo.toString());

			String sqlString = "insert into " + TABLE_NAME + "(app_id," + "title,"
					+ "icon_url," + "download_url," + "package_name,"
					+ "status) values(?,?,?,?,?,?)";
			db.execSQL(
					sqlString,
					new String[] { taskInfo.getTaskId() + "",
							taskInfo.getTaskName(), taskInfo.iconUrl,
							taskInfo.getDownloadUrl(), taskInfo.getPackageName(),
							taskInfo.getStatus() + "" });

			taskInfoList.add(taskInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

	public void setStatusByTaskId(int status, int id) {
		ContentValues values = new ContentValues();
		values.put("status", status);
		db.update(TABLE_NAME, values, "app_id=?", new String[] { id + "" });
		Log.i(TAG, "成功修改数据库中状态");
	}

	public void setStatusByTaskIdCustom(int status, int id) {
		String sqlString = "update " + TABLE_NAME
				+ " set status=? where app_id=?";
		db.execSQL(sqlString, new String[] { status + "", id + "" });
	}

	public void beginTranscation() {
		if (db == null) {
			db = getDb();
		}
		db.beginTransaction();
	}

	public void endTranscation() {
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void delTaskById(int id) {
		try {
			for (int i = 0; i < taskInfoList.size(); i++) {
				TaskInfo taskInfo = taskInfoList.get(i);
				if (taskInfo.getTaskId() == id) {
					taskInfoList.remove(taskInfo);
					break;
				}
			}
			// 数据库移除
			db.delete(TABLE_NAME, "app_id=?", new String[] { id + "" });
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void delTaskById(Context context, TaskInfo deleteTask) {
		try {
			for (int i = 0; i < taskInfoList.size(); i++) {
				TaskInfo taskInfo = taskInfoList.get(i);
				if (taskInfo.getTaskId() == deleteTask.getTaskId()) {
					taskInfoList.remove(taskInfo);
					break;
				}
			}
			// 数据库移除
						db.delete(TABLE_NAME, "app_id=?",
								new String[] { deleteTask.getTaskId() + "" });
			// 退出任务
			if (SecurityService.instance != null) {
				SecurityService.instance.cancelTaskById(deleteTask.getTaskId());
			}
			// 删除临时文件
			CommonUtil.deleteFile(context, deleteTask.getDownloadUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public SQLiteDatabase getDb() {

		try {
			db = SQLiteDatabase.openOrCreateDatabase(
					LauncherActivity.dbFilePath + "/" + "xxxxx.db", null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return db;
	}

	public Boolean replaceTaskInfoByTaskInfo(TaskInfo taskInfo) {
		Boolean replaced = false;
		for (int i = 0; i < taskInfoList.size(); i++) {
			if (taskInfoList.get(i).getTaskId() == taskInfo.getTaskId()) {
				taskInfoList.set(i, taskInfo);
				replaced = true;
				break;
			}
		}
		return replaced;
	}

	public TaskInfo getTaskInfoByPackageName(String packageName) {
		TaskInfo taskInfo = null;
		for (int i = 0; i < taskInfoList.size(); i++) {
			if (taskInfoList.get(i).getPackageName().equals(packageName)) {
				taskInfo = taskInfoList.get(i);
				break;
			}
		}
		return taskInfo;
	}
	
	public Boolean checkTaskInfoByPackageName(String packageName) {
		for (int i = 0; i < taskInfoList.size(); i++) {
			if (taskInfoList.get(i).getPackageName().equals(packageName)) {
				return true;
			}
		}
		return false;
	}

	public void delTaskByPackageName(String packageName) {
		TaskInfo taskInfo = getTaskInfoByPackageName(packageName);
		if (taskInfo != null) {
			Log.i("删除", taskInfo.toString());
			delTaskById(taskInfo.getTaskId());
		}
	}

	public int getTaskInfoCount() {
		return taskInfoList.size();
	}

	public TaskInfo getTaskInfoByAppId(int id) {
		for (int i = 0; i < taskInfoList.size(); i++) {
			if (taskInfoList.get(i).getTaskId() == id) {
				return taskInfoList.get(i);
			}
		}
		TaskInfo taskInfo = new TaskInfo();
		taskInfo.status = TaskInfo.INIT;
		return taskInfo;
	}

	public ArrayList<TaskInfo> getTaskList() {
		return taskInfoList;
	}
}