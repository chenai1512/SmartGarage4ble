package com.yzd.smartgarage.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.LockPatternView;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.LockPatternView.Cell;
import com.yzd.smartgarage.util.LockPatternView.OnPatternListener;
import com.yzd.smartgarage.util.MyApplication;

public class SetLockActivity extends Activity {

	private LockPatternView lockPatternView;
	private LockPatternUtils lockPatternUtils;
	private boolean opFLag = true;
	private static String inipattern;
	private int flag = 0;
	private int intentFlag;
	
	MyApplication myApplaction;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_lock);
		
		NowActivity.setActivity(this);
		
		myApplaction = (MyApplication) getApplication();
		
		Intent intent = getIntent();
		intentFlag = intent.getIntExtra("intentFlag", 0);
		
		if (intentFlag == 2) {
			TextView forgetpwd = (TextView)findViewById(R.id.forgetpwd);
			forgetpwd.setVisibility(View.VISIBLE);
			
			TextView back = (TextView)findViewById(R.id.back);
			back.setVisibility(View.INVISIBLE);
		}

		lockPatternView = (LockPatternView) findViewById(R.id.lpv_lock);

		lockPatternUtils = new LockPatternUtils(this);

		lockPatternView.setOnPatternListener(new OnPatternListener() {

			public void onPatternStart() {

			}

			public void onPatternDetected(List<Cell> pattern) {
				if (intentFlag == 1) {
					//创建手势密码
					flag++;
					Toast.makeText(SetLockActivity.this, R.string.putpwdagain,
							Toast.LENGTH_SHORT).show();
					if (flag == 2) {
						if (inipattern.equals(LockPatternUtils.patternToString(pattern))) {
							lockPatternUtils.saveLockPattern(pattern);
							Toast.makeText(SetLockActivity.this, R.string.pwdok,
									Toast.LENGTH_SHORT).show();
							myApplaction.isLocked = false;
							finish();
							/*Intent intent = new Intent(SetLockActivity.this, MoreActivity.class);
							startActivity(intent);*/
						} else {
							flag = 0;
							Toast.makeText(SetLockActivity.this, R.string.retrypwd,
									Toast.LENGTH_SHORT).show();
						}
					} else {
						inipattern = LockPatternUtils.patternToString(pattern);
					}
				} else if (intentFlag == 2) {
					//检测手势密码
					if (lockPatternUtils.checkPattern(pattern) == 1) {
						myApplaction.isLocked = false;
						finish();
						/*Intent intent = new Intent();
						intent.putExtra("isCheck", 1);
			            intent.setClass(SetLockActivity.this, EquipmentManagementActivity.class);
			           startActivity(intent);*/
					} else {
						Toast.makeText(SetLockActivity.this, R.string.pwderror,
								Toast.LENGTH_SHORT).show();
					}
					
				} else  if (intentFlag == 3) {
					//
				}
				
			}

			public void onPatternCleared() {

			}

			public void onPatternCellAdded(List<Cell> pattern) {

			}
		});

	}
	
	public void back(View view) {
		Intent intent = new Intent();
		intent.setClass(this, MoreActivity.class);
		startActivity(intent);
	}


	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.setClass(this, MoreActivity.class);
		startActivity(intent);
	}


	@Override
	public void finish() {
		super.finish();
	}


	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
		MobclickAgent.onResume(this);
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	
	
	
}
