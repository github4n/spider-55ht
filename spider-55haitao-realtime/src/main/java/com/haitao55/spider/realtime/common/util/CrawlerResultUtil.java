package com.haitao55.spider.realtime.common.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.entity.RTReturnCode;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.realtime.async.RealtimeKafkaThreadPoolExecutor;
import com.haitao55.spider.realtime.controller.RealtimeCrawlerController;

/**
 * 针对核价返回结果进行相应处理  判断  发送kafka 等
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月3日 下午4:02:03
* @version 1.0
 */
public class CrawlerResultUtil {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);
	private static final Logger logger_time_consuming = LoggerFactory.getLogger("realtime_time_consuming");
	private static final Logger LOGGER_REALTIME_RECEIVED_URLS = LoggerFactory.getLogger("realtime_received_urls");
	private static final String _6PM_DOMAIN = "6pm";
	private static String is_send_message = "Y";
	private static String realtime_result_from_mongo = "realtime_result_from_mongo";
//	private static String realtime_result_from_redis = "realtime_result_from_redis";
	private static String realtime_result_from_mongo_no_item = "realtime_result_from_mongo_no_item";
	/**
	 * 校验商品完整性,
	 * 
	 * @param jsonResult
	 */
	public static boolean isoffLine(String jsonResult) {
		boolean boo = false;
		// 6pm 校验
		if (StringUtils.isBlank(jsonResult)) {
			return boo;
		}
		if (StringUtils.contains(jsonResult, _6PM_DOMAIN)) {
			boo = is6pmOffLine(jsonResult);
		} else {
			boo = isGeneralOffLine(jsonResult);
		}
		return boo;

	}
	
	/**
	 * 通用返回数据校验
	 */
	private static boolean isGeneralOffLine(String jsonResult) {
		CrawlerJSONResult crawlerResult = CrawlerJSONResult.buildFrom(jsonResult);
		return crawlerResult.isValid();
	}

	/**
	 * 6pm 特有校验 目前还没有
	 * 
	 * @param jsonResult
	 */
	private static boolean is6pmOffLine(String jsonResult) {
		CrawlerJSONResult crawlerResult = CrawlerJSONResult.buildFrom(jsonResult);
		return crawlerResult.isValid();
	}
	
	/**
	 * 核价数据结果处理
	 * @param jsonResult
	 * @param boo
	 * @param task
	 * @param url 
	 * @param oo
	 * @param topic 
	 * @param producer 
	 * @param currentItemDAOImpl 
	 * @param realtimeStatisticsDO 
	 * @return
	 */
	public static String crawler_result(String jsonResult, boolean boo, Task task, String url, OutputObject oo, SpiderKafkaProducer producer, String topic, CurrentItemDAO currentItemDAOImpl, RealtimeStatisticsDO realtimeStatisticsDO) {
		String json = StringUtils.EMPTY;
		if (!boo) {
			CrawlerJSONResult result = null;
			RetBody retBody = new RetBody();
			retBody.setProdUrl(new ProdUrl(url));
			retBody.setStock(new Stock(0));
			//抛出offline异常和http异常时  docType 为delete
			if(StringUtils.endsWithIgnoreCase(oo.getDocType().name(), "DELETE")){
				result = new CrawlerJSONResult("OK", 1, retBody,
						String.valueOf(task.getTaskId()),DocType.DELETE.toString());
				result.setRtReturnCode(RTReturnCode.RT_ITEM_OFFLINE.getValue());
				// 下架 
				sendMessageToKafka(result.parseTo(),producer,topic);
				json = result.parseTo();
				//记录异常次数
				realtimeStatisticsDO.setException(1);
//				//offline 的 从mongo中获取数据  修改spu stock status 为 0  告知se 这个商品没货了
//				ItemDO queryMd5UrlLastItem = currentItemDAOImpl.queryMd5UrlLastItem(task.getTaskId(),
//						SpiderStringUtil.md5Encode(url));
//				String itemValue = queryMd5UrlLastItem.getValue();
//				if(StringUtils.isNotBlank(itemValue)){
//					CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(itemValue);
//					RetBody retbody = buildFrom.getRetbody();
//					retbody.setStock(new Stock(0));
//					buildFrom.setRetbody(retbody);
//					
//					json = buildFrom.parseTo();
//					
//				}
				
			}
			//爬取到数据 但是数据不符合校验规则时
			else{
				result = new CrawlerJSONResult("OK", 1, retBody,
						String.valueOf(task.getTaskId()),DocType.DELETE.toString());
				result.setRtReturnCode(RTReturnCode.RT_ITEM_OFFLINE.getValue());
				// 下架 
				sendMessageToKafka(result.parseTo(),producer,topic);
				json = result.parseTo();
			}
		} else {
			// crawler return data deal
			json = jsonResult;
			
			CrawlerJSONResult result = CrawlerJSONResult.buildFrom(json);
			// *****这一句“‘极其’重要”，决不能缺少***** //
			if (result.getRetbody().getStock().getStatus() == 0) {// 所有sku库存都为0,所以spu库存就为0
				if (DocType.INSERT.equals(result.getDocType())) {
					result.setRtReturnCode(RTReturnCode.RT_STOCK_NONE.getValue());
				} else if (DocType.DELETE.equals(result.getDocType())) {
					result.setRtReturnCode(RTReturnCode.RT_ITEM_OFFLINE.getValue());
				} else if (DocType.NOT_SELECT_REQUIRED_PROPERTY.equals(result.getDocType())) {
					result.setRtReturnCode(RTReturnCode.NOT_SELECT_REQUIRED_PROPERTY.getValue());
				} else {
					LOGGER_REALTIME_RECEIVED_URLS.error("found DO NOT support DocType, url:{}", url);
				}
			} else {
				result.setRtReturnCode(RTReturnCode.RT_SUCCESS.getValue());
			}
			
			json = result.parseTo();
			
			if (is_send_message.equalsIgnoreCase("Y")) {// Y 发送消息
				sendMessageToKafka(json,producer,topic);
			}
		}
		return json;
	}
	
	/**
	 * 
	 * 发送kafka消息
	 * @param jsonResult
	 * @param topic 
	 * @param producer 
	 */
	private static void sendMessageToKafka(String jsonResult, SpiderKafkaProducer producer, String topic){
		RealtimeKafkaThreadPoolExecutor.getInstance().submit(new Runnable() {
			
			@Override
			public void run() {
				try {
					String url = StringUtils.substringBetween(jsonResult, "\"url\":\"", "\",");
					long time7 = System.currentTimeMillis();
					SpiderKafkaResult result = producer.sendbyCallBack(topic, jsonResult);
					long time12 = System.currentTimeMillis();
//					logger_time_consuming.info("Debug20161115 url: {}, time12-time7 kafka consume time: {} ",url ,time12 - time7);
					logger.info("send a message offset :{}, message:{}", result.getOffset(), jsonResult);
				} catch (Exception e) {
					logger.error("kafka send a message error", e);
				}
			}
		});
	}
	
	/**
	 * 实时核价超过时间限制后 返回商品库数据
	 * 
	 * @param queryMd5UrlLastItem
	 * @param task
	 * @param monitorService 
	 * @return
	 */
	public static String mongo_result(ItemDO queryMd5UrlLastItem, Task task, String url, MonitorService monitorService) {
		String jsonResult = StringUtils.EMPTY;
		if (null == queryMd5UrlLastItem) {
			CrawlerJSONResult result = new CrawlerJSONResult("ERROR", 1, new RetBody(),
					String.valueOf(task.getTaskId()));
			result.setRtReturnCode(RTReturnCode.RT_CRAWLING_TIMEOUT.getValue());// 获取不到商品数据
			jsonResult = result.parseTo();
			monitorService.incField(realtime_result_from_mongo_no_item);
			logger.warn("got none item for url :{}", url);
		} else {
			jsonResult = queryMd5UrlLastItem.getValue();
			
			CrawlerJSONResult result = CrawlerJSONResult.buildFrom(jsonResult);
			result.setRtReturnCode(RTReturnCode.RT_SUCCESS.getValue());
			jsonResult = result.parseTo();
			
			monitorService.incField(realtime_result_from_mongo);
		}
		return jsonResult;
	}
	
	/**
	 * 实时核价返回结果封装 fromTag 字段
	 */
	public static String result_from_package(String jsonResult, String fromTag){
		CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(jsonResult);
		buildFrom.setFromTag(fromTag);
		return  buildFrom.parseTo();
	}
	
	public static String result_from_package(String jsonResult, RTReturnCode rtReturnCode){
		CrawlerJSONResult buildFrom = CrawlerJSONResult.buildFrom(jsonResult);
		buildFrom.setRtReturnCode(rtReturnCode.getValue());
		return  buildFrom.parseTo();
	}
}
