package com.yzd.smartgarage.activity;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.yzd.smartgarage.R;
import com.yzd.smartgarage.entity.UpdataInfo;
import com.yzd.smartgarage.util.DownLoadManager;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.UpdataInfoParser;

public class BTService extends Service {

	public static Map<String, Integer> state_map = new HashMap<String, Integer>();

	private BluetoothAdapter mBluetoothAdapter = null;
	
	static Map<String, BluetoothDevice> map_device = new HashMap<String, BluetoothDevice>();

	int connectState;


	BluetoothDevice device;
	
	Intent intent_conn;


	ArrayList<String> addresslist;
	String deviceAddress = "";
	
	private boolean isCheckVision = true;
	
	private String localVersion;
	private UpdataInfo info;
	
	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub

		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i("message", "BTService onCreate");
		
	}


	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		
		Log.i("message", "isCheckVision = " + isCheckVision);
		
		if (isCheckVision) {
			isCheckVision = false;
			
			try {
				localVersion = getVersionName();
				CheckVersionTask cv = new CheckVersionTask();
				new Thread(cv).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		map_device.clear();
		
		addresslist = intent.getExtras().getStringArrayList("garages");

		mBluetoothAdapter = null;
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		mBluetoothAdapter.cancelDiscovery();

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				// mPairedDevicesArrayAdapter.add(device.getName() + "\n" +
				// device.getAddress());
			}
		} else {
			// String noDevices =
			// getResources().getText(R.string.none_paired).toString();
			// mPairedDevicesArrayAdapter.add(noDevices);
		}

		mBluetoothAdapter.startDiscovery();
		
		Intent intent1 = new Intent(); // Itent就是我们要发送的内容
		intent1.putExtra("data", "device connect");
		intent1.setAction("oprate"); // 设置你这个广播的action，只有和这个action一样的接受者才能接受者才能接收广播
		sendBroadcast(intent1); // 发送广播
	}
	
	// The BroadcastReceiver that listens for discovered devices and
		// changes the title when discovery is finished
		private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// 获取查找到的蓝牙设备
					device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					map_device.put(device.getAddress(), device);
					intent_conn = new Intent();
					intent_conn.setAction("connect");
					intent_conn.putExtra("address", device.getAddress());
					sendBroadcast(intent_conn);
					System.out.println(device.getName());
					// 如果查找到的设备符合要连接的设备，处理
					
				} else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
					// 状态改变的广播
					final BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					map_device.put(device.getAddress(), device);
					intent_conn = new Intent();
					intent_conn.setAction("connect");
					intent_conn.putExtra("address", device.getAddress());
					sendBroadcast(intent_conn);
					
				}
			}
		};

	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	private String getVersionName() throws Exception {
		//getPackageName()是你当前类的包名，0代表是获取版本信息  
		PackageManager packageManager = getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
		return packInfo.versionName;
	}
	
	public class CheckVersionTask implements Runnable {
		InputStream is;
		public void run() {
			try {
				String path = getResources().getString(R.string.url_server);
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET"); 
                int responseCode = conn.getResponseCode(); 
                if (responseCode == 200) { 
                    // 从服务器获得一个输入流 
                	is = conn.getInputStream(); 
                } 
				info = UpdataInfoParser.getUpdataInfo(is);
				if (info.getVersion().equals(localVersion)) {
					Message msg = new Message();
					msg.what = UPDATA_NONEED;
					handler.sendMessage(msg);
					// LoginMain();
				} else {
					Message msg = new Message();
					msg.what = UPDATA_CLIENT;
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				Message msg = new Message();
				msg.what = GET_UNDATAINFO_ERROR;
				handler.sendMessage(msg);
				e.printStackTrace();
			}
		}
	}
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATA_NONEED:
//				Toast.makeText(getApplicationContext(), "不需要更新", Toast.LENGTH_SHORT).show();
//				getNextActivity();
				break;
			case UPDATA_CLIENT:
				 //对话框通知用户升级程序   
				showUpdataDialog();
				break;
			case GET_UNDATAINFO_ERROR:
				//服务器超时   
//	            Toast.makeText(getApplicationContext(), "获取服务器更新信息失败", 1).show(); 
//				getNextActivity();
				break;
			case DOWN_ERROR:
				//下载apk失败  
	            Toast.makeText(getApplicationContext(), R.string.downfail, 1).show(); 
//	            getNextActivity();
				break;
			}
		}
	};
	
	/* 
	 *  
	 * 弹出对话框通知用户更新程序  
	 *  
	 * 弹出对话框的步骤： 
	 *  1.创建alertDialog的builder.   
	 *  2.要给builder设置属性, 对话框的内容,样式,按钮 
	 *  3.通过builder 创建一个对话框 
	 *  4.对话框show()出来   
	 */  
	protected void showUpdataDialog() {
		AlertDialog.Builder builer = new Builder(this);
		builer.setTitle("版本升级");
		builer.setMessage(info.getDescription());
		 //当点确定按钮时从服务器上下载 新的apk 然后安装   װ
		builer.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				downLoadApk();
			}
		});
		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do sth
//				getNextActivity();
			}
		});
		AlertDialog dialog = builer.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
	}
	/* 
	 * 从服务器中下载APK 
	 */  
	protected void downLoadApk() {  
	    final ProgressDialog pd;    //进度条对话框  
	    pd = new  ProgressDialog(NowActivity.getActivity());  
	    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
	    pd.setMessage("正在下载更新");  
//	    pd。getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	    pd.show();  
	    new Thread(){  
	        @Override  
	        public void run() {  
	            try {  
	                File file = DownLoadManager.getFileFromServer(info.getUrl(), pd);  
	                sleep(3000);  
	                installApk(file);  
	                pd.dismiss(); //结束掉进度条对话框 
//	                getNextActivity();
	            } catch (Exception e) {  
	                Message msg = new Message();  
	                msg.what = DOWN_ERROR;  
	                handler.sendMessage(msg);  
	                e.printStackTrace();  
//	                getNextActivity();
	            }  
	        }}.start();  
	}  
	  
	//安装apk   
	protected void installApk(File file) {  
	    Intent intent = new Intent();  
	    //执行动作  
	    intent.setAction(Intent.ACTION_VIEW);  
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    //执行的数据类型  
	    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");  
	    startActivity(intent);  
	}  

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i("message", "btservice ondestroy");
		super.onDestroy();
	}
	
}
