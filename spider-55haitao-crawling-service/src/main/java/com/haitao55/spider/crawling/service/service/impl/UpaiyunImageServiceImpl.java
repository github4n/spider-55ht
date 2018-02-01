package com.haitao55.spider.crawling.service.service.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawling.service.service.UpaiyunImageService;
import com.haitao55.spider.crawling.service.utils.Constants;
import com.haitao55.spider.crawling.service.utils.HandleImagesUtil;

import main.java.com.UpYun;

public class UpaiyunImageServiceImpl implements UpaiyunImageService{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_CRAWLING_SERVICE);
	
	private static final int IMAGE_DOWNLOAD_RETRY_TIME = 3;
	
	private static final String UPYUN_DIR = "/prodimage/";
	
	private UpYun upyun;
	
	private String upyunAddress;
	
	public String getUpyunAddress() {
		return upyunAddress;
	}

	public void setUpyunAddress(String upyunAddress) {
		this.upyunAddress = upyunAddress;
	}
	
	public UpYun getUpyun() {
		return upyun;
	}

	public void setUpyun(UpYun upyun) {
		this.upyun = upyun;
	}
	
	
	@Override
	public boolean existInRepertory(Image image) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void uploadImage(Image image) {
		upyun.setTimeout(60);
		upyun.setApiDomain(UpYun.ED_AUTO);
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		boolean  result = false;
		for(int i =0; i < 5; i++){
			try{
				result = this.upyun.writeFile(path, image.getData());
			}catch(Throwable e){
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(result){
				image.setData(null);
				break;
			}
		}
	}

	@Override
	public void createImageRepertoryUrl(Image image) {
		if (image == null) {
			logger.error("image is null while fill image-url");
			return;
		}
		String repertoryImageUrl = (new StringBuilder())
				.append(this.getRepertoryImageAddressPrefix())
				.append(UPYUN_DIR)
				.append(SpiderStringUtil.upYunFileName(image.getOriginalUrl()))
				.append(this.getRepertoryImageAddressSuffix()).toString();
		image.setRepertoryUrl(repertoryImageUrl);
		
	}

	private String getRepertoryImageAddressPrefix() {
		return upyunAddress;
	}
	
	private String getRepertoryImageAddressSuffix() {
		return ".jpg";
	}

	@Override
	public void handleImageUrl(Image image) {
		
		this.createImageRepertoryUrl(image);
		
		for (int i = 0; i < IMAGE_DOWNLOAD_RETRY_TIME; i++) {
			HandleImagesUtil.downloadImageData(image);
			if (StringUtils.isNotBlank(image.getOriginalUrl()) && 
					ArrayUtils.isNotEmpty(image.getData())) {
				break;
			}
			try {
				Thread.sleep(i*1000);//downloading picture
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.uploadImage(image);
	}
	
}
