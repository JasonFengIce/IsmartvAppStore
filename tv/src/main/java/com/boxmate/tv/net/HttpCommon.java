package com.boxmate.tv.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.boxmate.tv.entity.Config;
import com.boxmate.tv.util.CommonUtil;

import android.R.string;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;




public class HttpCommon {
	
	
	static public HttpGet getApi(final String api,final HttpSuccessInterface r){
		final Handler loadedHandler = new Handler(){
			public void handleMessage(Message msg){
				r.run(msg.obj.toString());
			}
		};
		final HttpClient httpClient = new DefaultHttpClient();
        final HttpGet request = new HttpGet(api);
        request.addHeader("Cookie", buildCommonCookie());
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        
		        try{
		            HttpResponse response = httpClient.execute(request);
		            String result = EntityUtils.toString(response.getEntity());
		            Message msg = loadedHandler.obtainMessage();
		            msg.obj = result;
		            msg.sendToTarget();
		        } catch (ClientProtocolException e) {
		        	
		        	
		        	
		            e.printStackTrace();
		            return;
		        } catch (IOException e) {
		            e.printStackTrace();
		            return;
		        }
			}
		}).start();
		return request;
	}
	
	static public HttpPost postApi(final String api,final List<NameValuePair> params,final HttpSuccessInterface r){
		final Handler loadedHandler = new Handler(){
			public void handleMessage(Message msg){
				r.run(msg.obj.toString());
			}
		};
		final HttpClient httpClient = new DefaultHttpClient();
		final HttpPost request = new HttpPost(api);
		request.addHeader("Cookie", buildCommonCookie());
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				
		        try{
		        	request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		            HttpResponse response = httpClient.execute(request);
		            String result = EntityUtils.toString(response.getEntity());
		            Message msg = loadedHandler.obtainMessage();
		            msg.obj = result;
		            msg.sendToTarget();
		        } catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
		        } catch (ClientProtocolException e) {
		            e.printStackTrace();
		            return;
		        } catch (IOException e) {
		            e.printStackTrace();
		            return;
		        } 
			}
		}).start();
		return request;
	}
	static public String buildCommonCookie(){
		String packageName  = "";
		if(CommonUtil.context!=null) {
			packageName = CommonUtil.context.getPackageName();
		}
		//version
		String cookie = "";
		try {
			cookie = String.format("version=%d; channel=%s; guid=%s; device=%s; package=%s",
					CommonUtil.getVersion(), CommonUtil.getChannel(),
					CommonUtil.getGuid(),URLEncoder.encode(CommonUtil.getDeviceName(),"utf-8"),packageName);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.i("cookie",cookie);
		return cookie;
		
	}
	static public String buildApiUrl(HashMap<String, String> params) {
		String urlString = Config.appBase;
		for(HashMap.Entry<String, String> entry:params.entrySet()){  
			
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			try {
				urlString = urlString+key+"="+URLEncoder.encode(value,"utf-8")+"&";
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return urlString;
	}
}

