package com.haitao55.spider.crawler.core.callable.custom.dermstore;





import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
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
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

public class DermstoreSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String DERMS_API = "http://www.dermstore.com/list_grid.php?attributes=#pid#&ipp=120&layout=col-sm-4&sort=&page=#page#";
	
	@SuppressWarnings("unused")
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
		
		String pId = StringUtils.substringBetween(content, "pcat\":\"", "\"");
		if(StringUtils.isNotBlank(pId)){
			String dermsUrl = DERMS_API.replace("#pid#", pId).replace("#page#", "1");
			String html = StringUtils.EMPTY;
			
			if(StringUtils.isBlank(proxyRegionId)){
				html = Crawler.create().timeOut(60000).url(dermsUrl).method(HttpMethod.GET.getValue())
						.resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress=proxy.getIp();
				int proxyPort=proxy.getPort();
				html = Crawler.create().timeOut(60000).url(dermsUrl).method(HttpMethod.GET.getValue())
						.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
			}
			String pageCount = StringUtils.EMPTY;
			Document document = Jsoup.parse(html);
			Elements es = document.select("a.page_list");
			if(CollectionUtils.isNotEmpty(es)){
				for(int i = es.size()-2; i >= 0;--i){
					pageCount = es.get(i).text();
					break;
				}
			}
			if(StringUtils.isBlank(pageCount) ||
					StringUtils.containsIgnoreCase(pageCount, "next")){
				newUrlValues.add(DERMS_API.replace("#pid#", pId).replace("#page#", "1"));
			}
			
			if(StringUtils.isNotBlank(pageCount) 
					&& !StringUtils.containsIgnoreCase(pageCount, "next")){
				for(int j = 1 ;j <= Integer.parseInt(pageCount); j++){
					newUrlValues.add(DERMS_API.replace("#pid#", pId).replace("#page#", String.valueOf(j)));
				}
			}
			
		}
	
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(), grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Dermstore list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
