package com.haitao55.spider.realtime.controller.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.haitao55.spider.common.entity.RTReturnCode;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.realtime.cache.IgnoreDomainCache;
import com.haitao55.spider.realtime.controller.RealtimeCrawlerController;
import com.haitao55.spider.realtime.controller.reloadDomain.ReloadDomainUtil;

/**
 * 目前 有阿里云 和google云 两套环境， 这个类用作google逻辑封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月3日 下午3:30:06
* @version 1.0
 */
public class GoogleRealtimeCrawler {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);
	private static final Logger LOGGER_REALTIME_OUTCOME_RESULT = LoggerFactory.getLogger("realtime_outcome_result");

	private static final Logger LOGGER_REALTIME_RECEIVED_URLS = LoggerFactory.getLogger("realtime_received_urls");

	// google 云 地址
	private static final String path = "http://104.197.229.122:8080/spider-55haitao-realtime/realtime-crawler/pricing.action";
		/**
		 * 请求分发到google
		 * @param response 
		 * @param isSendMessage 
		 * @param timeout 
		 * @param requestFrom 
		 * @param url 
		 * @param model 
		 * @param request 
		 */
		public static boolean googleCloudTranserPackage(String url, String requestFrom, String timeout, String isSendMessage, HttpServletRequest request, HttpServletResponse response, Model model) {
			/**url pattern*/
//			UrlPatternCache urlPatterInstance = UrlPatternCache.getInstance();
//			String subUrl = StringUtils.substringBetween(url, "//", "/");
//			String md5Value=StringUtils.EMPTY;
//			if(StringUtils.isNotBlank(subUrl)){
//				md5Value =SpiderStringUtil.md5Encode(subUrl);
//				Pattern pattern = urlPatterInstance.get(md5Value);
//				if(null!=pattern){
//					googleCloudTransfer(url,requestFrom,timeout,isSendMessage,response);
//					return true;
//				}
//			}
			/**domain*/
			IgnoreDomainCache instance = IgnoreDomainCache.getInstance();
			if(null!=instance&&instance.size()==0){
				ReloadDomainUtil.reloadIgnoreDomain();
			}
			if(null!=instance&&instance.size()>0){
				List<String> domains = new ArrayList<String>();
				for (Pattern p : instance) {
					String str = p.toString();
					domains.add(str);
				}
				boolean pattern_flag = false;
				for (String regex : domains) {
					Pattern pattern = Pattern.compile(regex);
					Matcher matcher = pattern.matcher(url);
					if(matcher.matches()){
						pattern_flag = true;
					}
				}
				for (Pattern p : instance) {
					Matcher matcher = p.matcher(url);
					if(!matcher.matches() && !pattern_flag){
						//UrlPatternCache put value
//						UrlPatternCache.getInstance().put(md5Value, p);
						
						googleCloudTransfer(url,requestFrom,timeout,isSendMessage,response);
						return true;
					}
				}
			}
			return false;
		}
	
	/**
	 * 调用 google cloud realtime 接口
	 * 
	 * @param url
	 * @param requestFrom
	 * @param timeout
	 * @param isSendMessage
	 * @param response
	 */
	private static void googleCloudTransfer(String url, String requestFrom, String timeout, String isSendMessage,
			HttpServletResponse response) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);
		params.put("request_from", requestFrom);
		params.put("timeout", timeout);
		params.put("is_send_message", isSendMessage);
		
		String jsonResult = StringUtils.EMPTY;
		long startTime = System.currentTimeMillis();
		try {
			LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime first time start, url : {}, startTime :{}",url,startTime);
			jsonResult = HttpClientUtil.postDespiteException(path, params);
			
			long endTime = System.currentTimeMillis();
			
			LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime first time end, url : {}, endTime :{}",url,endTime);
			LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime first time internal, url : {}, internalTime :{}",url,(endTime-startTime));

			// 匹配到google云地址,返回处理结果
			writeResponse(response, jsonResult,url);
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime first time exception internal, url : {}, internalTime :{}",url,(endTime-startTime));

			LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime execution exception first time, url : {},exception : {}",url,e);
			
			//向google转发，重试一次
			try {
				jsonResult = HttpClientUtil.postDespiteException(path, params);
				writeResponse(response, jsonResult,url);
			} catch (Exception e1) {
				LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime execution exception second time, url : {},exception : {}",url,e1);
				writeResponseInErrorCase(response,RTReturnCode.RT_INNER_ERROR,url);
			}
		}
	}

	private static void writeResponse(HttpServletResponse response, String responseBody,String url) {
		try {
			LOGGER_REALTIME_OUTCOME_RESULT.info("call google realTime execution result , url : {},result : {}",url,responseBody);
			
			if(StringUtils.isBlank(responseBody)){
				writeResponseInErrorCase(response,RTReturnCode.RT_INNER_ERROR,url);
			}else{
				response.setContentType("text/html; charset=UTF-8");
				response.getWriter().print(responseBody);
			}
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
	
	private static void writeResponseInErrorCase(HttpServletResponse response, RTReturnCode rtReturnCode, String url) {
		CrawlerJSONResult jsonResult = new CrawlerJSONResult();

		jsonResult.setRetcode(1);
		jsonResult.setRtReturnCode(rtReturnCode.getValue());

		jsonResult.setMessage("error");

		RetBody retBody = new RetBody();
		retBody.setProdUrl(new ProdUrl(url));
		retBody.setStock(new Stock(0));
		jsonResult.setRetbody(retBody);

		jsonResult.setTaskId("");
		jsonResult.setDocType("");
		jsonResult.setFromTag("");

		String jsonStr = jsonResult.parseTo();

		writeResponse(response, jsonStr,url);
	}
	
}
