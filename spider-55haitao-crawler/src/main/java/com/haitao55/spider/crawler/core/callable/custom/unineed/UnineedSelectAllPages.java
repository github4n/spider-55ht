package com.haitao55.spider.crawler.core.callable.custom.unineed;

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
import com.haitao55.spider.crawler.utils.HttpUtils;

public class UnineedSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	

	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		String url = context.getCurrentUrl();
		List<String> newUrlValues = new ArrayList<String>();
		Document  doc = Jsoup.parse(content);
//		Elements cates = doc.select(".breadcrumbs ul li a"); 获取分类
//		StringBuffer sb = new StringBuffer();
//		for(int i = 1; i < cates.size(); i++){
//			String cate = cates.get(i).text();
//			if(StringUtils.isNotBlank(cate)){
//				sb.append(cate).append(">");
//			}
//		}
//		if(sb.length() > 0){
//			sb.deleteCharAt(sb.length()-1);
//		}
		Elements es = doc.select(".toolbar .pager .pages");
		String pageCount = StringUtils.EMPTY;
		if(es != null && es.size() >0){
			for(Element e :es){
				String attr = e.select("ol li a:nth-last-child(1)").text();
				if(StringUtils.isNotBlank(attr)){
					String [] page = attr.split(" ");
					if(page != null){
						if(page.length == 1){
							pageCount = page[0];
						}else{
							pageCount = page[page.length-1];
						}
					}
				}
				break;
			}
		}
		if(StringUtils.isNotBlank(pageCount)){
			for(int i = 1 ;i <= Integer.parseInt(pageCount); i++){
				newUrlValues.add(url+"?p="+i);
			}
		}else{
			newUrlValues.add(url);
		}
		List<String> newDetailUrlValues = new ArrayList<String>();
		for(String durl : newUrlValues){
			Url currentUrl = new Url(durl);
			currentUrl.setTask(context.getUrl().getTask());
			String html = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
			Document docment = Jsoup.parse(html);
			Elements  elements = docment.select(".category-products ul.products-grid li.item .product-image a");
			for(Element e : elements){
				String deatilUrl = e.attr("href");
				if(StringUtils.isNotBlank(deatilUrl)){
					newDetailUrlValues.add(deatilUrl);
				}
			}
		}
		
		Set<Url> newUrls = this.buildNewUrls(newDetailUrlValues, context, UrlType.ITEM.getValue(), 2);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("Unineed list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
	}
}

