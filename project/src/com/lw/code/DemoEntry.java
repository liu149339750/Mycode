package com.lw.code;

import java.io.Serializable;

public class DemoEntry implements Serializable{

	public String title;
	public String apk;
	public String icon;
	public String detail;
	public String minversion;
	public String openlink;
	public long size;
	
	private int id;

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getApk() {
		return apk;
	}

	public void setApk(String apk) {
		this.apk = apk;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getMinversion() {
		return minversion;
	}

	public void setMinversion(String minversion) {
		this.minversion = minversion;
	}

	public String getOpenlink() {
		return openlink;
	}

	public void setOpenlink(String openlink) {
		this.openlink = openlink;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "ttitle:"+title + ",icon:"+icon;
	}
}
