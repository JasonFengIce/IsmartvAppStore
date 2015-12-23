package com.boxmate.tv.entity;

public class ProcessInfo {
	private int id;
	private String processName;
	private String appName;
	private int pid;
	private String packageName;
	private String name;
	private int memory;
	private boolean SystemProcess;
	private boolean isFront;
	public boolean isFront() {
		return isFront;
	}

	public void setFront(boolean isFront) {
		this.isFront = isFront;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}


	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public boolean isSystemProcess() {
		return SystemProcess;
	}

	public void setSystemProcess(boolean systemProcess) {
		SystemProcess = systemProcess;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
