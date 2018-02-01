package com.haitao55.spider.crawler.core.callable.custom.ashford;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * ashford 详情页解析
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月4日 上午11:01:50
* @version 1.0
 */
public class Ashford extends AbstractSelect{
	private static final String domain = "zh.ashford.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_WOMEN2 = "女士";
	private static final String SEX_MEN = "men";
	private static final String SEX_MEN2 = "男士";
	private static final String image_preffix = "http://zh.ashford.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		
		String url = context.getCurrentUrl().toString();
		
		String content = this.getInputString(context);
		Document doc = this.getDocument(context);
		
		
		String itemData = StringUtils.substringBetween(content, "var _goodsData = ", ";</script>");
		
		if(StringUtils.isBlank(itemData)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"ashford.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
		}
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		JSONObject itemJson = JSONObject.parseObject(itemData);
		
		//productId
		String productId = itemJson.getString("id");
		
		//stock
		int sold_out = itemJson.getIntValue("soldOut");
		int stock_status = 1;
		if(0 != sold_out){
			stock_status = 0;
		}
		
		Elements stockElements = doc.select("input#add_To_Cart_Button");
		if(CollectionUtils.isEmpty(stockElements)){
			stock_status = 0;
		}
		
		//category
		String category = itemJson.getString("category");
		
		//title
		String title = itemJson.getString("name");
		
		//sale_price
		float sale_price = itemJson.getFloatValue("price");
	
		Elements salePriceElements = doc.select("td[class=highlight]");
		if(CollectionUtils.isNotEmpty(salePriceElements)){
			String special_offers = salePriceElements.text();
			sale_price = Float.parseFloat(StringUtils.replacePattern(special_offers, "[$,]", ""));
		}
		
		//orign_price
		float orign_price = itemJson.getFloatValue("origPrice");
		
		//unit
		String unit = itemJson.getString("currency");
		
		int save = Math.round((1 - sale_price / orign_price) * 100);
		
		
		//image
		List<Image> pics = new ArrayList<Image>();
		Elements imageElements = doc.select("ul.alt_imgs li a");
		Set<String> picUrls = new HashSet<String>();
		if(CollectionUtils.isNotEmpty(imageElements)){
			for (Element element : imageElements) {
				String 	image_url = element.attr("href");
				if(!StringUtils.contains(image_url, image_preffix)){
					image_url = image_preffix.concat(image_url);
					picUrls.add(image_url);
				}			
			}
		}else{
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"imagList not found..");
		}
		for(String picUrl : picUrls){
			pics.add(new Image(picUrl));
		}
		
		
		//brand
		String brand = StringUtils.EMPTY;
		Elements brandElements = doc.select("h1#prodName a#sameBrandProduct");
		if(CollectionUtils.isNotEmpty(brandElements)){
			brand = brandElements.text();
		}
		
		//desc
//		String desc = StringUtils.EMPTY;
//		Elements descElements = doc.select("div.commaSeparated");
//		if(CollectionUtils.isNotEmpty(descElements)){
//			desc = descElements.text();
//		}
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//给定默认goods_id
	    List<String> skuIds = new ArrayList<String>();
		
		//size 集合
		List<String> listSize = new ArrayList<String>();
		Elements elements = doc.select("div#sizeValues div");
		if(CollectionUtils.isNotEmpty(elements)){
			for (Element element : elements) {
				listSize.add(StringUtils.replacePattern(StringUtils.substringBetween(element.toString(), "this,'", "');"), " ", "%20"));
			}
		}
		
		JSONArray skuJsonArray = new AshfordHandler().process(productId,listSize, context.getUrl());
		//iterator
		if(null != skuJsonArray && skuJsonArray.size() > 0){
			for (Object object : skuJsonArray) {
				JSONObject jsonObject = (JSONObject)object;
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				
				//skuid
				String sku_Id  = jsonObject.getString("goods_id");
				
				//stock
				int sku_stock_status = jsonObject.getIntValue("stock_status");
				int stock_number = 0;
				if(sku_stock_status > 0){
					stock_status = 1;
					skuIds.add(sku_Id);
				}
				
				//selections
				String size = jsonObject.getString("size");
				List<Selection> selections = new ArrayList<Selection>();
				Selection selection = new Selection();
				selection.setSelect_id(0);
				selection.setSelect_name("Size");
				selection.setSelect_value(size);
				selections.add(selection);
				
				//lselectlist
				lselectlist.setGoods_id(sku_Id);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(sku_stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id("default");
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
			}
			
			//style package
			 LStyleList lStyleList = new LStyleList();
			 if(null == skuIds || skuIds.size() == 0){
				 throw new ParseException(CrawlerExceptionCode.OFFLINE,
							"ashford.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
			 }
			 lStyleList.setGood_id(skuIds.get(0));
			 lStyleList.setDisplay(true);
			 lStyleList.setStyle_cate_name("color");
			 lStyleList.setStyle_cate_id(0);
			 lStyleList.setStyle_name("default");
			 lStyleList.setStyle_id("default");
			 lStyleList.setStyle_switch_img("");
			 context.getUrl().getImages().put(skuIds.get(0), pics);// picture
			 l_style_list.add(lStyleList);
		}else{
			context.getUrl().getImages().put(productId,pics);
		}
		
		
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
		
		//price
		retBody.setPrice(new Price(orign_price, save, sale_price, unit));
		
		// full doc info
		String docid = SpiderStringUtil.md5Encode(domain.concat(productId));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//category breadcrumb
		category_package(category,brand ,retBody);
		
		// description
	    desc_package(doc,retBody);
	    
	    //properties
		properties_package(productId , doc , retBody);
		
		setOutput(context, retBody);
	}
	
	/**
	 * category breadcrumbs  封装
	 * @param category
	 * @param brand
	 * @param retBody 
	 */
	private static void category_package(String category, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String[] category_data = StringUtils.split(category, "-");
		if (null != category_data && category_data.length>0) {
			for (String cat : category_data) {
				cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(StringUtils.replacePattern(cat, " ", "")));
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
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Element es = doc.select("div.commaSeparated").get(0);
		StringBuilder sb = new StringBuilder();
		String text = es.text();
		if(StringUtils.isNotBlank(text)){
			featureMap.put("feature-" + 1, text);
			sb.append(text);
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	/**
	 * properties 封装
	 * @param productId 
	 * @param retBody
	 * @param gender 
	 */
	private static void properties_package(String productId, Document doc , RetBody retBody) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = getSex(retBody.getDescription().get("en").toString());
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getTitle().getEn());
		}
		if(StringUtils.isBlank(gender)){
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
		
		List<List<Object>> propattr=new ArrayList<List<Object>>();
		propMap.put("s_gender", gender);
		propMap.put("s_identifier", productId);
		Elements es = doc.select("div.col1 , div.col2");
		if(CollectionUtils.isNotEmpty(es)){
			es = es.select("tr td.head");
		}
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				List<Object> proList=new ArrayList<Object>();
				List<List<String>> list=new ArrayList<List<String>>();
				
				String keyValue=StringUtils.EMPTY;
				keyValue=e.text();
				
				//td --> tr
				Element parent = e.parent();
				
				Element key = parent.nextElementSibling();//同级元素
				
				//递归查找
				proList_package(list,key);
				
				proList.add(keyValue);
				proList.add(list);
				propattr.add(proList);
			}
		}
		propMap.put("attr", propattr);
		retBody.setProperties(propMap);
		
		retBody.setProperties(propMap);
	}
	
	private static void proList_package(List<List<String>> list, Element key) {
		if(null!=key){
			List<String> tempList=new ArrayList<String>();
			String prokey = StringUtils.trim(StringUtils.substringBefore(key.text(), "："));
			String value = StringUtils.trim(StringUtils.substringAfter(key.text(), "："));
			
			if (StringUtils.isNotBlank(prokey) && StringUtils.isNotBlank(value)) {
				tempList.add(prokey);
				tempList.add(value);
			}else if(StringUtils.isNotBlank(prokey) && StringUtils.isBlank(value)){
				tempList.add(prokey);
			}else if(StringUtils.isBlank(prokey) && StringUtils.isNotBlank(value)){
				tempList.add(value);
			}
			
			if(StringUtils.isBlank(prokey) && StringUtils.isBlank(value)){
				return;
			}
			list.add(tempList);
			
			Element nextElementSibling = key.nextElementSibling();//同级兄弟节点
			proList_package(list,nextElementSibling);
		}
	}

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN2)) {
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN2)) {
			gender = "men";
		}
		return gender;
	}


}
