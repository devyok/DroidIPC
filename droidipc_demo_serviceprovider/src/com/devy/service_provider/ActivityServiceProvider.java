package com.devy.service_provider;

import android.os.Bundle;
import android.util.Log;

import com.devy.droidipc.BundleColumns;
import com.devy.droidipc.BundleReceiver;
import com.devy.droidipc.LogControler;

public class ActivityServiceProvider extends BundleReceiver {

	public Bundle onReceiver(Bundle bundle) {
		
		Bundle result = new Bundle();
		
		String action = bundle.getString(BundleColumns.ACTION);
		
		Log.i(LogControler.TAG, "[service provider] receive action = " + action);
		
		result.putInt(BundleColumns.RESULT, 10010);
		
		return result;
	}

}
