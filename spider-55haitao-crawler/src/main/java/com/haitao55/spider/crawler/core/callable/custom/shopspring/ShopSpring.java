package com.haitao55.spider.crawler.core.callable.custom.shopspring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
 * ShopSpring网站收录
 * date:2017-2.10
 * @author denghuan
 *
 */
public class ShopSpring extends AbstractSelect{

	private static final String domain = "www.shopspring.com";
	
	@SuppressWarnings("unused")
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			String unit = StringUtils.substringBetween(content, "priceCurrency\": \"", "\"");
			//String shopValue = StringUtils.substringBetween(content, "<script type=\"application/ld+json\">","</script>");
			//String title = StringUtils.substringBetween(shopValue, "Product\", \"name\": \"", "\"");
			//String brand = StringUtils.substringBetween(shopValue, "Organization\", \"name\": \"", "\"");
			String productJson = StringUtils.substringBetween(content, "window.injectedProduct =", "};");
			Map<String,String>  dealutSkuMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(productJson)){
				productJson = productJson.concat("}");
				JSONObject jsonObject = JSONObject.parseObject(productJson);
				String author = jsonObject.getString("author");
				JSONObject authorJsonObject = JSONObject.parseObject(author);
				String brand = authorJsonObject.getString("name");
				String productId = jsonObject.getString("id");
				String title = jsonObject.getString("name");
				String gender = jsonObject.getString("gender");
				String desc = jsonObject.getString("more_info");
				
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
				
				String origPrice = jsonObject.getString("original_price");
				String salePrice = jsonObject.getString("price");	
				JSONArray dimensionsJsonArry = jsonObject.getJSONArray("dimensions");
				JSONArray inventoryJsonArray = jsonObject.getJSONArray("inventory");
				JSONArray imagesJsonArray = jsonObject.getJSONArray("images");
				
				Map<String,List<Image>> imageMap = new HashMap<>();
				List<Image> images = new ArrayList<>();
				for(int m = 0; m < imagesJsonArray.size(); m++){
					JSONObject imagesObject = imagesJsonArray.getJSONObject(m);
					String image = imagesObject.getString("url");
					if(imagesObject.containsKey("attributes")){
						String attributes = imagesObject.getString("attributes");
						if(attributes != null){
							JSONObject attrJsonObject = JSONObject.parseObject(attributes);
							if(attrJsonObject.containsKey("Color")){
								JSONArray colorJsonArray = attrJsonObject.getJSONArray("Color");
								if(colorJsonArray != null){
									for(int c = 0; c < colorJsonArray.size(); c++){
										String colorValue = colorJsonArray.getString(c);
										if(!imageMap.containsKey(colorValue)){
											images = new ArrayList<>();
										}
										images.add(new Image(image));
										imageMap.put(colorValue, images);
									}
								}
							}else{
								images.add(new Image(image));
								imageMap.put("Color", images);
							}
						}else{
							images.add(new Image(image));
							imageMap.put("Color", images);
						}
					
					}else{
						images.add(new Image(image));
						imageMap.put("Color", images);
					}
				}
				
				for(int i = 0;i < dimensionsJsonArry.size(); i++){
					JSONObject dimensionsObject = dimensionsJsonArry.getJSONObject(i);
					String name = dimensionsObject.getString("name");
					if(dimensionsObject.containsKey("default")){
						String value = dimensionsObject.getString("default");
						dealutSkuMap.put(name, value);
					}
				}
				
				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				Map<String,String> styleMap = new HashMap<>();
				boolean display = true;
				for(int j = 0;j < inventoryJsonArray.size(); j++){
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject inventoryObject = inventoryJsonArray.getJSONObject(j);
					String instock = inventoryObject.getString("count");
					String attributes = inventoryObject.getString("attributes");
					String skuId = inventoryObject.getString("true_variant_id");
					JSONObject attrJsonObject = JSONObject.parseObject(attributes);
					int stock_status = 0;
					if(StringUtils.isNotBlank(instock) && 
							!"0".equals(instock)){
						stock_status = 1;
					}
					
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(origPrice));
					lSelectionList.setSale_price(Float.parseFloat(salePrice));
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setPrice_unit(unit);
					String colorVal = StringUtils.EMPTY;
					if(attrJsonObject.containsKey("Color")){
						colorVal = attrJsonObject.getString("Color");
						lSelectionList.setStyle_id(colorVal);
					}else{
						lSelectionList.setStyle_id("default");
					}
					List<Selection> selections = new ArrayList<>();
					String widthVal = StringUtils.EMPTY;
					if(attrJsonObject.containsKey("Width")){
						Selection selection = new Selection();
						widthVal = attrJsonObject.getString("Width");
						selection.setSelect_name("Width");
						selection.setSelect_value(widthVal);
						selections.add(selection);
					}
					if(attrJsonObject.containsKey("Size")){
						Selection selection = new Selection();
						String sizeVal = attrJsonObject.getString("Size");
						selection.setSelect_name("Size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
					}
					lSelectionList.setSelections(selections);
					
					if(StringUtils.isNotBlank(colorVal)){
						if(!styleMap.containsKey(colorVal)){
							LStyleList lStyleList = new LStyleList();
							if(attrJsonObject.containsKey("Color") && 
									attrJsonObject.containsKey("Width")){
								String dealutColorVal = dealutSkuMap.get("Color");
								String dealutWidthVal = dealutSkuMap.get("Width");
								if(colorVal.equals(dealutColorVal) && 
										widthVal.equals(dealutWidthVal)){
									setSpuPrice(origPrice,salePrice,unit,rebody,lStyleList);
								}
							}else if(attrJsonObject.containsKey("Color")  && 
									!attrJsonObject.containsKey("Width")){
								String dealutColorVal = dealutSkuMap.get("Color");
								if(colorVal.equals(dealutColorVal)){
									setSpuPrice(origPrice,salePrice,unit,rebody,lStyleList);
								}
							}else if(!attrJsonObject.containsKey("Color")  && 
									attrJsonObject.containsKey("Width")){
								String dealutWidthVal = dealutSkuMap.get("Width");
								if(widthVal.equals(dealutWidthVal)){
									setSpuPrice(origPrice,salePrice,unit,rebody,lStyleList);
								}
							}
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("Color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							l_style_list.add(lStyleList);
							List<Image> imageList = imageMap.get(colorVal);
							if(CollectionUtils.isEmpty(imageList)){
								imageList = imageMap.get("Color");
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
						}
					}else{
						if(display){
							LStyleList lStyleList = new LStyleList();
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("Color");
							lStyleList.setStyle_id("default");
							lStyleList.setStyle_name("default");
							lStyleList.setStyle_switch_img("");
							l_style_list.add(lStyleList);
							setSpuPrice(origPrice,salePrice,unit,rebody,lStyleList);
							display = false;
							List<Image> imageList = imageMap.get(widthVal);
							if(CollectionUtils.isEmpty(imageList)){
								imageList = imageMap.get("Color");
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
						}
					}
					styleMap.put(colorVal, colorVal);
					l_selection_list.add(lSelectionList);
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
				
				JSONArray cateJsonArray = jsonObject.getJSONArray("frontend_taxonomy");
				if(cateJsonArray.size() > 0 && cateJsonArray != null){
					for(int i = cateJsonArray.size()-1; i >= 0; i--){
						JSONArray lastCateJsonArray = cateJsonArray.getJSONArray(i);
						for(int j = 0; j < lastCateJsonArray.size(); j++){
							JSONObject cateJsonObject = lastCateJsonArray.getJSONObject(j);
							String cate = cateJsonObject.getString("display_name");
							if(StringUtils.isNotBlank(cate)){
								cats.add(cate);
								breads.add(cate);
							}
						}
						break;
					}
				}
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				featureMap.put("feature-1", desc);
				rebody.setFeatureList(featureMap);
				descMap.put("en", desc);
				rebody.setDescription(descMap);
				Map<String, Object> propMap = new HashMap<String, Object>();
				propMap.put("s_gender", gender);
				rebody.setProperties(propMap);
				rebody.setSku(sku);
			}
		}
		setOutput(context, rebody);
	}
	
	/**
	 * set 默认价格
	 * @param origPrice
	 * @param salePrice
	 * @param unit
	 * @param rebody
	 * @param lStyleList
	 */
	private void setSpuPrice(String origPrice,String salePrice,String unit ,RetBody rebody,LStyleList lStyleList){
		lStyleList.setDisplay(true);
		if(StringUtils.isNotBlank(origPrice) && 
				StringUtils.isNotBlank(salePrice)){
			 int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
			 rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
		}
	}
}
