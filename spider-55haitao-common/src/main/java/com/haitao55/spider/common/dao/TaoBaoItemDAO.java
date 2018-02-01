package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ItemDO;

/**
 * 淘宝全球购商品 mongo库操作item
* Title:
* Description:
* Company: 55海淘
* @author denghuan 
* @date 2017年3月3日 下午4:51:57
* @version 1.0
 */
public interface TaoBaoItemDAO {

	/**
	 * 批量更新/新增商品信息,淘宝代购
	 * 
	 * @param itemList
	 *    商品信息列表
	 */
	public void upsertTaoBaoDgItem(ItemDO itemDO);

	/**
	 * 根据任务id 查询淘宝全球购商品item,淘宝代购
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<ItemDO> queryTaoBaoDgItems(int page, int pageSize);
	
	/**
	 * 获取商品总数 ,淘宝代购
	 * @param taskId
	 * @return
	 */
	public Long countTaoBaoDgItems();
	
	
	
	/**
	 * 批量更新/新增商品信息,淘宝直购
	 * 
	 * @param itemList
	 *    商品信息列表
	 */
	public void upsertTaoBaoZgItem(ItemDO itemDO);
	
	
	/**
	 * 批量更新/新增商品信息,淘宝商家
	 * 
	 * @param itemList
	 *    商品信息列表
	 */
	public void upsertTaoBaoMerchantItem(ItemDO itemDO);
	

	/**
	 * 根据任务id 查询淘宝全球购商品item,淘宝直购
	 * @param taskId
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<ItemDO> queryTaoBaoZgItems(int page, int pageSize);
	
	/**
	 * 获取商品总数 ,淘宝直购
	 * @param taskId
	 * @return
	 */
	public Long countTaoBaoZgItems();
	
	
}