package com.haitao55.spider.crawler.core.callable.custom.puma;






import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class PumaSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		List<String> newUrlValues = new ArrayList<String>();
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			Elements es = doc.select("a.thumb-link");
			es.forEach(e->{
				String proUrl = e.attr("href");
				if(proUrl.contains("?")){
					proUrl = proUrl.substring(0, proUrl.indexOf("?"));
				}
				newUrlValues.add(proUrl);
			});
		}

		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("PumaSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "*/*");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "us.puma.com");
		 //headers.put("Cookie", setCookie());
		return headers;
	}
}
