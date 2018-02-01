package com.haitao55.spider.crawler.core.callable.custom.allbeauty;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class AllbeautySelectAllPages extends SelectUrls{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		String currentPageSize = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(url, "?")){
			currentPageSize = StringUtils.substringAfterLast(url, "page=");
			url = url.substring(0,url.indexOf("?"));
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		Document  docment = Jsoup.parse(content);
		
		//String firstTotal = docment.select(".crumbHolder p.paging a:nth-child(2)").text();
		
		String lastTotal = docment.select(".crumbHolder p.paging a:nth-last-of-type(2)").text();
		
		if(StringUtils.isNotBlank(lastTotal)){
			if(StringUtils.containsIgnoreCase(lastTotal, "Previous")){
				lastTotal = "2";
			}
			if(StringUtils.isNotBlank(currentPageSize)){
				for(int i = Integer.parseInt(currentPageSize);
						i < Integer.parseInt(lastTotal); i++){
					
					String listUrl = url+"?sort=4&page="+i;
					newUrlValues.add(listUrl);
				}
			}else{
				for(int i = 1; i < Integer.parseInt(lastTotal); i++){
					String listUrl = url+"?sort=4&page="+i;
					newUrlValues.add(listUrl);
				}
			}
			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(), 1);
			context.getUrl().getNewUrls().addAll(newUrls);
		}else{
			newUrlValues.add(context.getCurrentUrl());
		}
		
		List<String> newProductUrlValues = new ArrayList<String>();
		
		if(CollectionUtils.isNotEmpty(newUrlValues)){
			for(String value : newUrlValues){
				Url currentUrl = new Url(value);
				currentUrl.setTask(context.getUrl().getTask());
				String result = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
				Document doc = Jsoup.parse(result);
				Elements es = doc.select(".productContainer .product a");
				for(Element e : es){
					String productUrl = e.attr("href");
					if(StringUtils.isNotBlank(productUrl)){
						newProductUrlValues.add(productUrl);
					}
				}
			}
			Set<Url> newProductUrls = this.buildNewUrls(newProductUrlValues, context, type, grade);
			context.getUrl().getNewUrls().addAll(newProductUrls);
			logger.info("allbeauty list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		}
		
	}

}
