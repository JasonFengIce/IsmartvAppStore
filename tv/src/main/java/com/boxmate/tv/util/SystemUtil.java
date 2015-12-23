package com.boxmate.tv.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.ProcessInfo;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Debug;
import android.util.Log;
import android.widget.ImageView;

public class SystemUtil {
	private final static String TAG = "SystemUtil";

	public static boolean adbConnect(String ip) {
		try {
			Process p = Runtime.getRuntime()
					.exec("adb connect " + ip + ":5555");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String result = "";
			String inline = "";
			while ((inline = br.readLine()) != null) {
				System.out.println(inline);
				result += inline;
				Log.i(TAG, inline);
			}
			if (result.contains("connected")) {
				Log.i(TAG, "成功连接ADB！！！");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static String[] deviceBackgroundFilterArray = { "MiBOX", "MiTV" };
	private static String[] deviceAdbFilterArray = { "MiBOX", "MiTV", "INPHIC" };

	public static void restartAdb() {

	}

	public static boolean adbDisConnect(String ip) {
		try {
			Process p = Runtime.getRuntime().exec(
					"adb disconnect " + ip + ":5555");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String result = "";
			String inline = "";
			while ((inline = br.readLine()) != null) {
				System.out.println(inline);
				result += inline;
				Log.i("adb=", inline);
			}
			if (!result.contains("No such device")) {
				Log.i("t=", "已断开ADB！！！");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static Boolean installApp(String apkPath, String ip) {
		String cmd = "adb -s " + ip + ":5555 shell pm install -r " + apkPath;
		Log.i(TAG, cmd);

		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String inline = "";
			while ((inline = br.readLine()) != null) {
				System.out.println(inline);
				Log.i(TAG, inline);
				if (inline.contains("Success")) {
					Log.i(TAG, "成功安装！！！");
					return true;
				} else if (inline
						.contains("INSTALL_FAILED_INSUFFICIENT_STORAGE")) {
					Log.i(TAG, "内存不足 安装失败！！！");
					return false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean uninstallApp(String packageName, String ip) {
		String cmd = "adb -s " + ip + ":5555  uninstall " + packageName;
		Log.i(TAG, cmd);
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String inline = "";
			while ((inline = br.readLine()) != null) {
				System.out.println(inline);
				Log.i(TAG, inline);
			}
			if (!"".equals(inline)) {
				Log.i(TAG, "成功卸载！！！");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 得到正在运行中的应用信息
	 * 
	 * @param intent
	 * @param pid
	 * @return
	 */
	public static List<ProcessInfo> queryAllRunningAppInfo(Context context) {
		Log.i("---", "--正在查询运行中的应用---------");

		PackageManager pm = context.getPackageManager();
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 查询所有已经安装的应用程序
		List<ApplicationInfo> listAppcations = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		Collections.sort(listAppcations,
				new ApplicationInfo.DisplayNameComparator(pm));// 排序

		// 保存所有正在运行的包名 以及它所在的进程信息
		Map<String, ActivityManager.RunningAppProcessInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningAppProcessInfo>();

		// 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
		List<ActivityManager.RunningAppProcessInfo> appProcessList = am
				.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {

			String[] pkgNameList = appProcess.pkgList; // 获得运行在该进程里的所有应用程序包

			// 输出所有应用程序的包名
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				pgkProcessAppMap.put(pkgName, appProcess);
			}
		}
		List<ProcessInfo> runningAppInfos = new ArrayList<ProcessInfo>(); // 保存过滤查到的AppInfo

		for (ApplicationInfo app : listAppcations) {
			// 如果该包名存在 则构造一个RunningAppInfo对象

			if (pgkProcessAppMap.containsKey(app.packageName)
					&& (app.flags & ApplicationInfo.FLAG_SYSTEM) == 0
					&& !context.getPackageName().equals(app.packageName)) {
				// 获得该packageName的 pid 和 processName
				int pid = pgkProcessAppMap.get(app.packageName).pid;
				String processName = pgkProcessAppMap.get(app.packageName).processName;
				int[] myMempid = new int[] { pid };
				// 此MemoryInfo位于android.os.Debug.MemoryInfo包中，用来统计进程的内存信息
				Debug.MemoryInfo[] memoryInfo = am
						.getProcessMemoryInfo(myMempid);
				// 获取进程占内存用信息 kb单位
				int memSize = memoryInfo[0].dalvikPrivateDirty;
				ProcessInfo process = new ProcessInfo();
				process.setAppName((String) app.loadLabel(pm));
				process.setPackageName(app.packageName);
				process.setPid(pid);
				process.setMemory(memSize);
				process.setProcessName(processName);
				runningAppInfos.add(process);
			}
		}

		return runningAppInfos;
	}

	/**
	 * 杀死所有正运行中的应用
	 * 
	 * @throws Exception
	 */
	public static void killRunningTask(Context context) throws Exception {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ProcessInfo> runningList = queryAllRunningAppInfo(context);
		for (ProcessInfo running : runningList) {
			am.killBackgroundProcesses(running.getPackageName());
			Log.i(TAG,
					"正在杀死进程" + running.getPackageName() + "--"
							+ running.getProcessName());
		}
	}

	/**
	 * 杀死所有正运行中的应用
	 * 
	 * @throws Exception
	 */
	public static void killRunningTask(Context context, String filterPkg)
			throws Exception {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ProcessInfo> runningList = queryAllRunningAppInfo(context);
		for (ProcessInfo running : runningList) {
			if (!running.getPackageName().equals(filterPkg)) {
				am.killBackgroundProcesses(running.getPackageName());
				Log.i(TAG,
						"正在杀死进程" + running.getPackageName() + "--"
								+ running.getProcessName());
			}

		}
	}

	public static List<ProcessInfo> getAllTask(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		PackageManager pm = context.getPackageManager();
		List<RunningAppProcessInfo> runningAppProcessInfos = am
				.getRunningAppProcesses();
		List<ProcessInfo> taskInfos = new ArrayList<ProcessInfo>();
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
			ProcessInfo taskInfo = new ProcessInfo();
			int id = runningAppProcessInfo.pid;
			taskInfo.setId(id);
			String packageName = runningAppProcessInfo.processName;
			taskInfo.setPackageName(packageName);
			try {
				// ApplicationInfo是AndroidMainfest文件里面整个Application节点的封装
				ApplicationInfo applicationInfo = pm.getPackageInfo(
						packageName, 0).applicationInfo;
				// 应用的名字
				String name = applicationInfo.loadLabel(pm).toString();
				taskInfo.setName(name);

				// 过滤进程 保留指定及安全进程
				taskInfo.setSystemProcess(!filterProcess(applicationInfo));

				// 过滤优先级较高进程
				if (runningAppProcessInfo.importance < ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
					taskInfo.setSystemProcess(true);
				}

			} catch (Exception e) {
				e.printStackTrace();

				// 当遇到没有界面的和图标的一些进程时候的处理方式
				taskInfo.setName(packageName);
				taskInfo.setSystemProcess(true);
			}

			// 可以返回一个内存信息的数组，传进去的id有多少个，就返回多少个对应id的内存信息
			android.os.Debug.MemoryInfo[] memoryInfos = am
					.getProcessMemoryInfo(new int[] { id });
			// 拿到占用的内存空间
			int memory = memoryInfos[0].getTotalPrivateDirty();
			taskInfo.setMemory(memory);
			taskInfos.add(taskInfo);
			taskInfo = null;
		}
		return taskInfos;
	}

	/**
	 * 只杀死优先级低于服务的进程&过滤下载进程
	 * 
	 * @param info
	 * @return
	 */
	private static boolean filterProcess(ApplicationInfo info) {
		// 有些系统应用是可以更新的，如果用户自己下载了一个系统的应用来更新了原来的，
		// 它就不是系统应用啦，这个就是判断这种情况的

		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0)// 判断是不是系统应用
		{
			return true;
		}
		return false;
	}

	/**
	 * 获得所有已安装并且正在运行中的应用包集合
	 * 
	 * @param intent
	 * @param pid
	 * @return
	 */
	public static List<String> quaryAllRunningPkg(Context context,
			List<String> videoPkgList) {
		// Log.i("---", "--正在查询运行中的视频---------");
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		// 保存所有正在运行的包名 以及它所在的进程信息
		List<String> runningList = new ArrayList<String>();
		// 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
		List<ActivityManager.RunningAppProcessInfo> appProcessList = am
				.getRunningAppProcesses();

		for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {

			String[] pkgNameList = appProcess.pkgList; // 获得运行在该进程里的所有应用程序包

			// 输出所有应用程序的包名
			for (int i = 0; i < pkgNameList.length; i++) {
				String pkgName = pkgNameList[i];
				runningList.add(pkgName);
			}
		}
		List<String> runningVideos = new ArrayList<String>(); // 保存过滤查到的AppInfo

		for (String pkg : videoPkgList) {
			if (runningList.contains(pkg)) {
				runningVideos.add(pkg);
			}
		}

		return runningVideos;
	}

	public static Boolean isTopActivity(Context context, String pkg) {

		try {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// 应用程序位于堆栈的顶层
				if (pkg.equals(tasksInfo.get(0).topActivity.getPackageName())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String getAppNameByPkg(Context context, String pkg) {

		PackageManager pm = context.getPackageManager();
		String name = "";
		try {
			name = pm.getApplicationLabel(
					pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA))
					.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return name;
	}

	public static Drawable getAppIconByPkg(PackageManager pm,String pkg) {

		ApplicationInfo application = new ApplicationInfo();
		try {
			application = pm.getApplicationInfo(pkg,
					PackageManager.GET_META_DATA);
			return pm.getApplicationIcon(application);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static Boolean checkBackgroundAvailable() {
		String deviceName = android.os.Build.PRODUCT + "-"
				+ android.os.Build.MODEL;
		try {
			for (int i = 0; i < deviceBackgroundFilterArray.length; i++) {
				if (deviceName.contains(deviceBackgroundFilterArray[i])) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	public static Boolean checkAdbAvailable() {
		String deviceName = android.os.Build.PRODUCT + "-"
				+ android.os.Build.MODEL;
		try {
			for (int i = 0; i < deviceAdbFilterArray.length; i++) {
				if (deviceName.contains(deviceAdbFilterArray[i])) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	
	/**
	 * 进程是否首次置顶
	 * @param context
	 * @return
	 */
	public static Boolean countHoursBeforeLastLauncher(Context context,String pkg){
		
		try {
			SharedPreferences sp=context.getSharedPreferences(Config.USER_DATA, Context.MODE_MULTI_PROCESS);
			String saveName=Config.PKG_PREFIX+pkg;
			int lastCheckHours=sp.getInt(saveName, -1);
			Editor ed=sp.edit();
			Calendar c=Calendar.getInstance();
			int hours=c.get(Calendar.HOUR);
			
			if (lastCheckHours==-1) {
				ed.putInt(saveName, hours-1);
				ed.commit();
				return true;
			}
			
			if ((hours-lastCheckHours)>7) {
				ed.putInt(saveName, hours);
				ed.commit();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return false;
	}


	/**
	 * 获取网络信号强度 若是有线网 则默认满格
	 * 
	 * @param context
	 * @return int
	 */
	public static String getNetName(Context context) {
		String netName = "";
		
		try {
			ConnectivityManager ctm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = ctm.getActiveNetworkInfo();

			if (null != activeNetwork && activeNetwork.isAvailable()) {

				switch (activeNetwork.getType()) {
				case ConnectivityManager.TYPE_ETHERNET:
					netName = "TYPE_ETHERNET";
					break;
				case ConnectivityManager.TYPE_WIFI:

					WifiManager wm = (WifiManager) context
							.getSystemService(context.WIFI_SERVICE);
					WifiInfo wfInfo = wm.getConnectionInfo();
					if (null != wfInfo) {
						netName=wfInfo.getSSID();
					}
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return netName;
	}

}
