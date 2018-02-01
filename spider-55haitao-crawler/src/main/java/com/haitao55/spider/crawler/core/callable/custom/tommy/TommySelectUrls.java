package com.haitao55.spider.crawler.core.callable.custom.tommy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class TommySelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String LIST_API = "http://usa.tommy.com/ProductListingView?pageSize=2000&";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
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
		List<String> newUrlValues = new ArrayList<String>();
		
		String catalogId = StringUtils.substringBetween(content, "catalogId\":'", "'");
		String langId = StringUtils.substringBetween(content, "langId\":'", "'");
		String storeId = StringUtils.substringBetween(content, "storeId\":'", "'");
		String categoryId = StringUtils.substringBetween(content, "pageId\" content=\"", "\"");
		
		if(StringUtils.isNotBlank(catalogId) && 
				StringUtils.isNotBlank(storeId)){
			String lurl = LIST_API+"catalogId="+catalogId+"&categoryId="+categoryId+"&langId="+langId+"&storeId="+storeId;
			Url currentUrl = new Url(lurl);
			currentUrl.setTask(context.getUrl().getTask());
			String html = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
			Document docment = Jsoup.parse(html);
			Elements es = docment.select("a.productThumbnail");
			if(es != null && es.size() > 0){
				for(Element e : es){
					String durl = e.attr("href");
					if(StringUtils.isNotBlank(durl)){
						newUrlValues.add(durl);
					}
				}
			}
		}
	
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("superdrug item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
