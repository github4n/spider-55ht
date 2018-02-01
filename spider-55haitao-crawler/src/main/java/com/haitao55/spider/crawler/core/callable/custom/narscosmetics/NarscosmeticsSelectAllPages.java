package com.haitao55.spider.crawler.core.callable.custom.narscosmetics;




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

public class NarscosmeticsSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String SUFFIX ="?sz=1000&start=0&format=page-element";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		if(StringUtils.isNotBlank(url)){
			newUrlValues.add(url + SUFFIX);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Narscosmetics list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
