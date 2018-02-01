package com.haitao55.spider.crawler.core.callable.custom.origins;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class OriginsSelectUrls extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://www.origins.com";

	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Pattern pattern = Pattern.compile("/products/\\d+");
		Matcher matcher = pattern.matcher(content);
		while(matcher.find()){
			String url = BASEURL+matcher.group();
			newUrlValues.add(url);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Toryburch item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}
