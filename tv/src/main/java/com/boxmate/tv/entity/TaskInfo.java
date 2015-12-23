package com.boxmate.tv.entity;

import java.io.File;

import reco.frame.tv.http.HttpHandler;

import android.R.integer;
import android.R.string;
import android.content.Context;

public class TaskInfo {
	
	//任务状态
    public static final int WAITING = 0;    //等待下载
    public static final int RUNNING = 1;    //正在下载
    public static final int CANCELED = 2;   //取消
    public static final int SUCCESS = 3;    //下载成功
    public static final int FAILED = 4;     
    public static final int INSTALLED = 5;
    public static final int INIT=6;         //初始状态
    
    
    public HttpHandler<java.io.File> downloadHandler;
    private int taskId;  
    private String taskName;  
    private int progress;  
    public int status;
	private String url;  
	private String packageName;	
    public int versionCode;
	public int fileSize;
	public String iconUrl="";
	private int downloadSize;
	
	
	public void setFileSize(long size) {
		fileSize = (int)size;
	}
	public TaskInfo() {
	}
	public void setPackageName(String name) {
		packageName = name;
	}
	
	
	public String getPackageName() {
		return packageName;
	}
	public void setDownloadSize(long size) {
		downloadSize = (int)size;
		//progress = (int) ((float)downloadSize/(float)fileSize*100);
	}
	public int getDownloadSize() {
		return downloadSize;
	}
    public int getTaskId() {  
        return taskId;  
    }  
    public void setTaskId(int taskId) {  
        this.taskId = taskId;  
    }  
    public void setDownloadUrl(String url) {
    	this.url = url;
    }
    public String getDownloadUrl(){
    	return this.url;
    }
    
    public String getTaskName() {  
        return taskName;  
    }  
    public void setTaskName(String taskName) {  
        this.taskName = taskName;  
    }  
    public int getProgress() {  
        return progress;  
    }  
    public void setProgress(int progress) {  
        this.progress = progress;  
    }  
    public int getStatus() {  
        return status;  
    } 
    
    public String toString(){
    	String resultString = "url:"+getDownloadUrl()+"\npackage:"+getPackageName()
    			+"\nid:"+getTaskId()+"\nstatus:"+status;
		return resultString;
    }
    
    public TaskInfo clone(){
    	TaskInfo taskInfo = new TaskInfo();
    	taskInfo.setTaskId(taskId);
        taskInfo.setTaskName(taskName);
        taskInfo.setProgress(progress);
        taskInfo.status = status;
        taskInfo.url = url;
        taskInfo.setPackageName(packageName);
        taskInfo.versionCode = versionCode;
        taskInfo.fileSize = fileSize;
        taskInfo.iconUrl = iconUrl;
        taskInfo.downloadSize = downloadSize;
    	return taskInfo;
    }
}
