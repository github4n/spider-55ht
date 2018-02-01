package com.haitao55.spider.crawler.core.callable.custom.kohls;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectPages;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;

/**
 * 
 * 功能：通过css选择器,专门用来提取页面上的urls超级链接
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 上午11:39:15
 * @version 1.0
 * @see Select
 * @see SelectPages
 */
public class SelectKohlsUrls extends AbstractSelectUrls {
	public int grade;
	public String type;
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		
		String productListData = StringUtils.substringBetween(content, "productList\":", ",\"tabInfo\"");
		
		if(StringUtils.isBlank(productListData)){
			String url = context.getCurrentUrl();
			url = StringUtils.replacePattern(url, " ", "%20");
			content = crawler_result(context,url);
		}
		
		JSONArray productJsonArray = JSONArray.parseArray(productListData);
		
		List<String> newUrlValues = new ArrayList<String>();
		
		for (Object object : productJsonArray) {
			String urlTemp = StringUtils.EMPTY;
			JSONObject jsonObject = (JSONObject)object;
			String color = jsonObject.getString("displayColor");
			String product_url = jsonObject.getString("prodSeoURL");
			if(StringUtils.isNotBlank(color)){
				if(!StringUtils.contains(product_url, "?")){
					urlTemp = product_url.concat("?color="+color+"");
				}else{
					urlTemp = product_url.concat("color="+color+"");
				}
			}
			
			newUrlValues.add(urlTemp);
			
		}

		// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
		if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
			newUrlValues = this.reformStrings(newUrlValues);
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(newUrls);
	}
	
	
	/**
	 * 线上爬取
	 * @param content
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Context context, String path) throws ClientProtocolException, HttpException, IOException {
		Url url = context.getUrl();
		String content = StringUtils.EMPTY;
		String proxyRegionId = url.getTask().getProxyRegionId();
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
	
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}