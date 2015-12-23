package com.boxmate.tv.net;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.methods.HttpGet;

import android.R.integer;
import android.content.Context;
import android.util.Log;


public class AppListDownloader {
	
	public interface AppListDownloaderListener{
		public void newAppListLoaded(int page,String result);
	}
	
	private String url;
	private int wantPage  = -1;
	private Boolean downloading = false;
	private int loadPage = -1;
	private HttpGet request;
	public AppListDownloaderListener delegate;
	
	public void setWantPage(int page) {
		wantPage = page;
		startDownload();
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public AppListDownloader() {
		// TODO Auto-generated constructor stub
	}
	
	public void reset(){
		if(request!=null) {
			request.abort();
		}
		wantPage = -1;
		loadPage = -1;
		downloading = false;
		
	}
	
	public void startDownload() {
		if(!downloading && loadPage<wantPage) {
			downloading = true;
			loadPage +=1;
			
			Log.i("加载分页","加载第"+loadPage+"页");
			
			
			String loadUrl = url+"&page="+loadPage;
			
			
			
			request = HttpCommon.getApi(loadUrl, new HttpSuccessInterface() {
				@Override
				public void run(String result) {
					// TODO Auto-generated method stub
					delegate.newAppListLoaded(loadPage,result);
					downloading = false;
					startDownload();
				}
			});
		}
	}
	
}
