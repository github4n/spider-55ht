package com.haitao55.spider.ui.service;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.ui.view.ItemsView;


/**
 * 
 * 功能：商品管理模块的服务层接口
 * 
 * @author Arthur.Liu
 * @time 2016年11月5日 下午9:40:32
 * @version 1.0
 */
public interface ItemsService {
	/**
	 * 分页查询商品统计信息
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public List<ItemsView> getAllTaskItems(int pageNum, int pageSize);

	/**
	 * 根据任务ID和DOCID,查询最新版本商品信息
	 * 
	 * @param taskId
	 * @param docId
	 * @return
	 */
	public String getLastItemValueByDocId(String taskId, String docId);
	/**
	 * 根据任务ID和URLMD5,查询最新版本商品信息
	 * @param taskId
	 * @param urlMd5
	 * @return
	 */
	public String getLastItemValueByUrlMd5(String taskId, String urlMd5);
	/**
	 * 根据任务ID和URLMD5,查询最历史商品信息
	 * @param taskId
	 * @param urlMd5
	 * @return
	 */
	public List<String> getHistoryItemList(String taskId, String urlMd5);
	
	
	/**
	 * 获取所有items
	 * @param pageSize 
	 * @param page 
	 * @param taskId 
	 * @param urlMd5 
	 * @return
	 */
	public Map<String,Object> getAllItems(String taskId, String urlMd5, int page, int pageSize);

	/**
	 * 获取current item 数据
	 * @param taskId
	 * @param docId
	 * @return
	 */
	public String getCurrentItemValueByDocId(String taskId, String docId);
	
}