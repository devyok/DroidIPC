package com.devy.droidipc;

import android.content.Context;
import android.os.IInterface;

import com.devy.droidipc.LogControler.Level;
import com.devy.droidipc.ServiceManagerImpl.OnQueryResultHandler;
import com.devy.droidipc.exception.IPCServiceNotFoundException;
import com.devy.droidipc.exception.IPCTimeoutException;

abstract class ServiceFactoryTemplate implements IServiceManager{

	private Object mLock = new Object();
	private Thread mLockThread = null;
	private boolean mTryLock = true;
	public IServiceManager mServiceManager;
	
	public ServiceFactoryTemplate() {
	}
	
	public ServiceFactoryTemplate(Context context) {
	}

	protected void queryRemoteServices(Context context){
		ServiceManagerImpl.asycQueryInterface(context, new OnQueryResultHandler() {
			
			@Override
			public void onQueryResult(IServiceManager serviceManager) {
				LogControler.print(Level.INFO,  "[ServiceFactoryTemplate] onQueryResult enter");
				onServiceManagerReady(serviceManager);
			}
		});
		
	}
	
	public void onServiceManagerReady(IServiceManager serviceManager){
		try {
			mServiceManager = serviceManager;
			unlock();
		}finally {
			mTryLock = false;
			mLockThread = null;
		}
	}
	
	private void lock(){
		LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] lock enter");
		synchronized (mLock) {
			
			if(mLockThread==null) {
				LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] mLockThread is null lock return");
				return ;
			}
			
			try {
				mLock.wait(6*1000);
				
				LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] lock timeout");
				
				mTryLock = true;
			} catch (InterruptedException e) {
				mTryLock = false;
				LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] lock interupted");
			}
		}
	}
	
	private void unlock(){
		LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] unlock enter lock thread = " + mLockThread);
		if(mLockThread!=null) {
			synchronized (mLock) {
				mLockThread.interrupt();
			}
		}
	}
	
	public IInterface getService2(String name) throws IPCServiceNotFoundException,IPCTimeoutException{
		LogControler.print(Level.INFO,  "[time trace] getService enter");
		LogControler.print(Level.ERROR, "[ServiceFactoryTemplate] getService name = " + name + " , try lock = " + mTryLock);
		
		if(mTryLock || mServiceManager == null){
			mLockThread = Thread.currentThread();
			lock();
			throw new IPCTimeoutException("find remote service timeout");
		}
		
		return findService(name);
	}
	
	protected abstract IInterface findService(String name) throws IPCServiceNotFoundException,IPCTimeoutException;
	
}
