package com.devy.service_manager;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.devy.droidipc.LogControler;
import com.devy.droidipc.ServiceManagerThread;

public class ServiceManagerApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		LogControler.enableDebug();
		ServiceManagerThread.attachSystem(base);
		Log.i(LogControler.TAG, "[service manager] attach system successed");
		super.attachBaseContext(base);
	}
	
}
