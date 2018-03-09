package com.dull.CDSpace.utils.httpClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.net.ssl.SSLContext;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.dull.CDSpace.controller.FMContextMenuController;
import com.dull.CDSpace.utils.ProxyUtil;
import com.dull.CDSpace.utils.StringUtil;

/*
 * Author 杜亮亮
 * 2016.3.21
 */
public class HttpClientUtil {
	private static Logger logger = Logger.getLogger(FMContextMenuController.class);
	private CloseableHttpClient httpclient;
	public static Map<String, String> cookies = new HashMap<>();
	private static SSLConnectionSocketFactory sslsf= null;
	private SSLConnectionSocketFactory getSSLConnectionSocketFactory(){
		if (sslsf!=null) {
			return sslsf;
		}
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
			        null,new TrustStrategy() {
			            //信任所有
			            public boolean isTrusted(X509Certificate[] chain, String authType)
			                    throws CertificateException {
			                return true;
			            }
			        }).build();
	        sslsf=new SSLConnectionSocketFactory(sslContext);
	        return sslsf;
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Header getHeader() {
		StringBuffer cookieString = new StringBuffer();
		cookies.forEach((k, v) -> {
			cookieString.append(k + "=" + v + ";");
		});
		return new BasicHeader("Cookie", cookieString.toString());
	}

	public void cacheHeader(CloseableHttpResponse response) {
		Header[] headers = response.getHeaders("Set-Cookie");
		if (headers.length > 0) {
			HeaderElement[] headerElements = headers[0].getElements();
			for (HeaderElement e : headerElements) {
				cookies.put(e.getName(), e.getValue());
			}
		}
	}

	public static HttpClientResponse doGet(String url, HashMap<String, String> headers) {
		HttpClientUtil httpClient = new HttpClientUtil();
		httpClient.init();
		HttpClientResponse response = httpClient.get(url, headers);
		httpClient.close();
		return response;
	}

	public static HttpClientResponse doPut(String url, HashMap<String, String> headers, String params) {
		HttpClientUtil httpClient = new HttpClientUtil();
		httpClient.init();
		HttpClientResponse response = httpClient.put(url, headers, params);
		httpClient.close();
		return response;
	}

	public static HttpClientResponse doDelete(String url, HashMap<String, String> headers) {
		HttpClientUtil httpClient = new HttpClientUtil();
		httpClient.init();
		HttpClientResponse response = httpClient.delete(url, headers);
		httpClient.close();
		return response;
	}

	public static HttpClientResponse doPost(String url, Map<String, String> headers, String params) {
		HttpClientUtil httpClient = new HttpClientUtil();
		httpClient.init();
		HttpClientResponse response = httpClient.post(url, headers, params);
		httpClient.close();
		return response;
	}

	private void init() {
		Properties config = ProxyUtil.getProperties();
		HttpHost proxy = null;
//		AuthScope authScope = null;
//		CredentialsProvider credentialsProvider = null;
		
		if (config!=null) {
			String ip = config.getProperty("ip");
			String port = config.getProperty("port");
			String enable = config.getProperty("enable");
			if (StringUtil.isBoolean(enable)&&Boolean.valueOf(enable)) {
				if (StringUtil.isIp(ip)&&StringUtil.isPort(port)) {
					proxy = new HttpHost(ip, Integer.valueOf(port));
//					credentialsProvider = new BasicCredentialsProvider();
//					authScope = new AuthScope(ip, Integer.valueOf(port));
//					credentialsProvider.setCredentials(authScope,new UsernamePasswordCredentials("username", "password"));
				}
			}
		}
		httpclient = HttpClientBuilder.create().setProxy(proxy).setSSLSocketFactory(getSSLConnectionSocketFactory()).build();
	}

	private HttpClientResponse get(String url, HashMap<String, String> headers) {
		logger.debug(url);
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader(getHeader());
			if (null != headers && !headers.isEmpty()) {
				Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
					Header[] cookieHeads = httpget.getHeaders("Cookie");
					if (entry.getKey().equals("Cookie")&&cookieHeads!=null&&cookieHeads.length>0) {
						httpget.setHeader("Cookie", cookieHeads[0].getValue()+entry.getValue());
					}else{
						httpget.setHeader(entry.getKey(), entry.getValue());
					}
				}
			}
			CloseableHttpResponse response = httpclient.execute(httpget);
			cacheHeader(response);
			Date date = new Date();
			httpClientResponse.setStateCode(date + "\n" + "\n" + response.getStatusLine().toString());
			Header[] responseHeaders = response.getAllHeaders();
			String responseHeadersString = "";
			for (int i = 0; i < responseHeaders.length; i++) {
				Header header = responseHeaders[i];
				responseHeadersString += header.getName() + ": " + header.getValue() + "\n";
			}
			responseHeadersString += "Cookies:" + cookies.toString();
			httpClientResponse.setHeaders(responseHeadersString);
			try {
				HttpEntity entity = response.getEntity();
				System.out.println(response.getStatusLine());
				if (entity != null) {
					httpClientResponse.setResponse(EntityUtils.toString(entity, "UTF-8"));
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return httpClientResponse;
	}

	private HttpClientResponse put(String url, HashMap<String, String> headers, String params) {
		logger.debug(url);
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		try {
			HttpPut httpput = new HttpPut(url);
			httpput.setHeader(getHeader());
			if (null != headers && !headers.isEmpty()) {
				Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
					httpput.setHeader(entry.getKey(), entry.getValue());
				}
			}
			httpput.setEntity(new StringEntity(params,Consts.UTF_8));
			CloseableHttpResponse response = httpclient.execute(httpput);
			cacheHeader(response);
			Date date = new Date();
			httpClientResponse.setStateCode(date + "\n" + "\n" + response.getStatusLine().toString());
			Header[] responseHeaders = response.getAllHeaders();
			String responseHeadersString = "";
			for (int i = 0; i < responseHeaders.length; i++) {
				Header header = responseHeaders[i];
				responseHeadersString += header.getName() + ": " + header.getValue() + "\n";
			}
			responseHeadersString += "Cookies:" + cookies.toString();
			httpClientResponse.setHeaders(responseHeadersString);
			try {
				HttpEntity entity = response.getEntity();
				System.out.println(response.getStatusLine());
				if (entity != null) {
					httpClientResponse.setResponse(EntityUtils.toString(entity, "UTF-8"));
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return httpClientResponse;
	}

	private HttpClientResponse delete(String url, HashMap<String, String> headers) {
		logger.debug(url);
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			httpDelete.setHeader(getHeader());
			if (null != headers && !headers.isEmpty()) {
				Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
					httpDelete.setHeader(entry.getKey(), entry.getValue());
				}
			}
			CloseableHttpResponse response = httpclient.execute(httpDelete);
			cacheHeader(response);
			Date date = new Date();
			httpClientResponse.setStateCode(date + "\n" + "\n" + response.getStatusLine().toString());
			Header[] responseHeaders = response.getAllHeaders();
			String responseHeadersString = "";
			for (int i = 0; i < responseHeaders.length; i++) {
				Header header = responseHeaders[i];
				responseHeadersString += header.getName() + ": " + header.getValue() + "\n";
			}
			responseHeadersString += "Cookies:" + cookies.toString();
			httpClientResponse.setHeaders(responseHeadersString);
			try {
				HttpEntity entity = response.getEntity();
				System.out.println(response.getStatusLine());
				if (entity != null) {
					httpClientResponse.setResponse(EntityUtils.toString(entity, "UTF-8"));
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return httpClientResponse;
	}

	private HttpClientResponse post(String url, Map<String, String> headers, String params) {
		logger.debug(url);
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		try {
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader(getHeader());
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			if (null != headers && !headers.isEmpty()) {
				Iterator<Entry<String, String>> iter = headers.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = (Entry<String, String>) iter.next();
					httppost.setHeader(entry.getKey(), entry.getValue());
				}
			}
//			if (params!=null&&params.length()>0) {
//				String[] paramArr = params.split("\\&");
//				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
//				for (String param:paramArr) {
//					int index = param.indexOf("=");
//					nvps.add(new BasicNameValuePair(param.substring(0,index), param.substring(index+1)));
//				}
//				httppost.setEntity(new UrlEncodedFormEntity(nvps,Consts.UTF_8));
//			}
			httppost.setEntity(new StringEntity(params, Consts.UTF_8));

			CloseableHttpResponse response = httpclient.execute(httppost);
			response.setLocale(Locale.CHINESE);
			cacheHeader(response);
			Date date = new Date();
			httpClientResponse.setStateCode(date + "\n" + "\n" + response.getStatusLine().toString());
			Header[] responseHeaders = response.getAllHeaders();
			String responseHeadersString = "";
			for (int i = 0; i < responseHeaders.length; i++) {
				Header header = responseHeaders[i];
				responseHeadersString += header.getName() + ": " + header.getValue() + "\n";
			}
			responseHeadersString += "Cookies:" + cookies.toString();
			httpClientResponse.setHeaders(responseHeadersString);
			try {
				HttpEntity entity = response.getEntity();
				System.out.println(response.getStatusLine());
				if (entity != null) {
					httpClientResponse.setResponse(EntityUtils.toString(entity, "UTF-8"));
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return httpClientResponse;
	}
	private void close() {
		try {
			httpclient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
