package com.boxmate.tv.ui.manage;

import java.util.HashMap;

import com.boxmate.tv.R;
import com.boxmate.tv.ui.MainActivity;
import com.boxmate.tv.ui.tool.ToolsApkManage;
import com.boxmate.tv.ui.tool.ToolsBigFileManage;
import com.boxmate.tv.ui.tool.ToolsFlush;
import com.boxmate.tv.ui.tool.ToolsGabageClear;
import com.boxmate.tv.ui.tool.ToolsSpeed;
import com.boxmate.tv.ui.tool.ToolsSystem;
import com.boxmate.tv.view.FocusScaleButton;
import com.boxmate.tv.view.FocusScaleButton;
import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

public class ManageFragment extends Fragment implements
		MainActivity.HomeFragmentInterface {

	private final static String TAG = "ManageFragment";
	public int page;
	private boolean flushFlag = false;
	private int[] imageIds = { R.drawable.manage_flush,
			R.drawable.manage_uninstall, R.drawable.manage_apk,
			R.drawable.manage_bigfile, R.drawable.manage_gc,
			R.drawable.manage_update, R.drawable.manage_system,
			R.drawable.manage_setting };
	private int[] viewIds = { R.id.fb_flush, R.id.fb_uninstall, R.id.fb_apk,
			R.id.fb_bigfile, R.id.fb_gc, R.id.fb_update, R.id.fb_system,
			R.id.fb_setting };
	private boolean loadFlag = true;

	public interface HomeFragmentCreate {
		public void setHomeFirstFocusButton(Button button);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		manageView = inflater.inflate(R.layout.fragment_manage, container,
				false);

		FocusScaleButton fb_flush = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_flush));
		FocusScaleButton fb_apk = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_apk));
		FocusScaleButton fb_bigfile = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_bigfile));
		FocusScaleButton fb_gc = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_gc));
		FocusScaleButton fb_system = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_system));
		FocusScaleButton fb_uninstall = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_uninstall));
		FocusScaleButton fb_update = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_update));
		FocusScaleButton fb_setting = ((FocusScaleButton) manageView
				.findViewById(R.id.fb_setting));

		fb_flush.page = page;
		fb_apk.page = page;
		fb_bigfile.page = page;
		fb_gc.page = page;
		fb_system.page = page;
		fb_uninstall.page = page;
		fb_update.page = page;
		fb_setting.page = page;
		// 在page切换中监听 添加图片 及 回收图片

		fb_flush.setOnClickListener(mClickListener);
		fb_apk.setOnClickListener(mClickListener);
		fb_bigfile.setOnClickListener(mClickListener);
		fb_gc.setOnClickListener(mClickListener);
		fb_system.setOnClickListener(mClickListener);
		fb_uninstall.setOnClickListener(mClickListener);
		fb_update.setOnClickListener(mClickListener);
		fb_setting.setOnClickListener(mClickListener);

		loadData();

		return manageView;
	}

	public void initAd(int rid, HashMap<String, String> adDetail) {

	}

	@Override
	public void onStart() {

		if (flushFlag) {
			flush();
			flushFlag = false;
		}
		super.onStart();
	}

	public void flush() {

	}

	@Override
	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public int getPage() {
		return page;
	}

	private View manageView;

	@Override
	public void loadData() {

		if (loadFlag) {
			loadFlag = false;
			for (int i = 0; i < viewIds.length; i++) {
				manageView.findViewById(viewIds[i]).setBackgroundResource(
						imageIds[i]);

				// manageView.findViewById(viewIds[i]).setOnKeyListener(
				// new OnKeyListener() {
				//
				// @Override
				// public boolean onKey(View arg0, int arg1,
				// KeyEvent event) {
				// if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
				//
				// return true;
				// }
				// return false;
				// }
				// });
			}
		}

	}

	@Override
	public void focusControl(int direction) {
		if (manageView == null)
			return;
		switch (direction) {
		case 0:
			manageView.findViewById(R.id.fb_flush).requestFocus();
			break;
		case 1:
			manageView.findViewById(R.id.fb_flush).requestFocus();
			break;
		case 2:
			manageView.findViewById(R.id.fb_bigfile).requestFocus();
			break;
		}

	}

	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.fb_flush:
				startActivity(new Intent(getActivity(), ToolsFlush.class));
				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_tool_flush_click));
				} catch (Exception e) {
				}
				break;
			case R.id.fb_apk:
				startActivity(new Intent(getActivity(), ToolsApkManage.class));
				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_tool_apk_click));
				} catch (Exception e) {
				}
				break;
			case R.id.fb_bigfile:
				startActivity(new Intent(getActivity(), ToolsBigFileManage.class));

				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_tool_bigfile_click));
				} catch (Exception e) {
				}
				break;
			case R.id.fb_gc:
				startActivity(new Intent(getActivity(), ToolsGabageClear.class));
				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_tool_gabage_click));
				} catch (Exception e) {
				}
				break;
			case R.id.fb_system:
				startActivity(new Intent(getActivity(), ToolsSystem.class));
				try {
					// 统计点击
					MobclickAgent.onEvent(
							getActivity(),
							getActivity().getResources().getString(
									R.string.count_tool_system_click));
				} catch (Exception e) {
				}
				break;
			case R.id.fb_uninstall:
				startActivity(new Intent(getActivity(), UninstallActivity.class));
				break;

			case R.id.fb_update:
				flushFlag = true;
				if (MainActivity.updateListSize>0) {
					startActivity(new Intent(getActivity(), AppUpdateActivity.class));
				}else{
					Toast.makeText(getActivity(), getResources().getString(R.string.toast_no_update), 0).show();
				}
				
				break;
			case R.id.fb_setting:
				startActivity(new Intent(getActivity(), SettingActivity.class));
				break;

			}

		}
	};
}
