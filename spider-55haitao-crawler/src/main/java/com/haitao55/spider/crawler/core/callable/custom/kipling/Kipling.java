package com.haitao55.spider.crawler.core.callable.custom.kipling;

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
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * kipling 详情页数据封装
* Title:
* Description:
* Company: 55海淘
* @author denghuan lastUpdatTime 2017年6月8日
* @date 2017年2月6日 下午5:33:16
* @version 1.0
 */
public class Kipling extends AbstractSelect{
	
	private static final String color_url = "http://www.kipling-usa.com/on/demandware.store/Sites-kip-Site/default/Product-Variation?pid=()&dwvar_()_color={}&format=ajax";
	private static final String domain = "www.kipling-usa.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = crawler_package(context);
		context.put(input, content);
		Document doc = this.getDocument(context);
		
		Pattern p = Pattern.compile("This item is currently not available");
		Matcher matcher = p.matcher(content);
		if(matcher.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"kipling.com itemUrl:"+context.getUrl().toString()+" is offline..");
		}
		
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		//spu stock status
		int spu_stock_status = 0;
		
		
		//productid
		String productId = StringUtils.EMPTY;
		//商品id
		Elements productElements = doc.select("div.mobile-item-number span");
		if(CollectionUtils.isNotEmpty(productElements)){
			productId = productElements.text();
		}
		
		//default colorid
		String colorId = doc.select("ul.Color li.selected a").attr("data-colorid");
		
		//title
		String title = StringUtils.substringBetween(content, "pageContext = {\"title\":\"", "\",");
		if(StringUtils.isEmpty(title)){
			Elements titleElements = doc.select("div.hidden-mobile span.last");
			
			if(CollectionUtils.isNotEmpty(titleElements)){
				
				title = titleElements.text();
			}
		}
		
		//封装不同颜色请求url
		List<String> url_list = new ArrayList<String>();
		Elements colorElemets = doc.select("ul.swatches.Color li a");
		if(CollectionUtils.isNotEmpty(colorElemets)){
			for (Element element : colorElemets) {
				String skuColorId = element.attr("data-colorid");
				String temp_url = StringUtils.replacePattern(color_url, "\\(\\)", productId);
				temp_url = StringUtils.replacePattern(temp_url, "\\{\\}", skuColorId);
				url_list.add(temp_url);
			}
		}
		
		//sku jsonarray
		JSONArray skuJsonArray = new JSONArray();
		//String image_url_temp = StringUtils.replacePattern(image_url, "\\{\\}", productId);
		//context.setCurrentUrl(image_url_temp);
		//String image_content = crawler_package(context);
		//context.setCurrentUrl(url);
		new KiplingHandler().process(url_list,skuJsonArray,productId,context);
		
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
				String skuId = skuJsonObejct.getString("color").concat(skuJsonObejct.getString("colorId"));
				skuJsonObejct.put("skuId", skuId);
				//stock
				int stock_status = skuJsonObejct.getIntValue("stock_status");
				int stock_number=0;
				if(stock_status > 0){
					spu_stock_status =1;
				}
				
				//price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				int save = skuJsonObejct.getIntValue("save");
				String unit = skuJsonObejct.getString("unit");
				
				//sku color id
				String sku_color_id = skuJsonObejct.getString("colorId");
				if(StringUtils.containsIgnoreCase(colorId, sku_color_id)){
					retBody.setPrice(new Price(orign_price, save, sale_price, unit));
				}
				//style_id
				String style_id = skuJsonObejct.getString("color");
				
				//selections
				List<Selection> selections = new ArrayList<Selection>();
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
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
		}
		
		if(null != styleJsonObject  && styleJsonObject.size()>0){
			for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
				String style_id = entry.getKey();
				
				JSONObject jsonObject = (JSONObject)entry.getValue();
				
				//sku color id
				String sku_color_id = jsonObject.getString("colorId");
				
				// stylelist
				LStyleList lStyleList = new LStyleList();
				//skuId
				String skuId = jsonObject.getString("skuId");
				String switch_img=StringUtils.EMPTY;
				if(StringUtils.isNotBlank(colorId)){
					if(StringUtils.containsIgnoreCase(colorId, sku_color_id)){
						lStyleList.setDisplay(true);
					}
				}else{
					lStyleList.setDisplay(true);
				}
				
				//switch_img
				switch_img = jsonObject.getString("switch_img");
				
				// stylelist
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_switch_img(switch_img);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_id(style_id);
				lStyleList.setStyle_cate_name("Color");
				lStyleList.setStyle_name(style_id);
				
				//images
				@SuppressWarnings("unchecked")
				List<Image> sku_pics = (List<Image>) jsonObject.get(sku_color_id);
				context.getUrl().getImages().put(skuId, sku_pics);
				
				//l_style_list
				l_style_list.add(lStyleList);
			}
		}
		
		//sku 
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);
		
		//stock
		retBody.setStock(new Stock(spu_stock_status));
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(domain.concat(productId));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//brand
		retBody.setBrand(new Brand("Kipling", "", "", ""));;
		
		//title
		retBody.setTitle(new Title(title, "", "", ""));
		
		//category breadcrumb
		category_package(doc,title,"Kipling" ,retBody);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(retBody);
		
		setOutput(context, retBody);
		
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

	/**
	 * category breadcrumbs  封装
	 * @param doc 
	 * @param title
	 * @param brand
	 * @param retBody 
	 */
	private static void category_package(Document doc, String title, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("div.breadcrumb a:not(:first-child)");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element elements : categoryElements) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(elements.text()));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		cats.add(title);
		breads.add(title);
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
		Elements es = doc.select("div.description-content");
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
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = StringUtils.EMPTY;
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
}
