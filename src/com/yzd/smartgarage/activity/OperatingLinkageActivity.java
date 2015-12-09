package com.yzd.smartgarage.activity;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.yzd.smartgarage.R;
import com.yzd.smartgarage.entity.Garage;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.Protocol;

public class OperatingLinkageActivity extends Activity {

	MyApplication myApplaction;
	Garage garage;

	private ReceiveBroadCast receiveBroadCast;

	Intent broIntent;

	public boolean isLongClick = false;

	private static boolean clickable = true;

	ImageView open_btn;

	ImageView authorize_btn;

	TextView deviceState;
	
	String address = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_operating_linkage);

		NowActivity.setActivity(this);

		// 注册广播接收
		receiveBroadCast = new ReceiveBroadCast();
		IntentFilter filter = new IntentFilter();
		registerReceiver(receiveBroadCast, filter);

		broIntent = new Intent(); // Itent就是我们要发送的内容
		IntentFilter brofilter = new IntentFilter();
		brofilter.addAction("operate");

		open_btn = (ImageView) findViewById(R.id.open_linkage);
		authorize_btn = (ImageView) findViewById(R.id.authorize);

		open_btn.setOnClickListener(new ItemClick());
		authorize_btn.setOnClickListener(new ItemClick());

		ImageView show_equ_img = (ImageView) findViewById(R.id.show_equ_img);
		garage = (Garage) getIntent().getSerializableExtra("garage");
		String img_address = garage.getImg_address();
		
		address = garage.getAddress();

		deviceState = (TextView) findViewById(R.id.deviceState);

		setState(garage.getBondState());

		if (img_address != null && img_address.length() > 0) {

			try {
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(Uri.parse(img_address)));
				show_equ_img.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		// 注册广播接收
		ReceiveBroadCast receiveBroadCast = new ReceiveBroadCast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("statechange"); // 只有持有相同的action的接受者才能接收此广播
		registerReceiver(receiveBroadCast, intentFilter);

	}

	public void setState(int state) {
		deviceState.setText("");
		if (state == 0) {
			deviceState.setText(R.string.deviceState0);
		} else if (state == 1) {
			deviceState.setText(R.string.deviceState1);
		} else if (state == 2) {
			deviceState.setText(R.string.deviceState2);
		}
	}

	private class ItemClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.open_linkage:
				Log.i("message", "open");
				broIntent.putExtra("operate", Protocol.setOne_operate(address));
				broIntent.setAction("operate"); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
				sendBroadcast(broIntent); // 发送广播
				break;
			case R.id.authorize:
				if (clickable) {
					open_btn.setClickable(false);
					open_btn.setLongClickable(false);
					open_btn.setFocusable(false);

					clickable = false;
				} else {
					open_btn.setClickable(true);
					open_btn.setLongClickable(true);
					open_btn.setFocusable(true);

					clickable = true;
				}
				break;

			default:
				break;
			}
		}

	}

	public void editequiment(View view) {
		Intent intent = new Intent(OperatingLinkageActivity.this,
				EditEquimentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("garage", garage);
		intent.putExtras(bundle);
		startActivity(intent);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.operating, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void back(View view) {
		Intent intent = new Intent(OperatingLinkageActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {// TODO Auto-generated method stub
		super.onResume();
		myApplaction = (MyApplication) getApplication();
		LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
		if (lockPatternUtils.checksLock() && myApplaction.isLocked) {// 判断是否需要跳转到密码界面
			Intent intent = new Intent(this, SetLockActivity.class);
			intent.putExtra("intentFlag", 2);
			startActivity(intent);
		}
	}

	public class ReceiveBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 得到广播中得到的数据，并显示出来
			String address = intent.getStringExtra("address");
			int state = intent.getIntExtra("connectstate", 0);
			if (address.equals(garage.getAddress())) {
				setState(state);
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(receiveBroadCast);
	}

}
