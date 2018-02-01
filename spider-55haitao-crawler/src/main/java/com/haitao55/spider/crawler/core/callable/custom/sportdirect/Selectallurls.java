package com.haitao55.spider.crawler.core.callable.custom.sportdirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

public class Selectallurls extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
	//	System.out.println(content);
	//	System.out.println(StringUtils.substringBetween(content,"<body>","</body>"));
	//	content = StringUtils.substringBetween(content,"<body>","</body>").replace("&quot;", "'");
		List<String> newUrlValues = new ArrayList<String>();
		JSONObject json = JSONObject.parseObject(content);
		String page_ret = json.getString("PaginationHtml");
		if(!page_ret.isEmpty()){
			Document document = Jsoup.parse(page_ret);
			Elements plist = document.select("a[class=swipeNumberClick]");
			Integer pno = 0;
			if(!plist.isEmpty()){
				pno = Integer.valueOf(plist.get(plist.size()-1).attr("data-dcp"));
			}
			int i =1;
			while(i<=pno){
			   String url =  context.getCurrentUrl().replace("currentPage=1", "currentPage="+String.valueOf(i));
			   newUrlValues.add(url);
			   i++;
			} 		
		}
		else{
			newUrlValues.add(context.getCurrentUrl());
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Sportsdirect item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
