package com.yzd.smartgarage.db;

import com.yzd.smartgarage.entity.Lock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "smartgarage.db";
	private static final int DATABASE_VERSION = 1;
	
	public DBHelper(Context context) {
		//CursorFactory����Ϊnull,ʹ��Ĭ��ֵ
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	//���ݿ��һ�α�����ʱonCreate�ᱻ����
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS garage" +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, address VARCHAR, name VARCHAR, devName VARCHAR, type INTEGER, bondState INTEGER, uuid VARCHAR, key VARCHAR, img_address VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS lock" +
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, isLock INTEGER, pattern VARCHAR)");
		
	}

	//���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("ALTER TABLE garage ADD COLUMN other STRING");
		db.execSQL("ALTER TABLE lock ADD COLUMN other STRING");
	}
	
}
