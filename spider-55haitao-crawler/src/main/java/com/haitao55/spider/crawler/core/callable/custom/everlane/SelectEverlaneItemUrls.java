package com.haitao55.spider.crawler.core.callable.custom.everlane;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * everlane 一级类目获取
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月28日 下午3:44:50
* @version 1.0
 */
public class SelectEverlaneItemUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String ITEM_LIST_URL = "https://www.everlane.com/api/v2";
	private static final String ITEM_URL_PREFFIX = "https://www.everlane.com/products/";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl().toString();
		if(StringUtils.isNotBlank(url)){
			url = StringUtils.substringAfter(url, "https://www.everlane.com");
		}
		String request_url = ITEM_LIST_URL+url;
		//crawler
		String content = crawler_package(context,request_url);
		//urls
		List<String> newUrlValues = new ArrayList<String>();
		//json
		JSONObject parseObject = JSONObject.parseObject(content);
		JSONArray jsonArray = parseObject.getJSONArray("products");
		if(null != jsonArray && jsonArray .size() > 0){
			for (Object object : jsonArray) {
				JSONObject jsonObject = (JSONObject)object;
				String item_url = jsonObject.getString("permalink");
				if(StringUtils.isNotBlank(item_url)){
					item_url = ITEM_URL_PREFFIX+item_url;
				}
				//add
				newUrlValues.add(item_url);
			}
		}

		// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
		if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
			newUrlValues = this.reformStrings(newUrlValues);
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(newUrls);
	}

	
	private String crawler_package(Context context,String url) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> headers = new HashMap<String,Object>();
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
}