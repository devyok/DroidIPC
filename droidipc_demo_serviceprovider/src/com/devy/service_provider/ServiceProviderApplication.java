package com.devy.service_provider;

import android.app.Application;
import android.util.Log;

import com.devy.droidipc.LogControler;
import com.devy.droidipc.ServiceManager;
import com.devy.service_interfaces.IPC;

public class ServiceProviderApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(LogControler.TAG, "[service provider] attach system successed");
		ServiceManager.init(getApplicationContext());
		ServiceManager.addService(IPC.Context.ACTIVITY_SERVICE, new ActivityServiceProvider());
	}
	
}
