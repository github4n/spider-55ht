package com.test.mankind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月23日 下午2:23:57  
 */
public class CrawlerUtils {
	
	public static final String KEY_UNIT = "unit";
	public static final String KEY_PRICE = "price";
	
	// 设置标题：在页面利用css选择的
	public static String setTitle(Document document, String css, String url, Logger logger){
		Elements etitle = document.select(css);
		if (CollectionUtils.isNotEmpty(etitle)) {
			return etitle.get(0).text().trim();
		} else {// 过滤掉没有Title的商品
			logger.error("Error while fetching url {} because of no title.", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching title with url " + url);
		}
	}
	
	//设置标题:从源码中截取的
	public static String setTitle(String title, String url, Logger logger){
		if(StringUtils.isNotBlank(title))
			return StringUtils.trim(title);
		else {// 过滤掉没有Title的商品
			logger.error("Error while fetching url {} because of no title.", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching title with url " + url);
		}
	}
	
	//设置面包屑和类别
	public static void setBreadAndCategory(List<String> breads, List<String> categories, Document document, String css, String title){
		Elements ebread = document.select(css);
		for (Element e : ebread) {
			breads.add(e.text());
			categories.add(e.text());
		}
		breads.add(title);
		categories.add(title);
	}
	
	//设置描述和feature
	public static synchronized void setDescription(Map<String, Object> featureMap, Map<String, Object> descMap, Document document, String css_desc, String css_detail){
		StringBuilder sb = new StringBuilder();
		Elements eDescriptions = document.select(css_desc);
		int count = 1;
		if (CollectionUtils.isNotEmpty(eDescriptions)) {
			for (Element e : eDescriptions) {
				String decs = StringUtils.trim(e.text());
				if(StringUtils.isNotBlank(decs)){
					featureMap.put("feature-" + count, decs);
					count++;
					sb.append(decs).append(".");
				}
			}
		}
		Elements eDetails = document.select(css_detail);
		if (CollectionUtils.isNotEmpty(eDetails)) {
			for (Element e : eDetails) {
				if (CollectionUtils.isNotEmpty(e.getElementsByAttributeValue("itemprop", "productID")))
					continue;
				featureMap.put("feature-" + count, e.text().trim());
				count++;
				sb.append(e.text().trim()).append(". ");
			}
		}
		descMap.put("en", sb.toString());
	}
	
	// 获取价格
	public static Map<String, Object> formatPrice(String tempPrice, String url, Logger logger) {
		tempPrice = trimStringIgnoreNull(tempPrice);
		if (tempPrice.contains("-")) {
			tempPrice = trimStringIgnoreNull(StringUtils.substringBefore(tempPrice, "-"));
		}
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isBlank(tempPrice)) {
			logger.error("input price is null and url:{}", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url);
		}
		String currency = StringUtils.substring(tempPrice, 0, 1);
		Currency cuy = Currency.codeOf(currency);
		if (cuy == null) {
			logger.error("currency is null and url:{}", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url);
		}
		map.put(KEY_UNIT, cuy.name());
		tempPrice = StringUtils.substring(tempPrice, 1).replace(",", "");
		try {
			float price = Float.parseFloat(tempPrice);
			price = formatNum(price);
			map.put(KEY_PRICE, price);
		} catch (NumberFormatException e) {
			logger.error("format price error and url is {},because of {}", url, e.getMessage());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url);
		}
		return map;
	}
	
	public static float formatNum(float num) {
		return ((float) Math.round(num * 100)) / 100; // 四舍五入法保留两位小数
	}

	// 避免直接使用trim()方法发生空指针异常
	public static String trimStringIgnoreNull(String string) {
		if (string == null)
			return StringUtils.EMPTY; // 这里不返回null的原因是，根据业务场景，不希望返回null
		return string.trim();
	}
	
	public static List<Image> convertToImageList(List<String> list) {
		List<Image> imgs = new ArrayList<Image>();
		if (CollectionUtils.isNotEmpty(list)) {
			for (String str : list) {
				if (StringUtils.isNotBlank(str)) {
					Image image = new Image(str);
					imgs.add(image);
				}
			}
		}
		return imgs;
	}
}
