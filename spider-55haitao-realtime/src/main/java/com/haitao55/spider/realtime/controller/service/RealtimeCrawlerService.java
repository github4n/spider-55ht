package com.haitao55.spider.realtime.controller.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.ImageDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.dos.RealtimeStatisticsDO;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.service.impl.RedisService;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.image.service.ImageService;
import com.haitao55.spider.realtime.async.RealtimeCrawlerThreadPoolExecutor;
import com.haitao55.spider.realtime.async.RealtimeServiceCallable;
import com.haitao55.spider.realtime.async.RealtimeThreadPoolExecutor;
import com.haitao55.spider.realtime.cache.IgnoreDomainCache;
import com.haitao55.spider.realtime.cache.RealtimeServiceIgnoreDomainCache;
import com.haitao55.spider.realtime.common.util.CrawlerResultUtil;
import com.haitao55.spider.realtime.common.util.OutPutDataUtil;
import com.haitao55.spider.realtime.controller.RealtimeCrawlerController;
import com.haitao55.spider.realtime.controller.reloadDomain.ReloadDomainUtil;
import com.haitao55.spider.realtime.service.OutputDataDealService;

/**
 * 用作调用接口进行核价, 目前亚马逊有用到
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月3日 下午3:53:53
* @version 1.0
 */
public class RealtimeCrawlerService {
	private static final Logger logger = LoggerFactory.getLogger(RealtimeCrawlerController.class);
	private static final Logger logger_time_consuming = LoggerFactory.getLogger("realtime_time_consuming");
	private static final Logger result_data_logger = LoggerFactory.getLogger("result_data_logger");
	
	
	private static long CRAWLING_WAIT_TIME = 7 * 1000;// 在线抓取最多等待时间
	private static final TimeUnit CRAWLING_WAIT_UNIT = TimeUnit.MILLISECONDS;// 在线抓取最多等待时间单位
	private static final int REDIS_EXPIRED_TIME = 300;// redis key过期时间
	//result json error flag
	private static String errorMsg="ERROR";
	//阿里云地址
	private static final String ali_path = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";
	
	//result from tag
	private static String REALTIME_RESULT_FROM_MONGO_TAG = "mongo";
	private static String REALTIME_RESULT_FROM_REDIS_TAG = "redis";
	private static String REALTIME_RESULT_FROM_CRAWLER_TAG = "crawler";
	/**
	 * 调用realtime_service提供的接口，返回json数据
	 * @param url 
	 * @param task 
	 * @param isSendMessage 
	 * @param CRAWLING_WAIT_TIME 
	 * @param model 
	 * @param response 
	 * @param request 
	 * @param realtimeStatisticsDO 
	 * @param currentItemDAOImpl 
	 * @param redisService 
	 * @param outputDataDealService 
	 * @param imageDAO 
	 * @param imageService 
	 * @param topic 
	 * @param producer 
	 * @param monitorService 
	 */
	public static boolean realtime_service_package(Task task, String url, long cRAWLING_WAIT_TIME, String isSendMessage,
			HttpServletRequest request, HttpServletResponse response, Model model,
			RealtimeStatisticsDO realtimeStatisticsDO, ImageService imageService, ImageDAO imageDAO, OutputDataDealService outputDataDealService, RedisService redisService, CurrentItemDAO currentItemDAOImpl, SpiderKafkaProducer producer, String topic, MonitorService monitorService) {
		long start = System.currentTimeMillis();
		//目前任务大多在google云上，所以只有在请求google核价再进行接口地址调用，返回jsonresult
		IgnoreDomainCache instance = IgnoreDomainCache.getInstance();
		if(null!=instance&&instance.size()==0){
			ReloadDomainUtil.reloadIgnoreDomain();
		}
		instance = IgnoreDomainCache.getInstance();
		if(null==instance||instance.size()==0){//google核价
			//请求url需要进行接口调用返回json
			boolean isPattern = realtime_service_url_pattern(url,request,response,model);
			if(isPattern){
				logger.info("request url : {} to realtime service",url);
				//调用接口
				final String urlTemp = url;
				Long taskId = task.getTaskId();
				String jsonResult = StringUtils.EMPTY;
				RealtimeThreadPoolExecutor postExecutorService = RealtimeThreadPoolExecutor.getInstance();
				ExecutorService executorService = RealtimeCrawlerThreadPoolExecutor.getInstance();
				RealtimeServiceCallable callable = new RealtimeServiceCallable(taskId,url);
				Future<OutputObject> future = executorService.submit(callable);
				OutputObject oo = null;
				try {
					// 当前线程最多等待一定时间,如果还获取不到抓取结果,则当前线程继续执行
					// 这里的"当前线程"也就是应用程序服务器(如tomcat)为处理当前http请求而启动的处理线程；
					oo = future.get(CRAWLING_WAIT_TIME, CRAWLING_WAIT_UNIT);
				} catch (InterruptedException e) {
					logger.error("Error while realtime-crawling::", e);
				} catch (ExecutionException e) {
					logger.error("Error while realtime-crawling::", e);
				} catch (TimeoutException e) {
					//针对超时线程，终止线程执行
					future.cancel(true);
					
					logger_time_consuming.error("realtime timeout url: {}",url);
					postExecutorService.submit(new Runnable(){
						@Override
						public void run() {
//							googleCloudTransfer(urlTemp,requestFrom,"60000",isSendMessage,response);
							Map<String, String> params = new HashMap<String, String>();
							params.put("url", urlTemp);
							params.put("request_from", "timeout realtime service");
							params.put("timeout", "60000");
							params.put("is_send_message", isSendMessage);
							HttpClientUtil.post(ali_path, params);
						}
					});
					logger.error("Error while realtime-crawling::", e);
				}
				if (Objects.nonNull(oo)) {
					jsonResult = oo.convertItem2Message();
					boolean boo = CrawlerResultUtil.isoffLine(jsonResult);
					if(boo){
						imageService.setImageDAO(imageDAO);
						oo=OutPutDataUtil.writeOutputObjectActually(oo, outputDataDealService,imageService);
					}
					result_data_logger.info("result json from realtime service  url : {} ,json :{}",url,jsonResult);
					jsonResult = CrawlerResultUtil.crawler_result(oo.convertItem2Message(), boo, task,url,oo,producer,topic,currentItemDAOImpl,realtimeStatisticsDO);
					// put redis(key, value, time:1minute)
					String msg = StringUtils.substringBetween(jsonResult, "message","retcode");
					if(!StringUtils.containsIgnoreCase(msg, errorMsg)){
						//when jsonResult is not ERROR, set redis
						redisService.set(SpiderStringUtil.md5Encode(url), jsonResult, REDIS_EXPIRED_TIME);
					}
					
					//from crawler
					jsonResult = CrawlerResultUtil.result_from_package(jsonResult,REALTIME_RESULT_FROM_CRAWLER_TAG);

					if(realtimeStatisticsDO.getException()==0){
						realtimeStatisticsDO.setCrawler(1);
					}
				}
				else {
					ItemDO queryMd5UrlLastItem = currentItemDAOImpl.queryMd5UrlLastItem(task.getTaskId(), SpiderStringUtil.md5Encode(url));
					
					long time9 = System.currentTimeMillis();
					logger_time_consuming.info("Debug20161115 url: {}, time9-start: {} ",url ,time9 - start);
					
					jsonResult = CrawlerResultUtil.mongo_result(queryMd5UrlLastItem, task, url,monitorService);
					result_data_logger.info("result json from mongo  url : {} ,json :{}",url,jsonResult);
					
					//from mongo
					jsonResult = CrawlerResultUtil.result_from_package(jsonResult,REALTIME_RESULT_FROM_MONGO_TAG);
					
					realtimeStatisticsDO.setMongo(1);
				}
				long end = System.currentTimeMillis();
				result_data_logger.info("result json from realtime service  url : {} ,json :{}",url,jsonResult);
				
				logger_time_consuming.info("Debug20161115 url: {}, realtime service consume: {} ",url ,end - start);
				
				if (StringUtils.isBlank(jsonResult)) {
					writeResponse(response, "Error:got no item neither by crawling nor from database!");
					return true;
				}
				
				writeResponse(response, jsonResult);
				
				return true;
			}
			
			return false;
		}
		return false;
	}
	
	
	/**
	 * 加载配置文件，验证url是否需要进行接口调用返回数据
	 * @param url
	 * @param model 
	 * @param response 
	 * @param request 
	 * @return
	 */
	private static boolean realtime_service_url_pattern(String url, HttpServletRequest request, HttpServletResponse response, Model model) {
		/**url pattern*/
//		RealtimeServiceUrlPatternCache urlPatterInstance = RealtimeServiceUrlPatternCache.getInstance();
//		String subUrl = StringUtils.substringBetween(url, "//", "/");
//		String md5Value=StringUtils.EMPTY;
//		if(StringUtils.isNotBlank(subUrl)){
//			md5Value =SpiderStringUtil.md5Encode(subUrl);
//			Pattern pattern = urlPatterInstance.get(md5Value);
//			if(null!=pattern){
//				return true;
//			}
//		}
		
		/**domain*/
		RealtimeServiceIgnoreDomainCache instance = RealtimeServiceIgnoreDomainCache.getInstance();
		if(null!=instance&&instance.size()==0){
			reloadRealtimeServiceIgnoreDomain(request,response,model);
		}
		if(null!=instance&&instance.size()>0){
			for (Pattern p : instance) {
				Matcher matcher = p.matcher(url);
				if(matcher.matches()){
					//RealtimeServiceUrlPatternCache put value
//					RealtimeServiceUrlPatternCache.getInstance().put(md5Value, p);
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	/**
	 * 加载需要进行接口调用的url的配置文件
	 * @param request
	 * @param response
	 * @param model
	 */
	private static void reloadRealtimeServiceIgnoreDomain(HttpServletRequest request, HttpServletResponse response,
			Model model) {
		// 加载 配置域名,访问google 云
		// WEB-INF/classes
//		String filepath = Thread.currentThread().getContextClassLoader().getResource("/").getPath();
		URL url = RealtimeCrawlerController.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String filepath = jarFile.getParent();
		
		filepath = StringUtils.replacePattern(filepath, "webapps.*", "");
		String fileName = "realtimeServcieDomain";
		StringBuffer stringBuffer = new StringBuffer();
		try {
			Set<Pattern> newSet = new LinkedHashSet<Pattern>();
			InputStream in = new FileInputStream(new File(filepath + "/" + fileName));
			String line = "";
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			line = buffer.readLine();
			while (line != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
				line = buffer.readLine();
			}
			buffer.close();
			in.close();
			JSONObject jsonObject = (JSONObject) JSONObject.parse(stringBuffer.toString());
			JSONArray jsonArray = (JSONArray) jsonObject.get("realtimeServcieDomain");
			for (Object object : jsonArray) {
				Pattern p = Pattern.compile(object.toString());
				newSet.add(p);
			}

			// 温和替换 RealtimeServiceIgnoreDomainCache  Pattern 本身不好比较, 这里先采取暴力替换
			RealtimeServiceIgnoreDomainCache.getInstance().clear();
			// 加载
			RealtimeServiceIgnoreDomainCache.getInstance().addAll(newSet);
		} catch (FileNotFoundException e) {
			logger.error("realtimeServcieDomain file is not exists", e);
		} catch (IOException e) {
			logger.error("realtimeServcieDomain file io error", e);
		}catch (Exception e) {
			logger.error("realtimeServcieDomain transfer error",e);
		}
	}
	
	
	private static void writeResponse(HttpServletResponse response, String responseBody) {
		try {
			response.setContentType("text/html; charset=UTF-8");
			response.getWriter().print(responseBody);
		} catch (IOException e) {
			logger.error("Error while writting HttpServletResponse::", e);
		}
	}
}
