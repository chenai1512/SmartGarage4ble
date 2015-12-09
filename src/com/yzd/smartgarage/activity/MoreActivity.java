package com.yzd.smartgarage.activity;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.yzd.smartgarage.R;
import com.yzd.smartgarage.db.DBManager;
import com.yzd.smartgarage.entity.Lock;
import com.yzd.smartgarage.entity.UpdataInfo;
import com.yzd.smartgarage.util.DownLoadManager;
import com.yzd.smartgarage.util.LockPatternUtils;
import com.yzd.smartgarage.util.MyApplication;
import com.yzd.smartgarage.util.NowActivity;
import com.yzd.smartgarage.util.UpdataInfoParser;

public class MoreActivity extends Activity {
	
	private final String TAG = this.getClass().getName();
	private final int UPDATA_NONEED = 0;
	private final int UPDATA_CLIENT = 1;
	private final int GET_UNDATAINFO_ERROR = 2;
	private final int SDCARD_NOMOUNTED = 3;
	private final int DOWN_ERROR = 4;
	private Button getVersion;
	private UpdataInfo info;
	private String localVersion;

	private DBManager mgr;
	List<Map<String, Object>> items;
	SimpleAdapter adpter;
	MyApplication myApplaction;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more);
		
		NowActivity.setActivity(this);
		

		mgr = new DBManager(this);

		ListView morelist = (ListView) findViewById(R.id.morelist);

		items = new ArrayList<Map<String, Object>>();

		Map<String, Object> maps1 = new HashMap<String, Object>();
		maps1.put("itemname", "版本更新");
		items.add(maps1);

		Map<String, Object> maps = new HashMap<String, Object>();
		List<Lock> listlock = mgr.queryLock();

		if (listlock.size() == 0) {
			maps.put("itemname", "创建手势密码");
		} else if (listlock.get(0).getIsLock() == 1) {
			maps.put("itemname", "修改手势密码");

			Map<String, Object> maps2 = new HashMap<String, Object>();
			maps2.put("itemname", "关闭手势密码");
			items.add(maps2);
		} else if (listlock.size() > 0 && listlock.get(0).getIsLock() == 0) {
			maps.put("itemname", "创建手势密码");

			Map<String, Object> maps3 = new HashMap<String, Object>();
			maps3.put("itemname", "开启手势密码");
			items.add(maps3);
		}
		items.add(maps);

		adpter = new SimpleAdapter(this, items, R.layout.listitem_morelist,
				new String[] { "itemname", }, new int[] { R.id.item_name });

		morelist.setAdapter(adpter);

		MoreListClickListener moreListClickListener = new MoreListClickListener();

		morelist.setOnItemClickListener(moreListClickListener);
	}

	public void back(View view) {
		Intent intent = new Intent(MoreActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}

	private class MoreListClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch (position) {
			case 0:
				try {
					localVersion = getVersionName();
					CheckVersionTask cv = new CheckVersionTask();
					new Thread(cv).start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:
				final TextView itemText = (TextView) view
						.findViewById(R.id.item_name);
				String itemName = itemText.getText().toString();
				if (itemName.equals("开启手势密码")) {
					Lock lock = mgr.queryLock().get(0);
					lock.setIsLock(1);
					mgr.updateLock(lock);
					Map<String, Object> maps = items.get(position);
					maps.put("itemname", "关闭手势密码");
					items.set(position, maps);
					adpter.notifyDataSetChanged();
					// Intent intent = new Intent(MoreActivity.this,
					// EquipmentManagementActivity.class);
					// startActivity(intent);
				} else if (itemName.equals("关闭手势密码")) {
					Lock lock = mgr.queryLock().get(0);
					lock.setIsLock(0);
					mgr.updateLock(lock);
					Map<String, Object> maps = items.get(position);
					maps.put("itemname", "开启手势密码");
					items.set(position, maps);
					adpter.notifyDataSetChanged();
					// Intent intent = new Intent(MoreActivity.this,
					// EquipmentManagementActivity.class);
					// startActivity(intent);
				} else if (itemName.equals("创建手势密码")) {
					Intent intent = new Intent(MoreActivity.this,
							SetLockActivity.class);
					intent.putExtra("intentFlag", 1);
					startActivity(intent);
				}
				break;
			case 2:
				Intent intent = new Intent(MoreActivity.this,
						SetLockActivity.class);
				intent.putExtra("intentFlag", 1);
				startActivity(intent);
				break;
			default:
				break;
			}
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
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setConnectTimeout(5000);
				conn.setRequestMethod("GET"); 
                int responseCode = conn.getResponseCode(); 
                if (responseCode == 200) { 
                    // 从服务器获得一个输入流 
                	is = conn.getInputStream(); 
                } 
				info = UpdataInfoParser.getUpdataInfo(is);
				if (info.getVersion().equals(localVersion)) {
					Log.i(TAG, "版本号相同");
					Message msg = new Message();
					msg.what = UPDATA_NONEED;
					handler.sendMessage(msg);
					// LoginMain();
				} else {
					Log.i(TAG, "版本号不相同 ");
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
				Toast.makeText(getApplicationContext(), R.string.notneedupdate, Toast.LENGTH_SHORT).show();
				break;
			case UPDATA_CLIENT:
				 //对话框通知用户升级程序   
				showUpdataDialog();
				break;
			case GET_UNDATAINFO_ERROR:
				//服务器超时   
	            Toast.makeText(getApplicationContext(), R.string.getservicefail, 1).show(); 
				break;
			case DOWN_ERROR:
				//下载apk失败  
	            Toast.makeText(getApplicationContext(), R.string.getservicefail, 1).show(); 
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
				Log.i(TAG, "下载apk,更新");
				downLoadApk();
			}
		});
		builer.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				//do sth
			}
		});
		AlertDialog dialog = builer.create();
		dialog.show();
	}
	/* 
	 * 从服务器中下载APK 
	 */  
	protected void downLoadApk() {  
	    final ProgressDialog pd;    //进度条对话框  
	    pd = new  ProgressDialog(this);  
	    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);  
	    pd.setMessage("正在下载更新");  
	    pd.show();  
	    new Thread(){  
	        @Override  
	        public void run() {  
	            try {  
	                File file = DownLoadManager.getFileFromServer(info.getUrl(), pd);  
	                sleep(3000);  
	                installApk(file);  
	                pd.dismiss(); //结束掉进度条对话框  
	            } catch (Exception e) {  
	                Message msg = new Message();  
	                msg.what = DOWN_ERROR;  
	                handler.sendMessage(msg);  
	                e.printStackTrace();  
	            }  
	        }}.start();  
	}  
	  
	//安装apk   
	protected void installApk(File file) {  
	    Intent intent = new Intent();  
	    //执行动作  
	    intent.setAction(Intent.ACTION_VIEW);  
	    //执行的数据类型  
	    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");  
	    startActivity(intent);  
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

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(MoreActivity.this,
				EquipmentManagementActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

}
