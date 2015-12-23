package com.boxmate.tv.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;

public class CacheHelper {

	private final static String TAG = "CacheHelper";
	private Context context;
	private long cachesize; // 缓存大小
	private long datasize; // 数据大小
	private long codesize; // 应用程序大小
	private long totalsize; // 总大小
	private SyncCallBack mSyncCallBack;
	private static CacheHelper mCacheHelper;
	private Handler handler;

	public CacheHelper(Context context) {
		this.context = context;
		handler = new Handler();
	}

	public static synchronized CacheHelper create(Context ctx) {
		if (mCacheHelper == null) {
			mCacheHelper = new CacheHelper(ctx.getApplicationContext());
		}
		return mCacheHelper;
	}

	public synchronized void queryPacakgeSize(String pkgName,
			SyncCallBack mCallBack) throws Exception {
		this.mSyncCallBack = mCallBack;
		if (pkgName != null) {
			// 使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
			PackageManager pm = context.getPackageManager(); // 得到pm对象
			try {
				// 通过反射机制获得该隐藏函数
				Method getPackageSizeInfo = pm.getClass().getMethod(
						"getPackageSizeInfo", String.class,
						IPackageStatsObserver.class);
				// 调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
				getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver());
			} catch (Exception ex) {
				Log.e(TAG, "NoSuchMethodException");
				ex.printStackTrace();
				throw ex; // 抛出异常
			}
		}
	}

	private static long getEnvironmentSize() {
		File localFile = Environment.getDataDirectory();
		long l1;
		if (localFile == null)
			l1 = 0L;
		while (true) {

			String str = localFile.getPath();
			StatFs localStatFs = new StatFs(str);
			long l2 = localStatFs.getBlockSize();
			l1 = localStatFs.getBlockCount() * l2;
			return l1;
		}
	}

	public void clearPackageCache() {
		PackageManager pm = context.getPackageManager();
		Class[] arrayOfClass = new Class[2];
		Class localClass2 = Long.TYPE;
		arrayOfClass[0] = localClass2;
		arrayOfClass[1] = IPackageDataObserver.class;
		try {
			Method localMethod = pm.getClass().getMethod(
					"freeStorageAndNotify", arrayOfClass);

			Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
			Object[] arrayOfObject = new Object[2];
			arrayOfObject[0] = localLong;
			localMethod.invoke(pm, localLong, new IPackageDataObserver.Stub() {
				public void onRemoveCompleted(String packageName,
						boolean succeeded) throws RemoteException {

					Log.e(TAG, packageName + "---" + succeeded);
				}
			});
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	// aidl文件形成的Bindler机制服务类
	public class PkgSizeObserver extends IPackageStatsObserver.Stub {
		/***
		 * 回调函数，
		 * 
		 * @param pStatus
		 *            ,返回数据封装在PackageStats对象中
		 * @param succeeded
		 *            代表回调成功
		 */
		@Override
		public synchronized void onGetStatsCompleted(final PackageStats pStats,
				boolean succeeded) throws RemoteException {
			// TODO Auto-generated method stub
			// cachesize = pStats.cacheSize; // 缓存大小
			// datasize = pStats.dataSize; // 数据大小
			// codesize = pStats.codeSize; // 应用程序大小
			totalsize = cachesize + datasize + codesize;
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					mSyncCallBack.onDataLoaded(pStats.codeSize , pStats.dataSize
							, pStats.cacheSize);
					
				}
			});
			
		
		}
	}

	// 系统函数，字符串转换 long -String (kb)
	private String formateFileSize(long size) {
		return Formatter.formatFileSize(context, size);
	}

	public interface SyncCallBack {
		public void onDataLoaded(long codesize,long dataSize,long cacheSize);
	}

}
