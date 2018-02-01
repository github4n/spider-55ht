package com.haitao55.spider.crawler.core.callable.custom.lastcall;




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

public class LastCallSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		
		String pageTotal = StringUtils.substringBetween(content, "category_results\":\"", "\"");

		if(StringUtils.isNotBlank(pageTotal) 
				&& !"0".equals(pageTotal)){
			Double page  = Double.valueOf(pageTotal) / 30;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url + "#page="+i+"&pageSize=30&rwd=true&sort=PCS_SORT");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("LastCall list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
