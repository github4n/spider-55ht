package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：字符串连接——当不包含时则连接
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:35:15
 * @version 1.0
 */
public class ConcatAbsent extends AbstractCallable {

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

		if (StringUtils.isNotBlank(input) && StringUtils.startsWith(input, prefix)) {
			prefix = StringUtils.EMPTY;
		}
		if (StringUtils.isNotBlank(input) && StringUtils.endsWith(input, suffix)) {
			suffix = StringUtils.EMPTY;
		}

		String output = (prefix == null ? StringUtils.EMPTY : prefix) + input
				+ (suffix == null ? StringUtils.EMPTY : suffix);
		setOutput(context, output);
	}
}