package com.haitao55.spider.crawler.core.callable.custom.mytheresa;



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

public class MytheresaSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		String pages = StringUtils.substringBetween(content, "amount-has-pages\">", "</p>");
		if(StringUtils.isNotBlank(pages)){
			String pageTotal =StringUtils.EMPTY;
			Pattern pattern = Pattern.compile("(\\d+)");
			Matcher matcher = pattern.matcher(pages);
			if(matcher.find()){
				pageTotal = matcher.group(1);
			}
			
			Double page  = Double.valueOf(pageTotal) / 60;
			int pageNumber=  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url + "?p="+i);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("mytheresa list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
