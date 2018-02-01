package com.haitao55.spider.crawling.service.utils;

public enum HttpStatus {
	HTTP404("404"), HTTP500("500"), HTTP200("200");

	private String value;

	private HttpStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public HttpStatus codeOf(String value) {
		for (HttpStatus val : values()) {
			if (val.getValue().equalsIgnoreCase(value)) {
				return val;
			}
		}

		return null;
	}
}