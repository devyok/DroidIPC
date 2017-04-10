package com.devy.droidipc;

import android.annotation.SuppressLint;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
/**
 * BundleSendDecorator��Ҫ����װ��{@link IBundleSender}<br>
 * ʵ�ַ���Զ�̻ص�{@link IBinder}
 * @author wei.deng
 */
@SuppressLint("NewApi") 
@Deprecated
public class BundleSendDecorator implements IBundleSender{

	private IBundleSender mBundleSender;
	private RemoteCallback mCallback;
	
	public BundleSendDecorator(IBundleSender bundleSender) {
		mBundleSender = bundleSender;
	}
	
	@Override
	public IBinder asBinder() {
		return null;
	}
	
	public void setCallback(RemoteCallback callback){
		mCallback = callback;
	}

	@Override
	public Bundle send(Bundle bundle) {
		
		bundle.putBinder(ServiceContext.EXTRA_CALLBACK, new IBinderCallback(mCallback));
		
		return mBundleSender.send(bundle);
	}

	private class IBinderCallback extends Binder{
		
		private RemoteCallback mRemoteCallback;
		
		public IBinderCallback(RemoteCallback remoteCallback){
			mRemoteCallback = remoteCallback;
		}
		
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
				throws RemoteException {
			
			if(mRemoteCallback != null && code == ServiceContext.CALLBACK){
				
				Bundle bundle = data.readBundle();
				
				Bundle result = mRemoteCallback.handle(bundle);
				
				reply.writeBundle(result);
				
			}
			
			return true;
		}

	}
	
	
}
