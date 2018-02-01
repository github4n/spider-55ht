package com.haitao55.spider.crawler.core.callable.custom.glowing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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


/**
 * date : 2017-3-6
 * Bglowing网站收录
 * @author denghuan
 *
 */
public class Bglowing extends AbstractSelect{

	
	private static final String domain = "www.b-glowing.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String origPrice = doc.select("p.old-price span.price").text();
			String brand = doc.select("div.brand-name span").text();
			String title = doc.select("div.product-name h1").text();
			String salePrice = StringUtils.substringBetween(content, "price\" content=\"", "\"");
			//String salePrice = doc.select(".regular-price span.price").text();
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			String product = StringUtils.substringBetween(content, "Product.Config(", ");");
			String imageJson = StringUtils.substringBetween(content, "jQuery.parseJSON('", "'))");
			String sizeVal = doc.select("span.prodsize").text();
			
			String	docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Map<String,String> imageMap = new HashMap<>();
			if(StringUtils.isNotBlank(imageJson)){
				String rsImage = imageJson.replaceAll("[\\\\]", "");
				JSONObject	imageJsonObject = JSONObject.parseObject(rsImage);
				String base_image = imageJsonObject.getString("base_image");
				JSONObject	baseImageJsonObject = JSONObject.parseObject(base_image);
				Set<String> baseSet = baseImageJsonObject.keySet();
				Iterator<String> it = baseSet.iterator();
				while(it.hasNext()){
					String key = it.next();
					String value = baseImageJsonObject.getString(key);
					if(StringUtils.isNotBlank(value)){
						imageMap.put(key, value);
					}
				}
			}
			
			Map<String,String> productMap = new HashMap<>();
			Map<String,String> stockMap = new HashMap<>();
			if(StringUtils.isNotBlank(product)){
				JSONObject	jsonObject = JSONObject.parseObject(product);
				String stock_status = jsonObject.getString("stock_status");
				JSONObject	stockJsonObject = JSONObject.parseObject(stock_status);
				Set<String> stockSet = stockJsonObject.keySet();
				Iterator<String> stockIt = stockSet.iterator();
				while(stockIt.hasNext()){
					String key = stockIt.next();
					String value = stockJsonObject.getString(key);
					if(StringUtils.isNotBlank(value)){
						stockMap.put(key, value);
					}
				}
				String attributes = jsonObject.getString("attributes");
				JSONObject	attrJsonObject = JSONObject.parseObject(attributes);
				Set<String> set = attrJsonObject.keySet();
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					String key = it.next();
					String optionJsonObject = attrJsonObject.getString(key);
					JSONObject	keyJsonObject = JSONObject.parseObject(optionJsonObject);
					JSONArray jsonArray = keyJsonObject.getJSONArray("options");
					for(int i = 0; i < jsonArray.size(); i++){
						JSONObject  skuJsonObject = jsonArray.getJSONObject(i);
						String id = skuJsonObject.getString("id");
						//String label = skuJsonObject.getString("label");
						JSONArray productsJsonArray =  skuJsonObject.getJSONArray("products");
						for(int j = 0; j < productsJsonArray.size(); j++){
							String productId = productsJsonArray.getString(j);
							productMap.put(id, productId);
						}
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			
			Elements  es = doc.select("ul.configurable-swatch-list li");
			if(es != null && es.size() > 0){
				for(int i = 0;i < es.size(); i++){
					LSelectionList lSelectionList = new LSelectionList();
					LStyleList lStyleList = new LStyleList();
					String skuId = es.get(i).attr("id");
					skuId = pattern(skuId);
					String colorVal = es.get(i).select("a span.swatch-customlabel").text();
					String swatchImg = es.get(i).select("a span.swatch-label img").attr("src");
					String skuSalePrice = StringUtils.EMPTY;
					String color = StringUtils.EMPTY;
					String orig_price = StringUtils.EMPTY;
					if(StringUtils.isNotBlank(colorVal)){
						if(StringUtils.containsIgnoreCase(colorVal, "-")){
							String[] sp = colorVal.split(" -");
							//color = sp[0];
							color = es.get(i).select("a span.swatch-label img").attr("alt");
							if(StringUtils.isBlank(color)){
								color = sp[0];
							}
							skuSalePrice = sp[1];
							skuSalePrice = pattern(skuSalePrice);
							if(StringUtils.isBlank(skuSalePrice)){
								skuSalePrice = StringUtils.substringAfter(colorVal, "$");
							}
						}
					}
					if(StringUtils.isNotBlank(color)){
						lSelectionList.setStyle_id(color);
					}
					if(StringUtils.isNotBlank(skuSalePrice)){
						skuSalePrice = skuSalePrice.replaceAll("[$, ]", "");
						orig_price = skuSalePrice;
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					}
					String proId = productMap.get(skuId);
					int stock_status = 0;
					if(StringUtils.isNotBlank(proId)){
						String instock = stockMap.get(proId);
						if("1".equals(instock)){
							stock_status = 1;
						}
					}
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setOrig_price(Float.parseFloat(orig_price));
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setGoods_id(skuId);
					List<Selection> selections = new ArrayList<>();
					if(StringUtils.isNotBlank(sizeVal)){
						sizeVal = sizeVal.replace("SIZE ", "");
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
					}
					lSelectionList.setSelections(selections);
					
					if(display){
						lStyleList.setDisplay(true);
						display = false;
						rebody.setPrice(new Price(Float.parseFloat(orig_price), 0, Float.parseFloat(skuSalePrice), unit));
					}
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_switch_img(swatchImg);
					lStyleList.setStyle_id(color);
					lStyleList.setStyle_name(color);
					lStyleList.setStyle_cate_name("color");
					List<Image> imageList = new ArrayList<>();
					String image = imageMap.get(proId);
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
					context.getUrl().getImages().put(skuId, imageList);// picture
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
				String instock = StringUtils.substringBetween(content, "Availability: <span>", "</span>");
				if(StringUtils.isNotBlank(instock) && 
						StringUtils.containsIgnoreCase(instock, "In stock")){
					spuStock = 1;
				}
				if(StringUtils.isNotBlank(salePrice) && 
						StringUtils.isNotBlank(origPrice)){
					salePrice = salePrice.replaceAll("[$,]", "");
					origPrice = origPrice.replaceAll("[$,]", "");
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
				}else if(StringUtils.isNotBlank(salePrice) && 
						StringUtils.isBlank(origPrice)){
					rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
				}
				String image = doc.select(".product-img-box img.etalage_thumb_image").attr("src");
				if(StringUtils.isNotBlank(image)){
					List<Image> imageList = new ArrayList<>();
					imageList.add(new Image(image));
					context.getUrl().getImages().put("productId", imageList);// picture
				}
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cateEs = doc.select(".breadcrumbs ol li a span");
			for(int i = 1; i < cateEs.size()-1; i++){
				String cate = cateEs.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			if(CollectionUtils.isEmpty(cats)){
				String cate = StringUtils.substringBetween(content, "category\" content=\"", "\"");
				cats.add(cate.trim());
				breads.add(cate.trim());
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
			}
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".resp-tabs-container .std span").text();
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
	
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		context.setCurrentUrl("https://www.b-glowing.com/sale/by-terry-cellularose-brightening-cc-lumi-serum/");
		Bglowing  bg = new Bglowing();
		bg.invoke(context);
	}
}
