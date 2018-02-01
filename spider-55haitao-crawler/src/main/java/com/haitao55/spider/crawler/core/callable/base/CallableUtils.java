package com.haitao55.spider.crawler.core.callable.base;

import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：Callable 相关utils方法
 * 
 * @author Arthur.Liu
 * @time 2016年8月22日 下午7:51:29
 * @version 1.0
 */
public class CallableUtils {
	public static String parse(Context context, String input) {

		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}

		String[] array = StringUtils.substringsBetween(input, "${", "}");
		if (ArrayUtils.isEmpty(array)) {
			return input;
		}

		for (String key : array) {
			Object value = null;
			if ("now".equalsIgnoreCase(key)) {
				value = now();
			} else {
				value = context.get(key);
				value = (value == null) ? context.get("${" + key + "}") : value;
			}

			if (value == null) {
				continue;
			}
			input = input.replaceFirst("\\$\\{" + key + "\\}", Objects.toString(value, ""));

		}
		return input;
	}

	public static String now() {
		return String.valueOf(System.currentTimeMillis());
	}

	public static String subString(String str, int length) {
		if (StringUtils.isEmpty(str)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.substring(str, 0, length) + ((str.length() > length) ? "..." : StringUtils.EMPTY);
	}
}