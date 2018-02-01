/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: Escentual.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.haitao55.spider.crawler.core.callable.custom 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月19日 下午1:53:08 
 * @version: V1.0   
 */
package com.haitao55.spider.crawler.core.callable.custom.escentual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * @ClassName: Escentual
 * @Description: Escentual网站抓取数据
 * @author: zhoushuo
 * @date: 2016年10月19日 下午1:53:08
 */
public class Escentual extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.escentual.com";

	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content);

			String unit = "";
			String title = "";
			String brand = "";
			int spuStock = 0;

			String temp = context.getCurrentUrl();
			if (temp.endsWith("/")) {
				temp = temp.substring(0, temp.length() - 1);
			}
			String productId = temp.substring(temp.lastIndexOf("/") + 1);
			String docid = SpiderStringUtil.md5Encode(domain + productId);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());

			Elements etitle = document.select("div.breadcrumbs > ul > li.product>strong");
			if (etitle != null && etitle.size() > 0) {
				title = etitle.get(0).text().trim();
			}
			Elements eBrand = document.select("div.product-name>h1 .name-brand");
			if (eBrand != null && eBrand.size() > 0) {
				brand = eBrand.get(0).text().trim();
			}
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			Elements ebread = document.select("div.breadcrumbs > ul > li > a span");
			for (Element e : ebread) {
				breads.add(e.text());
				categories.add(e.text());
			}
			breads.add(title);
			categories.add(title);

			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements es = document.select("div.main-description > p");
			StringBuilder sb = new StringBuilder();
			if (es != null && es.size() > 0) {
				int count = 1;
				for (Element e : es) {
					featureMap.put("feature-" + count, e.text());
					count++;
					sb.append(e.text());
				}
			}
			descMap.put("en", sb.toString());

			List<Picture> pl = new ArrayList<>();
			Elements ePictures = document.select("a.cloud-zoom-gallery");
			if (ePictures != null && ePictures.size() > 0) {
				for (Element e : ePictures) {
					pl.add(new Picture(e.attr("abs:href"), ""));
				}
			}
			LImageList image_list = new LImageList(pl);

			String style_cate_name = "Size";
			Elements eStyle_cate_name = document.select("table#super-product-table thead tr th:nth-child(2)");
			if (CollectionUtils.isNotEmpty(eStyle_cate_name))
				style_cate_name = eStyle_cate_name.get(0).ownText().trim();
			// 设置sku
			List<LSelectionList> l_selection_list = new ArrayList<>();
			List<LStyleList> l_style_list = new ArrayList<>();
			Elements eSku = document.select("table#super-product-table tbody");
			if (CollectionUtils.isNotEmpty(eSku))
				eSku = eSku.get(0).getElementsByAttributeValue("itemprop", "offers");
			int count = 0;
			for (Element e : eSku) {
				float nowPrice = 0f;
				float oldPrice = 0f;
				String stock = "";
				int sku_stock_status = 0;
				int sku_stock_number = 0;
				String style_switch_img = e.child(0).getElementsByTag("img").get(0).absUrl("src");
				String size = e.child(1).ownText();
				if (StringUtils.isBlank(size)) {
					size = e.child(1).select("span.name-size").get(0).ownText().trim();
				}
				String skuid = e.child(1).getElementsByAttributeValue("class", "product-code").get(0).ownText()
						.replace("Item#", "").trim();
				Elements tempNowPrice = e.child(2).getElementsByAttributeValue("class", "special-price");
				if (tempNowPrice == null || tempNowPrice.size() == 0)
					tempNowPrice = e.child(2).getElementsByAttributeValue("class", "regular-price");
				if (tempNowPrice != null && tempNowPrice.size() > 0) {
					tempNowPrice = tempNowPrice.get(0).getElementsByAttributeValue("class", "price");
					if (tempNowPrice != null && tempNowPrice.size() > 0) {
						Map<String, Object> map = formatPrice(tempNowPrice.get(0).ownText().trim());
						nowPrice = (float) map.get("price");
						unit = map.get("unit").toString();
					}
				}
				Elements tempOldPrice = e.child(2).getElementsByAttributeValue("class", "old-price");
				if (tempOldPrice != null && tempOldPrice.size() > 0) {
					tempOldPrice = tempOldPrice.get(0).getElementsByAttributeValue("class", "price");
					if (tempOldPrice != null && tempOldPrice.size() > 0) {
						Map<String, Object> map = formatPrice(tempOldPrice.get(0).ownText().trim());
						oldPrice = (float) map.get("price");
						unit = map.get("unit").toString();
					}
				} else {
					oldPrice = nowPrice;
				}
				int save = Math.round((1 - nowPrice / oldPrice) * 100);

				Elements stocks = e.child(4).getElementsByAttributeValue("class", "stock-status-main");
				if (stocks != null && stocks.size() > 0) {
					stock = stocks.get(0).ownText().trim();
					if (stock.contains("More than")) {
						sku_stock_status = 1;
						sku_stock_number = 20;
					} else if (stock.contains("in stock")) {
						Pattern pattern = Pattern.compile("(\\d+)");
						Matcher matcher = pattern.matcher(stock);
						if (matcher.find()) {
							sku_stock_number = Integer.parseInt(matcher.group());
						}
						sku_stock_status = 2;
					} else if (stock.contains("Out of stock")) {
						sku_stock_status = 0;
						sku_stock_number = 0;
					}
				}

				LSelectionList lsl = new LSelectionList();
				List<Selection> selections = new ArrayList<>();
				Selection selection = new Selection(0, size, style_cate_name);
				selections.add(selection);
				lsl.setGoods_id(skuid);
				lsl.setOrig_price(oldPrice);
				lsl.setPrice_unit(unit);
				lsl.setSale_price(nowPrice);
				lsl.setStyle_id("default");
				lsl.setStock_number(sku_stock_number);
				lsl.setStock_status(sku_stock_status);
				lsl.setSelections(selections);
				l_selection_list.add(lsl);

				if (count == 0) {
					LStyleList ll = new LStyleList();
					ll.setStyle_switch_img(style_switch_img);
					ll.setGood_id(skuid);
					ll.setStyle_id("default");
					ll.setStyle_cate_id(0);
					ll.setStyle_cate_name("color");
					ll.setStyle_name("default");
					ll.setDisplay(true);
					rebody.setPrice(new Price(oldPrice, save, nowPrice, unit));// 设置第一个sku的价格为默认价格
					// rebody.setStock(new Stock(sku_stock_status));//
					// 设置第一个sku的库存为默认库存
					List<Image> picsPerSku = getPicsByStyleId(pl);
					context.getUrl().getImages().put(skuid + "", picsPerSku);
					l_style_list.add(ll);
				}
				count++;
			}

			for (LSelectionList ll : l_selection_list) {
				int sku_stock = ll.getStock_status();
				if (sku_stock != 0)
					spuStock = 2;
				if (sku_stock == 1) {
					spuStock = 1;
					break;
				}
			}

			Map<String, Object> properties = new HashMap<>();
			if (categories != null && categories.size() > 0) {
				for (String cat : categories) {
					if (StringUtils.isNotBlank(cat)) {
						if ("For Men".equals(cat.trim()) || "Men".equals(cat.trim()) || "For Him".equals(cat.trim())) {
							properties.put("s_gender", "men");
							break;
						}
						if ("For Women".equals(cat.trim()) || "For Her".equals(cat.trim())) {
							properties.put("s_gender", "women");
							break;
						}
					}
				}
			}
			if (properties.get("s_gender") == null) {
				properties.put("s_gender", "");
			}
			// 设置rebody
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title.trim(), "", "", ""));
			rebody.setBrand(new Brand(brand, "", "", ""));
			rebody.setStock(new Stock(spuStock)); // 设置spu库存状态
			rebody.setBreadCrumb(breads);
			rebody.setCategory(categories);
			rebody.setFeatureList(featureMap);
			rebody.setDescription(descMap);
			rebody.setImage(image_list);
			rebody.setSku(new Sku(l_selection_list, l_style_list));
			rebody.setProperties(properties);
		}
		setOutput(context, rebody);
	}

	private Map<String, Object> formatPrice(String tempPrice) {
		Map<String, Object> map = new HashMap<>();
		String currency = StringUtils.substring(tempPrice, 0, 1);
		String unit = Currency.codeOf(currency).name();
		tempPrice = StringUtils.substring(tempPrice, 1);
		map.put("unit", unit);
		try {
			float price = Float.parseFloat(tempPrice);
			map.put("price", price);
		} catch (NumberFormatException e) {
			logger.error("Format Price Error.", e);
		}
		return map;
	}

	private List<Image> getPicsByStyleId(List<Picture> list) {
		List<Image> pics = new ArrayList<Image>();
		if (list != null && list.size() > 0) {
			for (Picture pic : list) {
				String imageUrl = pic.getSrc();
				if (StringUtils.isNotBlank(imageUrl)) {
					Image image = new Image(imageUrl);
					pics.add(image);
				}
			}
		}
		return pics;
	}
}
