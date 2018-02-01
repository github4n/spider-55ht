package com.haitao55.spider.crawler.core.callable.custom.vitacost;



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
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class VitacostSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String VITACOST_PAGE_ATTR = "&scrolling=true&No=";
	
	
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
		
		String pageTotal = StringUtils.substringBetween(content, "totalRecords: ", ",");

		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			Double page  = Double.valueOf(pageTotal) / 20;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				String pageListUrl = url+VITACOST_PAGE_ATTR+i*20;
				newUrlValues.add(pageListUrl);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("CoachSelectAllPages list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		//String html = Crawler.create().timeOut(60000).retry(3).proxy(true).proxyAddress("114.55.10.105").proxyPort(3128).url("https://www.coach.com/").resultAsString();
		//String html = CurlCrawlerUtil.get("https://www.vitacost.com/");
		String html = Crawler.create().timeOut(30000).url("https://www.vitacost.com/").method(HttpMethod.GET.getValue())
				.resultAsString();
		Document  doc = Jsoup.parse(html);
		int count = 0;
		Elements es = doc.select("ul#departmentSubNav li.topnav a");
		for(Element e : es){
			count ++;
			String url = e.attr("href");
			System.out.println(url);
		}
		System.out.println(count);
	}
	
}
