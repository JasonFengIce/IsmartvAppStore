package reco.frame.tv.view;

import reco.frame.tv.R;
import reco.frame.tv.view.component.TvBaseAdapter;
import reco.frame.tv.view.component.TvUtil;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObservable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

/**
 * 横向的gridview 自带选中框效果
 * 
 * @author keYence
 * 
 */
public class TvGallery extends RelativeLayout {

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
	 * 平移用时
	 */
	private int durationTraslate = 100;
	/**
	 * 放大延迟
	 */
	private int delay = 0;

	/**
	 * 滚动判断 默认离边级一行时刷新视图
	 */
	private int scrollEdge;
	/**
	 * 滚动延迟
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
	 * 焦点移出容器
	 */
	private boolean focusIsOut;
	/**
	 * 刷新延迟
	 */
	private final int DELAY = 371;
	/**
	 * 容器显示最大数
	 */
	private int screenMaxCount;
	/**
	 * 当前选中子项下示
	 */
	private int indexSelected;
	/**
	 * 内边距
	 */
	private int paddingLeft, paddingTop;
	/**
	 * item宽高 不包括纵横间距
	 */
	private int itemWidth, itemHeight;
	/**
	 * 子项间隔
	 */
	private int spaceHori;
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
	 * 是否初始默认获得焦点
	 */
	private boolean initFocus;
	/**
	 * 滚动模式
	 */
	private int scrollMode;
	/**
	 * layout辅助标记 表面有添加新的子项
	 */
	private boolean layoutFlag = false;
	private boolean isBuilding = false;
	private int initLength;
	private int indexMin, indexMax;
	/**
	 * 以1280为准,其余分辨率放大比率 用于适配
	 */
	private float screenScale = 1;

	private Handler handler = new Handler();

	public TvGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public TvGallery(Context context) {
		super(context);
	}

	public TvGallery(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray custom = getContext().obtainStyledAttributes(attrs,
				R.styleable.TvGallery);
		this.cursorRes = custom.getResourceId(R.styleable.TvGallery_cursorRes,
				0);
		this.scalable = custom.getBoolean(R.styleable.TvGallery_scalable, true);
		this.initFocus = custom.getBoolean(R.styleable.TvGallery_initFocus,
				true);

		this.scale = custom.getFloat(R.styleable.TvGallery_scale, 1.1f);
		this.animationType = custom.getInt(R.styleable.TvGallery_animationType,
				ANIM_DEFAULT);
		this.delay = custom.getInteger(R.styleable.TvGallery_delay, 110);
		this.scrollDelay = custom.getInteger(R.styleable.TvGallery_scrollDelay,
				171);
		this.scrollDuration = custom.getInteger(
				R.styleable.TvGallery_scrollDuration, 371);
		this.scrollMode = custom.getInt(R.styleable.TvGallery_scrollMode,
				TvView.SCROLL_GALLERY);
		this.durationLarge = custom.getInteger(
				R.styleable.TvGallery_durationLarge, 230);
		this.durationTraslate = custom.getInteger(
				R.styleable.TvGallery_durationTranslate, 230);
		this.durationSmall = custom.getInteger(
				R.styleable.TvGallery_durationSmall, 100);

		this.spaceHori = (int) custom.getDimension(
				R.styleable.TvGallery_spaceHori, 0);
		itemWidth = (int) custom.getDimension(R.styleable.TvGallery_itemWidth,
				10);
		itemHeight = (int) custom.getDimension(
				R.styleable.TvGallery_itemHeight, 10);

		paddingLeft = (int) custom.getDimension(
				R.styleable.TvGallery_paddingLeft, 0);
		paddingTop = (int) custom.getDimension(
				R.styleable.TvGallery_paddingTop, 0);

		this.boarder = (int) custom.getDimension(R.styleable.TvGallery_boarder,
				0) + custom.getInteger(R.styleable.TvGallery_boarderInt, 0);

		if (boarder == 0) {
			this.boarderLeft = (int) custom.getDimension(
					R.styleable.TvGallery_boarderLeft, 0)
					+ custom.getInteger(R.styleable.TvGallery_boarderLeftInt, 0);
			this.boarderTop = (int) custom.getDimension(
					R.styleable.TvGallery_boarderTop, 0)
					+ custom.getInteger(R.styleable.TvGallery_boarderTopInt, 0);
			this.boarderRight = (int) custom.getDimension(
					R.styleable.TvGallery_boarderRight, 0)
					+ custom.getInteger(R.styleable.TvGallery_boarderRightInt,
							0);
			this.boarderBottom = (int) custom.getDimension(
					R.styleable.TvGallery_boarderBottom, 0)
					+ custom.getInteger(R.styleable.TvGallery_boarderBottomInt,
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
						R.styleable.TvGallery_cursorRes_1280, 0);
				break;

			case TvUtil.SCREEN_1920:
				cursorRes = custom.getResourceId(
						R.styleable.TvGallery_cursorRes_1920, 0);
				break;
			case TvUtil.SCREEN_2560:
				cursorRes = custom.getResourceId(
						R.styleable.TvGallery_cursorRes_2560, 0);
				break;
			case TvUtil.SCREEN_3840:
				cursorRes = custom.getResourceId(
						R.styleable.TvGallery_cursorRes_3840, 0);
				break;
			case TvUtil.SCREEN_4096:
				cursorRes = custom.getResourceId(
						R.styleable.TvGallery_cursorRes_4096, 0);
				break;
			}
		}
		custom.recycle();
		// 关闭子控件动画缓存 使嵌套动画更流畅
		// setAnimationCacheEnabled(false);

		init();
	}

	/**
	 * 初始全局变量
	 */
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

	
	public void initBuild() {
		// 重设参数

		if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
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
		} else if (getLayoutParams() instanceof LinearLayout.LayoutParams) {
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
	/**
	 * 计算首次加载长度
	 */
	private void initList() {
		// 避免冲突
		if (getChildCount() > 0) {
			return;
		}

		int screenWidth = wm.getDefaultDisplay().getWidth();
		int initCols = (screenWidth - itemWidth) % (itemWidth + spaceHori) == 0 ? (screenWidth - itemWidth)
				/ (itemWidth + spaceHori) + 1
				: (screenWidth - itemWidth) / (itemWidth + spaceHori) + 2;

		initLength = Math.min(adapter.getCount(), initCols * 2);

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

		if (initFocus) {
			View focus = ((ViewGroup) getParent()).findFocus();
			if (focus == null) {
				View item = getChildAt(0);
				if (item != null) {
					// item.requestFocus();
				}
			}
		}

	}

	private void buildList(boolean down) {
		if (isBuilding||adapter.getCount()==initLength) {
			return;
		}
		isBuilding = true;
		int start = 0, end = 0;

		int scrapStart = 0, scrapEnd = 0;

		if (down) {

			scrapStart = indexMin;
			scrapEnd = Math.min(scrapStart + screenMaxCount + 1, indexMax
					- screenMaxCount + 1);

			indexMin = scrapEnd;

			start = scrapEnd == 1 ? indexMax : indexMax + 1;
			end = Math.min(start + screenMaxCount + 1, adapter.getCount() - 1);
			indexMax = end;

		} else {
			scrapEnd = indexMax + 1;
			scrapStart = Math.max(scrapEnd - screenMaxCount + 1, indexMin
					+ screenMaxCount + 1);

			indexMax = scrapStart - 1;

			start = Math.max(0, indexMin - screenMaxCount + 1);
			end = Math.max(0, indexMin - 1);
			indexMin = start;

		}

//		Log.e(VIEW_LOG_TAG, scrapStart + "---" + scrapEnd + "---" + start
//				+ "---" + end);

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
		int left = position * (itemWidth + spaceHori);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				itemWidth, itemHeight);
		if (initLength == 1) {
			rlp.setMargins(left, paddingTop, 0, paddingTop * 2);
		} else {
			rlp.setMargins(left, paddingTop, 0, 0);
		}

		View item = mRecycleBin.getScrapView(position);

		if (item == null) {

			View contentView = mRecycleBin.retrieveFromScrap();
			if (contentView != null) {
				//removeDetachedView(contentView, false);
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
					
					scrollToCenter(item);

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

		if (changed || layoutFlag) {
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
					cl = i * (itemWidth + spaceHori) + paddingLeft;
					ct = paddingTop;
					cr = cl + cWidth;
					cb = ct + cHeight;
					item.layout(cl, ct, cr, cb);

				}

			}
			screenMaxCount = (getWidth() + spaceHori) % (itemWidth + spaceHori) == 0 ? (getWidth() + spaceHori)
					/ (itemWidth + spaceHori)
					: (getWidth() + spaceHori) / (itemWidth + spaceHori) + 1;

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
				
				//出界判断
				if (next==null) {
					focusIsOut = true;
				}
				// 根据下标算出所在行
				if (next != null) {

					int focusIndex = itemIds.get(focused.getId());
					Integer temp = itemIds.get(next.getId());
					int indexNext = 0;
					// 焦点切出容器时
					if (temp != null) {
						indexNext = temp;
						focusIsOut = false;
//						Log.e(VIEW_LOG_TAG, indexNext + "--" + indexMin + "--"
//								+ indexMax);
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
							if ((next.getLeft() - mScroller.getFinalX()) >= ((itemWidth + spaceHori) * (screenMaxCount - 1))
									+ paddingLeft) {
								flag = true;
							}
						} else if (indexNext < focusIndex) {
							if ((next.getLeft() - mScroller.getFinalX()) < paddingLeft) {
								flag = true;

							}
						}
					} else {
						if (indexNext > focusIndex) {

							if ((next.getLeft() - mScroller.getFinalX()) >= ((itemWidth + spaceHori)
									* (screenMaxCount - 1) / 2)
									+ paddingLeft) {

								if (scrollMode == TvView.SCROLL_GALLERY) {
									flag = true;

								} else if (adapter.getCount() - indexNext >= (screenMaxCount - 1)
										/ 2 + (screenMaxCount + 1) % 2) {
									flag = true;
								}

							}

						} else if (indexNext < focusIndex) {
							if ((next.getLeft() - mScroller.getFinalX()) < paddingLeft
									+ (itemWidth + spaceHori)
									* (screenMaxCount - 3) / 2
									&& focusIndex != 0) {
								if (scrollMode == TvView.SCROLL_GALLERY) {
									flag = true;
								} else if (indexNext >= (screenMaxCount - 3)
										/ 2 + (screenMaxCount + 1) % 2) {
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
									scrollByRow(tempDirection,1);

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

		if (l == mScroller.getFinalX()) {
			if (l > oldl) {
				// 下翻加载 当剩余行数小于一屏时
				if ((indexMax - indexSelected) <= scrollEdge) {
					buildList(true);
				}

			} else if (l < oldl) {
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
	private void scrollByRow(final int direction,int rowCount) {
		if (indexSelected < 0 || indexSelected > indexMax) {
			return;
		}
		int shifting = (itemWidth + spaceHori)*rowCount;
		if (direction == View.FOCUS_LEFT) {
			mScroller.startScroll(mScroller.getFinalX(), 0, -shifting, 0,
					scrollDuration);
		} else if (direction == View.FOCUS_RIGHT) {
			mScroller.startScroll(mScroller.getFinalX(), 0, shifting, 0,
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

		if (scrollMode != TvView.SCROLL_EDGE && focusIsOut&&screenMaxCount>0) {
			if (!mScroller.isFinished() || isBuilding) {
				return;
			}

			
			int indexScroll=mScroller.getFinalX() / (itemWidth + spaceHori);
			int indexCenter = indexScroll+ scrollEdge;
			
			indexSelected = itemIds.get(focused.getId());
			int indexFocused=indexSelected+1;
			
			int edge=scrollEdge;
			if (scrollMode==TvView.SCROLL_GALLERY) {
				edge=0;
			}

//			Log.e(VIEW_LOG_TAG, indexFocused + "---" + indexCenter + "---"
//					+ scrollEdge + "---" + adapter.getCount() + "---" + screenMaxCount+"---"+indexScroll);

			if (indexFocused > indexCenter) {
				if (indexFocused <= adapter.getCount() - edge + 2&&indexScroll<adapter.getCount()-screenMaxCount+1) {
					int scrollCount=indexFocused-indexCenter>=(adapter.getCount()-indexFocused+1)?indexFocused-indexCenter-1:indexFocused-indexCenter;
					
					scrollByRow(View.FOCUS_RIGHT, scrollCount);
				}

			} else if (indexFocused < indexCenter) {
				if (indexFocused >= edge-1&&indexScroll>0) {
					int scrollCount=indexCenter-indexFocused>=indexFocused?indexCenter-indexFocused-1:indexCenter-indexFocused;
					scrollByRow(View.FOCUS_LEFT, scrollCount);
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
			scrollTo(mScroller.getCurrX(), 0);
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
		oa.start();
		ObjectAnimator oa2 = ObjectAnimator.ofFloat(item, "ScaleY", 1f);
		oa2.setDuration(durationSmall);
		oa2.start();
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
		final int t = params.topMargin - boarderTop;
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
			if (focusIsOut) {
				return;
			}

			animatorSet = new AnimatorSet();
			animatorSet.play(transAnimatorY).with(transAnimatorX);
			animatorSet.setDuration(durationTraslate);
			animatorSet.setInterpolator(new DecelerateInterpolator(1));
			// animatorSet.addListener(new AnimatorListener() {
			//
			// @Override
			// public void onAnimationStart(Animator arg0) {
			// }
			//
			// @Override
			// public void onAnimationRepeat(Animator arg0) {
			// }
			//
			// @Override
			// public void onAnimationEnd(Animator arg0) {
			// cursor.layout(l, t, r, b);
			// item.bringToFront();
			// cursor.bringToFront();
			// if (scalable) {
			// scaleToLarge(item);
			// }
			// }
			//
			// @Override
			// public void onAnimationCancel(Animator arg0) {
			// }
			// });
			animatorSet.start();

			break;

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
				TvGallery.this.detachViewFromParent(item);
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
		public void onItemSelect(View item, int position);
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
