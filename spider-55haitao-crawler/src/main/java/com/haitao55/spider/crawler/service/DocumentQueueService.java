package com.haitao55.spider.crawler.service;

import java.util.List;

import com.haitao55.spider.crawler.core.model.Url;

/**
 * 
 * 功能：用于处理document queue相关操作，包括获取doc，包装给应用使用等
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午10:20:20
 * @version 1.0
 */
public interface DocumentQueueService {

    /**
     * 从本地缓存队列里获取一些待抓取的Urls，使用默认数量
     * 
     * @return
     */
    public List<Url> getUrls();

    /**
     * 从本地缓存队列里获取一些待抓取的Urls，使用指定数量
     * 
     * @param maxCount
     * @return
     */
    public List<Url> getUrls(int maxCount);

}