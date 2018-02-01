package com.haitao55.spider.common.utils;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;

import com.haitao55.spider.common.http.HTTPSTrustManager;

public class LuminatiHttpClient {
    public static final String residential_username = "lum-customer-55haitao-zone-gen";
    public static final String residential_password = "5b1f51a85e5a";
    //zone_name = zone_us
    public static final String zone_us_username = "lum-customer-55haitao-zone-zone_us";
    public static final String zone_us_password = "s3rmk3t0e0wx";
    //zone_name = 55ht_zone_us
    public static final String ht_zone_us = "lum-customer-55haitao-zone-55ht_zone_us";
    public static final String ht_zone_passwd = "knicn7eopq3a";
    
    
	public static final int port = 22225;
	public String session_id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
	public CloseableHttpClient client;

	public LuminatiHttpClient(String country,boolean isResidential) {					
		String username = "";
		String password = "";
		if(isResidential){
			username = residential_username;
			password = residential_password;
		} else {
			username = zone_us_username;
			password = zone_us_password;					
		}
		String login = username + (country != null ? "-country-" + country : "") + "-session-" + session_id;
		System.out.println(login);
		HttpHost super_proxy = new HttpHost("zproxy.luminati.io", port);
		CredentialsProvider cred_provider = new BasicCredentialsProvider();
		cred_provider.setCredentials(new AuthScope(super_proxy),new UsernamePasswordCredentials(login, password));
		client = HttpClients.custom()
				.setConnectionManager(new BasicHttpClientConnectionManager())
				.setProxy(super_proxy).setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36")
				.setDefaultCredentialsProvider(cred_provider).build();
	}
	
	public LuminatiHttpClient(String country,String zone) {						
		String username = "";
		String password = "";
			if(StringUtil.isBlank(zone)){
				username = zone_us_username;
				password = zone_us_password;
			}else{
				if("55ht_zone_us".equals(zone)){
					username = ht_zone_us;
					password = ht_zone_passwd;
				}else if("zone_us".equals(zone)){
					username = zone_us_username;
					password = zone_us_password;
				}
			}	
		String login = username + (country != null ? "-country-" + country : "") + "-session-" + session_id;
		System.out.println(login);
		HttpHost super_proxy = new HttpHost("zproxy.luminati.io", port);
		CredentialsProvider cred_provider = new BasicCredentialsProvider();
		cred_provider.setCredentials(new AuthScope(super_proxy),new UsernamePasswordCredentials(login, password));
		client = HttpClients.custom()
				.setConnectionManager(new BasicHttpClientConnectionManager())
				.setProxy(super_proxy).setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36")
				.setDefaultCredentialsProvider(cred_provider).build();
	}

	public String request(String url,Map<String,Object> headers) {
	    HttpGet request = new HttpGet(url);
	    if(MapUtils.isNotEmpty(headers)){
	        headers.forEach( (k,v) -> {
	        	if(v == null){
	        		return ;
	        	}
		    	request.addHeader(k, v.toString());
		    });
	    }
		try (CloseableHttpResponse response = client.execute(request)) {
			return EntityUtils.toString(response.getEntity());
		}catch(Exception e){
		    e.printStackTrace();
		}
		return null;
	}
	
	public byte[] requestAsByteArray(String url,Map<String,Object> headers) {
	    HttpGet request = new HttpGet(url);
	    if(MapUtils.isNotEmpty(headers)){
	        headers.forEach( (k,v) -> {
	        	if(v == null){
	        		return ;
	        	}
		    	request.addHeader(k, v.toString());
		    });
	    }
		try (CloseableHttpResponse response = client.execute(request)) {
			return EntityUtils.toByteArray(response.getEntity());
		}catch(Exception e){
		    e.printStackTrace();
		}
		return null;
	}

	public void close() throws IOException {
		client.close();
	}
	
	
	public static void main(String[] args) {
	    /*String url = "https://www.amazon.com/s/ref=sr_pg_2?fst=as%3Aoff&rh=n%3A7141123011%2Cn%3A7147440011%2Cn%3A1040660%2Cn%3A1045024%2Cp_6%3AATVPDKIKX0DER%2Cp_n_is-min-purchase-required%3A5016683011&page=2&bbn=1045024&sort=price-asc-rank&ie=UTF8&qid=1488527813";
		LuminatiHttpClient client = new LuminatiHttpClient(null,true);
		try {
			long start = System.currentTimeMillis();
			String result = client.request(url);
			Pattern p1 = Pattern.compile("<div[^>]*id=[^>]*pagn[^>]*>(.*?)<span[^>]*class=[^>]*pagnDisabled[^>]*>(.*?)</span>");
      		Matcher m1 = p1.matcher(result);
      		System.out.println(result);
      		while(m1.find()){
      			System.out.println(m1.group());
      		}
			long end = System.currentTimeMillis();
			System.out.println(end - start);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/
	}
}
