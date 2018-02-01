package com.haitao55.spider.crawler.core.callable.custom.oshkosh;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class OshkoshSelectAllList extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		String curl = context.getCurrentUrl();
		String cleaningAftUrl = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(curl, "?")){
			cleaningAftUrl = curl.substring(0, curl.indexOf("?")+1);
		}
		
		Document document = Jsoup.parse(content);
		String itemCount = document.select("span#mini_counter").text();
		if(StringUtils.isNotBlank(itemCount)){
			itemCount = pattern(itemCount);
			Double page  = Double.valueOf(itemCount) / 24;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 0 ;i < pageNumber; i++){
				newUrlValues.add(cleaningAftUrl + "sz=24&start="+i*24+"&infiniteScroll=true&start=");
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Oshkosh item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private  String pattern(String itemCount){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(itemCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
}
