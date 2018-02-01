package com.haitao55.spider.crawler.core.callable.custom.vitaminplanet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class VitaminPlanetSelectAllPages extends SelectUrls{

	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Connection", "keep-alive");
		return headers;
	}
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String css;
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		Proxy proxy = null;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		Document doc = JsoupUtils.parse(content);
		String hdnTotalRecordsCount = doc.select("input#hdnTotalRecordsCount").get(0).attr("value");
		String hdnPageSize = doc.select("input#hdnPageSize").get(0).attr("value");
		
		if (StringUtils.isNotBlank(hdnTotalRecordsCount) && StringUtils.isNotBlank(hdnPageSize)) {
		 if (Integer.parseInt(hdnTotalRecordsCount) > Integer.parseInt(hdnPageSize)) {
			int totalPage = Integer.parseInt(hdnTotalRecordsCount) / Integer.parseInt(hdnPageSize);
			String catId = StringUtils.substringBetween(url, "fiCategoryId=", "&fiBrandId").trim();
			List<String> formatNewUrlValues = new ArrayList<String>();
			for( int i = 1 ;i <= totalPage;i++){
				String pageUrl = "https://www.vitaminplanet.cn/Products/getProducts?fsSort=Best+Selling&fsViewType=list&fiPageNo="+i+"&fsSearch=&fiCategoryId="+catId+"&fiBrandId=0&fiAttributeId=0";
				formatNewUrlValues.add(pageUrl);
			}
			Set<Url> newUrls = this.buildNewUrls(formatNewUrlValues, context, type, grade);
			context.getUrl().getNewUrls().addAll(newUrls);
		  }
		} else {
			logger.warn("Not get any data for page", context.getUrl().getValue());
		}
	}
	
	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

}
