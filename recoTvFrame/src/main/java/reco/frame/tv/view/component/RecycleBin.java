package reco.frame.tv.view.component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.renderscript.Int2;
import android.support.v4.util.LruCache;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class RecycleBin {
	private final static String TAG = "RecycleBin";
	public final static int STATE_ACTIVE = 0, STATE_SCRAP = 1;


	// 线程池
	private static ExecutorService sExecutorService;
	// 通知UI线程图片获取ok时使用
	private Handler handler;
	// 存储处于回收状态的item ID 及其中图片控件ID
	// private static SparseIntArray recycleIds;
	// 状态记录
	private static SparseIntArray itemStates;
	// 存储处于回收状态的item ID 及其中图片控件ID
	private Map<Integer, HashSet<Integer>> recycleIds;
	private Map<Integer,View> activeViews;
	private Map<Integer,View> scrapViews;

	private final static int STATE_RECYCLING = 1, STATE_RELOADING = 2;

	public RecycleBin() {
		handler = new Handler();
		// recycleIds = new SparseIntArray();
		itemStates = new SparseIntArray();
		startThreadPoolIfNecessary();

		recycleIds = new HashMap<Integer, HashSet<Integer>>();
		activeViews=new HashMap<Integer, View>();
		scrapViews=new HashMap<Integer, View>();

	}


	/** 开启线程池 */
	public static void startThreadPoolIfNecessary() {
		if (sExecutorService == null || sExecutorService.isShutdown()
				|| sExecutorService.isTerminated()) {
			sExecutorService = Executors.newFixedThreadPool(3);
		}
	}

	public void recycleChild(HashSet<Integer> idSet,
			final View scrapView, int itemId) {

		// 销毁图片
		if (scrapView instanceof ImageView) {
			((ImageView) scrapView).setImageResource(0);
			// if (front != null && front instanceof BitmapDrawable) {
			// BitmapDrawable bitmapDrawable = (BitmapDrawable) front;
			// Bitmap bitmap = bitmapDrawable.getBitmap();
			// if (bitmap != null && !bitmap.isRecycled()) {
			// recycleBitmap(bitmap);
			// }
			// }

		}

		Drawable background = scrapView.getBackground();
		if (background != null) {

			BitmapDrawable bitmapDrawable = null;

			if (background instanceof TransitionDrawable) {
				background = ((TransitionDrawable) background).getDrawable(1);
			}
			bitmapDrawable = (BitmapDrawable) background;
			final Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap != null && !bitmap.isRecycled()) {
				// 保存成功则将childId添加集合中
				idSet.add(scrapView.getId());
				handler.post(new Runnable() {

					@Override
					public void run() {
						scrapView.setBackgroundResource(0);

					}
				});
			}
			

		}

	}

	public void recycleView(final View item) {
		
		if (scrapViews.size()>10) {
			
		}
		scrapViews.put(item.getId(), item);

		sExecutorService.submit(new Runnable() {

			@Override
			public void run() {
				if (itemStates.get(item.getId(), -1) != -1) {
					return;
				}

				itemStates.put(item.getId(), STATE_RECYCLING);
				HashSet<Integer> set = new HashSet<Integer>();
				if (item instanceof ViewGroup) {
					ViewGroup container = (ViewGroup) item;

					for (int i = 0; i < container.getChildCount(); i++) {
						View child = container.getChildAt(i);
						recycleChild(set, child, item.getId());
					}
					if (!set.isEmpty()) {
						recycleIds.put(item.getId(), set);
					}
				}

			}
		});

	}

	public void reloadView(int position) {

		// 自缓存获取图片
		View child=scrapViews.get(position);
		
		if (child!=null) {
			
		}

	}



}