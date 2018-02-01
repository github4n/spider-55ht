package com.haitao55.spider.crawler.core.callable.custom.tommy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Tommy网站收录
 * @author denghuan
 *
 */
public class Tommy_bak extends AbstractSelect{

	private static final String domain = "usa.tommy.com";
	private static final String TOMMY_API = "http://usa.tommy.com/webapp/wcs/stores/servlet/GetCatalogEntryDetailsByID?";
	private static final String IMAGE_PREFIX ="http://shoptommy.scene7.com/is/image/ShopTommy/";
	private static final String IMAGE_SUFFIX ="?wid=700&hei=700&fmt=jpeg";
	private static final String[] ARRAY = {"BCK","DE1","DE2","DE3"};
	
	@Override
	public void invoke(Context context) throws Exception {
		//String content = this.getInputString(context);
		String content = Crawler.create().timeOut(30000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
				.resultAsString();
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document docment = Jsoup.parse(content);
			String error = docment.select("h2.inventoryOutStats").text();
			if(StringUtils.isNotBlank(error) &&
					StringUtils.containsIgnoreCase(error, "We're sorry, this product was so popular, we've sold out")){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			String productId = StringUtils.substringBetween(content, "browse_products=[\"", "\"");
			String catalogId = StringUtils.substringBetween(content, "var catalogId='", "';");
			String langId = StringUtils.substringBetween(content, "var langId='", "';");
			String storeId = StringUtils.substringBetween(content, "var storeId='", "';");
			String skus = StringUtils.substringBetween(content, "white-space:pre;\">", "</div>");
			String altImages = StringUtils.substringBetween(content, "altImages.colors = ", ";");
			String brand = StringUtils.substringBetween(content, "brand\" content=\"","\"/");
			String title = docment.select("span#catalog_link").text();
			//String salePrice = docment.select("span.price").text();
			//String listPrice = docment.select("span#listPrice").text();
			String defaultSku = docment.select(".productswatches li.active").attr("title");
			String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
			if(StringUtils.isBlank(unit)){
				unit = StringUtils.substringBetween(content, "og:price:currency\" content=\"", "\"");
			}
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(SpiderStringUtil.md5Encode(context.getCurrentUrl()));
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Map<String,String> imageMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(altImages)){
				JSONArray imageJsonArray = JSONArray.parseArray(altImages);
				for(int i =0; i < imageJsonArray.size();i++ ){
					JSONObject imageJsonObject =  imageJsonArray.getJSONObject(i);
					String imageSkuId = StringUtils.substringBetween(imageJsonObject.toString(), "catentryId\":\"", "\"");
					String hoverImage = StringUtils.substringBetween(imageJsonObject.toString(), "hoverImage\":\"", "&qlt");
					if(StringUtils.isNotBlank(imageSkuId) &&
							StringUtils.isNotBlank(hoverImage)){
						imageMap.put(imageSkuId, hoverImage);
					}
				}	
			}
			
			 Sku sku = new Sku();
			 List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			 List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			 Map<String,String> styleMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(skus)){
				JSONArray jsonArray = JSONArray.parseArray(skus);
				for(int i =0; i < jsonArray.size();i++ ){
					JSONObject skusObject =  jsonArray.getJSONObject(i);
					String skuId = skusObject.getString("catentry_id");
					String inventory = skusObject.getString("inventory");
					
					if(StringUtils.isNotBlank(inventory) &&
							(inventory.equals("0.0") || inventory.equals("0"))){
						continue;
					}
					String colorCode = StringUtils.substringBetween(skusObject.toString(), "ColorCode\":\"", "\"");
					LSelectionList lSelectionList = new LSelectionList();
					List<Selection> selections = new ArrayList<>();
					
					String apiUrl = TOMMY_API+"storeId="+storeId+"&langId="+langId+"&catalogId="+catalogId+"&productId="+skuId+"&onlyCatalogEntryPrice=true";
					Url currentUrl = new Url(apiUrl);
					currentUrl.setTask(context.getUrl().getTask());
					String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
					String skuSalePrice = StringUtils.substringBetween(skuJson, "offerPrice\": \"", "\"");
					String skuListPrice = StringUtils.substringBetween(skuJson, "listPrice\": \"", "\"");
					
					if(StringUtils.isNotBlank(skuSalePrice) && StringUtils.isNotBlank(skuListPrice)){
						skuSalePrice = skuSalePrice.replaceAll("[$,]", "");
						skuListPrice = skuListPrice.replaceAll("[$,]", "");
						lSelectionList.setOrig_price(Float.parseFloat(skuListPrice));
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					}else if(StringUtils.isNotBlank(skuSalePrice) && StringUtils.isBlank(skuListPrice)){
						skuSalePrice = skuSalePrice.replaceAll("[$,]", "");
						lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					}
					 
					List<Image> hoverImageList = new ArrayList<>();
					hoverImageList.add(new Image(imageMap.get(skuId)));
					
					String attributes = skusObject.getString("Attributes");
					JSONObject colorSizeObject = JSON.parseObject(attributes);
					Set<String> skuKey = colorSizeObject.keySet();
					Iterator<String> it = skuKey.iterator();
					while(it.hasNext()){
						String key = it.next();
						if(StringUtils.isNotBlank(key) && 
								StringUtils.containsIgnoreCase(key, "_")){
							String[] spt = StringUtils.split(key, "_");
							if(StringUtils.containsIgnoreCase(spt[0], "Size")){
								if(StringUtils.containsIgnoreCase(spt[1], "/")){
									String length = spt[1];
									String[] len =length.split("/");
									setSelects(len[1],"length",selections);
									setSelects(len[0],"Size",selections);
								}else{
									setSelects(spt[1],spt[0],selections);
								}
							}else{
								
								if(!styleMap.containsKey(spt[1])){
									LStyleList lStyleList = new LStyleList();
									setStyleList(lStyleList,spt[1],skuId);
									for(String arr : ARRAY){
										String imageUrl = IMAGE_PREFIX+productId+"_"+colorCode+"_"+arr+IMAGE_SUFFIX;
										boolean image_exists = image_exists(imageUrl,context);
										if(image_exists){//main　为主图，肯定存在，减少发送请求次数
											hoverImageList.add(new Image(imageUrl));
										}
									}
									if(StringUtils.isNotBlank(defaultSku)){
										if(defaultSku.equals(spt[1])){
											lStyleList.setDisplay(true);
											if(StringUtils.isNotBlank(skuSalePrice) && 
													StringUtils.isNotBlank(skuListPrice)){
												 int save = Math.round((1 - Float.parseFloat(skuSalePrice) / Float.parseFloat(skuListPrice)) * 100);// discount
												 rebody.setPrice(new Price(Float.parseFloat(skuListPrice), save, Float.parseFloat(skuSalePrice), unit));
											}else if(StringUtils.isNotBlank(skuSalePrice) && 
													StringUtils.isBlank(skuListPrice)){
												 rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 0, Float.parseFloat(skuSalePrice), unit));
											}
											context.getUrl().getImages().put(skuId, hoverImageList);// picture
										}else{
											context.getUrl().getImages().put(skuId, hoverImageList);// picture
										}
									}
									l_style_list.add(lStyleList);
								}
								lSelectionList.setStyle_id(spt[1]);
								styleMap.put(spt[1], spt[1]);
								
							}
						}
					}
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setSelections(selections);
					lSelectionList.setStock_status(1);
					lSelectionList.setGoods_id(skuId);
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
			 }
			 rebody.setStock(new Stock(spuStock));
			 
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 
			 Elements es = docment.select("#breadcrumb a");
			 for(Element e : es){
				 String cate = e.text();
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
			 
			 String description = docment.select(".itemDescription").text();
			 //String descriptionHtml = docment.select(".itemDescription").html();
			 int count = 0;
			 if(StringUtils.isNotBlank(description)){
				 String[] dscBr = description.split("• ");
				 for(String br : dscBr){
					 if(StringUtils.isNotBlank(br)){
						 count ++;
						 featureMap.put("feature-"+count, br);
					 }
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", description);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
			 
		}
		setOutput(context, rebody);
	}
	
	/**
	 * 验证图片是否存在
	 * @param image_url
	 * @param context
	 * @return
	 */
	private boolean image_exists(String image_url, Context context) {
		try {
			Url url = context.getUrl();
			String proxyRegionId = url.getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				Crawler.create().timeOut(15000).url(image_url).proxy(false).resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String ip = proxy.getIp();
				int port = proxy.getPort();
				Crawler.create().timeOut(20000).url(image_url).proxy(true).proxyAddress(ip)
				.proxyPort(port).resultAsString();
			}
			return true;
		} catch (ClientProtocolException e) {
		} catch (HttpException e) {
			if(200!=e.getStatus()){
				return false;
			}
		} catch (IOException e) {
		}
		return false;
	}
	
	
	private void setSelects(String value,String name,List<Selection> selections){
		Selection selection = new Selection();
		selection.setSelect_name(name);
		selection.setSelect_value(value);
		selections.add(selection);
	}
	
	private void setStyleList(LStyleList lStyleList,String color,String skuId){
		lStyleList.setGood_id(skuId);
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_switch_img("");
		lStyleList.setStyle_cate_name("color");	
		lStyleList.setStyle_id(color);
		lStyleList.setStyle_name(color);
		
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		context.setCurrentUrl("http://usa.tommy.com/en/women/SHIRTS-BLOUSES-WOMEN/stripe-dot-shirt-ww17797");
		Tommy tomm = new Tommy();
		tomm.invoke(context);
	}

}
