package com.haitao55.spider.crawler.core.callable.custom.zappos;

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
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: Zappos
  * @author denghuan
  * @date 2016年10月31日 下午2:19:57
  *
 */
public class ZapposSelectAllPages extends SelectUrls{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String SITE="http://www.zappos.com/";
	
	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = super.getInputString(context);
			String currentUrl = context.getCurrentUrl();
			
			String categoryName= currentUrl.substring(currentUrl.lastIndexOf("/")+1,currentUrl.length());
		    String totalStr = StringUtils.substringBetween(content, "searchHeader", "sortWrap");
		    Pattern p = Pattern.compile("<em[^>]*>(\\d+)</em[^>]*>");
			Matcher m = p.matcher(totalStr);
			List<String> newUrlValues = new ArrayList<String>();
			if(m.find()){
				String pageTotal = m.group(1);
				if(StringUtils.isNotBlank(pageTotal)){
					Double page  = Double.valueOf(pageTotal) / Double.valueOf(100);
					int pageNumber=  (int)Math.ceil(page);
					for(int i = 0 ;i < pageNumber; i++){
						newUrlValues.add(SITE+categoryName+"?p="+i+"&partial=true");
					}
				}
			}
			Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
			context.getUrl().getNewUrls().addAll(value);
		} catch (Exception e) {
			logger.error("zappos crawling list url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
}
