package com.devy.service_interfaces;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import com.devy.droidipc.BundleColumns;
import com.devy.droidipc.IBundleSender;

class IActivityServiceProxy implements IActivityServcie{

	private static IActivityServiceProxy sInstance = new IActivityServiceProxy();
	
	private IBundleSender bundleSender ;
	
	public static IActivityServcie asService(IInterface iInterface) {
		sInstance.bundleSender = (IBundleSender) iInterface;
		return sInstance;
	}
	
	@Override
	public IBinder asBinder() {
		return bundleSender.asBinder();
	}

	@Override
	public int startActivity(String className) {
		
		Bundle bundle = new Bundle();
		bundle.putString(BundleColumns.ACTION, ACTION_START_ACTIVITY);
		
		Bundle result = bundleSender.send(bundle);
		return result.getInt(BundleColumns.RESULT);
	}
	
	

}
