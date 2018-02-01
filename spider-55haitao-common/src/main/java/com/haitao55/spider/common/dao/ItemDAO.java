package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.ItemDO;

/**
 * 
 * 功能：用来操作item的DAO接口
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:07:48
 * @version 1.0
 */
public interface ItemDAO {
	/**
	 * 新增一条商品数据
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemDO
	 *            商品信息
	 */
	public void insertItem(Long taskId, ItemDO itemDO);
	
	/**
	 * mongodb库索引缓存，新增商品
	 * @param taskId
	 * @param itemDO
	 */
	public void insertItemEnsureIndex(Long taskId, ItemDO itemDO);

	/**
	 * 批量新增商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void insertItems(Long taskId, List<ItemDO> itemDOList);

	/**
	 * 批量更新商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void updateItems(Long taskId, List<ItemDO> itemList);

	/**
	 * 批量更新/新增商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void upsertItems(Long taskId, List<ItemDO> itemList);

	/**
	 * 批量查询商品信息
	 * 
	 * @param taskId
	 *            任务ID
	 * @param limit
	 *            查询数量限制
	 * @return 商品信息列表
	 */
	public List<ItemDO> queryItems(Long taskId, int limit);

	/**
	 * 
	 * 通过taskId docId等条件，获取商品表中最近时间一条增加的商品信息数据，进行对比
	 * 
	 * @param taskId
	 *            任务ID
	 * @param docId
	 */
	public ItemDO queryLastItem(Long taskId, String docId);
	
	
	/**
	 * 
	 * 通过taskId md5Url等条件，获取商品表中最近时间一条增加的商品信息数据，进行对比
	 * 
	 * @param taskId
	 *            任务ID
	 * @param md5Url
	 */
	
	public ItemDO queryMd5UrlLastItem(Long taskId, String md5Url);
	

	/**
	 * 
	 * 通过taskId md5Url等条件，获取商品列表
	 * 
	 * @param taskId
	 *            任务ID
	 * @param md5Url
	 */
	
	public List<ItemDO> queryMd5UrlList(Long taskId, String md5Url,int count,int limit);
	
	/**
	 * 查询一个集合中文档总量
	 * 
	 * @param taskId
	 *            任务ID
	 * @return 文档总数
	 */
	public long count(Long taskId);
	
	/**
	 * 查询所有商品数
	 * @param taskId
	 * @return
	 */
	public long countItems(Long taskId);
	/**
	 * 查询所有有效商品数
	 * @param taskId
	 * @return
	 */
	public long countOnlineItems(Long taskId);
	
	/**
	 * 查询所有商品数 历史商品查询功能
	 * @param urlMD5
	 * @return
	 */
	public long countUrlMD5Items(Long taskId,String urlMD5);
	
	
	/**
	 * 判断集合表是否存在
	 */
	public boolean checkCollecionIsExists(Long taskId);
	
	/**
	 * 查询商品是否存在,
	 * @param taskId
	 * @param urlMD5
	 * @return
	 */
	public boolean  queryMd5UrlIsexist(Long taskId, String md5Url);
}