package com.yzd.smartgarage.util;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MyApplication extends Application {

	public boolean isLocked = true;

	LockScreenReceiver receiver ;
	IntentFilter filter ;
	@Override
	public void onCreate() {
		super.onCreate();
		receiver = new LockScreenReceiver();
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(receiver, filter);

	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		this.unregisterReceiver(receiver);
	}
	

	class LockScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			/* 在这里处理广播 */
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				isLocked  = true;
			}
		}
	}

}
