package com.devy.droidipc;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * {@link BundleSender} 主要负责向远端发送{@link Bundle}对象
 * @author wei.deng
 */
public final class BundleSender implements IBundleSender,IBinder.DeathRecipient{

	private IBinder mService;
	
	private BundleSender(IBinder binder){
		mService = binder;
	}
	
	public static IBundleSender asInterface(IBinder binder){
		BundleSender intentSender = new BundleSender(binder);
		return intentSender;
	}
	
	public IBinder asBinder() {
		return mService;
	}

	public Bundle send(Bundle bundle) {
		
		Parcel data = Parcel.obtain();
		data.writeBundle(bundle);
		
		Parcel reply = Parcel.obtain();
		
		try {
			mService.transact(ServiceContext.TRANSACT_BUNDLE_SENDER, data, reply, 0);
			
			Bundle result = reply.readBundle();
			result.setClassLoader(getClass().getClassLoader());
			return result;
			
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		} finally {
			data.recycle();
			reply.recycle();
		}
		
	}

	@Override
	public void binderDied() {
		
	}

}
