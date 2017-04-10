package com.devy.droidipc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

import com.devy.droidipc.LogControler.Level;


/**
 * 每一个远程服务都需要在此处声明<br>
 * 同时{@link ServiceContext}支持获取所有声明服务的进程和声明使用服务的进程的{@link PackageInfo}<p>
 * {@link ServiceContext}部分实现不对外暴露
 * @author wei.deng
 */
class ServiceContext {

	private final static int sBeginCode = 10000;
	
	public final static int ERROR = sBeginCode + 1;
	public final static int SUCCESS = ERROR + 1;
	
	final static int SEND_BUNDLE = SUCCESS + 1;
	final static int SET_CALLBACK = SEND_BUNDLE + 1;
	final static int TRANSACT_BUNDLE_SENDER = SET_CALLBACK + 1;
	final static int ADD_SERVICE = TRANSACT_BUNDLE_SENDER + 1;
	final static int GET_SERVICE = ADD_SERVICE + 1;
	final static int MANAGED_CLIENT_SIVMGR = GET_SERVICE + 1;
	
	static final int CMD_CREATE_CLIENT_SERVICE_MANAGER = MANAGED_CLIENT_SIVMGR + 1;
	static final int CMD_FLUSH_CLIENT_SERVICES_TO_REMOTE_SERVICE_MANAGER_THREAD = CMD_CREATE_CLIENT_SERVICE_MANAGER + 1;
	static final int CMD_LAUNCH_CLIENT_SERVICE_PROCESS = CMD_FLUSH_CLIENT_SERVICES_TO_REMOTE_SERVICE_MANAGER_THREAD + 1;
	
	static final int ACTIVE_CLIEINT_SERVCIE_GETSERVICE = CMD_LAUNCH_CLIENT_SERVICE_PROCESS + 1;
	
	static final int CALLBACK = ACTIVE_CLIEINT_SERVCIE_GETSERVICE+1;
	
	static final int READY_REMOTE = CALLBACK+1;
	
	static final int CMD_GET_SERVER_SERVICE_MANAGER = READY_REMOTE+1;
	
	static final int SERVER_READY = CMD_GET_SERVER_SERVICE_MANAGER+1;
	
	static final int CMD_STOP_CLIENT_LISTINER = CMD_GET_SERVER_SERVICE_MANAGER + 1;
	
	
	final static String ACTION_RECEIVE_SERVICE_MANAGER = "action.receive.serviceManager";
	final static String EXTRA_BUNDLE_BINDER= "extra.bundle.binder";
	final static String EXTRA_BUNDLE_SERVER_READY_BINDER= "extra.bundle.server.ready.binder";
	final static String EXTRA_BUNDLE_PACKAGE_NAME= "extra.bundle.package.name";
	final static String EXTRA_COMMAND = "intent.command";
	final static String EXTRA_BUNDLE = "intent.bundle";
	final static String EXTRA_CALLBACK = "extra.bundle.callback";
	
	//管理所有服务的所在进程包名
	final static String SERVER_PACKAGE_NAME = "com.devy.service_manager";
	
	final static String ACTION_REMOTE_CLIENT_COMMAND = "com.devy.action.REMOTE_CLIENT_COMMAND_LISTENER";
	final static String ACTION_SERVER_MANAGER_THREAD_LISTENER = "com.devy.action.SERVER_MANAGER_THREAD_LISTENER";
	
	static RemoteServiceInfos sGlobalRemoteServiceInfos = null;
	
	final static class RemoteServiceInfos {
		/**
		 * 所有声明了需要提供远程服务的信息
		 *
		 * map.key = 服务名称
		 * map.value = 服务对应的详细信息
		 */
		public HashMap<String, ResolveInfo> mServiceProviderMappings = new HashMap<String, ResolveInfo>();
		/**
		 * 所有需要执行远程服务的配置信息
		 */
		public ArrayList<ResolveInfo> mAllConfigServiceNodeInfos = new ArrayList<ResolveInfo>();
		/**
		 * 所有提供服务的配置信息
		 */
		public ArrayList<ResolveInfo> mAllServiceProviderInfos = new ArrayList<ResolveInfo>();
		public ArrayList<ResolveInfo> mAllServiceClientInfos = new ArrayList<ResolveInfo>();
		/**
		 * 所有需要远程服务的进程名称
		 */
		public ArrayList<String> mAllConfigServiceProcessName = new ArrayList<String>();
		
		public void printAllInfos() {
			LogControler.print(Level.INFO, "[ServiceContext] RemoteServiceInfos.mServiceProviderMappings = " + mServiceProviderMappings);
			
			LogControler.print(Level.INFO, "[ServiceContext] RemoteServiceInfos.mAllConfigServiceProcessName = " + Arrays.toString(mAllConfigServiceProcessName.toArray()));
		}
		
	}
	
	/**
	 * map.key = 服务名称
	 * map.value = 服务对应的包名
	 */
	private static RemoteServiceInfos queryRemoteServiceInfos(Context context){
		
		RemoteServiceInfos remoteServiceInfos = new RemoteServiceInfos();
		
		HashMap<String, ResolveInfo> mapping = new HashMap<String, ResolveInfo>();
		ArrayList<ResolveInfo> allConfigServiceInfos = new ArrayList<ResolveInfo>();
		ArrayList<ResolveInfo> allServiceServerInfos = new ArrayList<ResolveInfo>();
		ArrayList<String> allConfigServiceProcessName = new ArrayList<String>();
		
		PackageManager pm = context.getPackageManager();
		
		List<PackageInfo> servicePackageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		int size = servicePackageInfos.size();
		
		LogControler.print(Level.INFO, "[ServiceContext] notifyRemoteListeners");
		
		for (int i = 0; i < size; i++) {
			PackageInfo packageInfo = servicePackageInfos.get(i);
			
			Intent queryIntent = new Intent();
			queryIntent.setComponent(new ComponentName(packageInfo.packageName,RemoteCommandListener.class.getName()));
			
			List<ResolveInfo> infos = pm.queryIntentServices(queryIntent,PackageManager.GET_META_DATA);
			
			if(infos == null || infos.size() == 0) continue;
			
			int serviceInfoLen = infos.size();
			
			for (int j = 0; j < serviceInfoLen; j++) {
				
				ResolveInfo resolveInfo = infos.get(j);
				ServiceInfo serviceInfo = resolveInfo.serviceInfo;
				
				String className = serviceInfo.name;
				
				try {
					if(RemoteCommandListener.class.getName().equals(className)) {
						
						Bundle bundle = resolveInfo.serviceInfo.metaData;
						
						if(bundle!=null){
							
							if(bundle.containsKey("remove.service.names")){
								String names = bundle.getString("remove.service.names");
								
								String[] serviceNames = names.split(",");
								
								for (int k = 0; k < serviceNames.length; k++) {
									mapping.put(serviceNames[k], resolveInfo);
								}
								
								allServiceServerInfos.add(resolveInfo);
								
								LogControler.print(Level.INFO, "[ServiceContext] bundle = " + bundle + ", names = " + names);
							}
							
						}
						
						allConfigServiceProcessName.add(resolveInfo.serviceInfo.applicationInfo.processName);
						allConfigServiceInfos.add(resolveInfo);
						
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
			}
		}
		
		remoteServiceInfos.mServiceProviderMappings.putAll(mapping);
		remoteServiceInfos.mAllConfigServiceNodeInfos.addAll(allConfigServiceInfos);
		remoteServiceInfos.mAllServiceProviderInfos.addAll(allServiceServerInfos);
		remoteServiceInfos.mAllConfigServiceProcessName.addAll(allConfigServiceProcessName);
		
		remoteServiceInfos.printAllInfos();
		
		return remoteServiceInfos;
	}
	
	static void initGlobalRemoteServiceInfos(Context context){
		sGlobalRemoteServiceInfos = queryRemoteServiceInfos(context);
		Log.i("ServiceContext","[ServiceContext] initGlobalRemoteServiceInfos enter");
	}
	
	static RemoteServiceInfos getGlobalRemoteServiceInfos(Context context){
		if(sGlobalRemoteServiceInfos==null){
			sGlobalRemoteServiceInfos = queryRemoteServiceInfos(context);
		}
		return sGlobalRemoteServiceInfos;
	}
	
	static boolean existServerPackage(Context context){
		
		PackageManager pm = context.getPackageManager();
		
		List<PackageInfo> servicePackageInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		int size = servicePackageInfos.size();
		
		LogControler.print(Level.INFO, "[ServiceContext] notifyRemoteListeners");
		
		for (int i = 0; i < size; i++) {
			PackageInfo packageInfo = servicePackageInfos.get(i);
			
			String packageName = packageInfo.applicationInfo.packageName;
			
			if(ServiceContext.SERVER_PACKAGE_NAME.equals(packageName)) {
				return true;
			}
			
		}
		
		return false;
	}
	
}
