package com.haitao55.spider.crawler.core.callable.custom.shopbop;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class ShopBopSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final int PAGE_NAO = 100;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		
		String pageTotal = StringUtils.substringBetween(content, "searchResultCount\">", "</span>");

		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			pageTotal = pattern(pageTotal);
			Double page  = Double.valueOf(pageTotal) / 100;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				newUrlValues.add(url + "?baseIndex="+PAGE_NAO*i+"&view=100");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("superdrug list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
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
