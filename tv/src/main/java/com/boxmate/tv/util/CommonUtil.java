package com.boxmate.tv.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.boxmate.tv.R;
import com.boxmate.tv.R.color;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.string;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.ProcessInfo;
import com.boxmate.tv.entity.VersionInfo;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.AppListActivity;
import com.boxmate.tv.ui.manage.UninstallActivity;
import com.boxmate.tv.ui.manage.VersionUpdateActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.VpnService;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CommonUtil {
	private final static String TAG = "SystemUtil";
	private static Toast toast;
	public static String salt = "X$#ERTF!1";
	public static Context context;

	static public void showBigTip(Context context, String tip) {
		Toast toast = new Toast(context);
		// toast.setGravity(Gravity.CENTER_HORIZONTAL, 0,0);

		TextView textView = new TextView(context);
		textView.setBackgroundResource(R.drawable.tip_radius);
		textView.setText(tip);
		textView.setTextSize(CommonUtil.Px2Dp(context, context.getResources()
				.getDimension(R.dimen.px30)));
		textView.setTextColor(context.getResources().getColor(R.color.white));
		textView.setPadding(15, 15, 20, 20);
		TextPaint tp = textView.getPaint();
		tp.setFakeBoldText(true);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		textView.setLayoutParams(params);

		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(textView);
		toast.show();

	}

	/**
	 * 自定义圆角白底TOAST
	 * 
	 * @param appName
	 */
	public static void toast(Context context, String hint) {
		int dy = (int) context.getResources().getDimension(R.dimen.px110);
		LayoutInflater inflater = LayoutInflater.from(context);
		View layout = inflater.inflate(R.layout.toast, null);
		TextView tv_appname = (TextView) layout.findViewById(R.id.tv_appname);
		tv_appname.setText(hint);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 10, dy);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	public static Boolean deleteFileByPath(Context context, String apkPath) {

		try {
			File fileSuccess = new File(apkPath);
			if (fileSuccess.exists()) {
				if (fileSuccess.delete()) {
					Log.i("成功删除文件=", apkPath);
				}
			}
			return true;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public static void deleteFile(Context context, String downlaodUrl) {

		try {
			String targetTemp = context.getFilesDir() + "/"
					+ DataUtil.md5(downlaodUrl) + ".temp.apk";
			String targetSuccess = context.getFilesDir() + "/"
					+ DataUtil.md5(downlaodUrl) + ".apk";
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

	public static void installApkByFilePath(Context context, String apkPath) {

		try {
			Log.i(TAG, "安装包路径" + apkPath);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(apkPath)),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void installApk(String downlaodUrl, Context context) {

		try {
			String target = context.getFilesDir() + "/"
					+ DataUtil.md5(downlaodUrl) + ".apk";

			Log.i(TAG, "安装文件路径" + target);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setDataAndType(Uri.fromFile(new File(target)),
					"application/vnd.android.package-archive");
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void uninstallApk(Context context, String pkg) {
		Uri uri = Uri.parse("package:" + pkg);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static Boolean isApkExist(Context context, String downlaodUrl) {
		return new File(context.getFilesDir() + "/" + DataUtil.md5(downlaodUrl)
				+ ".apk").exists();
	}

	public static VersionInfo getVersionInfoPkg(Context context, String pkg) {
		VersionInfo version = new VersionInfo();
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(pkg, 0);
			version.setVersionName(packageInfo.versionName);
			version.setVersionCode(packageInfo.versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return version;
	}

	public static Boolean isAppInstalled(Context context, String packageName) {
		PackageManager pm = context.getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}

	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		Log.i("list", list.toString());

		return list.size() > 0;
	}

	public static float getDesPx(float px, Context context) {
		float scale = context.getResources().getDisplayMetrics().density;
		return scale * px;
	}

	public static void setBorderParams(RelativeLayout.LayoutParams params,
			View view, View border) {
		int leftOffset = 10;
		int topOffset = 0; // (int)r.getDimension(R.dimen.px11);
		int rightOffset = 12; // (int)r.getDimension(R.dimen.px24);
		int bottomOffset = 22; // (int)r.getDimension(R.dimen.px28);

		int left = view.getLeft();
		int top = view.getTop();

		int coverLeft = left - leftOffset;
		int coverTop = top - topOffset;// offset;额

		border.layout(coverLeft, coverTop, view.getRight() + rightOffset,
				view.getBottom() + bottomOffset);

		params.leftMargin = coverLeft;
		params.topMargin = coverTop;
		params.width = leftOffset + view.getWidth() + rightOffset;
		params.height = topOffset + view.getHeight() + bottomOffset;
		// border.setLayoutParams(params);

	}

	public static void setBorderParams(RelativeLayout.LayoutParams params,
			View view) {

		params.addRule(RelativeLayout.ALIGN_LEFT, view.getId());
		params.addRule(RelativeLayout.ALIGN_TOP, view.getId());

		int leftOffset = 10;
		int topOffset = 0; // (int)r.getDimension(R.dimen.px11);
		int rightOffset = 12; // (int)r.getDimension(R.dimen.px24);
		int bottomOffset = 22; // (int)r.getDimension(R.dimen.px28);

		int coverLeft = 0 - leftOffset;
		int coverTop = 0 - topOffset;// offset;额

		params.leftMargin = coverLeft;
		params.topMargin = coverTop;

		params.width = leftOffset + view.getLayoutParams().width + rightOffset;
		params.height = topOffset + view.getLayoutParams().height
				+ bottomOffset;

	}

	public static String formatSize(int size) {
		float sizeM = (float) size / (float) 1024;

		return String.format("%.1fM", sizeM);
	}

	public static HashMap<String, Integer> appInstalled;

	public static boolean checkPackageInstalled(String packageName) {

		if (appInstalled != null) {
			if (appInstalled.get(packageName) != null) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static void loadInstalledApp() {
		appInstalled = new HashMap<String, Integer>();
		if (context == null) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				List<PackageInfo> packages = context.getPackageManager()
						.getInstalledPackages(0);
				for (int i = 0; i < packages.size(); i++) {
					PackageInfo packageInfo = packages.get(i);
					if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						appInstalled.put(packageInfo.packageName,
								packageInfo.versionCode);
					}
				}

			}
		}).start();
	}

	public static void bindGoAppListEvent(final Context context, View view,
			final String title, final String action, final int rankType) {

		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, AppListActivity.class);
				intent.putExtra("title", title);
				intent.putExtra("action", action);
				intent.putExtra("rank_type", rankType);
				context.startActivity(intent);
			}
		});
	}

	public static void checkUpdate(final Context context) {
		checkUpdate(context, false);
	}

	public static void checkUpdate(final Context context, final Boolean showTip) {

		String pkName = context.getPackageName();
		try {

			final String versionName = context.getPackageManager()
					.getPackageInfo(pkName, 0).versionName;
			final int versionCode = context.getPackageManager().getPackageInfo(
					pkName, 0).versionCode;

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("action", "get_product_by_package");
			params.put("package", pkName);

			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			String channel = appInfo.metaData.getString("UMENG_CHANNEL");
			params.put("channel", channel);

			String api = HttpCommon.buildApiUrl(params);

			Log.i("check api", api);

			HttpCommon.getApi(api, new HttpSuccessInterface() {

				@Override
				public void run(String result) {
					// TODO Auto-generated method stub
					JSONObject infoJsonObject;
					try {
						infoJsonObject = new JSONObject(result);
						int onlineVersionCode = infoJsonObject
								.getInt("version_code");
						Log.i("version", onlineVersionCode + "");
						if (onlineVersionCode > versionCode) {
							Intent intent = new Intent(context,
									VersionUpdateActivity.class);
							intent.putExtra("version_name", versionName);
							intent.putExtra("online_version",
									infoJsonObject.getString("version_name"));
							intent.putExtra("icon_url",
									infoJsonObject.getString("icon_url"));
							intent.putExtra("apk_url",
									infoJsonObject.getString("apk_url"));
							intent.putExtra("update_info",
									infoJsonObject.getString("desc"));
							context.startActivity(intent);
						} else {
							if (showTip) {
								showBigTip(
										context,
										context.getResources()
												.getString(
														R.string.manage_hint_no_new_version));
							}
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static int Px2Dp(Context context, float px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5);
	}

	public static Drawable getDrawableByUrl(String url, Context context) {
		Drawable drawable = null;
		// 缓存
		String imageKey = DataUtil.md5(url);
		if (context == null) {
			return null;
		}
		File file = new File(context.getCacheDir() + "/" + imageKey);
		if (file.exists()) {
			Log.d("缓存读取", url);
			try {
				drawable = Drawable.createFromPath(file.getPath());
				if (drawable == null) {
					Log.e("获取缓存发生错误1", url);
					file.delete();
				}
			} catch (Exception e) {
				// TODO: handle exception
				file.delete();
				Log.e("获取缓存发生错误2", url);
				e.printStackTrace();
			}
			if (drawable != null) {
				return drawable;
			}

		}

		Log.i("缓存不存在从网络下载", url);
		try {
			FileOutputStream fos = new FileOutputStream(file);
			try {
				InputStream is = new URL(url).openStream();
				byte buf[] = new byte[256];
				do {
					// 循环读取
					int numread = 0;
					try {
						numread = is.read(buf);
						if (numread == -1) {
							break;
						}
						fos.write(buf, 0, numread);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} while (true);
				is.close();
				fos.close();
				try {
					drawable = Drawable.createFromPath(file.getPath());

					if (drawable == null) {
						Log.e("获取缓存发生错误3", url);
						file.delete();
					}
				} catch (Exception e) {
					Log.e("获取缓存发生错误4", url);
					file.delete();
					// TODO: handle exception
					e.printStackTrace();
				}
				fos.close();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		return drawable;
	}

	public static int getVersion() {

		if (context != null) {
			String pkName = context.getPackageName();
			int versionCode;
			try {
				versionCode = context.getPackageManager().getPackageInfo(
						pkName, 0).versionCode;
				return versionCode;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static String getChannel() {
		if (context != null) {
			ApplicationInfo appInfo;
			try {
				appInfo = context.getPackageManager().getApplicationInfo(
						context.getPackageName(), PackageManager.GET_META_DATA);
				String channel = appInfo.metaData.getString("UMENG_CHANNEL");
				return channel;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * 获取设备本身网卡的MAC地址
	 * 
	 * @param context
	 * @return
	 */
	public static String getWLANMACAddress() {

		String macAddress = "";

		if (context != null) {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = wm.getConnectionInfo();
			if (info != null) {
				macAddress = info.getMacAddress();
			} else {
				macAddress = "No Wifi Device";
			}
		}
		return macAddress;
	}

	public static String getGuid() {
		if (context != null) {
			TelephonyManager TelephonyMgr = (TelephonyManager) context
					.getSystemService(context.TELEPHONY_SERVICE);
			String szImei = "";
			if (TelephonyMgr != null) {
				szImei = TelephonyMgr.getDeviceId();
			}

			ContentResolver cr = context.getContentResolver();
			String m_szAndroidID = "";
			if (cr != null) {
				m_szAndroidID = Secure.getString(cr, Secure.ANDROID_ID);
			}

			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			String m_szWLANMAC = "";
			if (wm != null) {
				m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
			}

			String m_szLongID = szImei + m_szAndroidID + m_szWLANMAC;

			return DataUtil.md5(m_szLongID);
		} else {
			return "unknown";
		}
	}

	public static String getWifiInfo(Context context) {
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

	public static String getIpAddress() {
		String ipaddress = "0.0.0.0";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				if (intf.getName().toLowerCase().equals("eth0")
						|| intf.getName().toLowerCase().equals("wlan0")) {
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							ipaddress = inetAddress.getHostAddress().toString();
							if (!ipaddress.contains("::")) {// ipV6的地址
								return ipaddress;
							}
						}
					}
				} else {
					continue;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		return ipaddress;
	}

	private static String intToIp(int i) {

		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
				+ "." + ((i >> 24) & 0xFF);
	}

	public static String getSDPath() {
		File sdDir = null;
		try {
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
			// 判断sd卡是否存在
			if (sdCardExist) {
				sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
				return sdDir.toString();
			} else {
				return "/data/data/";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "/data/";

	}

	public static Boolean copyApkToSdCard(Context context, String apkName) {

		File file = new File(context.getFilesDir().getAbsolutePath() + "/"
				+ apkName + ".apk");

		if (!file.exists()) {
			InputStream abpath = context.getClass().getResourceAsStream(
					"/assets/" + apkName + ".apk");

			FileOutputStream fos;
			try {

				fos = context.openFileOutput(apkName + ".apk",
						Context.MODE_WORLD_READABLE);
				while (true) {
					byte[] buffer = new byte[200];
					int numread;
					try {
						numread = abpath.read(buffer);
						if (numread == -1) {
							break;
						}
						fos.write(buffer, 0, numread);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				fos.close();
				abpath.close();
				Log.i(TAG, "apk复制成功");
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "apk复制失败");
			return false;
		} else {
			Log.i(TAG, "apk已存在");
			return true;
		}

	}

	public static int getAvailSpace() {
		int avail = 0;
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		// Log.d("", "block大小:"+ blockSize+",block数目:"+
		// blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
		// Log.d("", "可用的block数目：:"+ availCount+",可用大小:"+
		// availCount*blockSize/1024+"KB");

		avail += availCount * blockSize / (1024 * 1024 * 1024);
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sfSd = new StatFs(sdcardDir.getPath());
			long sdBlockSize = sfSd.getBlockSize();
			long sdBlockCount = sfSd.getBlockCount();
			long sdAvailCount = sfSd.getAvailableBlocks();
			// Log.d("", "block大小:"+ blockSize+",block数目:"+
			// blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
			// Log.d("", "可用的block数目：:"+ availCount+",剩余空间:"+
			// availCount*blockSize/1024+"KB");

			avail += sdAvailCount * sdBlockSize / (1024 * 1024 * 1024);
		}
		return avail;
	}

	public static int getTotalSpace() {
		int total = 0;
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		// Log.d("", "block大小:"+ blockSize+",block数目:"+
		// blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
		// Log.d("", "可用的block数目：:"+ availCount+",可用大小:"+
		// availCount*blockSize/1024+"KB");

		total += blockCount * blockSize / (1024 * 1024 * 1024);
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sfSd = new StatFs(sdcardDir.getPath());
			long sdBlockSize = sfSd.getBlockSize();
			long sdBlockCount = sfSd.getBlockCount();
			long sdAvailCount = sfSd.getAvailableBlocks();
			// Log.d("", "block大小:"+ blockSize+",block数目:"+
			// blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
			// Log.d("", "可用的block数目：:"+ availCount+",剩余空间:"+
			// availCount*blockSize/1024+"KB");

			total += sdBlockCount * sdBlockSize / (1024 * 1024 * 1024);
		}
		DecimalFormat df = new DecimalFormat("###.00");

		return total;
	}

	public static void saveGabageSize(Context context, long gabageSize) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_PRIVATE);
			long before = sp.getLong(Config.CLEAR_GABAGE_COUNT, 0);
			Editor editor = sp.edit();
			editor.putLong(Config.CLEAR_GABAGE_COUNT, before + gabageSize);
			editor.commit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static long readGagabeSize() {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_PRIVATE);
			return sp.getLong(Config.CLEAR_GABAGE_COUNT, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static void saveWifiName(Context context) {

		try {

			String wifiName = SystemUtil.getNetName(context);
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			Editor editor = sp.edit();
			editor.putString(Config.WIFI_NAME, wifiName);
			editor.commit();
			Log.i(TAG, "当前网络＝" + wifiName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String readWifiName() {

		try {

			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			return sp.getString(Config.WIFI_NAME, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String getDeviceName() {
		return android.os.Build.PRODUCT + "-" + android.os.Build.MODEL;
	}

	public static Boolean isFirstCheckUpgrade(Context context) {

		try {
			SharedPreferences sp = context.getSharedPreferences(
					Config.USER_DATA, Context.MODE_PRIVATE);
			int lastCheckDay = sp.getInt(Config.UPGRADE_CHECK_DATE, -1);
			Editor ed = sp.edit();
			Calendar c = Calendar.getInstance();
			int day = c.get(Calendar.DAY_OF_MONTH);

			if (lastCheckDay == -1) {
				ed.putInt(Config.UPGRADE_CHECK_DATE, day - 1);
				ed.commit();
				return false;
			}

			if (lastCheckDay < day) {

				ed.putInt(Config.UPGRADE_CHECK_DATE, day);
				ed.commit();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
