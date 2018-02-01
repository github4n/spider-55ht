package com.haitao55.spider.crawler.core.pipeline.valve;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dos.ImageDO;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputChannel;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.core.pipeline.context.ValveContext;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.OutputException;
import com.haitao55.spider.crawler.service.AbstractOutputService;
import com.haitao55.spider.crawler.service.impl.OutputServiceController;
import com.haitao55.spider.crawler.service.impl.OutputServiceFile;
import com.haitao55.spider.crawler.service.impl.OutputServiceKafka;
import com.haitao55.spider.crawler.service.impl.OutputServiceMail;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.image.service.ImageService;

/**
 * 
 * 功能：结果输出的Valve实现，实现批量输出功能；可能是抓取和解析结果的输出,也可能是根据前面步骤的设置输出删除类型的文档数据
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 下午1:55:59
 * @version 1.0
 */
public class OutputValve implements Valve {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final Logger url_crawler_consume_logger = LoggerFactory.getLogger("url_crawler_consume");
	private static final String JSON_BODY_KEY = "retbody";
	private static final String JSON_TBRETBODY_KEY = "tbRetbody";
	private static final int IMAGE_DOWNLOAD_RETRY_TIME = 3;
	private OutputServiceFile outputServiceFile;
	private OutputServiceController outputServiceController;
	private OutputServiceKafka outputServiceKafka;
	private OutputServiceMail outputServiceMail;
	private ImageService imageService;
	/**
	 * 提供监控功能的服务接口实现
	 */
	private MonitorService monitorService;

	@Override
	public String getInfo() {
		String info = (new StringBuilder()).append(this.getClass().getName()).append(SEPARATOR_INFO_FIELDS)
				.append(Thread.currentThread().toString()).append(SEPARATOR_INFO_FIELDS).append(this.toString())
				.toString();
		return info;
	}

	@Override
	public void invoke() throws Exception {
		List<Url> urls = ValveContext.getUrls();
		for (Url url : urls) {
			// 只有CRAWLED_OK和DELETED_OK两种类型的Url才允许作输出处理
			if (UrlType.ITEM.equals(url.getUrlType()) && (UrlStatus.CRAWLED_OK.equals(url.getUrlStatus())
					|| UrlStatus.DELETED_OK.equals(url.getUrlStatus()))) {
				try {

					logger.info("Output item onto local directory....");
					OutputObject oo = url.getOutputObject();
					if (oo != null) {
						long startTime = System.currentTimeMillis();
						this.writeOutputObjectActually(oo);
						long endTime = System.currentTimeMillis();
						long result = (endTime - startTime) % 1000 == 0 ? (endTime - startTime) / 1000
								: (endTime - startTime) / 1000 + 1;
						url_crawler_consume_logger.info("outputvalve url_crawler_consume url:{} time:{}",url,result);
						logger.info("Write output object successfully, url:{}", url.getValue());
					}
					logger.info("Output item onto local directory successfully!");
				} catch (Exception e) {
					logger.error("Write output object failed::url:{}; exception:{}", url.getValue(), e);
					this.monitorService.incField(
							Constants.CRAWLER_MONITOR_FIELD_PREFIX_ERROR + String.valueOf(url.getTask().getTaskName()));
				}
			}
		}
	}

	private void writeOutputObjectActually(OutputObject oo) {
		OutputChannel channel = oo.getOutputChannel();
		boolean isImageDownload = false;
		if(DocType.INSERT.equals(oo.getDocType())){
			isImageDownload = true;
		}
		switch (channel) {
		case FILE:
			if(isImageDownload){
				this.handleImages(oo,outputServiceFile);
			}
			this.outputServiceFile.write(oo);
			break;
		case CONTROLLER:
			if(isImageDownload){
				this.handleImages(oo,outputServiceController);
			}
			this.outputServiceController.write(oo);
			break;
		case KAFKA:
			//竞品抓取时  图片先不做处理
			Map<String, String> newItem = oo.getNewItem();
			String retboby = newItem.get(JSON_BODY_KEY);//原有离线爬虫数据封装
			String tbRetboby = newItem.get(JSON_TBRETBODY_KEY);//淘宝全球购
			if(isImageDownload && (StringUtils.isNotBlank(retboby) || StringUtils.isNotBlank(tbRetboby))){
				this.handleImages(oo,outputServiceKafka);
			}
			this.outputServiceKafka.write(oo);
			break;
		case MAIL:
			this.outputServiceMail.write(oo);
			break;
		default:
			throw new OutputException(CrawlerExceptionCode.UNKNOWN_ERROR,
					"No valid configured output channel:" + channel.toString());
		}

	}

	private void handleImages(OutputObject oo, AbstractOutputService outputService) {
		long start = System.currentTimeMillis();
		Map<String, List<Image>> images = oo.getImages();
		for (Map.Entry<String, List<Image>> entry : images.entrySet()) {
			List<Image> imageSet = entry.getValue();
			// String skuId = entry.getKey();
			List<ImageDO> imageDoList = new ArrayList<ImageDO>();
			for (Image image : imageSet) {
				outputService.createImageRepertoryUrl(image, oo);
				//if (!outputService.existInRepertory(image)) {// 图片在图片库中不存在,才执行实际的下载和上传过程
				boolean srcExist = true;
				try {
				    String cdn_key = StringUtils.substringBetween(image.getRepertoryUrl(), "/prodimage/", ".jpg");
					srcExist = imageService.isCdnKeyExist(Long.valueOf(oo.getTaskId()), cdn_key);
				} catch (Throwable e) {
					e.printStackTrace();
					if (e instanceof ConnectException
							|| e instanceof NoRouteToHostException
							|| e instanceof SocketTimeoutException
							|| e instanceof UnknownHostException
							|| e instanceof SocketException) {
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					srcExist = outputService.existInRepertory(image);
					logger.error("Error url {} ,taskId {},src image {},srcExist {}",oo.getUrl().toString(),oo.getTaskId(),image.getOriginalUrl(),srcExist);
				}
				logger.info("Info url {} ,taskId {},src image {},srcExist {}",oo.getUrl().toString(),oo.getTaskId(),image.getOriginalUrl(),srcExist);
				if(!srcExist){
					for (int i = 0; i < IMAGE_DOWNLOAD_RETRY_TIME; i++) {
						this.downloadImageData(image, oo);// 下载图片的逻辑都是一样的
						if (StringUtils.isNotBlank(image.getOriginalUrl()) && ArrayUtils.isNotEmpty(image.getData())) {
							break;
						}
						try {
							Thread.sleep(i*1000);//downloading picture
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					outputService.uploadImage(image, oo);
					ImageDO imageDO = new ImageDO();
					imageDO.setDoc_id(SpiderStringUtil.md5Encode(oo.getUrl().toString()));
					imageDO.setSrc(image.getOriginalUrl());
					imageDO.setSrc_key(SpiderStringUtil.md5Encode(image.getOriginalUrl()));
					imageDO.setCdn(image.getRepertoryUrl());
					imageDO.setCdn_key(StringUtils.substringBetween(image.getRepertoryUrl(), "/prodimage/", ".jpg"));
					imageDO.setStatus("1");//已下载
					imageDO.setCreate_time(System.currentTimeMillis());
					imageDoList.add(imageDO);
				}
			}
			
			try {
				//save images
				imageService.saveImages(Long.valueOf(oo.getTaskId()), imageDoList);
			} catch (Exception e) {
				//e.printStackTrace();
				if (e instanceof ConnectException
						|| e instanceof NoRouteToHostException
						|| e instanceof SocketTimeoutException
						|| e instanceof UnknownHostException
						|| e instanceof SocketException) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		long end = System.currentTimeMillis();
		logger.info("Write output object successfully, url:{},consume time:{}", oo.getUrl().getValue(), (end - start));
	}

	private void downloadImageData(Image image, OutputObject oo) {
		try {
			byte[] imageData = null;
			String proxyLocaleId = oo.getUrl().getTask().getProxyRegionId();
			if (StringUtils.isNotBlank(proxyLocaleId)) {// 使用代理
				Proxy proxy = ProxyCache.getInstance().pickup(proxyLocaleId, true);
				imageData = Crawler.create().retry(3).timeOut(60000).url(image.getOriginalUrl())
						.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(proxy.getIp())
						.proxyPort(proxy.getPort()).resultAsBytes();
			} else {// 不使用代理
				Map<String,Object> headers = new HashMap<String,Object>();
				if(StringUtils.contains(oo.getUrl().toString(), "victoriassecret.com")){
					headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
					headers.put("Upgrade-Insecure-Requests", "1");
					headers.put("Accept", "image/webp,image/*,*/*;q=0.8");
					headers.put("Referer", oo.getUrl().toString());
				} else if(StringUtils.contains(oo.getUrl().toString(), "amazon.com")){
					headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
					headers.put("Upgrade-Insecure-Requests", "1");
					headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					headers.put("Host", "images-na.ssl-images-amazon.com");
					headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
					headers.put("Cache-Control", "max-age=0");
					headers.put("Connection", "keep-alive");
				}
				for(int i = 0 ; i < 20; i++){
					try {
						imageData = Crawler.create().retry(1).timeOut(60000)
								.url(image.getOriginalUrl()).header(headers)
								.method(HttpMethod.GET.getValue())
								.resultAsBytes();
						if (imageData != null) {
							break;
						}
						Thread.sleep(10000);
					} catch (IOException e) {
						// ConnectException | NoRouteToHostException |
						// SocketTimeoutException e
						if (e instanceof ConnectException
								|| e instanceof NoRouteToHostException
								|| e instanceof SocketTimeoutException
								|| e instanceof UnknownHostException
								|| e instanceof SocketException) {
							Thread.sleep(10000);
							continue;
						}
						throw e;
					}
				}
				
			}
			image.setData(imageData);
		} catch (Throwable e) {
			logger.error("Error downloading image,image-original-url:{}, e:{}", image.getOriginalUrl(), e);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	public OutputServiceFile getOutputServiceFile() {
		return outputServiceFile;
	}

	public void setOutputServiceFile(OutputServiceFile outputServiceFile) {
		this.outputServiceFile = outputServiceFile;
	}

	public OutputServiceController getOutputServiceController() {
		return outputServiceController;
	}

	public void setOutputServiceController(OutputServiceController outputServiceController) {
		this.outputServiceController = outputServiceController;
	}

	public OutputServiceKafka getOutputServiceKafka() {
		return outputServiceKafka;
	}

	public void setOutputServiceKafka(OutputServiceKafka outputServiceKafka) {
		this.outputServiceKafka = outputServiceKafka;
	}

	public OutputServiceMail getOutputServiceMail() {
		return outputServiceMail;
	}

	public void setOutputServiceMail(OutputServiceMail outputServiceMail) {
		this.outputServiceMail = outputServiceMail;
	}

	public ImageService getImageService() {
		return imageService;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
}