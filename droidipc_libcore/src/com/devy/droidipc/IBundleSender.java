package com.devy.droidipc;

import android.os.Bundle;
import android.os.IInterface;
/**
 * ͳһ���̼�ͨ�Žӿ� 
 * @author wei.deng
 */
public interface IBundleSender extends IInterface{
	
	public Bundle send(Bundle bundle);
	
}
