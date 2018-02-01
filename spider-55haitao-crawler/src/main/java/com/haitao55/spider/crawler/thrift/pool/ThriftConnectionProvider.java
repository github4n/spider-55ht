package com.haitao55.spider.crawler.thrift.pool;

import com.haitao55.spider.common.thrift.ThriftService.Client;

/**
 * 
 * 功能：对象池接口
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:17:38
 * @version 1.0
 */
public interface ThriftConnectionProvider {	
    /**
     * 获取缓存池中的一个对象
     * 
     * @return
     */
    public Client getObject();

    /**
     * 作废一个对象
     * 
     * @param obj
     */
    public void invalidateObject(Client obj);

    /**
     * 返回对象
     * 
     * @param obj
     */
    public void returnObject(Client obj);
}