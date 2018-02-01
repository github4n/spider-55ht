package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ItemDO;

/**
 * 海豚村商品 mongo库操作item
* Title:
* Description:
* Company: 55海淘
* @author denghuan 
* @date 2017年3月3日 下午4:51:57
* @version 1.0
 */
public interface HaiTunCunItemDAO {

	/**
	 * 批量更新/新增商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void upsertItem(ItemDO itemDO);

	/**
	 * 根据任务id 查询海豚村商品item
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<ItemDO> queryCompartiotorItemsByTaskId(int page, int pageSize);
	
	/**
	 * 获取商品总数 
	 * @param taskId
	 * @return
	 */
	public Long countItems();
}