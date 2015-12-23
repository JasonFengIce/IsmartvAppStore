package reco.frame.tv.view;

import reco.frame.tv.R;
import reco.frame.tv.TvBitmap;
import reco.frame.tv.view.component.TvUtil;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 专用于电视布局的RelativeLayout 子项获得焦点时 自动获得选中框效果 建议作为最外层容器使用
 * 
 * @author keYence
 * 
 */
public class TvRelativeLayoutAsScroll extends RelativeLayout {
	/**
	 * 光标
	 */
	private View cursor;
	private final String cursorTag = "TvRelativeLayoutAsScroll";
	/**
	 * 光标资源
	 */
	private int cursorRes;
	/**
	 * 可否缩放
	 */
	private boolean scalable;
	/**
	 * 放大比率
	 */
	private float scale;
	/**
	 * 光标飘移动画 默认无效果
	 */
	private int animationType;
	public final static int ANIM_DEFAULT = 0;// 无效果
	public final static int ANIM_TRASLATE = 1;// 平移
	/**
	 * 放大用时
	 */
	private int durationLarge = 100;
	/**
	 * 缩小用时
	 */
	private int durationSmall = 100;
	/**
	 * 滑动用时
	 */
	private int durationTranslate;
	/**
	 * 触发延迟
	 */
	private int delay = 110;
	/**
	 * 光标边框宽度 包括阴影
	 */
	private int boarder;
	/**
	 * 光标左边框宽度 含阴影
	 */
	private int boarderLeft;
	/**
	 * 光标顶边框宽度 含阴影
	 */
	private int boarderTop;
	/**
	 * 光标右边框宽度 含阴影
	 */
	private int boarderRight;
	/**
	 * 光标底边框宽度 含阴影
	 */
	private int boarderBottom;
	/**
	 * 滚动延迟
	 */
	private int scrollDelay = 0;
	/**
	 * 滚动速度
	 */
	private int scrollDuration = 0;

	private int paddingLeft, paddingTop;
	/**
	 * 与屏幕边缘距离
	 */
	private int marginHori=1000;

	private boolean initFlag = true;
	/**
	 * 焦点离开容器
	 */
	private boolean focusIsOut;

	/**
	 * 屏幕宽度
	 */
	private int screenWidth;

	/**
	 * 是否初始化焦点
	 */
	private boolean initFocus = true;

	public boolean isInitFocus() {
		return initFocus;
	}

	public void setInitFocus(boolean initFocus) {
		this.initFocus = initFocus;
	}

	private AnimatorSet animatorSet;
	private ObjectAnimator largeX;
	private Scroller mScroller;
	private Handler handler = new Handler();

	private OnChildSelectListener onChildSelectListener;
	private OnChildClickListener onChildClickListener;

	public int getCursorRes() {
		return cursorRes;
	}

	public void setCursorRes(int cursorRes) {
		this.cursorRes = cursorRes;
	}

	public void setCursorResMultiDisplay(int cursorRes_1280,
			int cursorRes_1920, int cursorRes_2560, int cursorRes_3840) {

		switch (getResources().getDisplayMetrics().widthPixels) {
		case TvUtil.SCREEN_1280:
			cursorRes = cursorRes_1280;
			break;

		case TvUtil.SCREEN_1920:
			cursorRes = cursorRes_1920;
			break;
		case TvUtil.SCREEN_2560:
			cursorRes = cursorRes_2560;
			break;
		case TvUtil.SCREEN_3840:
			cursorRes = cursorRes_3840;
			break;
		}
	}

	public boolean isScalable() {
		return scalable;
	}

	public void setScalable(boolean scalable) {
		this.scalable = scalable;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void setBoarder(int boarderLeft, int boarderTop, int boarderRight,
			int boarderBottom) {
		this.boarderLeft = boarderLeft;
		this.boarderTop = boarderTop;
		this.boarderRight = boarderRight;
		this.boarderBottom = boarderBottom;
	}

	public int getBoarderLeft() {
		return boarderLeft;
	}

	public int getBoarderTop() {
		return boarderTop;
	}

	public int getBoarderRight() {
		return boarderRight;
	}

	public int getBoarderBottom() {
		return boarderBottom;
	}

	public TvRelativeLayoutAsScroll(Context context) {
		this(context, null);
	}

	public TvRelativeLayoutAsScroll(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TvRelativeLayoutAsScroll(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		TypedArray custom = getContext().obtainStyledAttributes(attrs,
				R.styleable.TvRelativeLayoutAsScroll);
		this.cursorRes = custom.getResourceId(
				R.styleable.TvRelativeLayoutAsScroll_cursorRes, 0);
		this.scalable = custom.getBoolean(
				R.styleable.TvRelativeLayoutAsScroll_scalable, true);
		this.scale = custom.getFloat(R.styleable.TvRelativeLayoutAsScroll_scale,
				1.1f);
		this.animationType = custom.getInt(
				R.styleable.TvRelativeLayoutAsScroll_animationType, 0);
		this.delay = custom.getInteger(
				R.styleable.TvRelativeLayoutAsScroll_delay, 10);
		this.durationLarge = custom.getInteger(
				R.styleable.TvRelativeLayoutAsScroll_durationLarge, 100);
		this.durationSmall = custom.getInteger(
				R.styleable.TvRelativeLayoutAsScroll_durationSmall, 100);
		this.durationTranslate = custom.getInteger(
				R.styleable.TvRelativeLayoutAsScroll_durationTranslate, 9);
		
		
		this.scrollDelay = custom.getInteger(R.styleable.TvRelativeLayoutAsScroll_scrollDelay,
				171);
		this.scrollDuration = custom.getInteger(
				R.styleable.TvGallery_scrollDuration, 371);
		
		this.boarder = (int) custom.getDimension(
				R.styleable.TvRelativeLayoutAsScroll_boarder, 0)
				+ custom.getInteger(
						R.styleable.TvRelativeLayoutAsScroll_boarderInt, 0);

		if (boarder == 0) {
			this.boarderLeft = (int) custom.getDimension(
					R.styleable.TvRelativeLayoutAsScroll_boarderLeft, 0)
					+ custom.getInteger(
							R.styleable.TvRelativeLayoutAsScroll_boarderLeftInt,
							0);
			this.boarderTop = (int) custom.getDimension(
					R.styleable.TvRelativeLayoutAsScroll_boarderTop, 0)
					+ custom.getInteger(
							R.styleable.TvRelativeLayoutAsScroll_boarderTopInt,
							0);
			this.boarderRight = (int) custom.getDimension(
					R.styleable.TvRelativeLayoutAsScroll_boarderRight, 0)
					+ custom.getInteger(
							R.styleable.TvRelativeLayoutAsScroll_boarderRightInt,
							0);
			this.boarderBottom = (int) custom.getDimension(
					R.styleable.TvRelativeLayoutAsScroll_boarderBottom, 0)
					+ custom.getInteger(
							R.styleable.TvRelativeLayoutAsScroll_boarderBottomInt,
							0);
		} else {
			this.boarderLeft = boarder;
			this.boarderTop = boarder;
			this.boarderRight = boarder;
			this.boarderBottom = boarder;
		}

		if (cursorRes == 0) {
			switch (getResources().getDisplayMetrics().widthPixels) {
			case TvUtil.SCREEN_1280:
				cursorRes = custom.getResourceId(
						R.styleable.TvRelativeLayoutAsScroll_cursorRes_1280, 0);
				break;

			case TvUtil.SCREEN_1920:
				cursorRes = custom.getResourceId(
						R.styleable.TvRelativeLayoutAsScroll_cursorRes_1920, 0);
				break;
			case TvUtil.SCREEN_2560:
				cursorRes = custom.getResourceId(
						R.styleable.TvRelativeLayoutAsScroll_cursorRes_2560, 0);
				break;
			case TvUtil.SCREEN_3840:
				cursorRes = custom.getResourceId(
						R.styleable.TvRelativeLayoutAsScroll_cursorRes_3840, 0);
				break;
			case TvUtil.SCREEN_4096:
				cursorRes = custom.getResourceId(
						R.styleable.TvRelativeLayoutAsScroll_cursorRes_4096, 0);
				break;
			}
		}

		custom.recycle();

		// 关闭子控件动画缓存 使嵌套动画更流畅
		setAnimationCacheEnabled(false);

		init();

	}

	private void init() {
		focusIsOut = true;
		mScroller = new Scroller(getContext());

		screenWidth = getResources().getDisplayMetrics().widthPixels;
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		

		for (int i = 0; i < getChildCount(); i++) {
			View item = getChildAt(i);
			if (item.getTag() != null
					&& item.getTag().toString().equals(cursorTag)) {

				continue;
			}

			measureChild(item, widthMeasureSpec, heightMeasureSpec);

			RelativeLayout.LayoutParams rlp = (LayoutParams) item
					.getLayoutParams();
			rlp.width = item.getMeasuredWidth();
			rlp.height = item.getMeasuredHeight();
			
			int currentLength = item.getLeft() + item.getWidth();
			if (contentLength < currentLength) {
				contentLength = currentLength;
			}
			
			
		}

		super.onMeasure(contentLength+1000, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		if (changed) {
			super.onLayout(changed, l, t, r, b);
		} else {
			int cCount = getChildCount();
			int cWidth = 0;
			int cHeight = 0;
			MarginLayoutParams cParams = null;
			/**
			 * 遍历所有childView根据其宽和高，以及margin进行布局 选中框无需重定位??初始位置
			 */
			for (int i = 0; i < cCount; i++) {
				View item = getChildAt(i);

				if (item.getTag() != null
						&& item.getTag().toString().equals(cursorTag)) {
					continue;
				}

				cWidth = item.getMeasuredWidth();
				cHeight = item.getMeasuredHeight();
				cParams = (MarginLayoutParams) item.getLayoutParams();
				int cl = 0, ct = 0, cr = 0, cb = 0;

				cl = item.getLeft();
				ct = item.getTop();
				cr = cl + cWidth;
				cb = cHeight + ct;
				item.layout(cl, ct, cr, cb);
				
				if (marginHori>item.getLeft()) {
					marginHori=item.getLeft();
				}
			}
		}

	}

	private int contentLength;

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		
//		if (!mScroller.isFinished()) {
//			return true;
//		}
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			int direction = 0;
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				direction = View.FOCUS_DOWN;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				direction = View.FOCUS_RIGHT;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				direction = View.FOCUS_UP;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				direction = View.FOCUS_LEFT;
				break;
			}
			View focus = findFocus();

			if (focus != null && direction != 0) {

				View next = focus.focusSearch(direction);

				if (next != null) {
					focusIsOut = false;
				} else {
					focusIsOut = true;
				}

				if (next != null) {

					boolean flag = false;

					// 计算中心坐标
					int focusCenter = focus.getLeft() + focus.getWidth() / 2
							+ focus.getWidth() % 2;
					int nextCenter = next.getLeft() + next.getWidth() / 2
							+ next.getWidth() % 2;
					

					
					int shifting = 0;
					
					
					// 计算焦点切换距离
					// 如果到达边缘,则平移距离以边界复位效果来计算
					//通过滚动条判断作边缘判断
					int maxScroll=contentLength-screenWidth+marginHori;

					if (contentLength - nextCenter > screenWidth / 2
							&& nextCenter > screenWidth / 2) {
						shifting = nextCenter
								- (mScroller.getFinalX() + screenWidth / 2);
					} else {

						if (nextCenter > focusCenter&&nextCenter > screenWidth / 2) {
							if (next.getLeft() + mScroller.getFinalX() < contentLength) {
								shifting=maxScroll-mScroller.getFinalX();
								
							}
						} else {
							if (mScroller.getFinalX() > marginHori&&contentLength - nextCenter > screenWidth / 2) {
								shifting=mScroller.getFinalX()*-1;
							}
						}

					}
					
//					Log.e(VIEW_LOG_TAG,
//							 mScroller.getFinalX()+"---"+next.getLeft() + "---"
//									+ nextCenter + "---" + contentLength+"---"+shifting);
					

					scroll(shifting);

				}

			}

		}

		return super.dispatchKeyEventPreIme(event);
	}

	@Override
	protected void onAttachedToWindow() {
		bindEvent();
		super.onAttachedToWindow();
	}

	// 初始化焦点
	private void bindEvent() {

		if (getChildCount() < 1) {
			return;
		}
		initFlag = false;
		View child = null;
		for (int i = 0; i < getChildCount(); i++) {

			child = getChildAt(i);

			if (child != null) {

				child.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(final View child, boolean focus) {
						if (focus) {

							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									moveCover(child);
								}
							}, delay);
							// 选中事件
							if (onChildSelectListener != null) {
								onChildSelectListener.onChildSelect(child);
							}

						} else {
							returnCover(child);
						}
					}
				});
				if (onChildClickListener != null) {
					child.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View child) {
							onChildClickListener.onChildClick(child);

						}
					});
				}

			}
		}

		// 初始化焦点
		final View focus = findFocus();
		if (focus != null && initFocus) {

			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					focus.requestFocus();
					moveCover(focus);
				}
			}, 300);

		}

	}

	/**
	 * 光标移动 到达后 与控件同时放大
	 */
	private void moveCover(View item) {
		if (cursor == null) {
			cursor = new View(getContext());
			cursor.setTag(cursorTag);
			cursor.setBackgroundResource(cursorRes);
			this.addView(cursor);
		}
		setBorderParams(item);

	}

	/**
	 * 还原控件状态
	 */

	private void returnCover(View item) {
		if (cursor == null) {
			return;
		}
		cursor.setVisibility(View.INVISIBLE);
		if (scalable) {
			scaleToNormal(item);
		}
	}

	private void scaleToLarge(View item) {

		if (!item.isFocused()) {
			return;
		}

		animatorSet = new AnimatorSet();
		largeX = ObjectAnimator.ofFloat(item, "ScaleX", 1f, scale);
		ObjectAnimator largeY = ObjectAnimator.ofFloat(item, "ScaleY", 1f,
				scale);
		ObjectAnimator cursorX = ObjectAnimator.ofFloat(cursor, "ScaleX", 1f,
				scale);
		ObjectAnimator cursorY = ObjectAnimator.ofFloat(cursor, "ScaleY", 1f,
				scale);

		animatorSet.setDuration(durationLarge);
		animatorSet.play(largeX).with(largeY).with(cursorX).with(cursorY);

		animatorSet.start();
	}

	private void scaleToNormal(View item) {
		if (animatorSet == null) {
			return;
		}
		if (animatorSet.isRunning()) {
			animatorSet.cancel();
		}
		ObjectAnimator oa = ObjectAnimator.ofFloat(item, "ScaleX", 1f);
		oa.setDuration(durationSmall);
		oa.start();
		ObjectAnimator oa2 = ObjectAnimator.ofFloat(item, "ScaleY", 1f);
		oa2.setDuration(durationSmall);
		oa2.start();
	}

	/**
	 * 配置网络图片地址
	 * 
	 * @param url
	 */
	public void configImageUrl(String url) {

		TvBitmap.create(getContext()).display(this, url);

	}

	/**
	 * 滚动
	 * 
	 * @param page
	 */
	private void scroll(int shifting) {
		if (shifting == 0) {
			return;
		}

		mScroller.startScroll(mScroller.getFinalX(), 0, shifting, 0, scrollDuration);

		invalidate();

	}

	@Override
	public void computeScroll() {
		super.computeScroll();

		// 先判断mScroller滚动是否完成
		if (mScroller.computeScrollOffset()) {

			// 这里调用View的scrollTo()完成实际的滚动
			scrollTo(mScroller.getCurrX(), 0);
			// 必须调用该方法，否则不一定能看到滚动效果
			postInvalidate();
		}
		super.computeScroll();
	}

	/**
	 * 指定光标相对位置
	 */
	private void setBorderParams(View item) {
		cursor.clearAnimation();
		cursor.setVisibility(View.VISIBLE);

		// 判断类型
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item
				.getLayoutParams();
		final int l = item.getLeft() - boarderLeft;
		final int t = item.getTop() - boarderTop;
		final int r = item.getLeft() + params.width + boarderRight;
		final int b = item.getTop() + params.height + boarderBottom;

		switch (animationType) {
		case ANIM_DEFAULT:
			cursor.layout(l, t, r, b);
			item.bringToFront();
			cursor.bringToFront();
			if (scalable) {
				scaleToLarge(item);
			}
			break;
		case ANIM_TRASLATE:
			if (cursor.getLeft() <= 0) {
				cursor.layout(l, t, r, b);
			}

			item.bringToFront();
			cursor.bringToFront();

			// if (focusIsOut) {
			// return;
			// }

			// Log.e(VIEW_LOG_TAG, l + "--" + t + "--" + r + "--" + b);
			TvAnimator animator = new TvAnimator(cursor, item);
			animator.setTargetParams(l, t, r - l, b - t);
			animator.execute();

			break;

		}

	}

	class TvAnimator extends AsyncTask<Void, Integer, Integer> {
		private View target, item;
		private int l, t, cl, ct;
		private int width, cwidth, height, cheight;
		private int dX, dY, dW, dH;
		private int frequence = 17;

		TvAnimator(View target, View item) {
			this.target = target;
			this.item = item;
		}

		public void setTargetParams(int l, int t, int width, int height) {
			this.l = l;
			this.t = t;
			this.width = width;
			this.height = height;
			this.cl = target.getLeft();
			this.ct = target.getTop();
			this.cwidth = target.getWidth();
			this.cheight = target.getHeight();
		}

		/** 
         * 
         */
		@Override
		protected void onPreExecute() {

			dW = (int) (width - cwidth > 0 ? Math.ceil((width - cwidth)
					/ (frequence * 1.0)) : Math.floor((width - cwidth)
					/ (frequence * 1.0)));
			dH = (int) (height - cheight > 0 ? Math.ceil((height - cheight)
					/ (frequence * 1.0)) : Math.floor((height - cheight)
					/ (frequence * 1.0)));
			dX = (int) (l - cl > 0 ? Math.ceil((l - cl) / (frequence * 1.0))
					: Math.floor((l - cl) / (frequence * 1.0)));
			dY = (int) (t - ct >= 0 ? Math.ceil((t - ct) / (frequence * 1.0))
					: Math.floor((t - ct) / (frequence * 1.0)));

			// Log.e(VIEW_LOG_TAG, cl + "---" + l + "---" + ct + "---" + t
			// + "====" + dX + ":" + dY + ":" + dW + ":" + dH + "---"
			// +durationTranslate);
		}

		/** 
         * 
         */
		@Override
		protected Integer doInBackground(Void... params) {

			while (cl != l || t != ct || cwidth != width || cheight != height) {

				if (Math.abs(cl - l) <= Math.abs(dX)) {
					cl = l;
				} else {
					cl += dX;
				}

				if (Math.abs(ct - t) <= Math.abs(dY)) {
					ct = t;
				} else {
					ct += dY;
				}

				if (Math.abs(width - cwidth) <= Math.abs(dW)) {
					cwidth = width;
					dW = 0;
				} else {
					cwidth += dW;
				}

				if (Math.abs(height - cheight) <= Math.abs(dH)) {
					cheight = height;
					dH = 0;
				} else {
					cheight += dH;
				}

				// Log.e(VIEW_LOG_TAG, cl+"=="+ct+"=="+(cl + cwidth +
				// dW)+"=="+(ct + cheight + dH));

				publishProgress(cl, ct, cl + cwidth + dW, ct + cheight + dH);
				try {
					Thread.sleep(durationTranslate);
				} catch (InterruptedException e) {
				}
			}

			return null;
		}

		/** 
         * 
         */
		@Override
		protected void onPostExecute(Integer integer) {
			if (scalable) {
				if (!item.isFocused()) {
					return;
				}

				animatorSet = new AnimatorSet();
				ValueAnimator largeX = ObjectAnimator.ofFloat(item, "ScaleX",
						1f, scale);
				ValueAnimator largeY = ObjectAnimator.ofFloat(item, "ScaleY",
						1f, scale);

				animatorSet.setDuration(durationLarge);
				animatorSet.setInterpolator(new DecelerateInterpolator(3.0f));
				animatorSet.play(largeX).with(largeY);
				animatorSet.start();
				// 中心放大 左上减 右下加
				ValueAnimator animation = ValueAnimator.ofFloat(1f, scale);
				animation.setDuration(durationLarge);
				animation.setInterpolator(new DecelerateInterpolator(3.0f));
				animation.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						// Log.i("update", ((Float)
						// animation.getAnimatedValue())
						// .toString());
						float curScale = (Float) animation.getAnimatedValue();

						int dw = (int) (width * (curScale - 1) / 2);
						int dh = (int) (height * (curScale - 1) / 2);

						target.layout(l - dw, t - dh, l + width + dw, t
								+ height + dh);
					}
				});
				animation.start();

			}

		}

		/** 
         * 
         */
		@Override
		protected void onProgressUpdate(Integer... values) {
			target.layout(values[0], values[1], values[2], values[3]);
		}
	}

	public void setOnChildSelectListener(OnChildSelectListener myListener) {
		this.onChildSelectListener = myListener;
	}

	public void setOnChildClickListener(OnChildClickListener myListener) {
		this.onChildClickListener = myListener;
	}

	public interface OnChildSelectListener {
		public void onChildSelect(View child);
	}

	public interface OnChildClickListener {
		public void onChildClick(View child);
	}

}
