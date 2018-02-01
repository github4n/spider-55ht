package com.haitao55.spider.ui.service;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.model.Task;

/**
 * 活动商品查询service，
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月17日 下午2:55:17
* @version 1.0
 */
public interface ActivityItemsService{
	/**
	 * 根据css样式　获取url对应的商品链接，taskId用于从mongo获取存在的docid
	 * @param cssJsonObject
	 * @param taskId
	 * @param url
	 * @param website_preffix 网站前缀　例如：http://zh.ashford.com/　　某些网站获取url时，只有相对地址，需要处理
	 * @param task 
	 * @return
	 */
	JSONObject getItemsAndUrls(JSONObject cssJsonObject,String taskId, String url, String website_preffix, Task task) throws ClientProtocolException, HttpException, IOException ;
}