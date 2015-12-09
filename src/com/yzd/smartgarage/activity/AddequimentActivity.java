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

	private Uri imageUri; // ͼƬ·��
	private String filename; // ͼƬ����
	// �Զ������
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
		// ͼƬ���� ʱ������
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Date date = new Date(System.currentTimeMillis());
		filename = format.format(date);
		// ����File�������ڴ洢���յ�ͼƬ SD����Ŀ¼
		// File outputImage = new
		// File(Environment.getExternalStorageDirectory(),"test.jpg");
		// �洢��DCIM�ļ���
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
		// ��File����ת��ΪUri�������������
		imageUri = Uri.fromFile(outputImage);
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE"); // ����
		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); // ָ��ͼƬ�����ַ
		startActivityForResult(intent, TAKE_PHOTO); // ��������
		// ������startActivityForResult() �������onActivityResult()����
	}

	@Override
	/** 
	 * ��Ϊ���ַ�ʽ���õ���startActivityForResult���� 
	 * �������ִ����󶼻�ִ��onActivityResult����, ����Ϊ�����𵽵�ѡ�����Ǹ���ʽ��ȡͼƬҪ�����ж�
	 * �����requestCode��startActivityForResult����ڶ���������Ӧ 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
//			Toast.makeText(AddequimentActivity.this,"ActivityResult resultCode error", Toast.LENGTH_SHORT).show();
			return;
		}
		switch (requestCode) {
		case TAKE_PHOTO:
			Intent intent = new Intent("com.android.camera.action.CROP"); // ����
			intent.setDataAndType(imageUri, "image/*");
			intent.putExtra("scale", true);
			// ���ÿ�߱���
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			// ���òü�ͼƬ���
			intent.putExtra("outputX", 340);
			intent.putExtra("outputY", 340);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			Toast.makeText(AddequimentActivity.this, R.string.cutpic, Toast.LENGTH_SHORT)
					.show();
			// �㲥ˢ�����
			Intent intentBc = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intentBc.setData(imageUri);
			this.sendBroadcast(intentBc);
			startActivityForResult(intent, CROP_PHOTO); // ���òü�������ʾͼƬ��ImageView
			break;
		case CROP_PHOTO:
			try {
				// ͼƬ������Bitmap����
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(imageUri));
				// Toast.makeText(AddequimentActivity.this,
				// imageUri.toString(),Toast.LENGTH_SHORT).show();
				show_equ_img.setImageBitmap(bitmap); // �����ú���Ƭ��ʾ����
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
		if (lockPatternUtils.checksLock() && myApplaction.isLocked) {// �ж��Ƿ���Ҫ��ת���������
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
				//����
				CONTROLTYPE = 2;
			} else if (radioId == R.id.jog){
				//�㶯
				CONTROLTYPE = 1;
			} else {
				
			}
			
			Log.i("message", "CONTROLTYPE = " + CONTROLTYPE);
		}
		
	}*/
	
	

}
