package com.haitao55.spider.crawler.core.callable.custom.bobbibrowncosmetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * Bobbibrown 收录
 * date :17-4-17
 * @author denghuan
 *
 */
public class Bobbibrown extends AbstractSelect{

	private static final String domain = "www.bobbibrowncosmetics.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product__title h3").text();
			String productId = StringUtils.substringBetween(content, "window.PRODUCT_ID = \"", "\"");
			String salePrice  = doc.select(".js-product__info .product__price").text();
			String page_data = StringUtils.substringBetween(content, "var page_data = ", "</script>");
			
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			String cy = StringUtils.substring(salePrice, 0, 1);
			String unit = Currency.codeOf(cy).name();
			
			salePrice = salePrice.replaceAll("[$,]", "");
			
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
			rebody.setBrand(new Brand("BobbiBrown", ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			
			if(StringUtils.isNotBlank(page_data)){
				JSONObject jsonObject = JSONObject.parseObject(page_data);
				String catalog_spp = jsonObject.getString("catalog-spp");
				JSONObject cataLogSppJson = JSONObject.parseObject(catalog_spp);
				JSONArray jsonArray = cataLogSppJson.getJSONArray("products");
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject productJsonObject = jsonArray.getJSONObject(i);
					JSONArray imageJsonArray = productJsonObject.getJSONArray("IMAGE_XXL");
					List<Image> imageList = new ArrayList<Image>();
					for(int j = 0 ; j < imageJsonArray.size(); j++){
						String image = imageJsonArray.getString(j);
						if(StringUtils.isNotBlank(image)){
							imageList.add(new Image("http://"+domain+image));
						}
					}
					JSONArray skusJsonArray =productJsonObject.getJSONArray("skus");
					for(int s = 0; s < skusJsonArray.size(); s++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject skuJsonObject = skusJsonArray.getJSONObject(s);
						String price = skuJsonObject.getString("PRICE");
						String skuId = skuJsonObject.getString("SKU_ID");
						String size = skuJsonObject.getString("PRODUCT_SIZE");
						String color = skuJsonObject.getString("SHADENAME");
						if(StringUtils.isNotBlank(color) && color.length() > 25){
							color = "";
						}
						String inventory_status = skuJsonObject.getString("INVENTORY_STATUS");
						List<Image> skuImageList = new ArrayList<Image>();
						if(skusJsonArray.size() > 1){
							if(skuJsonObject.containsKey("IMAGE_SMOOSH_XL")){
								JSONArray smImageArray = skuJsonObject.getJSONArray("IMAGE_SMOOSH_XL");
								for(int m = 0; m < smImageArray.size(); m++){
									String image = smImageArray.getString(m);
									if(StringUtils.isNotBlank(image)){
										skuImageList.add(new Image("http://"+domain+image));
									}
								}
							}
						}
						int stock_status = 0;
						if("1".equals(inventory_status)){
							stock_status = 1;
						}
						if(StringUtils.isNotBlank(color) || 
								StringUtils.isNotBlank(size)){
							lSelectionList.setGoods_id(skuId);
							lSelectionList.setOrig_price(Float.parseFloat(price));
							lSelectionList.setSale_price(Float.parseFloat(price));
							lSelectionList.setStock_status(stock_status);
							lSelectionList.setPrice_unit(unit);
							List<Selection> selections = new ArrayList<>();
							if(StringUtils.isNotBlank(size)){
								Selection selection = new Selection();
								selection.setSelect_name("size");
								selection.setSelect_value(size);
								selections.add(selection);
							}
							lSelectionList.setSelections(selections);
							
							if(StringUtils.isBlank(color)){
								lSelectionList.setStyle_id("default");
								if(display){
									LStyleList lStyleList = new LStyleList();
									lStyleList.setDisplay(true);
									display = false;
									lStyleList.setGood_id(skuId);
									lStyleList.setStyle_cate_name("color");
									lStyleList.setStyle_cate_id(0);
									lStyleList.setStyle_switch_img("");
									lStyleList.setStyle_id("default");
									lStyleList.setStyle_name("default");
									if(StringUtils.isNotBlank(price)){
										rebody.setPrice(new Price(Float.parseFloat(price), 
												0, Float.parseFloat(price), unit));
									}
									context.getUrl().getImages().put(skuId, imageList);// picture
									l_style_list.add(lStyleList);
								}else{
									context.getUrl().getImages().put(skuId, skuImageList);// picture
								}
							}else{
								LStyleList lStyleList = new LStyleList();
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_switch_img("");
								lStyleList.setStyle_id(color);
								lStyleList.setStyle_name(color);
								lSelectionList.setStyle_id(color);
								if(display){
									lStyleList.setDisplay(true);
									display = false;
									if(StringUtils.isNotBlank(price)){
										rebody.setPrice(new Price(Float.parseFloat(price), 
												0, Float.parseFloat(price), unit));
									}
									skuImageList.addAll(imageList);
									context.getUrl().getImages().put(skuId, skuImageList);// picture
								}else{
									//skuImageList.addAll(imageList);
									context.getUrl().getImages().put(skuId, skuImageList);// picture
								}
								
								l_style_list.add(lStyleList);
							}
							l_selection_list.add(lSelectionList);
							
						}else{//无SKU
							if(StringUtils.isNotBlank(price)){
								rebody.setPrice(new Price(Float.parseFloat(price), 
										0, Float.parseFloat(price), unit));
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
							rebody.setStock(new Stock(stock_status));
						}
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
				rebody.setStock(new Stock(spuStock));
			}
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String url = context.getCurrentUrl();
			String cates = url.substring(url.indexOf("product/"), url.lastIndexOf("/"));
			if(StringUtils.isNotBlank(cates)){
				String[] sp = cates.split("/");
				if(sp.length >= 2){
					cats.add(sp[3]);
					breads.add(sp[3]);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".how-to-use__content").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "women");
			rebody.setProperties(propMap);
			
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
}
