package com.haitao55.spider.crawler.core.model;

public enum DocType {
	INSERT("INSERT"), DELETE("DELETE"), NOT_SELECT_REQUIRED_PROPERTY("NOT_SELECT_REQUIRED_PROPERTY");

	private String value;

	private DocType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public DocType codeOf(String value) {
		for (DocType val : values()) {
			if (val.getValue().equalsIgnoreCase(value)) {
				return val;
			}
		}

		return null;
	}
}