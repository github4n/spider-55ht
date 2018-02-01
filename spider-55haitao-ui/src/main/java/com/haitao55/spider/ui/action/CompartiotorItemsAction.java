package com.haitao55.spider.ui.action;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.crawler.common.cache.TaskDOCache;
import com.haitao55.spider.ui.service.CompartiotorItemsService;

/**
 * 竞品 接口提供 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年2月23日 上午10:49:54
 * @version 1.0
 */
@Controller
@RequestMapping("compartiotor")
public class CompartiotorItemsAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(CompartiotorItemsAction.class);
	
	@Autowired
	private CompartiotorItemsService compartiotorItemsService;
	
	@RequestMapping("queryItems")
//	@ResponseBody
	public void queryCompartiotorItems(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("domain") String domain) {
		String currentDayAttr = request.getParameter("currentDay");
		String currentHourAttr = request.getParameter("currentHour");
		int currentDay = 0;
		int currentHour = 0;
		if(StringUtils.isNotBlank(currentDayAttr)){
			currentDay = Integer.parseInt(currentDayAttr);
		}
		if(StringUtils.isNotBlank(currentHourAttr)){
			currentHour = Integer.parseInt(currentHourAttr);
		}
		
		logger.info("queryCompartiotorItems attrValue ::: domain :{},currentDay :{},currentHour :{}",domain,currentDay,currentHour);
		
		if(StringUtils.isBlank(domain)){
			logger.error("CompartiotorItemsAction queryCompartiotorItems request domain is empty");
			this.writeResponse(response, "Error:got no domain");
			return;
		}
		
		TaskDOCache taskDOCache = TaskDOCache.getInstance();
		if(null == taskDOCache || taskDOCache.size() == 0){
			logger.error("ActivityItemsAction queryActivityItems taskDOCache is empty");
			this.writeResponse(response, "Error:taskDOCache is empty");
			return;
		}
		
		//匹配 domain　所属任务，找到taskId
		String taskId = StringUtils.EMPTY;
		for (Map.Entry<Long, TaskDO> entry: taskDOCache.entrySet()) {
			Long key = entry.getKey();
			TaskDO taskDo = entry.getValue();
			if(StringUtils.containsIgnoreCase(taskDo.getInitUrl(), domain)){
				taskId = String.valueOf(key);
				break;
			}
		}
		
		if(StringUtils.isBlank(taskId)){
			logger.error("ActivityItemsAction queryActivityItems taskId is null");
			this.writeResponse(response, "Error:taskId is null");
			return;
		}
		
		List<String> list = compartiotorItemsService.queryCompartiotorItemsByTaskId(Long.parseLong(taskId),currentDay,currentHour);
		if(null == list || list.size() == 0){
			logger.error("CompartiotorItemsAction queryCompartiotorItems resulyJSONArray is null");
			this.writeResponse(response, "resulyJSONArray is null");
			return ;
		}
		logger.info("CompartiotorItemsAction queryCompartiotorItems resulyJSONArray : {}",list.toString());
		this.writeResponse(response, list.toString());
		
	}
	
	private void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
	
	public static void main(String[] args) {
		
	}
}
