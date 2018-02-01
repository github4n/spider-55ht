package com.haitao55.spider.crawler.core.callable.custom.onlineshoes;



import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class OnlineshoesSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		if(StringUtils.containsIgnoreCase(url, "/1/60/")){
			url = url.substring(0, url.indexOf("/1/60/"));
		}
		List<String> newUrlValues = new ArrayList<String>();
		Document doc = Jsoup.parse(content);
		String pageTotal = doc.select("#productListCountEx b").text();

		if(StringUtils.isNotBlank(pageTotal)){
			Double page  = Double.valueOf(pageTotal) / 120;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url + "/"+i+"/120");
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Onlineshoes list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
