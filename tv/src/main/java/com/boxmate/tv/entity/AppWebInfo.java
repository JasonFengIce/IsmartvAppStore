package com.boxmate.tv.entity;

import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;

public class AppWebInfo {
	public String title;
	public String catTitle;
	public int versionCode;
	public String iconUrl;
	public String packageName;
	public String downloadUrl;
	public int appId;
	public ArrayList<String> imageList = new ArrayList<String>();
	public  ArrayList<HashMap<String, Object>> relative;
	public String author;
	public String versionName;
	public String lastUpdate;
	public int size;
	public int level;
	public int star;
	public int language;
	public int controller;
	public int cid;
	public String cname;
	public String desc;
	public int sourceQuality;
	@Override
	public String toString() {
		return "AppWebInfo [title=" + title + ", versionCode=" + versionCode
				+ ", iconUrl=" + iconUrl + ", packageName=" + packageName
				+ ", downloadUrl=" + downloadUrl + ", appId=" + appId
				+ ", imageList=" + imageList + ", relative=" + relative
				+ ", author=" + author + ", versionName=" + versionName
				+ ", lastUpdate=" + lastUpdate + ", size=" + size + ", level="
				+ level + ", language=" + language + ", controller="
				+ controller + ", desc=" + desc + "]";
	}
	
	
}
