package com.haitao55.spider.crawler.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.haitao55.spider.crawler.exception.CrawlerException;


/**
 * 
 * 功能：一个Url抓取/解析/输出过程中的运行时监控/跟踪/统计信息
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午5:34:05
 * @version 1.0
 */
public class RuntimeWatcher {
    /**
     * 开始时间
     */
    private long start;
    /**
     * 结束时间
     */
    private long end;
    /**
     * 结果是否完全成功
     */
    private boolean success = true;
    /**
     * 如果有不成功，抛出的异常对象，初始默认值为null
     */
    private CrawlerException exception = null;

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CrawlerException getException() {
        return exception;
    }

    public void setException(CrawlerException exception) {
        this.exception = exception;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}