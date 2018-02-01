/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: Test.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月27日 下午7:55:40 
 * @version: V1.0   
 */
package com.test.bluefly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @ClassName: Test 
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月27日 下午7:55:40  
 */
public class Test {
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		Map<String, Object> headers=new HashMap<String,Object>();
		JsonObject obj = new JsonObject();
		JsonArray arr  = new JsonArray();
		JsonObject internal = new JsonObject();
		internal.addProperty("attributeFQN", "tenant~dress-size");
		internal.addProperty("value", "42097");
		arr.add(internal);
		obj.add("options", arr);
		
		
//		headers.put("Accept", "application/json");
		headers.put("Content-type", "application/json");
		headers.put("x-vol-app-claims", "K0Fb51pK4PRPjSvd71NmA7Zy9i94m+E0SNjXMvZwtlk+fZuYhfNcJf/iEC2Ite15KzAHj8X2jU2JdNJh120Nh74rLuU9hIMaw0l1sFhRZMuv+rcochHOeMepaJ5IuVZytLKIDjMt/YXmwY7+/3AsMBYOucU452K/VO2iMVCKkbdq/8luVmqnUSO33D4+iF4xy4oUeQ7F3pFEr6WTz28jUgbRNxg1hsWY7J3ZY5brJ89JnBoji/1aFYImNShSC5PdzYZtHG+FB+l2v+P79ELN9A==/IGE0yqUGjrG+x9F+ZWu3zKaD/1102QulyOwGaG86btLHI735zuGUKhVi8AKw7p+jGWbx4QSTi4XDLFbM+ftM1yqXBJL4b4c3z+8H7bGzvmsFE5yBZLSueCR4c6dRmqQOXawIY2tdTy+8Pt8lMUmQq8ZeGTsw+mMkJ5Z/LKVRPoXCA==");
		headers.put("x-vol-user-claims", "wAj3vu0uqMfZAHOAY3jOHDWN4Z7mtt2WFSMNEeQ6whuEeH5TaJ79DCTRxtNosJdQy1inMkxTpmCibL0JAzeKZpHpUvdmWdzAKX9gOsIevmNFgl0TBchbrjU+Ov0W9ykj/znVT/CT18nCNFIdcoHOM0qexR4T/VMDkG4ksTESGMNng2whQwAPumO/CKTwIjC7XX97n5VODNDnlhhVwkrEGZp4xdoBXNtDp7eU5ZyuBGFINOD2ioAalfabsNrKynHzQCB6MMYL3zBHU/r265uyk6Q94k1sv5EmuIwrQPOVzYtM/rOfqSS5ChEbptM27LSRhdd3eLHpPtiVWioaZNxoFibXeSEcm/C0sKH4xCmNfSM=");
		String content = Crawler.create()
		.timeOut(30000)
		.url("http://www.bluefly.com/api/commerce/catalog/storefront/products/404716401/configure?includeOptionDetails=true&quantity=1")
		.retry(2)
		.method("POST")
//		.payload("attributeFQN=tenant~dress-size&value=42095")
//		.payload("options", "attributeFQN=tenant~dress-size&value=42095")
//		.payload(payload)
		.payload(obj.toString())
		.header(headers).resultAsString();
		System.out.println(content);
	}
}
