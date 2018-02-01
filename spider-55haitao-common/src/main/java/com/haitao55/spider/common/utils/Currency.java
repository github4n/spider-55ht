package com.haitao55.spider.common.utils;
/**
 * 
* Title:
* Description: 货币枚举
* Company: 55海淘
* @author zhaoxl 
* @date 2016年10月9日 下午8:38:02
* @version 1.0
 */
public enum Currency {
	USD("$"),
	EUR("€"),
	CNY("¥"),
	GBP("£"),
	HKD("HK$"),
	JPY("J￥"),
	AUD("AU$");
	
	private String value;

	private Currency(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static Currency codeOf(String value) {
		for (Currency us : values()) {
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
	
	public static void main(String[] args) {
		String str="EUR";
//		 String name = Currency.codeOf(str).name();
		String name = Currency.EUR.name();
		 System.out.println(name);
		 
	}
	
}
