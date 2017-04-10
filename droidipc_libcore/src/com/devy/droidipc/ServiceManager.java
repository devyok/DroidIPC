package com.devy.droidipc;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.text.TextUtils;

import com.devy.droidipc.LogControler.Level;
import com.devy.droidipc.ServiceManagerImpl.OnQueryResultHandler;
import com.devy.droidipc.exception.IPCServiceNotFoundException;
import com.devy.droidipc.exception.IPCTimeoutException;

public final class ServiceManager {

	private ServiceManager(){}
	
	public static void init(Context context){
		ServiceManagerInner.init(context);
	}
	
	public static int addService(final String name, final IBinder service) {
		
		return ServiceManagerInner.sDefalt.addService(name, service);
	}
	
	public static IInterface getService(final String name) throws IPCServiceNotFoundException, IPCTimeoutException {
		return ServiceManagerInner.sDefalt.getService2(name);
	}
	
	private final static class ServiceManagerInner extends ServiceFactoryTemplate{
		
		private static ServiceManagerInner sDefalt = new ServiceManagerInner();
		static Context sContext = null;
		
		private ServiceManagerInner(){
			
		}
		
		private ServiceManagerInner(Context context){
			super(context);
		}
		
		public static void init(Context context){
			sContext = context;
			sDefalt.queryRemoteServices(context);
		}
		
		@Override
		public int addService( final String name, final IBinder service) {
			
			ServiceManagerImpl.asycQueryInterface(sContext, new OnQueryResultHandler() {
				public void onQueryResult(IServiceManager serviceManager) {
					
					LogControler.print(Level.INFO, "[ServiceManager] before addService");
					
					serviceManager.addService(name,service);
					
					LogControler.print(Level.INFO, "[ServiceManager] before successed");
					
				}
			});
			
			return 0;
		}
		
		@Override
		protected IInterface findService(String name) throws IPCServiceNotFoundException, IPCTimeoutException{
			if(!TextUtils.isEmpty(name)){
				
				IBinder binder = mServiceManager.getService(name);
				
				IBundleSender bundleSender = BundleSender.asInterface(binder);
				
				return bundleSender;
				
			}
			
			throw new IPCServiceNotFoundException("serivce name("+name+") not found");
		}

		@Override
		public IBinder getService(String name) throws IPCServiceNotFoundException, IPCTimeoutException{
			return getService2(name).asBinder();
		}
		
		
	}
	

	
}
