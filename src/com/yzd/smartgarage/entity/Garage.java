package com.yzd.smartgarage.entity;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;


public class Garage implements Serializable{
	
	private static final long serialVersionUID = -7060210544600464481L;
	
	public int _id;
	public String address;
	public String name;
	public String devName;
	//1表示点动  2表示联动
	public int type;
	////0代表未连接  1代表失去连接成功  2代表连失去连接 3代表正在连接
	public int bondState;
	public String uuid;
	public String key;
	public String img_address;
	
	public Garage(String address, String name, String devName, int type,
			int bondState, String uuid, String key, String img_address) {
		super();
		this.address = address;
		this.name = name;
		this.devName = devName;
		this.type = type;
		this.bondState = bondState;
		this.uuid = uuid;
		this.key = key;
		this.img_address = img_address;
	}
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getDevName() {
		return devName;
	}
	public void setDevName(String devName) {
		this.devName = devName;
	}
	
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getBondState() {
		return bondState;
	}
	public void setBondState(int bondState) {
		this.bondState = bondState;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Garage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getImg_address() {
		return img_address;
	}

	public void setImg_address(String img_address) {
		this.img_address = img_address;
	}
	
	
	public static String getKey(String key) {
		key = "0000000000000000" + key;
		return key.substring(key.length() - 16, key.length());
	}
}
