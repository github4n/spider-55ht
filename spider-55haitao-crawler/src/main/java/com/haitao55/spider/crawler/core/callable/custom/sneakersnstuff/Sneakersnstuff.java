package com.haitao55.spider.crawler.core.callable.custom.sneakersnstuff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * sneakersnstuff 详情封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年3月7日 下午2:03:47
* @version 1.0
 */
public class Sneakersnstuff extends AbstractSelect{
//	private static final String INSTOCK = "InStock";
	private static final String SKU_OUTSTOCK = "unavailable";
	private static final String DOMAIN = "www.sneakersnstuff.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
//		String content = this.getInputString(context);
		Document doc = JsoupUtils.parse(crawler_package(context));
		
		Pattern p = Pattern.compile("The raffle is now closed and all winners have been notified");
		Matcher m = p.matcher(doc.toString());
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"sneakersnstuff.com itemUrl:"+context.getUrl().toString()+" not found..");
		}
		
		Elements productElements = doc.select("div.nosto_product");
		if(CollectionUtils.isNotEmpty(productElements)){
			RetBody retBody = new RetBody();
			Sku sku = new Sku();
			
			//spu stock_status
			int spu_stock_status = 0; 
			
			//product id
			String productId = StringUtils.EMPTY;
			Elements productIdElements = productElements.select("span.product_id");
			if(CollectionUtils.isNotEmpty(productIdElements)){
				productId = productIdElements.text();
			}
			//title
			String title = StringUtils.EMPTY;
			Elements titleElements = productElements.select("span.name");
			if(CollectionUtils.isNotEmpty(titleElements)){
				title = titleElements.text();
			}
			//brand
			String brand = StringUtils.EMPTY;
			Elements brandElements = productElements.select("span.brand");
			if(CollectionUtils.isNotEmpty(brandElements)){
				brand = brandElements.text();
			}
			//brand
			String category = StringUtils.EMPTY;
			Elements categoryElements = productElements.select("span.category");
			if(CollectionUtils.isNotEmpty(categoryElements)){
				StringBuffer buffer = new StringBuffer();
				for (Element element : categoryElements) {
					if(StringUtils.isNotBlank(element.text())){
						buffer.append(element.text()).append(",");
					}
				}
				category = buffer.toString();
			}
			
			//price
			//sale price
			String sale_price = StringUtils.EMPTY;
			Elements salePriceElements = productElements.select("span.price");
			if(CollectionUtils.isNotEmpty(salePriceElements)){
				sale_price = salePriceElements.text();
			}
			//orign price
			String orign_price = StringUtils.EMPTY;
			Elements orignPriceElements = productElements.select("span.list_price");
			if(CollectionUtils.isNotEmpty(orignPriceElements)){
				orign_price = orignPriceElements.text();
			}
			if(StringUtils.isBlank(orign_price)){
				orign_price = sale_price;
			}
			//unit
			String unit = StringUtils.EMPTY;
			Elements unitElements = productElements.select("span.price_currency_code");
			if(CollectionUtils.isNotEmpty(unitElements)){
				unit = unitElements.text();
			}
			//save
			int save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100);
			
			//gender
			String gender = StringUtils.EMPTY;
			Elements genderElements = productElements.select("span.tag1");
			if(CollectionUtils.isNotEmpty(genderElements)){
				gender = getSex(genderElements.text());
			}
			
			//描述封装
			desc_package(productElements, retBody);
			
			//color
			String color = StringUtils.EMPTY;
			Elements colorElements = doc.select("span#product-color");
			if(CollectionUtils.isNotEmpty(colorElements)){
				color = colorElements.text();
			}
			
			//封装 color size  json
			JSONArray skuJSONArray = new JSONArray();
			//size
			Elements sizeElements = doc.select("div.product-size-container fieldset div");
			if(CollectionUtils.isNotEmpty(sizeElements)){
				for (Element element : sizeElements) {
					JSONObject jsonObject = new JSONObject();
					//skuId
					String skuId = element.attr("data-productid");
					Elements sizeElement = element.select("span.size-type");
					String size = StringUtils.EMPTY;
					if(CollectionUtils.isNotEmpty(sizeElement)){
						size = sizeElement.text();
					}
					//stock status
					int stock_status = 1;
					String stock_flag = element.attr("class");
					if(StringUtils.containsIgnoreCase(stock_flag, SKU_OUTSTOCK)){
						stock_status = 0;
					}
					jsonObject.put("skuId", skuId);
					jsonObject.put("color", color);
					jsonObject.put("size", size);
					jsonObject.put("stock_status", stock_status);
					skuJSONArray.add(jsonObject);
				}
			}
			
			//images
			List<Image> pics = new ArrayList<Image>();
			Elements imageElements = doc.select("div#thumbnail-wrapper a img");
			if(CollectionUtils.isNotEmpty(imageElements)){
				for (Element element : imageElements) {
					String image_url = element.attr("data-large-image");
					if(!StringUtils.contains(image_url, "http")){
						image_url = "http://www.sneakersnstuff.com"+image_url;
					}
					pics.add(new Image(image_url));
				}
			}
			
			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style
			JSONObject styleJsonObject = new JSONObject();
			// iteartor skuarray
			if (null != skuJSONArray && skuJSONArray.size() > 0) {
				for (Object object : skuJSONArray) {
					JSONObject skuJsonObejct = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJsonObejct.getString("skuId");
					// stock
					int stock_number = 0;
					int stock_status = skuJsonObejct.getIntValue("stock_status");
					// spu stock status
					if (stock_status > 0) {
						spu_stock_status = 1;
					}


					String sku_color = skuJsonObejct.getString("color");
					String sku_size = skuJsonObejct.getString("size");

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					if (StringUtils.isNotBlank(sku_size)) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(sku_size);
						selections.add(selection);
					}

					// lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(Float.parseFloat(orign_price));
					lselectlist.setSale_price(Float.parseFloat(sale_price));
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(sku_color);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style json
					styleJsonObject.put(sku_color, skuJsonObejct);

				}
			}
			// stylelist 封装
			if (null != styleJsonObject && styleJsonObject.size() > 0) {
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String sku_color = entry.getKey();
					JSONObject jsonObject = (JSONObject) entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// skuId
					String skuId = jsonObject.getString("skuId");
					String switch_img = StringUtils.EMPTY;

					lStyleList.setDisplay(true);

					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(sku_color);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(sku_color);

					// images
					context.getUrl().getImages().put(skuId, pics);
					// l_style_list
					l_style_list.add(lStyleList);
				}
			}
			
			retBody.setPrice(new Price(Float.parseFloat(orign_price), save, Float.parseFloat(sale_price), unit));
			
			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			retBody.setSku(sku);

			// stock
			retBody.setStock(new Stock(spu_stock_status));
			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			//title
			retBody.setTitle(new Title(title, "", "", ""));
			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			category_package(brand,category,retBody);
			
			properties_package(gender, retBody);

			setOutput(context, retBody);
		}
	}
	
	
	/**
	 * category breadcrumbs  封装
	 * @param doc 
	 * @param brand
	 * @param category 
	 * @param retBody 
	 */
	private static void category_package(String brand, String category, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String[] categoryArray = category.split(",");
		if (null != categoryArray && categoryArray.length != 0) {
			for (String string : categoryArray) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(string));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		retBody.setCategory(cats);
		
		// BreadCrumb
		if(StringUtils.isNotBlank(brand)){
			breads.add(brand);
		}
		retBody.setBreadCrumb(breads);
	}
	
	
	private static void desc_package(Elements elements , RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = elements.select("span.description p");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String text = e.text();
				if (StringUtils.isNotBlank(text)) {
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}
	

	private static void properties_package(String gender, RetBody retBody) {
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getTitle().getEn());
		}
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}
	
	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		}
		return gender;
	}
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
