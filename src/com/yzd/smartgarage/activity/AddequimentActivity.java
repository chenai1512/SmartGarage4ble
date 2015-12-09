package com.yzd.smartgarage.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.db.DBManager;
import com.yzd.smartgarage.entity.Garage;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;
import com.yzd.smartgarage.util.NowActivity;

public class AddequimentActivity extends Activity {

	public TextView devicechannel_btn;
	public EditText devicekey_btn;
	public EditText devicecontext_btn;

	private DBManager mgr;

	InputMethodManager manager;

	private Uri imageUri; // 图片路径
	private String filename; // 图片名称
	// 自定义变量
	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO = 2;
	public static final int DEVICE_DATA = 3;
	private ImageView show_equ_img;
	
	private static int CONTROLTYPE = 1;

	MyApplication myApplaction;
	
	TextView showmessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addequiment);
		
		NowActivity.setActivity(this);

		mgr = new DBManager(this);

		devicechannel_btn = (TextView) findViewById(R.id.device_channel_edit);
		devicekey_btn = (EditText) findViewById(R.id.device_key_edit);
		devicecontext_btn = (EditText) findViewById(R.id.device_context_edit);
		show_equ_img = (ImageView) findViewById(R.id.show_equ_img);
		showmessage = (TextView)findViewById(R.id.showmessage);

		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
//		RadioGroup controlType = (RadioGroup)findViewById(R.id.controltype);
//		controlType.setOnCheckedChangeListener(new checkedChangeListener());
		
		String deviceaddress = getIntent().getStringExtra("deviceAddress");
		String devicename = getIntent().getStringExtra("deviceName");
		devicechannel_btn.setText(deviceaddress);
		devicecontext_btn.setText(devicename);
		
	}

	public void startCamera(View view) {
		// 图片名称 时间命名
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(System.currentTimeMillis());
		filename = format.format(date);
		// 创建File对象用于存储拍照的图片 SD卡根目录
		// File outputImage = new
		// File(Environment.getExternalStorageDirectory(),"test.jpg");
		// 存储至DCIM文件夹
		File path = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		File outputImage = new File(path, filename + ".jpg");
		try {
			if (outputImage.exists()) {
				outputImage.delete();
			}
			outputImage.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 将File对象转换为Uri并启动照相程序
		imageUri = Uri.fromFile(outputImage);
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); // 照相
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // 指定图片输出地址
		startActivityForResult(intent, TAKE_PHOTO); // 启动照相
		// 拍完照startActivityForResult() 结果返回onActivityResult()函数
	}

	@Override
	/** 
	 * 因为两种方式都用到了startActivityForResult方法 
	 * 这个方法执行完后都会执行onActivityResult方法, 所以为了区别到底选择了那个方式获取图片要进行判断
	 * 这里的requestCode跟startActivityForResult里面第二个参数对应 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
//			Toast.makeText(AddequimentActivity.this,"ActivityResult resultCode error", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (requestCode) {
		case TAKE_PHOTO:
			Intent intent = new Intent("com.android.camera.action.CROP"); // 剪裁
			intent.setDataAndType(imageUri, "image/*");
			intent.putExtra("scale", true);
			// 设置宽高比例
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// 设置裁剪图片宽高
			intent.putExtra("outputX", 340);
			intent.putExtra("outputY", 340);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			Toast.makeText(AddequimentActivity.this, R.string.cutpic, Toast.LENGTH_SHORT)
					.show();
			// 广播刷新相册
			Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intentBc.setData(imageUri);
			this.sendBroadcast(intentBc);
			startActivityForResult(intent, CROP_PHOTO); // 设置裁剪参数显示图片至ImageView
			break;
		case CROP_PHOTO:
			try {
				// 图片解析成Bitmap对象
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(imageUri));
				// Toast.makeText(AddequimentActivity.this,
				// imageUri.toString(),Toast.LENGTH_SHORT).show();
				show_equ_img.setImageBitmap(bitmap); // 将剪裁后照片显示出来
				Log.i("message", "imageUriadd = " + imageUri.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			break;
		case DEVICE_DATA:
//			devicechannel_btn.setText("");
//			devicecontext_btn.setText("");
//			Bundle devicedata = data.getExtras();
//			String deviceaddress = devicedata.getString("deviceAddress");
//			String devicename = devicedata.getString("deviceName");
//			devicechannel_btn.setText(deviceaddress);
//			devicecontext_btn.setText(devicename);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.addequiment, menu);
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

	public void saveequ(View view) {

		String address = devicechannel_btn.getText().toString();
		
		if (checkAddress(address)) {
			showmessage.setVisibility(0x00000000);
			return;
		}
		
		String key = devicekey_btn.getText().toString();
		
		if (key.trim().equals("")) {
			Toast.makeText(this, R.string.keynonull, Toast.LENGTH_SHORT).show();
			return;
		} else if (key.length() > 16) {
			Toast.makeText(this, R.string.keytolong, Toast.LENGTH_SHORT).show();
			return;
		}
		
//		key = "0000000000000000" + key;
//		key = key.substring(key.length() - 16, key.length());
		
		String name = devicecontext_btn.getText().toString();

		ArrayList<Garage> garages = new ArrayList<Garage>();
		Garage garage = new Garage(address, name, "", CONTROLTYPE, 0, "32432432", key,
				imageUri == null ? "" : imageUri.toString());
		garages.add(garage);
		mgr.add(garages);
		Intent intent = new Intent(AddequimentActivity.this, EquipmentManagementActivity.class);
		startActivity(intent);
	}

	public void back(View view) {
		Intent intent = new Intent(AddequimentActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}
	
	/*public void savefromscan(View view) {
		Intent intent = new Intent(AddequimentActivity.this, ListDeviceActivity.class);
		startActivityForResult(intent, DEVICE_DATA);
		
	}*/

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (getCurrentFocus() != null
					&& getCurrentFocus().getWindowToken() != null) {
				manager.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		myApplaction = (MyApplication) getApplication();
		LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
		if (lockPatternUtils.checksLock() && myApplaction.isLocked) {// 判断是否需要跳转到密码界面
			Intent intent = new Intent(this, SetLockActivity.class);
			intent.putExtra("intentFlag", 2);
			startActivity(intent);
		}
	}
	
	public boolean checkAddress(String address) {
		List<Garage> garages = mgr.query();
		for (Garage g : garages) {
			if (g.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	/*private class checkedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			int radioId = group.getCheckedRadioButtonId();
			if (radioId==R.id.linkage) {
				//联动
				CONTROLTYPE = 2;
			} else if (radioId == R.id.jog){
				//点动
				CONTROLTYPE = 1;
			} else {
				
			}
			
			Log.i("message", "CONTROLTYPE = " + CONTROLTYPE);
		}
		
	}*/
	
	

}
