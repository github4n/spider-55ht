package com.haitao55.spider.crawler.core.callable.custom.ebags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * ebags网站收录
 * @author denghuan
 *
 */
public class Ebags extends AbstractSelect{
	
	private static final String domain = "www.ebags.com";
	
	//private String CDN_IMAGE = "http://cdn1.ebags.com/is/image/im6/###?hei=600&wid=600";

	private String IMAGE_API = "http://externalservice.ebags.com/richmediaservice/api/richmediasets/";

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String skus = StringUtils.substringBetween(content, " var skus = ", ";");
			String brand = StringUtils.substringBetween(content, "brand\": \"", "\"");
			String title = StringUtils.substringBetween(content, "productName\": \"", "\",");
			//String productId = StringUtils.substringBetween(content, "productId\":", ",");
			String masterId = StringUtils.substringBetween(content, "masterId\": \"", "\"");
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String zoomImage = doc.select("img#pdZoomImage").attr("data-original");
			String puffixImage = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(zoomImage)){
				String puffix = zoomImage.substring(zoomImage.indexOf("//"), zoomImage.lastIndexOf("/"));
				puffixImage = "http:"+puffix+"/###"+"?hei=600&wid=600";
			}
			
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 Elements cateEs = doc.select("#jsBreadcrumbs ul li a");
			
			 for(int i = 1; i < cateEs.size(); i++){
				 String cate = cateEs.get(i).text();
				 if(StringUtils.isNotBlank(cate) && !cate.equals("...")){
					 cats.add(cate);
					 breads.add(cate);
				 }
			 }
			 rebody.setCategory(cats);
			 rebody.setBreadCrumb(breads);
			
			List<Image> imgList = new ArrayList<>();
			if(StringUtils.isNotBlank(masterId)){
				String imageUrl = IMAGE_API+masterId;
				Url currentUrl = new Url(imageUrl);
				currentUrl.setTask(context.getUrl().getTask());
				String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
				if(StringUtils.isNotBlank(skuJson)){
					String modelDetailAssets = StringUtils.substringBetween(skuJson, "ModelDetailAssets\":[", "]");
					String imageIds = modelDetailAssets.replaceAll("[\" ]", "");
					String[] imageId = imageIds.split(",");
					for(String id : imageId){
						if(StringUtils.isNotBlank(id)){
							String image = puffixImage.replace("###", id);
							 if(StringUtils.isNotBlank(image)){
								 imgList.add(new Image(image));
							 }
						}
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String, JSONObject> skuStyle = new HashMap<String, JSONObject>();
			String defaultColorName = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(skus)){
				JSONArray jsonArray = JSONArray.parseArray(skus);
				for(int i = 0; i < jsonArray.size(); i++){
					 boolean needFixData =false;
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject productsObject =  jsonArray.getJSONObject(i);
					String Pricing = productsObject.getString("Pricing");
					JSONObject priceJsonObject = JSONObject.parseObject(Pricing);
					String skuId = productsObject.getString("SkuId");
					String value = productsObject.getString("ColorName");
					String ColorName = StringUtils.EMPTY;
					if(value.contains("-")){
						ColorName = value.split("-")[1].trim();
						needFixData = true;
					}else{
						ColorName = value;
						needFixData = false;
					}
					String retailPrice= priceJsonObject.getString("RetailPrice");
					String ourPrice= priceJsonObject.getString("OurPrice");
					String salePrice = priceJsonObject.getString("SalePrice");
					String isSelected = productsObject.getString("IsSelected");
					String isStock = productsObject.getString("IsInWishlist");
					String imageId = StringUtils.substringBetween(productsObject.toString(), "IpsId\":\"", "\"");
					
					lSelectionList.setGoods_id(skuId);
					if(StringUtils.isNotBlank(salePrice) && !"null".equals(salePrice)){
						salePrice = salePrice.replaceAll("[$, ]", "");
					}
					if(StringUtils.isNotBlank(ourPrice) && !"null".equals(ourPrice)){
						ourPrice = ourPrice.replaceAll("[$, ]", "");
					}
					if(StringUtils.isNotBlank(retailPrice) && !"null".equals(retailPrice)){
						retailPrice = retailPrice.replaceAll("[$, ]", "");
					}
					
						if(StringUtils.isNotBlank(salePrice) &&
								StringUtils.isNotBlank(retailPrice)){
							lSelectionList.setSale_price(Float.parseFloat(salePrice));
							lSelectionList.setOrig_price(Float.parseFloat(retailPrice));
						}else if((StringUtils.isBlank(salePrice) || "null".equals(salePrice)) && 
								StringUtils.isNotBlank(ourPrice)){
							lSelectionList.setSale_price(Float.parseFloat(ourPrice));
							lSelectionList.setOrig_price(Float.parseFloat(ourPrice));
						}else if(StringUtils.isNotBlank(salePrice) && 
								StringUtils.isNotBlank(ourPrice)){
							lSelectionList.setSale_price(Float.parseFloat(salePrice));
							lSelectionList.setOrig_price(Float.parseFloat(ourPrice));
						}

					lSelectionList.setPrice_unit(unit);
					
					int stock_status = 0;
					if(StringUtils.isNotBlank(isStock) && 
							isStock.equals("false")){
						stock_status = 1;
					}
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id(ColorName);
					List<Selection> selections = new ArrayList<>();
					String sku_size = StringUtils.EMPTY;
					if(needFixData){
						sku_size = value.split("-")[0].trim();
					}else{
						Elements labelNames = doc.select("div.lbl");		
						int count = 0;
						for(Element element : labelNames){
							String labelName = element.text();
							if(StringUtils.isNotBlank(labelName) && labelName.contains("Size")){
								sku_size = doc.select("div.specs").get(count).text();
								break;
							}else{
								count ++;
								continue;
							}
						}
						
					}				
					if (StringUtils.isNotBlank(sku_size)) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(sku_size);
						selections.add(selection);
					}
                    lSelectionList.setSelections(selections);
					
					List<Image> images = new ArrayList<>();
					String image = puffixImage.replace("###", imageId);
					if("true".equals(isSelected)){
						defaultColorName = ColorName;
						LStyleList lStyleList = new LStyleList();
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id(ColorName);
						lStyleList.setStyle_name(ColorName);
						lStyleList.setStyle_switch_img("");
						lStyleList.setDisplay(true);
						if(CollectionUtils.isEmpty(imgList)){
							images.add(new Image(image));
							context.getUrl().getImages().put(skuId, images);// picture
						}else{
							images.add(new Image(image));
							images.addAll(imgList);
							context.getUrl().getImages().put(skuId, images);// picture
						}

						if(StringUtils.isNotBlank(salePrice) &&
								StringUtils.isNotBlank(retailPrice)){
							int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(retailPrice)) * 100);// discount
							rebody.setPrice(new Price(Float.valueOf(retailPrice), save, Float.valueOf(salePrice), unit));
						}else if((StringUtils.isBlank(salePrice) || "null".equals(salePrice)) && 
								StringUtils.isNotBlank(ourPrice)){
							rebody.setPrice(new Price(Float.valueOf(ourPrice), 0, Float.valueOf(ourPrice), unit));
						}else if(StringUtils.isNotBlank(salePrice) && 
								StringUtils.isNotBlank(ourPrice)){
							int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(ourPrice)) * 100);// discount
							rebody.setPrice(new Price(Float.valueOf(ourPrice), save, Float.valueOf(salePrice), unit));
						}
						l_style_list.add(lStyleList);
					}else{
						 images.add(new Image(image));
						 context.getUrl().getImages().put(skuId, images);// picture
					}
					skuStyle.put(ColorName, productsObject);									
					l_selection_list.add(lSelectionList);
				}
			}
			for(String colorName : skuStyle.keySet()){
				if(colorName.equals(defaultColorName)){
					continue;
				}
				JSONObject productsObject =  skuStyle.get(colorName);
				LStyleList lStyleList = new LStyleList();
				String skuId = productsObject.getString("SkuId");
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(colorName);
				lStyleList.setStyle_name(colorName);
				lStyleList.setStyle_switch_img("");
				List<Image> images = new ArrayList<>();
				String imageId = StringUtils.substringBetween(productsObject.toString(), "IpsId\":\"", "\"");
				String image = puffixImage.replace("###", imageId);	
				images.add(new Image(image));
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
			
			 Map<String, Object> propMap = new HashMap<String, Object>();
			 propMap.put("s_gender", "");
			 Map<String, Object> featureMap = new HashMap<String, Object>();
			 Map<String, Object> descMap = new HashMap<String, Object>();
			 
			 String desc = doc.select(".half-con .mobileSlider").text();
			 String  featureDesc = doc.select("ul.spaced-list li span").text();
			 
			 Elements featureEs = doc.select("ul.spaced-list li span");
			 int count = 0;
			 for(int i = 0; i < featureEs.size()-1; i++){
				 String feature = featureEs.get(i).text();
				 if(StringUtils.isNotBlank(feature)){
					 count ++;
					 featureMap.put("feature-"+count, feature);
				 }
			 }
			  String pops = doc.select(".for-jsSpecifications").text();
			  rebody.setProperties(propMap);
				
			  rebody.setFeatureList(featureMap);
			  descMap.put("en", desc+featureDesc+pops);
			  rebody.setDescription(descMap);
			  rebody.setSku(sku);	
		}
		setOutput(context, rebody);
	}

}
