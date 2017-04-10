package com.devy.droidipc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.devy.droidipc.LogControler.Level;
import com.devy.droidipc.exception.IPCTimeoutException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * 本对象主要负责与远程服务管理层通信<br>
 * <p>主要包括两种操作：</p>
 * 1.添加{@link ServiceManagerImpl#addService()}远程服务<br>
 * 2.获取{@link ServiceManagerImpl#getService()}远程服务<br>
 * <br>
 * <p>每一个服务的具体实现必须依赖{@link BundleReceiver}</p>
 * 
 * 当客户端通过{@link ServiceManagerImpl#getService()} 去获取远程服务时,如果此服务还没有被<br>
 * 服务实现者(另一个进程或APK)添加到{@link ServiceManagerThread}中,那么此时{@link ServiceManagerImpl#getService()}<br>
 * 将会被阻塞等待。 等待时间为{@link ServiceManagerImpl#TIMEOUT}，在这个时间段内，如果被添加到{@link ServiceManagerThread}<br>
 * 后，会通知所有客户端{@link ServiceManagerImpl}对象中断阻塞，并继续执行{@link ServiceManagerImpl#getService()}。
 * 
 * <p>代码示例如下：</p>
 * 
 * <p>1. 实现GPS服务对象：</p>
 * public class GpsService extends BundleReceiver {<br>

	public Bundle onReceiver(Bundle bundle) {<br>
		<p>//根据双发约定从参数bundle对象中获取参数 ，返回也同样， 根据约定返回相应 </p>
		return new Bundle();<br>
	}<br>

   }<br>
   
   <p>2.创建GPS服务对象</p>
   <p>GpsService gpsControler = new GpsService();</p>
   
   <p>3.托管GPS服务对象</p>
   ServiceManager.asycQueryInterface(getApplicationContext(), new OnQueryResult() {<br>
		public void onQueryResult(ServiceManager serviceManager) {<br>
			<br>
			serviceManager.addService(ServiceContext.GPS_SERVICE, gpsControler);<br>
			<br>
		}<br>
	});<br>
 * 
 * @author wei.deng
 */
@SuppressLint("NewApi") 
class ServiceManagerImpl extends Binder implements IServiceManager , IBinder.DeathRecipient{

	private static long sServerInitTime = 0L;
	/**
	 * 可以通过此对象，获取服务管理层所有业务对象
	 */
	private IBinder mRemoteServiceManager;
	
	static ServiceManagerImpl sGlobalServiceManager;
	static Context sClientContext;
	
	static final ArrayList<OnQueryResultHandler> sGlobalHandlers = new ArrayList<ServiceManagerImpl.OnQueryResultHandler>();
	
	/**
	 * 缓存所有客户端的服务对象
	 */
	static LinkedHashMap<String, IBinder> sClientServiceCache = new LinkedHashMap<String, IBinder>();
	/**
	 * waitter {@link ServiceManagerImpl#getService()}
	 */
	private Object mLock = new Object();
	private Thread mLockThread = null;
	private boolean mTryLock = true;
	
	private long TIMEOUT = 10*1000; 
	
	private static boolean sExistServerPackage = false;
	
	private static Binder EMPTY = new Binder();
	
	private ServiceManagerImpl(Context context, IBinder binder){
		mRemoteServiceManager = binder;
		ServiceManagerImpl.sClientContext = context;
		ServiceManagerImpl.sGlobalServiceManager = this;
	}
	
	static ServiceManagerImpl getGlobalServiceManager(){
		return sGlobalServiceManager;
	}
	
	public interface OnQueryResultHandler {
		public void onQueryResult(IServiceManager serviceManager);
	}
	/**
	 * 
	 * 本方法负责获取远程服务管理对象{@link ServiceManagerThread}， 获取此对象之后，
	 * <br>会返回本地{@link ServiceManagerImpl}
	 * 本地的{@link ServiceManagerImpl}对象， 是对远程服务对象{@link ServiceManagerThread}的代理。 
	 * <br>
	 * 
	 * 当客户端通过{@link ServiceManagerImpl#asycQueryInterface()}获取到远程服务层的管理对象{@link ServiceManagerThread}之后
	 * <br>
	 * 会第一时间发送此消息{@link ServiceContext#MANAGED_CLIENT_SIVMGR}将客户端对象{@link ServiceManagerImpl}发送给{@link ServiceManagerThread}，来托管自己。
	 * 
	 * @param context
	 * @param onQueryResult
	 */
	public static void asycQueryInterface(final Context context,final OnQueryResultHandler onQueryResult) {
		
		if(context == null || onQueryResult == null){
			log("asycQueryInterface exception");
			throw new IllegalArgumentException("Invalid Argument context = " + context + " , onQueryResult = " + onQueryResult); 
		}
		
		sExistServerPackage = ServiceContext.existServerPackage(context);
		
		if(!sExistServerPackage) {
			log("not exist server pacakge");
			return ;
		}
		
		sClientContext = context;
		
		managed(onQueryResult);
		
		log("asycQueryInterface enter");
		
		final ServiceManagerImpl global = sGlobalServiceManager;
		if(global!=null){
			log("asycQueryInterface exist global serviceManager");
			CoreThread.getHandler().post(new Runnable() {
				@Override
				public void run() {
					onQueryResult.onQueryResult(global);
				}
			});
		} else {
			log("asycQueryInterface get serviceManager from AMS");
//			ServiceManagerQueryHandler serviceManagerFinder = new ServiceManagerQueryHandler(context);
//			serviceManagerFinder.handleQuery();
			
			sendServerCommand();
			
		}
	}
	
	private static void managed(OnQueryResultHandler onQueryResult) {
		if(!sGlobalHandlers.contains(onQueryResult)) {
			sGlobalHandlers.add(onQueryResult);
		}
	}

	private static void notifyAllListeners(ServiceManagerImpl serviceManager){
		
		Log.i("ServiceInitializeNotifier", "[Bundle Trasanct] sGlobalHandlers size = " + sGlobalHandlers.size());
		
		for (int i = 0; i < sGlobalHandlers.size(); i++) {
			OnQueryResultHandler onQueryResultHandler = sGlobalHandlers.get(i);
			if(onQueryResultHandler!=null){
				onQueryResultHandler.onQueryResult(serviceManager);
			}
		}
	}
	
	private static void log(String log){
		LogControler.print(Level.INFO,  "[ServiceManagerImpl] (" + getLocalSerivceManagerPackage() + ")" + log);
	}
	
	private void managedSelf(String packageName) {
		
		log("managedSelf package name = " + packageName);
		
		Parcel data = Parcel.obtain();
		data.writeString(packageName);
		data.writeStrongBinder(this);
		
		Parcel reply = Parcel.obtain();
		
		try {
			mRemoteServiceManager.transact(ServiceContext.MANAGED_CLIENT_SIVMGR, data, reply, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
			log("managedSelf exception");
		}finally {
			data.recycle();
			reply.recycle();
		}
		
	}
	
	/**
	 * 向远程服务管理层添加业务实现对象，其他进程就可以通过{@link ServiceManagerImpl#getService()}获取此服务对象
	 * 
	 * @param name 服务名称
	 * @param service 服务的具体实现,必须实现{@link BundleReceiver}
	 */
	public int addService(String name, IBinder service) {
		
		log("addService name = " + name + " , service impl = " + service);
		
		cache(name, service);
		
		Parcel data = Parcel.obtain();
		data.writeString(name);
		data.writeStrongBinder(service);
		
		Parcel reply = Parcel.obtain();
		
		try {
			mRemoteServiceManager.transact(ServiceContext.ADD_SERVICE, data, reply, 0);
			
			int result = reply.readInt();
			
			log("addService result = " + result);
			
			return result;
			
		} catch (RemoteException e) {
			e.printStackTrace();
			log("addService exception");
			return ServiceContext.ERROR;
		} finally {
			data.recycle();
			reply.recycle();
		}
		
	}
	
	/**
	 * 从远程服务管理层获取服务对象
	 * @param name 服务名称
	 * @throws IPCTimeoutException 
	 */
	public IBinder getService(String name) throws IPCTimeoutException {
		
		log("getService name = " + name);
		
		if(!sExistServerPackage) {
			log("not exist server pacakge");
			return EMPTY;
		}
		
		if(!isReadyRemote() && mTryLock){
			mLockThread = Thread.currentThread();
			lock();
			throw new IPCTimeoutException("find remote service timeout");
		}
		
		return findService(name);
	}
	
	private IBinder findService(String name){
		Parcel data = Parcel.obtain();
		data.writeString(name);
		Parcel reply = Parcel.obtain();
		try {
			mRemoteServiceManager.transact(ServiceContext.GET_SERVICE, data, reply, 0);
			IBinder service = reply.readStrongBinder();
			log("getService result binder = " + service);
			return service;
		} catch (RemoteException e) {
			e.printStackTrace();
			log("getService exception");
			return null;
		} finally {
			data.recycle();
			reply.recycle();
		}
	}
	
	/**
	 * 检查所有的服务是否已经就位(已经添加到{@link ServiceManagerThread})
	 * @return
	 */
	private boolean isReadyRemote(){
		Parcel data = Parcel.obtain();
		Parcel reply = Parcel.obtain();
		try {
			mRemoteServiceManager.transact(ServiceContext.READY_REMOTE, data, reply, 0);
			int result = reply.readInt();
			
			if(result == ServiceContext.SUCCESS) {
				
				log("remote service manager ready");
				
				return true;
			}
			
			log("remote service manager not ready");
			
			return false;
		} catch (RemoteException e) {
			e.printStackTrace();
			log("getService exception");
			return false;
		} finally {
			data.recycle();
			reply.recycle();
		}
	}
	

	private void lock() {
		log("lock queryService waitting addService");
		synchronized (mLock) {
			try {
				mLock.wait(TIMEOUT);
				log("wait timeout");
				mTryLock = true;
			} catch (InterruptedException e) {
				log("lock interrupted");
				mTryLock = false;
				mLockThread = null;
			}
		}
		
	}
	
	private void interuptLock(){
		if(mLockThread!=null){
			synchronized (mLock) {
				mLockThread.interrupt();
			}
		}
	}

	@Override
	protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		
		if(code == ServiceContext.ACTIVE_CLIEINT_SERVCIE_GETSERVICE) {
			try {
				
				log("active getService Method");
				interuptLock();
				
				reply.writeInt(ServiceContext.SUCCESS);
				
			} finally {
				mTryLock = false;
				mLockThread = null;
			}
			
		}
		
		return true;
	}
	
	static final void sendServerCommand(){
		RemoteCommandSender.sendServerCommand(sClientContext, ServiceContext.CMD_GET_SERVER_SERVICE_MANAGER, new CommandCallback());
	}
	
	private static class CommandCallback extends Binder {

		private long startTime;
		
		public CommandCallback(){
			startTime = System.currentTimeMillis();
		}
		
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			
			
			if(code == ServiceContext.SERVER_READY) {
				
				IBinder remoteServiceManager = data.readStrongBinder();
				
				final ServiceManagerImpl clientServiceManager = ServiceManagerImpl.createLocalServiceManager(sClientContext, remoteServiceManager);
				
				Log.i("ServiceInitializeNotifier", "[Bundle Trasanct] "+getLocalSerivceManagerPackage()+" CommandCallback ok ");
				
				CoreThread.getHandler().post(new Runnable() {
					
					@Override
					public void run() {
						
						notifyAllListeners(clientServiceManager);
						
					}
				});
				
				reply.writeInt(ServiceContext.SUCCESS);
				
				sServerInitTime = (System.currentTimeMillis() - startTime);
				
			}
			
			return true;
		}
		
	}
	
	static final class ServiceManagerQueryHandler extends BroadcastReceiver {

		private Context mContext;
		
		public ServiceManagerQueryHandler(Context context) {
			mContext = context;
		}
		
		public void handleQuery() {
			mContext.registerReceiver(this, new IntentFilter(
					ServiceContext.ACTION_RECEIVE_SERVICE_MANAGER));
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {
			String action = intent.getAction();
			log("ServiceManagerQueryHandler receive action = " + action);
			if (ServiceContext.ACTION_RECEIVE_SERVICE_MANAGER.equals(action)) {
				CoreThread.getHandler().post(new Runnable() {
					
					@Override
					public void run() {
						notifyAllListeners(createClientServiceManager(mContext,intent));
					}
				});
			}
		}
		
	}
	
	static ServiceManagerImpl createLocalServiceManager(Context context,IBinder remoteServiceManager){
		ServiceManagerImpl serviceManager = new ServiceManagerImpl(context,remoteServiceManager);
		
		try {
			remoteServiceManager.linkToDeath(serviceManager, 0);
		} catch (RemoteException e) {
			e.printStackTrace(); //ignore
		}
		
		serviceManager.managedSelf(context.getPackageName());
		
		return serviceManager;
	}
	
	static ServiceManagerImpl createClientServiceManager(Context context,Intent intent) {
		
		log("createClientServiceManager");
		
		Bundle binders = intent.getBundleExtra(ServiceContext.EXTRA_BUNDLE);
		IBinder remoteServiceManager = binders.getBinder(ServiceContext.EXTRA_BUNDLE_BINDER);
		
		int cmd = intent.getIntExtra(ServiceContext.EXTRA_COMMAND, ServiceContext.CMD_CREATE_CLIENT_SERVICE_MANAGER);
		
		log("createClientServiceManager cmd = " + cmd);
		
		ServiceManagerImpl serviceManager = ServiceManagerImpl.createLocalServiceManager(context, remoteServiceManager);
		
		return serviceManager;
	}

	@Override
	public void binderDied() {
		log("ServiceManagerThread process died , local ServiceManager("+getLocalSerivceManagerPackage()+") receive this message");
		
		log("delay "+sServerInitTime+"ms run send Server cmd(Get or init serverManager)");
		
//		recycle();
		
		//服务管理进程异常崩溃
		
		CoreThread.getHandler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				
				sendServerCommand();
				
			}
		}, sServerInitTime > 2000 ? 1000 : sServerInitTime);
		
	}

	private  void recycle(){
	}
	
	private static String getLocalSerivceManagerPackage(){
		return (sClientContext!=null ? sClientContext.getPackageName() : "");
	}
	
	private void cache(String name,IBinder service) {
		sClientServiceCache.put(name, service);
	}
	
	static void flushCacheToRemoteServiceManager(ServiceManagerImpl serviceManager){
		log("flushCacheToRemoteServiceManager");
		for (Iterator<Map.Entry<String, IBinder>> iter = sClientServiceCache.entrySet().iterator();iter.hasNext();) {
			
			Map.Entry<String, IBinder> item = iter.next();
			
			String serviceName = item.getKey();
			IBinder service = item.getValue();
			
			serviceManager.addService(serviceName, service);
			
		}
		
	}
	
}
