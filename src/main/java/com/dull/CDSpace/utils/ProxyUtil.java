package com.dull.CDSpace.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ProxyUtil {
	public static Properties getProperties(){
		Properties config = new Properties();
		try {
			InputStream is = new FileInputStream("./project/proxy.properties");
			if (is!=null) {
				config.load(new BufferedInputStream(is));
			}
		} catch (IOException e) {
		}
		return config;
	}
	public static String getProxy(){
		String value = "";
		Properties config = getProperties();
		if (config!=null) {
			String ip = config.getProperty("ip");
			String port = config.getProperty("port");
			String enable = config.getProperty("enable");
			value = StringUtil.ifEmpty(ip, "ip")+":"+StringUtil.ifEmpty(port, "port")+":"+StringUtil.ifEmpty(enable, "true");
		}
		return value;
	}
	public static void writeProperties(Properties config){
		try {
			FileOutputStream fos = new FileOutputStream("./project/proxy.properties");
			if (fos!=null) {
				config.store(fos, null);
			}
		} catch (IOException e) {
		}
	}
	public static void writeProxy(String value){
		Properties config = new Properties();
		if (StringUtil.isNotEmpty(value)) {
			String[] values = value.split("[:：]");
			switch (values.length) {
			case 3:
				config.setProperty("enable",StringUtil.ifEmpty(values[2], "true"));
			case 2:
				config.setProperty("port",StringUtil.ifEmpty(values[1], ""));
			case 1:
				config.setProperty("ip",StringUtil.ifEmpty(values[0], ""));
				break;
			default:
				break;
			}
			writeProperties(config);
		}
	}
}
