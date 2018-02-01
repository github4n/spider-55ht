package com.haitao55.spider.crawler.core.callable.custom.asos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.haitao55.spider.crawler.utils.Constants;

public class AsosSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document docment = Jsoup.parse(content);
		Elements es = docment.select(".results ul li.product-container a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url) && 
						StringUtils.containsIgnoreCase(url, "?")){
					String proUrl = StringUtils.substring(url, 0, url.indexOf("?"));
					String cid = pattern(url);
					if(StringUtils.isNotBlank(cid)){
						newUrlValues.add(proUrl+"?"+cid);
					}
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("asos item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(cid=\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
}
