package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：枚举类型,标识urls数据库中urls的状态
 * 
 * @author Arthur.Liu
 * @time 2016年8月10日 下午5:48:22
 * @version 1.0
 */
public enum UrlsStatus {
	/**
	 * 初始化,刚被收录
	 */
	INIT("INIT"),
	/**
	 * 重生,等待处理
	 */
	RELIVE("RELIVE"),
	/**
	 * 正在处理,悬而未决
	 */
	PENDING("PENDING"),
	/**
	 * 处理结束,成功
	 */
	SUCCESS("SUCCESS"),
	/**
	 * 处理结束,错误
	 */
	ERROR("ERROR");

	private String value;

	private UrlsStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static UrlsStatus codeOf(String value) {
		for (UrlsStatus us : values()) {
			if (us.getValue().equals(value)) {
				return us;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.value;
	}
}