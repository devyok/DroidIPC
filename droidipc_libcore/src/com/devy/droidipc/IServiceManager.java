package com.devy.droidipc;

import com.devy.droidipc.exception.IPCServiceNotFoundException;
import com.devy.droidipc.exception.IPCTimeoutException;

import android.os.IBinder;
/**
 * ͳһ�ӿ�
 * @author wei.deng
 */
public interface IServiceManager {

	public int addService(String name,IBinder service);
	
	public IBinder getService(String name) throws IPCServiceNotFoundException , IPCTimeoutException;
	
}
