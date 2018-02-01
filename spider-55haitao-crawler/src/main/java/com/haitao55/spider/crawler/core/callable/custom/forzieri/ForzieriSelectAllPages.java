package com.haitao55.spider.crawler.core.callable.custom.forzieri;



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

public class ForzieriSelectAllPages extends SelectUrls{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		Document  docment = Jsoup.parse(content);
		
		String page = docment.select(".alsoin span").text();
		
		String pageTotal = StringUtils.EMPTY;
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(page);
		if(matcher.find()){
			pageTotal = matcher.group(1);
		}
		
		if(StringUtils.isNotBlank(pageTotal)){
			Double pageCount  = Double.valueOf(pageTotal) / 96;
			int pageNumber=  (int)Math.ceil(pageCount);
			for(int i = 1 ;i <= pageNumber; i++){
				newUrlValues.add(context.getCurrentUrl()+"/"+i+"-more");
			}
		}else{
			String lastTotal = docment.select("#pagination-top .pagination a:nth-last-of-type(2)").text();
			if(StringUtils.isNotBlank(lastTotal)){
				for(int i = 1;
						i <= Integer.parseInt(lastTotal); i++){
					String listUrl = url+"/"+i;
					newUrlValues.add(listUrl);
				}
			}else{
				newUrlValues.add(url+"/1");
			}
		
		}
		Set<Url> newProductUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newProductUrls);
		logger.info("forzieri list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}
	
}
