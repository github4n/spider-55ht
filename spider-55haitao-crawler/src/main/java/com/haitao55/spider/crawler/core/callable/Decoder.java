package com.haitao55.spider.crawler.core.callable;

import java.net.URLDecoder;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.BatchInputOutputCallable;

/**
 * 
 * 功能：提供解密功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:32:41
 * @version 1.0
 */
public class Decoder extends BatchInputOutputCallable {
	private String charset;

	protected Object process(Object input) throws Exception {
		String value = Objects.toString(input, "");
		if (StringUtils.isEmpty(value)) {
			return StringUtils.EMPTY;
		}
		return URLDecoder.decode(value, getCharset());
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}