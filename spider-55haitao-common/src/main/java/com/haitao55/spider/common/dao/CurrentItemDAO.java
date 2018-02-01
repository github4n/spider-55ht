package com.haitao55.spider.common.dao;

import com.haitao55.spider.common.dos.ItemDO;

/**
 * 
 * 功能：用来操作CurrentItem的DAO接口,爬虫爬取商品数据与mongodb商品数据进行比较发生变化的最新一条数据存放CurrentItem表中
 * 
 * @author denghuan
 * @time 2017年2月16日 上午11:07:48
 * @version 1.0
 */
public interface CurrentItemDAO {

	/**
	 * 更新/新增商品信息,表中数据存在进行更新,不存在做增加操作
	 * 
	 * @param taskId
	 *            任务ID
	 * @param itemList
	 *            商品信息列表
	 */
	public void upsertCurrentItems(Long taskId, ItemDO itemDO);
	
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
	 * 通过taskId docId等条件，获取商品表中最近时间一条增加的商品信息数据，进行对比
	 * 
	 * @param taskId
	 *            任务ID
	 * @param docId
	 */
	public ItemDO queryLastItem(Long taskId, String docId);
	
	/**
	 * 
	 * @param taskId
	 * @param md5Urls
	 * @return
	 */
	public ItemDO queryCurrentMD5Urls(Long taskId, String md5Urls);
	
}