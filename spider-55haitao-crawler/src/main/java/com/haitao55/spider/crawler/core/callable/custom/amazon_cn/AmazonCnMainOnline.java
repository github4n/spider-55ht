package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.io.IOUtils;

import com.haitao55.spider.common.utils.HttpClientUtil;

/**
 * 
 * 功能：用来测试编码url并发送http请求
 * 
 * @author Arthur.Liu
 * @time 2017年11月17日 下午8:41:09
 * @version 1.0
 */
public class AmazonCnMainOnline {
	public static void main(String... args) throws IOException, InterruptedException {
		String path = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";
		String url = "https://www.amazon.cn/Withings-Pulse-O2-%E8%BF%90%E5%8A%A8%E6%99%BA%E8%83%BD%E6%89%8B%E7%8E%AF-%E7%9D%A1%E7%9C%A0%E8%BF%BD%E8%B8%AA-%E5%BF%83%E7%8E%87%E8%AE%A1-%E8%AE%A1%E6%AD%A5%E5%99%A8-%E9%BB%91%E8%89%B2/dp/B00LF4BCFW/ref=lp_1323500071_1_1?s=music-players&ie=UTF8&qid=1511183536&sr=1-1";
		url = "https://www.amazon.cn/Aerosoles-%E5%A5%B3%E5%BC%8F%E9%80%82%E7%94%A8%E4%BA%8E-SHORE-Black-Snake-5-5-B-US/dp/B01HC6QTCC/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1511230647&sr=1-12";
		
		// url = "https://www.amazon.cn/gp/product/B016R92B3Q?pf_rd_p=c6d17c3b-92ef-4aa7-920b-19155bc9b830&pf_rd_s=merchandised-search-7&pf_rd_t=101&pf_rd_i=1403206071&pf_rd_m=A1AJ19PSB66TGU&pf_rd_r=JW8TZWRMF065Y609NCQK&ref=cn_ags_floor_hotasin_1403206071_mobile-2&th=1";
		url = "https://www.amazon.cn/gp/product/B000NY1VLU?pf_rd_p=c6d17c3b-92ef-4aa7-920b-19155bc9b830&pf_rd_s=merchandised-search-7&pf_rd_t=101&pf_rd_i=1403206071&pf_rd_m=A1AJ19PSB66TGU&pf_rd_r=YA6T3T5WX654FRRAJK2E&ref=cn_ags_floor_hotasin_1403206071_mobile-1";
		// url = "https://www.amazon.cn/Sam-Edelman-%E5%A5%B3-Hazel-%E9%AB%98%E8%B7%9F%E9%9E%8B-E5638LJ254-%E7%BB%8F%E5%85%B8%E8%A3%B8%E8%89%B2-35/dp/B01K0Y10CM/ref=sr_1_1?s=amazon-global-store&ie=UTF8&qid=1511264636&sr=1-1&dpID=41LH%252BbhM29L&preST=_SX395_QL70_&dpSrc=srch";
		// url = "https://www.amazon.cn/Skechers-Sport-Men-s-Vigor-2-0-Trait-Memory-Foam-Sneaker-Black-9-5-XW-US/dp/B00FF57I8M/ref=sr_1_1?s=amazon-global-store&ie=UTF8&qid=1511264718&sr=1-1&th=1&psc=1";
		url = "https://www.amazon.cn/dp/B01M1OJNUU";

		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);

		long start = System.currentTimeMillis();
		String jsonResult = HttpClientUtil.post(path, params);
		long end = System.currentTimeMillis();
		long interval = (end - start) / 1000;
		
		System.out.println(interval);
		System.out.println(jsonResult);
	}
}