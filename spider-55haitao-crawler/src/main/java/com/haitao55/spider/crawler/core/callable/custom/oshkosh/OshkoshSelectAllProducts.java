package com.haitao55.spider.crawler.core.callable.custom.oshkosh;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class OshkoshSelectAllProducts extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		Elements es = document.select("a.thumb-link");
		es.forEach(e -> {
			String url = e.attr("href");
			if(StringUtils.isNotBlank(url)){
				if(StringUtils.containsIgnoreCase(url, "?")){
					url = StringUtils.substringBefore(url, "?");
				}
				newUrlValues.add(url);
			}
		});
		
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info(" Oshkosh item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
