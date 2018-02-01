package com.haitao55.spider.realtime.common.util;

import javax.servlet.ServletContextEvent;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import com.haitao55.spider.crawler.utils.SpringUtils;

/**
 * 
 * 功能：ServletContext监听器，用于此web应用程序启动时加载配置文件初始化运行环境，在web应用程序退出时清理运行环境。
 * 现在通过继承自Spring的ContextLoaderListener来实现，可以添加补充的逻辑
 * 
 * @author Arthur.Liu
 * @time 2016年6月19日 上午11:34:57
 * @version 1.0
 */
public class ApplicationInitializationListener extends ContextLoaderListener {
	public void contextInitialized(ServletContextEvent event) {
		super.contextInitialized(event);
		
		ApplicationContext context = (ApplicationContext) event.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		SpringUtils.setContext(context);
	}

	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
	}
}