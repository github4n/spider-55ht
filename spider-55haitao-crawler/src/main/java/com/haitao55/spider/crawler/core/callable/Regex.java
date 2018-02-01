package com.haitao55.spider.crawler.core.callable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.haitao55.spider.crawler.common.cache.Cache;
import com.haitao55.spider.crawler.common.cache.SimpleCache;
import com.haitao55.spider.crawler.core.callable.base.BatchInputOutputCallable;

/**
 * 
 * 功能：通过正则表达式匹配来解析内容
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:43:55
 * @version 1.0
 */
public class Regex extends BatchInputOutputCallable {

	// cache的默认失效时间是一天
	private static final Cache<String, Pattern> cache = new SimpleCache<String, Pattern>();

	private String pattern;

	private int group;

	protected Object process(Object input) throws Exception {
		String p = getPattern();
		Pattern pattern = cache.get(p);
		// 可能会存在多线程put同一个value的情况，但是没太大影响，忽略
		if (pattern == null) {
			pattern = Pattern.compile(p);
			cache.put(p, pattern);
		}

		String value = Objects.toString(input, "");
		Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			return matcher.group(getGroup());
		}

		return null;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}
}