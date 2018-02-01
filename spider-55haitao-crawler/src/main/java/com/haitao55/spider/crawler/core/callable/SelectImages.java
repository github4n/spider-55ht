package com.haitao55.spider.crawler.core.callable;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：提供图片urls的选取功能；至于图片数据的实际下载功能,则留待最后的写数据时再进行；
 * 
 * @author Arthur.Liu
 * @time 2016年8月24日 下午3:04:04
 * @version 1.0
 */
public class SelectImages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private String css;
	private String attr;

	public void invoke(Context context) throws Exception {
		Document doc = super.getDocument(context);
		Elements elements = doc.select(this.css);
		if (CollectionUtils.isEmpty(elements)) {
			logger.warn("Images got no elements,url:{},css:{}", context.getUrl().getValue(), this.css);
			return;
		}

		List<String> imageUrls = this.pickupImageUrls(elements);

		// 只有在imageUrls不为空且regex不为空时,才改装imageUrls
		if (CollectionUtils.isNotEmpty(imageUrls) && StringUtils.isNotBlank(getRegex())) {
			imageUrls = this.reformStrings(imageUrls);
		}

		List<Image> images = new ArrayList<Image>(imageUrls.size());
		for (String reformedImageUrl : imageUrls) {
			Image image = new Image(reformedImageUrl);
			images.add(image);
		}
		//todo 目前图片是map集合
		//context.getUrl().getImages().addAll(images);
	}

	private List<String> pickupImageUrls(Elements elements) {
		List<String> imageUrls = new ArrayList<String>();

		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				String imgUrl = element.attr(attr);

				if (!StringUtils.startsWithIgnoreCase(imgUrl, "http")
						&& (StringUtils.equalsIgnoreCase("img", element.tagName())
								|| StringUtils.equalsIgnoreCase("a", element.tagName()))) {// 如果不是绝对地址(是相对地址)
					imgUrl = element.absUrl(attr);// 获取绝对地址
				}

				imageUrls.add(imgUrl);
			}
		}

		return imageUrls;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}
}