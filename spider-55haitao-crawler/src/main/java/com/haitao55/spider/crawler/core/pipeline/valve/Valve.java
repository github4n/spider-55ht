package com.haitao55.spider.crawler.core.pipeline.valve;

/**
 * 
 * 功能：Valve 接口定义
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午9:57:09
 * @version 1.0
 */
public interface Valve {

    public static final String SEPARATOR_INFO_FIELDS = "###";

    /**
     * @return 本Valve实例的描述信息
     */
    public String getInfo();

    /**
     * 执行本Valve的实现罗辑
     * 
     * @throws Exception
     *             实现罗辑执行过程中的任何异常
     */
    public void invoke() throws Exception;
}