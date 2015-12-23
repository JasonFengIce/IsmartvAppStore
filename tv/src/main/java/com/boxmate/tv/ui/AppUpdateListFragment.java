package com.boxmate.tv.ui;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

import reco.frame.tv.TvBitmap;
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
import com.boxmate.tv.R.string;
import com.boxmate.tv.entity.TaskInfo;
import com.boxmate.tv.net.AppDownloadManager;
import com.boxmate.tv.util.CommonUtil;
import com.boxmate.tv.util.DataUtil;
import com.boxmate.tv.view.FocusScaleFrame;
import com.boxmate.tv.view.WebImageView;
public class AppUpdateListFragment extends Fragment  {
	private ArrayList<HashMap<String, Object>> appList = new ArrayList<HashMap<String, Object>>();
	private HashMap<String, TextView> pTexViews = new HashMap<String, TextView>();
	private int page=0;
	
	public interface OnAppListener{
		public void onAppClick(int page,int position);
		public void onAppFocus(int page,int position);
		public void setFirstApp(FocusScaleFrame frame);
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	public void setList(ArrayList<HashMap<String, Object>> list) {
		this.appList = list;
	}
	
	public void updateStatus() {
		for(HashMap.Entry<String, TextView> entry:pTexViews.entrySet()){  
			
			String packageName = (String)entry.getKey();
			TextView textView = (TextView)entry.getValue();
			AppDownloadManager manager = AppDownloadManager.getInstance();		     
		    TaskInfo taskInfo = manager.getTaskInfoByPackageName(packageName);
		    
		    if(taskInfo==null) {
		    	textView.setText("");
		    } else {
		    	if(taskInfo.status==TaskInfo.WAITING || taskInfo.status==TaskInfo.RUNNING) {
		    		textView.setText(getActivity().getResources().getString(R.string.updateing));
		    	} else {
					textView.setText("");
				}
		    }
		}   
	}
	
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		RelativeLayout appView =  (RelativeLayout)inflater.inflate(R.layout.fragment_app_update_list, container,false);
		
		
			
		for(int i=0;i<3;i++) {
			for(int j=0;j<3;j++) {
				int index = i*3+j;
				if(index>=appList.size()) {
					break;
				}
				
				FocusScaleFrame frame = (FocusScaleFrame) inflater.inflate(R.layout.activity_update_list_item,null);
				
				frame.setTag(index);
				frame.setId(index+10);
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
						((OnAppListener)getActivity()).onAppClick(page, (Integer)arg0.getTag());
					}
				});
				
				
				
				int boxWidth = (int)getActivity().getResources().getDimension(R.dimen.px502);
				int boxHeight =(int)getActivity().getResources().getDimension(R.dimen.px234);
				
				
				int topOffset =(int)getActivity().getResources().getDimension(R.dimen.px50);
				int leftOffset =(int)getActivity().getResources().getDimension(R.dimen.px90);
				
				
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) getActivity().getResources().getDimension(
						R.dimen.px492), (int) getActivity()
						.getResources().getDimension(R.dimen.px224));
				
				
				params.setMargins(j * boxWidth + leftOffset, i * boxHeight
						+ topOffset, 0, 0);
				frame.setLayoutParams(params);
				
				
				HashMap<String, Object>appInfo = appList.get(index);
				
				
				WebImageView iconImageView = (WebImageView) frame.findViewById(R.id.app_icon);
				TvBitmap.create(getActivity()).display(iconImageView, appInfo.get("icon_url").toString());
				
				TextView titleTextView = (TextView) frame.findViewById(R.id.app_title);
				titleTextView.setText(appInfo.get("title").toString());
				
				
				TextView versionTextView = (TextView) frame.findViewById(R.id.app_version_change);
				versionTextView.setText(CommonUtil.getVersionInfoPkg(getActivity(), appInfo.get("package").toString()).getVersionName());
				
				TextView sizeTextView = (TextView) frame.findViewById(R.id.app_size);
				sizeTextView.setText(getActivity().getResources().getString(R.string.prefix_upgrade)
						+appInfo.get("version_name").toString());
				
				
				pTexViews.put(appInfo.get("package").toString(), (TextView)frame.findViewById(R.id.app_size));
				
				
				appView.addView(frame);
				
				if((page+i+j)==0) {
					((OnAppListener)getActivity()).setFirstApp(frame);
				}
			}
		}
		
		return appView;
	}
}






