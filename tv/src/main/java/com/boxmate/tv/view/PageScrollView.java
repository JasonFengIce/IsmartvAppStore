package com.boxmate.tv.view;

import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;

import android.R.integer;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class PageScrollView extends LinearLayout {
	private Scroller mScroller; 
	private int pageWidth = 0;
	public PageScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mScroller = new Scroller(context);
		pageWidth = (int)getResources().getDimension(R.dimen.px1608);
	}
	public void setPageWidth(int width) {
		pageWidth = width;
	}
	public PageScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mScroller = new Scroller(context); 
		pageWidth = (int)getResources().getDimension(R.dimen.px1608);
	}

	public PageScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context); 
		pageWidth = (int)getResources().getDimension(R.dimen.px1608);
	}
	public void scrollToPage(int page) {
		scrollTo(page*pageWidth, 0);
	}
	public void smoothToPage(int page) {
		smoothScrollTo(page*pageWidth, 0);
	}
	 //调用此方法滚动到目标位置  
    public void smoothScrollTo(int fx, int fy) {  
        int dx = fx - mScroller.getFinalX();  
        int dy = fy - mScroller.getFinalY();  
        smoothScrollBy(dx, dy);  
    }  
  
    //调用此方法设置滚动的相对偏移  
    public void smoothScrollBy(int dx, int dy) {  
  
        //设置mScroller的滚动偏移量  
        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), dx, dy,500);  
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果  
    }  
      
    @Override  
    public void computeScroll() {  
      
        //先判断mScroller滚动是否完成  
        if (mScroller.computeScrollOffset()) {  
          
            //这里调用View的scrollTo()完成实际的滚动  
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());  
              
            //必须调用该方法，否则不一定能看到滚动效果  
            postInvalidate();  
        }  
        super.computeScroll();  
    } 
}
