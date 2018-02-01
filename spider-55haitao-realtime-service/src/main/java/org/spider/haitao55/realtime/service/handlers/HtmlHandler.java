package org.spider.haitao55.realtime.service.handlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spider.haitao55.realtime.service.crawler.AmazonHtmlApiCrawler;


public class HtmlHandler extends AbstractHttpHandler {

	private static final long serialVersionUID = 9136981230093758506L;
	private final static Logger logger = LoggerFactory.getLogger(HtmlHandler.class);
	
	public HtmlHandler() {}
	
	@Override
	public void initialize() {
	}
	@Override
	public void doTask(HttpServletRequest request, HttpServletResponse response) {
		logger.info("request start ......");
		long start = System.currentTimeMillis();
		String url = request.getParameter("url");
		AmazonHtmlApiCrawler htmlApiCrawler = new AmazonHtmlApiCrawler(getPool());
		String result = htmlApiCrawler.nordstromByBrowser(url);
		output(result, response);
		logger.info("request end ......");
		long end = System.currentTimeMillis();
		logger.info(" url {} by browers and api ,total time : {}", url, (end-start));
	}

	@Override
	public void finalize() {
		
	}
}
