package com.haitao55.spider.realtime.service;

import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;

public interface OutputDataDealService {
	public boolean existInRepertory(Image image);
	public void uploadImage(Image image,OutputObject oo);
	public void createImageRepertoryUrl(Image image,OutputObject oo);
}
