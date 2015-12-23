package com.boxmate.tv.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import reco.frame.tv.view.TvRelativeLayout;
import com.boxmate.tv.R;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.ui.manage.UninstallActivity;
import com.boxmate.tv.util.CacheHelper;
import com.boxmate.tv.util.CacheHelper.SyncCallBack;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UninstallListAdapter extends PagerAdapter {

	private final static String TAG = "UninstallListAdapter";
	private List<List<AppInfo>> pageList;
	private List<AppInfo> appList;
	private Context context;
	private int width, height, spaceHori, spaceVert, margin;
	private LayoutInflater mInflater;
	private int marginTop;
	private int col = 3, row = 3;
	private int pageCount;

	public int getPageCount() {
		return pageCount;
	}

	public UninstallListAdapter(Context context, List<AppInfo> appList) {

		this.context = context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.appList = appList;
		pageList = new ArrayList<List<AppInfo>>();
		this.width = (int) context.getResources().getDimension(R.dimen.px490);
		this.height = (int) context.getResources().getDimension(R.dimen.px224);
		this.margin = (int) context.getResources().getDimension(R.dimen.px40);
		this.spaceHori = (int) context.getResources()
				.getDimension(R.dimen.px10);
		this.spaceVert = (int) context.getResources()
				.getDimension(R.dimen.px10);
		this.marginTop = (int) context.getResources().getDimension(
				R.dimen.px215);

		formatList();

	}

	@Override
	public int getCount() {
		return pageList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		View pageView = null;

		try {
			pageView = createView(pageList.get(position), position + 1);
			((ViewPager) container).addView(pageView);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return pageView;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	private void formatList() {

		List<AppInfo> page = new ArrayList<AppInfo>();
		for (int i = 0; i < appList.size(); i++) {
			page.add(appList.get(i));
			if (page.size() >= 9) {
				pageList.add(page);
				page = new ArrayList<AppInfo>();

			} else if (i == (appList.size() - 1)) {
				pageList.add(page);
				break;
			}

		}

		pageCount = pageList.size();
	}

	private RelativeLayout createView(List<AppInfo> imageList, final int pageNum) {

		RelativeLayout page = new RelativeLayout(context);
		ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		page.setLayoutParams(vlp);
		page.setClipChildren(false);
		page.setClipToPadding(false);

		for (int i = 0; i < imageList.size(); i++) {
			TvRelativeLayout item = (TvRelativeLayout) mInflater.inflate(
					R.layout.activity_uninstall_item, null);

			int vId = pageNum * 10 + i;
			item.setId(vId);

			ImageView iv_icon = (ImageView) item.findViewById(R.id.iv_icon);
			TextView tv_title = (TextView) item.findViewById(R.id.tv_title);
			TextView tv_version = (TextView) item.findViewById(R.id.tv_version);
			final TextView tv_size = (TextView) item.findViewById(R.id.tv_size);

			final AppInfo app = imageList.get(i);
			iv_icon.setBackgroundDrawable(DataUtil.readIconByPkg(context,
					app.getPackagename()));
			tv_title.setText(app.getAppname());
			tv_version.setText(app.getVersionName());

			try {
				new CacheHelper(context).queryPacakgeSize(app.getPackagename(),
						new SyncCallBack() {

							@Override
							public void onDataLoaded(long codesize,
									long dataSize, long cacheSize) {
								tv_size.setText(DataUtil.parseApkSize(codesize
										+ dataSize + cacheSize));

							}

						});
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			if (i == 0) {
				RelativeLayout.LayoutParams appLpFirst = new RelativeLayout.LayoutParams(
						width, height);
				appLpFirst.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpFirst.setMargins(margin, marginTop, 0, 0);
				page.addView(item, appLpFirst);
			} else if (i < col) {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.setMargins(spaceHori, marginTop, 0, 0);
				page.addView(item, appLpRight);
			} else if (i % col == 0) {

				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.BELOW, vId - col);
				appLpRight.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				appLpRight.setMargins(margin, spaceVert, 0, 0);
				page.addView(item, appLpRight);
			} else {
				RelativeLayout.LayoutParams appLpRight = new RelativeLayout.LayoutParams(
						width, height);
				appLpRight.addRule(RelativeLayout.BELOW, vId - col);
				appLpRight.addRule(RelativeLayout.RIGHT_OF, vId - 1);
				appLpRight.setMargins(spaceHori, spaceVert, 0, 0);
				page.addView(item, appLpRight);
			}

			item.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(final View v) {
					CommonUtil.uninstallApk(context, app.getPackagename());
					final Handler handler = new Handler() {
						public void handleMessage(Message msg) {
							// 刷新界面
							v.setClickable(false);
							v.setAlpha(0.3f);
						};
					};
					new Timer().schedule(new TimerTask() {

						@Override
						public void run() {
							if (v.getVisibility() == View.VISIBLE) {
								if (!CommonUtil.isAppInstalled(context,
										app.getPackagename())) {
									Message msg = handler.obtainMessage();
									msg.sendToTarget();
									cancel();
								}
							}
						}
					}, 1000, 1000);

				}
			});

			item.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View childView, int arg1, KeyEvent event) {
					int focusedId = childView.getId();

					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

						if (focusedId % 10 == 2) {
							try {
								UninstallActivity.mainView.findViewById(
										(pageNum + 1) * 10).requestFocus();
							} catch (Exception e) {
								e.printStackTrace();
							}

						}
						if (focusedId % 10 == 5) {
							try {
								UninstallActivity.mainView.findViewById(
										(pageNum + 1) * 10 + 3).requestFocus();
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					}

					if (event.getAction() == KeyEvent.ACTION_DOWN
							&& event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT
							&& focusedId > 11) {

						if (focusedId % 10 == 0 || focusedId % 10 == 3) {

							try {
								UninstallActivity.mainView.findViewById(
										childView.getId() - 8).requestFocus();
							} catch (Exception e) {
							}
						}

					}

					return false;
				}
			});

		}

		return page;
	}

}
