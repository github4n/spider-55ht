package com.test.pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能：测试正则表达式替换
 * 
 * @author Arthur.Liu
 * @time 2016年11月16日 上午11:14:51
 * @version 1.0
 */
public class PatternTesting {

	public static void main(String... args) {
		String src = "http://www.rebeccaminkoff.com/clothing/pants-shorts";
		String regex = "$";
		String replacement = "?limit=all";

		// String result = src.replaceAll(regex, replacement);
		String result = StringUtils.replacePattern(src, regex, replacement);
		System.out.println(result);
	}
}