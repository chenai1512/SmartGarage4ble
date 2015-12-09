package com.yzd.smartgarage.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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

public class EditEquimentActivity extends Activity {

	int _id;
	String address;
	String key;
	String name;
	String img_address;

	public TextView devicechannel_btn;
	public EditText devicekey_btn;
	public EditText devicecontext_btn;

	private DBManager mgr;
	
	Garage garage;

	private Uri imageUri; // 图片路径
	private String filename; // 图片名称
	// 自定义变量
	public static final int TAKE_PHOTO = 1;
	public static final int CROP_PHOTO = 2;
	
	private static int CONTROLTYPE = 1;
	private ImageView show_equ_img;
	MyApplication myApplaction;
	InputMethodManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_equiment);
		
		NowActivity.setActivity(this);

		mgr = new DBManager(this);
		
		manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		Intent intent = getIntent();
		
		garage = (Garage)intent.getSerializableExtra("garage");

		_id = garage.get_id();
		address = garage.getAddress();
		key = garage.getKey();
		name = garage.getName();
		img_address = garage.getImg_address();
		Log.i("message", "img_address = " + img_address);
		imageUri = Uri.parse(img_address);
		devicechannel_btn = (TextView) findViewById(R.id.device_channel_edit);
		devicekey_btn = (EditText) findViewById(R.id.device_key_edit);
		devicecontext_btn = (EditText) findViewById(R.id.device_context_edit);

		devicechannel_btn.setText(address);
		devicekey_btn.setText(key);
		devicecontext_btn.setText(name);

		show_equ_img = (ImageView) findViewById(R.id.show_equ_img);

		if (img_address != null && img_address.length() > 0) {

			try {
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(Uri.parse(img_address)));
				show_equ_img.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		CONTROLTYPE = garage.getType();
		
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
			Toast.makeText(EditEquimentActivity.this,
					"ActivityResult resultCode error", Toast.LENGTH_SHORT)
					.show();
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
			Toast.makeText(EditEquimentActivity.this, R.string.cutpic,
					Toast.LENGTH_SHORT).show();
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
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_equiment, menu);
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
	
	public void editKey(View view) {
		key = devicekey_btn.getText().toString().trim();
		String key_new = ((EditText)findViewById(R.id.device_key_new_edit)).getText().toString().trim();
		if (key_new.trim().equals("")) {
			Toast.makeText(this, R.string.keynonull, Toast.LENGTH_SHORT).show();
			return;
		} else if (key_new.length() > 16) {
			Toast.makeText(this, R.string.keytolong, Toast.LENGTH_SHORT).show();
			return;
		}
//		key_new = "0000000000000000" + key_new;
//		key_new = key_new.substring(key_new.length() - 16, key_new.length());
		Intent intent = new Intent(EditEquimentActivity.this, OperatingActivity.class);
		intent.putExtra("flag", 1);
		intent.putExtra("key", key);
		intent.putExtra("newkey", key_new);
		intent.putExtra("id", garage.get_id());
		Bundle bundle = new Bundle();
		bundle.putSerializable("garage", garage);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	public void saveequ(View view) {

		address = devicechannel_btn.getText().toString().trim();
		key = devicekey_btn.getText().toString().trim();
		if (key.trim().equals("")) {
			Toast.makeText(this, R.string.keynonull, Toast.LENGTH_SHORT).show();
			return;
		} else if (key.length() > 16) {
			Toast.makeText(this, R.string.keytolong, Toast.LENGTH_SHORT).show();
			return;
		}
//		key = "0000000000000000" + key;
//		key = key.substring(key.length() - 16, key.length());
		name = devicecontext_btn.getText().toString().trim();

		Garage garage = new Garage(address, name, "", CONTROLTYPE, 1, "32432432", key,
				imageUri == null ? "" : imageUri.toString());
		garage.set_id(_id);
		mgr.update(garage);
		Intent intent = new Intent(EditEquimentActivity.this, EquipmentManagementActivity.class);
		startActivity(intent);
	}
	
	public void deleteequ(View view) {
		
		AlertDialog.Builder builer = new Builder(this);
		builer.setTitle("删除设备");
		builer.setMessage("删除不可恢复，是否确定删除？");
		 //当点确定按钮时从服务器上下载 新的apk 然后安装   װ
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mgr.deleteOldgarage(_id);
				Intent intent = new Intent(EditEquimentActivity.this, EquipmentManagementActivity.class);
				startActivity(intent);
			}
		});
		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
		
	}

	public void back(View view) {
		Intent intent = new Intent(EditEquimentActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(EditEquimentActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}
	
}
