package reco.frame.tv.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import reco.frame.tv.R;
import reco.frame.tv.view.component.RecycleBin;
import reco.frame.tv.view.component.TvBaseAdapter;
import reco.frame.tv.view.component.TvUtil;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class TvGridView extends RelativeLayout {

	/**
	 * 光标
	 */
	private ImageView cursor;
	/**
	 * 光标id
	 */
	private int cursorId;
	/**
	 * 光标图片
	 */
	private int cursorRes;
	/**
	 * item可否缩放
	 */
	private boolean scalable;
	/**
	 * 放大比率
	 */
	private float scale;
	/**
	 * 光标飘移动画 默认无效果(尚未实现)
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
	 * 平移用时
	 */
	private int durationTraslate = 100;
	/**
	 * 放大延迟
	 */
	private int delay = 0;
	/**
	 * 滚动模式
	 */
	private int scrollMode;
	/**
	 * 滚动判断 默认离边级一行时刷新视图
	 */
	private int scrollEdge;
	/**
	 * 滚动速度
	 */
	private int scrollDelay = 0;
	/**
	 * 滚动速度
	 */
	private int scrollDuration = 0;
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
	 * 外层容器布局是否改变
	 */
	private boolean parentLayout = true;

	/**
	 * 是否默选认中首项
	 */
	private boolean initFocus;

	/**
	 * 可否滚动
	 */
	private final int ACTION_INIT_ITEMS = 1;

	/**
	 * 刷新延迟
	 */
	private final int DELAY = 371;
	/**
	 * 列数
	 */
	private int columns;
	/**
	 * 当前选中行
	 */
	private int selectRow;
	/**
	 * 屏幕可显示最大行数
	 */
	private int screenMaxCount;
	/**
	 * 当前选中子项下示
	 */
	private int selectIndex;
	private int paddingLeft, paddingTop;
	private int spaceHori;
	private int spaceVert;
	/**
	 * item宽高 不包括纵横间距
	 */
	private int itemWidth, itemHeight;
	private SparseArray<Integer> itemIds;

	private OnItemSelectListener onItemSelectListener;
	private OnItemClickListener onItemClickListener;
	public AdapterDataSetObservable mDataSetObservable;
	private TvBaseAdapter adapter;
	private AnimatorSet animatorSet;
	private ObjectAnimator largeX;
	private WindowManager wm;
	private Scroller mScroller;
	private RecycleBin mRecycleBin;
	private boolean isInit = true;

	/**
	 * 焦点移出容器
	 */
	private boolean focusIsOut;
	/**
	 * 以1280为准,其余分辨率放大比率 用于适配
	 */
	private float screenScale = 1;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {

			case ACTION_INIT_ITEMS:
				initItems();
				break;
			}

		};
	};

	public TvGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TvGridView(Context context) {
		super(context);
	}

	public TvGridView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray custom = getContext().obtainStyledAttributes(attrs,
				R.styleable.TvGridView);
		this.cursorRes = custom.getResourceId(R.styleable.TvGridView_cursorRes,
				0);
		this.scalable = custom
				.getBoolean(R.styleable.TvGridView_scalable, true);
		this.initFocus = custom.getBoolean(R.styleable.TvGridView_initFocus,
				true);

		this.scale = custom.getFloat(R.styleable.TvGridView_scale, 1.1f);
		this.animationType = custom.getInt(
				R.styleable.TvGridView_animationType, 0);
		this.delay = custom.getInteger(R.styleable.TvGridView_delay, 110);
		this.scrollMode = custom.getInt(R.styleable.TvGridView_scrollMode,
				TvView.SCROLL_FILL);
		this.scrollDelay = custom.getInteger(
				R.styleable.TvGridView_scrollDelay, 171);
		this.scrollDuration = custom.getInteger(
				R.styleable.TvGridView_scrollDuration, 371);
		this.durationLarge = custom.getInteger(
				R.styleable.TvGridView_durationLarge, 230);
		this.durationSmall = custom.getInteger(
				R.styleable.TvGridView_durationSmall, 230);
		this.durationTraslate = custom.getInteger(
				R.styleable.TvGridView_durationTranslate, 230);

		this.columns = custom.getInteger(R.styleable.TvGridView_columns, 2);
		this.spaceHori = (int) custom.getDimension(
				R.styleable.TvGridView_spaceHori, 10);
		this.spaceVert = (int) custom.getDimension(
				R.styleable.TvGridView_spaceVert, 10);

		itemWidth = (int) custom.getDimension(R.styleable.TvGridView_itemWidth,
				10);
		itemHeight = (int) custom.getDimension(
				R.styleable.TvGridView_itemHeight, 10);

		paddingLeft = (int) custom.getDimension(
				R.styleable.TvGridView_paddingLeft, 0);
		paddingTop = (int) custom.getDimension(
				R.styleable.TvGridView_paddingTop, 0);

		this.boarder = (int) custom.getDimension(
				R.styleable.TvGridView_boarder, 0)
				+ custom.getInteger(R.styleable.TvGridView_boarderInt, 0);

		if (boarder == 0) {
			this.boarderLeft = (int) custom.getDimension(
					R.styleable.TvGridView_boarderLeft, 0)
					+ custom.getInteger(R.styleable.TvGridView_boarderLeftInt,
							0);
			this.boarderTop = (int) custom.getDimension(
					R.styleable.TvGridView_boarderTop, 0)
					+ custom.getInteger(R.styleable.TvGridView_boarderTopInt, 0);
			this.boarderRight = (int) custom.getDimension(
					R.styleable.TvGridView_boarderRight, 0)
					+ custom.getInteger(R.styleable.TvGridView_boarderRightInt,
							0);
			this.boarderBottom = (int) custom.getDimension(
					R.styleable.TvGridView_boarderBottom, 0)
					+ custom.getInteger(
							R.styleable.TvGridView_boarderBottomInt, 0);
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
						R.styleable.TvGridView_cursorRes_1280, 0);
				break;

			case TvUtil.SCREEN_1920:
				cursorRes = custom.getResourceId(
						R.styleable.TvGridView_cursorRes_1920, 0);
				break;
			case TvUtil.SCREEN_2560:
				cursorRes = custom.getResourceId(
						R.styleable.TvGridView_cursorRes_2560, 0);
				break;
			case TvUtil.SCREEN_3840:
				cursorRes = custom.getResourceId(
						R.styleable.TvGridView_cursorRes_3840, 0);
				break;

			case TvUtil.SCREEN_4096:
				cursorRes = custom.getResourceId(
						R.styleable.TvGridView_cursorRes_4096, 0);
				break;
			}
		}
		custom.recycle();
		// 关闭子控件动画缓存 使嵌套动画更流畅
		// setAnimationCacheEnabled(false);

		init();
	}

	private void init() {

		itemIds = new SparseArray<Integer>();
		mScroller = new Scroller(getContext());

		wm = (WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE);

		mDataSetObservable = new AdapterDataSetObservable();

		mRecycleBin = new RecycleBin();
	}

	/**
	 * 设置适配器
	 * 
	 * @param adapter
	 */
	public void setAdapter(TvBaseAdapter adapter) {
		this.adapter = adapter;
		if (adapter != null) {
			adapter.registerDataSetObservable(mDataSetObservable);
		}
		// 清理原先数据
		clear();
		if (isInit) {
			initGridView();
			isInit = false;
		}

		Message msg = handler.obtainMessage();
		msg.what = ACTION_INIT_ITEMS;
		handler.sendMessageDelayed(msg, DELAY);
	}

	private void clear() {
		itemIds.clear();
		this.removeAllViews();
		this.clearDisappearingChildren();
		this.destroyDrawingCache();
		focusIsOut = true;
		mScroller.setFinalY(0);
		parentLayout = false;
	}

	/**
	 * 首次加载屏幕可见行数*2
	 */
	public void initGridView() {
		if (getParent() instanceof RelativeLayout) {

			// 重设参数
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
					params.width, params.height);
			this.setPadding((int) (boarderLeft * scale),
					(int) (boarderTop * scale), (int) (boarderRight * scale),
					boarderBottom);
			this.setClipChildren(false);
			this.setClipToPadding(false);
			newParams.setMargins(params.leftMargin, params.topMargin,
					params.rightMargin, params.bottomMargin);
			this.setLayoutParams(newParams);

		} else if (getParent() instanceof LinearLayout) {

			// 重设参数
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
			LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
					params.width, params.height);
			this.setPadding((int) (boarderLeft * scale),
					(int) (boarderTop * scale), (int) (boarderRight * scale),
					boarderBottom);
			this.setClipChildren(false);
			this.setClipToPadding(false);
			newParams.setMargins(params.leftMargin, params.topMargin,
					params.rightMargin, params.bottomMargin);
			this.setLayoutParams(newParams);

		}

		// switch (getResources().getDisplayMetrics().widthPixels) {
		// case TvConfig.SCREEN_1280:
		// screenScale = 1f;
		// break;
		//
		// case TvConfig.SCREEN_1920:
		// screenScale = 1.5f;
		// break;
		// case TvConfig.SCREEN_2560:
		// screenScale = 2f;
		// break;
		// case TvConfig.SCREEN_3840:
		// screenScale = 3f;
		// break;
		// }

		//
		// paddingLeft = (int) (screenScale*boarderLeft + itemWidth
		// * (scale - 1) / 2 );
		// paddingTop = (int) (boarderTop * screenScale + itemHeight * (scale -
		// 1) / 2);

	}

	private void initItems() {
		// 避免冲突
		if (getChildCount() > 0) {
			return;
		}

		int screenHeight = wm.getDefaultDisplay().getHeight();
		int initRows = (screenHeight - itemHeight) % (itemHeight + spaceVert) == 0 ? (screenHeight - itemHeight)
				/ (itemHeight + spaceVert) + 1
				: (screenHeight - itemHeight) / (itemHeight + spaceVert) + 2;

		initLength = Math.min(adapter.getCount() - 1, initRows * 2 * columns
				- 1);

		indexMin = 0;
		indexMax = initLength;
		minRow = 0;
		maxRow = initLength % columns == 0 ? initLength / columns : initLength
				/ columns + 1;
		for (int i = 0; i <= initLength; i++) {
			buildItem(i);
		}

		cursor = new ImageView(getContext());
		cursorId = TvUtil.buildId();
		cursor.setId(cursorId);
		cursor.setBackgroundResource(cursorRes);
		this.addView(cursor);
		cursor.setVisibility(View.INVISIBLE);

		if (initFocus) {
			View focus = ((ViewGroup) getParent()).findFocus();
			if (focus == null) {
				View item = getChildAt(0);
				if (item != null) {
					item.requestFocus();
				}
			}
		}

	}

	/**
	 * layout辅助标记 表面有添加新的子项
	 */
	private boolean layoutFlag = false;
	private boolean isBuilding = false;
	private int initLength;
	private int indexMin, indexMax, minRow, maxRow;

	private void buildList(boolean down) {

		if (isBuilding||adapter.getCount()==initLength) {
			return;
		}
		isBuilding = true;
		int start = 0, end = 0;

		int scrapStart = 0, scrapEnd = 0;

		if (down) {

			scrapStart = indexMin;
			scrapEnd = Math.min(scrapStart + (screenMaxCount + 1) * columns,
					indexMax - (screenMaxCount + 1) * columns + 1);

			scrapEnd = scrapEnd - scrapEnd % columns;

			indexMin = scrapEnd;

			start = scrapEnd == 1 ? indexMax : indexMax + 1;
			end = Math.min(start + (screenMaxCount + 1) * columns - 1,
					adapter.getCount() - 1);
			indexMax = end;

			minRow = scrapEnd % columns == 0 ? scrapEnd / columns : scrapEnd
					/ columns + 1;

			if (start < end) {

				maxRow = end % columns == 0 ? end / columns : end / columns + 1;
			}

		} else {
			scrapEnd = indexMax + 1;

			scrapStart = Math.max(scrapEnd - (screenMaxCount + 1) * columns,
					indexMin + (screenMaxCount + 1) * columns);

			// 确保以整行行式回收添加 判断是否为整行
			scrapStart = scrapStart - scrapStart % columns;
			indexMax = scrapStart - 1;

			start = Math.max(0, indexMin - (screenMaxCount + 1) * columns);
			start = start - start % columns;
			end = Math.max(0, indexMin - 1);
			end = end - (end + 1) % columns;
			indexMin = start;

			minRow = start % columns == 0 ? start / columns : start / columns
					+ 1;

			maxRow = indexMax % columns == 0 ? indexMax / columns : indexMax
					/ columns + 1;
		}

		// Log.e(VIEW_LOG_TAG, scrapStart + "---" + scrapEnd + "---" + start
		// + "---" + end + "---"+minRow+"---"+maxRow+"---" + screenMaxCount);

		for (int i = scrapStart; i < scrapEnd; i++) {
			mRecycleBin.scrapView(i);
		}

		for (int i = start; i <= end; i++) {
			buildItem(i);
		}

		parentLayout = false;
		isBuilding = false;
	}

	/**
	 * 安装view
	 * 
	 * @param position
	 */
	private void buildItem(int position) {
		int left = (position % columns) * (itemWidth + spaceHori);
		int top = (position / columns) * (spaceVert + itemHeight);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				itemWidth, itemHeight);
		if (initLength == 1) {
			rlp.setMargins(left, top, paddingLeft * 2, 0);
		} else {
			rlp.setMargins(left, top, 0, 0);
		}

		View item = mRecycleBin.getScrapView(position);
		// Log.i(VIEW_LOG_TAG, position + "----" + item);
		if (item == null) {

			View contentView = mRecycleBin.retrieveFromScrap();
			if (contentView != null) {
				removeDetachedView(contentView, false);
				itemIds.remove(contentView.getId());
			}

			item = adapter.getView(position, contentView, this);
			this.addView(item, rlp);

			int viewId = item.getId();
			if (viewId == -1) {
				viewId = TvUtil.buildId();
				// 此处硬设置id同时建议开发者不用此范围id
			}
			item.setId(viewId);
			itemIds.put(viewId, position);
			bindEventOnChild(item, position);
		} else {
			//

			if (getChildCount() > position) {
				attachViewToParent(item, position, rlp);
			} else {
				// 尝试官方方式 以scrapView 传为contentView
				// item = adapter.getView(position, null, this);
				//
				Log.i(VIEW_LOG_TAG, getChildCount() + "---" + position);
				itemIds.remove(item.getId());
				this.addView(item, rlp);

				int viewId = item.getId();
				if (viewId == -1) {
					viewId = TvUtil.buildId();
				}
				item.setId(viewId);
				itemIds.put(viewId, position);
				bindEventOnChild(item, position);
			}
		}

		mRecycleBin.addActiveView(position, item);
		layoutFlag = true;
	}

	private void bindEventOnChild(View child, final int index) {
		child.setFocusable(true);
		child.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(final View item, boolean focus) {

				if (focus) {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							moveCover(item);
						}
					}, delay);
					// 选中事件
					if (onItemSelectListener != null) {
						onItemSelectListener.onItemSelect(item, index);
					}

					scrollIToCenter(item);

				} else {

					returnCover(item);
				}
			}
		});

		child.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View item) {
				if (onItemClickListener != null) {
					onItemClickListener.onItemClick(item, index);
				}

			}
		});

	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		/**
		 * 获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式
		 */
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);

		// 计算出所有的childView的宽和高
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		/**
		 * 记录如果是wrap_content是设置的宽和高
		 */
		int width = 0;
		int height = 0;

		int cWidth = 0;
		int cHeight = 0;
		MarginLayoutParams cParams = null;
		/**
		 * 根据childView计算的出的宽和高，以及设置的margin计算容器的宽和高，主要用于容器是warp_content时
		 */
		for (int i = indexMin; i <= indexMax; i++) {
			// Log.e(VIEW_LOG_TAG,
			// "i="+i+"---"+itemIds.keyAt(i)+"---"+itemIds.size());
			View childView = mRecycleBin.getActiveView(i);
			if (childView == null) {
				continue;
			}
			cWidth = childView.getMeasuredWidth();
			cHeight = childView.getMeasuredHeight();
			cParams = (MarginLayoutParams) childView.getLayoutParams();

			// 上面两个childView
			width += cWidth + cParams.leftMargin + cParams.rightMargin;
			height += cHeight + cParams.topMargin + cParams.bottomMargin;

		}

		/**
		 * 如果是wrap_content设置为我们计算的值 否则：直接设置为父容器计算的值
		 */
		setMeasuredDimension(
				(widthMode == MeasureSpec.EXACTLY || width == 0) ? sizeWidth
						: width,
				(heightMode == MeasureSpec.EXACTLY || height == 0) ? sizeHeight
						: height);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		if (parentLayout) {
			parentLayout = false;
			return;
		}

		// 有时addView后 changed依旧为false故添加此标记
		if (layoutFlag || changed) {
			int cWidth = 0;
			int cHeight = 0;
			// boolean cursorFlag=false;
			/**
			 * 遍历所有childView根据其宽和高，以及margin进行布局
			 */
			for (int i = indexMin; i <= indexMax; i++) {

				View childView = mRecycleBin.getActiveView(i);
				if (childView != null) {
					cWidth = childView.getMeasuredWidth();
					cHeight = childView.getMeasuredHeight();

					int cl = 0, ct = 0, cr = 0, cb = 0;
					cl = (i % columns) * (itemWidth + spaceHori);
					ct = (i / columns) * (spaceVert + itemHeight);

					cr = cl + cWidth;
					cb = cHeight + ct;
					childView.layout(cl + paddingLeft, ct + paddingTop, cr
							+ paddingLeft, cb + paddingTop);
				}

			}
			screenMaxCount = (getHeight() + spaceVert)
					% (itemHeight + spaceVert) == 0 ? (getHeight() + spaceVert)
					/ (itemHeight + spaceVert) : (getHeight() + spaceVert)
					/ (itemHeight + spaceVert) + 1;
					

			// 滚动模式参数调整
			switch (scrollMode) {
			case TvView.SCROLL_FILL:
			case TvView.SCROLL_GALLERY:
				scrollEdge = screenMaxCount / 2 + screenMaxCount % 2;
				break;
			case TvView.SCROLL_EDGE:
				scrollEdge = 1;
				break;
			}
			layoutFlag = false;
		}

	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (!mScroller.isFinished() || isBuilding) {
				return true;
			}

			boolean flag = false;
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

			View focused = this.findFocus();

			if (focused != null && direction != 0) {

				View next = focused.focusSearch(direction);

				// 根据下标算出所在行
				if (next != null) {
					int focusIndex = itemIds.get(focused.getId());
					Integer temp = itemIds.get(next.getId());

					// 焦点切出容器时
					if (temp != null) {
						focusIsOut = false;
						selectIndex = temp;
						// 防止已detatch子项获得焦点
						if (selectIndex < indexMin || selectIndex > indexMax) {
							return true;
						}
					} else {
						parentLayout = true;
						focusIsOut = true;
						return super.dispatchKeyEventPreIme(event);
					}
					int nextRow = 0;
					selectRow = focusIndex / columns;
					nextRow = selectIndex / columns;

					if (scrollMode == TvView.SCROLL_EDGE) {
						if (nextRow > selectRow) {
							if ((next.getTop() - mScroller.getFinalY()) >= ((itemHeight + spaceVert) * (screenMaxCount - 1))
									+ paddingTop) {
								flag = true;
							}
						} else if (nextRow < selectRow) {

							if ((next.getTop() - mScroller.getFinalY()) < paddingTop
									&& selectRow != 0) {
								flag = true;
							}
						}
					} else {

						int rowMaxCount = adapter.getCount() % columns == 0 ? adapter
								.getCount() / columns
								: adapter.getCount() / columns + 1;

						if (nextRow > selectRow) {
							if ((next.getTop() - mScroller.getFinalY()) >= ((itemHeight + spaceVert)
									* (screenMaxCount - 1) / 2)
									+ paddingTop) {

								if (scrollMode == TvView.SCROLL_GALLERY) {
									flag = true;

								} else if (rowMaxCount - nextRow >= (screenMaxCount - 1)
										/ 2 + (screenMaxCount + 1) % 2) {
									flag = true;
								}

							}

						} else if (nextRow < selectRow) {
							if ((next.getTop() - mScroller.getFinalY()) < paddingTop
									+ (itemHeight + spaceVert)
									* (screenMaxCount - 3) / 2
									&& selectRow != 0) {
								if (scrollMode == TvView.SCROLL_GALLERY) {
									flag = true;
								} else if (nextRow >= (screenMaxCount - 3) / 2
										+ (screenMaxCount + 1) % 2) {
									flag = true;
								}

							}

						}
					}

					if (flag) {
						if (nextRow > -1) {
							selectRow = nextRow;
							final int tempDirection = direction;
							handler.postDelayed(new Runnable() {

								@Override
								public void run() {
									scrollByRow(tempDirection, 1);

								}
							}, scrollDelay);

						} else {
							return true;
						}
					}

				}

			}

		}

		return super.dispatchKeyEventPreIme(event);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		// 判断滚动是否结束
		if (t == mScroller.getFinalY()) {

			if (t > oldt) {
				// 下翻加载 当剩余行数小于一屏时
				// Log.e(VIEW_LOG_TAG,
				// selectRow+"===="+maxRow+"===="+scrollEdge);
				if ((maxRow - selectRow) <= scrollEdge) {
					buildList(true);
				}

			} else if (t < oldt) {
				// 上拉加载 当剩余行数小于一屏时
				// Log.e(VIEW_LOG_TAG,
				// selectRow+"===="+minRow+"===="+scrollEdge);
				if ((selectRow - minRow) <= scrollEdge) {
					buildList(false);
				}
			}

		}

		super.onScrollChanged(l, t, oldl, oldt);
	}

	/**
	 * 按行滚动
	 * 
	 * @param page
	 */
	private void scrollByRow(final int direction, int rowCount) {

		if (selectRow < 0 || selectRow > maxRow) {
			return;
		}
		int shifting = (itemHeight + spaceVert) * rowCount;
		if (direction == View.FOCUS_UP) {
			mScroller.startScroll(0, mScroller.getFinalY(), 0, -shifting,
					scrollDuration);
		} else if (direction == View.FOCUS_DOWN) {
			mScroller.startScroll(0, mScroller.getFinalY(), 0, shifting,
					scrollDuration);
		}
		invalidate();

	}

	/**
	 * 确保中心滚动模式下,光标居中
	 * 
	 * @param focused
	 */
	private void scrollIToCenter(View focused) {

		if (scrollMode != TvView.SCROLL_EDGE && focusIsOut&&screenMaxCount>0) {
			if (!mScroller.isFinished() || isBuilding) {
				return;
			}
			int rowMaxCount = adapter.getCount() % columns == 0 ? adapter
					.getCount() / columns : adapter.getCount() / columns + 1;

					
			int rowScroll=mScroller.getFinalY() / (itemHeight + spaceVert);
			int rowCenter = rowScroll
					+ scrollEdge;
			int focusIndex = itemIds.get(focused.getId());
			selectRow = focusIndex / columns;
			int rowFocused=selectRow+1;
			int edge=scrollEdge;
			if (scrollMode==TvView.SCROLL_GALLERY) {
				edge=0;
			}

//			Log.e(VIEW_LOG_TAG, rowFocused + "---" +rowCenter  + "---"
//					+ scrollEdge + "---" + rowMaxCount + "---" + screenMaxCount);

			if (rowFocused > rowCenter) {
				if (rowFocused <= rowMaxCount - edge + 2&&rowScroll<rowMaxCount-screenMaxCount+1) {
					int scrollCount=rowFocused-rowCenter>=
							(adapter.getCount()-rowFocused+1)?rowCenter-rowFocused-1:rowFocused-rowCenter;
					
					scrollByRow(View.FOCUS_DOWN, scrollCount);
				}

			} else if (rowFocused < rowCenter) {
				
				if (rowScroll>0) {
					int scrollCount=0;
					if (rowFocused >= edge-1) {
						scrollCount=rowCenter-rowFocused>=rowFocused?rowCenter-rowFocused-1:rowCenter-rowFocused;
					}else if(rowFocused==1){
						scrollCount=1;
					}
					scrollByRow(View.FOCUS_UP, scrollCount);
				}

			}

		}
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

	/**
	 * 光标移动 到达后 与控件同时放大
	 */
	private void moveCover(View item) {
		if (cursor == null) {
			return;
		}
		if (!item.isFocused()) {
			return;
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
		ObjectAnimator oa2 = ObjectAnimator.ofFloat(item, "ScaleY", 1f);
		oa2.setDuration(durationSmall);
		animatorSet.play(oa).with(oa2);
		animatorSet.start();
	}

	/**
	 * 指定光标相对位置
	 */
	private void setBorderParams(final View item) {
		cursor.clearAnimation();
		cursor.setVisibility(View.VISIBLE);
		final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item
				.getLayoutParams();
		final int l = params.leftMargin + paddingLeft - boarderLeft;
		final int t = params.topMargin + paddingTop - boarderTop;
		final int r = l + itemWidth + boarderRight;
		final int b = t + itemHeight + boarderBottom;
		// 判断动画类型
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
			ValueAnimator transAnimatorX = ObjectAnimator.ofFloat(cursor, "x",
					cursor.getLeft(), l);
			ValueAnimator transAnimatorY = ObjectAnimator.ofFloat(cursor, "y",
					cursor.getTop(), t);
			cursor.layout(l, t, r, b);
			item.bringToFront();
			cursor.bringToFront();
			if (scalable) {
				scaleToLarge(item);
			}

			animatorSet = new AnimatorSet();
			animatorSet.play(transAnimatorY).with(transAnimatorX);
			animatorSet.setDuration(durationTraslate);
			animatorSet.setInterpolator(new DecelerateInterpolator(1));
			// animatorSet.addListener(new AnimatorListener() {
			//
			// @Override
			// public void onAnimationStart(Animator arg0) {
			// // TODO Auto-generated method stub
			//
			// }
			//
			// @Override
			// public void onAnimationRepeat(Animator arg0) {
			// // TODO Auto-generated method stub
			//
			// }
			//
			// @Override
			// public void onAnimationEnd(Animator arg0) {
			// if (scalable) {
			// scaleToLarge(item);
			// }
			//
			// }
			//
			// @Override
			// public void onAnimationCancel(Animator arg0) {
			// // TODO Auto-generated method stub
			//
			// }
			// });
			animatorSet.start();
		}
	}

	class RecycleBin {
		private final static String TAG = "RecycleBin";
		private SparseArray<View> activeViews;
		private SparseArray<View> scrapViews;

		public RecycleBin() {
			activeViews = new SparseArray<View>();
			scrapViews = new SparseArray<View>();

		}

		private void recyleView(View view) {
			view.setBackgroundResource(0);
			if (view instanceof ImageView) {
				((ImageView) view).setImageResource(0);

			}
		}

		public SparseArray<View> getActiveViews() {
			return activeViews;
		}

		public SparseArray<View> getScrapViews() {
			return scrapViews;
		}

		public void recycleViewGroup(View scrapView) {

			if (scrapView instanceof ViewGroup) {

				ViewGroup group = (ViewGroup) scrapView;
				for (int i = 0; i < group.getChildCount(); i++) {
					recyleView(group.getChildAt(i));
				}
			} else {
				recyleView(scrapView);
			}

		}

		public void addActiveView(int position, View item) {
			activeViews.put(position, item);
		}

		/**
		 * 获取活动的view
		 * 
		 * @param position
		 * @param viewId
		 * @return
		 */
		public View getActiveView(int position) {
			View activeView = activeViews.get(position);
			return activeView;

		}

		/**
		 * 将不可见的item 存入缓存
		 * 
		 * @param item
		 */
		public void scrapView(int position, View item) {
			if (item != null) {
				activeViews.remove(position);
				TvGridView.this.detachViewFromParent(item);
				scrapViews.put(position, item);
				if (scrapViews.size() >= screenMaxCount * columns * 2) {
					View scrapView = scrapViews.valueAt(0);
					if (scrapView != null) {
						recycleViewGroup(scrapView);
						scrapViews.removeAt(0);
						itemIds.remove(scrapView.getId());
						scrapView = null;
					}
				}
			}

		}

		public void scrapView(int postion) {
			scrapView(postion, activeViews.get(postion));
		}

		/**
		 * 生成或重载view
		 * 
		 * @param position
		 * @param viewId
		 * @return
		 */
		public View getScrapView(int position) {

			View scrapView = scrapViews.get(position);
			if (scrapView != null) {
				scrapViews.remove(position);
			}

			return scrapView;
		}

		public View retrieveFromScrap() {

			int index = scrapViews.size() - 1;
			View scrapView = scrapViews.get(index);
			scrapViews.remove(index);
			return scrapView;
		}
	}

	public void setOnItemSelectListener(OnItemSelectListener myListener) {
		this.onItemSelectListener = myListener;
	}

	public void setOnItemClickListener(OnItemClickListener myListener) {
		this.onItemClickListener = myListener;
	}

	public interface OnItemSelectListener {
		public void onItemSelect(View item, int position);
	}

	public interface OnItemClickListener {
		public void onItemClick(View item, int position);
	}

	public class AdapterDataSetObservable extends DataSetObservable {
		@Override
		public void notifyChanged() {
			// 数据改变 若已翻至末端 则立即调用buildList
			Log.i(VIEW_LOG_TAG, "收到数据改变通知");

			if (adapter.getCount() <= indexMax + 1) {
				// 删减刷新
				clear();
				handler.post(new Runnable() {

					@Override
					public void run() {
						buildList(false);
					}
				});

			} else {
				// 添加刷新
				if ((maxRow - selectRow) <= scrollEdge) {
					handler.post(new Runnable() {

						@Override
						public void run() {
							buildList(true);
						}
					});
				}

			}

			super.notifyChanged();
		}

		@Override
		public void notifyInvalidated() {
			super.notifyInvalidated();
		}
	}
}
