package com.boxmate.tv.entity;

import android.graphics.drawable.Drawable;
import android.widget.TextView;

public class AppInfo {
	private int id;
	public int versionCode = 0;
	// 名称
	public String appname = "";
	// 包
	public String packagename = "";
	public String versionName = "";
	// 图标
	public Drawable appicon = null;

	public TextView title;
	private String iconUrl;
	private String apkUrl;
	private String catTitle;
	private int control;
	private long codeSize;
	private long cacheSize;
	private long dataSize;
	private long totalSize;
	public long getDataSize() {
		return dataSize;
	}

	public void setDataSize(long dataSize) {
		this.dataSize = dataSize;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public long getCodeSize() {
		return codeSize;
	}

	public void setCodeSize(long codeSize) {
		this.codeSize = codeSize;
	}

	public long getCacheSize() {
		return cacheSize;
	}

	public void setCacheSize(long cacheSize) {
		this.cacheSize = cacheSize;
	}

	public int getControl() {
		return control;
	}

	public void setControl(int control) {
		this.control = control;
	}

	private int csId;
	private String cname;
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "AppInfo [id=" + id + ", versionCode=" + versionCode
				+ ", appname=" + appname + ", packagename=" + packagename
				+ ", versionName=" + versionName + ", appicon=" + appicon
				+ ", title=" + title + ", iconUrl=" + iconUrl + ", apkUrl="
				+ apkUrl + ", catTitle=" + catTitle + ", control=" + control
				+ ", csId=" + csId + ", cname=" + cname + ", size=" + size
				+ ", star=" + star + "]";
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public String getCatTitle() {
		return catTitle;
	}

	public void setCatTitle(String catTitle) {
		this.catTitle = catTitle;
	}


	public int getCsId() {
		return csId;
	}

	public void setCsId(int csId) {
		this.csId = csId;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	private int size;
	private int star;

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Drawable getAppicon() {
		return appicon;
	}

	public void setAppicon(Drawable appicon) {
		this.appicon = appicon;
	}

	public TextView getTitle() {
		return title;
	}

	public void setTitle(TextView title) {
		this.title = title;
	}
}
