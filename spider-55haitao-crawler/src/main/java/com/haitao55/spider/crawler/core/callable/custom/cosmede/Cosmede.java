package com.haitao55.spider.crawler.core.callable.custom.cosmede;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * cosme-de 详情页封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年12月29日 下午1:46:20
* @version 1.0
 */
public class Cosmede extends AbstractSelect {
	
	private static final String OUT_OF_STOCK= "Email Me when it's restocked";
	private static final String domain = "www.cosme-de.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = crawler_package(context);
		Document doc = JsoupUtils.parse(content);
		
		String url = context.getCurrentUrl().toString();
				
        RetBody retBody = new RetBody();
		
		Sku sku = new Sku();
		
		
		//default skuNO
		String itemNO = StringUtils.substringBetween(content, "item: \"", "\" }");

		//spu stock status
		int spu_stock_status = 0;
		
		//sku json array
		JSONArray skuJsonArray = new JSONArray();
		
		skuJsonArray = sku_package(doc);
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		if(null!=skuJsonArray && skuJsonArray.size()>0){
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObejct = (JSONObject)object;
				
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				
				//skuId
				String skuId = skuJsonObejct.getString("skuId");
				//stock
				int stock_status = skuJsonObejct.getIntValue("stock_status");
				
				//spu stock status
				if(stock_status>0){
					spu_stock_status=1;
				}
				
				
				String color = skuJsonObejct.getString("color_value");
				String size = skuJsonObejct.getString("size_value");
				//style id
				String style_id = StringUtils.EMPTY;
				if(StringUtils.isNotBlank(color)){
					style_id = color;
				}else{
					style_id = size;
				}
				
				//price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				int save = skuJsonObejct.getIntValue("save");
				String unit = skuJsonObejct.getString("unit");
				
				
				String skuNO = skuJsonObejct.getString("skuNO");
				//spu price
				if(itemNO.equals(skuNO)){
					retBody.setPrice(
							new Price(orign_price, save, sale_price, unit));
				}
				

				//selections
				List<Selection> selections = new ArrayList<Selection>();
				if(StringUtils.isNotBlank(color) && StringUtils.isNotBlank(size)){
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name(skuJsonObejct.getString("size_catename"));
					selection.setSelect_value(size);
					selections.add(selection);
				}
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(0);
				lselectlist.setStyle_id(style_id);
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
				
				//style json
				styleJsonObject.put(style_id, skuJsonObejct);
			}
			
			if(null != styleJsonObject  && styleJsonObject.size()>0){
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();

					JSONObject jsonObject = (JSONObject)entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					
					//skuId
					String skuId = jsonObject.getString("skuId");
					String switch_img=jsonObject.getString("switch_image");
					String skuNO=jsonObject.getString("skuNO");
					if(itemNO.equals(skuNO)){
						lStyleList.setDisplay(true);
					}
					
					String style_catename = StringUtils.EMPTY;
					String color_value = jsonObject.getString("color_value");
					if(StringUtils.isNotBlank(color_value)){
						style_catename = jsonObject.getString("color_catename");
					}else{
						style_catename = jsonObject.getString("size_catename");
					}
					
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name(style_catename);
					lStyleList.setStyle_name(style_id);
					
					//images
					String image=jsonObject.getString("image");
					List<Image> pics = new ArrayList<Image>();
					pics.add(new Image(image));
					
					context.getUrl().getImages().put(skuId, pics);
							
					//l_style_list
					l_style_list.add(lStyleList);
				}
			}
		}
		
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//brand
		String brand =  StringUtils.EMPTY;
		Elements brandElements = doc.select("a.brand_page_h_txt b");
		if(CollectionUtils.isNotEmpty(brandElements)){
			brand = brandElements.text();
		}
		
		retBody.setBrand(new Brand(brand, "", "", ""));
		
		//title
		String title =  StringUtils.EMPTY;
		Elements titleElements = doc.select("td.content_h_txt");
		if(CollectionUtils.isNotEmpty(titleElements)){
			title = titleElements.get(0).text();
		}
		
		retBody.setTitle(new Title(title, "", "", ""));
		
		//stock
		retBody.setStock(new Stock(spu_stock_status));
		
		//category breadcrumb
		category_package(doc,retBody,brand,title);
		
		// description
		desc_package(doc,retBody);
		
		//properties
		properties_package(retBody);
		
		setOutput(context, retBody);
	}
	private static JSONArray sku_package(Document doc) {
		JSONArray skuJsonArray = new JSONArray();
		//sku elements
		Elements elements = doc.select("form[name=addtocart] + tbody tr");
		
		if(CollectionUtils.isNotEmpty(elements)){
			//i = 0 ,i = 1  表头
			for (int i=2 ; i<elements.size()-1 ;i+=2) {//element 元素为２个ｔｒ作为一个组合，所以一次循环加ｉ＋２个
				//skuJsonObejct
				JSONObject skuJsonObject = new JSONObject();
				
				
				Element skuImageElement = elements.get(i+1);
				
				Element skuDataElement = elements.get(i+2);
				
				//switch _image
				String switch_image = skuImageElement.select("td>img").attr("src");
				switch_image = image_url_filling(switch_image);
				// image url 根据switch_image 获取
				String image = StringUtils.replacePattern(switch_image, "_\\d+","");
				
				//sku data
				//color
				String color_value = StringUtils.EMPTY;
				Elements colorElements = skuDataElement.select("td font.product_color");
				if(CollectionUtils.isNotEmpty(colorElements)){
					color_value = colorElements.text();
				}
				String color_catename = StringUtils.EMPTY;
				if(StringUtils.isNotBlank(color_value)){
					color_catename = "Color";
				}
				
				//size
				String size_value = StringUtils.EMPTY;
				String size_catename = StringUtils.EMPTY;
				Elements sizeElements = skuDataElement.select("td:nth-child(2)");
				
				if(CollectionUtils.isNotEmpty(sizeElements)){
					size_value = sizeElements.text();
				}
				if(StringUtils.isNotBlank(size_value)){
					size_catename = "Size";
				}
				
				
				//skuid
				String text = skuDataElement.select("td:nth-child(1)").text();
				String skuId = StringUtils.substringAfter(text, "SKU: ");
				
				
				
				//price
				String sale_price = StringUtils.EMPTY;
				Elements salePriceElements = skuDataElement.select("td.price_txt>b");
				if(CollectionUtils.isNotEmpty(salePriceElements)){
					sale_price = salePriceElements.text();
				}
				//orignPrice
				String orign_price = StringUtils.EMPTY;
				Elements orignPriceElements = skuDataElement.select("td font.price_r_txt b");
				if(CollectionUtils.isNotEmpty(orignPriceElements)){
					orign_price = orignPriceElements.text();
				}
				//save
				String save = StringUtils.EMPTY;
				Elements saveElements = skuDataElement.select("td font.save_txt");
				if(CollectionUtils.isNotEmpty(saveElements)){
					save = saveElements.text();
				}
				
				// price trans
				String unit = getCurrencyValue(sale_price);// 得到货币代码
				sale_price = sale_price.replaceAll("[USD ]", "");

				if (StringUtils.isBlank(orign_price)) {
					orign_price = sale_price;
				}
				orign_price = orign_price.replaceAll("[USD ]", "");
				if (StringUtils.isBlank(sale_price)) {
					sale_price = orign_price;
				}
				if (StringUtils.isBlank(orign_price)
						|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
					orign_price = sale_price;
				}

				if(StringUtils.isNotBlank(save)){
					save = StringUtils.replacePattern(save, "[save %]", "");
				}
				if(StringUtils.isBlank(save)){
					save = "0";
				}
				
				
				//stock status
				int stock_status = 1;
				Elements stockElements = skuDataElement.select("td font.product_name a");
				if(CollectionUtils.isNotEmpty(stockElements)){
					String stockText = stockElements.text();
					Elements select = stockElements.select("img");
					if(StringUtils.equals(stockText, OUT_OF_STOCK)||CollectionUtils.isEmpty(select)){
						stock_status = 0;
					}
				}
				
				//sku NO >> item NO
				String skuNO = StringUtils.EMPTY;
				String image_temp = image;
				image_temp=StringUtils.substringBetween(image_temp, "/", ".jpg");
				int lastIndexOf = image_temp.lastIndexOf("/");
				skuNO=image_temp.substring(lastIndexOf+1, image_temp.length());
				
				
				//数据封装
				skuJsonObject.put("switch_image", switch_image);
				skuJsonObject.put("image", image);
				skuJsonObject.put("color_catename", color_catename);
				skuJsonObject.put("color_value", color_value);
				skuJsonObject.put("size_catename", size_catename);
				skuJsonObject.put("size_value", size_value);
				skuJsonObject.put("skuId", skuId);
				skuJsonObject.put("sale_price", sale_price);
				skuJsonObject.put("orign_price", orign_price);
				skuJsonObject.put("unit", unit);
				skuJsonObject.put("save", save);
				skuJsonObject.put("stock_status", stock_status);
				skuJsonObject.put("skuNO", skuNO);
				
				//add
				skuJsonArray.add(skuJsonObject);
			}
		}
		
		return skuJsonArray;
	}

	/**
	 * 图片地址填充
	 * @param switch_image
	 * @return
	 */
	private static String image_url_filling(String switch_image) {
		String url = StringUtils.EMPTY;
		if(StringUtils.isEmpty(switch_image)){
			return url;
		}
		if(!StringUtils.contains(switch_image, "http")){
			url ="http:".concat(switch_image);
		}
		
		return url;
	}
	
	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 3);
		String unit = StringUtils.EMPTY;
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.valueOf(currency).name();
		return unit;

	}
	
	
	/**
	 *　分类　封装
	 * @param doc
	 * @param retBody
	 * @param brand 
	 * @param title 
	 */
	private static void category_package(Document doc, RetBody retBody, String brand ,String title) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("tr table tbody ul li.category_h3b_txt a.category_h3h_txt");
//		if (CollectionUtils.isNotEmpty(cateElements)) {
			//tr td table tbody tr td ul li
//			Elements parentCateElements = cateElements.parents().parents().parents().parents().parents().parents().parents().parents();
			
//		}else{
		if (CollectionUtils.isEmpty(cateElements)) {
			cateElements = doc.select("tr td a.category_h3h_txt");
		}
		for (Element element : cateElements) {
			String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
			if (StringUtils.isNotBlank(cat)) {
				cats.add(cat);
				breads.add(cat);
			}
		}
		//cats
		cats.add(title);
		retBody.setCategory(cats);
		
		// BreadCrumb
		breads.add(title);
		breads.add(brand);
		retBody.setBreadCrumb(breads);
	}
	
	
	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		Elements es = doc.select("td[itemprop=description] ul li");
		if(CollectionUtils.isEmpty(es)){
			es = doc.select("td[itemprop=description]");
		}
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
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}
	
	/**
	 * properties 封装
	 * @param retBody
	 */
	private static void properties_package(RetBody retBody) {
		String gender = StringUtils.EMPTY;
		Map<String, Object> propMap = new HashMap<String, Object>();
		gender = getSex(retBody.getTitle().getEn());
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
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
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
//			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHeaders()).method(HttpMethod.GET.getValue())
//					.resultAsString();
			content = CurlCrawlerUtil.get(context.getCurrentUrl());
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = CurlCrawlerUtil.get(context.getCurrentUrl(), 20, proxyAddress, proxyPort);
//			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHeaders()).method(HttpMethod.GET.getValue())
//					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	private Map<String,Object> getHeaders(){
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Retry-After","120");
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
		return headers;
	}
}
