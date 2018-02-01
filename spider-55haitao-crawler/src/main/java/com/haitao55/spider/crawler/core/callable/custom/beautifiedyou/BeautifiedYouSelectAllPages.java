package com.haitao55.spider.crawler.core.callable.custom.beautifiedyou;




import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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

public class BeautifiedYouSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private  String BEAUTIFIED_API ="https://www.beautifiedyou.com/catalog?search_query=&page=#page#&limit=27&sort=bestselling&category=#cateId#&is_category_page=1";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		
		String categoryId = StringUtils.substringBetween(content, "base\":", "}");

		if(StringUtils.isNotBlank(categoryId)){
			String byUrl = BEAUTIFIED_API.replace("#cateId#", categoryId).replace("#page#", "1");
			String html = getByApiInterface(context,byUrl);
			String pageTotal = StringUtils.substringBetween(html, "total_count\":", "}");
			if(StringUtils.isNotBlank(pageTotal)){
				Double page  = Double.valueOf(pageTotal) / 27;
				int pageNumber =  (int)Math.ceil(page);
				for(int i = 1 ;i <= pageNumber; i++){
					String cateUrl = BEAUTIFIED_API.replace("#cateId#", categoryId).replace("#page#", String.valueOf(i));
					newUrlValues.add(cateUrl);
				}
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("BeautifiedYou list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
	private String getByApiInterface(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		// headers.put("Referer", "https://www.beautifiedyou.com/acne-products");
		 headers.put("Host", "www.beautifiedyou.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		 headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		return headers;
	}
}
