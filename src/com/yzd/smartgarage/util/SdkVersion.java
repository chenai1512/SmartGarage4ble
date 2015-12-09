package com.yzd.smartgarage.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.yzd.smartgarage.R;

public class SdkVersion {
	public static final int SDK_VERSION_ECLAIR = 5;
	public static final int SDK_VERSION_DONUT = 4;
	public static final int SDK_VERSION_CUPCAKE = 3;
	public static boolean PRE_CUPCAKE = 
	    	getSDKVersionNumber() < SDK_VERSION_CUPCAKE ? true : false;
	public static int getSDKVersionNumber() {
	  	int sdkVersion;
	  	try {
//	    	sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
	    	sdkVersion = android.os.Build.VERSION.SDK_INT;
	  	} catch (NumberFormatException e) {
	    	sdkVersion = 0;
	  	}
	  	return sdkVersion;
	}
	
	public static String getSYSVersionNumber() {
	  	String sysVersion;
	  	try {
//	    	sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
	  		sysVersion = android.os.Build.VERSION.RELEASE;
	  	} catch (NumberFormatException e) {
	  		sysVersion = "";
	  	}
	  	return sysVersion.substring(0, 5);
	}
	
	public static String getVersion(Context context)//»ñÈ¡°æ±¾ºÅ
	{
		try {
			PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pi.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return context.getString(R.string.version_unknown);
		}
	}
}
