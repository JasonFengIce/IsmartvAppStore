package com.boxmate.tv.ui;

import java.util.ArrayList;
import java.util.HashMap;

import reco.frame.tv.view.TvImageView;
import reco.frame.tv.view.TvRelativeLayout;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.boxmate.tv.R;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.view.StarList;

public class AppPageFragment extends Fragment {
	private ArrayList<HashMap<String, Object>> appList = new ArrayList<HashMap<String, Object>>();
	private int page = 0;

	public interface OnAppListener {
		public void onAppClick(int page, int position);

		public void onAppFocus(int page, int position);

		public void setFirstApp(TvRelativeLayout frame);
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setList(ArrayList<HashMap<String, Object>> list) {
		this.appList = list;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		RelativeLayout appView = (RelativeLayout) inflater.inflate(
				R.layout.fragment_app_list, container, false);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int index = i * 3 + j;
				if (index >= appList.size()) {
					break;
				}

				TvRelativeLayout frame = (TvRelativeLayout) inflater.inflate(
						R.layout.activity_app_list_item, null);

				frame.setTag(index);
				frame.setId(index + 10);
				frame.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						// TODO Auto-generated method stub
						if (arg1) {
							((OnAppListener) getActivity()).onAppFocus(page,
									(Integer) arg0.getTag());
						}
					}
				});
				frame.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						((OnAppListener) getActivity()).onAppClick(page,
								(Integer) arg0.getTag());
					}
				});

				int boxWidth = (int) getActivity().getResources().getDimension(
						R.dimen.px476);
				int boxHeight = (int) getActivity().getResources()
						.getDimension(R.dimen.px234);

				int topOffset = (int) getActivity().getResources()
						.getDimension(R.dimen.px50);
				int leftOffset = (int) getActivity().getResources()
						.getDimension(R.dimen.px90);

				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						(int) getActivity().getResources().getDimension(
								R.dimen.px466), (int) getActivity()
								.getResources().getDimension(R.dimen.px224));

				params.setMargins(j * boxWidth + leftOffset, i * boxHeight
						+ topOffset, 0, 0);
				frame.setLayoutParams(params);

				HashMap<String, Object> appInfo = appList.get(index);

				TvImageView tiv_icon = (TvImageView) frame
						.findViewById(R.id.tiv_icon);
				tiv_icon.configImageUrl(appInfo.get("icon_url").toString());

				TextView tv_title = (TextView) frame
						.findViewById(R.id.tv_title);
				tv_title.setText(appInfo.get("title").toString());

				TextView tv_controll = (TextView) frame
						.findViewById(R.id.tv_controll);
				tv_controll.setText(appInfo.get("cname").toString());

				StarList sl_star = (StarList) frame.findViewById(R.id.sl_star);

				sl_star.setStar(Integer
						.parseInt(appInfo.get("star").toString()));

				// 判断是否安装
				ImageView iv_installed = (ImageView) frame
						.findViewById(R.id.iv_installed);

				if (CommonUtil.checkPackageInstalled(appInfo.get("package")
						.toString())) {
					iv_installed.setVisibility(View.VISIBLE);
					iv_installed
							.setBackgroundResource(R.drawable.app_installed_cover);
				}

				appView.addView(frame);

				if ((page + i + j) == 0) {

					((OnAppListener) getActivity()).setFirstApp(frame);
				}
			}
		}

		return appView;
	}
}
