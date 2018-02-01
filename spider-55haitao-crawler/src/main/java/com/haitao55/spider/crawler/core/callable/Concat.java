package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：字符串连接
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:34:41
 * @version 1.0
 */
public class Concat extends AbstractCallable {

	// 前缀
	private String prefix;

	// 后缀
	private String suffix;

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
		String prefix = getPrefix();
		String suffix = getSuffix();
		String output = (prefix == null ? StringUtils.EMPTY : prefix) + input
				+ (suffix == null ? StringUtils.EMPTY : suffix);
		setOutput(context, output);
	}
}