package com.haitao55.spider.crawler.core.callable.custom.groupon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.exception.CrawlerException;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月12日 上午9:42:59  
 */
public class Groupon extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.groupon.com";

	private static final String CSS_TITLE = "h1#deal-title";
	private static final String CSS_BREADS = "section.breadcrumbs div a";
	private static final String CSS_DESCRIPTION = "h4";
	private static final String CSS_DETAIL = "li";
	private static final String CSS_ORIGPRICE = "div.price-and-value>span.discount-value";
	private static final String CSS_SALEPRICE = "div.price-and-value>span.price";

	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Fashion" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Fashion" };
	
	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		String content = super.getInputString(context);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);
			Elements es_offline = document.select("div#purchase-cluster div.buy>a>button");
			if(CollectionUtils.isEmpty(es_offline))
				es_offline = document.select("div.reason");
			if(CollectionUtils.isNotEmpty(es_offline)){
				String offlineMsg = StringUtils.trim(es_offline.get(0).text());
				if("Not yet available".equals(offlineMsg) || "No Longer Available".equals(offlineMsg) || "Sold Out".equals(offlineMsg)) {
					logger.info("this goods is offline and url is {}", currentUrl);
//					throw new ParseException(CrawlerExceptionCode.OFFLINE,
//							"this goods is offline and url is " + currentUrl);
				}
			}
			
			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = CrawlerUtils.getProductId(currentUrl);
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);
			
			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);
			
			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements es = document.getElementsByAttributeValue("itemprop", "description");
			CrawlerUtils.setDescription(featureMap, descMap, CrawlerUtils.parseToDocument(es), CSS_DESCRIPTION, CSS_DETAIL);
			if(featureMap.size() == 0 && CollectionUtils.isNotEmpty(es)){
				featureMap.put("feature-1", es.get(0).text());
				descMap.put("en", es.get(0).text());
			}
			
			float orig_price = 0f;
			float sale_price = 0f;
			int save = 0;
			String spu_Unit = "";
			int spu_stock_status = 0;
			
			Map<String, Object> properties = new HashMap<>();
			//设置性别
			CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);
			
			//设置SKU
			List<GroupOnSku> skus = new ArrayList<>();
			List<ColorImg> colorimgs = new ArrayList<>();
			List<String> noMappingsImgs = null;//部分商品没有颜色和图片的映射关系，则为每一种颜色都配上该图片
			String skuString = StringUtils.substringBetween(content, "window.payload =", "};");
			skuString = skuString+"}";
			if(StringUtils.isNotBlank(skuString)){
				JSONObject json = null;
				try {
					json = JSONObject.parseObject(StringUtils.trim(skuString));
				} catch (Exception e) {
					logger.error("get sku json error. url is {}",currentUrl);
					return;
				}
				JSONObject skuJsonContain = json.getJSONObject("variations");
				JSONObject colorImgsJsonContain = json.getJSONObject("carousel");
				if(colorImgsJsonContain != null){
					JSONArray colorImgsArray = colorImgsJsonContain.getJSONArray("dealImages");
					if(colorImgsArray != null){
						int len = colorImgsArray.size();
						for(int i=0; i<len; i++){
							ColorImg colorimg = new ColorImg();
							List<String> imgs = new ArrayList<>();
							JSONObject imgJson = colorImgsArray.getJSONObject(i);
							colorimg.setBaseUrl(StringUtils.trimToEmpty(imgJson.getString("baseURL")));
							String img = StringUtils.trimToEmpty(imgJson.getString("media"));
							if(StringUtils.isNotBlank(img))
								imgs.add(img);
							colorimg.setImgs(imgs);
							colorimgs.add(colorimg);
						}
					}
				}
				if(skuJsonContain != null){
					JSONArray skuJsonArray = skuJsonContain.getJSONArray("options");
					if(skuJsonArray != null){
						int len = skuJsonArray.size();
						for(int i=0; i < len; i++){
							GroupOnSku sku = new GroupOnSku();
							JSONObject skuJson = skuJsonArray.getJSONObject(i);
							sku.setSkuid(StringUtils.trimToEmpty(skuJson.getString("id")));
							sku.setSalePrice(CrawlerUtils.getPrice(skuJson.getString("newPrice"), currentUrl, logger));
							String originalPrice = skuJson.getString("oldPrice");
							if(StringUtils.isNotBlank(originalPrice))
								sku.setOriginalPrice(CrawlerUtils.getPrice(originalPrice, currentUrl, logger));
							sku.setUnit(CrawlerUtils.getUnit(skuJson.getString("newPrice"), currentUrl, logger));
							String isSoldOut = skuJson.getString("isSoldOut");
							if("false".equals(isSoldOut))
								sku.setStock_status(1);
							if("true".equals(isSoldOut))
								sku.setStock_status(0);
							String color = skuJson.getString("Color");
							if(color == null)
								color = skuJson.getString("Style");
							if(color == null)
								color = skuJson.getString("Option");
							if(color != null)
								sku.setColor(StringUtils.trim(color));
							else 
								sku.setColor("Default");
							String size = skuJson.getString("Size");
							if(size == null)
								size = skuJson.getString("Band Length");
							if(size != null)
								sku.setSize(StringUtils.trim(size));
							skus.add(sku);
						}
					}
					JSONObject imgsJson = skuJsonContain.getJSONObject("mappings");
					if(imgsJson == null || imgsJson.size() == 0){
						noMappingsImgs = new ArrayList<>();
						for(ColorImg colorimg : colorimgs) {
							noMappingsImgs.addAll(colorimg.getImgs());
						}
					} else {
						for(ColorImg colorimg : colorimgs){
							JSONObject obj = imgsJson.getJSONObject(colorimg.getBaseUrl());
							if(obj == null){
								colorimg.setColor("allColor");
								continue;
							}
							//兼容Color用Style表示的商品
							JSONArray colors = obj.getJSONArray("Color");
							if(colors == null)
								colors = obj.getJSONArray("Style");
							if(colors == null)
								colors = obj.getJSONArray("Option");
							if(colors != null)
								colorimg.setColor(StringUtils.trimToEmpty(colors.get(0).toString()));
							else {
								colorimg.setColor("Default");
							}
						}
					}
				}
			}
			
			Map<String,List<String>> color_imgsMap= new HashMap<>();//存放颜色对应的图片
			if(CollectionUtils.isEmpty(noMappingsImgs) && CollectionUtils.isNotEmpty(skus)){
				for(ColorImg colorImg : colorimgs){
					String key = colorImg.getColor();
					if(color_imgsMap.containsKey(key))
						color_imgsMap.get(key).addAll(colorImg.getImgs());
					else
						color_imgsMap.put(key, colorImg.getImgs());
				}
			}
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			int count = 0;
			List<String> colors = new ArrayList<>();
			for(GroupOnSku sku : skus){
				List<Selection> slist = new ArrayList<>();
				//兼容没有Size的商品
				if(StringUtils.isNotBlank(sku.getSize())){
					Selection sec = new Selection(0, sku.getSize(), "Size");
					slist.add(sec);
				}
				LSelectionList selection = new LSelectionList();
				selection.setGoods_id(sku.getSkuid());
				selection.setPrice_unit(sku.getUnit());
				selection.setSale_price(sku.getSalePrice());
				selection.setOrig_price(sku.getOriginalPrice());
				if(selection.getOrig_price() < selection.getSale_price())
					selection.setOrig_price(selection.getSale_price());
				selection.setSelections(slist);
				selection.setStock_number(sku.getStock());
				selection.setStock_status(sku.getStock_status());
				selection.setStyle_id(sku.getColor());
				l_selection_list.add(selection);
				
				if(!colors.contains(sku.getColor())){
					colors.add(sku.getColor());
					LStyleList style = new LStyleList();
					style.setStyle_switch_img("");
					style.setGood_id(sku.getSkuid());
					style.setStyle_id(sku.getColor());
					style.setStyle_cate_name(CrawlerUtils.STYLE_CATE_NAME);
					style.setStyle_cate_id(0);
					style.setStyle_name(sku.getColor());
					if(count == 0){
						style.setDisplay(true);
						orig_price = sku.getOriginalPrice();
						sale_price = sku.getSalePrice();
						spu_Unit = sku.getUnit();
						if (orig_price != 0)
							save = Math.round((1 - sale_price / orig_price) * 100);
						List<String> defaultColorImgs = new ArrayList<>();
						if(CollectionUtils.isNotEmpty(noMappingsImgs)){
							defaultColorImgs.addAll(noMappingsImgs);
						} else {
							List<String> firstColors = color_imgsMap.get(style.getStyle_id());
							if(CollectionUtils.isNotEmpty(firstColors))
								defaultColorImgs.addAll(firstColors);
							else {
								logger.error("colorImgs is empty and url is {}", currentUrl);
								System.out.println("colorImgs is empty and url is "+ currentUrl);
								return;
							}
							for(ColorImg imgs : colorimgs){
								if(imgs.getColor().equals(style.getStyle_id()))
									continue;
								defaultColorImgs.addAll(imgs.getImgs());
							}
						}
						context.getUrl().getImages().put(sku.getSkuid()+"", CrawlerUtils.convertToImageList(defaultColorImgs));
					} else {
						List<String> color_imgs = new ArrayList<>();
						if(CollectionUtils.isNotEmpty(noMappingsImgs))
							color_imgs.addAll(noMappingsImgs);
						else  
							color_imgs = color_imgsMap.get(style.getStyle_id());
						context.getUrl().getImages().put(sku.getSkuid() + "", CrawlerUtils.convertToImageList(color_imgs));
					}
					l_style_list.add(style);
					count++;
				}
			}
			
			for(GroupOnSku sku : skus){
				if(sku.getStock_status() == 1){
					spu_stock_status = 1;
					break;
				}
			}

			//部分商品没有sku的情况
			if(CollectionUtils.isEmpty(skus)){
				String origPriceStr = CrawlerUtils.getValueByAttr(document, CSS_ORIGPRICE, null);
				String salePriceStr = CrawlerUtils.getValueByAttr(document, CSS_SALEPRICE, null);
				if(StringUtils.isBlank(origPriceStr) && StringUtils.isBlank(salePriceStr)){
					salePriceStr = CrawlerUtils.getValueByAttr(document, "div#purchase-cluster span.price", null);
					origPriceStr = CrawlerUtils.getValueByAttr(document, "div#purchase-cluster div.deal-discount td.discount-value", null);
				}
				if(StringUtils.isBlank(salePriceStr)){
					logger.error("can not get sale price and url is {}",currentUrl);
//					logger.error("current response content is {}",content);
//					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
//							"can not get sale price with url " + currentUrl);
				}
				if(StringUtils.isBlank(origPriceStr) && StringUtils.isNotBlank(salePriceStr))
					origPriceStr = salePriceStr;
				spu_Unit = CrawlerUtils.getUnit(salePriceStr, currentUrl, logger);
				orig_price = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
				sale_price = CrawlerUtils.getPrice(salePriceStr, currentUrl, logger);
				if (orig_price < sale_price)
					orig_price = sale_price;
				if (orig_price != 0)
					save = Math.round((1 - sale_price / orig_price) * 100);
				else {
					logger.error("can not get the price, url is {}", currentUrl);
//					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "can not get the price, url is " + currentUrl);
				}
				List<String> noskuColorImgs = new ArrayList<>();
				for(ColorImg imgs : colorimgs){
					noskuColorImgs.addAll(imgs.getImgs());
				}
				if("Buy!".equals(StringUtils.trim(CrawlerUtils.getValueByAttr(document, "a#buy-link", null))))
					spu_stock_status = 1;
				context.getUrl().getImages().put(productID+"", CrawlerUtils.convertToImageList(noskuColorImgs));
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
			retbody.setPrice(new Price(orig_price, save, sale_price, spu_Unit));
			retbody.setStock(new Stock(spu_stock_status));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		setOutput(context, retbody);
	}
}
