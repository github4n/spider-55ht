package com.haitao55.spider.crawler.core.callable.custom.kidsroom;

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

public class Kidsroomselectallpages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		List<String> newUrlValues = new ArrayList<String>();
		Document document = Jsoup.parse(content);
		Elements page_ret = document.select("div[class=PagerSizeContainer]");
		if(!page_ret.isEmpty()){
			int all_num = 0;
			for(Element p:page_ret.select("span[class=ClickItem]")){
				if(Integer.valueOf(p.text())>all_num){
					all_num = Integer.valueOf(p.text());
				}
				for(int i=2;i<=all_num;i++){
					String url = context.getCurrentUrl()+"&page="+i;
					newUrlValues.add(url);
				}
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Kidsroom item url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}

}
