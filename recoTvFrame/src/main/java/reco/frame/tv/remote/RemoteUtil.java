package reco.frame.tv.remote;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class RemoteUtil {
	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

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

	public static void deleteFile(Context context, String downlaodUrl) {

		try {
			String targetTemp = context.getFilesDir() + "/" + md5(downlaodUrl)
					+ ".temp.apk";
			String targetSuccess = context.getFilesDir() + "/"
					+ md5(downlaodUrl) + ".apk";
			File fileTemp = new File(targetTemp);
			if (fileTemp.exists()) {
				fileTemp.delete();
				Log.i("成功删除临时文件=", downlaodUrl);
			}
			File fileSuccess = new File(targetSuccess);
			if (fileSuccess.exists()) {
				fileSuccess.delete();
				Log.i("成功删除安装包=", downlaodUrl);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	public static boolean isFileExist(String path) {

		try {
			if (new File(path).exists()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean installApk(Context context, String path) {

		try {
			// String target = context.getFilesDir() + "/"
			// + DataUtil.md5(downlaodUrl) + ".apk";

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(path)),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean hasInstall(Context context, String packageName) {
		PackageInfo packageInfo;

		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
			if (packageInfo != null) {
				return true;
			}
		} catch (Exception e) {
			packageInfo = null;
			e.printStackTrace();
		}
		return false;
	}

	public static String listToJson(List<TaskInfo> taskList) {

		Gson gson = new Gson();
		String json = gson.toJson(taskList);
		Log.i("t=", "已生成JSON串");
		return json;
	}

	public static String objToJson(TaskInfo task) {

		Gson gson = new Gson();
		String json = gson.toJson(task);
		Log.i("t=", "已生成JSON对象");
		return json;
	}

	public static String getIp(Context context) {
		try {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			return intToIp(wifiInfo.getIpAddress());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	private static String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}
}
