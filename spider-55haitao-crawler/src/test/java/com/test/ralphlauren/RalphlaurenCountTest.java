package com.test.ralphlauren;

import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.test.rebeccaminkoff.SelectItemUrls;
import com.test.utils.SelectUrls;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月16日 下午6:49:21  
 */
public class RalphlaurenCountTest extends SelectUrls {
	private String hostUrl = "http://www.ralphlauren.com/home/index.jsp?geos=1";
	
	public static void main(String[] args) throws Exception {
		RalphlaurenCountTest siu = new RalphlaurenCountTest();
		siu.setCss("ul#global-nav>li>a");
		siu.setGrade(1);
		siu.setAttr("abs:href");
		siu.setItemCss("div#left-nav ul.nav-items li a");
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
