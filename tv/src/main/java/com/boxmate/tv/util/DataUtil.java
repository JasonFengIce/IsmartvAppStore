package com.boxmate.tv.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.entity.Config;

public class DataUtil {

	private final static String TAG = "DataUtil";

	public static String md5(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
			// System.out.println("result: " + buf.toString());// 32位的加密
			// System.out.println("result: " + buf.toString().substring(8,
			// 24));// 16位的加密
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	public static HashMap<String, Object> buildDataForShow(JSONObject app) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			data.put("title", app.getString("title"));
			data.put("icon_url", app.getString("icon_url"));
			data.put("download_url", app.getString("download_url"));
			data.put("cat_title", app.getString("cat_title"));
			data.put("app_id", app.getInt("id"));
			data.put("star", app.getInt("star"));
			data.put("package", app.getString("package"));
			data.put("controller", app.getInt("controller"));
			data.put("csid", getControllerRsid(app.getInt("controller")));
			data.put("cname", getControllerName(app.getInt("controller")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static String getControllerName(int c) {

		if (c == Config.CONTROL_REMOTE) {
			return "遥控";
		} else if (c == Config.CONTROL_HANDLE) {
			return "手柄";
		} else if (c == Config.CONTROL_BODY) {

			return "体感";

		} else if (c == Config.CONTROL_PHONE) {

			return "手机";

		} else if (c == Config.CONTROL_MOUSE) {
			return "鼠标";
		} else {
			return "遥控";
		}
	}

	public static int getControllerRsid(int c) {

		if (c == Config.CONTROL_REMOTE) {
			return R.drawable.icon_remote;
		} else if (c == Config.CONTROL_HANDLE) {
			return R.drawable.icon_handler;
		} else if (c == Config.CONTROL_BODY) {
			return R.drawable.icon_body;
		} else if (c == Config.CONTROL_PHONE) {
			return R.drawable.icon_phone;
		} else if (c == Config.CONTROL_MOUSE) {
			return R.drawable.icon_mouse;
		} else {
			return R.drawable.remote_icon;
		}
	}

	/**
	 * 获取所有已安装应用信息
	 * 
	 * @param context
	 * @return
	 */
	public static List<AppInfo> readInstalledAppList(Context context) {

		List<AppInfo> appList = new ArrayList<AppInfo>();
		List<PackageInfo> packageList = context.getPackageManager()
				.getInstalledPackages(0);

		for (int i = 0; i < packageList.size(); i++) {

			PackageInfo packageInfo = packageList.get(i);

			// 过滤 系统应用 市场 与本身
			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					& !context.getPackageName().equals(packageInfo.packageName)) {
				AppInfo appInfo = new AppInfo();
				String appName = packageInfo.applicationInfo.loadLabel(
						context.getPackageManager()).toString();
				appInfo.setAppname(appName);
				appInfo.setVersionName(packageInfo.versionName);
				appInfo.setPackagename(packageInfo.packageName);
				appList.add(appInfo);
			}

		}

		return appList;
	}

	/**
	 * 获取所有应用包包名
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getAppList(Context context) {

		List<String> appList = new ArrayList<String>();
		List<PackageInfo> packageList = context.getPackageManager()
				.getInstalledPackages(0);

		for (int i = 0; i < packageList.size(); i++) {

			PackageInfo packageInfo = packageList.get(i);

			if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					& !context.getPackageName().equals(packageInfo.packageName)) {
				appList.add(packageInfo.packageName);
				// String appName = packageInfo.applicationInfo.loadLabel(
				// context.getPackageManager()).toString();
				// Log.e(TAG, appName+"---"+packageInfo.packageName);
			}

		}

		return appList;
	}

	
	
	public static Drawable readIconByPkg(Context context,String pkg) {
		// 根据包名获取程序图标
		ApplicationInfo application = new ApplicationInfo();
		try {
			application = context.getPackageManager().getApplicationInfo(pkg,
					PackageManager.GET_META_DATA);
			Drawable appIcon = context.getPackageManager().getApplicationIcon(
					application);

			return appIcon;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return new BitmapDrawable();

	}
	
	public static String parseApkSize(long size) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (size < 1024) {
			fileSizeString = df.format((double) size) + "B";
		} else if (size < 1024 * 1024) {
			fileSizeString = df.format((double) size / 1024) + "KB";
		} else if (size < 1024 * 1024 * 1024) {
			fileSizeString = df.format((double) size / (1024 * 1024)) + "MB";
		} else {
			fileSizeString = df.format((double) size / (1000 * 1000 * 1000))
					+ "GB";
		}
		return fileSizeString;
	}

	/**
	 * BASE64存储列表
	 * 
	 * @param context
	 * @param appList
	 * @param fileName
	 */
	public static void saveStringList(Context context,
			List<String> videoPkgList, String fileName) {
		try {
			// 先读取 再添加 最后存储
			SharedPreferences preferences = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			// 创建字节输出流
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			// 创建对象输出流，并封装字节流
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			// 将对象写入字节流
			oos.writeObject(videoPkgList);
			// 将字节流编码成base64的字符窜
			String appList_Base64 = new String(Base64.encodeBase64(baos
					.toByteArray()));
			Editor editor = preferences.edit();
			editor.putString(fileName, appList_Base64);
			editor.commit();

			Log.i("t=", "已保存视频包名列表");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 以BASE64码读取LIST<STRING>文件
	 * 
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static List<String> readStringList(Context context, String fileName)
			throws Exception {
		ArrayList<String> pkgList = new ArrayList<String>();
		SharedPreferences preferences = context.getSharedPreferences(
				Config.USER_DATA, Context.MODE_MULTI_PROCESS);
		String productBase64 = preferences.getString(fileName, "");

		// 读取字节
		byte[] base64 = Base64.decodeBase64(productBase64.getBytes());

		// 封装到字节流
		ByteArrayInputStream bais = new ByteArrayInputStream(base64);
		try {
			// 再次封装
			ObjectInputStream bis = new ObjectInputStream(bais);
			try {
				// 读取对象
				pkgList = (ArrayList<String>) bis.readObject();

				Log.i("t=", "已读取视频包名列表");

				return pkgList;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pkgList;
	}

	public static void saveVideoSpeed(Context context, boolean flag) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			Editor editor = sp.edit();
			editor.putBoolean(Config.SETTING_VIDEO_FLAG, flag);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Boolean readVideoSpeed(Context context) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			return sp.getBoolean(Config.SETTING_VIDEO_FLAG, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Boolean readUpdateHint(Context context) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			return sp.getBoolean(Config.SETTING_UPDATE_HINT_FLAG, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void saveUpdateHint(Context context, boolean flag) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			Editor editor = sp.edit();
			editor.putBoolean(Config.SETTING_UPDATE_HINT_FLAG, flag);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Boolean readAdbState(Context context) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			return sp.getBoolean(Config.SETTING_ADB_FLAG, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void saveAdbState(Context context, boolean flag) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			Editor editor = sp.edit();
			editor.putBoolean(Config.SETTING_ADB_FLAG, flag);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public static String formatSize(int size) {
		float sizeM = (float) size / (float) 1024;

		return String.format("%.1fM", sizeM);
	}
}
