package com.yzd.smartgarage.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yzd.smartgarage.db.DBManager;
import com.yzd.smartgarage.entity.Lock;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

public class LockPatternUtils {
	
	//private static final String TAG = "LockPatternUtils";
	private final static String KEY_LOCK_PWD = "lock_pwd";
	
	
	private static Context mContext;
	
	private static SharedPreferences preference;
	
	private DBManager mgr;
	
	//private final ContentResolver mContentResolver;
	
	 public LockPatternUtils(Context context) {
	        mContext = context;
	        mgr = new DBManager(context);
	        preference = PreferenceManager.getDefaultSharedPreferences(mContext);
	       // mContentResolver = context.getContentResolver();
	 }
	
	 /**
     * Deserialize a pattern.
     * @param string The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<LockPatternView.Cell> stringToPattern(String string) {
        List<LockPatternView.Cell> result = new ArrayList<LockPatternView.Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(LockPatternView.Cell.of(b / 3, b % 3));
        }
        return result;
    }

    /**
     * Serialize a pattern.
     * @param pattern The pattern.
     * @return The pattern in string form.
     */
    public static String patternToString(List<LockPatternView.Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            LockPatternView.Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return Arrays.toString(res);
    }
    
    public void saveLockPattern(List<LockPatternView.Cell> pattern){
    	List<Lock> listlock = mgr.queryLock();
    	if(listlock.size() == 0) {
    		Lock lock = new Lock(1, patternToString(pattern));
        	
        	mgr.addLock(lock);
    	} else {
    		updateLockPattern(pattern);
    	}
    	
    	
    	
    	
//    	Log.i("message", mgr.queryLock().get(0).getPattern());
    	
    	/*Editor editor = preference.edit();
    	editor.putString(KEY_LOCK_PWD, patternToString(pattern));
    	editor.commit();*/
    }
    
    public void updateLockPattern(List<LockPatternView.Cell> pattern){
    	
    	
    	Lock lock = new Lock(1, patternToString(pattern));
    	lock.set_id(0);
    	mgr.updateLock(lock);
    	
    	Log.i("message", mgr.queryLock().get(0).getPattern());
    	
    	
    }
    
    public String getLockPaternString(){
    	List<Lock> locklist = mgr.queryLock();
    	
    	Lock lock = locklist.get(0);
    	return lock.getPattern();
//    	return preference.getString(KEY_LOCK_PWD, "");
    }
    
    public boolean checksLock() {
    	List<Lock> locklist = mgr.queryLock();
    	
    	if (locklist.size() == 0 || locklist == null) {
			return false;
		}
    	
    	Lock lock = locklist.get(0);
    	
    	if (lock.getIsLock() == 0) {
			return false;
		}else if(lock.getIsLock() == 1){
			return true;
		} else {
			return false;
		}
    }
    
    public int checkPattern(List<LockPatternView.Cell> pattern) {
    	String stored = getLockPaternString();
    	
    	if(!stored.isEmpty()){
    		return stored.equals(patternToString(pattern))?1:0;
    	}
    	return -1;
    }
    

    public void clearLock() {
    	saveLockPattern(null);
    }
  

}
