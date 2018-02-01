package com.haitao55.spider.crawler.core.callable.custom.carters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

public class CartersSelectAllLists extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		Elements cate_nav_ret = document.select("div[id=navigation]");
		for(Element cate_item:cate_nav_ret.select("li[class~=.*topCat")){
			for(Element cate_url_item:cate_item.select("div[class=subnav-categories]")){
				for(Element li:cate_url_item.select("li")){
					if(!li.select("a").isEmpty()&&!li.select("a").attr("href").isEmpty()){
						String url = li.select("a").attr("href").split("\\?")[0]+"?startRow=0&sz=all";
						newUrlValues.add(url);
					}
				}
			}
			
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Carters item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
