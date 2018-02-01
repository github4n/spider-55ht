package com.haitao55.spider.crawler.core.callable.custom.bloomingdales;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class BloomingdalesSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US","55ht_zone_us");
		String content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		String	id = StringUtils.substringBetween(url, "?", "&");
		String	listUrl = url.substring(0, url.indexOf("?"));
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String pageTotal = doc.select("#productCount span").text();
			
			if(StringUtils.isNotBlank(pageTotal)){
				Double page  = Double.valueOf(pageTotal) / 90;
				int pageNumber=  (int)Math.ceil(page);
				for(int i = 1 ;i <= pageNumber; i++){
					newUrlValues.add(listUrl + "/Pageindex/"+i+"?"+id);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("bloomingdales list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put(":authority", "www.bloomingdales.com");
		headers.put(":method", "GET");
		headers.put(":scheme", "https");
		headers.put("accept-encoding", "gzip, deflate, br");
		return headers;
	}
}
