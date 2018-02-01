package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：任务类型枚举类
 * 
 * @author Arthur.Liu
 * @time 2016年6月7日 下午2:15:43
 * @version 1.0
 */
public enum TaskType {
	MANUAL("M"), AUTOMATIC("A");

	private String value;

	public String getValue() {
		return this.value;
	}

	private TaskType(String value) {
		this.value = value;
	}

	public static TaskType codeOf(String value) {
		for (TaskType tt : values()) {
			if (tt.getValue().equals(value)) {
				return tt;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.value;
	}
}