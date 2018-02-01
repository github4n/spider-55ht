package com.haitao55.spider.cleaning.service;

/**
 * 后处理业务处理类
 * 
 * @author denghuan
 *
 */
public interface ICleaningService {

	/**
	 * 处理一条商品数据
	 * 
	 * @param item
	 *            一条商品数据信息
	 */
	public void handleItem(String item);
}
