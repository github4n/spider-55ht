package com.test.proxy;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.haitao55.spider.common.http.HttpClient;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpResult;

public class ProxyTest {
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String ip="10.47.90.213,10.47.57.20,10.47.104.132,10.47.96.231,10.47.120.9";
		String[] split = ip.split(",");
		HttpResult result;
		StringBuilder build=new StringBuilder();
		int count=0;
		HttpClient http = new HttpClient();
		for (int i = 0; i <split.length; i++) {
//			result=Crawler.create().url("www.baidu.com").proxy(true).proxyAddress(split[i])
//			.proxyPort(3128).resultAsString();
			try {
				
				result=http.get("http://www.baidu.com", 3000, split[i], 3128);
				if(StringUtils.isNotBlank(result.toString())){
					count++;
					continue;
				}
				build.append(split[i]+"   ,");
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		System.out.println(count);
		System.out.println(build.toString());
	}
}
