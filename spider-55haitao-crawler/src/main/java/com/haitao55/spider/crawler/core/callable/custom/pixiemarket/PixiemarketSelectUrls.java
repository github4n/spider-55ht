package com.haitao55.spider.crawler.core.callable.custom.pixiemarket;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class PixiemarketSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private static final String BASE_URL = "https://www.pixiemarket.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = CurlCrawlerUtil.get(context.getCurrentUrl());
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = CurlCrawlerUtil.get(context.getCurrentUrl(), 30, proxyAddress, proxyPort);
		}
		List<String> newUrlValues = new ArrayList<String>();
		Document docment = Jsoup.parse(content);
		Elements es = docment.select("h2.product-name>a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					if(StringUtils.containsIgnoreCase(url, "?")){
						url = url.substring(0, url.indexOf("?"));
					}
					newUrlValues.add(BASE_URL + url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("PixiemarketSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
