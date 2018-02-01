package com.haitao55.spider.crawler.core.callable.custom.pixiemarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年4月27日 下午3:27:00  
 */
public class Pixiemarket extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.pixiemarket.com";

	private static final String CSS_TITLE = "div.product-name>span.h1";
	private static final String CSS_BREADS = "div.breadcrumbs li a";
	private static final String CSS_DESCRIPTION = "div.tab-content div.std p";
	private static final String CSS_DETAIL = "not require";
	private static final String CSS_INVENTORY = "div.extra-info p.in-stock span.value";
	private static final String CSS_IMAGES = "div.more-views ul.product-image-thumbs li a img";
	
	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Sale", "Women's Final Sale" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Sale", "Men's Final Sale" };


	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = CurlCrawlerUtil.get(context.getCurrentUrl());
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = CurlCrawlerUtil.get(context.getCurrentUrl(), 30, proxyAddress, proxyPort);
		}
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);
			
			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = CrawlerUtils.getProductId(currentUrl);
			if (StringUtils.isBlank(productID)) {
				logger.error("get productID error");
				return;
			}
			if (StringUtils.isBlank(title)) {
				logger.error("get title error");
				return;
			}
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);

			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			CrawlerUtils.setDescription(featureMap, descMap, document, CSS_DESCRIPTION, CSS_DETAIL);

			float spu_orig_price = 0l;
			float spu_sale_price = 0l;
			String spu_Unit = "";
			int spu_stock_status = 0;
			
			Elements tempNowPrice = document.select("div.product-shop div.price-info div.price-box span.regular-price span.price");
			if(CollectionUtils.isEmpty(tempNowPrice))
				tempNowPrice = document.select("div.product-shop div.price-info div.price-box p.special-price span.price");
			if(CollectionUtils.isNotEmpty(tempNowPrice)){
				spu_sale_price = CrawlerUtils.getPrice(tempNowPrice.get(0).text().trim(), currentUrl, logger);
				spu_Unit = CrawlerUtils.getUnit(tempNowPrice.get(0).text().trim(), currentUrl, logger);
			}
			Elements tempOldPrice = document.select("div.product-shop div.price-info div.price-box p.old-price span.price");
			if(CollectionUtils.isNotEmpty(tempOldPrice)){
				spu_orig_price = CrawlerUtils.getPrice(tempOldPrice.get(0).text().trim(), currentUrl, logger);
			} else {
				spu_orig_price = spu_sale_price;
			}
			
			String orig = document.select(".product-shop .price-info .price-box .compare_at_price").text();
			if(StringUtils.isNotBlank(orig)){
				orig = orig.replaceAll("[$, ]", "");
				spu_orig_price = Float.parseFloat(orig);
			}
			
			int save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
			
			//设置spu库存
			Elements es = document.select(CSS_INVENTORY);
			if(CollectionUtils.isNotEmpty(es)){
				String inventoryMsg = StringUtils.trim(es.get(0).text());
				if(StringUtils.containsIgnoreCase(inventoryMsg, "In stock"))
					spu_stock_status = 1;
			}

			Map<String, Object> properties = new HashMap<>();
			// 设置性别
			CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);

			// 设置图片
			List<String> imgs = new ArrayList<>();
			Elements epictures = document.select(CSS_IMAGES);
			if(CollectionUtils.isNotEmpty(epictures)){
				for(Element e : epictures) {
					imgs.add(StringUtils.trimToEmpty(e.absUrl("src")));
				}
			}

			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
	
			String productJson = StringUtils.substringBetween(content, "product: ", "onVariantSelected");
			if(StringUtils.isNotBlank(productJson)){
				productJson = productJson.trim();
				productJson = productJson.substring(0,productJson.length()-1);
				JSONObject json = JSONObject.parseObject(productJson);
				productID = json.getString("id");
				docid = SpiderStringUtil.md5Encode(domain + productID);
				
				JSONArray variants = json.getJSONArray("variants");
				if(variants == null) {
					logger.error("get size error ang url is {}", currentUrl);
					return;
				}
				if(variants != null && variants.size() != 0){
					int count = 0;
					int len = variants.size();
					for(int i=0; i<len; i++){
						int sku_stock_status = 0;
						int sku_stock_number = 0;
						String style_switch_img = "";
						String size = variants.getJSONObject(i).getString("option1");
						String skuid = variants.getJSONObject(i).getString("id");
						String instock = variants.getJSONObject(i).getString("available");
						
						if(!instock.equals("false"))
							sku_stock_status = 1;
						
						LSelectionList lsl = new LSelectionList();
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection(0, size, "size");
						selections.add(selection);
						lsl.setGoods_id(skuid);
						lsl.setPrice_unit(spu_Unit);
						lsl.setSale_price(spu_sale_price);
						lsl.setOrig_price(spu_orig_price);
						if(spu_orig_price < spu_sale_price)
							lsl.setOrig_price(spu_sale_price);
						lsl.setStyle_id("default");
						lsl.setStock_number(sku_stock_number);
						lsl.setStock_status(sku_stock_status);
						lsl.setSelections(selections);
						l_selection_list.add(lsl);
						
						LStyleList ll = new LStyleList();
						if(count == 0){
							ll.setStyle_switch_img(style_switch_img);
							ll.setGood_id(skuid);
							ll.setStyle_id("default");
							ll.setStyle_cate_id(0);
							ll.setStyle_cate_name("color");
							ll.setStyle_name("default");
							ll.setDisplay(true);
							context.getUrl().getImages().put(skuid, CrawlerUtils.convertToImageList(imgs));
							l_style_list.add(ll);
						}
						count++;
					}
				}
			} else {
				context.getUrl().getImages().put(productID, CrawlerUtils.convertToImageList(imgs));
			}
			
			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand("", "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setPrice(new Price(spu_orig_price, save, spu_sale_price,
			 spu_Unit));
			retbody.setStock(new Stock(spu_stock_status));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		setOutput(context, retbody);
	}
}
