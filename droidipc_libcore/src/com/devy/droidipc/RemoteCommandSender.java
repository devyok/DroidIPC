package com.devy.droidipc;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.devy.droidipc.LogControler.Level;
/**
 * 主要负责向远程服务端发送命令 
 * @author wei.deng
 */
@SuppressLint("NewApi") 
class RemoteCommandSender {

	public static boolean sendCommand(Context context,List<ResolveInfo> list,int cmd){
		
		int size = list.size();
		
		for(int i = 0 ;i < size;i++){
			sendCommand(context, list.get(i),cmd);
		}
		
		return true;
	}

	public static void sendCommand(Context context,ResolveInfo resolveInfo,int cmd) {
		
		ServiceInfo serviceInfo = resolveInfo.serviceInfo;
		String sender = serviceInfo.packageName;
		Intent intent = new Intent();
		intent.setAction(serviceInfo.packageName+"._SERVICE_");
		intent.setPackage(sender);
		Bundle binderBunder = new Bundle();
		binderBunder.putBinder(ServiceContext.EXTRA_BUNDLE_BINDER, ServiceManagerThread.getDefault());
		binderBunder.putString(ServiceContext.EXTRA_BUNDLE_PACKAGE_NAME, sender);
		
		intent.putExtra(ServiceContext.EXTRA_BUNDLE, binderBunder);
		intent.putExtra(ServiceContext.EXTRA_COMMAND, cmd);
		
		LogControler.print(Level.INFO, "[RemoteCommandSender] "+sender+" sendCommand start service pkg name = "+serviceInfo.packageName+", class name = " + serviceInfo.name);
		
		ComponentName componentName = context.startService(intent);
		
		LogControler.print(Level.INFO, "[RemoteCommandSender] "+sender+" sendCommand start service result = " + componentName);
	}
	
	public static void sendServerCommand(Context context,int cmd,IBinder callback){
		Intent intent = new Intent();
		intent.setAction(ServiceContext.ACTION_REMOTE_CLIENT_COMMAND);
		intent.setPackage(ServiceContext.SERVER_PACKAGE_NAME);
		
		String sender = context.getPackageName();
		
		Bundle binderBunder = new Bundle();
		binderBunder.putBinder(ServiceContext.EXTRA_BUNDLE_SERVER_READY_BINDER, callback);
		binderBunder.putString(ServiceContext.EXTRA_BUNDLE_PACKAGE_NAME, sender);
		
		intent.putExtra(ServiceContext.EXTRA_BUNDLE, binderBunder);
		intent.putExtra(ServiceContext.EXTRA_COMMAND, cmd);
		
		ComponentName componentName = context.startService(intent);
		
		LogControler.print(Level.INFO, "[RemoteCommandSender] "+sender+" sendServerCommand start service result = " + componentName);
	}
	
}
