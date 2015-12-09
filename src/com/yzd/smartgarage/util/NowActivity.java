package com.yzd.smartgarage.util;

import android.content.Context;

public class NowActivity {
	public static Context nowActivity;
	
	public static void setActivity(Context context) {
		nowActivity = context;
	}
	
	public static Context getActivity () {
		return nowActivity;
	}
}
