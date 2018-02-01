package com.haitao55.spider.crawler.core.callable.custom.weibo;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class WeiBoCates  extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		
		for(int i = 0; i <= 50; i++){
			String searchUrl = url+"&page="+i;
			newUrlValues.add(searchUrl);
		}
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("WeiBoCates cates url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	public static void main(String[] args) {
		
		for(int i = 1; i <= 50; i++){
			String searchUrl = "http://s.weibo.com/weibo/shopbop&page="+i;
			System.out.println(searchUrl);
		}
	}
}
