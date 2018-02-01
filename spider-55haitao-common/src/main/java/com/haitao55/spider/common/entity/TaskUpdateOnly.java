package com.haitao55.spider.common.entity;

/**
 * 
 * 功能：枚举类型,用来指示任务是否只做更新
 * 
 * @author Arthur.Liu
 * @time 2016年7月21日 下午5:48:08
 * @version 1.0
 */
public enum TaskUpdateOnly {
	YES("Y"), NO("N");

	private String value;

	public String getValue() {
		return this.value;
	}

	private TaskUpdateOnly(String value) {
		this.value = value;
	}

	public static TaskUpdateOnly codeOf(String value) {
		for (TaskUpdateOnly tt : values()) {
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