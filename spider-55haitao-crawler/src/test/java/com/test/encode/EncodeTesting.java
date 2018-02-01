package com.test.encode;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能：URL编码测试
 * 
 * @author Arthur.Liu
 * @time 2016年11月19日 下午12:43:40
 * @version 1.0
 */
public class EncodeTesting {
	public static void main(String... args) throws IOException {
		String[] specialChars = new String[] { "&", "\"", "'", "+", "<", ">" };

		String url = "http://shop.samsonite.com/luggage/carry-on-luggage/samsonite-freeform-21\"-spinner/78255XXXX.html?cgidmaster=lugaz-lu050&dwvar_78255XXXX_color=Black";

		System.out.println(url);

		for (String specialChar : specialChars) {
			url = StringUtils.replace(url, specialChar, URLEncoder.encode(specialChar, "UTF-8"));
		}

		System.out.println(url);

		for (String specialChar : specialChars) {
			url = StringUtils.replace(url, URLEncoder.encode(specialChar, "UTF-8"), specialChar);
		}

		System.out.println(url);
	}
}