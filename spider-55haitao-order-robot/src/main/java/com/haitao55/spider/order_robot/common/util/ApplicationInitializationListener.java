package com.haitao55.spider.order_robot.common.util;

import javax.servlet.ServletContextEvent;

import org.springframework.web.context.ContextLoaderListener;

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
	}

	public void contextDestroyed(ServletContextEvent event) {
		super.contextDestroyed(event);
	}
}