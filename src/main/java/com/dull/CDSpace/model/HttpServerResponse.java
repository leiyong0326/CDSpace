package com.dull.CDSpace.model;

import com.alibaba.fastjson.JSON;
import com.dull.CDSpace.utils.FileUtil;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class HttpServerResponse {
	private String port = "";
	
	private String serverType = "";
	
	private String protocolType = "";
	
	private HttpServerResponseContext[] contexts;
	
	
	public HttpServerResponse() {
		super();
	}

	public HttpServerResponse(String port, String serverType, String protocolType, HttpServerResponseContext[] contexts) {
		this.port = port;
		this.serverType = serverType;
		this.protocolType = protocolType;
		this.contexts = contexts;
	}
	
	public HttpServerResponse(String filePath) {
		String jsonString = FileUtil.readFile(filePath);
		HttpServerResponse httpServerResponse = JSON.parseObject(jsonString, HttpServerResponse.class);
		this.port = httpServerResponse.port;
		this.serverType = httpServerResponse.serverType;
		this.protocolType = httpServerResponse.protocolType;
		this.contexts = httpServerResponse.contexts;
	}
	
	public static void requestToFile(HttpServerResponse httpServerResponse, String filePath){
		String jsonString = JSON.toJSONString(httpServerResponse);
		FileUtil.createFileWithString(jsonString, filePath);
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	public HttpServerResponseContext[] getContexts() {
		return contexts;
	}

	public void setContexts(HttpServerResponseContext[] contexts) {
		this.contexts = contexts;
	}
}
