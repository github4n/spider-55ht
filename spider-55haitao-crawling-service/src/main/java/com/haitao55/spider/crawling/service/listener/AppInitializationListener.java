package com.haitao55.spider.crawling.service.listener;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;

/**
 * 功能：ServletContext监听器，用于此web应用程序启动时加载配置文件初始化运行环境，在web应用程序退出时清理运行环境。
 * 现在通过继承自Spring的ContextLoaderListener来实现，可以添加补充的逻辑
* Company: 55海淘
* @author denghuan 
* @date 2017年3月23日 下午3:13:02
* @version 1.0
 */
public class AppInitializationListener extends ContextLoaderListener {
	public void contextInitialized(ServletContextEvent arg0) {
		super.contextInitialized(arg0);
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		super.contextDestroyed(arg0);
	}
}