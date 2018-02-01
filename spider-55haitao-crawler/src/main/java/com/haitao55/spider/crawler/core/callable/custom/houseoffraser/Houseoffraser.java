package com.haitao55.spider.crawler.core.callable.custom.houseoffraser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * 
 * Houseoffraser网站收录
 * date:2017-4-10
 * @author denghuan
 *
 */
public class Houseoffraser extends AbstractSelect{

	private static final String domain = "www.houseoffraser.co.uk";
	private static final String IMAGE_SUFFIX= "?wid=450&hei=600&qlt=80";
	private static final String IMAGE_URL= "https://houseoffraser.scene7.com/is/image/HOF/";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String defaultColor = StringUtils.substringBetween(content, "productColour\":\"", "\"");
			String brand = doc.select("h1.product-title span.brand").text();
			String title = doc.select("h1.product-title span.name").text();
			String unit = StringUtils.substringBetween(content, "priceCurrency\":\"", "\"");
			String productId = StringUtils.substringBetween(content, "productID\":\"", "\"");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String variations = StringUtils.substringBetween(content, "ProductDetailViewModel(", "),");
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			List<Image> spuImageList = new ArrayList<>();
			
			String spuSalePrice = StringUtils.EMPTY;
			String spuOrigPrice = StringUtils.EMPTY;
			String spuStockLevel = StringUtils.EMPTY;
			
			if(StringUtils.isNotBlank(variations)){
				JSONObject jsonObject = JSONObject.parseObject(variations);	
				JSONArray imageJsonArr = jsonObject.getJSONArray("ImageData");
				for(int m = 0; m < imageJsonArr.size(); m++){
					String image = imageJsonArr.getString(m);
					spuImageList.add(new Image(IMAGE_URL+image+IMAGE_SUFFIX));
				}
				String spuPrice = jsonObject.getString("Price");
				JSONObject priceJsonObject = JSONObject.parseObject(spuPrice);	
				spuSalePrice = priceJsonObject.getString("Value");
				spuOrigPrice = priceJsonObject.getString("WasPrice");
				
				String spuStockJson = jsonObject.getString("Stock");
				JSONObject stockJsonObject = JSONObject.parseObject(spuStockJson);	
				spuStockLevel = stockJsonObject.getString("StockLevel");
				
				JSONArray jsonArray = jsonObject.getJSONArray("VariantOptions");
				if(jsonArray != null){
					for(int i = 0 ;i < jsonArray.size(); i++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject skuJsonObject = jsonArray.getJSONObject(i);
						JSONArray jsonArrayImage = skuJsonObject.getJSONArray("ImageData");
						if(jsonArrayImage == null){
							continue;
						}
						String customAttributes = skuJsonObject.getString("CustomAttributes");
						JSONObject customAttrJson = JSONObject.parseObject(customAttributes);	
						String productAttribute = customAttrJson.getString("ProductAttribute");
						JSONObject productAttributeJson = JSONObject.parseObject(productAttribute);	
						String skuId = productAttributeJson.getString("Code");
						String colorVal = productAttributeJson.getString("Color");
						String sizeVal = productAttributeJson.getString("Size");
						String price = skuJsonObject.getString("Price");
						JSONObject priceJson = JSONObject.parseObject(price);	
						Float salePrice = priceJson.getFloat("Value");
						Float origPrice = priceJson.getFloat("WasPrice");
						String stock = skuJsonObject.getString("Stock");
						JSONObject stockJson = JSONObject.parseObject(stock);	
						String stockLevel = stockJson.getString("StockLevel");
						
						if(origPrice == 0.0){
							origPrice = salePrice;
						}
						
						int stock_status = 0;
						if(StringUtils.isNotBlank(stockLevel) && 
								Integer.parseInt(stockLevel) > 0){
							stock_status = 1;
						}
						List<Image> imageList = new ArrayList<>();
						for(int j = 0; j < jsonArrayImage.size(); j++){
							String image = jsonArrayImage.getString(j);
							imageList.add(new Image(IMAGE_URL+image+IMAGE_SUFFIX));
						}
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setSale_price(salePrice);
						lSelectionList.setOrig_price(origPrice);
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id(colorVal);
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(sizeVal)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(sizeVal);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
						
						if(StringUtils.isNotBlank(colorVal)){
							if(!styleMap.containsKey(colorVal)){
								LStyleList lStyleList = new LStyleList();
								if(colorVal.equals(defaultColor)){
									lStyleList.setDisplay(true);
									int save = Math.round((1 - salePrice / origPrice) * 100);// discount
									rebody.setPrice(new Price(origPrice, save, salePrice, unit));
								}
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_id(colorVal);
								lStyleList.setStyle_name(colorVal);
								lStyleList.setStyle_switch_img("");
								context.getUrl().getImages().put(skuId, imageList);// picture
								l_style_list.add(lStyleList);
							}
						}
						styleMap.put(colorVal, colorVal);
					}
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
				if(StringUtils.isBlank(spuSalePrice) || "0.0".equals(spuSalePrice)){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
				}
				
				if(StringUtils.isBlank(spuOrigPrice) || "0.0".equals(spuOrigPrice)){
					spuOrigPrice = spuSalePrice;
				}
				int save = Math.round((1 - Float.parseFloat(spuSalePrice) / Float.parseFloat(spuOrigPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(spuOrigPrice), save, Float.parseFloat(spuSalePrice), unit));
				
				context.getUrl().getImages().put(productId, spuImageList);// picture
				
				if(StringUtils.isNotBlank(spuStockLevel) && 
						Integer.parseInt(spuStockLevel) > 0){
					spuStock = 1;
				}
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String gender = StringUtils.EMPTY;
			Elements es = doc.select("ul#js-crumb-trail-container li.crumb-item a span");
			for(int i = 0; i < es.size(); i++){
				String cate = es.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
					if(cate.equalsIgnoreCase("women")){
						gender = "women";
					}else if(cate.equals("men")){
						gender = "men";
					}
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select("#description .col-md-12").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

}
