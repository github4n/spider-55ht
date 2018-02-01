package com.haitao55.spider.crawling.service.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.crawling.service.service.CheckUrlsService;

/**
 * 
 * @ClassName: CheckAction
 * @Description: 检查url跳转后是否正确
 * @author songsong.xu
 * @date 2017年3月30日 下午5:55:40
 *
 */
@Controller
@RequestMapping("/check")
public class CheckUrlsAction {
	
	private static final Logger logger = LoggerFactory.getLogger(CheckUrlsAction.class);
	@Autowired
	private CheckUrlsService checkUrlsService;
	
	@RequestMapping(path = "/urls", produces = "application/json; charset=utf-8",consumes = "application/json; charset=utf-8")
	public @ResponseBody JsonObject checkUrls(@RequestBody JsonArray param,HttpServletRequest request) {
		String rootDir = request.getSession().getServletContext().getRealPath("/");
		logger.info("checkUrls root dir {}",rootDir);
		return checkUrlsService.checks(param, rootDir);
	}
	
	@RequestMapping(path = "/gets", produces = "application/json; charset=utf-8",consumes = "application/json; charset=utf-8")
	public @ResponseBody JsonArray getUrls(@RequestBody JsonObject param,HttpServletRequest request) {
		String rootDir = request.getSession().getServletContext().getRealPath("/");
		logger.info("getUrls root dir {}",rootDir);
		return checkUrlsService.gets(param, rootDir);
	}
}
