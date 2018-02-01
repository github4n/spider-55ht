package com.haitao55.spider.crawler.core.callable.custom.mankind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import com.haitao55.spider.common.gson.bean.LImageList;
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
	public static final String KEY_GENDER = "s_gender";
	public static final String MEN = "men";
	public static final String WOMEN = "women";
	public static final String STYLE_CATE_NAME = "COLOR";
	
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
	
	public static String setProductID(Document document, String css, String url, Logger logger){
		Elements eproduct = document.select(css);
		String productId = null;
		if (CollectionUtils.isNotEmpty(eproduct)) 
			productId = StringUtils.trim(eproduct.get(0).text());
		if(StringUtils.isBlank(productId))
			productId = UUID.randomUUID().toString();
		return productId;
	}
	
	public static String getProductId(String currentUrl){
		if (currentUrl.endsWith("/")) {
			currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
		}
		return currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
	}
	
	/**
	 * @description 根据属性选择值
	 * @param document
	 * @param css
	 * @param attr
	 * @return return the match value. If not match,return empty.
	 */
	public static String getValueByAttr(Document document, String css, String attr){
		Elements es = document.select(css);
		String value = StringUtils.EMPTY;
		if(CollectionUtils.isNotEmpty(es)){
			if(StringUtils.isBlank(attr) || "text".equals(attr))
				value = StringUtils.trim(es.get(0).text());
			else
				value = StringUtils.trim(es.get(0).attr(attr));
		}
		return value;
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
	public static void setDescription(Map<String, Object> featureMap, Map<String, Object> descMap, Document document, String css_desc, String css_detail){
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
	
	//设置性别
	public static void setGender(Map<String, Object> properties, List<String> categories, String[] man_keywords, String[] woman_keywords){
		if (CollectionUtils.isNotEmpty(categories)) {
			gender: for (String cat : categories) {
				for (String male_key : man_keywords) {
					if (male_key.equals(cat.trim())) {
						properties.put(KEY_GENDER, MEN);
						break gender;
					}
				}
				for (String female_key : woman_keywords) {
					if (female_key.equals(cat.trim())) {
						properties.put(KEY_GENDER, WOMEN);
						break gender;
					}
				}
			}
		}
		if (properties.get(KEY_GENDER) == null)
			properties.put(KEY_GENDER, "");
	}
	
	//获取价格
	public static float getPrice(String priceStr, String url, Logger logger){
		return (float)formatPrice(priceStr, url, logger).get(KEY_PRICE);
	}
	//获取单位
	public static String getUnit(String priceStr, String url, Logger logger){
		return formatPrice(priceStr, url, logger).get(KEY_UNIT).toString();
	}
	
	//Elements转换成Document
	public static Document parseToDocument(Elements es){
		return Jsoup.parse(es.outerHtml());
	}
	
	public static LImageList getImageList(List<String> imgs){
		List<Picture> l_image_list = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(imgs)){
			for(String img : imgs){
				Picture pic = new Picture(img, "");
				l_image_list.add(pic);
			}
		}
		LImageList image_list = new LImageList(l_image_list);
		return image_list;
	}
	
	public static String getNumberFromString(String str) {
		if (StringUtils.isBlank(str))
			return str;
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find())
			return matcher.group();
		else
			return null;
	}
}
