package com.devy.droidipc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.devy.droidipc.LogControler.Level;

@SuppressLint("NewApi") 
public class RemoteClientCommandListener extends RemoteCommandListener {

	@Override
	protected void onHandleCommand(int cmd, Intent intent) {
		
		if(cmd == ServiceContext.CMD_GET_SERVER_SERVICE_MANAGER) {
			
			Bundle binders = intent.getBundleExtra(ServiceContext.EXTRA_BUNDLE);
			IBinder serverReadyBinder = binders.getBinder(ServiceContext.EXTRA_BUNDLE_SERVER_READY_BINDER);
			String packageName = binders.getString(ServiceContext.EXTRA_BUNDLE_PACKAGE_NAME);
			
			ServerReadyNotifier serverReadyNotifier = new ServerReadyNotifier(packageName,serverReadyBinder);
			serverReadyNotifier.runInCoreThread();
			
		}
		
	}
	
	
	
	private class ServerReadyNotifier implements Runnable {
		
		private IBinder mClient;
		private String mPackageName;

		public ServerReadyNotifier(String packageName,IBinder client) {
			mPackageName = packageName; 
			mClient = client;
		}
		
		public void runInCoreThread() {
			CoreThread.getHandler().post(this);
		}
		
		private void log(String log){
			LogControler.print(Level.INFO, "[RemoteClientCommandListener] (" + mPackageName + ") " + log);
		}

		@Override
		public void run() {
			
			if(mClient!=null){
				
				Parcel data = Parcel.obtain();
				Parcel reply = Parcel.obtain();
				try {
					
					log("package name = " + mPackageName);
					
					data.writeStrongBinder(ServiceManagerThread.getDefault());
					
					mClient.transact(ServiceContext.SERVER_READY, data, reply, 0);
					int result = reply.readInt();
					
					if(result == ServiceContext.SUCCESS) {
						log("ServerReadyNotifier success");
					} else {
						log("ServerReadyNotifier error");
					}
					
				} catch (RemoteException e) {
					e.printStackTrace();
					log("getService exception");
				} finally {
					data.recycle();
					reply.recycle();
				}
				
			}
			
		}
		
		
		
	}


	
	
	
}
