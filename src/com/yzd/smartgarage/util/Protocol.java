package com.yzd.smartgarage.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol {
	
	//4键
	//点动
//	public static String open_jog = "AAAA1111A041010112EE";
//	public static String close_jog = "AAAA1111A042010112EE";
//	public static String stop_jog = "AAAA1111A043010112EE";
//	public static String lock_jog = "AAAA1111A044010112EE";
//	public static String study_jog = "AAAA1111A045010112EE";
//	public static String clock_jog = "AAAA1111A044010112EE";
//	
//	//联动
//	public static String open_linkage = "AAAA1111A041010212EE";
//	public static String close_linkage = "AAAA1111A042010212EE";
//	
//	//1键
//	public static String one_operate = "AAAA1111A041010212EE";

	public static String setOpen_jog(String key) {
		return "AAAA0000" + key + "A041010112EE";
	}

	public static String setClose_jog(String key) {
		return "AAAA0000" + key + "A042010112EE";
	}

	public static String setStop_jog(String key) {
		return "AAAA0000" + key + "A043010112EE";
	}

	public static String setLock_jog(String key) {
		return "AAAA0000" + key + "A044010112EE";
	}

	public static String setStudy_jog(String key) {
		return "AAAA0000" + key + "A045010112EE";
	}

	public static String setClock_jog(String key) {
		return "AAAA0000" + key + "A044010112EE";
	}

	public static String setOpen_linkage(String key) {
		return "AAAA0000" + key + "A041010212EE";
	}

	public static String setClose_linkage(String key) {
		return "AAAA0000" + key + "A042010212EE";
	}

	public static String setOne_operate(String key) {
		return "AAAA0000" + key + "A041010212EE";
	}
	
	public static String getOP(String s) {
		if (s.length() >10) {
			s = s.replaceAll("\\s*", "");
			if (s.substring(0, 4).equals("BBBB")) {
				return s.substring(30, 32);
			}
		}
		return s;
	}
	
	public static String getOK(String s) {
		if(s.length() > 10) {
			s = s.replaceAll("\\s*", "");
			if (s.substring(0, 4).equals("BBBB")) {
				return s.substring(26, 28);
			}
			
		} 
			return s;
	}
	
}
