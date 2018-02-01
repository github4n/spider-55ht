package com.haitao55.spider.ui.service;

import java.util.List;

/**
 * 竞品 service  竞争对手商品抓取
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月23日 上午10:53:02
* @version 1.0
 */
public interface CompartiotorItemsService {

	/**
	 * 根据任务id查询对应竞品
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<String> queryCompartiotorItemsByTaskId(long taskId , int currentDay,int currentHour);
}
