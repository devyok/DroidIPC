package com.devy.service_interfaces;

import android.os.IInterface;

import com.devy.droidipc.ServiceManager;
import com.devy.droidipc.exception.IPCServiceNotFoundException;

public final class IPC {
	
	public static synchronized void onAppReady(Context context){
	}
	
	public static final class Context {
		public final static String ACTIVITY_SERVICE = "activity_service";
		private Context(){}
	}
	
	public static IInterface proxyService(String name) throws IPCServiceNotFoundException {
		return ServiceManagerProxy.instance.getService(name);
	}
	
	static final class ServiceManagerProxy{
		
		private static ServiceManagerProxy instance = new ServiceManagerProxy();
		
		private ServiceManagerProxy(){}
		
		public IInterface getService(String name) throws IPCServiceNotFoundException {
			
			IInterface service = ServiceManager.getService(IPC.Context.ACTIVITY_SERVICE);
			
			if(Context.ACTIVITY_SERVICE.equals(name)) {
				IActivityServcie activityServcie = IActivityServiceProxy.asService((service));
				return activityServcie;
			}
			return service;
		}

	}
	
}
