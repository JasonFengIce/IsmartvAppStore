package com.boxmate.tv.background;


import com.boxmate.tv.entity.Config;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.SystemUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiReceiver extends BroadcastReceiver{
	private final static String TAG="WifiReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
//        if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
//         } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//             //Log.e(TAG, "网络状态改变");
//             NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//             if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {// 如果断开连接
//                 Log.e(TAG, "wifi网络连接断开");
//             }
//             
//            if(info.getState().equals(NetworkInfo.State.CONNECTING)){
//                 Log.e(TAG, "连接到wifi网络");
//             }
//         } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
//             // WIFI开关
//             int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_DISABLED);
//             if (wifistate == WifiManager.WIFI_STATE_DISABLED) {// 如果关闭
// 
//                 System.out.println("系统关闭wifi");
//                 //Log.e(TAG, "系统关闭wifi");
//             }
//             
//             if(wifistate==WifiManager.WIFI_STATE_ENABLED){
//                System.out.println("系统开启wifi");
//                //Log.e(TAG, "系统开启wifi");
//             }
//         }
		
		try {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				// //监听连接状态的变化莫测
				NetworkInfo networkInfo = intent
						.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
				if (networkInfo.isConnected()) {
					Log.e(TAG, "网络连接");
					//已停止下载 市场处于前台 且网络名未改变
					if (!SecurityService.isDownloading&&
							SystemUtil.getNetName(context).equals(CommonUtil.readWifiName())) {
						SecurityService.isDownloading=true;
						SecurityService.instance.continueDownload();
					}
				} else {
					Log.e(TAG, "网络断开");
					SecurityService.isDownloading=false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
