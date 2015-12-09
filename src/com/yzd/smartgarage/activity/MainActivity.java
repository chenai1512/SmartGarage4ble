package com.yzd.smartgarage.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.yzd.smartgarage.R;
import com.yzd.smartgarage.util.MyImageView;
import com.yzd.smartgarage.util.NowActivity;


public class MainActivity extends Activity {
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        NowActivity.setActivity(this);
        
        
        GridView mainGrid = (GridView)findViewById(R.id.mainGrid);
        
        ArrayList<HashMap<String, Object>> menuList = new ArrayList<HashMap<String, Object>>(); 
        
        HashMap<String, Object> itemMap_1 = new  HashMap<String, Object>();
        itemMap_1.put("ItemImage", R.drawable.app_store);
        itemMap_1.put("ItemText", "应用商店");
        menuList.add(itemMap_1);
        
        HashMap<String, Object> itemMap_2 = new  HashMap<String, Object>();
        itemMap_2.put("ItemImage", R.drawable.scurity_center);
        itemMap_2.put("ItemText", "安全中心");
        menuList.add(itemMap_2);
        
        HashMap<String, Object> itemMap_3 = new  HashMap<String, Object>();
        itemMap_3.put("ItemImage", R.drawable.smart_home);
        itemMap_3.put("ItemText", "智慧家居");
        menuList.add(itemMap_3);
        
        
        SimpleAdapter saMenuItem = new SimpleAdapter(this, menuList, R.layout.meunitem, new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage,R.id.ItemText});
        
      //添加Item到网格中 
        mainGrid.setAdapter(saMenuItem);
        
        mainGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 2) {
					Intent intent = new Intent();
		            intent.setClass(MainActivity.this, ShowCompanyActivity.class);
		
		           startActivity(intent);
				}
			}
		});
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
}
