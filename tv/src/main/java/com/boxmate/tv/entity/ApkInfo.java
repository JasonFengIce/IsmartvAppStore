package com.boxmate.tv.entity;

import android.graphics.drawable.Drawable;

public class ApkInfo {
	
	
	private int id;
	private String name;
	private String path;
	private Drawable icon;
	private String version;
	private String packageName;
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "ApkInfo [id=" + id + ", name=" + name + ", path=" + path
				+ ", version=" + version + ", sizeString=" + sizeString + "]";
	}
	private String sizeString;
	public String getSizeString() {
		return sizeString;
	}
	public void setSizeString(String sizeString) {
		this.sizeString = sizeString;
	}

}
