package com.devy.droidipc;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * BundleReceiveDecorator主要负责装饰{@link BundleReceiver}<br>
 * 实现接收远程回调，回调的{@link IBinder}由{@link BundleSendDecorator}发出；
 * @author wei.deng
 */
@SuppressLint("NewApi")
@Deprecated
public abstract class BundleReceiveDecorator extends BundleReceiver{

	@Override
	protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		
		if(code == ServiceContext.TRANSACT_BUNDLE_SENDER) {
			Bundle bundle = data.readBundle();
			
			IBinder binder = bundle.getBinder(ServiceContext.EXTRA_CALLBACK);
			
			Bundle result = onReceiver(bundle,new IBinderCallback(binder));
			reply.writeBundle(result);
		}
		
		return true;
	}
	
	public Bundle onReceiver(Bundle bundle){
		return Bundle.EMPTY;
	}
	
	public abstract Bundle onReceiver(Bundle bundle,RemoteCallback callback);
	
	private class IBinderCallback implements RemoteCallback{
		
		private IBinder mRemoteCallback;
		
		public IBinderCallback(IBinder remoteCallback){
			mRemoteCallback = remoteCallback;
		}

		@Override
		public Bundle handle(Bundle bundle) {
			
			Parcel data = Parcel.obtain();
			Parcel reply = Parcel.obtain();
			
			data.writeBundle(bundle);
			
			try {
				mRemoteCallback.transact(ServiceContext.CALLBACK, data, reply, 0);
				
				Bundle result = reply.readBundle();
				
				return result;
				
			} catch (RemoteException e) {
				e.printStackTrace();
			} finally {
				
				data.recycle();
				reply.recycle();
				
			}
			
			return Bundle.EMPTY;
		}
		
		

	}
	
}
