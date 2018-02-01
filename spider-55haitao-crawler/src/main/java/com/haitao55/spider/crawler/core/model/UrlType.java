package com.haitao55.spider.crawler.core.model;

import com.haitao55.spider.common.thrift.UrlTypeModel;

/**
 * 
 * 功能：Url类型的枚举类
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 下午2:02:04
 * @version 1.0
 */
public enum UrlType {
	LINK("LINK"), ITEM("ITEM");

	private String value;

	private UrlType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static UrlType codeOf(String value) {
		for (UrlType urlType : values()) {
			if (urlType.getValue().equals(value)) {
				return urlType;
			}
		}

		return null;
	}

	public static UrlType convertUrlTypeModel2UrlType(UrlTypeModel urlTypeModel) {
		switch (urlTypeModel) {
		case LINK:
			return UrlType.LINK;
		case ITEM:
			return UrlType.ITEM;
		default:
			return null;
		}
	}

	public static UrlTypeModel convertUrlType2UrlTypeModel(UrlType urlType) {
		switch (urlType) {
		case LINK:
			return UrlTypeModel.LINK;
		case ITEM:
			return UrlTypeModel.ITEM;
		default:
			return null;
		}
	}
}