package com.haitao55.spider.crawler.core.callable.custom.matchesfashion;




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

public class matchesfashionSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
        String pageTotal = StringUtils.substringBetween(content, "redefine__left__results\">", "results</div>");
       
        if(StringUtils.isNotBlank(pageTotal)){
        	pageTotal = pageTotal.replace(",", "");
			Double page  = Double.valueOf(pageTotal) / 120;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url + "?page="+i+"&noOfRecordsPerPage=120");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("matchesfashion list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
