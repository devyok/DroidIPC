package com.devy.droidipc;

import android.os.Bundle;
import android.os.IInterface;
/**
 * ͳһ���̼�ͨ�Žӿ� 
 * @author wei.deng
 */
public interface IBundleReceiver extends IInterface{
	public Bundle onReceiver(Bundle bundle);
}
