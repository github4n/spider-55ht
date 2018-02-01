package com.haitao55.spider.crawler.core.model;

/**
 * 
 * 功能：结果数据输出方式
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:12:34
 * @version 1.0
 */
public enum OutputChannel {
	FILE("file"), CONTROLLER("controller"),KAFKA("kafka"),MAIL("mail");

	private String value;

	private OutputChannel(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public static OutputChannel codeOf(String value) {
		for (OutputChannel oc : values()) {
			if (oc.getValue().equals(value)) {
				return oc;
			}
		}

		return null;
	}
}