package com.haitao55.spider.common.http;

/**
 * 
 * 功能：异常定义
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午3:07:46
 * @version 1.0
 */
public class HttpException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int status;

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public HttpException(String message) {
        super(message);
    }

    public HttpException(Exception e) {
        super(e);
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}