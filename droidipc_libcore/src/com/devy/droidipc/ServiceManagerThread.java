package com.devy.droidipc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.devy.droidipc.ServiceContext.RemoteServiceInfos;
import com.devy.droidipc.ServiceProcessMonitor.OnClientProcessStateChangedListener;
import com.devy.droidipc.LogControler.Level;
/**
 *  ServerManagerThread主要负责管理所有的客户端服务
 * @author wei.deng
 */
@SuppressLint("NewApi") 
public class ServiceManagerThread extends Binder implements IServiceManager{

	private static ServiceManagerThread sDefault = new ServiceManagerThread();
	
	private ConcurrentHashMap<String, IBinder> mClientServiceManagerContainer = new ConcurrentHashMap<String, IBinder>();
	private ConcurrentHashMap<String, IBinder> mServiceContainer = new ConcurrentHashMap<String, IBinder>();
	
	private ConcurrentHashMap<String, DeathRecipientHandler> mServiceProcessDeathHandlers = new ConcurrentHashMap<String, DeathRecipientHandler>();
	
	private static Context sGlobalContext;
	
	static ServiceManagerThread getDefault(){
		return sDefault;
	}
	
	public static final ServiceManagerThread getManager(Context context){
		
		String packageName = context.getPackageName();
		if(!ServiceContext.SERVER_PACKAGE_NAME.equals(packageName)) {
			throw new RuntimeException("don't invoke , no server process");
		}
		
		return sDefault;
	}
	
	@Override
	protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		
		LogControler.print(Level.INFO, "[ServerManagerThread] pid = "+ getCallingPid() +" , tid = " + Thread.currentThread().getId() + " , tname = " + Thread.currentThread().getName());
		
		if(code == ServiceContext.MANAGED_CLIENT_SIVMGR) {
			String serviceManagerName = data.readString();
			IBinder clientServiceManager = data.readStrongBinder();
			
			mClientServiceManagerContainer.put(serviceManagerName, clientServiceManager);
			
			LogControler.print(Level.INFO, "[ServerManagerThread] MANAGED_CLIENT_SIVMGR servcieManager name = " + serviceManagerName + " , service manager impl = " + clientServiceManager);
			
			activeClientGetService();
			
		} else if(code == ServiceContext.ADD_SERVICE) {
			String serviceName = data.readString();
			IBinder service = data.readStrongBinder();
			
			LogControler.print(Level.INFO, "[ServerManagerThread] add service name = " + serviceName + " , service impl = " + service);
			
			int result = addService(serviceName, service);
			
			DeathRecipientHandler deathRecipientHandler = mServiceProcessDeathHandlers.get(serviceName);
			
			if(deathRecipientHandler==null){
				
				DeathRecipientHandler newDeathRecipientHandler = new DeathRecipientHandler(serviceName,getServicePackageName(serviceName),service);
				mServiceProcessDeathHandlers.put(serviceName, newDeathRecipientHandler);
				service.linkToDeath(newDeathRecipientHandler, 0);
			}
			
			reply.writeInt(result);
			
			activeClientGetService();
			
		} else if(code == ServiceContext.GET_SERVICE) {
			
			String serviceName = data.readString();
			IBinder binder = getService(serviceName);
			reply.writeStrongBinder(binder);
			LogControler.print(Level.INFO, "[ServerManagerThread] get service name = " + serviceName + " , service impl = " + binder);
		} else if(code == ServiceContext.READY_REMOTE) {
			
			if(isReady()){
				reply.writeInt(ServiceContext.SUCCESS);
			} else {
				reply.writeInt(ServiceContext.ERROR);
			}
			
		}
		
		return true;
	}
	
	
	
	private ResolveInfo getServicePackageName(String service) {
		ResolveInfo info = ServiceContext.getGlobalRemoteServiceInfos(sGlobalContext).mServiceProviderMappings.get(service);
		return info;
	}

	private boolean isReady(){
		int countAllAddedServices = countAllAddedServices();
		int countAllConfigServies = countAllConfigServices();
		
		LogControler.print(Level.INFO, "[ServerManagerThread] isReady() countAllAddedServices = " + countAllAddedServices + " ,countAllConfigServies = " + countAllConfigServies);
		
		if(countAllAddedServices >= countAllConfigServies) {
			return true;
		}
		
		return false;
	}
	
	private void activeClientGetService() {
		
		if(isReady()){
			for (Iterator<Map.Entry<String,IBinder>> iter = mClientServiceManagerContainer.entrySet().iterator();iter.hasNext();) {
				
				Map.Entry<String,IBinder> me = iter.next();
				
				IBinder clientServiceManager = me.getValue();
				
				Parcel reply = Parcel.obtain();
				
				try {
					clientServiceManager.transact(ServiceContext.ACTIVE_CLIEINT_SERVCIE_GETSERVICE, Parcel.obtain(), reply, 0);
					
					int result = reply.readInt();
					
					if(result == ServiceContext.SUCCESS) {
						
						LogControler.print(Level.INFO, "[ServerManagerThread] activeClientGetService success");
						
					} else {
						
						LogControler.print(Level.INFO, "[ServerManagerThread] activeClientGetService fail");
						
					}
					
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				
			}
		}
		
	}

	private int countAllConfigServices(){
		return ServiceContext.getGlobalRemoteServiceInfos(sGlobalContext).mServiceProviderMappings.size();
	}
	
	
	private int countAllAddedServices(){
		return mServiceContainer.size();
	}
	
	public static void attachSystem(Context context) {
		
		if(context == null){
			LogControler.print(Level.INFO, "[ServerManagerThread] attachSystem exception");
			throw new IllegalArgumentException("Invalid Argument context = " + context); 
		}
		
		CoreThread.getHandler();
		
		sGlobalContext = context;
		
		ServiceContext.initGlobalRemoteServiceInfos(context);
		
		startMonitorAllClientProcess(context);
		
		launchAllClientProcess(context);
		
	}
	
	
	private static void startMonitorAllClientProcess(final Context context) {
		
		RemoteServiceInfos remoteServiceInfos = ServiceContext.getGlobalRemoteServiceInfos(context);
		List<String> allConfigServiceClientProcessNames = remoteServiceInfos.mAllConfigServiceProcessName;
		
		ServiceProcessMonitor.startMonitor(context,allConfigServiceClientProcessNames,new OnClientProcessStateChangedListener() {

			@Override
			public void onAllClientProcessStarted() {
				
				attachToActivityManagerService(context);
				notifyAllClientServerReady();
			}
		});
		
	}

	private static void notifyAllClientServerReady() {
		
	}

	private static void launchAllClientProcess(Context context) {
		RemoteCommandSender.sendCommand(context,ServiceContext.getGlobalRemoteServiceInfos(context).mAllConfigServiceNodeInfos,ServiceContext.CMD_LAUNCH_CLIENT_SERVICE_PROCESS);
	}

	private static void attachToActivityManagerService(Context context) {
		
		LogControler.print(Level.INFO, "[ServerManagerThread] attach To ActivityManagerService");
		
		Intent intent = new Intent(ServiceContext.ACTION_RECEIVE_SERVICE_MANAGER);
		Bundle binderBunder = new Bundle();
		binderBunder.putBinder(ServiceContext.EXTRA_BUNDLE_BINDER, ServiceManagerThread.getDefault());
		intent.putExtra(ServiceContext.EXTRA_BUNDLE, binderBunder);
		intent.putExtra(ServiceContext.EXTRA_COMMAND, ServiceContext.CMD_CREATE_CLIENT_SERVICE_MANAGER);
		context.sendStickyBroadcast(intent);
	}

	public int addService(String name, IBinder service) {
		mServiceContainer.put(name, service);
		return ServiceContext.SUCCESS;
	}

	public IBinder getService(String name) {
		return mServiceContainer.get(name);
	}
	
	final class DeathRecipientHandler implements IBinder.DeathRecipient{

		private ResolveInfo mResolveInfo;
		private IBinder mBinder;
		private String mServiceName;
		public DeathRecipientHandler(String serviceName,ResolveInfo info,IBinder binder) {
			mServiceName = serviceName;
			mResolveInfo = info;
			mBinder = binder;
		}
		
		@Override
		public void binderDied() {
			
			synchronized (this) {
				if(mResolveInfo!=null) {
					
					RemoteCommandSender.sendCommand(sGlobalContext, mResolveInfo, ServiceContext.CMD_LAUNCH_CLIENT_SERVICE_PROCESS);
					
					mBinder.unlinkToDeath(this, 0);
					mServiceProcessDeathHandlers.remove(mServiceName);
					mResolveInfo = null;
					mServiceName = null;
					
				}
			}
			
		}
		
	}

	

}
