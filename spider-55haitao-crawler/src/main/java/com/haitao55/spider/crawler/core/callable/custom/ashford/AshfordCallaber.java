package com.haitao55.spider.crawler.core.callable.custom.ashford;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;

public class AshfordCallaber implements Callable<JSONObject> {
	private static final String item_url = "http://zh.ashford.com/us/browse/gadgets/pickerContentsSizeSelector.jsp?selector=size&productId=()&selectedSize={}";
	private static final String stock_flag = "暂时缺货";
	
	private String productId;
	private String param;
	private Url url;
	

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public AshfordCallaber(String productId, String param, Url url) {
		this.productId = productId;
		this.param = param;
		this.url = url;
	}

	@Override
	public JSONObject call() throws Exception {
		//sku JsonArray
		JSONObject skuJsonObject = new JSONObject();
		
		String url_temp = StringUtils.replacePattern(item_url, "\\(\\)", productId);
		url_temp = StringUtils.replacePattern(url_temp, "\\{\\}", param);
		String content = crawler_result(url,url_temp);
		
		int stock_status = 1;
		if(StringUtils.contains(content, stock_flag)){
			stock_status = 0;
		}
		
		param = StringUtils.replacePattern(param,"%20"," ");
		skuJsonObject.put("size", param);
		skuJsonObject.put("stock_status", stock_status);
		skuJsonObject.put("goods_id", param.concat(productId));
		return skuJsonObject;
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
			 content = Crawler.create().timeOut(15000).url(path).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(15000).url(path).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 return content;
	}
}
