package com.boxmate.tv.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.entity.Config;

import reco.frame.tv.TvHttp;
import reco.frame.tv.http.AjaxCallBack;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.View;

public class NetUtil {

	private final static String TAG = "NetUtil";



	public static String buildCommonCookie(Context context) {

		// version
		String cookie = "";
		try {
			cookie = String.format(
					"version=%d; channel=%s; guid=%s; device=%s",
					CommonUtil.getVersion(),
					CommonUtil.getChannel(),
					CommonUtil.getGuid(),
					URLEncoder.encode(CommonUtil.getDeviceName(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.i("cookie", cookie);

		return cookie;

	}
	
	public static String buildCommonCookie(Context context,String appId) {

		// version
		String cookie = "";
		try {
			cookie = String.format(
					"version=%d; channel=%s; guid=%s; device=%s",
					CommonUtil.getVersion(),
					CommonUtil.getChannel(),
					CommonUtil.getGuid(),
					URLEncoder.encode(CommonUtil.getDeviceName(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Log.i("cookie", cookie);

		return cookie;

	}

	
	public static String buildApiUrl(HashMap<String, String> params) {
		String urlString = Config.appBase;
		for(HashMap.Entry<String, String> entry:params.entrySet()){  
			
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			try {
				urlString = urlString+key+"="+URLEncoder.encode(value,"utf-8")+"&";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return urlString;
	}
	
//	/**
//	 * 如果图片存在则直接加载 不存在则下载 并加载上一次设置的启动图
//	 * 
//	 * @param context
//	 * @param showTip
//	 */
//	public static void loadLauncher(final Context context, final View view) {
//		Log.i(TAG, "正在获取启动图");
//		final String launcherFileName = DataUtil.readLauncherPath(context)+"";
//
//		final String launcherPath = context.getFilesDir() + "/"
//				+ launcherFileName;
//		// 如果没有网络
//		if (new File(launcherPath).exists()) {
//			Bitmap bmp = BitmapFactory.decodeFile(launcherPath);
//			if (bmp==null) {
//				view.setBackgroundResource(R.drawable.launcher_bg);
//			}else{
//				view.setBackgroundDrawable(new BitmapDrawable(bmp));
//			}
//		} else {
//			view.setBackgroundResource(R.drawable.launcher_bg);
//		}
//
//	}
//	
//	
//	public static void checkUpdate(final Context context, final Boolean showTip) {
//
//		String pkName = context.getPackageName();
//		try {
//			
//			
//			final String versionName = context.getPackageManager()
//					.getPackageInfo(pkName, 0).versionName;
//			final int versionCode = context.getPackageManager().getPackageInfo(
//					pkName, 0).versionCode;
//
//			final AsyncImageLoader loader=new AsyncImageLoader(context);
//			TvHttp http=new TvHttp(context);
//			http.addHeader(Config.Cookie, NetUtil.buildCommonCookie(context));
//			http.get(Config.UPDATE_URL+context.getPackageName(), new AjaxCallBack<Object>() {
//				@Override
//				public void onSuccess(Object t) {
//					JSONObject infoJsonObject;
//					try {
//						//Log.e(TAG, t.toString());
//						infoJsonObject = new JSONObject(t.toString());
//						int onlineVersionCode = infoJsonObject
//								.getInt("version_code");
//						final String launcherUrl = infoJsonObject
//								.getString("launcher_url") + "";
//						//更新启动图
//						
//						final String launcherFileName = DataUtil.readLauncherPath(context)+"";
//						final String launcherPath = context.getFilesDir() + "/"
//								+ launcherFileName;
//						if (DataUtil.md5(launcherUrl).equals(launcherFileName)) {
//							if (!new File(launcherPath).exists()) {
//								loader.loadImageByUrl(launcherUrl, false, new ImageCallback() {
//									@Override
//									public void onImageLoaded(Drawable bitmap, String uri, int type) {
//										
//										if (bitmap!=null) {
//											Log.i(TAG, "老图加载成功");
//											DataUtil.saveLauncherPath(
//													context, DataUtil.md5(launcherUrl));
//										}
//										
//									}
//								});
//							}
//						} else {
//							Log.i(TAG, "有新图=");
//							loader.loadImageByUrl(launcherUrl, true, new ImageCallback() {
//								
//								@Override
//								public void onImageLoaded(Drawable bitmap, String uri, int type) {
//									
//									if (bitmap!=null) {
//										Log.e(TAG, "新图加载成功");
//										DataUtil.saveLauncherPath(
//												context, DataUtil.md5(launcherUrl));
//									}
//									
//								}
//							});
//						}
//						
//						if (onlineVersionCode > versionCode) {
//							Intent intent = new Intent(context,
//									VersionUpdateActivity.class);
//							intent.putExtra("version_name", versionName);
//							intent.putExtra("online_version",
//									infoJsonObject.getString("version_name"));
//							intent.putExtra("icon_url",
//									infoJsonObject.getString("icon_url"));
//							intent.putExtra("apk_url",
//									infoJsonObject.getString("apk_url"));
//							intent.putExtra("update_info",
//									infoJsonObject.getString("desc"));
//							context.startActivity(intent);
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//					super.onSuccess(t);
//				}
//			});
//
//			
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//	}
}
