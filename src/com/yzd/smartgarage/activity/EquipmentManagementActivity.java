package com.yzd.smartgarage.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.db.DBManager;
import com.yzd.smartgarage.entity.Garage;
import com.yzd.smartgarage.entity.UpdataInfo;
import com.yzd.smartgarage.util.DownLoadManager;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.UpdataInfoParser;

@SuppressLint("NewApi")
public class EquipmentManagementActivity extends ListActivity {
	
	private String localVersion;
	private UpdataInfo info;
	
	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;
	
	public static boolean ISCHECK = false;

	private BluetoothAdapter mBluetoothAdapter;

	List<Garage> garages;
	
	private ArrayList<HashMap<String, Object>> listItem;
	
	HashMap<String, Object> map = null;

	public static ArrayList<String> addresslist = new ArrayList<String>();

	private LegarageListAdapter mLegarageListAdapter;
	private DrawerLayout mDrawerLayout = null;
	private DBManager mgr;
	int index;
	ArrayList<HashMap<String, Object>> mLegarages;
	public static final String[] dawer_item = new String[] { "设备管理", "更多设置" };
	MyApplication myApplaction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_equipmentmanagement);
		
		NowActivity.setActivity(this);
		
		if (!ISCHECK) {
			ISCHECK = true;
			try {
				localVersion = getVersionName();
				CheckVersionTask cv = new CheckVersionTask();
				new Thread(cv).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.btnoexist, Toast.LENGTH_SHORT).show();
			finish();
		}
		// 如果本地蓝牙没有开启，则开启
		if (!mBluetoothAdapter.isEnabled()) {
			// 我们通过startActivityForResult()方法发起的Intent将会在onActivityResult()回调方法中获取用户的选择，比如用户单击了Yes开启，
			// 那么将会收到RESULT_OK的结果，
			// 如果RESULT_CANCELED则代表用户不愿意开启蓝牙
			// 用enable()方法来开启，无需询问用户(实惠无声息的开启蓝牙设备),这时就需要用到android.permission.BLUETOOTH_ADMIN权限。
			 if (!mBluetoothAdapter.enable()) {
				 Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				 startActivityForResult(mIntent, 1);
				
			}
			// mBluetoothAdapter.disable();//关闭蓝牙
		}
		
		Intent intent1 = getIntent();
		int isCheck = intent1.getIntExtra("isCheck", 0);
		

		mgr = new DBManager(this);
		
		garages = mgr.query();
		
		listItem = new ArrayList<HashMap<String, Object>>(); 

		ListView dawer_lv = (ListView) findViewById(R.id.left_drawer);
		dawer_lv.setAdapter(new ArrayAdapter<String>(this,
				R.layout.list_item_leftdawer, dawer_item));

		DawerClickListener dawerClickListener = new DawerClickListener();

		dawer_lv.setOnItemClickListener(dawerClickListener);

		 if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
	            Toast.makeText(this,R.string.nobt, Toast.LENGTH_SHORT).show();
	            finish();
	        }
		 
		 // 初始化一个蓝牙适配器。对API 18级以上，可以参考 bluetoothmanager。
	        final BluetoothManager bluetoothManager =
	                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
	        mBluetoothAdapter = bluetoothManager.getAdapter();

	        if (mBluetoothAdapter == null) {
	            Toast.makeText(this,R.string.devicenosupport, Toast.LENGTH_SHORT).show();
	            finish();
	            return;
	        }
	        
	        for (Garage garage : garages) {
				map = new HashMap<String, Object>();
				map.put("name", garage.getName());
				map.put("andrass", garage.getAddress());
				map.put("garage", garage);
				listItem.add(map);
			}

			mLegarageListAdapter = new LegarageListAdapter();
			mLegarageListAdapter.addgarage(listItem);
			setListAdapter(mLegarageListAdapter);
		
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private class DawerClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent;
			switch (position) {
			case 0:
				intent = new Intent(EquipmentManagementActivity.this, EquipmentManagementActivity.class);
				startActivity(intent);
				break;
			case 1:
				intent = new Intent(EquipmentManagementActivity.this, MoreActivity.class);
				startActivity(intent);
				break;
			default:
				break;
			}
		}

	}

	public void add() {
		ArrayList<Garage> garages = new ArrayList<Garage>();
		Garage garage = new Garage("2222", "我的车库1", "设备1", 1, 1, "32432432",
				"4343423", "");
		garages.add(garage);
		mgr.add(garages);
		Log.i("message", "add sucess");
	}

	public void update() {

		Garage garage = new Garage("2222", "我的车库10", "设备2", 2, 2, "fsdf",
				"sdf", "");
		garage.set_id(1);
		mgr.update(garage);
		Log.i("message", "update success");
	}

	public void addequiment(View view) {
		Log.i("message", "addequiment");
		Intent intent = new Intent(EquipmentManagementActivity.this, ListDeviceActivity.class);
		startActivity(intent);

	}

	@Override
	protected void onResume() {

		super.onResume();
		MobclickAgent.onResume(this);
		myApplaction = (MyApplication) getApplication();
		LockPatternUtils lockPatternUtils = new LockPatternUtils(this);
		if (lockPatternUtils.checksLock() && myApplaction.isLocked) {// 判断是否需要跳转到密码界面
			Intent intent = new Intent(this, SetLockActivity.class);
			intent.putExtra("intentFlag", 2);
			startActivity(intent);
		}
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		TextView button = (TextView) findViewById(R.id.left_bar);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 按钮按下，将抽屉打开
				mDrawerLayout.openDrawer(Gravity.LEFT);

			}
		});

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// Adapter for holding garages found through scanning.
	private class LegarageListAdapter extends BaseAdapter {
		private LayoutInflater mInflator;
		Garage garage;

		public LegarageListAdapter() {
			super();
			Log.i("message", "进入方法");
			mLegarages = new ArrayList<HashMap<String, Object>>();
			mInflator = EquipmentManagementActivity.this.getLayoutInflater();
			Log.i("message", mInflator.toString());
		}

		public void addgarage(ArrayList<HashMap<String, Object>> listitem) {
			mLegarages = listitem;
		}

		public Garage getgarage(int position) {
			return (Garage)mLegarages.get(position).get("garage");
		}

		public void clear() {
			mLegarages.clear();
		}

		@Override
		public int getCount() {
			return mLegarages.size();
		}

		@Override
		public Object getItem(int i) {
			return mLegarages.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(final int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			MyListener myListener = null;
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.show_equ_img = (ImageView) view
						.findViewById(R.id.equ_img);
				viewHolder.garageName = (TextView) view
						.findViewById(R.id.device_name);
				viewHolder.deviceState = (TextView)view.findViewById(R.id.device_state);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			garage = (Garage)mLegarages.get(i).get("garage");
			final String garageName = garage.getName();
			final String img_address = garage.getImg_address();
			final int deviceState = garage.getBondState();
			Log.i("message", "garagename = " + garageName);
			Log.i("message", "img_address = " + img_address);
			if (garageName != null && garageName.length() > 0)
				viewHolder.garageName.setText(garageName);
			else
				viewHolder.garageName.setText(R.string.unknown_device);
			viewHolder.deviceState.setText("");
			if (img_address != null && img_address.length() > 0) {

				try {
					Bitmap bitmap = BitmapFactory
							.decodeStream(getContentResolver().openInputStream(
									Uri.parse(img_address)));
					viewHolder.show_equ_img.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			return view;
		}
	}

	static class ViewHolder {
		ImageView show_equ_img;
		TextView garageName;
		TextView deviceState;
	}

	private class MyListener implements OnClickListener {
		int mPosition;

		public MyListener(int inPosition) {
			mPosition = inPosition;
		}

		@Override
		public void onClick(View v) {
			Log.i("message", "mPosition = " + String.valueOf(mPosition));
		}

	}

	@Override
	protected void onDestroy() {
		mgr.closeDB();
		super.onDestroy();
		Log.i("message", "EquipmentManagementActivity ondestroy");
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Garage garage = (Garage)mLegarages.get(position).get("garage");
		
		Intent intent = new Intent(EquipmentManagementActivity.this,
				OperatingActivity.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("garage", garage);
		intent.putExtras(bundle);
		
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
//		super.onBackPressed(); 
		// 返回主界面
		Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

		mHomeIntent.addCategory(Intent.CATEGORY_HOME);
		mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(mHomeIntent);

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
		dialog.setCanceledOnTouchOutside(false);
//		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		dialog.show();
		
	}
	/* 
	 * 从服务器中下载APK 
	 */  
	protected void downLoadApk() {  
	    final ProgressDialog pd;    //进度条对话框  
	    pd = new  ProgressDialog(NowActivity.getActivity());  
	    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
	    pd.setCanceledOnTouchOutside(false);
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
}
