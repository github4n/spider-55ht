package com.haitao55.spider.crawler.core.callable.custom.nordstromrack;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * 
* Title:	nordstromrack 网站详情封装
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月8日 下午4:06:37
* @version 1.0
 */
public class Nordstromrack extends AbstractSelect{
	private static final String stock_flag = "sold_out";
	private static final String stock_flag2 = "on_hold";
	private static final String domain = "www.nordstromrack.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		HashMap<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");

//		headers.put("Cookie", "split_cookie=mythings; cmTPSet=Y; CoreID6=72883509382514865382477&ci=90409925; AMCVS_246B0A595411C35F0A4C98BC%40AdobeOrg=1; __bcOnsite__=15a1c95a17f60b-00378e5f6eab56-24414032-100200-15a1c95a180603; AMCV_246B0A595411C35F0A4C98BC%40AdobeOrg=817868104%7CMCIDTS%7C17206%7CMCMID%7C19829779071216455882289296883148516977%7CMCAAMLH-1487143052%7C9%7CMCAAMB-1487143052%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1486545452s%7CNONE%7CMCAID%7CNONE; AAMC_nordstrom_1=AMSYNCSOP%7C411-17213; SSP_AB_ec407f5_20160726=Test; SSP_AB_80eb483_20160922=Control; 90409925_clogin=v=1&l=1486545537&e=1486547473129; _sp_id.1223=7f7bd79c-64cb-456a-8eea-e7b40ef6554b.1486538250.2.1486545674.1486541272.519265da-7524-4e17-b4f9-a664fd201ebd; _sp_ses.1223=*; mp_nrhl_mixpanel=%7B%22distinct_id%22%3A%20%2215a1c95a17f60b-00378e5f6eab56-24414032-100200-15a1c95a180603%22%7D; _sp_id.edec=a-00co--18428f23-b2fc-4c18-8688-4c0815d53ceb.1486538253.2.1486545674.1486541270.5d6f228b-b358-4bd2-9ce6-4f960f80b5f1; _sp_ses.edec=*; utag_main=v_id:015a1c958dd40056d6e297dda37402085001e07d0086e$_sn:2$_ss:1$_st:1486547473338$dc_visit:2$_pn:1%3Bexp-session$ses_id:1486545673182%3Bexp-session$dc_event:1%3Bexp-session$dc_region:us-east-1%3Bexp-session; aam_uuid=19570530052019494702263373077586593147");
		headers.put("Host", "www.nordstromrack.com");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");

		String content = crawler_package(context);
		context.put(input, content);
		Document doc = this.getDocument(context);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		String productData = StringUtils.substringBetween(content, "window.__INITIAL_STATE__ =", "</script>");
		
		if(StringUtils.isNoneBlank(productData)){
			JSONObject productJsonObject = JSONObject.parseObject(productData);
			
			//productId
			String productId = StringUtils.EMPTY;
			Elements elements = doc.select("div.fp-root");
			if(CollectionUtils.isNotEmpty(elements)){
				productId = elements.attr("data-product-id");
			}
			
			//brand
			String brand = StringUtils.substringBetween(productData, "\"brandName\":\"", "\"");
			//title
			String title = StringUtils.EMPTY;
			Pattern pattern = Pattern.compile("\"maxItemsPerPerson\":\\d+,\"name\":\"(.*?)\",\"returnability\"");
	        Matcher matcher = pattern.matcher(productData);
	        if(matcher.find()){
	            title = matcher.group(1); 
	        }
			//String title = StringUtils.substringBetween(productData, "\"name\":\"", "\"");
			
			//gender
			String gender = StringUtils.substringBetween(productData, "\"division\":\"", "\"");
			
			//spu stock status
			int spu_stock_status = 0;
			
			//default color
			String default_color = StringUtils.substringAfter(url, "color=");
			
			//unit
			String unit = StringUtils.EMPTY;
			Elements salePriceElements = doc.select("span.product-details__sale-price");
			if(CollectionUtils.isNotEmpty(salePriceElements)){
				String sale_price = salePriceElements.text();
				unit = getCurrencyValue(sale_price);// 得到货币代码
			}
			
			
			//sku jsonarray
			JSONObject skuJsonObject = productJsonObject.getJSONObject("productPage");
			skuJsonObject = skuJsonObject.getJSONObject("product");
			JSONArray skuJsonArray = skuJsonObject.getJSONArray("skus");
			
			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			//style 
			JSONObject styleJsonObject = new JSONObject();
			
			if(null != skuJsonArray && skuJsonArray.size() > 0){
				for (Object object : skuJsonArray) {
					JSONObject skuJsonObejct = (JSONObject)object;
					
					// selectlist
					LSelectionList lselectlist = new LSelectionList();
					
					//skuId
					String skuId = skuJsonObejct.getString("sku");
					
					//color
					String color = skuJsonObejct.getString("color");
					
					//size
					String size = skuJsonObejct.getString("size");
					
					if(StringUtils.equals("null", skuId) || StringUtils.isBlank(skuId)){
						skuId = color.concat(size);
					}
					
					//stock
					String sku_stock_flag = skuJsonObejct.getString("inventoryLevel");
					int stock_status =0;
					int stock_number=0;
					if(!StringUtils.contains(stock_flag, sku_stock_flag) && !StringUtils.contains(stock_flag2, sku_stock_flag)){
						stock_status = 1;
						spu_stock_status =1;
					}
					
					JSONObject priceJsonObject = skuJsonObejct.getJSONObject("prices");
					
					//price
					String sale_price = priceJsonObject.getString("sale");
					String orign_price = priceJsonObject.getString("retail");
					String save = StringUtils.EMPTY;
					if (StringUtils.isBlank(orign_price)) {
						orign_price = sale_price;
					}
					if (StringUtils.isBlank(sale_price)) {
						sale_price = orign_price;
					}
					if (StringUtils.isBlank(orign_price)
							|| Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
						orign_price = sale_price;
					}
					if (StringUtils.isBlank(save)) {
						save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100)
								+ "";// discount
					}
					
					//spu price
					if(StringUtils.containsIgnoreCase(default_color, StringUtils.replacePattern(color, " ", ""))){
						retBody.setPrice(new Price(Float.parseFloat(orign_price), StringUtils.isBlank(save)?0:Integer.parseInt(save), Float.parseFloat(sale_price), unit));
					}
					//style_id
					String style_id = color;
					
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
					lselectlist.setOrig_price(Float.parseFloat(orign_price));
					lselectlist.setSale_price(Float.parseFloat(sale_price));
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);
					
					//l_selection_list
					l_selection_list.add(lselectlist);
					
					//style json
					styleJsonObject.put(style_id, skuJsonObejct);
				}
				
				//stylelist
				if(!styleJsonObject.isEmpty()){
					for (Map.Entry<String,Object> entry : styleJsonObject.entrySet()) {
						String style_id = entry.getKey();
						
						JSONObject jsonObject = (JSONObject) entry.getValue();
						
						// stylelist
						LStyleList lStyleList = new LStyleList();
						//skuId
						String skuId = jsonObject.getString("sku");
						
						//color
						String color = jsonObject.getString("color");
						
						//size
						String size = jsonObject.getString("size");
						
						if(StringUtils.equals("null", skuId) || StringUtils.isBlank(skuId)){
							skuId = color.concat(size);
						}
						String switch_img=StringUtils.EMPTY;
						if(StringUtils.containsIgnoreCase(default_color, StringUtils.replacePattern(style_id, " ", ""))){
							lStyleList.setDisplay(true);
						}
						
						//switch_img
						switch_img = jsonObject.getString("swatch");
						
						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(style_id);
						
						//images
						List<Image> sku_pics = new ArrayList<Image>();
						JSONArray imagesJsonArray = jsonObject.getJSONArray("originalImageTemplates");
						for (int i = 0; i<imagesJsonArray.size() ; i++) {
							String image_url = imagesJsonArray.getString(i);
							image_url = StringUtils.replacePattern(image_url, "\\{size\\}", "large");
							sku_pics.add(new Image(image_url));
						}
						
						context.getUrl().getImages().put(skuId, sku_pics);
						
						//l_style_list
						l_style_list.add(lStyleList);
						
					}
				}
			}
			//sku 
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			retBody.setSku(sku);
			
			//stock
			retBody.setStock(new Stock(spu_stock_status));
			
			// full doc info
			String docId = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docId = domain + productId;
			}else{
				docId = url;
			}
			String docid = SpiderStringUtil.md5Encode(docId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(domain));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			//brand
			retBody.setBrand(new Brand(brand, "", "", ""));;
			
			//title
			retBody.setTitle(new Title(title, "", "", ""));
			
			//category breadcrumb
			category_package(doc,brand ,retBody);
			
			// description
		    desc_package(doc,retBody);
		    
		    //properties
			properties_package(retBody,gender);
		}
		
		setOutput(context, retBody);
	}
	
	/**
	 * category breadcrumbs  封装
	 * @param doc 
	 * @param brand
	 * @param retBody 
	 */
	private static void category_package(Document doc , String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("ul.category-breadcrumbs__category-breadcrumbs li a");
		if(CollectionUtils.isEmpty(categoryElements)){
			categoryElements = doc.select("ul.category-breadcrumbs.category-breadcrumbs--nordstromrack li a");
		}
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
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

	
	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Elements es = doc.select("dl.product-details-section__definition-list dt , dl.product-details-section__definition-list dd");
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
	 * @param gender 
	 */
	private static void properties_package(RetBody retBody, String gender) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getTitle().getEn());
		}
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
	
	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		unit = Currency.codeOf(currency).name();
		return unit;

	}
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(context.getCurrentUrl(),getHeaders());
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			if (StringUtils.isBlank(proxyRegionId)) {
				content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString())
						.method(HttpMethod.GET.getValue()).header(getHeaders()).resultAsString();
			} else {
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress = proxy.getIp();
				int proxyPort = proxy.getPort();
				content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString())
						.method(HttpMethod.GET.getValue()).header(getHeaders()).proxy(true).proxyAddress(proxyAddress)
						.proxyPort(proxyPort).resultAsString();
			}
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-insecure-requests", "1");
		headers.put("Connection", "keep-alive");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Cache-Control", "max-age=0");
		return headers;
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
	    String url = "https://www.nordstromrack.com/shop/product/1345570/kate-spade-new-york-montford-park-nisha-zip-around-leather-wallet?color=BLACK";
	    String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("47.88.19.97").proxyPort(3128).retry(3).resultAsString();
	      //System.out.println(content);
	    Pattern pattern = Pattern.compile("\"maxItemsPerPerson\":\\d+,\"name\":\"(.*?)\",\"returnability\"");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            System.out.println(matcher.group(1) ); 
        }
  }
}
