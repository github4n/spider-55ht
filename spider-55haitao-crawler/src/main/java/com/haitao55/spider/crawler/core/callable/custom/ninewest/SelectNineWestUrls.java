package com.haitao55.spider.crawler.core.callable.custom.ninewest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

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

/**
 * ninewest 获取urls
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月6日 下午2:12:20
* @version 1.0
 */
public class SelectNineWestUrls extends AbstractSelectUrls {
	private static final String TOKEN = "http://www.ninewest.com/on/demandware.store/Sites-ninewest-Site/default/Token-RequestToken";
	public int grade;
	public String type;
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		//由于page 页的token参数是改变的
		if(StringUtils.containsIgnoreCase(content, "error")){
			String url = context.getCurrentUrl().toString();
			Pattern compile = Pattern.compile("token=(.*)&returnObject");
			Matcher matcher = compile.matcher(url);
			String token_temp = StringUtils.EMPTY;
			if(matcher.find()){
				token_temp = matcher.group(1);
			}
			String token = crawler_package(context,TOKEN);
			if(StringUtils.isNotBlank(token)){
				token = StringUtils.replacePattern(token, "[\n]", "");
			}
			url = StringUtils.replacePattern(url, token_temp, token);
			content = crawler_package(context,url);
		}
		JSONObject resultJSONObject = JSONObject.parseObject(content);
		JSONObject responseJSONObject = resultJSONObject.getJSONObject("response");
		JSONArray productJsonArray = responseJSONObject.getJSONArray("searchResults");
		
		List<String> newUrlValues = new ArrayList<String>();
		
		for (Object object : productJsonArray) {
			JSONObject jsonObject = (JSONObject)object;
			String product_url = jsonObject.getString("url");
			
			newUrlValues.add(product_url);
			
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
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
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