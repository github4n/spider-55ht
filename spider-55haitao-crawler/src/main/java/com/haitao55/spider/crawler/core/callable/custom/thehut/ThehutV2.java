package com.haitao55.spider.crawler.core.callable.custom.thehut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

public class ThehutV2 extends AbstractSelect{

	private static final String domain = "www.thehut.com";
	private static final String IMAGE_URL = "https://s1.thcdn.com/";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			if(content.contains("Sorry we couldn't find any results matching")){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+"item couldn't find any results matching..");
			}
			String productsJson = StringUtils.substringBetween(content, "<script type=\"application/ld+json\">", "</script>").trim();
			String brand = StringUtils.substringBetween(content, "productBrand: \"", "\"");
			String productId = null;
			JSONObject products = null;
			String title = null;
			JSONArray offers = null;
			String description = null;
			String gender = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productsJson)){
				products = JSONObject.parseObject(productsJson);
				title = products.getString("name");
				if(StringUtil.isBlank(gender)){
					gender = setGender(title,gender);
				}
				productId = products.getString("@id");
				description = products.getString("description");
				if(StringUtil.isBlank(gender)){
					gender = setGender(description,gender);
				}
				offers = products.getJSONArray("offers");
			}else{
				 throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"item Json data not found..");
			 }
			Document doc = Jsoup.parse(content);
			
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
			rebody.setTitle(new Title(title, "","",""));
			rebody.setBrand(new Brand(brand, "","",""));
				 	    
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			int spuStock = 0;
			Elements options = doc.select("fieldset.js-fieldSet-variations");
			if(CollectionUtils.isNotEmpty(options)){
				if(options.size() > 1){
					Elements sizeEs = doc.select("select#opts-1 option");
					Elements colorEs = doc.select("select#opts-2 option");
					boolean hasSizeSku = false;
					boolean hasColorSku = false;
					if(CollectionUtils.isNotEmpty(sizeEs) && sizeEs.size() > 2){
						hasSizeSku = true;
					}
					if(CollectionUtils.isNotEmpty(colorEs) && colorEs.size() > 2){
						hasColorSku = true;
					}			
						if(hasSizeSku && hasColorSku){
							String unit = StringUtils.substringBetween(content, "priceCurrency\":\"", "\",");
							setSkuWithTwoStyle(offers,rebody,spuStock,context,productId,sizeEs,l_selection_list,l_style_list,colorEs,unit);
						}else if(!hasSizeSku && hasColorSku){
							String unit = StringUtils.substringBetween(content, "priceCurrency\":\"", "\",");
							setSkuWithTwoStyle(offers,rebody,spuStock,context,productId,sizeEs,l_selection_list,l_style_list,colorEs,unit);
						}else if(hasSizeSku && !hasColorSku){
							String styleValue = colorEs.attr("rel");
							setSkuWithSingleStyle("color",sizeEs,offers,l_selection_list,l_style_list,productId,rebody,spuStock,styleValue);
							setSkuWithNoAndOneStyleImages(doc,context,products,productId);							
						}else if(!(hasSizeSku && !hasColorSku)){
							spuStock = setSkuWithNoStyle(offers,rebody,spuStock,context);
							setSkuWithNoAndOneStyleImages(doc,context,products,productId);
						}			
				}else{
					String styleName = options.get(0).select("legend").text();
				    if(styleName.toLowerCase().contains("colour")){
				    	Elements styleEs = options.get(0).select("select option");
						String styleValue = options.get(0).select("select option").attr("rel");
				    	setSkuWithSingleStyle("color",styleEs,offers,l_selection_list,l_style_list,productId,rebody,spuStock,styleValue);
				    	setSkuWithNoAndOneStyleImages(doc,context,products,productId);
				    }					
				}
			}else{
				spuStock = setSkuWithNoStyle(offers,rebody,spuStock,context);
				setSkuWithNoAndOneStyleImages(doc,context,products,productId);
				
			}		
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cateEs = doc.select("ul.breadcrumbs_container li.breadcrumbs_item");
			if(CollectionUtils.isNotEmpty(cateEs)){
				for(int i = 1; i < cateEs.size(); i++){
					String cate = cateEs.get(i).text();
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
						if(StringUtil.isBlank(gender)){
							gender = setGender(cate,gender);
						}
					}
				}
			}else{
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"Category & BreadCrumb not found..");
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements featureEs = doc.select(".column__left ul li");
			if(CollectionUtils.isNotEmpty(featureEs)){
				int count = 1;
				for(Element e : featureEs){
			    	featureMap.put("feature-"+count, e.text());
			    	count++;
			    }
			}else{
				featureMap.put("feature-1", description);
			}		    		
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
	private String setGender(String context,String gender){
		if(context.toLowerCase().contains("women") || context.toLowerCase().contains("woman")){
			gender = "women";
		}else if(context.toLowerCase().contains("men") || context.toLowerCase().contains("man")){
			gender = "men";
		}else if(context.toLowerCase().contains("girl")){
			gender = "girls";
		}else if(context.toLowerCase().contains("boy")){
			gender = "boys";
		}else if(context.toLowerCase().contains("kid")){
			gender = "kids";
		}
		return gender;
	}			
	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/57.0.2987.98 Chrome/57.0.2987.98 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		 headers.put("ADRUM", "isAjax:true");
		 headers.put("Host", "www.thehut.com");
		 headers.put("Origin", "https://www.thehut.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		 headers.put("Referer", context.getCurrentUrl());
		return headers;
	}
	private String getContent(Context context,String url,Map<String, Object> payload) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context)).method(HttpMethod.POST.getValue())
					.payload(payload)
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).retry(3).url(url).header(getHeaders(context)).method(HttpMethod.POST.getValue())
					.payload(payload).proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	 private void setSkuWithNoAndOneStyleImages(Document doc,Context context,JSONObject products,String productId){
		 List<Image> imageList = new ArrayList<>();		
		 Elements imgEs = doc.select("ul.list-menu.product-thumbnails.tac li.list-item a");
		 if(CollectionUtils.isNotEmpty(imgEs)){
			 for(Element e : imgEs){
			    	String imgUrl = e.attr("href").trim();
			    	imageList.add(new Image(imgUrl));
			 }
		 }else{
			 String imageUrl = products.getString("image");
			 if(StringUtils.isNotBlank(imageUrl)){
				 imageList.add(new Image(imageUrl));
			 }else{
				 throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"imageList not found.."); 
			 }			 
		 }		
		 context.getUrl().getImages().put(productId, imageList);// picture
	 }
	 private int setSkuWithNoStyle(JSONArray offers,RetBody rebody,int spuStock,Context context){
			JSONObject offer = offers.getJSONObject(0);
			String salePrice = offer.getString("price");
			String skuCurrency = offer.getString("priceCurrency");
			String instock = offer.getString("availability");
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"salePrice not found..");
			}
			rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), skuCurrency));
			
			if(StringUtils.isNotBlank(instock) && StringUtils.containsIgnoreCase(instock, "InStock")){
				spuStock = 1;
			}
			return spuStock;
	 }
	 private void setSkuWithSingleStyle(String skuType,Elements es,JSONArray offers,List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,String productId,RetBody rebody,int spuStock,String styleValue){
			boolean defaultSku = true;
			int count = 0;
			for(Element e : es){
				String value = e.text();
				if(StringUtils.isNotBlank(value)){
					if(value.equals("Please select")){
						continue;
					}
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject jsonObject = offers.getJSONObject(count);
					String skuSalePrice = jsonObject.getString("price");
					String skuOrigPrice = jsonObject.getString("rrp");
					String instock = jsonObject.getString("availability");
					String skuId = jsonObject.getString("sku");
					String skuCurrency = jsonObject.getString("priceCurrency");
					
					if(StringUtils.isBlank(skuOrigPrice)){
						skuOrigPrice = skuSalePrice;
					}
					
					int stock_status = 0;
					if(StringUtils.isNotBlank(instock) && 
							StringUtils.containsIgnoreCase(instock, "InStock")){
						stock_status = 1;
					}
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
					lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					lSelectionList.setPrice_unit(skuCurrency);
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id(styleValue);
					List<Selection> selections = new ArrayList<>();
					
						Selection selection = new Selection();
						selection.setSelect_name("size");						
						selection.setSelect_value(value);
						selections.add(selection);

					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
					
					if(defaultSku){							
						int save = Math.round((1 - Float.parseFloat(skuSalePrice) /  Float.parseFloat(skuOrigPrice)) * 100);// discount
						rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), save,  Float.parseFloat(skuSalePrice), skuCurrency));							
						defaultSku = false;
					}
					count ++;
				}
			}
			
			LStyleList lStyleList = new LStyleList();
			lStyleList.setGood_id(productId);
			lStyleList.setDisplay(true);
			lStyleList.setStyle_cate_name(skuType);		
			lStyleList.setStyle_cate_id(0);
			lStyleList.setStyle_id(styleValue);
			lStyleList.setStyle_name(styleValue);
			lStyleList.setStyle_switch_img("");
			l_style_list.add(lStyleList);
			if(l_selection_list != null && l_selection_list.size() > 0){
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
		
	 }
	 private void setSkuWithTwoStyle(JSONArray offers,RetBody rebody,int spuStock,Context context,String productId,Elements sizeEs,List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,Elements colorEs,String unit) throws Exception{
			String url = "https://www.thehut.com/variations.json?productId="+productId;
			boolean imageFlag = true;
			Set<String> colorStyle = new HashSet<String>();
			for(Element e : sizeEs){
				String sizeValue = e.text();
				String sizeCode = e.attr("value");
				if(StringUtils.isNotBlank(sizeCode)){
					if(sizeValue.equals("Please select")){
						continue;
					}
					Map<String, Object> sizePayload = new HashMap<String, Object>();
					sizePayload.put("selected", "2");
					sizePayload.put("variation1", "1");
					sizePayload.put("option1", sizeCode);
					String sizeRs = getContent(context,url,sizePayload);
					JSONObject sizeJsonObject = JSONObject.parseObject(sizeRs);
					JSONArray colorArray = sizeJsonObject.getJSONArray("variations");
					for(int i = 0; i < colorArray.size(); i++){
						JSONObject json = colorArray.getJSONObject(i);
						String jsonType = json.getString("variation");
						if(jsonType.toLowerCase().equals("colour")){
							JSONArray colorOptionArray = json.getJSONArray("options");
							for(int j = 0; j < colorOptionArray.size(); j++){
								JSONObject colorOption = colorOptionArray.getJSONObject(j);
								String colorValue = colorOption.getString("value");
								String colorCode = colorOption.getString("id");
								
								Map<String, Object> payload = new HashMap<String, Object>();
								payload.put("selected", "2");
								payload.put("variation1", "1");
								payload.put("option1", sizeCode);
								payload.put("variation2", "2");
								payload.put("option2", colorCode);								

								LSelectionList lSelectionList = new LSelectionList();
								String rs = getContent(context,url,payload);
								JSONObject jsonObject = JSONObject.parseObject(rs);
								String skuSalePrice = jsonObject.getString("price");
								String skuOrigPrice = jsonObject.getString("rrp");
								String skuId = jsonObject.getString("productId");
								String instock = jsonObject.getString("availabilityPrefix");
								if(StringUtils.isNotBlank(skuSalePrice)){
									skuSalePrice = skuSalePrice.replace("&#163;", "").replace(",", "");
								}
								if(StringUtils.isBlank(skuOrigPrice)){
									skuOrigPrice = skuSalePrice;
								}
								
								int stock_status = 0;
								if(StringUtils.isNotBlank(instock) && 
										StringUtils.containsIgnoreCase(instock, "In stock")){
									stock_status = 1;
								}
								lSelectionList.setGoods_id(skuId);
								lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
								lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
								lSelectionList.setPrice_unit(unit);
								lSelectionList.setStock_status(stock_status);
								lSelectionList.setStyle_id(colorValue);
								List<Selection> selections = new ArrayList<>();
								if(StringUtils.isNotBlank(sizeValue)){
									Selection selection = new Selection();
									selection.setSelect_name("size");
									selection.setSelect_value(sizeValue);
									selections.add(selection);
								}
								lSelectionList.setSelections(selections);
								l_selection_list.add(lSelectionList);	
								LStyleList lStyleList = new LStyleList();
								lStyleList.setGood_id(skuId);					
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_id(colorValue);
								lStyleList.setStyle_name(colorValue);
								lStyleList.setStyle_switch_img("");
								JSONArray jsonArray = jsonObject.getJSONArray("images");
								List<Image> imageList = new ArrayList<>();		
								for(int k = 0; k < jsonArray.size(); k++){
									JSONObject imageJsonObj = jsonArray.getJSONObject(k);
									String type = imageJsonObj.getString("type");
									if("zoom".equals(type)){
										String image = imageJsonObj.getString("name");
										imageList.add(new Image(IMAGE_URL+image));
									}
								}
								if(imageFlag){									
									int save = Math.round((1 - Float.parseFloat(skuSalePrice) /  Float.parseFloat(skuOrigPrice)) * 100);// discount
									rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), save,  Float.parseFloat(skuSalePrice), unit));	
									lStyleList.setDisplay(true);	
									imageFlag = false;
								}else{
									lStyleList.setDisplay(false);
								}
								if(colorStyle.isEmpty()){
									l_style_list.add(lStyleList);
									colorStyle.add(colorValue);
									context.getUrl().getImages().put(skuId, imageList);// picture
								}else{
									if(!colorStyle.contains(colorValue)){
										l_style_list.add(lStyleList);
										colorStyle.add(colorValue);
										context.getUrl().getImages().put(skuId, imageList);// picture
									}
								}																																																														
							}
						}
					}
				}
			}
			if(l_selection_list != null && l_selection_list.size() > 0){
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
	 }
}
