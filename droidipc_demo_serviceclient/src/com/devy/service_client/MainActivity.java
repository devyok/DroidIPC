package com.devy.service_client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.devy.droidipc.LogControler;
import com.devy.droidipc.ServiceManager;
import com.devy.droidipc.exception.IPCServiceNotFoundException;
import com.devy.droidipc.exception.IPCTimeoutException;
import com.devy.service_interfaces.IActivityServcie;
import com.devy.service_interfaces.IPC;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ServiceManager.init(getApplicationContext());
		
		this.findViewById(R.id.demo_id).setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				
				try {
					IActivityServcie activityServcie = (IActivityServcie)IPC.proxyService(IPC.Context.ACTIVITY_SERVICE);
					int result = activityServcie.startActivity(MainActivity.class.getName());
					
					Log.i(LogControler.TAG, "[client] invoke remove service result = " + result);
				} catch (IPCServiceNotFoundException e) {
					e.printStackTrace();
					
					Log.i(LogControler.TAG, "[client] IPCServiceNotFoundException");
					
				} catch (IPCTimeoutException e) {
					e.printStackTrace();
					
					Log.i(LogControler.TAG, "[client] IPCTimeoutException");
				}
			}
		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
}
