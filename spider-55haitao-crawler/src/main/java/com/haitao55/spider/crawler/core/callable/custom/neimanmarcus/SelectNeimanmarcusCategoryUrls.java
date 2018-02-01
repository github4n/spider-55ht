package com.haitao55.spider.crawler.core.callable.custom.neimanmarcus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 获取首页分类url Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月16日 上午11:41:22
 * @version 1.0
 */
public class SelectNeimanmarcusCategoryUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String SERVICE_URL = "http://www.neimanmarcus.com/deferred.service?instart_disable_injection=true";
	private static final String ATTR_HREF = "href";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// css选择器
	private String css;
	// 使用css选择器进行选择后,取元素的什么属性值；默认是href
	private String attr = ATTR_HREF;
	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;

	@Override
	public void invoke(Context context) throws Exception {
		String content = crawler_package(context,getPayload() ,getHeaders(),  SERVICE_URL, HttpMethod.POST.getValue());
		if (StringUtils.isNotBlank(content)) {
			JSONObject parseObject = JSONObject.parseObject(content);
			JSONObject jsonObject = parseObject.getJSONObject("RWD.deferredContent.DeferredContentRespObj");
			if(null != jsonObject && !jsonObject.isEmpty()){
				String urls_content = jsonObject.getString("content");
				Document doc = JsoupUtils.parse(urls_content);
				Elements elements = doc.select("nav[role=navigation] a");
				List<String> newUrlValues = JsoupUtils.attrs(elements, attr);
	
				Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
	
				// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
				context.getUrl().getNewUrls().addAll(newUrls);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}

	private Map<String, Object> getPayload() {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("data",
				"$b64$eyJSV0QuZGVmZXJyZWRDb250ZW50LkRlZmVycmVkQ29udGVudFJlcU9iaiI6eyJjb250ZW50UGF0aCI6Ii9wYWdlX3J3ZC9oZWFkZXIvc2lsb3Mvc2lsb3MuanNwIiwiY2F0ZWdvcnkiOiJjYXQwMDAwMDAiLCJjYWNoZUtleSI6InJfcmVzcG9uc2l2ZURyYXdlcnNIZWFkZXJfVVNfZW4ifX0$");
		payload.put("sid", "getResponse");
		payload.put("bid", "DeferredContentReqObj");
		return payload;
	}

	private Map<String, Object> getHeaders() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Cookie",
				"D_SID=192.243.119.27:ktv05/kM5944hhpHeBWoVQNg/u+tgjfEpcwSc3mjMl8; D_UID=00DF1175-B02B-3AB1-9E0F-E825739FB2FC; D_HID=TRqZifCO2NfBKlXLVOFDX5Aq7WFPuaR+0z5LPSPhmVs;");
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		return headers;
	}

	private String crawler_package(Context context,  Map<String, Object> payload,Map<String, Object> headers,
			String url, String method) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(15000).url(url).payload(payload).header(headers).method(method)
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).payload(payload).header(headers).method(method)
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
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