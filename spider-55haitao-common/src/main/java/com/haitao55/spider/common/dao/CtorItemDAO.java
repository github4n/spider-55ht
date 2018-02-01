package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.CtorItemDO;

/**
 * 竞品商品 mongo库操作item
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月21日 下午4:51:57
* @version 1.0
 */
public interface CtorItemDAO {

	/**
	 * 批量更新/新增商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void upsertItem(Long taskId, CtorItemDO itemDO);

	/**
	 * 根据任务id 查询竞争对手商品item
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<CtorItemDO> queryCompartiotorItemsByTaskId(long taskId, int currentDay,int currentHour);

}