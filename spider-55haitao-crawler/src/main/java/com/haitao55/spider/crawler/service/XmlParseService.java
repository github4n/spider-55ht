package com.haitao55.spider.crawler.service;

import java.io.File;

/**
 * 
 * 功能：xml 解析服务，用于解析rule config文件
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:04:31
 * @version 1.0
 */
public interface XmlParseService {
    /**
     * 解析任务配置文件
     * 
     * @param doc
     *            Xml格式的任务配置文件内容
     * @return
     */
    public <T> T parse(String doc) throws Exception;

    /**
     * 解析任务配置文件
     * 
     * @param file
     *            Xml文件
     * @return
     */
    public <T> T parse(File file) throws Exception;

}