package com.haitao55.spider.crawler.core.callable.custom.topshop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

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
public class SelectTopShopUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private static final String page_to_url_flag = "Y";
	
	private static final String ATTR_HREF = "href";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// css选择器
	private String css;
	// 使用css选择器进行选择后,取元素的什么属性值；默认是href
	private String attr = ATTR_HREF;
	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	private String regex2;
	private String replacement2;
	
	private String page_to_url;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = crawler_result(context.getUrl(),context.getCurrentUrl());
		context.put(input, content);
		List<String> newUrlValues = new ArrayList<String>();
		if(StringUtils.equalsIgnoreCase(page_to_url_flag, page_to_url)){
			JSONObject jsonObject = JSONObject.parseObject(content);
			JSONObject resultJsonObject = jsonObject.getJSONObject("results");
			JSONArray contentsJsonArray = resultJsonObject.getJSONArray("contents");
			if(null != contentsJsonArray && contentsJsonArray.size() > 0){
				JSONObject dataJsonObject = contentsJsonArray.getJSONObject(0);
				JSONArray productJsonArray = dataJsonObject.getJSONArray("records");
				if(null != productJsonArray && productJsonArray.size() > 0){
					for (Object object : productJsonArray) {
						JSONObject productJsonObject = (JSONObject)object;
						String url = productJsonObject.getString("productUrl");
						newUrlValues.add(url);
					}
				}
			}
		}else{
			Document doc = super.getDocument(context);
			Elements elements = doc.select(css);
			newUrlValues = JsoupUtils.attrs(elements, attr);
		}
		
		if (CollectionUtils.isNotEmpty(newUrlValues)) {
			
			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
				newUrlValues = this.reformStrings(newUrlValues);
			}
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex2())) {
				newUrlValues = this.replaceNewUrls(newUrlValues);
			}

			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}

	private List<String> replaceNewUrls(List<String> newUrlValues) {
		List<String> newUrls=new ArrayList<String>();
		if(null!=newUrlValues&&newUrlValues.size()>0){
			for (String value : newUrlValues) {
				value=value.replaceAll(regex2, replacement2);
				newUrls.add(value);
			}
		}
		return newUrls;
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
	public String getRegex2() {
		return regex2;
	}

	public void setRegex2(String regex2) {
		this.regex2 = regex2;
	}

	public String getReplacement2() {
		return replacement2;
	}

	public void setReplacement2(String replacement2) {
		this.replacement2 = replacement2;
	}
	
	public String getPage_to_url() {
		return page_to_url;
	}

	public void setPage_to_url(String page_to_url) {
		this.page_to_url = page_to_url;
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
			 content = Crawler.create().timeOut(15000).url(path).header(getHeaders()).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(15000).url(path).header(getHeaders()).proxy(true).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		 Map<String,Object> headers = new HashMap<String,Object>();
			headers.put("Cookie", "userLanguage=en; prefShipCtry=GB; userCountry=United Kingdom; usergeo=USER;");
		 return content;
	}
	
	private static Map<String,Object> getHeaders(){
		@SuppressWarnings("serial")
		final Map<String,Object> headers = new HashMap<String,Object>(){
			{
				put("Cookie", "userLanguage=en; prefShipCtry=GB; userCountry=United Kingdom; usergeo=USER;");
			}
		};
		return headers;
	}
}