package com.haitao55.spider.crawler.core.callable.custom.dermstore;





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

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

public class DermstoreSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL ="https://www.dermstore.com";
	
	@Override
	public void invoke(Context context) throws Exception {
	    
	    String content =  StringUtils.EMPTY;
	    String url = context.getUrl().getValue();
	    String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
        if(context.getUrl().getTask() != null && StringUtils.isNotBlank(proxyRegionId)){
            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
            String proxyAddress=proxy.getIp();
            int proxyPort=proxy.getPort();
            content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
            
        }else{
            content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
                    .resultAsString();
        }
	    
		//String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();

		Document doc = Jsoup.parse(content);
		Elements es = doc.select("#navPS > div > div > div.container > div.row.noPad > div.col-md-3.subnav_section > ul > li > a");
		logger.info("DermstoreSelectUrls es size {}",es== null?0:es.size());
		for(Element e : es){
			String href = e.attr("href");
			logger.info("DermstoreSelectUrls href {}",href);
			if(StringUtils.isNotBlank(url) && !StringUtils.contains(href, "dermstore.com")){
				newUrlValues.add(BASE_URL+href);
			}
		}
		
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
        context.getUrl().getNewUrls().addAll(value);
		logger.info("Dermstore item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
