package reco.frame.tv.bitmap;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

public class LoaderImpl {
	private final static String TAG = "LoaderImpl";
	private Context context;
	// 内存中的软应用缓存
	static public LruCache<String, Bitmap> mLruCache; 

	// 是否缓存图片至本地文件
	private boolean cache2FileFlag = true;

	private String cachedDir;

	public LoaderImpl(Context context) {
		this.context = context;
		this.cache2FileFlag = true;
		this.cachedDir=context.getCacheDir().getAbsolutePath();
	}
	
	public static void initLruCache() {
		if (LoaderImpl.mLruCache == null) {
			// 获取到可用内存的最大值，使用内存超出这个值会引起OutOfMemory异常。
			// LruCache通过构造函数传入缓存值，以KB为单位。
			int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			// 使用最大可用内存值的1/6作为缓存的大小。
			int cacheSize = maxMemory / 5;
			LoaderImpl.mLruCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					// 重写此方法来衡量每张图片的大小，默认返回图片数量。
					return bitmap.getByteCount() / 1024;
				}
			};
		}

	}

	/**
	 * 是否缓存图片至外部文件
	 * 
	 * @param flag
	 */
	public void setCache2File(boolean flag) {
		cache2FileFlag = flag;
	}

	/**
	 * 设置缓存图片到外部文件的路径
	 * 
	 * @param cacheDir
	 */
	public void setCachedDir(String cacheDir) {
		this.cachedDir = cacheDir;
	}

	/**
	 * 从网络端下载图片
	 * 
	 * @param url
	 *            网络图片的URL地址
	 * @param cache2Memory
	 *            是否缓存(缓存在内存中)
	 * @return bitmap 图片bitmap结构
	 * 
	 */
	public Bitmap getBitmapFromUrl(String url, boolean cache2Memory) {
		Bitmap bitmap = null;
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);

			if (cache2Memory) {
				// 1.缓存bitmap至内存软引用中
				mLruCache.put(md5(url), bitmap);
				if (cache2FileFlag) {
					String fileName = md5(url);
					String filePath = this.cachedDir + "/" + fileName;
					FileOutputStream fos = new FileOutputStream(filePath);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.close();
					Log.d(TAG, "成功存储图片到本地！");
				}
			}

			is.close();
			conn.disconnect();
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 从内存缓存中获取bitmap
	 * 
	 * @param url
	 * @return bitmap or null.
	 */
	public static Bitmap getBitmapFromMemory(String key) {
		if (mLruCache==null) {
			initLruCache();
		}
		return mLruCache.get(md5(key));
	}
	
	/**
	 * 存储图到到缓存中
	 * @param key
	 * @param bitmap
	 */
	public static void setBitmapToMemory(String key, Bitmap bitmap) { 
		String md5=md5(key);
	    if (mLruCache.get(md5) == null) { 
	    	LoaderImpl.mLruCache.put(md5, bitmap); 
	    } 
	} 

	public Bitmap loadIconByPkg(String pkg) {
		// 根据包名获取程序图标
		ApplicationInfo application = new ApplicationInfo();
		try {
			application = context.getPackageManager().getApplicationInfo(pkg,
					PackageManager.GET_META_DATA);
			Drawable appIcon = context.getPackageManager().getApplicationIcon(
					application);
			Bitmap bmp = ((BitmapDrawable) appIcon).getBitmap();
			if (bmp != null)
				mLruCache.put(md5(pkg), bmp);
			return bmp;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;

	}

	private Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成 bitmap
	{
		int width = drawable.getIntrinsicWidth(); // 取 drawable 的长宽
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // 取 drawable 的颜色格式
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应
																	// bitmap
		Canvas canvas = new Canvas(bitmap); // 建立对应 bitmap 的画布
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // 把 drawable 内容画到画布中
		return bitmap;
	}

	private Drawable zoomDrawable(Drawable drawable, float w, float h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
		float scaleWidth = (w / width); // 计算缩放比例
		float scaleHeight = (h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
		return new BitmapDrawable(newbmp); // 把 bitmap 转换成 drawable 并返回
	}

	/**
	 * 从外部文件缓存中获取bitmap
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getBitmapFromFile(String url) {
		Bitmap bitmap = null;
		String fileName = md5(url);
		if (fileName == null)
			return null;

		String filePath = cachedDir + "/" + fileName;

		try {
			FileInputStream fis = new FileInputStream(filePath);
			bitmap = BitmapFactory.decodeStream(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			bitmap = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (bitmap != null) {
			mLruCache.put(fileName,bitmap);
		}

		return bitmap;
	}

	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// 创建一个Buffer字符串
		byte[] buffer = new byte[1024];
		// 每次读取的字符串长度，如果为-1，代表全部读取完毕
		int len = 0;
		// 使用一个输入流从buffer里把数据读取出来
		while ((len = inStream.read(buffer)) != -1) {
			// 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
			outStream.write(buffer, 0, len);
		}
		// 关闭输入流
		inStream.close();
		// 把outStream里的数据写入内存
		return outStream.toByteArray();
	}

	/**
	 * MD5 加密
	 */
	private static String md5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
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

}
