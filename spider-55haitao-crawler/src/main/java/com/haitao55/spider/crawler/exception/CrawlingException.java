package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：抓取过程中产生的异常类型
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午1:35:11
 * @version 1.0
 */
public class CrawlingException extends CrawlerException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public CrawlingException(CrawlerExceptionCode code, String message) {
        super(code, message);
    }
}