package com.haitao55.spider.crawler.core.callable;

import java.net.URLEncoder;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.BatchInputOutputCallable;

/**
 * 
 * 功能：提供加密功能
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 下午2:11:33
 * @version 1.0
 */
public class Encoder extends BatchInputOutputCallable {
	private String charset;

	protected Object process(Object input) throws Exception {
		String value = Objects.toString(input, "");
		if (StringUtils.isEmpty(value)) {
			return StringUtils.EMPTY;
		}
		return URLEncoder.encode(value, getCharset());
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}