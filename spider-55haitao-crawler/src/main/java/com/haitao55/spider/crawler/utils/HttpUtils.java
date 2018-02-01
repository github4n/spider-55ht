package com.haitao55.spider.crawler.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;

/**
 * 
 * 功能：Http工具类
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:53:53
 * @version 1.0
 */
public class HttpUtils {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	public static final int DEFAULT_TIMEOUT = 30 * 1000;
	public static final int DEFAULT_RETRY_TIMES = 3;
	public static HashMap<String, Object> headers = null;
	
	static{
		headers=new HashMap<String,Object>();
		headers.put("Retry-After", 120);
		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; BIDUBrowser 2.x");
//		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36");
	}
	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String get(Url url) {
		return get(url, DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES,false);
	}

	/**
	 * 
	 * @param url
	 * @param timeOutMills
	 * @param retryTimes
	 * @return
	 */
	public static String get(Url url, int timeOutMills, int retryTimes) {
		return get(url, DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES,false);
	}
	
	public static String get(Url url, int timeOutMills, int retryTimes,boolean useFixedProxy) {
		if (url.getTask() != null && StringUtils.isNotBlank(url.getTask().getProxyRegionId())) {// 使用代理
			String proxyRegionId = url.getTask().getProxyRegionId();
			if(useFixedProxy){
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				return get(url.getValue(), timeOutMills, retryTimes, proxy);
			} else {
				String result = StringUtils.EMPTY;
				for(int i= 0; i < retryTimes;i++){
					Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
					result = get(url.getValue(), timeOutMills, 1, proxy);
					if(StringUtils.isNotBlank(result)){
						break;
					}
				}
				return result;
			}
			
		} else {// 不使用代理
			logger.info("Do not use proxy, url:{}", url.getValue());
			return get(url.getValue(), timeOutMills, retryTimes);
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		return get(url, DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES);
	}

	public static String get(String url, int timeOutMills, int retryTimes) {
		return get(url, timeOutMills, retryTimes, null);
	}

	/**
	 * 最终执行抓取操作的方法
	 * 
	 * @param url
	 * @param timeOutMills
	 * @return
	 */
	public static String get(String url, int timeOutMills, int retryTimes, Proxy proxy) {
		try {
			if (proxy != null) {// 1.使用代理
				String ip = proxy.getIp();
				int port = proxy.getPort();
				logger.info("Use proxy:ip:{}, port:{}, url:{}", proxy.getIp(), proxy.getPort(), url);
				return Crawler.create().timeOut(timeOutMills).url(url).retry(retryTimes).proxy(true).proxyAddress(ip)
						.proxyPort(port).header(headers).resultAsString();
			} else {// 2.不使用代理
				logger.info("Do not use proxy, url:{}", url);
				return Crawler.create().timeOut(timeOutMills).url(url).retry(retryTimes).header(headers).resultAsString();
			}
		} catch (HttpException e) {
			logger.error("url {} timeout {} {} {}", url, timeOutMills, e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			logger.error("url {} got error:{}", url, e);
		}
		return StringUtils.EMPTY;
	}

	public static byte[] getBytes(Url url) {
		return getBytes(url, DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES);
	}

	public static byte[] getBytes(Url url, int timeOutMills, int retryTimes) {
		String proxyIpLocale = url.getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyIpLocale)) {// 使用代理
			Proxy proxy = ProxyCache.getInstance().pickup(proxyIpLocale, true);
			return getBytes(url.getValue(), timeOutMills, retryTimes, proxy);
		} else {// 不使用代理
			return getBytes(url.getValue(), timeOutMills, retryTimes);
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public static byte[] getBytes(String url) {
		return getBytes(url, DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES);
	}

	/**
	 * 
	 * @param url
	 * @param timeOutMills
	 * @return
	 */
	public static byte[] getBytes(String url, int timeOutMills, int retryTimes) {
		return getBytes(url, timeOutMills, retryTimes, null);
	}

	public static byte[] getBytes(String url, int timeOutMills, int retryTimes, Proxy proxy) {
		try {
			if (proxy != null) {// 使用代理
				String ip = proxy.getIp();
				int port = proxy.getPort();
				logger.info("Use Proxy in crawler for bytes::ip:{};port:{}", ip, port);
				return Crawler.create().timeOut(timeOutMills).url(url).retry(retryTimes).proxy(true).proxyAddress(ip)
						.proxyPort(port).header(headers).resultAsBytes();
			} else {// 不使用代理
				return Crawler.create().timeOut(timeOutMills).url(url).retry(retryTimes).header(headers).resultAsBytes();
			}
		} catch (HttpException e) {
			logger.error("url {} timeout {} {}", url, timeOutMills, e.getMessage(), e);
			throw e;
		} catch (Exception ignore) {

		}
		return null;
	}
	
	
	/**
	 * 爬取公用方法
	 * @param url
	 * @param headers
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	public static String crawler_package(Url url, Map<String, Object> headers) throws ClientProtocolException, HttpException, IOException {
		return crawler_package(url, headers,null,null,DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES);
	}
	
	
	/**
	 * 爬取公用方法，当需要设置headers payload 等时   使用
	 * @param context
	 * @param headers
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 * @throws ClientProtocolException 
	 */
	public static String crawler_package(Context context,Map<String,Object> headers) throws ClientProtocolException, HttpException, IOException{
		return crawler_package(context,headers,null,null);
		
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException{
		return crawler_package(context,null,null,null);
	}
	/**
	 * 
	 * @param context
	 * @param headers
	 * @param payload
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String crawler_package(Context context,Map<String,Object> headers,Map<String,Object> payload) throws ClientProtocolException, HttpException, IOException{
		return crawler_package(context,headers,payload,null);
		
	}
	public static String crawler_package(Context context,Map<String,Object> headers,Map<String,Object> payload,String method) throws ClientProtocolException, HttpException, IOException {
		return crawler_package(context,headers,payload,method,DEFAULT_TIMEOUT, DEFAULT_RETRY_TIMES);
	}
	/**
	 * 
	 * @param context
	 * @param headers
	 * @param payload
	 * @param method
	 * @param timeOutMills
	 * @param retryTimes
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private static String crawler_package(Object context,Map<String,Object> headers,Map<String,Object> payload,String method,int timeOutMills, int retryTimes) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = StringUtils.EMPTY;
		if(context instanceof Context){
			Context contextTemp = (Context)context;
			proxyRegionId = contextTemp.getUrl().getTask().getProxyRegionId();
			
		}else if(context instanceof Url){
			Url urlTemp = (Url)context;
			proxyRegionId = urlTemp.getTask().getProxyRegionId();
		}
		if(StringUtils.isBlank(proxyRegionId)){
			content = crawler(context,headers,payload,method,null,null,timeOutMills,retryTimes);
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = crawler(context,headers,payload,method,proxyAddress,proxyPort,timeOutMills,retryTimes);
		}
		return content;
	}

	private static String crawler(Object context, Map<String, Object> headers, Map<String, Object> payload, String method, String proxyAddress, Integer proxyPort, int timeOutMills, int retryTimes) throws ClientProtocolException, HttpException, IOException {
		String url = StringUtils.EMPTY;
		if(context instanceof Context){
			Context contextTemp = (Context)context;
			url = contextTemp.getCurrentUrl().toString();
			
		}else if(context instanceof Url){
			Url urlTemp = (Url)context;
			url = urlTemp.getValue();
		}
		String content = StringUtils.EMPTY;
		if(StringUtils.isBlank(method)){
			method = HttpMethod.GET.getValue();
		}
		Crawler crawler = Crawler.create().timeOut(timeOutMills).retry(retryTimes).url(url).method(method);
		if(null!=headers&& !headers.isEmpty()){
			crawler.header(headers);
		}
		if(null!=payload&& !payload.isEmpty()){
			crawler.payload(payload);
		}
		if(StringUtils.isNotBlank(proxyAddress)){
			crawler.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort);
		}
		content = crawler.resultAsString();
		return content;
	}

}