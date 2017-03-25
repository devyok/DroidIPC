package com.devy.service_interfaces;

import android.os.IInterface;

public interface IActivityServcie extends IInterface{

	public static final String ACTION_START_ACTIVITY = "com.devyok.action.START_ACTIVITY"; 
	
	public int startActivity(String className);
	
}
