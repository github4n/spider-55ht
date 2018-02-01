package com.test.rebeccaminkoff;

import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.test.utils.SelectUrls;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月15日 下午6:11:56  
 */
public class SelectItemUrls extends SelectUrls {
	
	private String hostUrl = "http://www.rebeccaminkoff.com";
	
	public static void main(String[] args) throws Exception {
		SelectItemUrls siu = new SelectItemUrls();
		siu.setCss("div.mm-text-links>ul>li>a");
		siu.setGrade(1);
		siu.setAttr("abs:href");
		siu.setRegex("$");
		siu.setReplacement("?limit=all");
		siu.setItemCss("div.product-info h2.product-name>a");
		Context context = new Context();
		context.setCurrentUrl(siu.hostUrl);
		context.setUrl(new Url(){{
			setTask(new Task(){{
				setTaskId(System.currentTimeMillis());
			}});
		}});
		siu.invoke(context);
	}
}
