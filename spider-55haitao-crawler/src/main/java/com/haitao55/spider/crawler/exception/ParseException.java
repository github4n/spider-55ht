package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：解析过程中的异常
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午1:38:21
 * @version 1.0
 */
public class ParseException extends CrawlerException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public ParseException(CrawlerExceptionCode code, String message) {
        super(code, message);
    }
}