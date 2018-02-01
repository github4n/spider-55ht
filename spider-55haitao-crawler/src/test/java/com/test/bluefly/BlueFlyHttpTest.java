/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: BlueFlyHttpTest.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月25日 下午4:24:47 
 * @version: V1.0   
 */
package com.test.bluefly;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/** 
 * @ClassName: BlueFlyHttpTest 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月25日 下午4:24:47  
 */
public class BlueFlyHttpTest extends AbstractSelectUrls{
	
	public static void main(String[] args) {
		BlueFlyHttpTest bf = new BlueFlyHttpTest();
		Context context = new Context();
		context.setCurrentUrl("http://www.bluefly.com/women");
		String content = HttpUtils.get(context.getCurrentUrl(), 30000, 1, null);
		context.setCurrentHtml(content);
		context.setUrl(new Url("http://www.bluefly.com/women"));
		try {
			bf.invoke(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		try {
			if(StringUtils.isNotBlank(context.getCurrentHtml())){
				Document document = Jsoup.parse(context.getCurrentHtml(), context.getCurrentUrl());
				Elements elements = document.select("div.mz-productlisting-info > a");
				for(Element e : elements){
					System.out.println(e.attr("abs:href"));
				}
				if(CollectionUtils.isNotEmpty(elements)){
					List<String> firstUrlValues = JsoupUtils.attrs(elements, "abs:href");

					Set<Url> newUrls = this.buildNewUrls(firstUrlValues, context, "ITEM", 2);

					context.getUrl().getNewUrls().addAll(newUrls);
				}
			}
		} catch (Throwable e) {
			System.err.println("抓取过程中出现异常");
			e.printStackTrace();
		}
		
	}
}
