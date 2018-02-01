package com.haitao55.spider.crawler.core.callable.custom.houseoffraser;



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

public class HouseoffraserSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final int PAGE_NAO = 30;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		String attr = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(url, "#")){
			attr = StringUtils.substringAfter(url, "#");
			attr = "#"+attr;
			url = StringUtils.substringBefore(url, "#");
		}
		
		List<String> newUrlValues = new ArrayList<String>();
		
		String totalCount = StringUtils.substringBetween(content, "TotalCount\":", ",");
		if(StringUtils.isNotBlank(totalCount) 
				&& !"0".equals(totalCount)){
			Double page  = Double.valueOf(totalCount) / PAGE_NAO;
			int pageNumber =  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(url +"?page="+i+attr);
			}
		}
		/**
		 * 下面是旧版本
		 */
//		Document doc = Jsoup.parse(content);
//		String paging = doc.select(".now-showing p.paging-items-summary").text();
//		if(StringUtils.isNotBlank(paging)){
//			String pageTotal = pattern(paging);
//			if(StringUtils.isNotBlank(pageTotal) 
//					&& !"0".equals(pageTotal)){
//				Double page  = Double.valueOf(pageTotal) / PAGE_NAO;
//				int pageNumber =  (int)Math.ceil(page);
//				for(int i = 1 ;i < pageNumber; i++){
//					newUrlValues.add(url + attr + "start="+i*PAGE_NAO+"&sz=30&fix&spcl");
//				}
//			}
//		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Houseoffraser list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("of\\s?(\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
}
