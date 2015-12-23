package com.boxmate.tv.net;

  
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.boxmate.tv.entity.Config;

import android.content.Context;  
import android.content.pm.PackageInfo;  
import android.content.pm.PackageManager;  
import android.content.pm.PackageManager.NameNotFoundException;  
import android.os.Build;  
import android.os.Looper;  
import android.widget.Toast;  
/** 
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告. 
 *  
 *  
 */  
public class CrashHandler implements UncaughtExceptionHandler {  
    private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类  
    private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler实例  
    private Context mContext;// 程序的Context对象  
    private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息  
    
  
    /** 保证只有一个CrashHandler实例 */  
    private CrashHandler() {  
  
    }  
  
    /** 获取CrashHandler实例 ,单例模式 */  
    public static CrashHandler getInstance() {  
        return INSTANCE;  
    }  
  
    /** 
     * 初始化 
     *  
     * @param context 
     */  
    public void init(Context context) {  
        mContext = context;  
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器  
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器  
    }  
  
    /** 
     * 当UncaughtException发生时会转入该重写的方法来处理 
     */  
    public void uncaughtException(Thread thread, Throwable ex) {  
    	new Thread() {  
            public void run() {  
                Looper.prepare();  
                Toast.makeText(mContext, "哎呀，出问题了，如果意外退出喽，请主人一定要重新唤醒我噢！^_^", 0).show();  
                Looper.loop();  
            }  
        }.start();
        try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        handleException(ex);
    	mDefaultHandler.uncaughtException(thread, ex);  
    }  
  
    /** 
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 
     *  
     * @param ex 
     *            异常信息 
     * @return true 如果处理了该异常信息;否则返回false. 
     */  
    public boolean handleException(Throwable ex) {  

 
    	collectDeviceInfo(mContext);
        sendToServer(ex);
        return true;  
    }  
  
    /** 
     * 收集设备参数信息 
     *  
     * @param context 
     */  
    public void collectDeviceInfo(Context context) {  
        try {  
            PackageManager pm = context.getPackageManager();// 获得包管理器  
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),  
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity  
            if (pi != null) {  
                String versionName = pi.versionName == null ? "null"  
                        : pi.versionName;  
                String versionCode = pi.versionCode + "";  
                info.put("versionName", versionName);  
                info.put("versionCode", versionCode);  
                info.put("packageName", context.getPackageName());
            }  
        } catch (NameNotFoundException e) {  
            e.printStackTrace();  
        }  
        info.put("device", Build.MANUFACTURER+"-"+Build.MODEL);
    }  
  
    private String sendToServer(Throwable ex) {  
    	
    	
    	String api = Config.appBase
				+ "action=log_crash";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
        for (Map.Entry<String, String> entry : info.entrySet()) {  
            String key = entry.getKey();  
            String value = entry.getValue();  
            params.add(new BasicNameValuePair(key, value));
        }  
        
        Writer writer = new StringWriter();  
        PrintWriter pw = new PrintWriter(writer);  
        ex.printStackTrace(pw);  
        Throwable cause = ex.getCause();  
        // 循环着把所有的异常信息写入writer中  
        while (cause != null) {  
            cause.printStackTrace(pw);  
            cause = cause.getCause();  
        }  
        pw.close();// 记得关闭  
        String result = writer.toString();  
        params.add(new BasicNameValuePair("error", result));
        
        HttpCommon.postApi(api, params, new HttpSuccessInterface() {
			@Override
			public void run(String result) {
				// TODO Auto-generated method stub
				
			}
		});
        
        return null;  
    }  
}  