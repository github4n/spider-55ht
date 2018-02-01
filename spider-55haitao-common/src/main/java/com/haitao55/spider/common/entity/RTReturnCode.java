package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：实时核价返回代码枚举类
 * 
 * @author Arthur.Liu
 * @time 2017年9月22日 下午7:19:32
 * @version 1.0
 */
public enum RTReturnCode {
	/** 成功 */
	RT_SUCCESS(0),
	/** 商品缺库存 */
	RT_STOCK_NONE(1),
	/** 商品已下架 */
	RT_ITEM_OFFLINE(2),
	/** 核价超时 */
	RT_CRAWLING_TIMEOUT(3),
	/** 缺少核价URL */
	RT_URL_NONE(4),
	/** 非法的核价URL */
	RT_URL_ILLEGAL(5),
	/** 不支持的商家网站 */
	RT_WEBSITE_NOT_SUPPORT(6),
	/** 核价异常 */
	RT_INNER_ERROR(7),
	/** 未选择必要属性（暂时针对中亚的核价抓取） */
	NOT_SELECT_REQUIRED_PROPERTY(8);

	private int value;

	private RTReturnCode(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}