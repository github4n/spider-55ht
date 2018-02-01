package com.haitao55.spider.crawler.core.callable.custom.glowing;




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

public class BglowingSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final int PAGE_NAO = 12;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		Document doc = Jsoup.parse(content);
		String paging = doc.select(".toolbar-bottom .show-results-desktop strong").text();
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.isNotBlank(paging)){
			String pageTotal = pattern(paging);
			if(StringUtils.isNotBlank(pageTotal) 
					&& !"0".equals(pageTotal)){
				Double page  = Double.valueOf(pageTotal) / PAGE_NAO;
				int pageNumber =  (int)Math.ceil(page);
				for(int i = 1 ;i <= pageNumber; i++){
					newUrlValues.add(url + "?" + "p="+i+"&infinite=true");
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Bglowing list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
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
