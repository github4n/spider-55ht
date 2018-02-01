package com.haitao55.spider.crawler.core.callable.custom.wconcept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.CaseInsensitiveMap;
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

/**
 * wconcept收录
 * @author denghuan
 * date :  17-4-17
 */

public class Wconcept extends AbstractSelect{

	private static final String domain = "us.wconcept.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			String productId = StringUtils.substringBetween(content, "productId\":\"", "\"");
			String optionPrice = StringUtils.substringBetween(content, "Product.OptionsPrice(", ");");
			String salePrice = StringUtils.substringBetween(optionPrice, "productPrice\":", ",");
			String origPrice = StringUtils.substringBetween(optionPrice, "productOldPrice\":", ",");
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			salePrice = salePrice.replaceAll("[$, ]", "");
			origPrice = origPrice.replaceAll("[$, ]", "");
			
			String brand = doc.select(".short_des a").text();
			String title = StringUtils.substringBetween(content, "itemprop=\"name\">", "</span>");
			
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
			
			@SuppressWarnings("unchecked")
			Map<String, String> imageMap = new CaseInsensitiveMap(); 
			String imageJson = StringUtils.substringBetween(content, "jQuery.parseJSON('", "'))");
			if(StringUtils.isNotBlank(imageJson)){
				JSONObject jsonObject = JSONObject.parseObject(imageJson);
				String optionLabels = jsonObject.getString("option_labels");
				JSONObject optionLabelsjsonObject = JSONObject.parseObject(optionLabels);
				Set<String> set = optionLabelsjsonObject.keySet();
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					String key = it.next();
					if("one size".equals(key)){
						break;
					}
					String keyStr = optionLabelsjsonObject.getString(key);
					JSONObject keyJsonObject = JSONObject.parseObject(keyStr);
					String configurable_product = keyJsonObject.getString("configurable_product");
					JSONObject configProJsonObject = JSONObject.parseObject(configurable_product);
					String image = configProJsonObject.getString("base_image");
					if(StringUtils.isNotBlank(image)){
						imageMap.put(key, image);
					}
				}
			}
			
			List<Image> imageList = new ArrayList<>();
			Elements es = doc.select(".arw-slick-slider a img");
			if(CollectionUtils.isNotEmpty(es)){
				for(Element e : es){
					String image = e.attr("src");
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
				}
			}
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			boolean display = true;
			
			String skus = StringUtils.substringBetween(content, "new Product.Config(", ");");
			if(StringUtils.isNotBlank(skus)){
				JSONObject jsonObject = JSONObject.parseObject(skus);
				String attributes = jsonObject.getString("attributes");
				JSONObject attrJsonObject = JSONObject.parseObject(attributes);
				String colorJson = attrJsonObject.getString("92");
				String sizeJson = attrJsonObject.getString("179");
				JSONObject colorJsonObject = JSONObject.parseObject(colorJson);
				JSONObject sizeJsonObject = JSONObject.parseObject(sizeJson);
				//String styleKey = colorJsonObject.getString("label");
				
				JSONArray jsonArray = colorJsonObject.getJSONArray("options");
			
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject optionJsonObject = jsonArray.getJSONObject(i);
					String colorVal = optionJsonObject.getString("label");
					String skuId = optionJsonObject.getString("id");
					JSONArray pdJsonArray = optionJsonObject.getJSONArray("products");
					//String sizeId = pdJsonArray.toString();
					JSONArray sizeJsonArray = sizeJsonObject.getJSONArray("options");
					for(int j = 0; j < sizeJsonArray.size(); j++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject sizejsonObj = sizeJsonArray.getJSONObject(j);
						String sizeVal = sizejsonObj.getString("label");
						String id = sizejsonObj.getString("id");
						
						JSONArray sizePdJsonArray = sizejsonObj.getJSONArray("products");
						//String colorSizeJson = sizePdJsonArray.toString();
						boolean stock = false;
						for(int s = 0; s < pdJsonArray.size(); s++){
							String pdsizeId = pdJsonArray.getString(s);
							for(int c = 0; c < sizePdJsonArray.size(); c ++){
								String pdcolorId = sizePdJsonArray.getString(c);
								if(StringUtils.isNotBlank(pdsizeId) && 
										pdsizeId.equals(pdcolorId)){
									stock = true;
									break;
								}
							}
						}
						
						int stock_status = 0;
						if(stock){
							stock_status = 1;
						}
						
						lSelectionList.setGoods_id(skuId+id+i+j);
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(unit);
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
						if(!styleMap.containsKey(colorVal)){
							LStyleList lStyleList = new LStyleList();
							lStyleList.setGood_id(skuId+id+i+j);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_switch_img("");
							lStyleList.setStyle_name(colorVal);
							if(display){
								context.getUrl().getImages().put(skuId+id+i+j, imageList);// picture
								lStyleList.setDisplay(display);
								int save = Math.round((1 - Float.parseFloat(salePrice) /  Float.parseFloat(origPrice)) * 100);// discount
								rebody.setPrice(new Price(Float.parseFloat(origPrice), save,  Float.parseFloat(salePrice), unit));
								display = false;
							}else{
								List<Image> images = new ArrayList<>();
								String image = imageMap.get(colorVal);
								if(StringUtils.isNotBlank(image)){
									images.add(new Image(image));
									context.getUrl().getImages().put(skuId+id+i+j, images);// picture
								}else{
									context.getUrl().getImages().put(skuId+id+i+j, imageList);// picture
								}
							}
							l_style_list.add(lStyleList);
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
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			cats.add("home");
			cats.add(title);
			breads.add("home");
			breads.add(title);
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select("#product_tabs_description_contents .std").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
}
