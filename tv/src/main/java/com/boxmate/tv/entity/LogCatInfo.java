package com.boxmate.tv.entity;

public class LogCatInfo {

	private String pkgName;
	private String processName;
	private Boolean isRunning;
	public String getPkgName() {
		return pkgName;
	}
	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	public Boolean getIsRunning() {
		return isRunning;
	}
	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}
}
