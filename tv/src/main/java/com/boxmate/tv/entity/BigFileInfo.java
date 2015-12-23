package com.boxmate.tv.entity;

public class BigFileInfo {
	
	
	private int id;
	private String name;
	private String path;
	private int type;//0为影音 1为音乐  3为图片  4为安装包 5为其他
	private String sizeString;
	@Override
	public String toString() {
		return "BigFileInfo [id=" + id + ", name=" + name + ", path=" + path
				+ ", type=" + type + ", sizeString=" + sizeString + "]";
	}
	public String getSizeString() {
		return sizeString;
	}
	public void setSizeString(String sizeString) {
		this.sizeString = sizeString;
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	
	

}
