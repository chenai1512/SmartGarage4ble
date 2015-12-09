package com.yzd.smartgarage.activity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.db.DBManager;
import com.yzd.smartgarage.entity.Garage;
import com.yzd.smartgarage.util.H2S;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.Protocol;
import com.yzd.smartgarage.util.SampleGattAttributes;

public class OperatingActivity extends Activity {

	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	MyApplication myApplaction;
	Garage garage;

	ImageView open_btn;

	ImageView close_btn;

	ImageView stop_btn;

	ImageView authorize_btn;

	TextView deviceState;

	ImageView show_equ_img;

	private DBManager mgr;

	String key = "";

	String key_new = "";
	int _id = 0;

	private boolean result;

	private int clickCount = 0;

	private boolean isAllowClick = true;

	private boolean flg;

	private BluetoothLeService mBluetoothLeService;

	private BluetoothGattCharacteristic mNotifyCharacteristic;

	private final static String UUID_KEY_DATA = "00002af1-0000-1000-8000-00805f9b34fb";

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	String _protocol = "";

	private ArrayList<BluetoothGattCharacteristic> charas;
	@SuppressLint("NewApi")
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				result = true;
				clickCount = 0;
				setState(1);
				if (getIntent().getIntExtra("flag", 0) == 1) {
					String key = getIntent().getStringExtra("key");
					key_new = getIntent().getStringExtra("newkey");
					_id = getIntent().getIntExtra("id", 0);
					int leng = key.length();
					String lengStr = "04";
					if (leng < 10) {
						lengStr = "0" + leng;
					}
					sendLongPT("AAAA0000" + Garage.getKey(key) + "A046" + "10"
							+ H2S.Str2ACSIstr(Garage.getKey(key_new)) + "11EE");
				} else {
					sendLongPT("AAAA0000" + Garage.getKey(key) + "A049" + "10"
							+ H2S.Str2ACSIstr(Garage.getKey(key)) + "11EE");
				}
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
					.equals(action)) {
				result = false;
				mBluetoothLeService.close();
				mBluetoothLeService.disconnect();
				setState(2);
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				List<BluetoothGattService> supportedGattServices = mBluetoothLeService
						.getSupportedGattServices();
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());

				for (int i = 0; i < supportedGattServices.size(); i++) {
					Log.e("a", "1:BluetoothGattService UUID=:"
							+ supportedGattServices.get(i).getUuid());
					List<BluetoothGattCharacteristic> cs = supportedGattServices
							.get(i).getCharacteristics();
					for (int j = 0; j < cs.size(); j++) {
						Log.e("a", "2:BluetoothGattCharacteristic UUID=:"
								+ cs.get(j).getUuid());

						List<BluetoothGattDescriptor> ds = cs.get(j)
								.getDescriptors();
						for (int f = 0; f < ds.size(); f++) {
							Log.e("a", "3:BluetoothGattDescriptor UUID=:"
									+ ds.get(f).getUuid());

							byte[] value = ds.get(f).getValue();

							Log.e("a", "4:value=:" + Arrays.toString(value));
							Log.e("a",
									"5:value=:"
											+ Arrays.toString(ds.get(f)
													.getCharacteristic()
													.getValue()));
						}
					}
				}

			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

				String returnStr = intent.getStringExtra(EXTRA_DATA);

				if (returnStr.length() > 10) {
					try {
						int op = Integer.parseInt(Protocol.getOP(returnStr));
						int ok = Integer.parseInt(Protocol.getOK(returnStr));

						switch (op) {
						case 41:
							switch (ok) {
							case 81:
								final EditText inputServer1 = new EditText(
										OperatingActivity.this);
								AlertDialog.Builder builder1 = new AlertDialog.Builder(
										OperatingActivity.this);
								builder1.setTitle(R.string.key_rong)
										.setIcon(
												android.R.drawable.ic_dialog_info)
										.setView(inputServer1)
										.setNegativeButton(R.string.cancle,
												null);
								builder1.setPositiveButton(R.string.sure,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {
												if (inputServer1.getText()
														.toString().trim()
														.length() == 0) {
													Toast.makeText(
															OperatingActivity.this,
															R.string.keynoempty,
															Toast.LENGTH_SHORT)
															.show();
													return;
												}
												key = inputServer1.getText()
														.toString().trim();
												garage = new Garage(garage
														.getAddress(), garage
														.getName(), "", garage
														.getType(), 1,
														"32432432", key,
														garage.getImg_address());
												garage.set_id(_id);
												result = mBluetoothLeService
														.connect(garage
																.getAddress());
											}
										});
								builder1.show();
								mBluetoothLeService.close();
								mBluetoothLeService.disconnect();
								break;
							case 82:
								Toast.makeText(OperatingActivity.this,
										R.string.devicelock, Toast.LENGTH_SHORT)
										.show();
								break;
							case 80:
								break;
							default:
								break;
							}
							break;
						case 42:
							switch (ok) {
							case 81:
								final EditText inputServer1 = new EditText(
										OperatingActivity.this);
								AlertDialog.Builder builder1 = new AlertDialog.Builder(
										OperatingActivity.this);
								builder1.setTitle(R.string.key_rong)
										.setIcon(
												android.R.drawable.ic_dialog_info)
										.setView(inputServer1)
										.setNegativeButton(R.string.cancle,
												null);
								builder1.setPositiveButton(R.string.sure,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {
												// inputServer.getText().toString();
												if (inputServer1.getText()
														.toString().trim()
														.length() == 0) {
													Toast.makeText(
															OperatingActivity.this,
															R.string.keynoempty,
															Toast.LENGTH_SHORT)
															.show();
													return;
												}
												key = inputServer1.getText()
														.toString().trim();
												garage = new Garage(garage
														.getAddress(), garage
														.getName(), "", garage
														.getType(), 1,
														"32432432", key,
														garage.getImg_address());
												garage.set_id(_id);
												result = mBluetoothLeService
														.connect(garage
																.getAddress());
											}
										});
								builder1.show();
								mBluetoothLeService.close();
								mBluetoothLeService.disconnect();
								break;
							case 82:
								Toast.makeText(OperatingActivity.this,
										R.string.devicelock, Toast.LENGTH_SHORT)
										.show();
								break;
							case 80:
								break;
							default:
								break;
							}
							break;
						case 46:
							switch (ok) {
							case 80:

								garage = new Garage(garage.getAddress(),
										garage.getName(), "", garage.getType(),
										1, "32432432", key_new,
										garage.getImg_address());
								garage.set_id(_id);
								key = key_new;
								mgr.update(garage);
								Toast.makeText(OperatingActivity.this,
										R.string.editkey_ok, Toast.LENGTH_SHORT)
										.show();
								break;
							case 81:
								Toast.makeText(OperatingActivity.this,
										R.string.editkey_false,
										Toast.LENGTH_SHORT).show();
							default:
								break;
							}
							break;
						case 49:
							switch (ok) {
							case 80:

								break;
							case 81:
								final EditText inputServer = new EditText(
										OperatingActivity.this);
								AlertDialog.Builder builder = new AlertDialog.Builder(
										OperatingActivity.this);
								builder.setTitle(R.string.key_rong)
										.setIcon(
												android.R.drawable.ic_dialog_info)
										.setView(inputServer)
										.setNegativeButton(R.string.cancle,
												null);
								builder.setPositiveButton(R.string.sure,
										new DialogInterface.OnClickListener() {

											public void onClick(
													DialogInterface dialog,
													int which) {
												// inputServer.getText().toString();
												if (inputServer.getText()
														.toString().trim()
														.length() == 0) {
													Toast.makeText(
															OperatingActivity.this,
															R.string.keynoempty,
															Toast.LENGTH_SHORT)
															.show();
													return;
												}
												key = inputServer.getText()
														.toString().trim();
												garage = new Garage(garage
														.getAddress(), garage
														.getName(), "", garage
														.getType(), 1,
														"32432432", key,
														garage.getImg_address());
												garage.set_id(_id);
												mgr.update(garage);
												result = mBluetoothLeService
														.connect(garage
																.getAddress());
											}
										});
								builder.show();
								mBluetoothLeService.close();
								mBluetoothLeService.disconnect();
							default:
								break;
							}
							break;
						default:
							break;
						}
					} catch (Exception e) {
					}

				}

			} else if (BluetoothLeService.ACTION_RSSI.equals(action)) {
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_operating);

		Log.i("message", "OperatingActivity onCreate");

		NowActivity.setActivity(this);

		mgr = new DBManager(this);

		open_btn = (ImageView) findViewById(R.id.open);
		close_btn = (ImageView) findViewById(R.id.close);
		stop_btn = (ImageView) findViewById(R.id.stop);
		authorize_btn = (ImageView) findViewById(R.id.authorize);

		open_btn.setOnClickListener(new ItemClick());
		close_btn.setOnClickListener(new ItemClick());
		stop_btn.setOnClickListener(new ItemClick());
		authorize_btn.setOnClickListener(new ItemClick());

		// open_btn.setOnLongClickListener(new ItemLongClick());
		// close_btn.setOnLongClickListener(new ItemLongClick());
		// authorize_btn.setOnLongClickListener(new ItemLongClick());
		//
		// open_btn.setOnTouchListener(new touckEvent());
		// close_btn.setOnTouchListener(new touckEvent());
		// authorize_btn.setOnTouchListener(new touckEvent());

		TextView title = (TextView) findViewById(R.id.garagename);

		show_equ_img = (ImageView) findViewById(R.id.show_equ_img);
		garage = (Garage) getIntent().getSerializableExtra("garage");

		_id = garage.get_id();

		key = garage.getKey();

		deviceState = (TextView) findViewById(R.id.deviceState);

		title.setText(garage.getName());

		String img_address = garage.getImg_address();

		if (img_address != null && img_address.length() > 0) {

			try {
				Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(Uri.parse(img_address)));
				show_equ_img.setImageBitmap(bitmap);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		show_equ_img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i("message", "鐐瑰嚮浜�");

				clickCount = clickCount + 1;
				Log.i("message", clickCount + "");
				if (clickCount == 3) {
					clickCount = 0;
					// setState(4);
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					mBluetoothAdapter.disable();
					show_equ_img.setEnabled(false);
				}

				if (!result) {
					setState(3);
					result = mBluetoothLeService.connect(garage.getAddress());
				}
			}
		});

		show_equ_img.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();

				mBluetoothAdapter.disable();
				show_equ_img.setEnabled(false);

				return true;
			}
		});

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		Intent gattServiceIntent = new Intent(OperatingActivity.this,
				BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(
				BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, filter);

		setState(3);

	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				finish();
			}

			result = mBluetoothLeService.connect(garage.getAddress());

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService.close();
			mBluetoothLeService.disconnect();
			mBluetoothLeService = null;
		}
	};

	public void setState(int state) {
		if (state == 0) {
			deviceState.setText(R.string.deviceState0);
		} else if (state == 1) {

			deviceState.setText(R.string.deviceState1);
		} else if (state == 2) {
			deviceState.setText(R.string.deviceState2);
		} else if (state == 3) {
			deviceState.setText(R.string.deviceState3);
		} else if (state == 4) {
			deviceState.setText(R.string.deviceState4);
		}
	}

	private class ItemClick implements OnClickListener {
		boolean success = false;
		int count = 0;

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.open:
				Log.i("message", "ItemClick");
				Log.i("message", "open");
				Log.i("message", garage.getKey());
				Log.i("message", Garage.getKey(garage.getKey()));
				success = mBluetoothLeService.write(mNotifyCharacteristic,
						Protocol.setOpen_jog(Garage.getKey(garage.getKey())));
				if (!success) {
					count = count + 1;
				}
				if (result && !success && (count == 3)) {
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					mBluetoothAdapter.disable();
					show_equ_img.setEnabled(false);
				}
				break;
			case R.id.close:
				Log.i("message", garage.getKey());
				Log.i("message", Garage.getKey(garage.getKey()));
				Log.i("message", "close");
				success = mBluetoothLeService.write(mNotifyCharacteristic,
						Protocol.setClose_jog(Garage.getKey(garage.getKey())));
				if (!success) {
					count = count + 1;
				}
				if (result && !success && (count == 3)) {
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					mBluetoothAdapter.disable();
					show_equ_img.setEnabled(false);
				}
				break;
			case R.id.stop:
				Log.i("message", garage.getKey());
				Log.i("message", Garage.getKey(garage.getKey()));
				Log.i("message", "stop");
				success = mBluetoothLeService.write(mNotifyCharacteristic,
						Protocol.setStop_jog(Garage.getKey(garage.getKey())));
				if (!success) {
					count = count + 1;
				}
				if (result && !success && (count == 3)) {
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					mBluetoothAdapter.disable();
					show_equ_img.setEnabled(false);
				}
				break;
			case R.id.authorize:
				Log.i("message", garage.getKey());
				Log.i("message", Garage.getKey(garage.getKey()));
				success = mBluetoothLeService.write(mNotifyCharacteristic,
						Protocol.setClock_jog(Garage.getKey(garage.getKey())));
				if (!success) {
					count = count + 1;
				}
				if (result && !success && (count == 3)) {
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();

					mBluetoothAdapter.disable();
					show_equ_img.setEnabled(false);
				}
				break;

			default:
				break;
			}
		}

	}

	/*
	 * private class ItemLongClick implements OnLongClickListener {
	 * 
	 * @Override public boolean onLongClick(View v) { switch (v.getId()) { case
	 * R.id.open: Log.i("message", "open-linkage");
	 * mBluetoothLeService.write(mNotifyCharacteristic,
	 * Protocol.setOpen_linkage(Garage.getKey(key))); break; case R.id.close:
	 * Log.i("message", "close-linkage");
	 * mBluetoothLeService.write(mNotifyCharacteristic,
	 * Protocol.setClose_linkage(Garage.getKey(key))); break; case R.id.stop:
	 * break; case R.id.authorize: break;
	 * 
	 * default: break; } return true; }
	 * 
	 * }
	 */

	/*
	 * private class touckEvent implements OnTouchListener {
	 * 
	 * @Override public boolean onTouch(View v, MotionEvent event) { if
	 * (event.getAction() == MotionEvent.ACTION_UP && isLongClick) { switch
	 * (v.getId()) { case R.id.open: Log.i("message", "open-linkage");
	 * mBluetoothLeService.write(mNotifyCharacteristic,
	 * Protocol.setOpen_linkage(Garage.getKey(key))); isLongClick = false;
	 * break; case R.id.close: Log.i("message", "close-linkage");
	 * mBluetoothLeService.write(mNotifyCharacteristic,
	 * Protocol.setClose_linkage(Garage.getKey(key))); isLongClick = false;
	 * break; default: break; } } return false; }
	 * 
	 * }
	 */

	public void editequiment(View view) {
		Intent intent = new Intent(OperatingActivity.this,
				EditEquimentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("garage", garage);
		intent.putExtras(bundle);
		startActivity(intent);
		mBluetoothLeService.close();
		mBluetoothLeService.disconnect();
		unregisterReceiver(mGattUpdateReceiver);
		onDestroy();
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

	@Override
	protected void onResume() {// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		myApplaction = (MyApplication) getApplication();
		LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
		if (lockPatternUtils.checksLock() && myApplaction.isLocked) {
			Intent intent = new Intent(this, SetLockActivity.class);
			intent.putExtra("intentFlag", 2);
			startActivity(intent);
		}

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			result = mBluetoothLeService.connect(garage.getAddress());

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
		flg = false;
		// unregisterReceiver(mGattUpdateReceiver);
	}

	protected void onDestroy() {
		super.onDestroy();
		Log.v("MainStadyServics", "out");
		unbindService(mServiceConnection);
		unregisterReceiver(mReceiver);
	}

	@SuppressLint("NewApi")
	private void displayGattServices(List<BluetoothGattService> gattServices) {

		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = "service_UUID";
		String unknownCharaString = "characteristic_UUID";
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			currentServiceData.put("NAME",
					SampleGattAttributes.lookup(uuid, unknownServiceString));
			currentServiceData.put("UUID", uuid);
			gattServiceData.add(currentServiceData);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();
			charas = new ArrayList<BluetoothGattCharacteristic>();

			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put("NAME",
						SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put("UUID", uuid);
				gattCharacteristicGroupData.add(currentCharaData);
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}
		if (charas.size() == 0) {
			finish();
		}
		final BluetoothGattCharacteristic characteristic = charas.get(charas
				.size() - 1);
		final int charaProp = characteristic.getProperties();
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
			if (mNotifyCharacteristic != null) {
				mBluetoothLeService.setCharacteristicNotification(
						mNotifyCharacteristic, false);
				mNotifyCharacteristic = null;
			}
			mBluetoothLeService.readCharacteristic(characteristic);

		}
		if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
			mNotifyCharacteristic = characteristic;
			mBluetoothLeService.setCharacteristicNotification(characteristic,
					true);
		}

	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_RSSI);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_RSSI);
		return intentFilter;
	}

	static private List<Integer> getElement(int number) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < 32; i++) {
			int b = 1 << i;
			if ((number & b) > 0)
				result.add(b);
		}

		return result;
	}

	public void sendLongPT(String protocol) {
		_protocol = protocol;
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (_protocol.length() < 40) {
					mBluetoothLeService.write(mNotifyCharacteristic, _protocol);
					timer.cancel();
				} else {
					mBluetoothLeService.write(mNotifyCharacteristic,
							_protocol.substring(0, 40));
					_protocol = _protocol.substring(40);
				}
			}
		}, 1000, 20);
	}

	public void back(View view) {

		if (result) {
			result = false;
		}
		mBluetoothLeService.close();
		mBluetoothLeService.disconnect();
		unregisterReceiver(mGattUpdateReceiver);
		Intent intent = new Intent();
		intent.setClass(this, EquipmentManagementActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (result) {
			result = false;
		}
		unregisterReceiver(mGattUpdateReceiver);
		mBluetoothLeService.close();
		mBluetoothLeService.disconnect();
		Intent intent = new Intent();
		intent.setClass(this, EquipmentManagementActivity.class);
		startActivity(intent);
		finish();
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				Bundle b = intent.getExtras();
				int state = b.getInt(BluetoothAdapter.EXTRA_STATE);
				if (state == BluetoothAdapter.STATE_OFF) {
					setState(4);
					// result = false;
					BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
							.getDefaultAdapter();
					if (!mBluetoothAdapter.isEnabled()) {
						if (!mBluetoothAdapter.enable()) {
							Intent mIntent = new Intent(
									BluetoothAdapter.ACTION_REQUEST_ENABLE);
							startActivityForResult(mIntent, 1);

						}
						;

					}
				} else if (state == BluetoothAdapter.STATE_ON) {
					setState(3);
					show_equ_img.setEnabled(true);
					Timer timer = new Timer();

					TimerTask task = new TimerTask() {

						public void run() {
							result = mBluetoothLeService.connect(garage
									.getAddress());
						}

					};

					timer.schedule(task, 1000);
				}
			}
		}
	};

}
