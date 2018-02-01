package com.haitao55.spider.crawling.service.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.crawling.service.newcrawler.LookfantasticTask;
import com.haitao55.spider.crawling.service.newcrawler.MacysTask;
import com.haitao55.spider.crawling.service.newcrawler.SixpmTask;
import com.haitao55.spider.crawling.service.newcrawler.ZapposTask;

/**
 * @Description: 通过核价接口抓取新上架的商品数据
 * @author: zhoushuo
 * @date: 2017年5月4日 下午5:34:46
 */
@Controller
@RequestMapping("/newItem")
public class NewCrawlerAction {

	// 6pm新品抓取
	@RequestMapping("sixpmNewCrawler.action")
	@ResponseBody
	public void sixpmNewCrawler() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
		fixedThreadPool.execute(new SixpmTask());
	}

	// zappos新品抓取
	@RequestMapping("zapposNewCrawler.action")
	@ResponseBody
	public void zapposNewCrawler() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
		fixedThreadPool.execute(new ZapposTask());
	}

	// lookfantastic新品抓取
	@RequestMapping("lookfantasticNewCrawler.action")
	@ResponseBody
	public void lookfantasticNewCrawler() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
		fixedThreadPool.execute(new LookfantasticTask());
	}

	// macys新品抓取
	@RequestMapping("macysNewCrawler.action")
	@ResponseBody
	public void macysNewCrawler() {
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
		fixedThreadPool.execute(new MacysTask());
	}
}
