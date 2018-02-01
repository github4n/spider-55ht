package com.haitao55.spider.ui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.TaskCache;
import com.haitao55.spider.crawler.core.model.Rule;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.ui.service.ItemsService;
import com.haitao55.spider.ui.view.ItemHistoryView;
import com.haitao55.spider.ui.view.ItemsView;
import com.haitao55.spider.ui.view.ProxyView;

/**
 * 
 * 功能：商品管理的Action类
 * 
 * @author Arthur.Liu
 * @time 2016年11月5日 下午8:19:43
 * @version 1.0
 */
@Controller
@RequestMapping("/items")
public class ItemsMgrAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(ItemsMgrAction.class);
	
	@Value("#{configProperties['google.query.taskId.url']}")
	private String googleQuerTaskIdUrl;
	
	@Autowired
	private ItemsService itemsService;

	@RequestMapping("/gotoItemsMgrHome")
	public String gotoItemsMgrHome(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("page") String page, @RequestParam("rows") String rows) {
		int pageInt = Integer.parseInt(page);
		int rowsInt = Integer.parseInt(rows);

		List<ItemsView> listView = this.itemsService.getAllTaskItems(pageInt, rowsInt);
		model.addAttribute("pageInfo", new PageInfo<ItemsView>(listView));

		return "/items/items-mgr-home";
	}

	@RequestMapping("/gotoQueryItem")
	public String gotoQueryItem(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("taskId") String taskId, @RequestParam("docId") String docId,
			@RequestParam("url") String url) {

		model.addAttribute("taskId", taskId);
		model.addAttribute("docId", docId);
		model.addAttribute("url", url);

		if (StringUtils.isBlank(taskId)) {
			logger.error("gotoQueryItem(), taskId should not be blank!");
			model.addAttribute("itemQueryResult", "taskId should not be blank!");
			return "/items/query-item";
		}

		if (StringUtils.isNotBlank(docId)) {
			String itemQueryResult = this.itemsService.getLastItemValueByDocId(taskId, docId);
			model.addAttribute("itemQueryResult", itemQueryResult);
			return "/items/query-item";
		} else if (StringUtils.isNotBlank(url)) {
			String urlMd5 = SpiderStringUtil.md5Encode(url);
			String itemQueryResult = this.itemsService.getLastItemValueByUrlMd5(taskId, urlMd5);
			model.addAttribute("itemQueryResult", itemQueryResult);
			return "/items/query-item";
		} else {
			logger.error("gotoQueryItem(), DOCID and URL should not be blank at the same time!");
			model.addAttribute("itemQueryResult", "DOCID and URL should not be blank at the same time!");
			return "/items/query-item";
		}
	}

	@RequestMapping("/queryItem")
	public @ResponseBody String queryItem(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("url") String url) {
		String docId = request.getParameter("docId");
		if(StringUtils.isBlank(docId)){
			docId = (String) request.getAttribute("docId");
		}
		return queryItemFromDB(url,docId);
		//this.writeResponse(response, queryItemFromDB(url,docId));
	}

	private void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
	
	@RequestMapping("gotoItemsQueryHome.action")
	public String gotoItemsQueryHome(){
		return "/items/items-query-home";
	}
	
	@RequestMapping("queryItemForUI.action")
	public @ResponseBody String queryItemForUI(HttpServletRequest request, @RequestParam("targetUrl") String url){
		return queryItemFromDB(url,null);
	}

	//在谷歌云环境提供对外部从阿里云环境获取部署在谷歌云环境的任务taskId的接口
	@RequestMapping("queryTaskIdByUrl.action")
	public @ResponseBody String queryTaskIdByUrl(String url){
		Set<Entry<Long, Task>> entries = TaskCache.getInstance().entrySet();
		if (CollectionUtils.isEmpty(entries)) 
			return null;
		return this.getTaskIdByURL(url, entries);
	}
	
	private String queryItemFromDB(String url, String docId){
		long startTime = System.currentTimeMillis();
		logger.info("request queryItem method url : {}",url);
		url = StringUtils.trim(url);
		if (StringUtils.isBlank(url)) 
			return "url should not be empty!";
		
		//第一步：清洗URL
		url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
		
		//第二步：获取该url所属的任务id
		Set<Entry<Long, Task>> entries = TaskCache.getInstance().entrySet();
		String taskId = this.getTaskIdByURL(url, entries);
		if (StringUtils.isBlank(taskId)){
			Map<String,String> params = new HashMap<>();
			params.put("url", url);
			taskId = HttpClientUtil.post(googleQuerTaskIdUrl, params);
		}
		if (StringUtils.isBlank(taskId)) 
			return "There is no task to handle this url!";
		logger.info("Handler queryItem, current-time:{}",System.currentTimeMillis() - startTime);
		//第三步：根据taskId和urlMd5查询最新的商品数据
		String urlMd5 = SpiderStringUtil.md5Encode(url);
		String result = this.itemsService.getLastItemValueByUrlMd5(taskId, urlMd5);
		if(StringUtils.isBlank(result)&&StringUtils.isNotBlank(docId)){
			result = this.itemsService.getCurrentItemValueByDocId(taskId, docId);
		}
		if(StringUtils.isBlank(result)){
			result = "{\"message\": \"Not found in productdb!\"}";
		}
		
		logger.info("query mongodb lastItemValueByUrlMd5 , Total-time:{}, afterUrl : {}, urlMd5 :{}",System.currentTimeMillis() - startTime,url,urlMd5);
		return result;
	}
	
	private String getTaskIdByURL(String url, Set<Entry<Long, Task>> entries){
		String taskId = "";
		if(CollectionUtils.isNotEmpty(entries)){
			outter: for (Entry<Long, Task> entry : entries) {
				Task task = entry.getValue();
				List<Rule> rules = task.getRules();
				if (CollectionUtils.isEmpty(rules)) {
					continue;
				}

				for (Rule rule : rules) {
					if (StringUtils.isNotBlank(rule.getRegex()) && rule.matches(url)) {
						taskId = String.valueOf(task.getTaskId());
						break outter;
					}
				}
			}
		}
		return taskId;
	}
	
	/**
	 * 查询历史价格商品 -> 对外提供接口调用
	 * @param request
	 * @param response
	 * @param model
	 * @param url
	 */
	@RequestMapping("/queryHistoryItem")
	public @ResponseBody void queryHistoryItem(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("url") String url) {
		this.writeResponse(response, queryHistoryItemFromDB(url));
	}
	
	private String queryHistoryItemFromDB(String url){
		url = StringUtils.trim(url);
		if (StringUtils.isBlank(url)) 
			return "url should not be empty!";
		
		//第一步：清洗URL
		url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
		
		//第二步：获取该url所属的任务id
		Set<Entry<Long, Task>> entries = TaskCache.getInstance().entrySet();
		String taskId = this.getTaskIdByURL(url, entries);
		if (StringUtils.isBlank(taskId)){
			Map<String,String> params = new HashMap<>();
			params.put("url", url);
			taskId = HttpClientUtil.post(googleQuerTaskIdUrl, params);
		}
		if (StringUtils.isBlank(taskId)) 
			return "There is no task to handle this url!";
		
		//第三步：根据taskId和urlMd5查询最新的商品数据
		String urlMd5 = SpiderStringUtil.md5Encode(url);
		List<String> items = this.itemsService.getHistoryItemList(taskId, urlMd5);
		String result =  JSON.toJSONString(items);
		if(CollectionUtils.isEmpty(items)){
			result = "{\"message\": \"Not found in productdb!\"}";
		}
		return result;
	}

	@RequestMapping("/getItemsHistoryHome")
	public String getItemsHistoryHome(HttpServletRequest request,HttpServletResponse response,Model model,ProxyView proxyView) {
		return "items/items-history";
	}
	
	/**
	 * 获取所有item -> 分页查询内部使用
	 * @param request
	 * @param response
	 * @param model
	 * @return
	 */
	@RequestMapping("/searchItemHistory")
	public String getAllProxies(HttpServletRequest request,HttpServletResponse response,Model model) {
		String pageNos = request.getParameter("pageNos");
		String url = request.getParameter("url");
		url = StringUtils.trim(url);
		if (StringUtils.isBlank(url)) 
			return "items/items-history";
		
		//第一步：清洗URL
		url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
		
		//第二步：获取该url所属的任务id
		Set<Entry<Long, Task>> entries = TaskCache.getInstance().entrySet();
		String taskId = this.getTaskIdByURL(url, entries);
		if (StringUtils.isBlank(taskId)){
			Map<String,String> params = new HashMap<>();
			params.put("url", url);
			taskId = HttpClientUtil.post(googleQuerTaskIdUrl, params);
		}
		if (StringUtils.isBlank(taskId)) 
			return "items/items-history";
		
		//第三步：根据taskId和urlMd5查询最新的商品数据
		String urlMd5 = SpiderStringUtil.md5Encode(url);
		
		int pageNo = 1;
		if(StringUtils.isNotBlank(pageNos)){
			pageNo = Integer.parseInt(pageNos);
		}
		int pageSize=getPageSize(request);
		int index = (pageNo-1) * pageSize;
		Map<String,Object> itemMap = this.itemsService.getAllItems(taskId,urlMd5,index,pageSize);
		@SuppressWarnings("unchecked")
		List<ItemHistoryView> itemHistoryViewList = (List<ItemHistoryView>)itemMap.get("historyList");
		String total = String.valueOf(itemMap.get("count"));
		String pages = String.valueOf(itemMap.get("pages"));
		model.addAttribute("itemHistoryList", itemHistoryViewList);
		model.addAttribute("pageNo", pageNo);
		model.addAttribute("cleaningUrl", url);
		model.addAttribute("origUrl", request.getParameter("url"));
		model.addAttribute("docId", urlMd5);
		model.addAttribute("total", total);
		model.addAttribute("pages", pages);
		return "items/items-history";
	}
	
	public String getGoogleQuerTaskIdUrl() {
		return googleQuerTaskIdUrl;
	}

	public void setGoogleQuerTaskIdUrl(String googleQuerTaskIdUrl) {
		this.googleQuerTaskIdUrl = googleQuerTaskIdUrl;
	}
	
}