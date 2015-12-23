package com.boxmate.tv.ui.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.util.CommonUtil;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ToolsSystem extends Activity {

	private String[][] deviceNameArray = { { "MiBOX_iCNTV", "小米盒子" },
			{ "MiBOX1S", "新小米盒子" }, { "MiBOX2", "小米盒子增强版" },
			{ "MagicBox2", "天猫魔盒二代" }, { "MagicBox", "天猫魔盒" },
			{ "INPHIC", "英菲克盒子" }, { "Softwiner", "迈乐盒子" },
			{ "10MOONS", "天敏盒子" }, { "Honzon", "宏卓盒子" }, { "Konka", "康佳电视" },
			{ "HIMEDIA", "海美迪盒子" }, { "CVTE", "海尔电视" }, { "Skyworth", "创维" },
			{ "MiTV", "小米电视" }, { "mango", "芒果盒子" }, { "D39LW", "海尔电视" } };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_system);

		TextView tv_ip = (TextView) findViewById(R.id.tv_ip);
		TextView tv_device = (TextView) findViewById(R.id.tv_device);
		TextView tv_version = (TextView) findViewById(R.id.tv_version);
		TextView tv_cpu = (TextView) findViewById(R.id.tv_cpu);
		TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
		TextView tv_space = (TextView) findViewById(R.id.tv_space);
		tv_ip.setText(CommonUtil.getIpAddress());
		String deviceName = android.os.Build.PRODUCT + "-"
				+ android.os.Build.MODEL;
		try {

			for (int i = 0; i < deviceNameArray.length; i++) {
				if (deviceName.contains(deviceNameArray[i][0])) {
					deviceName = deviceNameArray[i][1];
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		tv_device.setText(deviceName);
		tv_version.setText(getSystemVersion());

		String coreNum = getCpuCoreNum() + "核";
		switch (getCpuCoreNum()) {
		case 1:
			coreNum = "单核";
			break;

		case 2:
			coreNum = "双核";
			break;

		case 3:
			coreNum = "三核";
			break;
		case 4:
			coreNum = "四核";
			break;
		case 5:
			coreNum = "五核";
			break;
		case 6:
			coreNum = "六核";
			break;

		case 7:
			coreNum = "七核";
			break;
		case 8:
			coreNum = "八核";
			break;
		}
		String cupName = getCpuName();
		if (cupName == null) {

		} else if (cupName.length() < 2) {
			cupName = "";
		}
		tv_cpu.setText(cupName + " " + coreNum + getCpuFrequence() + "GHz");
		tv_memory.setText(getAvailMemory() + "M/" + getTotalMemory()
				+ "M 可用/总共");
		tv_space.setText(getSpace());

	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private String getSystemVersion() {

		String version = "未知版本";

		switch (android.os.Build.VERSION.SDK_INT) {

		case 15:
			version = "Android 4.0";
			break;

		case 16:
			version = "Android 4.1";
			break;

		case 17:

			version = "Android 4.2";
			break;
		case 18:

			version = "Android 4.3";
			break;
		case 19:

			version = "Android 4.4";
			break;
		}

		return version;
	}

	private String getSpace() {

		float avail = 0;
		float total = 0;
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		long availCount = sf.getAvailableBlocks();
		// Log.d("", "block大小:"+ blockSize+",block数目:"+
		// blockCount+",总大小:"+blockSize*blockCount/1024+"KB");
		// Log.d("", "可用的block数目：:"+ availCount+",可用大小:"+
		// availCount*blockSize/1024+"KB");

		avail += availCount * blockSize / (1024 * 1024 * 1024 * 1.0f);
		total += blockCount * blockSize / (1024 * 1024 * 1024 * 1.0f);
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

			avail += sdAvailCount * sdBlockSize / (1024 * 1024 * 1024 * 1.0f);
			total += sdBlockCount * sdBlockSize / (1024 * 1024 * 1024 * 1.0f);
		}
		DecimalFormat df = new DecimalFormat("###.00");

		String insertNumA = "", insertNumB = "";
		if (avail < 1) {
			insertNumA = "0";
		}
		if (total < 1) {
			insertNumB = "0";
		}

		return insertNumA + df.format(avail) + "G/" + insertNumB
				+ df.format(total) + "G 可用/总共";

	}

	private long getAvailMemory() {
		// 获取android当前可用内存大小
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		// mi.availMem; 当前系统的可用内存

		// return Formatter.formatFileSize(context, mi.availMem);// 将获取的内存大小规格化
		return mi.availMem / (1024 * 1024);
	}

	private static long getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

			arrayOfString = str2.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(str2, num + "\t");
			}

			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
			localBufferedReader.close();

		} catch (IOException e) {
		}
		// return Formatter.formatFileSize(context, initial_memory);//
		// Byte转换为KB或者MB，内存大小规格化
		return initial_memory / (1024 * 1024);
	}

	public static String getCpuName() {
		String cpuName = "N/A";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/cpuinfo"));
			String line = reader.readLine();
			while (line != null) {
				if (line.toLowerCase().indexOf("processor") >= 0) {
					String[] array = line.split(":\\s");
					cpuName = array[1].trim();
					break;
				}
				line = reader.readLine();
			}
			reader.close();
			reader = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cpuName;
	}

	public static int getCpuCoreNum() {
		class CpuFilter implements FileFilter {
			public boolean accept(File pathname) {
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}
		try {
			File dir = new File("/sys/devices/system/cpu/");
			File[] files = dir.listFiles(new CpuFilter());
			return files.length;
		} catch (Exception e) {
			return 1;
		}
	}

	public static String getCpuFrequence() {
		ProcessBuilder cmd;
		try {
			String[] args = { "/system/bin/cat",
					"/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			float frequence = 0;
			try {
				frequence = Float.parseFloat(line) / (1024 * 1024 * 1.0f);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (frequence > 1) {
				DecimalFormat df = new DecimalFormat("###.00");
				return df.format(frequence);
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return "1.0";
	}
}
