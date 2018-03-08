package com.dull.CDSpace.utils;

import java.util.Random;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class StringUtil {
	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	public static boolean isEmpty(String str){
		return str==null||str.isEmpty();
	}
	public static boolean isNotEmpty(String str){
		return !isEmpty(str);
	}
	public static String ifEmpty(String str,String defaultStr){
		return isEmpty(str)?defaultStr:str;
	}
	public static boolean isIp(String ip){
		return ip!=null&&ip.trim().matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
	}
	public static boolean isPort(String port){
		return port!=null&&port.trim().matches("^\\d{1,6}$");
	}
	public static boolean isBoolean(String enable){
		if (enable!=null) {
			String pl = enable.trim().toLowerCase();
			return pl.equals("true")||pl.equals("false");
		}
		return false;
	}
}
