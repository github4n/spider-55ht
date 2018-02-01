package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：输出结果过程中产生的异常
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 下午3:06:07
 * @version 1.0
 */
public class OutputException extends CrawlerException {

	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public OutputException(CrawlerExceptionCode code, String message) {
		super(code, message);
	}
}