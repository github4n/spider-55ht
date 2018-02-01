package com.haitao55.spider.realtime.common.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dos.ImageDO;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.image.service.ImageService;
import com.haitao55.spider.realtime.service.OutputDataDealService;

public class ImageRunnable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final int IMAGE_DOWNLOAD_RETRY_TIME = 3;
	private OutputDataDealService outputServiceKafka;
	private ImageService imageService;
	private Image image;
	private OutputObject oo;
	
	
	public ImageRunnable() {
		super();
	}


	public ImageRunnable(OutputDataDealService outputServiceKafka, ImageService imageService, Image image, OutputObject oo) {
		super();
		this.outputServiceKafka = outputServiceKafka;
		this.imageService = imageService;
		this.image = image;
		this.oo = oo;
	}


	@Override
	public void run() {
		long start =System.currentTimeMillis();
		boolean cdnExist = true;
		try {
		    String cdn_key = StringUtils.substringBetween(image.getRepertoryUrl(), "/prodimage/", ".jpg");
            cdnExist = imageService.isCdnKeyExist(Long.valueOf(oo.getTaskId()), cdn_key);
		} catch (Throwable e) {
			e.printStackTrace();
			cdnExist = outputServiceKafka.existInRepertory(image);
			logger.error("Error url {} ,taskId {},src image {},srcExist {}",oo.getUrl().toString(),oo.getTaskId(),image.getOriginalUrl(),cdnExist);
		}
		logger.info("Info url {} ,taskId {},src image {},srcExist {}",oo.getUrl().toString(),oo.getTaskId(),image.getOriginalUrl(),cdnExist);
		if(!cdnExist){
		    long downStart = System.currentTimeMillis();
			for (int i = 0; i < IMAGE_DOWNLOAD_RETRY_TIME; i++) {
				boolean isSucc = this.downloadImageData(image, oo);// 下载图片的逻辑都是一样的
				logger.info("Info url {} ,img src {} , cdn  {} ,download isSucc {}",oo.getUrl().toString(),image.getOriginalUrl(),image.getRepertoryUrl(),isSucc);
				if (StringUtils.isNotBlank(image.getOriginalUrl()) && ArrayUtils.isNotEmpty(image.getData())) {
					break;
				}
				try {
					Thread.sleep(i*1000);//downloading picture
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			long downEnd = System.currentTimeMillis();
			logger.info("Info url {} ,img src {},img cdn {},donwload consume:[{}]",oo.getUrl().toString(),image.getOriginalUrl(),image.getRepertoryUrl(),(downEnd - downStart));
			long uploadStart = System.currentTimeMillis();
			outputServiceKafka.uploadImage(image, oo);
			long uploadEnd = System.currentTimeMillis();
	        logger.info("Info url {} ,img src {},img cdn {},upload consume:[{}]",oo.getUrl().toString(),image.getOriginalUrl(),image.getRepertoryUrl(),(uploadEnd - uploadStart));
			ImageDO imageDO = new ImageDO();
			imageDO.setDoc_id(SpiderStringUtil.md5Encode(oo.getUrl().toString()));
			imageDO.setSrc(image.getOriginalUrl());
			imageDO.setSrc_key(SpiderStringUtil.md5Encode(image.getOriginalUrl()));
			imageDO.setCdn(image.getRepertoryUrl());
			imageDO.setCdn_key(StringUtils.substringBetween(image.getRepertoryUrl(), "/prodimage/", ".jpg"));
			imageDO.setStatus("1");//已下载
			imageDO.setCreate_time(System.currentTimeMillis());
			try {
			    List<ImageDO> imageDoList = new ArrayList<ImageDO>();
			    imageDoList.add(imageDO);
	            //save images
	            boolean result = imageService.saveImages(Long.valueOf(oo.getTaskId()), imageDoList);
	            logger.info("Info url {} ,img src {},img cdn {},save status {}",oo.getUrl().toString(),image.getOriginalUrl(),image.getRepertoryUrl(),result);
			} catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
	long end = System.currentTimeMillis();
	logger.info("Write output object successfully, url:{},consume time:{}", oo.getUrl().getValue(), (end - start));
		
//		boolean existInRepertory = outputServiceKafka.existInRepertory(image);
//		logger.info("image exist upyun aaaaa boolean: {} ",existInRepertory);
//		if (!existInRepertory) {// 图片在图片库中不存在,才执行实际的下载和上传过程
//			long start =System.currentTimeMillis();
//			for (int i = 0; i < IMAGE_DOWNLOAD_RETRY_TIME; i++) {
//				downloadImageData(image, oo);// 下载图片的逻辑都是一样的
//				if (StringUtils.isNotBlank(image.getOriginalUrl()) && ArrayUtils.isNotEmpty(image.getData())) {
//					break;
//				}
//			}
//			outputServiceKafka.uploadImage(image, oo);
//			long end =System.currentTimeMillis();
//			logger.info("upyun handle image cusimg xxxx time: {} ",end-start);
//		}
	}

	private boolean downloadImageData(Image image, OutputObject oo) {
		try {
			byte[] imageData = null;
			
			// 为中国亚马逊下载图片逻辑单独实现，使用Luminati下载图片
//			if(StringUtils.startsWith(image.getOriginalUrl(), "https://images-cn.ssl-images-amazon.com")){
//				LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient(AmazonCNPage.COUNTRY, true);
//				imageData = luminatiHttpClient.requestAsByteArray(image.getOriginalUrl(), AmazonCNPage.getHeaders());
//			}else{
				String proxyLocaleId = oo.getUrl().getTask().getProxyRegionId();
				if (StringUtils.isNotBlank(proxyLocaleId)) {// 使用代理
					Proxy proxy = ProxyCache.getInstance().pickup(proxyLocaleId, true);
					imageData = Crawler.create().retry(3).timeOut(10000).url(image.getOriginalUrl())
							.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(proxy.getIp())
							.proxyPort(proxy.getPort()).resultAsBytes();
				} else {// 不使用代理
					imageData = Crawler.create().retry(3).timeOut(10000).url(image.getOriginalUrl())
							.method(HttpMethod.GET.getValue()).resultAsBytes();
				}
//			}
			
			image.setData(imageData);
			return true;
		} catch (Exception e) {
		    e.printStackTrace();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return false;
	}



	public OutputDataDealService getOutputServiceKafka() {
		return outputServiceKafka;
	}


	public void setOutputServiceKafka(OutputDataDealService outputServiceKafka) {
		this.outputServiceKafka = outputServiceKafka;
	}


	public Image getImage() {
		return image;
	}


	public void setImage(Image image) {
		this.image = image;
	}


	public OutputObject getOo() {
		return oo;
	}


	public void setOo(OutputObject oo) {
		this.oo = oo;
	}
	
	
	

}
