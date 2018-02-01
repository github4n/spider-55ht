package com.haitao55.spider.crawler.core.callable.custom.jomalone;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

public class Jomalone extends AbstractSelect{

	private static final String domain = "www.jomalone.com";
	private String IMAGE_URL = "http://www.jomalone.com/media/export/cms/products/926x542/jo_###_926x542.jpg";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.spp_product_name").text();
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Jo Malone", ""));
			
			String skus = StringUtils.substringBetween(content, "var page_data = ", "</script>");	
			
			if(StringUtils.isNotBlank(skus)){
				JSONObject jsonObject = JSONObject.parseObject(skus);
				String catalog = jsonObject.getString("catalog");
				JSONObject catalogJsonObject = JSONObject.parseObject(catalog);
				String spp = catalogJsonObject.getString("spp");
				JSONObject sppJsonObject = JSONObject.parseObject(spp);
				String rpcdata = sppJsonObject.getString("rpcdata");
				JSONObject rpcdataJsonObject = JSONObject.parseObject(rpcdata);
				JSONArray jsonArray = rpcdataJsonObject.getJSONArray("products");
				
				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				boolean display = true;
				String unit = "USD";
				for(int i = 0;i < jsonArray.size(); i++){
					JSONObject skuObject =  jsonArray.getJSONObject(i);
					String  desc = doc.select(".spp_product_description").text();
					if(StringUtils.isBlank(desc)){
						desc =  skuObject.getString("META_DESCRIPTION");
					}
					
					String category = skuObject.getString("category");
					String cate = StringUtils.substringBetween(category, "CATEGORY_NAME\":\"", "\"");
					List<String> cats = new ArrayList<String>();
					List<String> breads = new ArrayList<String>();
					if(StringUtils.isNotBlank(cate)){
						 cats.add(cate);
						 breads.add(cate);
					}
					
					 rebody.setCategory(cats);
					 rebody.setBreadCrumb(breads);
					 Map<String, Object> propMap = new HashMap<String, Object>();
					 propMap.put("s_gender", "");
					 Map<String, Object> featureMap = new HashMap<String, Object>();
					 Map<String, Object> descMap = new HashMap<String, Object>();
					 featureMap.put("feature-1", desc);
					 
					 rebody.setProperties(propMap);
					 rebody.setFeatureList(featureMap);
					 descMap.put("en", desc);
					 rebody.setDescription(descMap);
					 	
					 
					JSONArray skuJsonArray = skuObject.getJSONArray("skus");
					
					for(int j = 0; j < skuJsonArray.size(); j++){
						LSelectionList lSelectionList = new LSelectionList();
						LStyleList  lStyleList = new LStyleList();
						JSONObject sObject =  skuJsonArray.getJSONObject(j);
						Float price = sObject.getFloat("PRICE");
						String skuId = sObject.getString("SKU_ID");
						String code = sObject.getString("PRODUCT_CODE");
						String size = sObject.getString("PRODUCT_SIZE");
						String status = sObject.getString("INVENTORY_STATUS");
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(price);
						lSelectionList.setSale_price(price);
						int stock_status = 0;
						if(StringUtils.isNotBlank(status) && 
								"1".equals(status)){
							stock_status = 1;
						}
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id(size);
						lSelectionList.setPrice_unit(unit);
						List<Selection> selections = new ArrayList<>();
						lSelectionList.setSelections(selections);
						List<Image> images = new ArrayList<>();
						String image = IMAGE_URL.replace("###", code);
						images.add(new Image(image));
						context.getUrl().getImages().put(skuId, images);// picture
						if(display){
							lStyleList.setDisplay(display);
							display = false;
							rebody.setPrice(new Price(price, 0, price, unit));
						}
						lStyleList.setStyle_cate_name("size");
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(size);
						lStyleList.setStyle_name(size);
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img("");
						l_selection_list.add(lSelectionList);
						l_style_list.add(lStyleList);
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
					
				}
				rebody.setStock(new Stock(spuStock));
				rebody.setSku(sku);
			}
		}
		setOutput(context, rebody);
	}
}
