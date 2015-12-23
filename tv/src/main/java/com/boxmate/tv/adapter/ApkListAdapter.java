package com.boxmate.tv.adapter;

import java.util.List;
import reco.frame.tv.view.component.TvBaseAdapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.boxmate.tv.R;
import com.boxmate.tv.entity.ApkInfo;
import com.boxmate.tv.util.CommonUtil;
public class ApkListAdapter extends TvBaseAdapter {
	
	private Context context;
    private LayoutInflater mInflater;
    private List<ApkInfo> apkList;
    private PackageManager pm;
    private int height;
	public ApkListAdapter(Context context,List<ApkInfo> apkList){
		this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.apkList=apkList;
        this.pm=context.getPackageManager();
        this.height=(int) context.getResources().getDimension(R.dimen.px88);
	}
	
	@Override
	public int getCount() {
		return apkList.size();
	}
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return apkList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	@Override
	public View getView(final int i, View view, ViewGroup arg2) {
		if (view == null) {
			view = mInflater.inflate(R.layout.tools_apk_item, null);
		}
		
		AbsListView.LayoutParams alp=new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, height);
		view.setLayoutParams(alp);
		
		final ApkInfo apk= apkList.get(i);
		final String apkPath=apk.getPath();
		ImageView iv_icon=(ImageView) view.findViewById(R.id.iv_icon);
		TextView tv_name=(TextView) view.findViewById(R.id.tv_name);
		TextView tv_version=(TextView) view.findViewById(R.id.tv_version);
		TextView tv_size=(TextView) view.findViewById(R.id.tv_size);
		tv_name.setText(apk.getName());
		tv_version.setText(apk.getVersion()+"版");
		tv_size.setText(apk.getSizeString());
		Drawable icon=loadIcon(apkPath);
		if (icon!=null) {
			iv_icon.setBackgroundDrawable(icon);
		}
		final Button tsb_delete=(Button) view.findViewById(R.id.tsb_delete);
		
		view.findViewById(R.id.tsb_install).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				CommonUtil.installApkByFilePath(context, apkPath);
			}
		});
		
		tsb_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CommonUtil.deleteFileByPath(context, apkPath);
				v.setAlpha(0.5F);
				v.setFocusable(false);
//				apkList.remove(apk);
//				notifyDataSetChanged();
				
			}
		});
		
		
		
	
		return view;
	}
	
	private Drawable loadIcon(String apkPath){
		
		try {
			PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			ApplicationInfo appInfo = pkgInfo.applicationInfo;
			if (appInfo!=null) {
				appInfo.sourceDir=apkPath;
				appInfo.publicSourceDir=apkPath;//此处以确保资源正确读取
				return pm.getApplicationIcon(appInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
		
	}

}
