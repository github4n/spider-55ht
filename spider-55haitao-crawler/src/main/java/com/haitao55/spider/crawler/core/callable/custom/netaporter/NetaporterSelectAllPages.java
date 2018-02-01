package com.haitao55.spider.crawler.core.callable.custom.netaporter;





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

public class NetaporterSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private  String NET_ATTR ="pn=#page#&npp=60&image_view=product&dScroll=0";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		
		if(StringUtils.containsIgnoreCase(url, "?")){
			url = url.substring(0, url.indexOf("?"));
		}
		
		String pageTotal = StringUtils.substringBetween(content, "total-number-of-products\">", "<");
		pageTotal = pageTotal.replaceAll("[,]", "");
		if(StringUtils.isNotBlank(pageTotal) && 
				!StringUtils.equals(pageTotal, "0")){
			Double page  = Double.valueOf(pageTotal) / 60;
			int pageNumber=  (int)Math.ceil(page);
			for(int i = 1 ;i <= pageNumber; i++){
				String attr = NET_ATTR.replace("#page#", String.valueOf(i));
				newUrlValues.add(url + "?" + attr);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Netaporter list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
