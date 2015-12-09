package com.yzd.smartgarage.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.SdkVersion;

public class ShowCompanyActivity extends Activity {
	
	private final String TAG = this.getClass().getName();
	
	public boolean isStart = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_showcompany);
		
		MobclickAgent.updateOnlineConfig(this);
		AnalyticsConfig.enableEncrypt(true);
		
		if (SdkVersion.getSDKVersionNumber() < 18) {
			new AlertDialog.Builder(this)  
			                .setTitle("版本不匹配")
			                .setMessage("系统版本不兼容，需要安卓4.3以上，请升级系统！")
			                .setPositiveButton("确定", new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							})
			                .show();
			
			return;
		}
		
		getNextActivity();
		
		NowActivity.setActivity(this);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_company, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void getNextActivity() {
		
	    Timer timer=new Timer();
	
	    TimerTask task=new TimerTask(){
	
	        public void run(){
	
	            Intent intent = new Intent();
	            intent.setClass(ShowCompanyActivity.this, EquipmentManagementActivity.class);
	            isStart = true;
	            startActivity(intent);
	
	            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
	
	        }
	
	    };

	    timer.schedule(task, 3000);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		if (isStart) {
			Intent intent = new Intent();
	        intent.setClass(ShowCompanyActivity.this, EquipmentManagementActivity.class);

	       startActivity(intent);
		}
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	
}
