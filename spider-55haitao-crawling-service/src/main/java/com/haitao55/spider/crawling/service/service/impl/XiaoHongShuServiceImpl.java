package com.haitao55.spider.crawling.service.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.xiaohongshu.XiaoHongShu;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.JsoupUtils;
import com.haitao55.spider.crawling.service.service.UpaiyunImageService;
import com.haitao55.spider.crawling.service.service.XiaoHongShuService;

/**
 * 收录小红书帖子信息
 * date : 2017-3-24
 * @author denghuan
 *
 */

@Service("xiaoHongShuService")
public class XiaoHongShuServiceImpl implements XiaoHongShuService{
	
	private static final Logger logger = LoggerFactory.getLogger(XiaoHongShuServiceImpl.class);
	
	private UpaiyunImageService upaiyunImageService;


	public UpaiyunImageService getUpaiyunImageService() {
		return upaiyunImageService;
	}
	public void setUpaiyunImageService(UpaiyunImageService upaiyunImageService) {
		this.upaiyunImageService = upaiyunImageService;
	}


	@Override
	public XiaoHongShu crawlItemInfo(String url) {
		long startTime = System.currentTimeMillis();
		XiaoHongShu xhs = new XiaoHongShu();
		try {
			String	html = Crawler.create().timeOut(30000).retry(3).url(url).resultAsString();
			Document doc = JsoupUtils.parse(html);
			List<Image> images = new ArrayList<Image>();
			Elements imageEs = doc.select(".goods-images-list .image-wrap h3 img");
			for(Element e : imageEs){
				String image = e.attr("data-src");
				if(StringUtils.isNotBlank(image)){
					images.add(new Image(image));
				}
			}
			List<Picture> spuImage = new ArrayList<Picture>();
			if(CollectionUtils.isNotEmpty(images)){
				for(Image image : images){
					upaiyunImageService.handleImageUrl(image);
					spuImage.add(new Picture(image.getRepertoryUrl()));
				}
				xhs.setImage(new LImageList(spuImage));
			}
			
			String headImage = doc.select(".user-info #note_user_img a img").attr("src");
			if(StringUtils.isNotBlank(headImage)){
				Image image = new Image(headImage);
				upaiyunImageService.handleImageUrl(image);//处理头像图片
				xhs.setHeadImage(image.getRepertoryUrl());
			}
			
			String nickName = doc.select(".user-info .user-name a").text();
			
			String title = doc.select(".note-desc h2.title").text();
			
			String text = doc.select(".note-desc p.j_goods_desc").text();
			
			xhs.setNickName(nickName);
			xhs.setProductUrl(url);
			xhs.setText(text);
			xhs.setTitle(title);
		} catch (Exception httpException) {
			logger.error("url {} xiaHongShu crawler exception:{}",url, httpException.getMessage());
			return null;
		}
		logger.info("crawler xiaHongShu item success -> ,url:{} , time : {}",url,System.currentTimeMillis()-startTime);
		
		return xhs;
	}
}
