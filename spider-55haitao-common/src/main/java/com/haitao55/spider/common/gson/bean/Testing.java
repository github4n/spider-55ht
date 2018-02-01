package com.haitao55.spider.common.gson.bean;

import com.haitao55.spider.common.entity.RTReturnCode;

public class Testing {
	public static void main(String... args) {
		CrawlerJSONResult jsonResult = new CrawlerJSONResult();
		jsonResult.setRetcode(1);
		jsonResult.setRtReturnCode(RTReturnCode.RT_INNER_ERROR.getValue());
		String jsonStr = jsonResult.parseTo();
		
		System.out.println(jsonStr);
	}
}