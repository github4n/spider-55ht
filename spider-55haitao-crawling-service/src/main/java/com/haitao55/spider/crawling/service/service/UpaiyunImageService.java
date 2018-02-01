package com.haitao55.spider.crawling.service.service;

import com.haitao55.spider.crawler.core.model.Image;

/**
 * Upaiyun 图片处理service
 * @author denghuan
 *
 */
public interface  UpaiyunImageService {

	public boolean existInRepertory(Image image);
	public void uploadImage(Image image);
	public void createImageRepertoryUrl(Image image);
	public void handleImageUrl(Image image);
}
