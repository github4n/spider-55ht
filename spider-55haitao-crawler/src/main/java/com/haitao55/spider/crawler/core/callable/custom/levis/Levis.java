package com.haitao55.spider.crawler.core.callable.custom.levis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

public class Levis extends AbstractSelect{
	private static final String domain = "www.levi.com";
	private static final String BRAND = "Levis";
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content);	
			String outStock = document.select(".rich-media-para h2").text();
			if(StringUtils.isNotBlank(outStock) && 
					StringUtils.containsIgnoreCase(outStock, "no longer available")){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"levi.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
			}
			String title = document.select("#pdp-buystack-form h1.title").text();
			String productId = StringUtils.substringBetween(content, "data-productid=\"", "\"");
			String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
			List<Image> imageList = new ArrayList<>();
			Elements es = document.select(".alternate-images ul li");
			for(Element e : es){
				String image = e.attr("data-asset");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			String thenPrice = document.select("span.then-price").text();
			String wasPrice = document.select("span.was-price").text();
			String nowPrice  = document.select("span.now-price").text();
			if(StringUtils.isNotBlank(thenPrice)){
				thenPrice = thenPrice.replaceAll("[$, ]", "");
			}
			if(StringUtils.isNotBlank(wasPrice)){
				wasPrice = wasPrice.replaceAll("[$, ]", "");
			}
			if(StringUtils.isNotBlank(nowPrice)){
				nowPrice = nowPrice.replaceAll("[$, ]", "");
			}
			
			if(StringUtils.isNotBlank(nowPrice) && StringUtils.isNotBlank(wasPrice)){
				int save = Math.round((1 - Float.parseFloat(nowPrice) / Float.parseFloat(wasPrice)) * 100);// discount
				retbody.setPrice(new Price(Float.parseFloat(wasPrice), 
						save, Float.parseFloat(nowPrice), unit));
			}else if(StringUtils.isNotBlank(thenPrice)){
				retbody.setPrice(new Price(Float.parseFloat(thenPrice), 
						0, Float.parseFloat(thenPrice), unit));
			}
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, ""));
			retbody.setBrand(new Brand(BRAND, ""));
			
			Sku sku = new Sku();
			boolean needFixColorName = false;
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			String jsonString  = StringUtils.substringBetween(content,"var buyStackJSON = '", "';");
			Set<String> colorNames = new HashSet<String>();
			Map<String, String> colorFixNames = new HashMap<String, String>();
			if(StringUtils.isNotBlank(jsonString)){
				String product = jsonString.replace("\\'", "\\\\'").replace("\\\"", "\\\\\"");
				JSONObject json = JSONObject.parseObject(product);
				JSONObject colorIds = json.getJSONObject("colorid");
				for(Entry<String, Object> e: colorIds.entrySet()){
					LStyleList lStyleList = new LStyleList();
					JSONObject style_Map = (JSONObject)e.getValue();
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					String colorName = style_Map.getJSONObject("finish").getString("title");
					String colorId = style_Map.getString("colorid");
					if(CollectionUtils.isEmpty(colorNames)){
						colorNames.add(colorName);
					}else{
						if(colorNames.contains(colorName)){
							String sec_colorName = style_Map.getString("productName");
                            if(sec_colorName.contains("&reg;")){
                            	sec_colorName = sec_colorName.split("&reg;")[1].trim();
                            }							
							colorName = colorName +" - "+sec_colorName;	
							needFixColorName = true;
						}else{
							colorNames.add(colorName);
						}
					}
					colorFixNames.put(colorId, colorName);
					lStyleList.setStyle_id(colorName);
					lStyleList.setStyle_name(colorName);
					lStyleList.setStyle_switch_img("");
					lStyleList.setGood_id(colorId);
					if(productId.equals(style_Map.getString("colorid"))){
						lStyleList.setDisplay(true);
						context.getUrl().getImages().put(style_Map.getString("colorid"),imageList);
					}else{
						List<Image> img_list = new ArrayList<Image>();
						boolean imageBoo = style_Map.containsKey("altViews");
						if(imageBoo){
							JSONArray jsonArray = style_Map.getJSONArray("altViews");
							for(int i = 0; i < jsonArray.size();i++){
								String image_suff = jsonArray.getString(i);
								if(StringUtils.isNotBlank(image_suff)){
									img_list.add((new Image(style_Map.getString("imageURL")+image_suff)));
								}
							}
						}else{
							img_list.add((new Image(style_Map.getString("imageURL")+"front-pdp.jpg")));
						}
						context.getUrl().getImages().put(style_Map.getString("colorid"),img_list);
					}
					l_style_list.add(lStyleList);	
				}
				
				JSONObject selection_Jsons = json.getJSONObject("sku");
				for(Entry<String, Object> e:selection_Jsons.entrySet()){
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject selectioin_Map = (JSONObject)e.getValue();
					lSelectionList.setGoods_id(selectioin_Map.getString("skuid"));
					if(needFixColorName){
						lSelectionList.setStyle_id(colorFixNames.get(selectioin_Map.getString("colorid")));
					}else{
						if(!selectioin_Map.getString("colorid").isEmpty()){
							lSelectionList.setStyle_id(colorIds.getJSONObject(selectioin_Map.getString("colorid")).getJSONObject("finish").getString("title"));
						}else {
							lSelectionList.setStyle_id("OneColor");
						}
					}				
					lSelectionList.setPrice_unit(unit);
					if(selectioin_Map.getJSONArray("price").size()>1){
						lSelectionList.setOrig_price(Float.valueOf((selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
						lSelectionList.setSale_price(Float.valueOf((selectioin_Map.getJSONArray("price").getJSONObject(1).getString("amount").replace("$", ""))));
					}
					else if(selectioin_Map.getJSONArray("price").size() ==1){
						lSelectionList.setOrig_price(Float.valueOf((selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
						lSelectionList.setSale_price(Float.valueOf((selectioin_Map.getJSONArray("price").getJSONObject(0).getString("amount").replace("$", ""))));
					}
					int stock_status = 0;
					if(selectioin_Map.getInteger("stock") >0){
						stock_status = 1;
					}
					lSelectionList.setStock_status(stock_status);
					
					List<Selection> selections = new ArrayList<>();
					if(selectioin_Map.getString("size") != null ){
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(selectioin_Map.getString("size"));
						selections.add(selection);
					}
					if(selectioin_Map.getString("length") != null){
						Selection selection = new Selection();
						selection.setSelect_name("length");
						selection.setSelect_value(selectioin_Map.getString("length"));
						selections.add(selection);
					}
					if(selectioin_Map.getString("waist") != null){
						Selection selection = new Selection();
						selection.setSelect_name("waist");
						selection.setSelect_value(selectioin_Map.getString("waist"));
						selections.add(selection);
					}
					
					if(CollectionUtils.isEmpty(selections)){
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value("OneSize");
						selections.add(selection);
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
				context.getUrl().getImages().put(productId,imageList);
				spuStock = 1;
			}
			retbody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String cates = StringUtils.substringBetween(content, "categoryIds=", "';");
			if(StringUtils.isNotBlank(cates)){
				String cateString = StringUtils.substringBetween(cates, ",", ",");
				if(StringUtils.isNotBlank(cateString)){
					String[] sp = cateString.split("/");
					for(String cate : sp){
						cats.add(cate);
						breads.add(cate);
					}
				}else{
					cats.add(title);
					breads.add(title);
				}
			}else{
				cats.add(title);
				breads.add(title);
			}
			retbody.setCategory(cats);
			retbody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = document.select(".pdp-description p").text();
		    featureMap.put("feature-1", description);
					
		    retbody.setFeatureList(featureMap);
			descMap.put("en", description);
			retbody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			retbody.setProperties(propMap);
			
			retbody.setSku(sku);
		}
		setOutput(context, retbody);
	}
}
