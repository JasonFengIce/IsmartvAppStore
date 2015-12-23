package com.boxmate.tv.background;

import java.util.List;
import android.content.Context;
import android.util.Log;


public class LogcatObsever {

	private final static String TAG = "LogcatObsever";
	private List<String> pkgList;
	private Boolean isObserver;
	private Context context;

	public LogcatObsever(Context context) {
		this.context = context;
	}

	public void stopObsever() {
		isObserver = false;
	}



}
