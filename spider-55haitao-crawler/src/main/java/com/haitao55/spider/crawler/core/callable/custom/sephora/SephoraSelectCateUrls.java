package com.haitao55.spider.crawler.core.callable.custom.sephora;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

public class SephoraSelectCateUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String base_url ="https://www.sephora.com";

	@Override
	public void invoke(Context context) throws Exception {
		try {
			String content = super.getInputString(context);
			List<String> newUrlValues = new ArrayList<String>();
			if(StringUtils.isNotBlank(content)){
				
				Pattern p = Pattern.compile("\"targetUrl\":\"(.*?)\",");
				Matcher m = p.matcher(content);
				while(m.find()){
					String url = m.group(1);
					if(!url.contains("https://")){
						if(StringUtils.contains(url, "?")){
							url = url.substring(0, url.indexOf("?"));
						}
						if(!url.contains("\"")){
							newUrlValues.add(base_url+url);
						}
					}
				}
			}
			Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
			context.getUrl().getNewUrls().addAll(value);
		} catch (Exception e) {
			logger.error("Sephora crawling cate url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
}
