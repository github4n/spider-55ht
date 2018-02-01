package com.haitao55.spider.crawler.core.callable;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：字符串连接——通过Key从context中获取值再连接
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:35:34
 * @version 1.0
 */
public class ConcatByKey extends AbstractCallable {

	// 前缀
	private String prefix = StringUtils.EMPTY;

	// 后缀
	private String suffix = StringUtils.EMPTY;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	@Override
	public void invoke(Context context) throws Exception {
		String input = getInputString(context);
		String prefix = Objects.toString(context.get(getPrefix()), "");
		String suffix = Objects.toString(context.get(getSuffix()), "");
		String output = (prefix == null ? StringUtils.EMPTY : prefix) + input
				+ (suffix == null ? StringUtils.EMPTY : suffix);
		setOutput(context, output);
	}
}