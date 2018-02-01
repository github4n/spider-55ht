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
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class ForeoSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL ="https://www.foreo.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document docment = Jsoup.parse(content);
		Elements es = docment.select(".sub-categories ul.links li a");
		
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url) && 
						!StringUtils.containsIgnoreCase(url, "foreo.com")){
					Url currentUrl = new Url(BASE_URL+url);
					currentUrl.setTask(context.getUrl().getTask());
					String html = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					Document proDoc =  Jsoup.parse(html);
					newUrls(proDoc,newUrlValues);
				}
			}
		}else{
			newUrls(docment,newUrlValues);
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Foreo item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
	
	private void newUrls(Document docment,List<String> newUrlValues){
		Elements proEs = docment.select(".view-content .views-row .views-field a");
		for(Element ps :proEs){
			String purl = ps.attr("href");
			if(StringUtils.isNotBlank(purl)  
					&& !StringUtils.containsIgnoreCase(purl, "foreo.com")){
				newUrlValues.add(BASE_URL+purl);
			}
		}
	}
}
