package com.haitao55.spider.crawler.core.callable;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.BatchInputOutputCallable;

/**
 * 
 * 功能：字符串替换支持
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:45:24
 * @version 1.0
 */
public class Replace extends BatchInputOutputCallable {

	private String regex;

	private String replacement;

	@Override
	protected Object process(Object input) throws Exception {
		if (input == null) {
			return StringUtils.EMPTY;
		}

		String regex = getRegex();
		if (regex == null) {
			return input;
		}

		String replacement = getReplacement();
		if (replacement == null) {
			replacement = StringUtils.EMPTY;
		}

		return Objects.toString(input, "").replaceAll(regex, replacement);
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}