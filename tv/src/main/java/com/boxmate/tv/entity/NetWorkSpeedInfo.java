package com.boxmate.tv.entity;

public class NetWorkSpeedInfo {

	private float speed = 0;
	public long hadFinishBytes = 0;
	private long totalBytes = 1024;
	private int netWorkType = 0;
	private int downloadPercent = 0;

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public long getHadFinishBytes() {
		return hadFinishBytes;
	}

	public void setHadFinishBytes(long hadFinishBytes) {
		this.hadFinishBytes = hadFinishBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public int getNetWorkType() {
		return netWorkType;
	}

	public void setNetWorkType(int netWorkType) {
		this.netWorkType = netWorkType;
	}

	public int getDownloadPercent() {
		return downloadPercent;
	}

	public void setDownloadPercent(int downloadPercent) {
		this.downloadPercent = downloadPercent;
	}

}
