package com.yzd.smartgarage.activity;

import android.app.Activity;
import android.content.Intent;

import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;

public class BaseActivity extends Activity {

	MyApplication myApplaction;

	protected void onResume() {
		super.onResume();
		myApplaction = (MyApplication) getApplication();
		if (myApplaction.isLocked) {
			LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
			if (lockPatternUtils.checksLock() && !myApplaction.isLocked) {
				Intent intent = new Intent(BaseActivity.this, SetLockActivity.class);
				intent.putExtra("intentFlag", 2);
				startActivity(intent);
			}
		}
	};

}
