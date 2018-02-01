package com.haitao55.spider.crawler.core.callable.custom.ssense;



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

public class SsenseSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final int PAGE_NAO = 60;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		
		Document doc = Jsoup.parse(content);
		String pageCount = doc.select("li.last-page").text();
		if(StringUtils.isBlank(pageCount)){
			pageCount = "1";
		}
		
		for(int i = 1 ;i <= Integer.parseInt(pageCount); i++){
			newUrlValues.add(url + "/pages/"+i);
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Ssense list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
}
