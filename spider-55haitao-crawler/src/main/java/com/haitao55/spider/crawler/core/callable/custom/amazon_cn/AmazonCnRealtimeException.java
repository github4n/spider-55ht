package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

/**
 * 
 * 功能：中亚海外购功能中定义的异常类，为了达到核价Callable与外层的RealtimeCrawlerController之间相互通讯的目的
 * 
 * @author Arthur.Liu
 * @time 2017年11月17日 下午3:03:59
 * @version 1.0
 */
public class AmazonCnRealtimeException extends Exception {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private AmazonCnRealtimeExceptionCode code;
	private String message;

	public AmazonCnRealtimeException(AmazonCnRealtimeExceptionCode code, String message) {
		this.code = code;
		this.message = message;
	}

	public AmazonCnRealtimeExceptionCode getCode() {
		return code;
	}

	public void setCode(AmazonCnRealtimeExceptionCode code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}