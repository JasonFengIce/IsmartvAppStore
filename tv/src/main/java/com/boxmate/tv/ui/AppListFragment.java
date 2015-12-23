package com.boxmate.tv.ui;


import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.boxmate.tv.R;
import com.boxmate.tv.R.dimen;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.entity.AppInfo;
import com.boxmate.tv.view.FocusScaleFrame;
import com.boxmate.tv.view.WebImageView;
public class AppListFragment extends Fragment {
	private ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
	private int page=0;
	
	public interface OnAppListener{
		public void onAppClick(int page,int position);
		public void onAppFocus(int page,int position);
		public void setFirstApp(FocusScaleFrame frame);
		public void setFrame(String pa,FocusScaleFrame appView);
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	public void setList(ArrayList<AppInfo> list) {
		this.appList = list;
	}
	
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		RelativeLayout appView =  (RelativeLayout)inflater.inflate(R.layout.fragment_app_list, container,false);
		
		
		
		
			
		for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				int index = i*3+j;
				if(index>=appList.size()) {
					break;
				}
				
				
				FocusScaleFrame frame = (FocusScaleFrame)inflater.inflate(R.layout.activity_uninstall_item, null);
				frame.setFocusable(true);
				frame.setTag(index);
				frame.setId(index+10);
				
				
				
				int boxWidth = (int)getActivity().getResources().getDimension(R.dimen.px476);
				int boxHeight =(int)getActivity().getResources().getDimension(R.dimen.px234);
				
				
				int topOffset =(int)getActivity().getResources().getDimension(R.dimen.px50);
				int leftOffset =(int)getActivity().getResources().getDimension(R.dimen.px90);
				
				
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						(int) getActivity().getResources().getDimension(
								R.dimen.px466), (int) getActivity()
								.getResources().getDimension(R.dimen.px224));
				
				
				params.setMargins(j * boxWidth + leftOffset, i * boxHeight
						+ topOffset, 0, 0);
				frame.setLayoutParams(params);
				
				AppInfo appInfo = (AppInfo)appList.get(index);
				
				
				WebImageView iconImageView = (WebImageView)frame.findViewById(R.id.app_icon);
				iconImageView.setImageDrawable(appInfo.appicon);
				
				TextView titleTextView = (TextView)frame.findViewById(R.id.app_title);
				titleTextView.setText(appInfo.appname);
				
				
				TextView versionTextView = (TextView)frame.findViewById(R.id.tv_title);
				versionTextView.setText(appInfo.versionName);
				
				
				frame.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View arg0, boolean arg1) {
						// TODO Auto-generated method stub
						if(arg1) {
							((OnAppListener)getActivity()).onAppFocus(page,(Integer)arg0.getTag());
						}
					}
				});
				
				frame.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						((OnAppListener)getActivity()).onAppClick(page,(Integer)arg0.getTag());
						
					}
				});
				
				((OnAppListener)getActivity()).setFrame(appInfo.packagename, frame);
				
				appView.addView(frame);
				
				
				if((page+i+j)==0) {
					((OnAppListener)getActivity()).setFirstApp(frame);
				}
			}
		}
		
		
		return appView;
	}
}
