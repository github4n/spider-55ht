package com.haitao55.spider.crawler.exception;

/**
 * 
 * 功能：爬虫程序顶级异常类型
 *
 * @author Arthur.Liu
 * @time Aug 4, 2015 7:20:54 PM
 * @version 1.0
 *
 */
public class CrawlerException extends RuntimeException {

    /**
     * default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private CrawlerExceptionCode code;

    public CrawlerException(CrawlerExceptionCode code, String message) {
        super(message);
        this.code = code;
    }

    public CrawlerExceptionCode getCode() {
        return code;
    }

    /**
     * 
     * 功能：异常代码的枚举类型
     *
     * @author Arthur.Liu
     * @time Aug 4, 2015 7:32:51 PM
     * @version 1.0
     *
     */
    public enum CrawlerExceptionCode {
        SUCCESS(0), CHECK_ERROR(1), CRAWLING_ERROR(2), PARSE_ERROR(3), PARSE_ERROR_REQUIRED(
                31),OFFLINE(32), OUTPUT_ERROR(4), UNKNOWN_ERROR(99);

        private int value;

        private CrawlerExceptionCode(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public CrawlerExceptionCode codeOf(int value) {
            for (CrawlerExceptionCode instance : values()) {
                if (instance.getValue() == value) {
                    return instance;
                }
            }

            return null;
        }
    }
}