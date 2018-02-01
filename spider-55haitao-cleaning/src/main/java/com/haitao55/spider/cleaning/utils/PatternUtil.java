package com.haitao55.spider.cleaning.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtil {

	private static final String PRICE_REGEX = "(^[0-9]\\d*(\\.\\d{1,4})?$)|(^[0]{1}(\\.\\d{1,4})?$)";
	/**
	 * 提起正则表达式的第一个值
	 * @param regex
	 * @param text
	 * @return
	 */
	public static String   regexMatch(String text){
		Pattern p = Pattern.compile(PRICE_REGEX);
		Matcher m = p.matcher(text);
		if (m.find())
			return m.group();

		return null;
	}
}
