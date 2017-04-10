package com.devy.droidipc;

import android.annotation.SuppressLint;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * {@link BundleReceiver}主要负责接收由{@link BundleSender}发送的{@link Bundle}对象。
 * @author wei.deng
 */
@SuppressLint("NewApi") 
public abstract class BundleReceiver extends Binder implements IBundleReceiver{

	public IBinder asBinder() {
		return this;
	}
	
	@Override
	protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
			throws RemoteException {
		
		if(code == ServiceContext.TRANSACT_BUNDLE_SENDER) {
			Bundle bundle = data.readBundle();
			Bundle result = onReceiver(bundle);
			reply.writeBundle(result);
		}
		
		return true;
	}

	
}
