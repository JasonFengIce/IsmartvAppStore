package com.boxmate.tv.entity;

public class VersionInfo {

	private int versionCode;
	private String desc;
	private boolean newVersion;
	public boolean isNewVersion() {
		return newVersion;
	}

	public void setNewVersion(boolean newVersion) {
		this.newVersion = newVersion;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}



	private String versionName;
	private String apkUrl;
	private String iconUrl;
	private String packageName;
	private String welcomeUrl;

	public String getWelcomeUrl() {
		return welcomeUrl;
	}

	public void setWelcomeUrl(String welcomeUrl) {
		this.welcomeUrl = welcomeUrl;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	@Override
	public String toString() {
		return "VersionInfo [versionCode=" + versionCode + ", versionName="
				+ versionName + ", apkUrl=" + apkUrl + ", iconUrl=" + iconUrl
				+ ", packageName=" + packageName + ", welcomeUrl=" + welcomeUrl
				+ "]";
	}

	public VersionInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getApkUrl() {
		return apkUrl;
	}

	public void setApkUrl(String apkUrl) {
		this.apkUrl = apkUrl;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
