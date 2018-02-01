package com.haitao55.spider.crawler.core.callable.custom.wconcept;






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

public class WconceptSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		if(StringUtils.containsIgnoreCase(url, "?")){
			url = url.substring(0, url.indexOf("?"));
		}
		List<String> newUrlValues = new ArrayList<String>();
		
		String pageTotal = StringUtils.substringBetween(content, "Total", "<");
		pageTotal = pattern(pageTotal);
		if(StringUtils.isNotBlank(pageTotal) && 
				!StringUtils.equals(pageTotal, "0")){
			Double page  = Double.valueOf(pageTotal) / 36;
			int pageNumber=  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url + "?limit=36&p="+i);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Wconcept list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
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
