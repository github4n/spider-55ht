package com.haitao55.spider.ui.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.service.impl.RedisService;

/**
 * 用作接口,提供se 使用,接收55海淘 搜索或者首页url
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月9日 下午1:53:29
* @version 1.0
 */
@Controller
@RequestMapping("focus")
public class FocusItemsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(FocusItemsAction.class);
	private static final String HOME_KEY = "home";
	private static final String SEARCH_KEY = "search";
	private static final long SEARCH_KEY_LENGTH = 100;
	@Autowired
	private RedisService  redisService;
	/**
	 * 接收首页商品urls
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("homeItems")
	public void homeItems(HttpServletRequest request,HttpServletResponse response,Model model){
		String urls = request.getParameter("urls");
		if(StringUtils.isBlank(urls)){
			urls = (String) request.getAttribute("urls");
		}
		if(StringUtils.isBlank(urls)){
			logger.error("FocusItemsAction homeItems request urls is empty");
			this.writeResponse(response, "Error:urls is empty");
			return;
		}
		try {
			if(StringUtils.isNotBlank(urls)){
				JSONObject urlsJSONObject = JSONObject.parseObject(urls);
				JSONArray urlsJSONArray = urlsJSONObject.getJSONArray("urls");
				//设置 HOME_KEY 过期时间
				if(null==redisService.llen(HOME_KEY) ||redisService.llen(HOME_KEY) == 0){
					//设置一天有效期
					redisService.expire(HOME_KEY, 60*60*24);
				}
				if(null != urlsJSONArray && urlsJSONArray.size() > 0){
					String [] strs = new String[urlsJSONArray.size()];
					for (int i=0;i<urlsJSONArray.size();i++) {
						String url = (String)urlsJSONArray.get(i);
						strs[i] = url;
					}
					redisService.sadd(HOME_KEY, strs);
				}
			}
			success_return_package(response,HOME_KEY);
		} catch (Exception e) {
			logger.error("FocusItemsAction homeItems redis sadd exception");
			exception_return_package(response,HOME_KEY);
		}
		
	}
	
	/**
	 * 错误结果封装返回
	 * @param response 
	 * @param key 
	 */
	private void exception_return_package(HttpServletResponse response, String key) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "error");
		jsonObject.put("message", "redis存储urls失败");
		jsonObject.put("from", key);
		writeResponse(response, jsonObject.toJSONString());
	}
	
	/**
	 * 正确结果返回
	 * @param response 
	 * @param homeKey 
	 */
	private void success_return_package(HttpServletResponse response, String key) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("status", "success");
		jsonObject.put("message", "redis存储urls成功");
		jsonObject.put("from", key);
		writeResponse(response, jsonObject.toJSONString());
	}

	/**
	 * 接收搜索之后展示的商品urls
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping("searchItems")
	public void searchItems(HttpServletRequest request,HttpServletResponse response,Model model){
		String urls = request.getParameter("urls");
		if(StringUtils.isBlank(urls)){
			urls = (String) request.getAttribute("urls");
		}
		if(StringUtils.isBlank(urls)){
			logger.error("FocusItemsAction searchItems request urls is empty");
			this.writeResponse(response, "Error:urls is empty");
			return;
		}
		try{
			if(StringUtils.isNotBlank(urls)){
				JSONObject urlsJSONObject = JSONObject.parseObject(urls);
				JSONArray urlsJSONArray = urlsJSONObject.getJSONArray("urls");
	
				if(null != urlsJSONArray && urlsJSONArray.size() > 0){
					//SEARCH_KEY 队列长度定长100
					if(null != redisService.llen(SEARCH_KEY)){
						if(redisService.llen(SEARCH_KEY)+urlsJSONArray.size()>SEARCH_KEY_LENGTH){
							long beyond = redisService.llen(SEARCH_KEY)+urlsJSONArray.size()-SEARCH_KEY_LENGTH;
							if(beyond > 0){
								for (int i = 0; i< beyond;i++) {
									//移除队列末尾元素
									redisService.rpop(SEARCH_KEY);
								}
							}
						}
					}
					for (Object object : urlsJSONArray) {
						String url = (String)object;
						redisService.lpush(SEARCH_KEY, url);
					}
				}
			}
			success_return_package(response,SEARCH_KEY);
		} catch (Exception e) {
			logger.error("FocusItemsAction searchItems redis sadd exception");
			exception_return_package(response,SEARCH_KEY);
		}
	}
	
	private void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
}
