package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ItemDO;

/**
 * 新浪微博 mongo库操作item
* Title:
* Description:
* Company: 55海淘
* @author denghuan 
* @date 2017年8月21日 下午4:51:57
* @version 1.0
 */
public interface WeiBoItemDAO {

	/**
	 * 批量更新/新增商品信息,微博
	 * 
	 * @param itemList
	 *    商品信息列表
	 */
	public void upsertWeiBoItem(ItemDO itemDO,Long taskId);
	/**
	 * 根据任务id 查询微博item
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<ItemDO> queryWeiBoItems(int page, int pageSize,Long taskId);
}