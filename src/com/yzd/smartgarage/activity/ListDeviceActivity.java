package com.yzd.smartgarage.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.util.NowActivity;

@SuppressLint("NewApi")
public class ListDeviceActivity extends ListActivity {

	BluetoothAdapter mAdapter;
	private BluetoothAdapter mBluetoothAdapter = null;
	 private Handler mHandler;
	// Member object for the chat services
//	private BluetoothChatService mChatService = null;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	
	public static final int DEVICE_DATA = 3;
	// Debugging
	private static final String TAG = "BluetoothChat";
	private static final boolean D = true;
	public static final String TOAST = "toast";
	
	HashMap<String, Object> mapDevice;
	
	ArrayList<HashMap<String, Object>> menuList;
	
	SimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_device);
		
		NowActivity.setActivity(this);
		
		mHandler = new Handler();
		
		// 初始化一个蓝牙适配器。对API 18级以上，可以参考 bluetoothmanager。
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

       //  检查是否支持蓝牙的设备。
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,R.string.devicenosupport, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
		

		menuList = new ArrayList<HashMap<String, Object>>();
		
		mapDevice = new HashMap<String, Object>();
		

		adapter = new SimpleAdapter(this, menuList, R.layout.listitem_btdevice, new String[]{"deviceName", "deviceAddress"}, new int[]{R.id.deviceName, R.id.deviceAddress});
		
		ListDeviceActivity.this.setListAdapter(adapter);
		

		scanLeDevice(true);

	}
	
	public void refresh(View view) {
		mapDevice.clear();
		menuList.clear();
		adapter.notifyDataSetChanged();
		scanLeDevice(true);
	}


	@SuppressWarnings("deprecation")
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            //停止后一个预定义的扫描周期扫描。
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                    mScanning = false;
//                    bar.setVisibility(View.GONE);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            }, 10000);
//        	bar.setVisibility(View.VISIBLE);
//            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
//			bar.setVisibility(View.GONE);
//            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        
        invalidateOptionsMenu();
    }

  // 扫描装置的回调。
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                       Log.e("a", "RSSI=:"+rssi+"");
                       
                       for (HashMap<String, Object> map : menuList) {
       					if (map.get("deviceAddress").equals(device.getAddress())) {
       						return;
       					}
       				}
                       
                    mapDevice = new HashMap<String, Object>();
       				if (device.getName() != null) {
       					mapDevice.put("deviceName", device.getName());
       				} else {
       					mapDevice.put("deviceName", R.string.unknown_device);
       				}
       				if (device.getAddress() == null) {
       					mapDevice.put("deviceAddress", "00.00.00.00");
       				}
       				mapDevice.put("deviceAddress", device.getAddress());
       				menuList.add(mapDevice);
       				adapter.notifyDataSetChanged();
                       
                }
            });
        }
    };

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String deviceName = "";
		String deviceAddress = "";
		deviceName = (String) menuList.get(position).get("deviceName");
		deviceAddress = (String)menuList.get(position).get("deviceAddress");
		
		Intent intent = new Intent(ListDeviceActivity.this, AddequimentActivity.class);
		intent.putExtra("deviceName", deviceName == null ? "" : deviceName);
		intent.putExtra("deviceAddress", deviceAddress == null ? "" : deviceAddress);
//		setResult(RESULT_OK, intent);
		
		startActivity(intent);
		
		finish();
	}

	@Override
	protected void onDestroy() {
//		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	

}
