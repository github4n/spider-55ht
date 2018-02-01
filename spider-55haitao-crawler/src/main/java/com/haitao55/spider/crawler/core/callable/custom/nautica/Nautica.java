package com.haitao55.spider.crawler.core.callable.custom.nautica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
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

/**
 * nautica 详情收录 Title: Description: Company: 55海淘
 * 
 * @author denghuan
 * @date 2017年4月17日 上午11:34:17
 * @version 1.0
 */
public class Nautica extends AbstractSelect {
	private  String request_nautica_url = "http://www.nautica.com/on/demandware.store/Sites-nau-Site/default/Product-GetVariants?pid={}&format=json";
	private static final String DOMAIN = "www.nautica.com";
	private static final String BRAND = "nautica";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "ID: \"", "\",");
			String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
			String title = doc.select("h2.product-name").text();
//			String defaultColorId = StringUtils.substringBetween(context.getCurrentUrl(), "_color=", "&");
//			if (StringUtils.isBlank(defaultColorId)) {
//				defaultColorId = StringUtils.substringAfter(context.getCurrentUrl(), "_color=");
//			}
			String defaultColorId = StringUtils.EMPTY;
			Elements defaultColorElements = doc.select("ul.Color li.selected a");
			if(CollectionUtils.isNotEmpty(defaultColorElements)){
				defaultColorId = defaultColorElements.text();
			}
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(DOMAIN));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(BRAND, ""));
			List<Image> spuImageList = null;
			String imageId = StringUtils.substringBetween(content, "NauticaBrand/swatch/", "_A_swatch");
			if(StringUtils.isNotBlank(imageId)){
				spuImageList = getImgList(imageId,context);
			}
			
			
			Map<String,List<Image>> imgMap = new HashMap<>();
			String product = StringUtils.substringBetween(content, "vals: [", "vals:");
			if(StringUtils.isNotBlank(product)){
				String[] attrs = StringUtils.substringsBetween(product, "val:", ".jpg");
				for(String attr : attrs){
					String key = StringUtils.substringBetween(attr, "\"", "\"");
					String value = StringUtils.substringBetween(attr, "swatch/", "_A");
					if(StringUtils.isNotBlank(key) && 
							StringUtils.isNotBlank(value)){
						List<Image> images = getImgList(value,context);
						if(CollectionUtils.isNotEmpty(images)){
							imgMap.put(key, images);
						}
					}
				}
			}
			
			 Sku sku = new Sku();
			 List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			 List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			 Map<String,String> styleMap = new HashMap<String,String>();
			 
			 if(StringUtils.isNotBlank(productId)){
				 String reqUrl = request_nautica_url.replace("{}", productId);
				 String rs = getContent(context,reqUrl);
				 if(StringUtils.isNotBlank(rs)){
					 JSONObject jsonObject = JSONObject.parseObject(rs);
					 String variations = jsonObject.getString("variations");
					 JSONObject variationJsonObject = JSONObject.parseObject(variations);
					 JSONArray jsonArray = variationJsonObject.getJSONArray("variants");
					 for(int i = 0; i < jsonArray.size(); i++){
						 LSelectionList  lSelectionList = new LSelectionList();
						 JSONObject skuJsonObj = jsonArray.getJSONObject(i);
						 String skuId = skuJsonObj.getString("id");
						 String attributes = skuJsonObj.getString("attributes");
						 JSONObject attributesJson= JSONObject.parseObject(attributes);
						 String colorVal = attributesJson.getString("color");
						 String sizeVal = attributesJson.getString("size");
						 String inStock = skuJsonObj.getString("inStock");
						 String pricing = skuJsonObj.getString("pricing");
						 JSONObject pricingJson= JSONObject.parseObject(pricing);
						 Float origPrice = pricingJson.getFloat("standard");
						 Float salePrice = pricingJson.getFloat("sale");
						 if(origPrice == null || origPrice == 0.0){
							 origPrice = salePrice;
						 }
						 int stock_status = 0;
						 if(!StringUtils.equals(inStock, "false")){
							 stock_status = 1;
						 }
						 
						 lSelectionList.setGoods_id(skuId);
						 lSelectionList.setOrig_price(origPrice);
						 lSelectionList.setPrice_unit(unit);
						 lSelectionList.setSale_price(salePrice);
						 lSelectionList.setStock_status(stock_status);
						 lSelectionList.setStock_status(stock_status);
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
							 lStyleList.setGood_id(skuId);
							 lStyleList.setStyle_cate_id(0);
							 lStyleList.setStyle_cate_name("color");
							 lStyleList.setStyle_id(colorVal);
							 lStyleList.setStyle_name(colorVal);
							 lStyleList.setStyle_switch_img("");
							 
							 List<Image> skuImages = imgMap.get(colorVal);
							 if(CollectionUtils.isNotEmpty(skuImages)){
								 context.getUrl().getImages().put(skuId, skuImages);// picture
							 }else{
								 context.getUrl().getImages().put(skuId, spuImageList);// picture
							 }
							 
							 if(StringUtils.isNotBlank(defaultColorId) && 
									 StringUtils.equals(defaultColorId, colorVal)){
								 lStyleList.setDisplay(true);
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
			 Elements es = doc.select(".breadcrumb a");
			 for(int i = 0;i < es.size(); i++){
				 String cate = es.get(i).text();
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
			 Elements es1 = doc.select(
					 "div.tab-content.description , div.tab-content.description div.bulletDescription ul.bullets li");
			 StringBuilder sb = new StringBuilder();
			 if (es1 != null && es1.size() > 0) {
				 int count = 1;
				 for (Element e : es1) {
					 String text = e.text();
					 if (StringUtils.isNotBlank(text)) {
						 featureMap.put("feature-" + count, text);
						 count++;
						 sb.append(text);
					 }
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", sb.toString());
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	
	private List<Image> getImgList(String value,Context context){
		List<Image> imageList = new ArrayList<>();
		String[] attrs = {"_A","_B","_C"};
		String img_suffix = "http://s7d9.scene7.com/is/image/NauticaBrand/large/";
		String img_prffix = ".jpg";
		for(String attr : attrs){
			String imageUrl = img_suffix+value+attr+img_prffix;
			boolean flag = image_exists(imageUrl,context);
			if(flag){
				imageList.add(new Image(imageUrl));
			}
		}
		return imageList;
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
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		return content;
	}
	
	public static void main(String[] args) {
		System.out.println(SpiderStringUtil.md5Encode("https://www.katespade.com/products/haute-stuff-takeout-bag/PXRU7811.html"));
		
		String product = "{val:\"Bright White\",  images swatch: {url: 'http://s7d9.scene7.com/is/image/NauticaBrand/swatch/B71001_101_A_swatch.jpg', {val:\"Bright White\",  images swatch: {url: 'http://s7d9.scene7.com/is/image/NauticaBrand/swatch/B71001_101_A_swatch.jpg',alt";
		String[] str = StringUtils.substringsBetween(product, "val:\"", "',");
		for(String s :str){
			System.out.println(s);
		}
//		Pattern pattern = Pattern.compile("val:\"(.*)\",.*url: '(.*)'");
//		Matcher matcher = pattern.matcher(product);
//		while(matcher.find()){
//			System.out.println(matcher.group(1));
//			System.out.println(matcher.group(2));
//		}
	}
	
}
