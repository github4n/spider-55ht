package com.haitao55.spider.crawler.core.callable.custom.neimanmarcus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
 * 功能：通过css选择器,专门用来提取页面上的urls超级链接
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 上午11:39:15
 * @version 1.0
 * @see Select
 * @see SelectNeimanmarcusPages
 */
public class SelectNeimanmarcusUrls extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String REQUEST_URL = "http://www.neimanmarcus.com/category.service?instart_disable_injection=true";
	private static final String REQUEST_PARAM = "{\"GenericSearchReq\":{\"pageOffset\":{},\"pageSize\":\"()\",\"refinements\":\"\",\"selectedRecentSize\":\"\",\"activeFavoriteSizesCount\":\"0\",\"activeInteraction\":\"true\",\"mobile\":false,\"sort\":\"PCS_SORT\",\"definitionPath\":\"/nm/commerce/pagedef_rwd/template/EndecaDrivenHome\",\"userConstrainedResults\":\"true\",\"rwd\":\"true\",\"advancedFilterReqItems\":{\"StoreLocationFilterReq\":[{\"locationInput\":\"\",\"radiusInput\":\"100\",\"allStoresInput\":\"false\",\"onlineOnly\":\"\"}]},\"categoryId\":\"[]\",\"sortByFavorites\":false,\"isFeaturedSort\":false,\"prevSort\":\"\"}}";

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
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl().toString();
		String content = HttpUtils.crawler_package(context);
		String param = request_param_package(content, url, REQUEST_PARAM);
		String service_content = crawler_package(context,param,REQUEST_URL);
		JSONObject parseObject = JSONObject.parseObject(service_content);
		String data = StringUtils.EMPTY;
		if(MapUtils.isNotEmpty(parseObject)){
			JSONObject jsonObject = parseObject.getJSONObject("GenericSearchResp");
		    data = jsonObject.getString("productResults");
		}
		Document doc = JsoupUtils.parse(data);
		Elements elements = doc.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> newUrlValues = JsoupUtils.attrs(elements, attr);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
				newUrlValues = this.reformStrings(newUrlValues);
			}
			if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex2())) {
				newUrlValues = this.replaceNewUrls(newUrlValues);
			}
			if(CollectionUtils.isNotEmpty(newUrlValues)){
				newUrlValues = substringUrls(newUrlValues);
			}
			
			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}
	}

	private Map<String, Object> getPayload(String param) {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("data", param);
		payload.put("service", "getCategoryGrid");
		payload.put("sid", "getCategoryGrid");
		payload.put("bid", "GenericSearchReq");
		return payload;
	}

	private Map<String, Object> getHeaders() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Cookie",
				"D_SID=192.243.119.27:ktv05/kM5944hhpHeBWoVQNg/u+tgjfEpcwSc3mjMl8; D_UID=00DF1175-B02B-3AB1-9E0F-E825739FB2FC; D_HID=TRqZifCO2NfBKlXLVOFDX5Aq7WFPuaR+0z5LPSPhmVs;");
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		headers.put("X-Distil-Ajax", "tvuqytaytfduavtvxyedrtbtxrrffwre");
		return headers;
	}
	
	private String request_param_package(String content, String url, String requestParam) {
		String categoryId = StringUtils.substringBetween(content, "categoryId=\"", "\"");
		String page = StringUtils.substringBetween(url, "page=", "&");
		String pageSize = StringUtils.substringBetween(url, "pageSize=", "&");
		String replacePattern = StringUtils.replacePattern(REQUEST_PARAM, "\\{\\}", page);
		replacePattern = StringUtils.replacePattern(replacePattern, "\\(\\)", pageSize);
		replacePattern = StringUtils.replacePattern(replacePattern, "\\[\\]", categoryId);
		return replacePattern;
	}

	private List<String> substringUrls(List<String> newUrlValues) {
		List<String> newUrls=new ArrayList<String>();
		if(null!=newUrlValues&&newUrlValues.size()>0){
			for (String value : newUrlValues) {
				String substringBetween = StringUtils.substringBetween(value, "/en-", "/");
				if(StringUtils.isNotBlank(substringBetween)){
					value = StringUtils.replacePattern(value, "/en-"+substringBetween+"/", "/");
				}
				newUrls.add(value);
			}
		}
		return newUrls;
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
	private String crawler_package(Context context,String param,String url) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(15000).url(url).payload(getPayload(param)).header(getHeaders())
					.method(HttpMethod.POST.getValue()).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).payload(getPayload(param)).header(getHeaders())
					.method(HttpMethod.POST.getValue()).proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort)
					.resultAsString();
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
}