package com.haitao55.spider.crawler.core.callable.custom.toryburch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.common.http.HTTPSTrustManager;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Toryburch网站收录
 * date:2017-2-8
 * @author denghuan
 *
 */
public class Toryburch extends AbstractSelect{

	private static final String domain = "www.toryburch.com";
	private static final String IMAGE_DOMAIN = "https://s7.toryburch.com/is/image/ToryBurchNA/";
	private String SKU_API = "https://www.toryburch.com/on/demandware.store/Sites-ToryBurch_US-Site/default/Product-GetVariants?pid={}&format=json";

	@Override
	public void invoke(Context context) throws Exception {
		String parentUrl = context.getUrl().getParentUrl();
		String url = context.getUrl().getValue();
		if(url.contains("|")){
			url = url.replace("|", "%7C");
		}
		String content = crawlerUrl(context,url,parentUrl);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String selectColor = doc.select("ul.swatchesdisplay li.selected a span").text();
			if(StringUtil.isBlank(selectColor)){
				selectColor = doc.select("li.swatches__item.swatches__item--lg.selected div.swatches__disp-name").text();
			}
			String unit = StringUtils.substringBetween(content, "priceCurrency\" class=\"posoffpage\">", "</span>");
			String productCache = StringUtils.substringBetween(content, "app.ProductCache = new", "pricing");
			String title = StringUtils.substringBetween(productCache, "name\": \"", "\",");
			String brand = StringUtils.substringBetween(productCache, "brand\": \"", "\",");
			String productId = StringUtils.substringBetween(productCache, "ID\": \"", "\",");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"item productId data not found..");
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String variations = StringUtils.substringBetween(content, "variations\": ", "]},");
			Map<String,String> sizeMap = new HashMap<>();
			Map<String,String> colorMap = new HashMap<>();
			if(StringUtils.isNotBlank(variations)){
				variations = variations+"]}";
				JSONObject jsonObject = JSONObject.parseObject(variations);
				JSONArray jsonArray = jsonObject.getJSONArray("attributes");
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject skuObject =  jsonArray.getJSONObject(i);
					String id = skuObject.getString("id");
					JSONArray valJsonArray = skuObject.getJSONArray("vals");
					if("color".equals(id)){
						for(int j = 0; j < valJsonArray.size(); j++){
							JSONObject valObject =  valJsonArray.getJSONObject(j);
							String vId = valObject.getString("id");
							String val = valObject.getString("val");
							colorMap.put(vId, val);
						}
						
					}else if("size".equals(id)){
						for(int j = 0; j < valJsonArray.size(); j++){
							JSONObject valObject =  valJsonArray.getJSONObject(j);
							String vId = valObject.getString("id");
							String val = valObject.getString("val");
							sizeMap.put(vId, val);
						}
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			if(StringUtils.isNotBlank(productId)){
				String  skuApi = SKU_API.replace("{}", productId);
				Url currentUrl = new Url(skuApi);
				currentUrl.setTask(context.getUrl().getTask());
				String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
				if(StringUtils.isNotBlank(skuJson)){
					JSONObject jsonObject = JSONObject.parseObject(skuJson);
					String varist = jsonObject.getString("variations");
					JSONObject variationsJsonObject = JSONObject.parseObject(varist);
					JSONArray variantsJsonArray = variationsJsonObject.getJSONArray("variants");
					Map<String,String> styleMap = new HashMap<>();
					for(int i = 0; i < variantsJsonArray.size(); i++){
						JSONObject skuObject =  variantsJsonArray.getJSONObject(i);
						String inStock = skuObject.getString("inStock");
						int stock_status = 0;
						if("true".equals(inStock)){
							stock_status = 1;
						}
						if(stock_status == 0){
							continue;
						}
						String pricing = skuObject.getString("pricing");
						JSONObject pricingJsonObject = JSONObject.parseObject(pricing);
						String origPrice = pricingJsonObject.getString("standard");
						String salePrice = pricingJsonObject.getString("sale");
						if("0.0".equals(salePrice)){
							continue;
						}
						if(StringUtils.equals(origPrice, "0.0") && !StringUtils.equals(salePrice, "0.0")){
							origPrice = salePrice;
						}
						
						LSelectionList lSelectionList = new LSelectionList();
						String skuId = skuObject.getString("id");
						
						String attributes = skuObject.getString("attributes");
						
						List<Image> imageList = new ArrayList<>();
						JSONArray imageJsonArray = skuObject.getJSONArray("s7Images");
						for(int m = 0; m < imageJsonArray.size(); m++){
							String image = imageJsonArray.getString(m);
							if(StringUtils.isNotBlank(image)){
								imageList.add(new Image(IMAGE_DOMAIN+image));
							}
						}
				
						JSONObject attrJsonObject = JSONObject.parseObject(attributes);
						String colorKey = attrJsonObject.getString("color");
						String sizeKey = attrJsonObject.getString("size");
						String colorVal = colorMap.get(colorKey);
						String sizeVal = sizeMap.get(sizeKey);
					
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id(colorVal);
						lSelectionList.setPrice_unit(unit);
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						
						if(!styleMap.containsKey(colorVal)){
							LStyleList lStyleList = new LStyleList();
							if(StringUtils.isNotBlank(selectColor) && 
									selectColor.equals(colorVal)){
								lStyleList.setDisplay(true);
								if(StringUtils.isNotBlank(origPrice) && 
										StringUtils.isNotBlank(salePrice)){
									 int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
									 rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
								}
							}
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							
							
//							String imageUrl = IMAGE_DOMAIN+"TB_"+productId+"_"+colorKey;
//							boolean flag = image_exists(imageUrl,context);
//							if(flag){
//								imageList.add(new Image(imageUrl));
//							}
//							for(String s : attr){
//								String url = IMAGE_DOMAIN+"TB_"+productId+"_"+colorKey+"_"+s;
//								boolean imageFlag = image_exists(url,context);
//								if(imageFlag){
//									imageList.add(new Image(url));
//								}
//							}
							context.getUrl().getImages().put(skuId, imageList);// picture

							l_style_list.add(lStyleList);
						}
						styleMap.put(colorVal, colorVal);
						l_selection_list.add(lSelectionList);
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
			Elements es = doc.select("#breadcrumb span ol li a span");
			if(es != null &&  es.size() > 0){
				for(int i = 1; i < es.size(); i++){
					String cate = es.get(i).text();
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
					}
				}
			}else{
				cats.add(title);
				breads.add(title);
			}
			
			if(CollectionUtils.isEmpty(breads)){
				breads.add(title);
			}
			
			if(CollectionUtils.isEmpty(cats)){
				cats.add(title);
			}

			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			
			String description = doc.select("#panel1 .panelContent").text();
			if(StringUtil.isBlank(description)){
				description = doc.select("div.v-offset-top-m.body-copy--s.body-copy.product-description__content").text();
			}
			
			Elements featureEs = doc.select("#panel3 .panelContent ul li");
			int count = 0;
			if(featureEs != null &&  featureEs.size() > 0){
				for(Element e : featureEs){
					String text = e.text();
					if(StringUtils.isNotBlank(text)){
						 count ++;
						 featureMap.put("feature-"+count, text);
					}
				}
			}else{
				Elements feature = doc.select("#panel3 .panelContent p");
				if(feature != null &&  feature.size() > 0){
					for(Element e : feature){
						String text = e.text();
						if(StringUtils.isNotBlank(text)){
							 count ++;
							 featureMap.put("feature-"+count, text);
						}
					}
				}else{
					Elements feature2 = doc.select("div#longDescription ul li");
					if(feature2 != null &&  feature2.size() > 0){
						for(Element e : feature2){
							String text = e.text();
							if(StringUtils.isNotBlank(text)){
								 count ++;
								 featureMap.put("feature-"+count, text);
							}
						}
					}else{
						//we need check if supplier site change
					}					
				}				
			}
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
	
	private String crawlerUrl(Context context,String url,String parentUrl) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(url.startsWith("https")){
			HTTPSTrustManager.allowAllSSL();
		}
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(parentUrl)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(parentUrl)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(String parentUrl){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.75 Chrome/62.0.3202.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("upgrade-insecure-requests", "1");
		headers.put("referer", parentUrl);
		headers.put(":authority", "www.toryburch.com");
		headers.put(":method", "GET");
		return headers;
	}
	
}
