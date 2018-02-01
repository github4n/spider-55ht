package com.haitao55.spider.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class CurlCrawlerUtil {
	
	private static final int DEFAULT_TIMEOUT = 30;// 单位:秒
	
	public static String get(String url){
		return get(url, DEFAULT_TIMEOUT);
	}
	
	public static String get(String url, int timeout){
		return get(url, timeout, null, -1);
	}
	
	public static String getByLuminati(String url){
		StringBuffer sb = new StringBuffer();
		String content = StringUtils.EMPTY;
		BufferedReader reader = null;
		try{
			String command = "curl -k --proxy 10.128.0.2:24000 '" + url+"' -H 'Accept-Language: en-US,en;q=0.8' -H 'Upgrade-Insecure-Requests: 1' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36' -H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8' -H 'Connection: keep-alive' -H 'Cache-Control: max-age=0' ";
			Process process = Runtime.getRuntime().exec(command);
	        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String lineStr = StringUtils.EMPTY;
			while ((lineStr = reader.readLine()) != null) { 
				sb.append(lineStr);
			} 
			content = sb.toString();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(reader);
		}
		return content;
	}

	public static String get(String url, int timeout, String proxyIp, int proxyPort){
		if(timeout < 0){
			timeout = DEFAULT_TIMEOUT;
		}
		
		StringBuffer sb = new StringBuffer();
		String content = StringUtils.EMPTY;
		BufferedReader reader = null;
		try{
			String command = StringUtils.EMPTY;
			
			if(StringUtils.isNotBlank(proxyIp) && proxyPort > 0){
				command = "curl -x "+proxyIp+":"+proxyPort+" -connect-timeout "+timeout+" -s "+url;
			}else{
				command = "curl -connect-timeout "+timeout+" -s "+url;
			} 
			Process process = Runtime.getRuntime().exec(command);
	        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String lineStr = StringUtils.EMPTY;
			while ((lineStr = reader.readLine()) != null) { 
				sb.append(lineStr);
			} 
			content = sb.toString();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			IOUtils.closeQuietly(reader);
		}
		return content;
	}
}
