package com.test.pm6;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 
 * 功能：正则表达式测试
 * 
 * @author Arthur.Liu
 * @time 2016年9月6日 下午8:58:26
 * @version 1.0
 */
public class Regex6pmTesting {

	public static void main(String... args) {
		@SuppressWarnings("serial")
		List<String> regexList = new ArrayList<String>() {
			{
				add("^http://www.6pm.com$");
				add("^(?!http://www.6pm.com.*-page).*recentSalesStyle/desc(|/)$");
				add("^http://www.6pm.com/.*-page\\d+/.*recentSalesStyle/desc/$");
				add("^http://www.6pm.com/[A-Za-z0-9]+-[A-Za-z0-9]+-[A-Za-z0-9]+-[A-Za-z0-9]+-[A-Za-z0-9]+-.*$");
			}
		};
		@SuppressWarnings("serial")
		List<String> urlList = new ArrayList<String>() {
			{
				add("http://www.6pm.com");
				add("http://www.6pm.com/women-jeans~d?s=isNew/desc/goLiveDate/desc/recentSalesStyle/desc/");
				add("http://www.6pm.com/men-clothing-page2/CKvXAcABAuICAhgB.zso?p=1&s=isNew/desc/goLiveDate/desc/recentSalesStyle/desc/");
				add("http://www.6pm.com/mavi-jeans-aubrey-harem-in-indigo-tencel-indigo-tencel");
			}
		};

		for (String regex : regexList) {
			for (String url : urlList) {
				Pattern pattern = Pattern.compile(regex);
				boolean result = pattern.matcher(url).matches();
				System.out.println("result== " + result);
			}
			System.out.println("");
		}
	}
}