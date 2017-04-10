package com.devy.service_provider;

import android.app.Activity;
import android.os.Bundle;

import com.devy.service_provider.R;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
