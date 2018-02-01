package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：枚举类型,标识urls数据库中urls的类型
 * 
 * @author Arthur.Liu
 * @time 2016年8月11日 下午4:19:09
 * @version 1.0
 */
public enum UrlsType {
	/**
	 * 链接类型
	 */
	LINK("LINK"),
	/**
	 * 商品类型
	 */
	ITEM("ITEM");

	private String value;

	private UrlsType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static UrlsType codeOf(String value) {
		for (UrlsType ut : values()) {
			if (ut.getValue().equals(value)) {
				return ut;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.value;
	}
}