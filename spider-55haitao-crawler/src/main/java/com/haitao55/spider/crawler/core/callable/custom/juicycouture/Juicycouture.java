package com.haitao55.spider.crawler.core.callable.custom.juicycouture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

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


/**
 * juicycouture.com网站收录
 * date:2017-01-11
 * @author denghuan
 *
 */
public class Juicycouture extends AbstractSelect{

	private static final String domain = "www.juicycouture.com";

	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			String pageData = StringUtils.substringBetween(content, "var __onestop_pageData = ", ";</script>");
			String title = StringUtils.substringBetween(content, "ModelName\":\"", "\"");
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Juicy Couture", ""));
			
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			String unit = "USD";
			if(StringUtils.isNotBlank(pageData)){
				JSONObject jsonObject = JSONObject.parseObject(pageData);
				String product = jsonObject.getString("product");
				JSONObject productObject = JSONObject.parseObject(product);
				String defalutColor = productObject.getString("DefaultColor");
				String defalutId = StringUtils.EMPTY;
				if(StringUtils.isNotBlank(defalutColor)){
					JSONObject defalutColorObject = JSONObject.parseObject(defalutColor);
					defalutId = defalutColorObject.getString("Id");
				}
				JSONArray inventoryJsonArray = productObject.getJSONArray("Inventory");
				Map<String,String> inventoryMap = new HashMap<>();
				for(int j = 0; j < inventoryJsonArray.size(); j++){
					JSONObject productColorsObject =  inventoryJsonArray.getJSONObject(j);
					String colorId = productColorsObject.getString("ColorId");
					String sizeId = productColorsObject.getString("SizeId");
					String IsInStock = productColorsObject.getString("IsInStock");
					inventoryMap.put(colorId+sizeId, IsInStock);
				}
				
				JSONArray skuPricesJsonArray = productObject.getJSONArray("SkuPrices");
				Map<String,JuicyCoutureSKuVO> skuPricesMap = new HashMap<>();
				for(int j = 0; j < skuPricesJsonArray.size(); j++){
					JuicyCoutureSKuVO juicyCoutureSKuVO = new JuicyCoutureSKuVO();
					JSONObject productColorsObject =  skuPricesJsonArray.getJSONObject(j);
					String origPrice = productColorsObject.getString("RegularPrice");
					String salePrice = productColorsObject.getString("SalePrice");
					String price = productColorsObject.getString("Price");
					String productColor = productColorsObject.getString("ProductColor");
					JSONObject productColorObject = JSONObject.parseObject(productColor);
					String colorId = productColorObject.getString("Id");
					String productSize = productColorsObject.getString("ProductSize");
					JSONObject productSizeObject = JSONObject.parseObject(productSize);
					String sizeId = productSizeObject.getString("Id");
					juicyCoutureSKuVO.setOrigPrice(origPrice);
					juicyCoutureSKuVO.setSalePrice(salePrice);
					juicyCoutureSKuVO.setPrice(price);
					skuPricesMap.put(colorId+sizeId, juicyCoutureSKuVO);
				}
				
				Map<String,List<String>> imageMap = new HashMap<String,List<String>>();
				JSONArray imageJsonArray = productObject.getJSONArray("Media");
				for(int s = 0; s < imageJsonArray.size(); s++){
					JSONObject mediaObject =  imageJsonArray.getJSONObject(s);
					String colorImageId  = mediaObject.getString("ColorId");
					JSONArray itemImageJsonArray = mediaObject.getJSONArray("Items");
					List<String> imageList = new ArrayList<>();
					for(int r = 0; r < itemImageJsonArray.size(); r++){
						JSONObject imageObject =  itemImageJsonArray.getJSONObject(r);
						String imageUrl = imageObject.getString("ImageUrl");
						if(StringUtils.isNotBlank(imageUrl)){
							imageList.add(imageUrl);
						}
					}
					imageMap.put(colorImageId, imageList);
				}
				
				
				JSONArray colorJsonArray = productObject.getJSONArray("ProductColors");
				JSONArray sizeJsonArray = productObject.getJSONArray("ProductSizes");
				boolean defalutSku = true;
				for(int i = 0; i < colorJsonArray.size(); i++){
					LStyleList lStyleList = new LStyleList();
					JSONObject productColorsObject =  colorJsonArray.getJSONObject(i);
					String colorId = productColorsObject.getString("Id");
					String colorName = productColorsObject.getString("ColorName");
					//String colorImageUrl = productColorsObject.getString("ColorImageUrl");
					boolean skuIdFlag = true;
					
					String skuId = StringUtils.EMPTY;
					for(int c = 0; c < sizeJsonArray.size(); c++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject sizeObject =  sizeJsonArray.getJSONObject(c);
						String sizeId = sizeObject.getString("Id");
						String sizeName = sizeObject.getString("SizeName");
						JuicyCoutureSKuVO skuPrice = skuPricesMap.get(colorId+sizeId);
						String origPrice = skuPrice.getOrigPrice();
						//String salePrice = skuPrice.getSalePrice();
						String price = skuPrice.getPrice();
						lSelectionList.setGoods_id(colorId+sizeId);
						lSelectionList.setStyle_id(colorName);
						if((StringUtils.isNotBlank(origPrice) && !"0".equals(origPrice))&& 
								(StringUtils.isNotBlank(price) && !"0".equals(price))){
							lSelectionList.setOrig_price(Float.parseFloat(origPrice));
							lSelectionList.setSale_price(Float.parseFloat(price));
						}else if("0".equals(origPrice)&& 
								(StringUtils.isNotBlank(price) && !"0".equals(price))){
							lSelectionList.setOrig_price(Float.parseFloat(price));
							lSelectionList.setSale_price(Float.parseFloat(price));
						}
						lSelectionList.setPrice_unit(unit);
						
						String stock = inventoryMap.get(colorId+sizeId);
						int stock_status = 0;
						if(StringUtils.isNotBlank(stock) &&
								"true".equals(stock)){
							stock_status = 1;
						}
						
						if(skuIdFlag){
							skuId = colorId+sizeId;
							skuIdFlag = false;
						}
						if(defalutSku){
							if(StringUtils.isNotBlank(defalutId)){
								if(defalutId.equals(colorId)){
									lStyleList.setDisplay(true);
								}
							}else{
								lStyleList.setDisplay(true);
							}
							if((StringUtils.isNotBlank(origPrice) && !"0".equals(origPrice))&& 
									(StringUtils.isNotBlank(price) && !"0".equals(price))){
								int save = Math.round((1 - Float.valueOf(price) / Float.valueOf(origPrice)) * 100);// discount
								rebody.setPrice(new Price(Float.valueOf(origPrice), save, Float.valueOf(price), unit));
							}else if("0".equals(origPrice) && 
									(StringUtils.isNotBlank(price) && !"0".equals(price))){
								rebody.setPrice(new Price(Float.valueOf(price), 0, Float.valueOf(price), unit));
							}
							defalutSku = false;
							
						}
						
						lSelectionList.setStock_status(stock_status);
						List<Selection> selections = new ArrayList<>();
						Selection  selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeName);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
					
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(colorName);
					lStyleList.setStyle_switch_img("");
					lStyleList.setStyle_name(colorName);
					lStyleList.setGood_id(skuId);
					List<Image> images = new ArrayList<>();
					List<String> list = imageMap.get(colorId);
					if(CollectionUtils.isNotEmpty(list)){
						for(String image : list){
							if(!StringUtils.containsIgnoreCase(image, "http")){
								images.add(new Image("http:"+image));
							}
						}
					}
					context.getUrl().getImages().put(skuId, images);// picture
					
					l_style_list.add(lStyleList);
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
				}
				rebody.setStock(new Stock(spuStock));
				
				
				 List<String> cats = new ArrayList<String>();
				 List<String> breads = new ArrayList<String>();
				 
				 String primaryCategoryName = productObject.getString("PrimaryCategoryName");
				 if(StringUtils.isNotBlank(primaryCategoryName)){
					 if(StringUtils.containsIgnoreCase(primaryCategoryName, "->")){
						String[] cates = primaryCategoryName.split("->");
						for(String cate : cates){
							if(StringUtils.isNotBlank(cate)){
								cats.add(cate);
								breads.add(cate);
							}
						}
					 }else{
						 cats.add(primaryCategoryName);
						 breads.add(primaryCategoryName);
					 }
				 }
				
				 rebody.setCategory(cats);
				 rebody.setBreadCrumb(breads);
				 
				 Map<String, Object> propMap = new HashMap<String, Object>();
				 propMap.put("s_gender", "");
				 Map<String, Object> featureMap = new HashMap<String, Object>();
				 Map<String, Object> descMap = new HashMap<String, Object>();
				 
				 String html = productObject.getString("Description1");
				 String description = Jsoup.parse(html).text();
				 featureMap.put("feature-1", description);
				 
				 rebody.setProperties(propMap);
				 rebody.setFeatureList(featureMap);
				 descMap.put("en", description);
				 rebody.setDescription(descMap);
				 rebody.setSku(sku);
			}
			
		}
		setOutput(context, rebody);
	}

}
