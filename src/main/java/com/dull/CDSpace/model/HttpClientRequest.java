package com.dull.CDSpace.model;

import java.util.HashMap;

import com.alibaba.fastjson.JSON;
import com.dull.CDSpace.utils.FileUtil;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class HttpClientRequest {
	private String method = "";
	
	private String url = "";
	
	private String body = "";
	
	private HashMap<String, String> headers;
	
	public HttpClientRequest() {
		
	}
	
	public HttpClientRequest(String filePath) {
		String jsonString = FileUtil.readFile(filePath);
		HttpClientRequest httpClientRequest = JSON.parseObject(jsonString, HttpClientRequest.class);
		this.method = httpClientRequest.method;
		this.url = httpClientRequest.url;
		this.body = httpClientRequest.body;
		this.headers = httpClientRequest.headers;
	}
	
	public static void requestToFile(HttpClientRequest httpClientRequest, String filePath){
		String jsonString = JSON.toJSONString(httpClientRequest);
		FileUtil.createFileWithString(jsonString, filePath);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
