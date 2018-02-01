package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 功能：正则表达式测试
 * 
 * @author Arthur.Liu
 * @time 2017年11月22日 下午8:48:39
 * @version 1.0
 */
public class RegexTestMain {
	public static void main(String... args) {
		String STOCK_PATTERN = "库存中仅剩 (\\d+) 件|现在有货|通常在.*发货";
		String stockStr = "通常在 1 到 2 个工作日内发货";
		// stockStr = "现在有货";
		// stockStr = "库存中仅剩 13 件";

		Pattern pattern = Pattern.compile(STOCK_PATTERN);
		Matcher matcher = pattern.matcher(stockStr);
		if (matcher.find()) {
			String stockNumString = matcher.group(1);
			System.out.println(stockNumString);
		}
	}
}