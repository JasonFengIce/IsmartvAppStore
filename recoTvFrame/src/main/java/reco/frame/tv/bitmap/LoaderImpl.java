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
	static public LruCache<String, Bitmap> mLruCache;

	private boolean cache2FileFlag = true;

	private String cachedDir;

	public LoaderImpl(Context context) {
		this.context = context;
		this.cache2FileFlag = true;
		this.cachedDir=context.getCacheDir().getAbsolutePath();
	}
	
	public static void initLruCache() {
		if (LoaderImpl.mLruCache == null) {
			// ��ȡ�������ڴ�����ֵ��ʹ���ڴ泬�����ֵ������OutOfMemory�쳣��
			// LruCacheͨ���캯���뻺��ֵ����KBΪ��λ��
			int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			// ʹ���������ڴ�ֵ��1/6��Ϊ����Ĵ�С��
			int cacheSize = maxMemory / 5;
			LoaderImpl.mLruCache = new LruCache<String, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					// ��д�˷���������ÿ��ͼƬ�Ĵ�С��Ĭ�Ϸ���ͼƬ������
					return bitmap.getByteCount() / 1024;
				}
			};
		}

	}

	/**
	 * �Ƿ񻺴�ͼƬ���ⲿ�ļ�
	 * 
	 * @param flag
	 */
	public void setCache2File(boolean flag) {
		cache2FileFlag = flag;
	}

	/**
	 * ���û���ͼƬ���ⲿ�ļ���·��
	 * 
	 * @param cacheDir
	 */
	public void setCachedDir(String cacheDir) {
		this.cachedDir = cacheDir;
	}

	/**
	 * �����������ͼƬ
	 * 
	 * @param url
	 *            ����ͼƬ��URL��ַ
	 * @param cache2Memory
	 *            �Ƿ񻺴�(�������ڴ���)
	 * @return bitmap ͼƬbitmap�ṹ
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
				// 1.����bitmap���ڴ���������
				mLruCache.put(md5(url), bitmap);
				if (cache2FileFlag) {
					String fileName = md5(url);
					String filePath = this.cachedDir + "/" + fileName;
					FileOutputStream fos = new FileOutputStream(filePath);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
					fos.close();
					Log.d(TAG, "�ɹ��洢ͼƬ�����أ�");
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
	 * ���ڴ滺���л�ȡbitmap
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
	 * �洢ͼ����������
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
		// ��ݰ����ȡ����ͼ��
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

	private Bitmap drawableToBitmap(Drawable drawable) // drawable ת���� bitmap
	{
		int width = drawable.getIntrinsicWidth(); // ȡ drawable �ĳ���
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // ȡ drawable ����ɫ��ʽ
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // ������Ӧ
																	// bitmap
		Canvas canvas = new Canvas(bitmap); // ������Ӧ bitmap �Ļ���
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // �� drawable ���ݻ���������
		return bitmap;
	}

	private Drawable zoomDrawable(Drawable drawable, float w, float h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable ת���� bitmap
		Matrix matrix = new Matrix(); // ��������ͼƬ�õ� Matrix ����
		float scaleWidth = (w / width); // �������ű���
		float scaleHeight = (h / height);
		matrix.postScale(scaleWidth, scaleHeight); // �������ű���
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // �����µ� bitmap ���������Ƕ�ԭ bitmap �����ź��ͼ
		return new BitmapDrawable(newbmp); // �� bitmap ת���� drawable ������
	}

	/**
	 * ���ⲿ�ļ������л�ȡbitmap
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
		// ����һ��Buffer�ַ�
		byte[] buffer = new byte[1024];
		// ÿ�ζ�ȡ���ַ��ȣ����Ϊ-1�����ȫ����ȡ���
		int len = 0;
		// ʹ��һ����������buffer�����ݶ�ȡ����
		while ((len = inStream.read(buffer)) != -1) {
			// ���������buffer��д����ݣ��м��������ĸ�λ�ÿ�ʼ����len����ȡ�ĳ���
			outStream.write(buffer, 0, len);
		}
		// �ر�������
		inStream.close();
		// ��outStream������д���ڴ�
		return outStream.toByteArray();
	}

	/**
	 * MD5 ����
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
			// System.out.println("result: " + buf.toString());// 32λ�ļ���
			// System.out.println("result: " + buf.toString().substring(8,
			// 24));// 16λ�ļ���
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

}
