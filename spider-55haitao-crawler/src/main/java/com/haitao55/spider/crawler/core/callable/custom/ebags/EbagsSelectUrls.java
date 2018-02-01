package com.haitao55.spider.crawler.core.callable.custom.ebags;





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

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class EbagsSelectUrls extends SelectUrls{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASE_URL ="http://www.ebags.com";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		JSONObject jsonObject = JSONObject.parseObject(content);
		String item = jsonObject.getString("ItemsRenderedHtmlContent");
		Document docment = Jsoup.parse(item);
		Elements es = docment.select(".listPageItem .listPageItemDisplay .listPageImage a");
		
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				if(StringUtils.isNotBlank(url)){
					url = url.replaceAll(" ", "");
					if(!StringUtils.containsIgnoreCase(url, "ebags.com")){
					    newUrlValues.add(BASE_URL+url.trim());
					}else{
						newUrlValues.add(url);
					}
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Ebags item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}
