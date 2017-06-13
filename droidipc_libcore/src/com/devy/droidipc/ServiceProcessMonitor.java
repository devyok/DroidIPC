package com.devy.droidipc;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.devy.droidipc.LogControler.Level;

/**
 * ClientProcessMonitorService主要负责监控所有服务提供者与使用者进程的状态<br>
 * 所有进程启动后通过{@link OnClientProcessStateChangedListener}通知
 * @author wei.deng
 */
class ServiceProcessMonitor extends HandlerThread{

	public ServiceProcessMonitor() {
		super("process.monitor.thread", android.os.Process.THREAD_PRIORITY_DEFAULT);
	}
	
	public static abstract class OnClientProcessStateChangedListener {
		
		public void onAllClientProcessStarted(){}
		
	}
	
	private static ServiceProcessMonitor sInstance;
    private static Handler sHandler;

    private static void ensureThreadLocked() {
        if (sInstance == null) {
            sInstance = new ServiceProcessMonitor();
            sInstance.start();
            sHandler = new Handler(sInstance.getLooper());
        }
    }

    public static ServiceProcessMonitor get() {
        synchronized (ServiceProcessMonitor.class) {
            ensureThreadLocked();
            return sInstance;
        }
    }

    public static Handler getHandler() {
        synchronized (ServiceProcessMonitor.class) {
            ensureThreadLocked();
            return sHandler;
        }
    }
    
    public static void startMonitor(final Context context,List<String> clientProcessNames,final OnClientProcessStateChangedListener lis){
    	synchronized (ServiceProcessMonitor.class) {
            ensureThreadLocked();
            
            getHandler().post(new ClientProcessListenLooper(context, clientProcessNames, lis));
        }
    }
    
    private static final class ClientProcessListenLooper implements Runnable{

    	List<String> allConfigServiceClientProcessNames = null;
    	
    	WeakReference<Context> refContext;
    	
    	WeakReference<OnClientProcessStateChangedListener> refLis;
    	
    	private ClientProcessListenLooper(Context context,List<String> allConfigServiceClientProcessNames,OnClientProcessStateChangedListener lis){
    		refContext = new WeakReference<Context>(context);
    		this.refLis = new WeakReference<ServiceProcessMonitor.OnClientProcessStateChangedListener>(lis);
    		this.allConfigServiceClientProcessNames = allConfigServiceClientProcessNames;
    	}
    	
		@Override
		public void run() {
			loop();
		}
		
		private void loop() {
			
			List<String> allStartedProcess = new ArrayList<String>();
			ActivityManager manager = (ActivityManager) refContext.get().getSystemService(Context.ACTIVITY_SERVICE);
			
			boolean quit = false;
			while(!quit){
				
				List<RunningAppProcessInfo> runningAppProcessInfos = manager.getRunningAppProcesses();
				
				if(runningAppProcessInfos!=null && runningAppProcessInfos.size() == 1) {
					RunningAppProcessInfo info = runningAppProcessInfos.get(0);
					if(info.processName.equals(ServiceContext.SERVER_PACKAGE_NAME)){
						break;
					}
				}
		        
		        for (int i = 0; i < runningAppProcessInfos.size(); i++) {
					
		        	RunningAppProcessInfo runningAppProcessInfo = runningAppProcessInfos.get(i);
		        	
		        	String processName = runningAppProcessInfo.processName;
		        	
		        	LogControler.print(Level.INFO, "[ClientProcessMonitorService] running processName = " + processName);
		        	
		        	if(processName!=null && allConfigServiceClientProcessNames.contains(processName)) {
		        		
		        		LogControler.print(Level.INFO, "[ClientProcessMonitorService] add started list processName = " + processName);
		        		
		        		if(!allStartedProcess.contains(processName)) {
		        			allStartedProcess.add(processName);
		        		}
		        		
		        		if(allConfigServiceClientProcessNames.containsAll(allStartedProcess)){
		        			
		        			quit = true;
		        			
		        			if(refLis.get()!=null){
		        				LogControler.print(Level.INFO, "[ClientProcessMonitorService] notify onAllClientProcessStarted");
		        				refLis.get().onAllClientProcessStarted();
		        			}
		        			
		        			break;
		        		}
		        		
		        	}
				}
			}
			LogControler.print(Level.INFO, "[ClientProcessMonitorService] startMonitor quit");
			
			recycle();
		}

		private void recycle() {
			allConfigServiceClientProcessNames.clear();
			allConfigServiceClientProcessNames = null;
			
			refContext = null;
			refLis = null;
		}
    	
    }

}
