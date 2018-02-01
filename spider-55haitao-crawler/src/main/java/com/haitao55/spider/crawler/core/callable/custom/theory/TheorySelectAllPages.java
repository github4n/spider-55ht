package com.haitao55.spider.crawler.core.callable.custom.theory;




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

public class TheorySelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String THEORY_API = "?sz={sz}&start={start}&format=page-element";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.POST.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.POST.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		
		
		String pageTotal = StringUtils.substringBetween(content, "Showing", "Results");

		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			Double page  = Double.valueOf(pageTotal.trim()) / 12;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				String attr = THEORY_API.replace("{sz}", String.valueOf(i*12)).replace("{start}", String.valueOf(i*12));
				String urlList = url.concat(attr);
				newUrlValues.add(urlList);
			}
		}else{
			String attr = THEORY_API.replace("{sz}", "0").replace("{start}", "0");
			newUrlValues.add(url.concat(attr));
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("TheorySelectAllPages list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String html = Crawler.create().timeOut(30000).url("http://www.theory.com/").method(HttpMethod.POST.getValue())
				.resultAsString();
		Document  doc = Jsoup.parse(html);
		int count = 0;
		Elements es = doc.select("ul.menu-category a");
		for(Element e : es){
			count ++;
			String url = e.attr("href");
			if(StringUtils.isNotBlank(url)){
				System.out.println(url);
			}
		}
		System.out.println(count);
	}
	
}
