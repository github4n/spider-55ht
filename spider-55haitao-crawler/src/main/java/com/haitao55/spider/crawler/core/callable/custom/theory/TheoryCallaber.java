package com.haitao55.spider.crawler.core.callable.custom.theory;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class TheoryCallaber implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(TheoryCallaber.class);
	private String sizeKey;
	private String param;
	private Url url;

	public TheoryCallaber(String sizeKey ,String param , Url url) {
		super();
		this.sizeKey = sizeKey;
		this.param = param;
		this.url = url;
	}

	public TheoryCallaber() {
		super();
	}


	@Override
	public JSONObject call(){
		JSONObject jsonObject = new JSONObject();
		
		try {
			String html= StringUtils.EMPTY;
			boolean isRunInRealTime = true;//context.isRunInRealTime();
			if (isRunInRealTime) {
				LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
				html = luminatiHttpClient.request(param, getHeaders());
			}else{
				html = crawler_result(url,param);
			}
			
			if(StringUtils.isNotBlank(html)){
				Document doc = JsoupUtils.parse(html);
				String instock = doc.select("span.bold").text();
				if(StringUtils.isNotBlank(instock) && 
						StringUtils.containsIgnoreCase(instock, "is sold out")){
					jsonObject.put(sizeKey, 0);
				}else{
					jsonObject.put(sizeKey, 1);
				}
			}
			
		} catch (HttpException | IOException e) {
			logger.error("TheoryCallaber request url error , url: {} ,  exception:{} ",param,e);
		}
		return jsonObject;
		
	}

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.75 Chrome/62.0.3202.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.theory.com");
		headers.put("cookie","__cfduid=d93ce31324d0eb2776d120da671fe777e1510292470; dwac_bcCM6iaagVSqEaaadr8UJrigw0=Z3Kwqt2TdUvA-833nTw_XtGTFxdrvY7ATVU%3D|dw-only|||USD|false|US%2FEastern|true; cqcid=abEOJH2tNdZud5YO9yKENWVQJc; sid=Z3Kwqt2TdUvA-833nTw_XtGTFxdrvY7ATVU; dwanonymous_d7f948559b2a8e9db6182d915a93ea03=abEOJH2tNdZud5YO9yKENWVQJc; dwsid=8teX1DCOImy-91ksrRl2QEQ2MGmGoNnnM1kytYr1_TPN3EJw-CFE1sVl0qysGPWz-FEAN7Xa3Z014oBCHjMLnw==; dw=1; dw_cookies_accepted=1; sr_browser_id=44afcd15-0fae-4a50-a839-4500290d5600; sr_pik_session_id=be84a2d8-1aa7-795b-3513-18153bf39ed4; newSession=new; _uetsid=_uet4933208e; _ga=GA1.2.1964719823.1510292472; _gid=GA1.2.2102611376.1510292472; __cq_bc=%7B%22aado-theory2_US%22%3A%5B%7B%22id%22%3A%22H1071103%22%7D%2C%7B%22id%22%3A%22H05AC007%22%2C%22sku%22%3A%22190789110127%22%7D%5D%7D; __cq_seg=0~0.15!1~0.45!2~-0.03!3~-0.06!4~-0.53!5~0.67!6~0.09!7~-0.09!8~-0.00!9~-0.16!f0~15~9; __cq_uuid=41de96c0-a6ef-11e6-92ed-47e9ea91daa6; _sr_sp_id.5eb1=23000d73-7a1d-4e49-9483-c4e2deae7460.1510292473.1.1510292584.1510292473.c9baba3f-ce76-41be-aa66-226739ab1aee; _sr_sp_ses.5eb1=*");
		return headers;
	}
	
	/**
	 * 线上爬取
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Url url, String path) throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = url.getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).proxy(false).method(HttpMethod.GET.getValue()).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).proxy(true).method(HttpMethod.GET.getValue()).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}
	
}
