package com.haitao55.spider.crawler.core.callable.custom.coach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.crawler.utils.Constants;

public class Coach extends AbstractSelect{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.coach.com";
	
	public void invoke(Context context) throws Exception {
		
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url);
		
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "productId = \"", "\"");
			String unit = StringUtils.substringBetween(content, "priceCurrency\": \"", "\"");
			String title = doc.select(".product-name-desc").text();
			
			String docid = StringUtils.EMPTY;
			if(!url.equals(context.getCurrentUrl())){
				logger.info("origin url {} is diff from response url,url {}",url,context.getCurrentUrl());
			}
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(url);
			}			
			String url_no = SpiderStringUtil.md5Encode(url);
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("COACH", ""));
			
			String origPrice = doc.select(".product-info-master span.strike-through").text();
			String salePrice = doc.select(".product-info-master span.sales").text();
			
			salePrice = pattern(salePrice);
			origPrice = pattern(origPrice);
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$ ]", "");
			}
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$ ]", "");
			
			List<Image> imageList = new ArrayList<>();
			
			Elements imgEs = doc.select("ul.swiper-wrapper li.swiper-slide");
			for(Element e : imgEs){
				String image = e.attr("data-regular-size");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			String defulteColor = doc.select(".pdp-main__swatches li.active img.pdpSwatch").attr("title");

			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			if(StringUtils.isNotBlank(defulteColor)){
				Elements  es = doc.select(".pdp-main__swatches li");
				if(es != null && es.size() > 0){
					for(Element  e : es){
						String skuId = e.attr("data-style-color");
						String defaultImg = e.attr("data-default-image");
						String pdpUrl = e.select("a").attr("href");
						String colorVal = e.select("img").attr("title");
						if(StringUtils.isNotBlank(pdpUrl)){
							String html = crawlerUrl(context,pdpUrl);
							if(StringUtils.isNotBlank(html)){
								
								JSONObject jsonObject = JSONObject.parseObject(html);
								String product = jsonObject.getString("product");
								JSONObject productJson = JSONObject.parseObject(product);
								String price = productJson.getString("price");
								JSONObject priceJson = JSONObject.parseObject(price);
								String sales = priceJson.getString("sales");
								String salePirce = StringUtils.substringBetween(sales, "value\":","}");
								if(StringUtils.isBlank(salePirce)){
									salePirce = StringUtils.substringBetween(sales, "value\":",",");
								}
								String origPirce = StringUtils.EMPTY;
								if(priceJson.containsKey("list")){
									String list = priceJson.getString("list");
									origPirce = StringUtils.substringBetween(list, "value\":","}");
									if(StringUtils.isBlank(origPirce)){
										origPirce = StringUtils.substringBetween(list, "value\":",",");
									}
								}
								
								if(StringUtils.isBlank(origPirce)){
									origPirce = salePirce;
								}
								
								String colorSKuId = StringUtils.EMPTY;
								boolean sizeIsExist = false;
								
								JSONArray jsonArray = productJson.getJSONArray("variationAttributes");
								for(int i = 0;i < jsonArray.size(); i++){
									JSONObject skuJsonObj = jsonArray.getJSONObject(i);
									String colorKey = skuJsonObj.getString("attributeId");
									if(StringUtils.isNotBlank(colorKey)){
										if("size".equals(colorKey)){
											JSONArray valueJsonArr = skuJsonObj.getJSONArray("values");
											for(int j = 0; j < valueJsonArr.size(); j++){
												LSelectionList lSelectionList = new LSelectionList();
												JSONObject sizeJsonObj = valueJsonArr.getJSONObject(j);
												String sizeVal = sizeJsonObj.getString("displayValue");
												String selectable = sizeJsonObj.getString("selectable");
												int stock_status = 0;
												if(StringUtils.equals(selectable, "true")){
													stock_status = 1;
												}
												colorSKuId = skuId+sizeVal;
												lSelectionList.setGoods_id(colorSKuId);
												lSelectionList.setPrice_unit(unit);
												lSelectionList.setOrig_price(Float.parseFloat(origPirce));
												lSelectionList.setSale_price(Float.parseFloat(salePirce));
												lSelectionList.setStyle_id(colorVal);
												lSelectionList.setStock_status(stock_status);
												List<Selection> selections = new ArrayList<>();
												Selection selection = new Selection();
												selection.setSelect_name("size");
												selection.setSelect_value(sizeVal);
												selections.add(selection);
												lSelectionList.setSelections(selections);
												sizeIsExist = true;
												l_selection_list.add(lSelectionList);
											}
										}
									}
									
								}
								
								if(!sizeIsExist){
									int stock_status = 0;
									for(int i = 0;i < jsonArray.size(); i++){
										JSONObject skuJsonObj = jsonArray.getJSONObject(i);
										String colorKey = skuJsonObj.getString("attributeId");
										if(StringUtils.isNotBlank(colorKey)){
											if("color".equals(colorKey)){
												JSONArray valueJsonArr = skuJsonObj.getJSONArray("values");
												for(int j = 0; j < valueJsonArr.size(); j++){
													JSONObject colorJsonObj = valueJsonArr.getJSONObject(j);
													String dsValue = colorJsonObj.getString("displayValue");
													if(dsValue.equalsIgnoreCase(colorVal)){
														String selectable = colorJsonObj.getString("selectable");
														if(StringUtils.equals(selectable, "true")){
															stock_status = 1;
														}
														break;
													}
												}
												
											}
										}
									}
									
									LSelectionList lSelectionList = new LSelectionList();
									lSelectionList.setGoods_id(skuId);
									lSelectionList.setPrice_unit(unit);
									lSelectionList.setOrig_price(Float.parseFloat(origPirce));
									lSelectionList.setSale_price(Float.parseFloat(salePirce));
									lSelectionList.setStyle_id(colorVal);
									lSelectionList.setStock_status(stock_status);
									List<Selection> selections = new ArrayList<>();
									lSelectionList.setSelections(selections);
									l_selection_list.add(lSelectionList);
								}
								
								LStyleList  lStyleList = new LStyleList();
								if(StringUtils.isNotBlank(colorSKuId)){
									lStyleList.setGood_id(colorSKuId);
								}else{
									lStyleList.setGood_id(skuId);
								}
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_id(colorVal);
								lStyleList.setStyle_switch_img("");
								lStyleList.setStyle_name(colorVal);
								if(defulteColor.equals(colorVal)){
									lStyleList.setDisplay(true);
									int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
									rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
								}
								l_style_list.add(lStyleList);
								
								List<Image> images = new ArrayList<>();
								if(StringUtils.isNotBlank(defaultImg)){
									images.add(new Image(defaultImg));
								}
								images.addAll(imageList);
								
								if(StringUtils.isNotBlank(colorSKuId)){
									context.getUrl().getImages().put(colorSKuId, images);// picture
								}else{
									context.getUrl().getImages().put(skuId, images);// picture
								}
							}
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
			}
			
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements breadcrumbEs = doc.select(".pdp-main__breadcrumbs li a");
			for(Element e : breadcrumbEs){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			
			if(CollectionUtils.isEmpty(breads)){
				cats.add(title);
				breads.add(title);
			}
			
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".pdp-info__description").text();
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
	
	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "www.coach.com");
		return headers;
	}
	
	private  String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		Coach fo = new Coach();
		context.setCurrentUrl("http://www.coach.com/coach-fisherman-clog-with-rhinestone-buckle/G1307.html?dwvar_color=BLK");
	    fo.invoke(context);
	}
	
}
