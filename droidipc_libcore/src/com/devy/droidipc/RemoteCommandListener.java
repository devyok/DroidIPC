package com.devy.droidipc;

import com.devy.droidipc.LogControler.Level;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
/**
 * 监听所有命令，并分发<br>
 * 所有命令定义参考{@link ServiceContext} 
 * @author wei.deng
 */
public class RemoteCommandListener extends Service {

	static final String LOG_TAG = RemoteCommandListener.class.getSimpleName();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogControler.print(Level.INFO, "[ServiceManagerThreadListener] onCreate package name = " + getApplicationContext().getPackageName());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			
			LogControler.print(Level.INFO, "[ServiceManagerThreadListener] onStartCommand package name = " + getApplicationContext().getPackageName());
			
			String action = intent.getAction();

			LogControler.print(Level.INFO, "[ServiceManagerThreadListener] onStartCommand action = " + action);
			
			int cmd = intent.getIntExtra(ServiceContext.EXTRA_COMMAND, ServiceContext.CMD_CREATE_CLIENT_SERVICE_MANAGER);
			
			LogControler.print(Level.INFO, "[ServiceManagerThreadListener] onStartCommand cmd = " + cmd);
			
			onHandleCommand(cmd,intent);
			
		} finally {
		}
		
		return START_STICKY_COMPATIBILITY;
	}
	
	protected void onHandleCommand(int cmd,Intent intent) {
		if(cmd == ServiceContext.CMD_CREATE_CLIENT_SERVICE_MANAGER) {
			ServiceManagerImpl serviceManager = ServiceManagerImpl.createClientServiceManager(getApplicationContext(), intent);
			LogControler.print(Level.INFO, "[ServiceManagerThreadListener] RemoteCommandListener create new serviceManager = " + serviceManager);
		} else if(cmd == ServiceContext.CMD_FLUSH_CLIENT_SERVICES_TO_REMOTE_SERVICE_MANAGER_THREAD) {
			ServiceManagerImpl.flushCacheToRemoteServiceManager(ServiceManagerImpl.getGlobalServiceManager());
		} else if(cmd == ServiceContext.CMD_LAUNCH_CLIENT_SERVICE_PROCESS) {
			LogControler.print(Level.INFO,  "[ServiceManagerThreadListener] RemoteCommandListener only wake up client");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOG_TAG, "[Bundle Trasanct] RemoteCommandListener onDestroy package name = " + getApplicationContext().getPackageName());
	}

}
