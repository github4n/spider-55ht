package com.haitao55.spider.realtime.service;

import com.haitao55.spider.realtime.view.ItemView;

/**
 * 
 * 功能：实时抓取(RealtimeCrawler)模块范围内使用的商品服务接口类
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午6:35:57
 * @version 1.0
 */
public interface ItemService {
	
	/**
	 * 根据商品DOCID和SKUID,获取最新一条数据信息
	 * 
	 * @param taskId
	 * @param docId
	 * @param skuId
	 * @return
	 */
	public ItemView getLastItem(Long taskId, String docId, String skuId);

}