package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：在抓取之前验证Url合法性和配置完整性过程中产生的异常类型
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午1:33:19
 * @version 1.0
 */
public class CheckException extends CrawlerException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public CheckException(CrawlerExceptionCode code, String message) {
        super(code, message);
    }
}