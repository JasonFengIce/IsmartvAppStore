package com.boxmate.tv.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvBitmap;
import reco.frame.tv.view.TvButton;

import com.boxmate.tv.LauncherActivity;
import com.boxmate.tv.R;
import com.boxmate.tv.adapter.FragmentAdapter;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.ui.home.DownloadListActivity;
import com.boxmate.tv.ui.home.HomeFragment;
import com.boxmate.tv.ui.home.SearchActivity;
import com.boxmate.tv.ui.manage.AppUpdateActivity;
import com.boxmate.tv.ui.manage.ManageFragment;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.FocusScaleShineButton;
import com.boxmate.tv.view.FragViewPager;
import com.umeng.analytics.MobclickAgent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.RelativeLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnClickListener;

public class MainActivity extends FragmentActivity {

	private final static int FLUSH_DOWNLOAD_SUCCESS=0;
	private final static int FLUSH_UPDATE_SUCCESS=1;
	public static int updateListSize,downloadListSize;
	private int curPage = 0;
	private Boolean exitConfirm = false;
	public List<Fragment> fragmentList = new ArrayList<Fragment>();
	private FragViewPager vp_frag_list;
	public static TvBitmap tb;
	private TvButton tb_download,tb_update;
	private int topHintHeight,topHintWidth,topHintMarginRight_A,topHintMarginRight_B;
	private Boolean isRestart=false;
	
	private Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch (msg.what) {
			case FLUSH_DOWNLOAD_SUCCESS:
				downloadListSize=(Integer) msg.obj;
				if (downloadListSize>0) {
					tb_download.setVisibility(View.VISIBLE);
					tb_download.setText("下载 "+downloadListSize);
					if (tb_update.getVisibility()==View.VISIBLE) {
						RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(topHintWidth,topHintHeight);
						rlp.setMargins(0, 0,topHintMarginRight_B,0);
						rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
						rlp.addRule(RelativeLayout.CENTER_VERTICAL);
						tb_update.setLayoutParams(rlp);
						tb_download.clearFocus();
						tb_update.clearFocus();
						//tb_update.requestFocus();
						
					}
				}else{
					if (tb_update.getVisibility()==View.VISIBLE) {
						RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(
								topHintWidth, topHintHeight);
						rlp.setMargins(0, 0,topHintMarginRight_A,0);
						rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
						rlp.addRule(RelativeLayout.CENTER_VERTICAL);
						tb_update.setLayoutParams(rlp);
					}
					tb_download.setVisibility(View.GONE);
				}
				tb_download.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View btn) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MainActivity.this,
								DownloadListActivity.class);
						startActivity(intent);
						
					}
				});
				tb_update.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View btn) {
						Intent intent = new Intent(MainActivity.this,
								AppUpdateActivity.class);
						startActivity(intent);
					}
				});
				break;
			case FLUSH_UPDATE_SUCCESS:
				HashMap<String, String> infHashMap = (HashMap<String, String>) msg.obj;

				String api = Config.appBase
						+ "action=get_app_list_by_package_names";
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("packages", infHashMap
						.get("package")));
				params.add(new BasicNameValuePair("versions", infHashMap
						.get("version")));

				HttpCommon.postApi(api, params, new HttpSuccessInterface() {

					@Override
					public void run(String result) {
						JSONArray updateJsonArray;
						try {
							updateJsonArray = new JSONArray(result);

							ArrayList<HashMap<String, String>> updateList = new ArrayList<HashMap<String, String>>();

							for (int i = 0; i < updateJsonArray.length(); i++) {
								JSONObject appJson = (JSONObject) updateJsonArray
										.opt(i);
								HashMap<String, String> appData = new HashMap<String, String>();
								appData.put("title", appJson.getString("title"));

								appData.put("package",
										appJson.getString("package"));
								updateList.add(appData);
							}
							updateListSize=updateList.size();
							if (updateListSize > 0) {
								
								tb_update.setVisibility(View.VISIBLE);
								tb_update.setText("更新 "+updateListSize);
								if (tb_download.getVisibility()==View.VISIBLE) {
									RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(topHintWidth, topHintHeight);
									rlp.setMargins(0, 0,topHintMarginRight_B,0);
									rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
									rlp.addRule(RelativeLayout.CENTER_VERTICAL);
									tb_update.setLayoutParams(rlp);
								}else{
									RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(topHintWidth, topHintHeight);
									rlp.setMargins(0, 0,topHintMarginRight_A,0);
									rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
									rlp.addRule(RelativeLayout.CENTER_VERTICAL);
									tb_update.setLayoutParams(rlp);
								}
							} else {
								tb_update.setVisibility(View.GONE);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
				tb_download.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View btn) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(MainActivity.this,
								DownloadListActivity.class);
						startActivity(intent);
						
					}
				});
				tb_update.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View btn) {
						Intent intent = new Intent(MainActivity.this,
								AppUpdateActivity.class);
						startActivity(intent);
					}
				});
				break;

			}
		};
	};
	

	public interface HomeFragmentInterface {
		public void setPage(int page);

		public int getPage();

		public void loadData();

		public void focusControl(int direction);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState!=null) {
			isRestart=true;
		}
		
		tb = TvBitmap.create(getApplicationContext());
		//tb.configLoadingImage(R.drawable.image_loading_center);
		setContentView(R.layout.activity_main);

		vp_frag_list = (FragViewPager) findViewById(R.id.main_container);
		tb_download=(TvButton) findViewById(R.id.tb_download);
		tb_update=(TvButton) findViewById(R.id.tb_update);

		curPage = 0;
		exitConfirm = false;
		topHintWidth= (int)getResources().getDimension(R.dimen.px114);
		topHintHeight=(int)getResources().getDimension(R.dimen.px42);
		topHintMarginRight_A=(int)getResources().getDimension(R.dimen.px10);
		topHintMarginRight_B=(int)getResources().getDimension(R.dimen.px144);
		
		
		if (fragmentList.size() < 1) {
			for (int i = 0; i < Config.PAGE_COUNT; i++) {
				Fragment mTextFragmentOne = getFragmentByPage(i);
				fragmentList.add(mTextFragmentOne);
			}
			vp_frag_list.setAdapter(new FragmentAdapter(
					getSupportFragmentManager(), fragmentList));
			vp_frag_list.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int page) {

					if (findViewById(R.id.home_bottom_for_focus).isFocused()||isRestart) {
						return;
					}

					if (curPage < page) {// 向右翻
						((MainActivity.HomeFragmentInterface) fragmentList
								.get(page)).focusControl(1);
					} else {// 向左
						((MainActivity.HomeFragmentInterface) fragmentList
								.get(page)).focusControl(2);
					}
					mainFocusShow(page, false);

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {

				}

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub

				}
			});


			findViewById(R.id.home_bottom_for_focus).setOnFocusChangeListener(
					new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View arg0, boolean isfocus) {
							// TODO Auto-generated method stub
							if (isfocus) {
								getButtonByPage(curPage).focus(true);
							} else {
								getButtonByPage(curPage).focus(false);
							}
						}
					});

			tb_download.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View btn) {
							// TODO Auto-generated method stub
							Intent intent = new Intent(MainActivity.this,
									DownloadListActivity.class);
							startActivity(intent);
							
						}
					});
			tb_update.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View btn) {
					Intent intent = new Intent(MainActivity.this,
							AppUpdateActivity.class);
					startActivity(intent);
				}
			});
			
			//获取意外销毁时所保存数据
			if (isRestart) {
				int saveCurPage=savedInstanceState.getInt("curPage");
				if (saveCurPage!=0) {
					curPage=saveCurPage;
					//去除多个选中
					getButtonByPage(0).setTextColor(
							getResources().getColor(R.color.MainNavColorNormal));
					getButtonByPage(curPage).setTextColor(
							getResources().getColor(R.color.MainNavColorFocus));
				}
				isRestart=false;
				
			} else { //第次一初始化 MainActivity
				if (Config.PAGE_INDEX==Config.PAGE_HOME) { //如果是首页
					CommonUtil.checkUpdate(this);  //检查更新
					mainFocusShow(Config.PAGE_HOME);
					CommonUtil.saveWifiName(getApplicationContext());
				}
			}
		}
	}
	@Override
	protected void onStart() {
		if (Config.PAGE_INDEX!=1) {
			mainFocusShow(Config.PAGE_INDEX);//进入指定页
			Config.PAGE_INDEX=Config.PAGE_HOME;
		}
		
		super.onStart();
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("curPage", curPage);
		super.onSaveInstanceState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	public boolean onKeyDown(int kCode, KeyEvent kEvent) {

		if (kCode != KeyEvent.KEYCODE_BACK) {
			exitConfirm = false;
		}

		switch (kCode) {

		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (findViewById(R.id.home_bottom_for_focus).isFocused()) {
				int toPage = curPage - 1;
				if (toPage >= 0) {
					mainFocusShow(toPage);
				}
				return true;
			} else {
				break;
			}
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (findViewById(R.id.home_bottom_for_focus).isFocused()) {
				int toPage = curPage + 1;
				if (toPage < Config.PAGE_COUNT) {
					mainFocusShow(toPage);
				} else {
					if (tb_update.getVisibility()==View.VISIBLE) {
						tb_update.requestFocus();
					}else if(tb_download.getVisibility()==View.VISIBLE){
						tb_download.requestFocus();
					}
				}
				return true;
			} else {
				break;
			}
		case KeyEvent.KEYCODE_BACK:
			if (curPage != 1) {
				findViewById(R.id.home_bottom_for_focus).requestFocus();
				mainFocusShow(Config.PAGE_HOME);
				return true;
			} else {
				if (!exitConfirm) {
					CommonUtil.toast(getApplicationContext(), getResources()
							.getString(R.string.hint_exit));
					exitConfirm = true;
					return true;
				} else {
					// MainActivity.this.finish();
					// System.exit(0);
					break;
				}
			}
		case KeyEvent.KEYCODE_MENU:
			Intent intent = new Intent(this, SearchActivity.class);
			startActivity(intent);
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (findViewById(R.id.home_bottom_for_focus).isFocused()) {
				((MainActivity.HomeFragmentInterface) fragmentList.get(curPage))
						.focusControl(0);
				return true;
			} else {
				break;
			}

		}
		return super.onKeyDown(kCode, kEvent);
	}

	private FocusScaleShineButton getButtonByPage(int page) {
		int homeIds[] = { R.id.main_manage, R.id.main_home, R.id.main_game, R.id.main_app };
		return (FocusScaleShineButton) findViewById(homeIds[page]);
	}

	private void mainFocusShow(int page) {
		mainFocusShow(page, true);
	}

	private void mainFocusShow(int page, Boolean focus) {
		if (page == 0) {
			((ManageFragment) fragmentList.get(page)).flush();
		}
		if (focus) {
			getButtonByPage(curPage).focus(false);
			getButtonByPage(page).focus(true);
		}
		
		//显示左边提示
		if (page==Config.PAGE_HOME) {
			findViewById(R.id.iv_tools_hint).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.iv_tools_hint).setVisibility(View.GONE);
		}

		vp_frag_list.setCurrentItem(page);
		getButtonByPage(curPage).setTextColor(
				getResources().getColor(R.color.MainNavColorNormal));
		getButtonByPage(page).setTextColor(
				getResources().getColor(R.color.MainNavColorFocus));
		curPage = page;
	}

	private Fragment getFragmentByPage(int page) {
		Fragment fg = null;
		if (page == 1) { // 当前页是主页
			fg = new HomeFragment();
		} else if (page == 3) {
			fg = new AppFragment();
		} else if (page == 2) {
			fg = new GameFragment();
		} else if (page == 0) {
			fg = new ManageFragment();
		} else {
			fg = new HomeFragment();
		}
		((MainActivity.HomeFragmentInterface) fg).setPage(page);
		return fg;
	}

	protected void onResume() {
		// 进程遭强杀后返回，防数据库路径为空
		if (LauncherActivity.dbFilePath == null) {
			LauncherActivity.dbFilePath = getFilesDir().getAbsolutePath();
		}
		//刷新界面
		reflesh();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	
	private void reflesh(){
		new Thread(){
			public void run() {
				AppDownloadManager manager = AppDownloadManager.getInstance();
				int num = manager.getTaskInfoCount();
				Message msg=mHandler.obtainMessage();
				msg.what=FLUSH_DOWNLOAD_SUCCESS;
				msg.obj=num;
				mHandler.sendMessage(msg);
			};
		}.start();
		freshUpdate();
		
	}
	
	public void freshUpdate() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					List<PackageInfo> packages = getPackageManager()
							.getInstalledPackages(0);
					String packagesString = "";
					String versionCodeString = "";
					for (int i = 0; i < packages.size(); i++) {
						PackageInfo packageInfo = packages.get(i);

						if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
							packagesString += packageInfo.packageName + "|";
							versionCodeString += packageInfo.versionCode + "|";
						}
					}

					HashMap<String, String> infoHashMap = new HashMap<String, String>();
					infoHashMap.put("package", packagesString);
					infoHashMap.put("version", versionCodeString);
					Message msg = mHandler.obtainMessage();
					msg.what = FLUSH_UPDATE_SUCCESS;
					msg.obj = infoHashMap;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}).start();
	}
}
