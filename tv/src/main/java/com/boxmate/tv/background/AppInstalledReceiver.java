package com.boxmate.tv.background;

import com.boxmate.tv.ui.manage.UninstallActivity;
import com.boxmate.tv.util.CommonUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class AppInstalledReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Log.i("noti",arg1.getAction().toString());
		String pn = arg1.getDataString().split(":")[1];
		if(arg1.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			
			SecurityService.packageInstalled(pn);
		} else if(arg1.getAction().equals("android.intent.action.PACKAGE_REMOVED")){
			Log.i("app removed",pn);
			//UninstallActivity.appRemoved(pn);
		}
		CommonUtil.loadInstalledApp();
	}
}
