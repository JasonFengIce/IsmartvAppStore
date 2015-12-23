package com.boxmate.tv.entity;

import com.boxmate.tv.net.CrashHandler;
import com.boxmate.tv.util.CommonUtil;

import android.app.Application;

public class Bomate extends Application {  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        CommonUtil.context = this;
        CrashHandler crashHandler = CrashHandler.getInstance();  
        crashHandler.init(this);  
    }  
}