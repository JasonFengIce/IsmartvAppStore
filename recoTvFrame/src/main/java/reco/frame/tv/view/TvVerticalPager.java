package reco.frame.tv.view;

import reco.frame.tv.R;
import reco.frame.tv.view.component.TvUtil;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class TvVerticalPager extends ViewGroup {

	private final String TAG = "VerticalPager";
	private final int ACTION_START_SCROLL = 0;
	private boolean initFlag;
	private final int DELAY = 231, DURATION = 570;
	private Scroller mScroller;
	public final static int PAGE_DESK = 1, PAGE_TOOL = 0, PAGE_PUSH = 2;
	private OnPageChangeListener OnPageChangeListener;
	public int curPage, initPage;
	private int pageHeight;
	private int maxPage;
	private SparseArray<Integer> itemIds;
	private PagerAdapter adapter;
	/**
	 * 外层容器布局是否改变
	 */
	private boolean parentLayout = true;

	private Handler handler;

	public TvVerticalPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TvVerticalPager(Context context) {
		super(context);
		init(context);
	}

	public TvVerticalPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		handler = new Handler();
		itemIds = new SparseArray<Integer>();
		mScroller = new Scroller(context);
		this.initFlag = true;
		this.initPage = 0;
		this.curPage = 0;
		this.pageHeight = (int) getResources().getDimension(R.dimen.px630);

	}

	public void setAdaper(PagerAdapter adapter) {
		this.adapter = adapter;
		initLayout();
	}

	private void initLayout() {

		for (int i = 0; i < adapter.getCount(); i++) {
			ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			View page = (View) adapter.instantiateItem(this, i);
			this.addView(page, vlp);
			int viewId = page.getId();
			if (viewId == -1) {
				viewId = TvUtil.buildId();
			}
			page.setId(viewId);
			itemIds.put(viewId, i);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int count = getChildCount();
		this.maxPage = count;
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(width, height);
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		this.pageHeight = b;
		int totalHeight = -initPage * b;
		int count = getChildCount();

		for (int i = 0; i < count; i++) {
			View childView = getChildAt(i);
			childView.layout(l, totalHeight, r, totalHeight + b);
			totalHeight += b;
		}

		// if (!bindAlready) {
		// bindEvent();
		// }

	}

	private boolean bindAlready = false;

	private void bindEvent() {

		if (getChildCount() < 1) {
			return;
		}
		bindAlready = true;
		for (int i = 0; i < getChildCount(); i++) {
			ViewGroup page = (ViewGroup) getChildAt(i);
			for (int j = 0; j < page.getChildCount(); j++) {
				View child = page.getChildAt(j);
				int viewId = child.getId();
				if (viewId == -1) {
					viewId = TvUtil.buildId();
					// 此处硬设置id同时建议开发者不用此范围id
				}
				child.setId(viewId);
				itemIds.put(viewId, i + 1);
			}

		}

	}

	public void setInitPage(int page) {
		this.initPage = page;
		this.curPage = page;
	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		// Log.e(TAG, "curPage=" + curPage + "---" + scollFlag);
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (!mScroller.isFinished()) {
				return true;
			}
			boolean flag = false;
			View focus = findFocus();
			int focusIndex = itemIds.get(((View) focus.getParent()).getId());
			int direction = 0;
			View next = null;
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				direction = View.FOCUS_DOWN;
				if (focusIndex < itemIds.size() - 1) {
					next = findViewById(itemIds.keyAt(focusIndex + 1));
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				direction = View.FOCUS_RIGHT;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				direction = View.FOCUS_UP;
				if (focusIndex > 0) {
					next = findViewById(itemIds.keyAt(focusIndex - 1));
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				direction = View.FOCUS_LEFT;
				break;
			}
			if (next != null && direction != 0) {
				Integer temp = itemIds.get(next.getId());
				// 焦点切出容器时
				if (temp != null) {
					// curPage = temp;
				} else {
					parentLayout = true;
					return super.dispatchKeyEventPreIme(event);
				}

				final int nextIndex = temp;

				if (nextIndex != focusIndex) {
					if (nextIndex > -1 && nextIndex < maxPage) {
						// 先清除按钮动画

						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								setCurrentPage(nextIndex);

							}
						}, DELAY);
					} else {
						return true;
					}
				}
			}
		}

		return super.dispatchKeyEventPreIme(event);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		if (t > oldt) {
			if (OnPageChangeListener != null) {
				this.OnPageChangeListener.onPageChange(curPage - 1, curPage);
			}

		} else if (t < oldt) {
			if (OnPageChangeListener != null) {
				this.OnPageChangeListener.onPageChange(curPage + 1, curPage);
			}

		}
		super.onScrollChanged(l, t, oldl, oldt);
	}

	/**
	 * 跳转至指定页面
	 * 
	 * @param page
	 */
	public void setCurrentPage(int page) {

		if (page < 0 || page > maxPage - 1) {
			return;
		}
		if (page == curPage) {
			return;
		}
		if (page < curPage) {
			int distance = (page - curPage) * pageHeight;
			mScroller.startScroll(0, mScroller.getFinalY(), 0, distance,
					DURATION);
		} else {
			int distance = (page - curPage) * pageHeight;
			mScroller.startScroll(0, mScroller.getFinalY(), 0, distance,
					DURATION);
		}

		curPage = page;
		invalidate();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// if (mVelocityTracker == null) {
		// mVelocityTracker = VelocityTracker.obtain();
		// }
		// mVelocityTracker.addMovement(event);
		//
		// int action = event.getAction();
		//
		// float y = event.getY();
		//
		// switch (action) {
		// case MotionEvent.ACTION_DOWN:
		// if (!mScroller.isFinished()) {
		// mScroller.abortAnimation();
		// }
		// mLastMotionY = (int) y;
		//
		// break;
		// case MotionEvent.ACTION_MOVE:
		// int deltaY = (int) (mLastMotionY - y);
		// scrollBy(0, deltaY);
		// invalidate();
		//
		// mLastMotionY = (int) y;
		// break;
		// case MotionEvent.ACTION_UP:
		// if (mVelocityTracker != null) {
		// mVelocityTracker.recycle();
		// mVelocityTracker = null;
		// }
		// // Log.e("montion", "" +
		// // getScrollY()+"==="+getHeight()+"==="+mLastMotionY);
		// if (getScrollY() < 0) {
		// mScroller.startScroll(0, -mLastMotionY, 0, -getScrollY());
		// } else if (getScrollY() > (getHeight() * (getChildCount() - 1))) {
		// View lastView = getChildAt(getChildCount() - 1);
		// mScroller.startScroll(0, lastView.getTop() + getHeight(), 0,
		// -300);
		// } else {
		// int position = getScrollY() / getHeight();
		// int mod = getScrollY() % getHeight();
		//
		// if (mod > getHeight() / 3) {
		// View positionView = getChildAt(position + 1);
		// mScroller.startScroll(0, positionView.getTop() - 300, 0,
		// +300);
		// } else {
		// View positionView = getChildAt(position);
		// mScroller.startScroll(0, positionView.getTop() + 300, 0,
		// -300);
		// }
		//
		// }
		// invalidate();
		// break;
		// }

		return true;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();

		// 先判断mScroller滚动是否完成
		if (mScroller.computeScrollOffset()) {

			// 这里调用View的scrollTo()完成实际的滚动
			scrollTo(0, mScroller.getCurrY());
			// 必须调用该方法，否则不一定能看到滚动效果
			postInvalidate();
		}
		super.computeScroll();
	}

	public void setPageChangeListener(OnPageChangeListener myListener) {
		this.OnPageChangeListener = myListener;
	}

	public interface OnPageChangeListener {
		public void onPageChange(int pageBefore, int pageCurrent);
	}

}