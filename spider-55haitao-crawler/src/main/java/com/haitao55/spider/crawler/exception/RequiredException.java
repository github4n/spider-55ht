package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：必要字段异常，在检查必要字段缺失时使用
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午1:59:29
 * @version 1.0
 */
public class RequiredException extends ParseException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public RequiredException(CrawlerExceptionCode code, String message) {
        super(code, message);
    }
}