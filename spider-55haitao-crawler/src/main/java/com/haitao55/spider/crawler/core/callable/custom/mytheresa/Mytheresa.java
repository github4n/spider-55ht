package com.haitao55.spider.crawler.core.callable.custom.mytheresa;

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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;


/**
 * Mytheresa.com网站收入
 * date : 2016-11-28
 * @author denghuan
 *
 */
public class Mytheresa extends AbstractSelect{

	private static final String domain = "www.mytheresa.com";

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			 Document docment = Jsoup.parse(content);
			 String error = docment.select(".col-1 h1.hs1").text();
			 if(StringUtils.isNotBlank(error) && 
					 StringUtils.containsIgnoreCase(error, "page not found")){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			 }
			 String brand = docment.select(".product-shop .product-designer span a").text();
			 String title = docment.select(".product-shop .product-name span").text();
			 String salePrice = StringUtils.substringBetween(content, "price\":", ",");
			 String unit = StringUtils.substringBetween(content, "currencyCode\":\"", "\"");
			 String exisStock = docment.select(".add-to-cart button span span").text();
			 String origPrice = docment.select(".product-shop .price-info .price-box .old-price span.price").text();
			// String productSku = docment.select(".product-shop .product-sku").html();
			 String spuSkuId = StringUtils.substringBetween(content, "item no.&nbsp;", "</span>");
			
			 
			 if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling Mytheresa saleprice error ,url"+context.getUrl().toString());
			 }
			 if(StringUtils.isNotBlank(origPrice) && 
					 StringUtils.containsIgnoreCase(origPrice, "€")){
				 origPrice = origPrice.replaceAll("[€,.]", "");
			 }
			 if(StringUtils.isNotBlank(salePrice)){
				 salePrice = salePrice.replaceAll("\"", "");
			 }
			 if(StringUtils.isNotBlank(salePrice) && 
					 StringUtils.isNotBlank(origPrice)){
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			 }else if(StringUtils.isNotBlank(salePrice) && 
					 StringUtils.isBlank(origPrice)){
				 rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
			 }
			 String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			 rebody.setDOCID(SpiderStringUtil.md5Encode(context.getCurrentUrl()));
			 rebody.setSite(new Site(domain));
			 rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			 rebody.setTitle(new Title(title, ""));
			 rebody.setBrand(new Brand(brand, ""));
			 
			 List<Image> imgList = new ArrayList<>();
			 Elements images = docment.select(".product-image .product-image-gallery img");
			 for(Element e : images){
				 String image = e.attr("src");
				 if(StringUtils.isNotBlank(image)){
					 imgList.add(new Image("https:"+image));
				 }
			 }
			 
			 Sku sku = new Sku();
			 List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			 List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			 
			 Elements sizeEs = docment.select(".size-chooser ul.sizes li a");
			 boolean display = true;
			 if(sizeEs != null  && sizeEs.size() > 0){
				 for(int i = 0; i < sizeEs.size(); i++){
					 LSelectionList lSelectionList = new LSelectionList();
					 List<Selection> selections = new ArrayList<>();
					 String skuId = sizeEs.get(i).attr("data-option");
					 String wishlistHtml = sizeEs.get(i).html();
					 //String wishList = StringUtils.substringBetween(wishlistHtml, "</span>", "</a>");
					 int stock_status = 0;
					 String sizeValue = StringUtils.EMPTY;
					 if(StringUtils.isNotBlank(wishlistHtml) && 
							 StringUtils.containsIgnoreCase(wishlistHtml, "Add to wishlist")){
						 sizeValue = sizeEs.get(i).select("span").text();
					 }else{
						 stock_status = 1;
						 sizeValue = sizeEs.get(i).text();
					 }
					 
					// String isStock = sizeEs.get(i).text();
					 lSelectionList.setGoods_id(skuId);
					 if(StringUtils.isNotBlank(origPrice)){
						 lSelectionList.setOrig_price(Float.parseFloat(origPrice));
					 }else{
						 lSelectionList.setOrig_price(Float.parseFloat(salePrice));
					 }
					 lSelectionList.setSale_price(Float.parseFloat(salePrice));
					 lSelectionList.setPrice_unit(unit);
					 lSelectionList.setStyle_id("default");
				
					 lSelectionList.setStock_status(stock_status);
					 Selection selection = new Selection();
					 selection.setSelect_name("size");
					 selection.setSelect_value(sizeValue);
					 selections.add(selection);
					 lSelectionList.setSelections(selections);
					
					 if(stock_status > 0){
						 if(display){
							 LStyleList lStyleList = new LStyleList();
							 lStyleList.setDisplay(display);
							 display = false;
							 lStyleList.setGood_id(skuId);
							 lStyleList.setStyle_cate_id(0);
							 lStyleList.setStyle_cate_name("color");
							 lStyleList.setStyle_id("default");
							 lStyleList.setStyle_name("default");
							 lStyleList.setStyle_switch_img("");
							 context.getUrl().getImages().put(skuId, imgList);// picture
							 l_style_list.add(lStyleList);
						 }
					 }
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
					if(StringUtils.isNotBlank(exisStock) 
							&& !StringUtils.containsIgnoreCase(exisStock, "sold out")){
						spuStock = 1;
					}
					 context.getUrl().getImages().put(spuSkuId, imgList);// picture
				}
			 rebody.setStock(new Stock(spuStock));
			 
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 Elements es = docment.select(".breadcrumbs ul li");
			 for(int i = 1;i < es.size()-1; i++){
				 String cate = es.get(i).text();
				 if(StringUtils.isNotBlank(cate)){
					 cats.add(cate);
					 breads.add(cate);
				 }
			 }
			 rebody.setCategory(cats);
			 rebody.setBreadCrumb(breads);
			 
			 Map<String, Object> featureMap = new HashMap<String, Object>();
			 Map<String, Object> descMap = new HashMap<String, Object>();
			 Map<String, Object> propMap = new HashMap<String, Object>();
			 propMap.put("s_gender", "");
//			 Elements pops = docment.select(".fit-advisor ul.disc li.pa1");
//			 for(Element e : pops){
//				 String feature = e.text();
//				 if(StringUtils.isNotBlank(feature)){
//					 String key = StringUtils.trim(StringUtils.substringBefore(e.text(), " "));
//					 String value = StringUtils.trim(StringUtils.substringAfter(e.text(), " "));
//					 if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
//						 propMap.put(key, value);
//					 }
//				 }
//			 }
			 
			 String dsc = docment.select(".current div ul.disc li").text();
			 int count = 0;
			 Elements pops = docment.select(".fit-advisor ul.disc li.pa1");
			 for(Element e : pops){
				 String feature = e.text();
				 if(StringUtils.isNotBlank(feature)){
					 count ++;
					 featureMap.put("feature-"+count, feature);
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 String description = docment.select(".current div").text();
			 descMap.put("en", description+dsc);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

}
