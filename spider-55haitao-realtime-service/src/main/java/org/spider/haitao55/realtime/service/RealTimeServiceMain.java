package org.spider.haitao55.realtime.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;
import org.spider.haitao55.realtime.service.handlers.AbstractHttpHandler;
import org.spider.haitao55.realtime.service.handlers.HtmlHandler;
import org.spider.haitao55.realtime.service.jetty.JettyBootstrap;
import org.spider.haitao55.realtime.service.jetty.JettyBootstrapException;

/**
 * 
  * @ClassName: RealTimeServiceMain
  * @Description: 入口方法
  * @author songsong.xu
  * @date 2017年1月11日 下午6:19:28
  *
 */
public class RealTimeServiceMain {
	
	static {
		PropertyConfigurator.configure("config/log4j.properties");
	}
	
	public static void main(String[] args) throws InterruptedException, JettyBootstrapException {
		Map<String,AbstractHttpHandler> servlets = new HashMap<String,AbstractHttpHandler>();
		//servlets.put("/55haitao/api", new ApiHandler());
		servlets.put("/55haitao/html", new HtmlHandler());
		JettyBootstrap bootstrap = new JettyBootstrap(servlets);
		bootstrap.startServer();
	}
}
