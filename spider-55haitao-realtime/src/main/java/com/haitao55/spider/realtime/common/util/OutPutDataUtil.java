package com.haitao55.spider.realtime.common.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.image.service.ImageService;
import com.haitao55.spider.realtime.service.OutputDataDealService;

/**
 * 
* Title:
* Description: 实时核价返回数据封装,进行图片上传和cdn封装
* Company: 55海淘
* @author zhaoxl 
* @date 2016年11月1日 下午6:13:37
* @version 1.0
 */
public class OutPutDataUtil {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final int DEFAULT_CORE_POOL_SIZE = 5;
	private static final int DEFAULT_MAXIMUM_POOL_SIZE = 50;
	private static final int DEFAULT_KEEP_ALIVE_TIME = 10 * 1000;
	private static final int DEFAULT_WORK_QUEUE_SIZE = 1000;
	private static ExecutorService imageHandleExecutorService = null;
	
	static{// 所有商品的所有图片的 判空/下载/上传 都使用这一个线程池
		imageHandleExecutorService = new ThreadPoolExecutor(DEFAULT_CORE_POOL_SIZE,
				DEFAULT_MAXIMUM_POOL_SIZE,
				DEFAULT_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(DEFAULT_WORK_QUEUE_SIZE),
				Executors.defaultThreadFactory(), new ThreadPoolExecutor.DiscardOldestPolicy());// 采用默默丢弃的策略
	}
	
	public static OutputObject writeOutputObjectActually(OutputObject oo,OutputDataDealService outputDataDealService, ImageService imageService) {
		handleImages(oo,outputDataDealService,imageService);
		return oo;
	}
	
	private static void handleImages(OutputObject oo,OutputDataDealService outputServiceKafka, ImageService imageService) {
		long start = System.currentTimeMillis();
		Map<String, List<Image>> images = oo.getImages();
		for (Map.Entry<String, List<Image>> entry : images.entrySet()) {
			List<Image> imageSet = entry.getValue();
			// String skuId = entry.getKey();
			//List<ImageDO> imageDoList = new ArrayList<ImageDO>();
			if(null != imageSet && imageSet.size() > 0){
				for (Image image : imageSet) {
					outputServiceKafka.createImageRepertoryUrl(image, oo);// 为了向image对象中设置完整的图片cdn地址链接字符串
					Runnable runnable=new ImageRunnable(outputServiceKafka,imageService,image,oo);
					imageHandleExecutorService.submit(runnable);
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Write output object successfully, url:{},consume time:{}", oo.getUrl().getValue(), (end - start));
	}
}
