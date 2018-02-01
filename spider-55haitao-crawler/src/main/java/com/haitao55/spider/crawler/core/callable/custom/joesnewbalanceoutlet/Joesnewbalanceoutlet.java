package com.haitao55.spider.crawler.core.callable.custom.joesnewbalanceoutlet;

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
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.exception.CrawlerException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月29日 上午11:09:49  
 */
public class Joesnewbalanceoutlet extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.joesnewbalanceoutlet.com";

	private static final String CSS_TITLE = "#DetailsHeading h1";
	private static final String CSS_PRODUCTID = "#Sku";
	private static final String CSS_BRAND = "#Brand";
	private static final String CSS_BREADS = "#Breadcrumbs a";
	private static final String CSS_DESCRIPTION = "div.hideOnMobile div#Description p.productFeaturesDetails";
	private static final String CSS_DETAIL = "ul.productFeaturesDetails li";
	private static final String CSS_IMAGE_LIST = "div.swiper-wrapper div.swiper-slide a";
	private static final String CSS_ORIGPRICE = "div#Price span.origPrice>span";
	private static final String CSS_SALEPRICE = "div#Price span.productPrice";
	private static final String QUERY_INVENTORY_URL = "https://www.joesnewbalanceoutlet.com/product/getInventoryQuantity";

	// 男性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls" };
	// 女性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys" };

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		String content = super.getInputString(context);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);

			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = CrawlerUtils.getProductId(currentUrl);
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);
			String brand = CrawlerUtils.getValueByAttr(document, CSS_BRAND, "value");

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);

			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			CrawlerUtils.setDescription(featureMap, descMap, document, CSS_DESCRIPTION, CSS_DETAIL);

			// 设置spu价格
			String origPriceStr = CrawlerUtils.getValueByAttr(document, CSS_ORIGPRICE, null);
			String salePriceStr = CrawlerUtils.getValueByAttr(document, CSS_SALEPRICE, null);
			if(StringUtils.isBlank(origPriceStr) && StringUtils.isNotBlank(salePriceStr))
				origPriceStr = salePriceStr;
			String spu_Unit = CrawlerUtils.getUnit(salePriceStr, currentUrl, logger);
			float orig_price = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
			float sale_price = CrawlerUtils.getPrice(salePriceStr, currentUrl, logger);
			int save = 0;
			if (orig_price < sale_price)
				orig_price = sale_price;
			if (orig_price != 0)
				save = Math.round((1 - sale_price / orig_price) * 100);
			else {
				logger.error("can not get the price, url is {}", currentUrl);
				new CrawlerException(CrawlerExceptionCode.PARSE_ERROR, "can not get the price, url is " + currentUrl);
			}
			// 设置spu图片
			List<String> imgs = new ArrayList<>();
			List<Picture> pl = new ArrayList<>();
			Elements ePictures = document.select(CSS_IMAGE_LIST);
			for (Element e : ePictures) {
				String img = StringUtils.trim(e.absUrl("href"));
				if (StringUtils.isNotBlank(img)) {
					pl.add(new Picture(img, ""));
					imgs.add(img);
				}
			}
			if(CollectionUtils.isEmpty(ePictures)){
				ePictures = document.select("span#ProductImage>img");
				for (Element e : ePictures) {
					String img = StringUtils.trim(e.absUrl("src"));
					if (StringUtils.isNotBlank(img)) {
						pl.add(new Picture(img, ""));
						imgs.add(img);
					}
				}
			}
			LImageList image_list = new LImageList(pl);

			Map<String, Object> properties = new HashMap<>();
			//设置性别
			Elements genderEs = document.getElementsByAttributeValue("name", "Gender");
			if(CollectionUtils.isNotEmpty(genderEs)) {
				String gender = genderEs.get(0).attr("value");
				if("Male".equals(StringUtils.trim(gender)))
					properties.put(CrawlerUtils.KEY_GENDER, CrawlerUtils.MEN);
				if("Female".equals(StringUtils.trim(gender)))
					properties.put(CrawlerUtils.KEY_GENDER, CrawlerUtils.WOMEN);
			}
			if(properties.get(CrawlerUtils.KEY_GENDER) == null)	
				CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);

			// 设置sku
			String keyWords = StringUtils.substringBetween(content, "sf.productDetail.init", "</script>");
			String color = StringUtils.substringBetween(keyWords, "custom.color\",\"v\":\"", "\"}");
			if(StringUtils.isBlank(color)){
				logger.error("color is empty. url:{}",context.getCurrentUrl());
				return;
			}
			color = Native2AsciiUtils.ascii2Native(color);
			List<JNSku> skus = new ArrayList<>();
			String skuStr = StringUtils.trim(StringUtils.substringBetween(keyWords, "variants:", "images"));
			if (StringUtils.isNotBlank(skuStr))
				skuStr = skuStr.replace("],", "]");
			JSONArray array = JSONArray.parseArray(skuStr);
			if (array != null && array.size() != 0) {
				for (int i = 0; i < array.size(); i++) {
					Object obj = array.getJSONObject(i).get("v");
					if (obj == null) {
						continue;
					}
					String skuid = obj.toString();
					Map<String, String> map = new HashMap<>();
					map.put("VariantID", skuid);
					String inventoryStr = StringUtils.trim(HttpClientUtil.post(QUERY_INVENTORY_URL, map));
					JNSku sku = new JNSku();
					sku.setSkuId(skuid);
					sku.setOriginal_price(orig_price);
					sku.setSale_price(sale_price);
					if (StringUtils.isNotBlank(inventoryStr)) {
						int inventory = Integer.parseInt(inventoryStr);
						sku.setInventory(inventory);
						if (inventory > 0) {
							sku.setStatus(2);
						}
					}
					JSONArray attrs = array.getJSONObject(i).getJSONArray("a");
					if(attrs != null){
						int len = attrs.size();
						for(int j=0; j<len; j++) {
							String name = attrs.getJSONObject(j).getString("n");
							String value = attrs.getJSONObject(j).getString("v");
							if("size".equals(StringUtils.trim(name).toLowerCase()))
								sku.setSize(StringUtils.trim(value));
							if("width".equals(StringUtils.trim(name).toLowerCase()))
								sku.setWidth(StringUtils.trim(value));
						}
					}
//					String[] strs = skuid.split("-");
//					if (strs.length > 1)
//						sku.setSize(strs[1]);
//					if (strs.length > 2)
//						sku.setWidth(strs[2]);
					skus.add(sku);
				}
			}

			int stock = 0;//spu库存状态
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(skus)) {
				LStyleList style = new LStyleList();
				style.setDisplay(true);
				style.setStyle_cate_id(0);
				style.setStyle_cate_name("Color");
				if (StringUtils.isNotBlank(color)) {
					style.setStyle_id(color);
					style.setStyle_name(color);
				} else {
					style.setStyle_id("Default");
					style.setStyle_name("Default");
				}
				style.setGood_id(skus.get(0).getSkuId());
				style.setStyle_switch_img("");
				context.getUrl().getImages().put(style.getGood_id(), CrawlerUtils.convertToImageList(imgs));
				l_style_list.add(style);

				for (JNSku sku : skus) {
					if (sku.getStatus() > 0)
						stock = 2;
					List<Selection> slist = new ArrayList<>();
					LSelectionList selection = new LSelectionList();
					if (StringUtils.isNotBlank(sku.getSize())) {
						Selection size_selection = new Selection(0, sku.getSize(), "Size");
						slist.add(size_selection);
					}
					if (StringUtils.isNotBlank(sku.getWidth())) {
						Selection width_selection = new Selection(0, sku.getWidth(), "Width");
						slist.add(width_selection);
					}

					selection.setGoods_id(sku.getSkuId());
					selection.setOrig_price(sku.getOriginal_price());
					selection.setPrice_unit(spu_Unit);
					selection.setSale_price(sku.getSale_price());
					selection.setStock_number(sku.getInventory());
					selection.setStock_status(sku.getStatus());
					selection.setStyle_id(color);
					selection.setSelections(slist);
					l_selection_list.add(selection);
				}
			} else {
				context.getUrl().getImages().put(productID, CrawlerUtils.convertToImageList(imgs));
				if ("display:none".equals(StringUtils.trim(CrawlerUtils.getValueByAttr(document, "div.outOfStock", "style"))))
					stock = 1;
			}
			
			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand(brand, "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setImage(image_list);
			retbody.setPrice(new Price(orig_price, save, sale_price, spu_Unit));
			retbody.setStock(new Stock(stock));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		setOutput(context, retbody);
	}
}
