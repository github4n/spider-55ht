package com.haitao55.spider.crawler.core.callable.custom.foreo;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

public class ForeoSelectCateUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String base_url ="https://www.foreo.com";

	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = super.getInputString(context);
			List<String> newUrlValues = new ArrayList<String>();
			if(StringUtils.isNotBlank(content)){
				Document docment = Jsoup.parse(content);
				Elements es = docment.select(".view-footer p a");
				if(es != null && es.size() > 0){
					for(Element e :es){
						String url = e.attr("href");
						if(StringUtils.isNotBlank(url) && 
								!StringUtils.containsIgnoreCase(url, "foreo.com")){
							newUrlValues.add(base_url+url);
						}
					}
				}
				String linkUrl = StringUtils.substringBetween(content, "class=\"leaf\"><a href=\"", "\"><span class=\"shop-icon");
				
				if(StringUtils.isNotBlank(linkUrl) && 
						!StringUtils.containsIgnoreCase(linkUrl, "foreo.com")){
					newUrlValues.add(base_url+linkUrl);
				}
			}
			Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
			context.getUrl().getNewUrls().addAll(value);
		} catch (Exception e) {
			logger.error("foreo crawling links url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
}
