package com.boxmate.tv.ui;


import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import reco.frame.tv.TvBitmap;


import android.app.Fragment;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import android.widget.TextView;

import com.boxmate.tv.R;
import com.boxmate.tv.R.drawable;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.net.HttpCommon;
import com.boxmate.tv.net.HttpSuccessInterface;
import com.boxmate.tv.view.WebImageView;

public class SubjectBlockFragment extends Fragment {
	
	public String nameId;
	private View mainView;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mainView = inflater.inflate(R.layout.fragment_subject_block, container,false);
		loadSubject();
		return mainView;
	}
	public void loadSubject(){
		//获取主页ad
        HashMap<String, String> params = new HashMap<String, String>();
		params.put("action", "get_subject_by_name_id");
		params.put("name_id", nameId);
		String api = HttpCommon.buildApiUrl(params);
		
		Log.i("api fuck",api);
		HttpCommon.getApi(api, new HttpSuccessInterface() {
			@Override
			public void run(String result) {
				try {
					final JSONObject subject = new JSONObject(result);
					((TextView) mainView.findViewById(
							R.id.subject_block_title)).setText(subject
							.getString("title"));
					
					WebImageView webImageView=((WebImageView) mainView.findViewById(
							R.id.subject_block_image));
							TvBitmap.create(getActivity()).display(webImageView, subject.getString("icon_url"));
					
					if((View) mainView.getParent()==null) {
						return ;
					}
					
					((View) mainView.getParent())
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									try {
										// TODO Auto-generated method stub
										Intent intent = new Intent(getActivity(),
												AppListActivity.class);
										intent.putExtra("title", subject.getString("title"));
										intent.putExtra("action",
												"get_app_list_by_subject_name_id&name_id="+nameId);
										intent.putExtra("rank_type", RankType.RANK);
										intent.putExtra("isSubject", 1);
										startActivity(intent);
									} catch (Exception e) {
										// TODO: handle exception
									}
								}
							});
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
}
