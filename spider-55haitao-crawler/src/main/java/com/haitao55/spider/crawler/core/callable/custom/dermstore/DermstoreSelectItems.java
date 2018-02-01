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
import com.haitao55.spider.crawler.utils.Constants;

public class DermstoreSelectItems extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	
	@Override
	public void invoke(Context context) throws Exception {
	    String url = context.getCurrentUrl();
	    String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
	    String content = StringUtils.EMPTY;
	    if(StringUtils.isNotBlank(proxyRegionId)){
	        //content = super.getInputString(context);
	        Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
            String proxyAddress=proxy.getIp();
            int proxyPort=proxy.getPort();
            content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
	    }
		//String content = super.getInputString(context);
		
		List<String> newUrlValues = new ArrayList<String>();
		
		Document doc = Jsoup.parse(content,url);
		Elements es = doc.select("#prodGrid > div.row > div.col-sm-4 > div.prod-widget-responsive > a");
		if(es != null && es.size() > 0 ){
		    for(Element el : es){
		        String href = el.attr("abs:href");
		        if(StringUtils.isNotBlank(href)){
		            newUrlValues.add(href);	        
		        }
		    }
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Dermstore list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
