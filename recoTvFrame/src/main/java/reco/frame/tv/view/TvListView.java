package reco.frame.tv.view;

import reco.frame.tv.R;
import reco.frame.tv.view.component.RecycleBin;
import reco.frame.tv.view.component.TvBaseAdapter;
import reco.frame.tv.view.component.TvUtil;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class TvListView extends RelativeLayout {

	/**
	 * 光标
	 */
	private ImageView cursor;
	private int cursorId;
	/**
	 * 光标资源
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
	 * 放大延迟
	 */
	private int delay = 0;
	/**
	 * 滚动延迟
	 */
	private int scrollDelay = 0;
	/**
	 * 滚动速度
	 */
	private int scrollDuration = 0;
	/**
	 * 滚动模式
	 */
	private int scrollMode;
	/**
	 * 滚动判断 默认离边级一行时刷新视图
	 */
	private int scrollEdge;
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
	 * 刷新延迟
	 */
	private final int DELAY = 231;
	/**
	 * 屏幕可显示最大行数
	 */
	private int screenMaxCount;
	/**
	 * 当前选中子项下示
	 */
	private int indexSelected;
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
	 * 是否默认获得焦点
	 */
	private boolean initFocus;
	/**
	 * 以1280为准,其余分辨率放大比率 用于适配
	 */
	private float screenScale = 1;
	/**
	 * 焦点优先级
	 */
	private int focusOption = 0;

	/**
	 * layout辅助标记 表面有添加新的子项
	 */
	private boolean layoutFlag = false;
	/**
	 * 焦点移出容器
	 */
	private boolean focusIsOut;
	/**
	 * 是否正在生成视图
	 */
	private boolean isBuilding = false;
	private int initLength;
	private int indexMin, indexMax;
	/**
	 * 焦点模式
	 */
	private final static int PARENT_ONLY = 0;
	private final static int CHILD_ONLY = 1;

	private Handler handler = new Handler();

	public TvListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TvListView(Context context) {
		super(context);
	}

	public TvListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray custom = getContext().obtainStyledAttributes(attrs,
				R.styleable.TvListView);
		this.cursorRes = custom.getResourceId(R.styleable.TvListView_cursorRes,
				0);
		this.scalable = custom
				.getBoolean(R.styleable.TvListView_scalable, true);

		this.initFocus = custom.getBoolean(R.styleable.TvListView_initFocus,
				false);
		this.scale = custom.getFloat(R.styleable.TvListView_scale, 1.1f);
		this.animationType = custom.getInt(
				R.styleable.TvListView_animationType, 0);
		this.focusOption = custom.getInt(R.styleable.TvListView_focusOption, 0);
		this.delay = custom.getInteger(R.styleable.TvListView_delay, 110);
		this.scrollDelay = custom.getInteger(
				R.styleable.TvGridView_scrollDelay, 171);
		this.scrollDuration = custom.getInteger(
				R.styleable.TvGridView_scrollDuration, 371);
		this.scrollMode = custom.getInt(R.styleable.TvListView_scrollMode,
				TvView.SCROLL_FILL);
		this.durationLarge = custom.getInteger(
				R.styleable.TvListView_durationLarge, 100);
		this.durationSmall = custom.getInteger(
				R.styleable.TvListView_durationSmall, 100);
		this.spaceVert = (int) custom.getDimension(
				R.styleable.TvListView_spaceVert, 10);

		itemWidth = (int) custom.getDimension(R.styleable.TvListView_itemWidth,
				10);
		itemHeight = (int) custom.getDimension(
				R.styleable.TvListView_itemHeight, 10);

		paddingLeft = (int) custom.getDimension(
				R.styleable.TvListView_paddingLeft, 0);
		paddingTop = (int) custom.getDimension(
				R.styleable.TvListView_paddingTop, 2);

		this.boarder = (int) custom.getDimension(
				R.styleable.TvListView_boarder, 0)
				+ custom.getInteger(R.styleable.TvListView_boarderInt, 0);

		if (boarder == 0) {
			this.boarderLeft = (int) custom.getDimension(
					R.styleable.TvListView_boarderLeft, 0)
					+ custom.getInteger(R.styleable.TvListView_boarderLeftInt,
							0);
			this.boarderTop = (int) custom.getDimension(
					R.styleable.TvListView_boarderTop, 0)
					+ custom.getInteger(R.styleable.TvListView_boarderTopInt, 0);
			this.boarderRight = (int) custom.getDimension(
					R.styleable.TvListView_boarderRight, 0)
					+ custom.getInteger(R.styleable.TvListView_boarderRightInt,
							0);
			this.boarderBottom = (int) custom.getDimension(
					R.styleable.TvListView_boarderBottom, 0)
					+ custom.getInteger(
							R.styleable.TvListView_boarderBottomInt, 0);
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
						R.styleable.TvListView_cursorRes_1280, 0);
				break;

			case TvUtil.SCREEN_1920:
				cursorRes = custom.getResourceId(
						R.styleable.TvListView_cursorRes_1920, 0);
				break;
			case TvUtil.SCREEN_2560:
				cursorRes = custom.getResourceId(
						R.styleable.TvListView_cursorRes_2560, 0);
				break;
			case TvUtil.SCREEN_3840:
				cursorRes = custom.getResourceId(
						R.styleable.TvListView_cursorRes_3840, 0);
				break;

			case TvUtil.SCREEN_4096:
				cursorRes = custom.getResourceId(
						R.styleable.TvListView_cursorRes_4096, 0);
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
			initBuild();
			isInit = false;
		}

		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				initList();
			}
		}, DELAY);
	}

	private void clear() {
		itemIds.clear();
		this.removeAllViews();
		this.clearDisappearingChildren();
		this.destroyDrawingCache();
		mScroller.setFinalY(0);
		parentLayout = false;
		focusIsOut = true;
	}

	/**
	 * 首次加载屏幕可见行数*2
	 */
	public void initBuild() {

		if (getParent() instanceof RelativeLayout) {

			// 重设参数
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			RelativeLayout.LayoutParams newParams = new RelativeLayout.LayoutParams(
					params.width, params.height);
			this.setPadding((int) (boarderLeft * scale),
					(int) (boarderTop * scale), boarderRight, boarderBottom);

			newParams.setMargins(params.leftMargin, params.topMargin,
					params.rightMargin, params.bottomMargin);
			this.setLayoutParams(newParams);

		} else if (getParent() instanceof LinearLayout) {
			// 重设参数
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
			LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(
					params.width, params.height);
			this.setPadding((int) (boarderLeft * scale),
					(int) (boarderTop * scale), boarderRight, boarderBottom);

			newParams.setMargins(params.leftMargin, params.topMargin,
					params.rightMargin, params.bottomMargin);
			this.setLayoutParams(newParams);

		}

	}

	private void initList() {
		// 避免冲突
		if (getChildCount() > 0) {
			return;
		}

		int screenHeight = wm.getDefaultDisplay().getHeight();
		int initRows = (screenHeight - itemHeight) % (itemHeight + spaceVert) == 0 ? (screenHeight - itemHeight)
				/ (itemHeight + spaceVert) + 1
				: (screenHeight - itemHeight) / (itemHeight + spaceVert) + 2;

		initLength = Math.min(adapter.getCount(), initRows * 2);
		indexMin = 0;
		indexMax = initLength - 1;

		for (int i = 0; i < initLength; i++) {
			buildItem(i);

		}

		cursor = new ImageView(getContext());
		cursorId = TvUtil.buildId();
		cursor.setId(cursorId);
		cursor.setBackgroundResource(cursorRes);
		this.addView(cursor);
		cursor.setVisibility(View.INVISIBLE);

		View focus = ((ViewGroup) getParent()).findFocus();

		if (initFocus || (focus == null && !initFocus)) {
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					View item = getChildAt(0);
					if (item != null) {
						item.requestFocus();
					}

				}
			}, 100);
		}

		if (focus == null) {

		}

	}

	private void buildList(boolean down) {
		if (isBuilding || adapter.getCount() == initLength) {
			return;
		}
		isBuilding = true;
		int start = 0, end = 0;

		int scrapStart = 0, scrapEnd = 0;

		// Log.e(VIEW_LOG_TAG, down + "---" + indexMin + "---" + indexMax);

		if (down) {

			scrapStart = indexMin;
			scrapEnd = Math.min(scrapStart + screenMaxCount + 1, indexMax
					- screenMaxCount + 1);

			indexMin = scrapEnd;

			start = indexMax + 1;
			end = Math.min(start + screenMaxCount + 2, adapter.getCount());
			indexMax = end - 1;

		} else {
			scrapEnd = indexMax + 1;
			scrapStart = Math.max(scrapEnd - screenMaxCount + 1, indexMin
					+ screenMaxCount + 1);

			indexMax = scrapStart - 1;

			start = Math.max(0, indexMin - screenMaxCount + 1);
			end = Math.max(0, indexMin);
			indexMin = start;

		}

		// Log.e(VIEW_LOG_TAG, scrapStart + "---" + scrapEnd + "---" + start
		// + "---" + end);

		for (int i = scrapStart; i < scrapEnd; i++) {
			mRecycleBin.scrapView(i);
		}

		for (int i = start; i < end; i++) {
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
		int left = 0;
		int top = position * (spaceVert + itemHeight);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				itemWidth, itemHeight);
		if (initLength == 1) {
			rlp.setMargins(left, top, paddingLeft * 2, 0);
		} else {
			rlp.setMargins(left, top, 0, 0);
		}

		View item = mRecycleBin.getScrapView(position);

		if (item == null) {

			View contentView = mRecycleBin.retrieveFromScrap();
			if (contentView != null) {
				// removeDetachedView(contentView, false);
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
			bindEventOnItem(item, position);
		} else {

			if (getChildCount() > position) {
				attachViewToParent(item, position, rlp);
			} else {
				itemIds.remove(item.getId());
				this.addView(item, rlp);

				int viewId = item.getId();
				if (viewId == -1) {
					viewId = TvUtil.buildId();
				}
				item.setId(viewId);
				itemIds.put(viewId, position);
				bindEventOnItem(item, position);
			}
		}

		mRecycleBin.addActiveView(position, item);
		layoutFlag = true;

	}

	/**
	 * 绑定事件
	 * 
	 * @param child
	 */
	private void bindEventOnItem(final View item, final int index) {

		// 根据焦点优先级设定选中效果
		switch (focusOption) {
		case PARENT_ONLY:
			item.setFocusable(true);
			item.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(final View child, boolean focus) {

					if (focus) {
						new Handler().postDelayed(new Runnable() {

							@Override
							public void run() {
								moveCover(item, child);
							}
						}, delay);
						// 选中事件
						if (onItemSelectListener != null) {
							onItemSelectListener.onItemSelect(item, index);
						}
						scrollToCenter(child);

					} else {
						returnCover(child);
						// 失去焦点
						if (onItemSelectListener != null) {
							onItemSelectListener.onItemDisSelect(item, index);
						}
					}
				}
			});

			if (onItemClickListener != null) {
				item.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View item) {
						onItemClickListener.onItemClick(item, index);

					}
				});
			}

			break;
		case CHILD_ONLY:
			item.setFocusable(false);
			ViewGroup itemGroup = ((ViewGroup) item);

			for (int i = 0; i < itemGroup.getChildCount(); i++) {

				View itemChild = itemGroup.getChildAt(i);
				if (!itemChild.isFocusable()) {
					continue;
				}

				itemChild.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(final View child, boolean focus) {

						if (focus) {
							new Handler().postDelayed(new Runnable() {

								@Override
								public void run() {
									moveCover(item, child);
								}
							}, delay);
							// 选中事件
							if (onItemSelectListener != null) {
								onItemSelectListener.onItemSelect(item, index);
							}

							scrollToCenter(child);

						} else {
							returnCover(child);
							// 失去焦点
							if (onItemSelectListener != null) {
								onItemSelectListener.onItemDisSelect(item,
										indexSelected);
							}
						}
					}
				});

				if (onItemClickListener != null) {
					itemChild.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View item) {
							onItemClickListener.onItemClick(item, index);

						}
					});
				}

			}

			itemGroup = null;

			break;
		}

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

		if (layoutFlag || changed) {
			int cWidth = 0;
			int cHeight = 0;
			/**
			 * 遍历所有childView根据其宽和高，以及margin进行布局
			 */
			for (int i = indexMin; i <= indexMax; i++) {
				View item = mRecycleBin.getActiveView(i);
				if (item != null) {
					cWidth = item.getMeasuredWidth();
					cHeight = item.getMeasuredHeight();

					int cl = 0, ct = 0, cr = 0, cb = 0;
					cl = 0;
					ct = i * (spaceVert + itemHeight);

					cr = cl + cWidth;
					cb = cHeight + ct;
					item.layout(cl + paddingLeft, ct + paddingTop, cr
							+ paddingLeft, cb + paddingTop);
					screenMaxCount = (getHeight() + spaceVert)
							% (itemHeight + spaceVert) == 0 ? (getHeight() + spaceVert)
							/ (itemHeight + spaceVert)
							: (getHeight() + spaceVert)
									/ (itemHeight + spaceVert) + 1;

					switch (scrollMode) {
					case TvView.SCROLL_FILL:
					case TvView.SCROLL_GALLERY:
						scrollEdge = screenMaxCount / 2 + screenMaxCount % 2;
						break;
					case TvView.SCROLL_EDGE:
						scrollEdge = 1;
						break;
					}
				}

			}

			layoutFlag = false;
		}

	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {

			if (!mScroller.isFinished() || isBuilding || itemIds == null) {
				return true;
			}
			boolean flag = false;
			View focused = this.findFocus();
			View next = null;
			int focusIndex = 0;
			switch (focusOption) {
			case PARENT_ONLY:
				focusIndex = itemIds.get(focused.getId());
				break;

			case CHILD_ONLY:
				focusIndex = itemIds.get(((View) focused.getParent()).getId());
				break;
			}
			int direction = 0;

			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				direction = View.FOCUS_DOWN;
				// 获得目标焦点控件
				int tempPosition1 = itemIds.indexOfValue(focusIndex + 1);
				if (tempPosition1 == -1) {
					return true;
				}
				next = findViewById(itemIds.keyAt(tempPosition1));
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				direction = View.FOCUS_RIGHT;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				int tempPosition2 = itemIds.indexOfValue(focusIndex - 1);
				if (tempPosition2 == -1) {
					return true;
				}
				next = findViewById(itemIds.keyAt(itemIds
						.indexOfValue(focusIndex - 1)));
				direction = View.FOCUS_UP;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				direction = View.FOCUS_LEFT;
				break;
			}
			if (next == null) {
				focusIsOut = true;
			}

			if (direction != 0 && next != null) {
				Integer temp = itemIds.get(next.getId());
				int indexNext = 0;
				// 焦点切出容器时
				if (temp != null) {
					indexNext = temp;
					focusIsOut = false;
					// Log.e(VIEW_LOG_TAG,
					// indexNext+"----"+indexMin+"---"+indexMax);
					if (indexNext < indexMin || indexNext > indexMax) {
						return true;
					}
				} else {
					parentLayout = true;
					focusIsOut = true;
					return super.dispatchKeyEventPreIme(event);
				}

				if (scrollMode == TvView.SCROLL_EDGE) {
					if (indexNext > focusIndex) {
						if ((next.getTop() - mScroller.getFinalY()) >= ((itemHeight + spaceVert) * (screenMaxCount - 1))
								+ paddingTop) {
							flag = true;
						}
					} else if (indexNext < focusIndex) {

						if ((next.getTop() - mScroller.getFinalY()) < paddingTop) {
							flag = true;

						}
					}
				} else {
					if (indexNext > focusIndex) {

						if ((next.getTop() - mScroller.getFinalY()) >= ((itemHeight + spaceVert)
								* (screenMaxCount - 1) / 2)
								+ paddingTop) {

							if (scrollMode == TvView.SCROLL_GALLERY) {
								flag = true;

							} else if (adapter.getCount() - indexNext >= (screenMaxCount - 1)
									/ 2 + (screenMaxCount + 1) % 2) {
								flag = true;
							}

						}

					} else if (indexNext < focusIndex) {

						if ((next.getTop() - mScroller.getFinalY()) < paddingTop
								+ (itemHeight + spaceVert)
								* (screenMaxCount - 3) / 2
								&& focusIndex != 0) {
							if (scrollMode == TvView.SCROLL_GALLERY) {
								flag = true;
							} else if (indexNext >= (screenMaxCount - 3) / 2
									+ (screenMaxCount + 1) % 2
									|| mScroller.getFinalY() > 0) {
								flag = true;
							}

						}

					}
				}
				if (flag) {
					if (indexNext > -1) {
						indexSelected = indexNext;
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

		return super.dispatchKeyEventPreIme(event);
	}

	/**
	 * 根据下标获取item
	 * @param position
	 */
//	public View getItemAtPosition(int position){
//		
//		return findViewById(itemIds.keyAt(itemIds.indexOfValue(position)));
//	}
	
	private int getItemRootIndex(View focused) {

		View parent = ((View) focused.getParent());
		int id = parent.getId();
		if (id != -1) {

			int index = itemIds.indexOfKey(id);
			if (index > -1 && index < itemIds.size()) {
				return index;
			} else {
				return getItemRootIndex(parent);
			}

		} else {

			return getItemRootIndex(parent);
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {

		if (t == mScroller.getFinalY()) {

			if (t > oldt) {
				// 下翻加载 当剩余行数小于一屏时
				if ((indexMax - indexSelected) <= scrollEdge) {
					buildList(true);
				}

			} else if (t < oldt) {
				if (indexSelected - indexMin <= scrollEdge) {
					buildList(false);
				}

			}

		}

		super.onScrollChanged(l, t, oldl, oldt);
	}

	/**
	 * 翻页
	 * 
	 * @param page
	 */
	private void scrollByRow(final int direction, int rowCount) {
		if (indexSelected < 0 || indexSelected > indexMax) {
			return;
		}
		int shifting = (itemHeight + spaceVert) * rowCount;
		if (direction == View.FOCUS_UP) {
			// 放止向上滚动超出
			if (mScroller.getFinalY() <= 0) {
				return;
			}
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
	private void scrollToCenter(View focused) {

		if (scrollMode != TvView.SCROLL_EDGE && focusIsOut
				&& screenMaxCount > 0) {
			if (!mScroller.isFinished() || isBuilding) {
				return;
			}

			int indexScroll = mScroller.getFinalY() / (itemHeight + spaceVert);
			int indexCenter = indexScroll + scrollEdge;

			switch (focusOption) {
			case PARENT_ONLY:
				indexSelected = itemIds.get(focused.getId());
				break;

			case CHILD_ONLY:
				indexSelected = itemIds.get(((View) focused.getParent())
						.getId());
				break;
			}
			int indexFocused = indexSelected + 1;

			int edge = scrollEdge;
			if (scrollMode == TvView.SCROLL_GALLERY) {
				edge = 0;
			}

//			Log.e(VIEW_LOG_TAG,
//					indexSelected + "---" + indexFocused + "---" + indexCenter
//							+ "---" + scrollEdge + "---" + adapter.getCount()
//							+ "---" + screenMaxCount + "---" + indexScroll);

			if (indexFocused > indexCenter) {
				if (indexFocused <= adapter.getCount() - edge + 2
						&& indexScroll < adapter.getCount() - screenMaxCount
								+ 1) {
					int scrollCount = indexFocused - indexCenter >= (adapter
							.getCount() - indexFocused + 1) ? indexFocused
							- indexCenter - 1 : indexFocused - indexCenter;
					scrollByRow(View.FOCUS_DOWN, scrollCount);
				}

			} else if (indexFocused < indexCenter) {

				if (indexScroll > 0) {
					int scrollCount = 0;
					if (indexCenter >= edge - 1) {
						scrollCount = indexCenter - indexFocused > indexScroll ? indexScroll
								: indexCenter - indexFocused;
					} else if (indexFocused == 1) {
						scrollCount = 1;
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
	private void moveCover(View item, View focus) {

		if (cursor == null) {
			return;
		}

		setBorderParams(item, focus);
		focus.bringToFront();
		cursor.bringToFront();
		if (scalable) {
			scaleToLarge(focus);
		}

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
	 * 指定光标相对位置
	 */
	private void setBorderParams(View item, View focus) {
		cursor.clearAnimation();
		cursor.setVisibility(View.VISIBLE);

		// 判断类型
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) item
				.getLayoutParams();
		int l = 0, t = 0, r = 0, b = 0;
		
//		int[] location = new int[2];
//		focus.getLocationInWindow(location);
//		
//		Log.e(VIEW_LOG_TAG, location[0]+"---"+location[1]);

		if (item == focus) {
			l = params.leftMargin + paddingLeft - boarderLeft;
			t = params.topMargin + paddingTop - boarderTop;
			r = l + itemWidth + boarderRight + boarderLeft;
			b = t + itemHeight + boarderBottom + boarderTop;
		} else {
			l = params.leftMargin + focus.getLeft() + paddingLeft - boarderLeft;
			t = params.topMargin + focus.getTop() + paddingTop - boarderTop;
			r = l + focus.getWidth() + boarderRight + boarderLeft;
			b = t + focus.getHeight() + boarderBottom + boarderTop;
		}

		cursor.layout(l, t, r, b);

	}

	class RecycleBin {
		private final static String TAG = "RecycleBin";
		private SparseArray<View> activeViews;
		private SparseArray<View> scrapViews;

		public RecycleBin() {
			activeViews = new SparseArray<View>();
			scrapViews = new SparseArray<View>();
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
					recycleViewGroup(group.getChildAt(i));
				}
			} else {
				scrapView.setBackgroundResource(0);
				if (scrapView instanceof ImageView) {
					((ImageView) scrapView).setImageResource(0);

				} else if (scrapView instanceof TextView) {
					((TextView) scrapView).setText(null);

				}
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
				TvListView.this.detachViewFromParent(item);
				scrapViews.put(position, item);
				if (scrapViews.size() >= screenMaxCount * 2) {
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
		/**
		 * 子项获得焦点
		 * 
		 * @param item
		 * @param position
		 */
		public void onItemSelect(View item, int position);

		/**
		 * 子项失去焦点
		 * 
		 * @param item
		 * @param position
		 */
		public void onItemDisSelect(View item, int position);

	}

	public interface OnItemClickListener {
		public void onItemClick(View item, int position);
	}

	public class AdapterDataSetObservable extends DataSetObservable {
		@Override
		public void notifyChanged() {
			// 数据改变 若已翻至末端 则立即调用addNewItems
			Log.i(VIEW_LOG_TAG, "收到数据改变通知");

			if (adapter.getCount() <= indexMax - 1) {
				// 删减刷新
				clear();
				buildList(false);
			} else {
				// 添加刷新
				if ((indexMax - indexSelected) <= 1) {
					buildList(true);
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
