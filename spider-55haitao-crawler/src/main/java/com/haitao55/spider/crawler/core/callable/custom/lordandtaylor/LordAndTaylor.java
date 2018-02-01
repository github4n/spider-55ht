package com.haitao55.spider.crawler.core.callable.custom.lordandtaylor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class LordAndTaylor extends AbstractSelect {
	/* 用于拼接商品image url **/
	public static final String image_temp = "https://s7d9.scene7.com/is/image/LordandTaylor/";
	private static final String domain = "www.lordandtaylor.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	@Override
	public void invoke(Context context) throws Exception {
		String content = "";
		String currentUrl = context.getUrl().getValue();
		String url = context.getUrl().getValue();
		if(url.contains("<>")){
			url = url.replace("<>", "%3C%3E");
		}
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(url, getHeaders());
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			if (StringUtils.isBlank(proxyRegionId)) {
				content = Crawler.create().timeOut(6000).url(url).header(getHeaders())
						.method(HttpMethod.GET.getValue()).resultAsString();
			} else {
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress = proxy.getIp();
				int proxyPort = proxy.getPort();
				content = Crawler.create().timeOut(6000).url(url).header(getHeaders())
						.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort)
						.resultAsString();
			}
		}		
		context.put("${html}", content);
		RetBody retBody = new RetBody();
		Sku sku=new Sku();		
		boolean spu_price_flag=false;		
		int spu_stock_status = 0;
		
		String productsJson = StringUtils.substringBetween(content, "<script type=\"application/json\">", "</script></div>").trim();
		String brand_name = null;
		JSONObject products = null;
		String title = null;
		String ProductId = null;
		if(StringUtils.isNotBlank(productsJson)){
			products = JSONObject.parseObject(productsJson).getJSONObject("ProductDetails").getJSONArray("main_products").getJSONObject(0);			
			// DOCID & Site & ProdUrl
			 ProductId = products.getString("product_id");
			if (StringUtils.isNotBlank(ProductId)) {
				String docid = SpiderStringUtil.md5Encode(domain.concat(ProductId));
				String url_no = SpiderStringUtil.md5Encode(currentUrl);
				retBody.setDOCID(docid);
				retBody.setSite(new Site(domain));
				retBody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			}
			//Brand
			brand_name = products.getJSONObject("brand_name").getString("label");
			retBody.setBrand(new Brand(brand_name, "", "", ""));
			//Title
			title = products.getJSONObject("clarity_event_tags").getJSONObject("product_name_event").getString("value");
			if(StringUtils.isBlank(title)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"lordandtaylor.com itemUrl:" + context.getUrl().toString() + " not found..");
			}
			retBody.setTitle(new Title(title, "", "", ""));
			//FeatureList & Description
			String description = products.getString("description");
			Document descriptionToDoc = JsoupUtils.parse(description);
			desc_package(descriptionToDoc,retBody);
			
		}
		// BreadCrumb & Category
		List<String> breads = new ArrayList<String>();
		String categoriesJson = StringUtils.substringBetween(content, "var defaultPageData = ", "defaultPageData.visitor =").trim();
		if(StringUtils.isNotBlank(categoriesJson)){
			categoriesJson = categoriesJson.substring(0,categoriesJson.length() - 1);
			JSONObject jsonObject = JSONObject.parseObject(categoriesJson).getJSONObject("page");
			JSONArray categoriesArray = jsonObject.getJSONArray("categories");	
			if(categoriesArray != null && categoriesArray.size() > 0){
				for(int i = 0; i < categoriesArray.size(); i++){
					String category = categoriesArray.getJSONObject(i).toString();
					if (StringUtils.containsIgnoreCase(category, "home")) {
						continue;
					}
					breads.add(category);
				}
			}
		}	
		
		if(CollectionUtils.isEmpty(breads)){
			breads.add(title);
		}
		retBody.setCategory(breads);
		breads.add(brand_name);
		retBody.setBreadCrumb(breads);
		
		JSONArray colorArray = products.getJSONObject("colors").getJSONArray("colors");
		Map<String, String> colorData = null;
		if(colorArray != null && colorArray.size()>0){
			colorData = new HashMap<String, String>();			
			for(int i = 0; i < colorArray.size(); i++){
				JSONObject colorJsonObj = colorArray.getJSONObject(i);
				String colorId = colorJsonObj.getString("id");
				String colorLabel = colorJsonObj.getString("label");
				String colorize_image_url = colorJsonObj.getString("colorize_image_url");
				String is_soldout = colorJsonObj.getString("is_soldout");
				colorData.put(colorId, colorLabel+"@@"+colorize_image_url+"@@"+is_soldout);
			}			
		}
		
		JSONArray sizesArray = products.getJSONObject("sizes").getJSONArray("sizes");
		Map<String, String> sizesData = null;
		if(sizesArray != null && sizesArray.size()>0){
			sizesData = new HashMap<String, String>();			
			for(int i = 0; i < sizesArray.size(); i++){
				JSONObject sizeJsonObj = sizesArray.getJSONObject(i);
				String sizeId = sizeJsonObj.getString("id");
				String sizeValue = sizeJsonObj.getString("value");
				String is_soldout = sizeJsonObj.getString("is_soldout");
				sizesData.put(sizeId, sizeValue+"@@"+is_soldout);
			}			
		}
		
		JSONObject skusJsonObject = products.getJSONObject("skus");
		JSONArray skuArray = null;
		if(!skusJsonObject.isEmpty()){			
			String skuStatus = skusJsonObject.getString("enabled");
			if("true".equals(skuStatus)){
				skuArray = skusJsonObject.getJSONArray("skus");				
			}			
		}		
		
		List<String> image_suffix_list = new ArrayList<String>();
		JSONObject imageJsonObject = products.getJSONObject("media");
		JSONArray imageArray = null;
		if(!imageJsonObject.isEmpty()){			
			imageArray = imageJsonObject.getJSONArray("images");	
			if(imageArray.size() > 1){
				for(int i = 0; i < imageArray.size(); i++){
					String image_suffix = imageArray.getString(i);
					if(image_suffix.contains("_")){
						image_suffix_list.add(image_suffix);
					}			
				}	
			}else{
				String image_suffix = imageArray.getString(0);
				image_suffix_list.add(image_suffix);
			}
			
		}				
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		//　iteartor skuarray
		if(null!=skuArray && skuArray.size()>0){
			if(skuArray.size() > 1){
				for (Object object : skuArray) {
					JSONObject skuJsonObejct=(JSONObject)object;
					
					// selectlist
					LSelectionList lselectlist = new LSelectionList();
					//skuId
					String skuId = skuJsonObejct.getString("sku_id");
					//stock
					int stock_status = 0;
					int stock_number=0;
					String is_size_soldout = sizesData.get(skuJsonObejct.getString("size_id")).split("@@")[1];
					String is_color_soldout = colorData.get(skuJsonObejct.getString("color_id")).split("@@")[2];
					String sku_soldout = skuJsonObejct.getString("status_alias");
					if("true".equals(is_size_soldout) || "true".equals(is_color_soldout) || "soldout".equals(sku_soldout)){
						continue;
					}else{
						stock_status=1;
					}
									
					//spu stock status
					if(stock_status>0){
						spu_stock_status=1;
					}
					
					String color=colorData.get(skuJsonObejct.getString("color_id")).split("@@")[0];
					String size=sizesData.get(skuJsonObejct.getString("size_id")).split("@@")[0];
					
					//price
					/*String salePrice = skuJsonObejct.getJSONObject("price").getJSONObject("sale_price").getString("local_currency_value");
					String origPrice = skuJsonObejct.getJSONObject("price").getJSONObject("list_price").getString("local_currency_value");
					String unit = skuJsonObejct.getJSONObject("price").getJSONObject("sale_price").getString("local_currency_code");*/
					
					String salePrice = skuJsonObejct.getJSONObject("price").getJSONObject("sale_price").getString("usd_currency_value");
					String origPrice = skuJsonObejct.getJSONObject("price").getJSONObject("list_price").getString("usd_currency_value");
					String unit = Currency.USD.name();
					
					String save=StringUtils.EMPTY;

					if (StringUtils.isBlank(replace(origPrice))) {
						origPrice = salePrice;
					}
					if (StringUtils.isBlank(replace(salePrice))) {
						salePrice = origPrice;
					}
					if (StringUtils.isBlank(origPrice)
							|| Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
						origPrice = salePrice;
					}
					if (StringUtils.isBlank(save)) {
						save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)
								+ "";// discount
					}
					
					//spu price
					if(!spu_price_flag){
							retBody.setPrice(new Price(Float.valueOf(origPrice), Integer.parseInt(save), Float.valueOf(salePrice), unit));	
						spu_price_flag=true;
					}
					

					//selections
					List<Selection> selections = new ArrayList<Selection>();
					if(StringUtils.isNotBlank(size)){
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(size);
						selections.add(selection);
					}
					
					//lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(Float.valueOf(origPrice));
					lselectlist.setSale_price(Float.valueOf(salePrice));
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(color);
					lselectlist.setSelections(selections);
					
					//l_selection_list
					l_selection_list.add(lselectlist);
					
					//style json
					styleJsonObject.put(color, skuJsonObejct);
				}
				boolean StyleList = false;
				// lord and taylor 验证图片是否存在逻辑　　放在线程中进行判断
		//	    JSONObject image_result_json = new LordAndTaylorHandler().process(styleJsonObject,image_suffix_list,context.getUrl());
				if(null != styleJsonObject  && styleJsonObject.size()>0){
					for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
						String style_id = entry.getKey();
						JSONObject jsonObject = (JSONObject)entry.getValue();
						// stylelist
						LStyleList lStyleList = new LStyleList();
						//skuId
						String skuId = jsonObject.getString("sku_id");
						String color_id = jsonObject.getString("color_id");
						String switch_img=jsonObject.getString("ItemSwatchImage2");
						if(switch_img != null && !StringUtils.contains(switch_img, "http")){
							switch_img = "http:".concat(switch_img);
						}
						//images
						List<Image> image_list = new ArrayList<Image>();
						if(!StyleList){
							lStyleList.setDisplay(true);
							for(String image_suffix : image_suffix_list){
								image_list.add(new Image(image_temp+image_suffix));	
							}
							StyleList=true;
					    }
						image_list.add(new Image(image_temp+colorData.get(color_id).split("@@")[1]));														
						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("COLOR");
						lStyleList.setStyle_name(style_id);
																				
						context.getUrl().getImages().put(skuId, image_list);
						
						//l_style_list
						l_style_list.add(lStyleList);
					}
				}
			}else{
					JSONObject skuJsonObejct=(JSONObject)skuArray.get(0);					
					spu_stock_status=1;																			
					String salePrice = skuJsonObejct.getJSONObject("price").getJSONObject("sale_price").getString("usd_currency_value");
					String origPrice = skuJsonObejct.getJSONObject("price").getJSONObject("list_price").getString("usd_currency_value");
					String unit = Currency.USD.name();
					
					String save=StringUtils.EMPTY;

					if (StringUtils.isBlank(replace(origPrice))) {
						origPrice = salePrice;
					}
					if (StringUtils.isBlank(replace(salePrice))) {
						salePrice = origPrice;
					}
					if (StringUtils.isBlank(origPrice)
							|| Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
						origPrice = salePrice;
					}
					if (StringUtils.isBlank(save)) {
						save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)
								+ "";// discount
					}
					List<Image> image_list = new ArrayList<Image>();
					for(String image_suffix : image_suffix_list){
						image_list.add(new Image(image_temp+image_suffix));	
					}
					context.getUrl().getImages().put(ProductId, image_list);
					//spu price
					retBody.setPrice(new Price(Float.valueOf(origPrice), Integer.parseInt(save), Float.valueOf(salePrice), unit));																	
			}

			
			//sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			retBody.setSku(sku);
			
			//stock
			retBody.setStock(new Stock(spu_stock_status));																		
			
			properties_package(retBody);
			
			setOutput(context, retBody);
		}
	}
	
	/**
	 * properties 封装
	 * @param retBody
	 */
	private void properties_package(RetBody retBody) {
		String gender = StringUtils.EMPTY;
		Map<String, Object> propMap = new HashMap<String, Object>();
		String title = StringUtils.EMPTY;
		if(!retBody.getTitle().getEn().isEmpty()){
			title = retBody.getTitle().getEn();
		}
		String Category = StringUtils.EMPTY;
		if(retBody.getCategory()!= null && retBody.getCategory().size() > 0){
			Category = retBody.getCategory().toString();
		}
		if(!StringUtil.isBlank(title)){
			gender = getSex(title);
		}		
		if(StringUtils.isBlank(gender)){
			if(!StringUtil.isBlank(Category)){
				gender = getSex(Category);
			}		
		}
		if(StringUtils.isBlank(gender)){
			propMap.put("s_gender", gender);
			retBody.setProperties(propMap);
		}		
	}

	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		Elements es = doc.select("li");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String text = e.text();
				if(StringUtils.isNotBlank(text)){
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}else{
			String text = doc.select("body").text();
			featureMap.put("feature-" + 1, text);
			sb.append(text);
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	
	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

	}
	
	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} 
		return gender;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put("Cookie", "session_id=15089171769607wGkT!X-na03e3H08wQoqoxiTC8rOpbpWGwcVyZXRrf2TG4USObwM5qf");
		return headers;
	}
	
	public static void main(String[] args) throws Exception{
		
		String url = "https://www.lordandtaylor.com/main/ProductDetail.jsp?PRODUCT%3C%3Eprd_id=845524442374316";
		Context context = new Context();
		LordAndTaylor pm = new LordAndTaylor();
		context.setCurrentUrl(url);
		pm.invoke(context);		
		String content = Crawler.create().timeOut(6000).url("https://www.lordandtaylor.com/main/ProductDetail.jsp?PRODUCT%3C%3Eprd_id=845524442374316").header(getHeaders())
					.method(HttpMethod.GET.getValue())
					.resultAsString();
		
//		Document doc = JsoupUtils.parse(content);
/*		String categoriesJson = StringUtils.substringBetween(content, "var defaultPageData = ", "defaultPageData.visitor =").trim();
		if(StringUtils.isNotBlank(categoriesJson)){
			categoriesJson = categoriesJson.substring(0,categoriesJson.length() - 1);
			JSONObject jsonObject = JSONObject.parseObject(categoriesJson).getJSONObject("page");
			JSONArray categoriesArray = jsonObject.getJSONArray("categories");	
			for(int i = 0; i < categoriesArray.size(); i++){
				String category = categoriesArray.getJSONObject(i).toString();
				System.out.println(category);		
			}
		}*/
		String ProductsJson = StringUtils.substringBetween(content, "<script type=\"application/json\">", "</script></div>").trim();
		if(StringUtils.isNotBlank(ProductsJson)){
			JSONObject products = JSONObject.parseObject(ProductsJson).getJSONObject("ProductDetails").getJSONArray("main_products").getJSONObject(0);
			String brand_name = products.getJSONObject("brand_name").getString("label");

		//	String title = jsonObject.getJSONObject("clarity_event_tags").getJSONObject("product_name_event").getString("value");
		//	JSONArray colorArray = jsonObject.getJSONObject("colors").getJSONArray("colors");	
			System.out.println(brand_name);	
		}
		}
}
