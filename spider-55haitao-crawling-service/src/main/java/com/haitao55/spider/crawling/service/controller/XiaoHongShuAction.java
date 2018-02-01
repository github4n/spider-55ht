package com.haitao55.spider.crawling.service.controller;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.common.gson.bean.xiaohongshu.XiaoHongShu;
import com.haitao55.spider.crawling.service.pojo.XhsResultView;
import com.haitao55.spider.crawling.service.service.XiaoHongShuService;
import com.haitao55.spider.crawling.service.utils.HttpStatus;

/**
 * 提供小红书对外接口服务接口
 * 爬取小红书帖子信息内容
 * date:2017-3-30
 * @author denghuan
 *
 */
@Controller
@RequestMapping("/xiaohongshu")
public class XiaoHongShuAction {

	@Autowired
	private XiaoHongShuService xiaoHongShuService;
	
	 @RequestMapping(path="/crawlingArticle", produces = "application/json; charset=utf-8")
	 public @ResponseBody String crawlingArticle(String url,HttpServletRequest request, HttpServletResponse response){
		XhsResultView rst = new XhsResultView();
		//封装error格式
		if(StringUtils.isBlank(url)){
			rst.setCode(HttpStatus.HTTP404.getValue());
			rst.setMsg("url is empty");
			return rst.parseTo();
		} 
		
		XiaoHongShu xhs = xiaoHongShuService.crawlItemInfo(url);
		if(!Objects.isNull(xhs)){
			rst.setCode(HttpStatus.HTTP200.getValue());
			rst.setMsg("成功");
			rst.setXiaoHongShu(xhs);
			return rst.parseTo();
		}
		//爬虫解析失败
		rst.setCode(HttpStatus.HTTP500.getValue());
		rst.setMsg("失败");
		
		return rst.parseTo();
	 }
}
