package com.haitao55.spider.realtime.service.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.realtime.service.OutputDataDealService;

import main.java.com.UpYun;
public class OutputDataDealServiceImpl implements OutputDataDealService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final String UPYUN_DIR = "/prodimage/";
	private UpYun upyun;
	public UpYun getUpyun() {
		return upyun;
	}

	public void setUpyun(UpYun upyun) {
		this.upyun = upyun;
	}
	
	private String upyunAddress;
	
	public String getUpyunAddress() {
		return upyunAddress;
	}

	public void setUpyunAddress(String upyunAddress) {
		this.upyunAddress = upyunAddress;
	}
	@Override
	public boolean existInRepertory(Image image) {
		String path=StringUtils.substring(image.getRepertoryUrl(), StringUtils.indexOf(image.getRepertoryUrl(), UPYUN_DIR));
		Map<String, String> fileInfo = upyun.getFileInfo(path);
		if(null==fileInfo||fileInfo.isEmpty()){
			return false;//不存在
		}
		return true;//存在
	}

	@Override
	public void uploadImage(Image image, OutputObject oo) {
		upyun.setTimeout(30);
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
				break;
			}
		}

	}

	@Override
	public void createImageRepertoryUrl(Image image, OutputObject oo) {
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

}
