package com.devy.droidipc;

import android.os.Bundle;
import android.os.IInterface;
/**
 * 统一进程间通信接口 
 * @author wei.deng
 */
public interface IBundleSender extends IInterface{
	
	public Bundle send(Bundle bundle);
	
}
