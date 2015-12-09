package com.yzd.smartgarage.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yzd.smartgarage.entity.Garage;
import com.yzd.smartgarage.entity.Lock;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;
	
	public DBManager(Context context) {
		helper = new DBHelper(context);
		//��ΪgetWritableDatabase�ڲ�������mContext.openOrCreateDatabase(mName, 0, mFactory);
		//����Ҫȷ��context�ѳ�ʼ��,���ǿ��԰�ʵ����DBManager�Ĳ������Activity��onCreate��
		db = helper.getWritableDatabase();
	}
	
	/**
	 * add persons
	 * @param persons
	 */
	public boolean add(List<Garage> garages) {
        db.beginTransaction();	//��ʼ����
        try {
        	for (Garage garage : garages) {
        		db.execSQL("INSERT INTO garage VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?)", new Object[]{garage.getAddress(), garage.getName(), garage.getDevName(), garage.getType(),garage.getBondState(), garage.getUuid(), garage.getKey(), garage.getImg_address()});
        		
        	}
        	db.setTransactionSuccessful();	//��������ɹ����
        	return true;
        } finally {
        	db.endTransaction();	//��������
        	return false;
        }
	}
	
	public boolean addLock(Lock lock) {
        db.beginTransaction();	//��ʼ����
        try {
        		db.execSQL("INSERT INTO lock VALUES(null, ?, ?)", new Object[]{lock.getIsLock(), lock.getPattern()});
        		
        	db.setTransactionSuccessful();	//��������ɹ����
        	return true;
        } finally {
        	db.endTransaction();	//��������
        	return false;
        }
	}
	
	/**
	 * update person's age
	 * @param person
	 */
	public int update(Garage garage) {
		ContentValues cv = new ContentValues();
		cv.put("address", garage.getAddress());
		cv.put("uuid", garage.getUuid());
		cv.put("key", garage.getKey());
		cv.put("name", garage.getName());
		cv.put("devName", garage.getDevName());
		cv.put("bondState", garage.getBondState());
		cv.put("type", garage.getType());
		cv.put("img_address", garage.getImg_address());
		return db.update("garage", cv, "_id = ?", new String[]{String.valueOf(garage.get_id())});
	}
	
	public void updateLock(Lock lock) {
		ContentValues cv = new ContentValues();
		cv.put("isLock", lock.getIsLock());
		cv.put("pattern", lock.getPattern());
//		String id = String.valueOf(lock.get_id());
		String id = "1";
		Log.i("message", "update = " + db.update("lock", cv, "_id = ?", new String[]{id}));
	}
	
	public void clearState(List<Garage> garages) {
		for (Garage garage : garages) {
			ContentValues cv = new ContentValues();
			cv.put("address", garage.getAddress());
			cv.put("uuid", garage.getUuid());
			cv.put("key", garage.getKey());
			cv.put("name", garage.getName());
			cv.put("devName", garage.getDevName());
			cv.put("bondState", 0);
			cv.put("type", garage.getType());
			cv.put("img_address", garage.getImg_address());
			db.update("garage", cv, "_id = ?", new String[]{String.valueOf(garage.get_id())});
		}
	}
	
	public void deleteLock() {
		db.rawQuery("delete from lock", null);
	}
	
/*	*//**
	 * delete old person
	 * @param person
	 */
	public void deleteOldgarage(int id) {
		db.delete("garage", "_id = ?", new String[]{String.valueOf(id)});
	}
	
	/**
	 * query all persons, return list
	 * @return List<Person>
	 */
	public List<Garage> query() {
		ArrayList<Garage> garages = new ArrayList<Garage>();
		Cursor c = queryTheCursor();
        while (c.moveToNext()) {
        	Garage garage = new Garage();
        	garage.set_id(c.getInt(c.getColumnIndex("_id")));
        	garage.setAddress(c.getString(c.getColumnIndex("address")));
        	garage.setBondState(c.getInt(c.getColumnIndex("bondState")));
        	garage.setDevName(c.getString(c.getColumnIndex("devName")));
        	garage.setKey(c.getString(c.getColumnIndex("key")));
        	garage.setName(c.getString(c.getColumnIndex("name")));
        	garage.setType(c.getInt(c.getColumnIndex("type")));
        	garage.setUuid(c.getString(c.getColumnIndex("uuid")));
        	garage.setImg_address(c.getString(c.getColumnIndex("img_address")));
        	garages.add(garage);
        }
        c.close();
        return garages;
	}
	
	public List<Lock> queryLock() {
		ArrayList<Lock> locks = new ArrayList<Lock>();
		Cursor c = queryTheLockCursor();
        while (c.moveToNext()) {
        	Lock lock = new Lock();
        	lock.set_id(c.getColumnIndex("_id"));
        	lock.setIsLock(c.getInt(c.getColumnIndex("isLock")));
        	lock.setPattern(c.getString(c.getColumnIndex("pattern")));
        	locks.add(lock);
        }
        c.close();
        return locks;
	}
	
	/**
	 * query all persons, return cursor
	 * @return	Cursor
	 */
	public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM garage", null);
        return c;
	}
	
	public Cursor queryTheLockCursor() {
        Cursor c = db.rawQuery("SELECT * FROM lock", null);
        return c;
	}
	
	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}
}
