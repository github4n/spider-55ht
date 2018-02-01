package com.haitao55.spider.crawler.core.callable.custom.rebeccaminkoff;





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

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class RebeccaminkoffSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL ="https://www.rebeccaminkoff.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = crawlerUrl(context,context.getCurrentUrl());
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			Elements es  = doc.select(".collection-grid--block-img a");
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					newUrlValues.add(BASE_URL+url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("RebeccaminkoffSelectUrls item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content =CurlCrawlerUtil.get(url, 30);
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = CurlCrawlerUtil.get(url, 30, proxyAddress, proxyPort);
		}
		return content;
	}
}
