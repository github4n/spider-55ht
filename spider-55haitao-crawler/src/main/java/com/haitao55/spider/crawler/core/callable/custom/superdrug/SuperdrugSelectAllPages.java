package com.haitao55.spider.crawler.core.callable.custom.superdrug;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class SuperdrugSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		String dataDisplay = StringUtils.substringBetween(content, "class=\"data-display-view-all", "</div>");
		String pageTotal = StringUtils.substringBetween(dataDisplay, "of&nbsp;", "</span");
		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			Double page  = Double.valueOf(pageTotal) / 20;
			int pageNumber=  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				newUrlValues.add(context.getCurrentUrl()+"?page="+i);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("superdrug list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
