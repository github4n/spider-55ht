package com.haitao55.spider.crawler.core.callable.custom.footlocker;



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

public class FootlockerCates extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isNotBlank(proxyRegionId)) {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String ip = proxy.getIp();
			int port = proxy.getPort();
			content = CurlCrawlerUtil.get(url,20,ip,port);
			logger.info("use proxy ip {}, port {},url {}",ip,port,url);
		} else {
			content = CurlCrawlerUtil.get(url);
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		Elements es = doc.select(".shop_sub_menu a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String urlList = e.attr("href");
				if(StringUtils.isNotBlank(urlList)){
					newUrlValues.add(urlList);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Footlocker Cates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
}
