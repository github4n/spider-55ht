package com.haitao55.spider.crawler.core.callable.custom.jimmyjazz;




import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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

public class JimmyjazzSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		//String content = super.getInputString(context);
		
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		String url = context.getCurrentUrl();
		String attr = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(url, "?")){
			attr = "&";
		}else{
			attr = "?";
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		
		String pageTotal = StringUtils.substringBetween(content, "Page 1 of", "</div>");
		if(StringUtils.isNotBlank(pageTotal)){
			if(StringUtils.isNotBlank(pageTotal) 
					&& !"0".equals(pageTotal)){
				pageTotal = pattern(pageTotal);
				for(int i = 1 ;i <= Integer.parseInt(pageTotal); i++){
					newUrlValues.add(url + attr + "page="+i);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Jimmyjazz list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
}
