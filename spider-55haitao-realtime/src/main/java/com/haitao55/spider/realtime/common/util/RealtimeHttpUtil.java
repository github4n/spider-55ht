package com.haitao55.spider.realtime.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.HttpClientUtil;

/**
 * 二次核价接口调用
 * @author denghuan
 *
 */
public class RealtimeHttpUtil {
	private static final Logger LOGGER_REALTIME_RECEIVED_URLS = LoggerFactory.getLogger("realtime_received_urls");

	// 阿里 云 地址
	private static final String ALI_PATH = "http://10.25.169.237/spider-55haitao-realtime/realtime-crawler/pricing.action";
	
	// google 云 地址
	private static final String GOOGLE_PATH = "http://10.128.0.2:8080/spider-55haitao-realtime/realtime-crawler/pricing.action";
	
	/**
	 * 调用 cloud realtime 接口
	 * 
	 * @param url
	 * @param requestFrom
	 * @param timeout
	 * @param isSendMessage
	 * @param response
	 */
	public static void realTimeCloudTransfer(String url, String requestFrom, String timeout, String isSendMessage,
			String isGoogleRealTime) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);
		params.put("request_from", requestFrom);
		params.put("timeout", timeout);
		params.put("is_send_message", isSendMessage);
		
		try {
			LOGGER_REALTIME_RECEIVED_URLS.info("call second realTime start, url : {}, requestFrom :{}, timeout : {} , isSendMessage :{} ,isGoogleRealTime : {} ",
					url,requestFrom,timeout,isSendMessage,isGoogleRealTime);
			if(StringUtils.isNotBlank(isGoogleRealTime)){
				LOGGER_REALTIME_RECEIVED_URLS.info("call google realTime, url : {}, isGoogleRealTime :{}",url,isGoogleRealTime);
				HttpClientUtil.postDespiteException(GOOGLE_PATH, params);
			}else{
				LOGGER_REALTIME_RECEIVED_URLS.info("call aliYun realTime, url : {}, isGoogleRealTime :{}",url,isGoogleRealTime);
				HttpClientUtil.postDespiteException(ALI_PATH, params);
			}
			LOGGER_REALTIME_RECEIVED_URLS.info("call second realTime end, url : {}, isGoogleRealTime :{}",url,isGoogleRealTime);
		} catch (Exception e) {
			LOGGER_REALTIME_RECEIVED_URLS.error("call second realTime execution exception , url : {},exception : {}",url,e);
		}
	}
}
