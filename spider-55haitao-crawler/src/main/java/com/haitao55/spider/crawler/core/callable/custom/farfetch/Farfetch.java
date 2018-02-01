package com.haitao55.spider.crawler.core.callable.custom.farfetch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;


/**
 * Farfetch 网站收录
 * date : 2017-3-1
 * @author denghuan
 *
 */
public class Farfetch extends AbstractSelect{

	private static final String domain = "www.farfetch.com";
	private  String FARFETCH_API= "https://www.farfetch.com/uk/product/GetDetailState?";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "ProductId\\\":", ",");
			String storeId = StringUtils.substringBetween(content, "storeId\":", ",");
			String designerId = StringUtils.substringBetween(content, "manufacturerId\":", ",");
			String categoryId = StringUtils.substringBetween(content, "categoryId\":", ",");
			String hasStock = StringUtils.substringBetween(content, "hasStock\":", ",");
			String salePrice = StringUtils.substringBetween(content, "unitSalePrice\":", ",");
			String origPrice = StringUtils.substringBetween(content, "unitPrice\":", ",");
			String unit = StringUtils.substringBetween(content, "CurrencyCode\":\"", "\"");
			if(!StringUtils.equals(unit, "GBP")){
	            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" Farfetch-price-unit-is-not-GBP...");
	        }
			
			String brand = doc.select("h1.detail-brand a").text();
			String title = doc.select("h1.detail-brand span.heading-regular").text();
			
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
			
			List<Image> imageList = new ArrayList<>();
			Elements es = doc.select(".js-sliderProduct li.sliderProduct-slide a.sliderProduct-link img");
			for(Element e : es){
				String image = e.attr("data-zoom-image");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			String gender = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId) && StringUtils.isNotBlank(storeId) 
					&& StringUtils.isNotBlank(designerId) && StringUtils.isNotBlank(categoryId)){
				String api_url = FARFETCH_API+"productId="+productId+"&storeId="+storeId+"&sizeId=&categoryId="+categoryId+"&designerId="+designerId+"";
				String html = StringUtils.EMPTY;
				String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
				if(StringUtils.isBlank(proxyRegionId)){
					html = Crawler.create().timeOut(30000).url(api_url).method(HttpMethod.GET.getValue())
							.resultAsString();
				}else{
					Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
					String proxyAddress=proxy.getIp();
					int proxyPort=proxy.getPort();
					html = Crawler.create().timeOut(30000).url(api_url).method(HttpMethod.GET.getValue())
							.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
				}
				
				if(StringUtils.isNotBlank(html)){
					JSONObject jsonObject = JSONObject.parseObject(html);
					String sizeInfo = jsonObject.getString("SizesInformationViewModel");
					String breadCrumb = jsonObject.getString("ProductBreadCrumb");
					if(StringUtils.containsIgnoreCase(breadCrumb, "women")){
						gender = "women";
					}else if(StringUtils.containsIgnoreCase(breadCrumb, "men")){
						gender = "men";
					}
					
					JSONObject sizeJsonObject = JSONObject.parseObject(sizeInfo);
					JSONArray jsonArray = sizeJsonObject.getJSONArray("AvailableSizes");
					for(int i = 0; i < jsonArray.size(); i++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject skuJsonObject = jsonArray.getJSONObject(i);
						String desc = skuJsonObject.getString("Description");
						String scaleDesc = skuJsonObject.getString("ScaleDescription");
						String instock = skuJsonObject.getString("LastInStock");
						String skuId = skuJsonObject.getString("SizeId");
						
						String priceInfo = skuJsonObject.getString("PriceInfo");
						JSONObject priceJsonObject = JSONObject.parseObject(priceInfo);
						String skuSalePrice = priceJsonObject.getString("FormatedPrice");
						String skuOrigPrice = priceJsonObject.getString("FormatedPriceWithoutPromotion");
						if(StringUtils.isBlank(skuOrigPrice)){
							skuOrigPrice = skuSalePrice;
						}
						if(StringUtils.isNotBlank(skuSalePrice)){
							skuSalePrice = skuSalePrice.replaceAll("[£,]", "");
							skuOrigPrice = skuOrigPrice.replaceAll("[£,]", "");
						}
						int stock_status = 0;
						if(StringUtils.isNotBlank(instock) && 
								StringUtils.containsIgnoreCase(instock, "true")){
							stock_status = 1;
						}
						if(StringUtils.isNotBlank(hasStock) && "true".equals(hasStock) &&  
								StringUtils.containsIgnoreCase(desc, "One size")){
							stock_status = 1;
						}
						
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id("default");
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(desc) || StringUtils.isNotBlank(scaleDesc)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(desc+scaleDesc);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						
						l_selection_list.add(lSelectionList);
						
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
							if(StringUtils.isNotBlank(skuOrigPrice) && StringUtils.isNotBlank(skuSalePrice)){
								int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
								rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), 
										save, Float.parseFloat(skuSalePrice), unit));
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
							l_style_list.add(lStyleList);
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
			}else{
				context.getUrl().getImages().put(productId, imageList);// picture
				if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,
							"farfetch.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
				}
				if(StringUtils.isBlank(origPrice)){
					origPrice = salePrice;
				}
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), 
						save, Float.parseFloat(salePrice), unit));
				
				if(StringUtils.isNotBlank(hasStock) && 
						"true".equals(hasStock)){
					spuStock = 1;
				}
				
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String category = StringUtils.substringBetween(content, "category\":\"", "\"");
			String subcategory = StringUtils.substringBetween(content, "subcategory\":\"", "\"");
			if(StringUtils.isNotBlank(category)){
				cats.add(category);
				breads.add(category);
			}
			if(StringUtils.isNotBlank(subcategory)){
				cats.add(subcategory);
				breads.add(subcategory);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".product-detail .pl10").text();
			
			String featureEs = doc.select(".product-detail .accordion-item .accordion-content dd").text();
			int count = 0;
			if(StringUtils.isNotBlank(featureEs)){
				String[] sp = featureEs.split(",");
				for(int i = 0; i < sp.length; i++){
					 count ++;
					 featureMap.put("feature-"+count, sp[i]);
				}
						
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
}
