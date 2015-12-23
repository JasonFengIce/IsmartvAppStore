package com.boxmate.tv.ui;



import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


public class AppDescriptionFragment extends Fragment implements AppDetail.OnPressOkListener {
	private String desc;
	private TextView textView;
	private LinearLayout appView;
	private int scrolled = 0;
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		appView =  (LinearLayout)inflater.inflate(R.layout.fragment_app_desc, container,false);
		textView = (TextView)appView.findViewById(R.id.desc_textview);
		textView.setText(this.desc);
		return appView;
	}
	
	
	
	@Override
	public void onPress() {
		// TODO Auto-generated method stub
		//goNextPage();
		int scrollHeight = appView.getHeight()/2;
		
		int txtHeight = textView.getHeight();
		int cHeight = appView.getHeight();
		
		Log.i("rect","h:"+txtHeight+"c:"+cHeight+"ed:"+scrolled);
		
		ScrollView scrollView =  (ScrollView)appView.findViewById(R.id.desc_textview_scroll);
		
		
		
		
		if((txtHeight-scrolled)>cHeight) {
			
			scrollHeight = (txtHeight-scrollHeight)>scrollHeight?scrollHeight:(txtHeight-scrollHeight);
			((ScrollView)appView.findViewById(R.id.desc_textview_scroll)).scrollBy(0, scrollHeight);
			scrolled += scrollHeight;
			
		} else {
			
			scrolled = 0;
			((ScrollView)appView.findViewById(R.id.desc_textview_scroll)).scrollTo(0, 0);
		}
		
		
	}
	@Override
	public void onFocusChange(Boolean isFocus) {
		// TODO Auto-generated method stub
		if(isFocus) {
		} else {
		}
	}
	
}
