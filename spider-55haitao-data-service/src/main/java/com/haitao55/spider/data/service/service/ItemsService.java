package com.haitao55.spider.data.service.service;

/**
 * 
 * 功能：商品管理模块的服务层接口
 * 
 * @author denghuan
 * @time 2017年4月19日 下午9:40:32
 * @version 1.0
 */
public interface ItemsService {

	/**
	 * 根据任务ID和URLMD5,查询最新版本商品信息
	 * @param taskId
	 * @param urlMd5
	 * @return
	 */
	public String getLastItemValueByUrlMd5(String taskId, String urlMd5);

	/**
	 * 获取current item 数据
	 * @param taskId
	 * @param docId
	 * @return
	 */
	public String getCurrentItemValueByDocId(String taskId, String docId);
	
	
	/**
	 * 根据任务ID和URLMD5,查询历史商品库Item信息
	 * @param taskId
	 * @param urlMd5
	 * @return
	 */
	public String getHistoryItemByUrlMd5(String taskId, String urlMd5);
	
	/**
	 * 查询urlsMD5
	 * @param taskId
	 * @param md5Urls
	 * @return
	 */
	public String queryCurrentMD5Urls(String taskId, String md5Urls);
	
}