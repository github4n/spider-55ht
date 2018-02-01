package com.haitao55.spider.crawler.core.callable.custom.mybag;

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

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * 
* Title:mybag 详情数据封装
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年2月14日 下午2:47:30
* @version 1.0
 */
public class Mybag extends AbstractSelect{
	private static final String INSTOCK = "InStock";
	private static final String domain = "www.mybag.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = crawler_package(context);
		context.put(input, content);
		Document doc = this.getDocument(context);
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		//productId
		String productId = StringUtils.substringBetween(content, "productID: \"", "\",");
		//title
		String title = StringEscapeUtils.unescapeHtml(StringUtils.substringBetween(content, "productTitle: \"", "\","));
		//brand
		String brand = StringEscapeUtils.unescapeHtml(StringUtils.substringBetween(content, "productBrand: \"", "\","));
		//sale_price
		String sale_price = StringEscapeUtils.unescapeHtml(StringUtils.substringBetween(content, "productPrice: \"", "\","));
		
		//orign_price
		String save_price = StringUtils.EMPTY;
		String orign_price = StringUtils.EMPTY;
		
		Elements priceElements = doc.select("p.yousave.saving-amount span");
		if(CollectionUtils.isNotEmpty(priceElements)){
			save_price = priceElements.text();
		}
		
		//save
		String save = StringUtils.EMPTY;
		
		//unit
		String unit = getCurrencyValue(sale_price);
		
		sale_price = sale_price.replaceAll("[£,]", "");
		orign_price = orign_price.replaceAll("[£,]", "");
		save_price = save_price.replaceAll("[£,]", "");
		
		if(StringUtils.isNotBlank(save_price)){
			orign_price = String.valueOf(Float.parseFloat(sale_price)+Float.parseFloat(save_price));
		}

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
		
		int stock_status = 1;
		String instock  = doc.select(".product-stock-message").text();
		if(StringUtils.isNotBlank(instock) && 
				!"In stock".equals(instock)){
			stock_status = 0;
		}
		
		//images
		List<Image> images = new ArrayList<Image>();
		Elements imageElements = doc.select("ul.product-thumbnails.nav-items li a");
		if(CollectionUtils.isNotEmpty(imageElements)){
			for (Element element : imageElements) {
				String image_url = element.attr("href");
				images.add(new Image(image_url));
			}
		}else{
			imageElements = doc.select("div.product-image.main-product-image a");
			if(CollectionUtils.isNotEmpty(imageElements)){
				String image_url = imageElements.attr("href");
				images.add(new Image(image_url));
			}
		}
		
		context.getUrl().getImages().put(productId, images);
		
		//price
		retBody.setPrice(new Price(Float.parseFloat(orign_price), StringUtils.isBlank(save)?0:Integer.parseInt(save), Float.parseFloat(sale_price), unit));
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		//sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//stock
		retBody.setStock(new Stock(stock_status));
		
		//brand
		retBody.setBrand(new Brand(brand, "", "", ""));;
		
		//title
		retBody.setTitle(new Title(title, "", "", ""));
		
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(domain.concat(productId));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//category breadcrumb
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		cats.add(title);
		breads.add(title);
		breads.add(brand);
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(retBody);
		
		setOutput(context, retBody);
		
	}
	
	/***
	 * 描述　　封装
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Elements es = doc.select("div[itemprop=description] p");
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
	 * @param gender 
	 * @param retBody
	 */
	private static void properties_package(RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();

		String gender = getSex(retBody.getTitle().getEn());
		
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
		} 
		else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
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
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHerder()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHerder()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private Map<String, Object> getHerder() {
		final Map<String,Object> header = new HashMap<String,Object>(){
			{
				put("Cookie", "en_chosenSubsite_V6=EN; en_currency_V6=GBP; en_shippingCountry_V6=GB;");
			}
		};
		return header;
	}
	
	
}
