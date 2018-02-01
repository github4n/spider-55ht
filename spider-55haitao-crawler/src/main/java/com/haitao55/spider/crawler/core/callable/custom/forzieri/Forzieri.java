package com.haitao55.spider.crawler.core.callable.custom.forzieri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Forzieri 网站收录
 * @author denghuan
 *
 */
public class Forzieri extends AbstractSelect{

	private static final String domain = "www.forzieri.com";
	private static final String FORZIERI_API = "http://public.forzieri.com/v1/products/###?l=usa&c=usa&t=pv";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document docment = Jsoup.parse(content);
			String error = docment.select(".container section h1").text();
			if (StringUtils.isNotBlank(error) && StringUtils.containsIgnoreCase(error, "We're sorry")) {
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"itemUrl:" + context.getUrl().toString() + " not found..");
			}
			String styleId = StringUtils.substringBetween(content, "variants\"", "\">");
			String brand = docment.select("span.brand-name").text();
			String title = docment.select("span.product-name").text();
			String spuSkuId = StringUtils.substringBetween(content, "sku: '", "',");
			// String salePrice_t = StringUtils.substringBetween(content,
			// "price\" content=\"", "\">");
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\">");
			//docment.select("#productPriceOffer span#listPrice span.cents").remove();
			String listPrice = docment.select("#productPriceOffer span#listPrice").text();

			//docment.select("#productPriceOffer span#salePrice span.cents").remove();
			String salePrice = docment.select("#productPriceOffer span#salePrice").text();
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());

			String docid = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(spuSkuId)) {
				docid = SpiderStringUtil.md5Encode(domain + spuSkuId);
			} else {
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));

			if (StringUtils.isNotBlank(salePrice) && StringUtils.isNotBlank(listPrice)) {
				listPrice = listPrice.replaceAll("[$,]", "");
				salePrice = salePrice.replaceAll("[$,]", "");
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(listPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(listPrice), save, Float.parseFloat(salePrice), unit));
			} else if ((StringUtils.isNotBlank(listPrice) && StringUtils.isBlank(salePrice))) {
				listPrice = listPrice.replaceAll("[$,]", "");
				rebody.setPrice(new Price(Float.parseFloat(listPrice), 0, Float.parseFloat(listPrice), unit));
			}
			 
			 List<Image> imgList = new ArrayList<>();
			 Elements imagesEs = docment.select(".product-image-wrap img");
			 for(Element ig : imagesEs){
				 String image = ig.attr("src");
				 if(StringUtils.isNotBlank(image)){
					 imgList.add(new Image(image));
				 }
			 }
			 
			 Sku sku = new Sku();
			 List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			 List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			 Elements sizeEs  = docment.select("#variants #variantSelect option");
			 Elements colorEs  = docment.select("#product-variants-scroller ul li a");
			 
			 if(StringUtils.isNotBlank(styleId) && 
					 StringUtils.containsIgnoreCase(styleId, "color")){
				 for(Element e : colorEs){
					 LSelectionList lSelectionList = new LSelectionList();
					 LStyleList lStyleList = new LStyleList();
					 List<Selection> selections = new ArrayList<>();
					 String skuId =  e.attr("data-sku");
					 String color = e.select("img").attr("alt");
					 String apiUrl = FORZIERI_API.replace("###", skuId);
					 Url currentUrl = new Url(apiUrl);
					 currentUrl.setTask(context.getUrl().getTask());
					 String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					 if(StringUtils.isNotBlank(skuJson)){
						 String sale_price = StringUtils.substringBetween(skuJson, "sale_price\":\"", "\"");
						 //String skuSalePrice = StringUtils.substringBetween(skuJson, "\"price\\\" content=\\\"", "\\\"");
						 //String in_stock = StringUtils.substringBetween(skuJson, "in_stock\":", ",");
						 String sold_out = StringUtils.substringBetween(skuJson, "sold_out\":", ",");
						 //String temp_salePrice = StringUtils.substringBetween(skuJson, "sale_price\":", ",");
						 String image_zoom = StringUtils.substringBetween(skuJson, "image_zoom\":\"", "\",");
						
						 String skuSalePrice = StringUtils.EMPTY;
						 if(StringUtils.isNotBlank(sale_price)){
							 String sprice = StringUtils.substringBetween(sale_price, "price\\\" content=\\\"", "\\\"");
							 if(StringUtils.isNotBlank(sprice)){
								 skuSalePrice = sprice;
							 }
						 }
						 
						 if(StringUtils.isNotBlank(skuSalePrice)){
							 String lPrice = StringUtils.substringBetween(skuJson, "list_price\":\"", "\"");
							 if(StringUtils.isNotBlank(lPrice) && 
									 !StringUtils.containsIgnoreCase(lPrice, "<span")){
								 String list_price = lPrice.replaceAll("[$,]", "");
								 lSelectionList.setOrig_price(Float.parseFloat(list_price));
								 lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
							 }
						 }else{
							 String sku_list_price = StringUtils.substringBetween(skuJson, "list_price\":\"", ">\"");
							 String list_price = StringUtils.substringBetween(sku_list_price, "price\\\" content=\\\"", "\\\"");
							 if(StringUtils.isNotBlank(list_price)){
								 lSelectionList.setOrig_price(Float.parseFloat(list_price));
								 lSelectionList.setSale_price(Float.parseFloat(list_price));
							 }
						 }
						 
						 int stock = 0;
						 if(StringUtils.isNotBlank(sold_out) &&
								 !sold_out.equals("true")){
							 stock = 1;
						 }
						 lSelectionList.setStock_status(stock);
						 lSelectionList.setStyle_id(color);
						 if(spuSkuId.equals(skuId)){
							 lStyleList.setDisplay(true);
							 context.getUrl().getImages().put(skuId, imgList);// picture
						 }else{
							 List<Image> skuImgList = new ArrayList<>();
							 String cleaingImage = image_zoom.replaceAll("[\\\\]", "");
							 skuImgList.add(new Image(cleaingImage));
							 context.getUrl().getImages().put(skuId, skuImgList);// picture
						 }
					 }
					 lSelectionList.setPrice_unit(unit);
					 lSelectionList.setGoods_id(skuId);
					 lSelectionList.setSelections(selections);
					 lStyleList.setGood_id(skuId);
					 lStyleList.setStyle_cate_id(0);
					 lStyleList.setStyle_cate_name("color");
					 lStyleList.setStyle_name(color);
					 lStyleList.setStyle_id(color);
					 lStyleList.setStyle_switch_img("");
					
					 l_selection_list.add(lSelectionList);
					 l_style_list.add(lStyleList);
				 }
			 }else if(StringUtils.isNotBlank(styleId) && 
					 StringUtils.containsIgnoreCase(styleId, "size")){
				 for(Element e : sizeEs){
					 LSelectionList lSelectionList = new LSelectionList();
					 LStyleList lStyleList = new LStyleList();
					 List<Selection> selections = new ArrayList<>();
					 String skuId =  e.attr("value");
					 String size = e.text();
					 String apiUrl = FORZIERI_API.replace("###", skuId);
					 Url currentUrl = new Url(apiUrl);
					 currentUrl.setTask(context.getUrl().getTask());
					 String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					 if(StringUtils.isNotBlank(skuJson)){
						 String sale_price = StringUtils.substringBetween(skuJson, "sale_price\"", ",");
						// String skuSalePrice = StringUtils.substringBetween(skuJson, "\"price\\\" content=\\\"", "\\\"");
						 //String in_stock = StringUtils.substringBetween(skuJson, "in_stock\":", ",");
						 String sold_out = StringUtils.substringBetween(skuJson, "sold_out\":", ",");
						 
						 String skuSalePrice = StringUtils.EMPTY;
						 if(StringUtils.isNotBlank(sale_price)){
							 String sprice = StringUtils.substringBetween(sale_price, "price\\\" content=\\\"", "\\\"");
							 if(StringUtils.isNotBlank(sprice)){
								 skuSalePrice = sprice;
							 }
						 }
						 
						 if(StringUtils.isNotBlank(skuSalePrice)){
							 String lPrice = StringUtils.substringBetween(skuJson, "list_price\":\"", "\"");
							 if(StringUtils.isNotBlank(lPrice) && 
									 !StringUtils.containsIgnoreCase(lPrice, "<span")){
								 String list_price = lPrice.replaceAll("[$,]", "");
								 lSelectionList.setOrig_price(Float.parseFloat(list_price));
								 lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
							 }
						 }else{
							 String sku_list_price = StringUtils.substringBetween(skuJson, "list_price\":\"", ">\"");
							 String list_price = StringUtils.substringBetween(sku_list_price, "price\\\" content=\\\"", "\\\"");
							 if(StringUtils.isNotBlank(list_price)){
								 lSelectionList.setOrig_price(Float.parseFloat(list_price));
								 lSelectionList.setSale_price(Float.parseFloat(list_price));
							 }
						 }
						 
						 lSelectionList.setPrice_unit(unit);
						 int stock = 0;
						 if(StringUtils.isNotBlank(sold_out) &&
								 !sold_out.equals("true")){
							 stock = 1;
						 }
						 lSelectionList.setStock_status(stock);
						 lSelectionList.setStyle_id("default");
					 }
					 lSelectionList.setGoods_id(skuId);
					 Selection selection = new Selection();
					 selection.setSelect_name("size");
					 selection.setSelect_value(size);
					 selections.add(selection);
					 if(spuSkuId.equals(skuId)){
						 lStyleList.setGood_id(skuId);
						 lStyleList.setDisplay(true);
						 lStyleList.setStyle_cate_name("color");
						 lStyleList.setStyle_cate_id(0);
						 lStyleList.setStyle_name("default");
						 lStyleList.setStyle_id("default");
						 lStyleList.setStyle_switch_img("");
						 context.getUrl().getImages().put(skuId, imgList);// picture
						 l_style_list.add(lStyleList);
					 }
					 lSelectionList.setSelections(selections);
					 l_selection_list.add(lSelectionList);
				 }
			 }
			 sku.setL_selection_list(l_selection_list);
			 sku.setL_style_list(l_style_list);
			 
			 int spuStock = 0;
				if(l_selection_list != null 
						&& l_selection_list.size() > 0){
					for(LSelectionList ll : l_selection_list){
						int sku_stock = ll.getStock_status();
						if (sku_stock == 1) {
							spuStock = 1;
							break;
						}
						if (sku_stock == 2){
							spuStock = 2;
						}
					}
				}else{
					 String apiUrl = FORZIERI_API.replace("###", spuSkuId);
					 Url currentUrl = new Url(apiUrl);
					 currentUrl.setTask(context.getUrl().getTask());
					 String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					 if(StringUtils.isNotBlank(skuJson)){
						 String sold_out = StringUtils.substringBetween(skuJson, "sold_out\":", ",");
						 if(StringUtils.isNotBlank(sold_out) &&
								 !sold_out.equals("true")){
							 spuStock = 1;
						 }
					 }
					context.getUrl().getImages().put(spuSkuId, imgList);// picture
				}
				
				rebody.setStock(new Stock(spuStock));
			 
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 String cate = StringUtils.substringBetween(url, "com/", "/");
			 if(StringUtils.isNotBlank(cate)){
				 cats.add(cate);
				 breads.add(cate);
			 }else{
				 cats.add(title);
				 breads.add(title);
			 }
			 rebody.setCategory(cats);
			 rebody.setBreadCrumb(breads);
			 
			 Map<String, Object> featureMap = new HashMap<String, Object>();
			 Map<String, Object> descMap = new HashMap<String, Object>();
			 Map<String, Object> propMap = new HashMap<String, Object>();
			 String description = docment.select(".product-description-wrap").text();
			 propMap.put("s_gender", "");
			 featureMap.put("feature-1", description);
			 
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", description);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
}
