package com.haitao55.spider.crawler.service;

import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;

public abstract class AbstractOutputService implements OutputService {
	
	public abstract boolean existInRepertory(Image image);
	public abstract void uploadImage(Image image,OutputObject oo);
	public abstract void createImageRepertoryUrl(Image image,OutputObject oo);
}
