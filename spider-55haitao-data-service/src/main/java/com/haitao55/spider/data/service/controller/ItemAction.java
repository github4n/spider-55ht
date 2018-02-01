package com.haitao55.spider.data.service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.data.service.service.ItemsService;
import com.haitao55.spider.data.service.utils.CleaningUrlUtil;
import com.haitao55.spider.data.service.utils.WebSiteTaskCache;


/** 
 * @Description:海淘商品对外接口,通过URL参数值查询商品数据库的商品数据
 * @author: denghuan
 * @date: 2017年4月19日 下午6:09:05  
 */
@Controller
@RequestMapping("/items")
public class ItemAction {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemAction.class);
	
	private static final String ALI_PATH = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";
	
	@Value("#{configProperties['google.query.taskId.url']}")
	private String googleQuerTaskIdUrl;
	
	@Autowired
	private ItemsService itemsService;
	
	@RequestMapping(path="/queryItem", produces = "application/json; charset=utf-8")
	public @ResponseBody String queryItem(HttpServletRequest request,@RequestParam("url") String url){
		String docId = request.getParameter("docId");
		if(StringUtils.isBlank(docId)){
			docId = (String) request.getAttribute("docId");
		}
		return queryItem(url,docId);
	}
	
	private String queryItem(String url, String docId){
		String tempUrl = url;
		
		String result = StringUtils.EMPTY;
		long startTime = System.currentTimeMillis();
		
		url = StringUtils.trim(url);
		if (StringUtils.isBlank(url)) 
			return "{\"message\": \"url should not be empty!\"}";
		
		//第一步：清洗URL
		try {
			url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
			String temp = StringUtils.substringAfter(url, "//");
			String domain = StringUtils.substringBefore(temp, "/");//获取域名
			
			//第二步：获取该url所属的任务id
			String taskId = WebSiteTaskCache.getInstance().get(domain);
		
			if (StringUtils.isBlank(taskId)){
				Map<String,String> params = new HashMap<>();
				params.put("url", url);
				
				taskId = HttpClientUtil.post(googleQuerTaskIdUrl, params);//调用Google UI接口,获取taskId
				
				if(StringUtils.isBlank(taskId)){
					return "{\"message\": \"There is no task to handle this url!\"}";
				}else{
					WebSiteTaskCache.getInstance().put(domain, taskId);
				}
			}
			
			//logger.info("Handler queryItem,taskId:{},domain:{},cleaningUrl:{},current-time:{}",taskId,domain,url,System.currentTimeMillis() - startTime);
			//第三步：根据taskId和urlMd5查询最新的商品数据
			String urlMd5 = SpiderStringUtil.md5Encode(url);
			
			if(checkWebSiteIsExist(url)){
				docId = CleaningUrlUtil.cleanUrl(url);
				if(StringUtils.isNotBlank(docId)){
					result = this.itemsService.getCurrentItemValueByDocId(taskId, docId);
				}
				if(StringUtils.isBlank(result)){
					result = this.itemsService.getLastItemValueByUrlMd5(taskId, urlMd5);
				}
				logger.info("handle url Contain productId webSite:: taskID : {} ,url : {}, docId : {}, endTime : {}",taskId,url,docId,System.currentTimeMillis() - startTime);
			}else {
				result = this.itemsService.getLastItemValueByUrlMd5(taskId, urlMd5);
			}
			
			if(StringUtils.isBlank(result) && 
					(StringUtils.containsIgnoreCase(url, "6pm.com") || 
							StringUtils.containsIgnoreCase(url, "zappos.com") || 
							StringUtils.containsIgnoreCase(url, "victoriassecret.com"))){
				result = this.itemsService.getHistoryItemByUrlMd5(taskId, urlMd5);
			}
			
			if(StringUtils.isBlank(result)){
				result = itemsService.queryCurrentMD5Urls(taskId, urlMd5);//查询urlsMD5字段获取Item
				logger.info("find md5Urls::: taskId : {}, url :{}, md5Urls :{} result:{}",taskId,url,urlMd5,result);
			}
			
			logger.info("query lastItemValueByUrlMd5:: taskId: {}, afterUrl: {}, urlMd5:{}",taskId,url,urlMd5);
		} catch (Exception e) {
			logger.info("mongodb Connection Exception : {}",e.getMessage());
			return "{\"message\": \"queryItem Exception!\"}";
		}
		
		if(StringUtils.isBlank(result)){
			result = "{\"message\": \"Not found in productdb!\"}";
		}
		logger.info("compare request url and cleaningUrl:: requestUrl: {}, cleaningUrl: {},Total-time: {}",tempUrl,url,System.currentTimeMillis() - startTime);
		return result;
	}
	
	@RequestMapping(path="/fetchItem", produces = "application/json; charset=utf-8")
	public @ResponseBody String fetchItem(HttpServletRequest request,@RequestParam("url") String url){
		String rs = queryItem(url,null);
		if(StringUtils.isNotBlank(rs) && 
				!StringUtils.containsIgnoreCase(rs, "DOCID")){
			Map<String, String> params = new HashMap<String, String>();
			params.put("url", url);
			params.put("request_from", "fetchItem");
			String realTimeRs = HttpClientUtil.post(ALI_PATH, params);
			if(StringUtils.isNotBlank(realTimeRs) && 
					StringUtils.containsIgnoreCase(realTimeRs, "DOCID")){
				return realTimeRs;
			}else{
				logger.info("fetchItem --> url :{} , result : {} ",url,rs);
			}
		}
		return rs;
	}
	
	@RequestMapping("cleanUrl")
	public @ResponseBody String cleanUrl(String url){
		Pattern pattern = Pattern.compile("(http://|https://)");
        Matcher matcher = pattern.matcher(url);
        if (StringUtils.isBlank(url) || !matcher.find()) {
            return "please type the valid url.";
        }
        return DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
	}
	
	public String getGoogleQuerTaskIdUrl() {
		return googleQuerTaskIdUrl;
	}

	public void setGoogleQuerTaskIdUrl(String googleQuerTaskIdUrl) {
		this.googleQuerTaskIdUrl = googleQuerTaskIdUrl;
	}
	
	private boolean checkWebSiteIsExist(String url){
		if(StringUtils.containsIgnoreCase(url, "carters.com") || 
				StringUtils.containsIgnoreCase(url, "bergdorfgoodman.com") || 
				StringUtils.containsIgnoreCase(url, "www.escentual.com") || 
				StringUtils.containsIgnoreCase(url, "www.backcountry.com") || 
				StringUtils.containsIgnoreCase(url, "zh.ashford.com") || 
				StringUtils.containsIgnoreCase(url, "www.6pm.com") ||
				StringUtils.containsIgnoreCase(url, "www.footlocker.com") ||
				StringUtils.containsIgnoreCase(url, "www.asos.com") ||
				StringUtils.containsIgnoreCase(url, "www.shopspring.com") ||
				StringUtils.containsIgnoreCase(url, "www.ralphlauren.com") ||
				StringUtils.containsIgnoreCase(url, "www.levi.com")){
			
			return true;
		}
		return false;
	}
	
}