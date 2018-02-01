package com.test.md5;

import com.haitao55.spider.common.utils.SpiderStringUtil;

public class MD5Testing {
	public static void main(String... args) {
		String str = "www.6pm.com" + "8772862";
		String docId = SpiderStringUtil.md5Encode(str);
		System.out.println(docId);

	}
}