package com.yzd.smartgarage.entity;

public class Lock {
	int _id;
	int isLock;
	String pattern;
	
	public Lock() {
		super();
	}
	
	public Lock(int isLock, String pattern) {
		super();
		this.isLock = isLock;
		this.pattern = pattern;
	}
	public int getIsLock() {
		return isLock;
	}
	public void setIsLock(int isLock) {
		this.isLock = isLock;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	
}
