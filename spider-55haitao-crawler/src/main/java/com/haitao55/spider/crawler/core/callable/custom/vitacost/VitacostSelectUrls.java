package com.haitao55.spider.crawler.core.callable.custom.vitacost;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class VitacostSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL = "https://www.vitacost.com";
	
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
		if(StringUtils.isNotBlank(content)){
			content = Native2AsciiUtils.ascii2Native(content);
			content = content.replaceAll("[\\\\]", "");
			Document doc = Jsoup.parse(content);
			Elements es = doc.select(".pb-image a");
			es.forEach(e->{
				String proUrl = e.attr("href");
				if(StringUtils.isNotBlank(proUrl)){
					newUrlValues.add(BASE_URL+proUrl);
				}
			});
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("VitacostSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
