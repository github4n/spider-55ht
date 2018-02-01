package com.haitao55.spider.crawler.core.callable.custom.famousfootwear;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * famousfootwear 详情页数据封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月6日 下午2:14:06
* @version 1.0
 */
public class FamousFootwear extends AbstractSelect{
	private static final String color_selected = "selected";
	private static final String domain = "www.famousfootwear.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_WOMEN2 = "girl";
	private static final String SEX_MEN = "men";
	private static final String SEX_MEN2 = "boy";
	
	@Override
	public void invoke(Context context) throws Exception {
		
//		String content = this.getInputString(context);
		Document doc = this.getDocument(context);
		
		Pattern pattern = Pattern.compile("currently out of stock");
		Matcher matcher = pattern.matcher(doc.toString());
		if(matcher.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"famousfootwear.com itemUrl:"+context.getUrl().toString()+" is offline..");
		}
		
		String url = context.getCurrentUrl().toString();
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		String default_color = StringUtils.EMPTY;
		
		int spu_stock_status = 0;
		boolean spu_price_flag = false;
		//brand
		String brand = StringUtils.EMPTY;
		Elements elements = doc.select("span.PD_Brand");
		if(CollectionUtils.isNotEmpty(elements)){
			brand = StringUtils.trim(elements.get(0).text());
		}
		
		//title
		String title = StringUtils.EMPTY;
		elements = doc.select("span.PD_Style");
		if(CollectionUtils.isNotEmpty(elements)){
			title = elements.get(0).text();
		}
		
		//color
		List<String> params = new ArrayList<String>();
		elements = doc.select("select#ctl00_cphPageMain_ProductSelection2_ddlColor option");
		if(CollectionUtils.isNotEmpty(elements)){
			for (Element element : elements) {
				String color = element.text();
				String colorId = element.attr("value");
				String selected = element.attr("selected");
				if(StringUtils.equals(color_selected, selected)){
					default_color = color;
				}
				
				params.add(colorId);
				
			}
		}
		
		//sku JSONArray
		JSONArray skuJsonArray = new JSONArray();
		skuJsonArray = new FamousFootwearHandler().process(params, context.getUrl());
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		//sku iteator
		if(null != skuJsonArray && skuJsonArray.size() > 0){
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObejct = (JSONObject)object;
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				
				//stock
				int stock_number=0;
				int stock_status = skuJsonObejct.getIntValue("stock_status");
				
				//spu stock status
				if(stock_status>0){
					spu_stock_status=1;
				}
				
				//style id
				String style_id = skuJsonObejct.getString("color");
				
				//price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orig_price = skuJsonObejct.getFloatValue("orign_price");
				String unit = skuJsonObejct.getString("unit");// 得到货币代码
				int save=skuJsonObejct.getIntValue("save");

				if(orig_price < sale_price){
					orig_price = sale_price;
				}
				
				//spu price
				if(!spu_price_flag){
					if(default_color.equals(style_id)){
						retBody.setPrice(
								new Price(orig_price, save, sale_price, unit));
					}
					spu_price_flag=true;
				}
				
				//skuId 
				String skuId = StringUtils.EMPTY;
				
				//selections
				String size = skuJsonObejct.getString("size");
				List<Selection> selections = new ArrayList<Selection>();
				if(StringUtils.isNotBlank(size)){
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(size);
					selections.add(selection);
					
					skuId = style_id.concat(size);
				}
				
				else{
					skuId = style_id;
				}
				
				//lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orig_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(style_id);
				lselectlist.setSelections(selections);
				
				//l_selection_list
				l_selection_list.add(lselectlist);
				
				
				skuJsonObejct.put("skuId", skuId);
				//style json
				styleJsonObject.put(style_id, skuJsonObejct);
				
			}
			
			//style iterator
			if(null != styleJsonObject  && styleJsonObject.size()>0){
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();
					JSONObject jsonObject = (JSONObject)entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					//skuId
					String skuId = jsonObject.getString("skuId");
					String switch_img=jsonObject.getString("switch");
					if(default_color.equals(style_id)){
						lStyleList.setDisplay(true);
					}
					
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);
					
					//images
					@SuppressWarnings("unchecked")
					List<Image> pics = (List<Image>) jsonObject.get("images");
					context.getUrl().getImages().put(skuId, pics);
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
			
			//title
			retBody.setTitle(new Title(title, "", "", ""));
			
			//brand
			retBody.setBrand(new Brand(brand, "", "", ""));

			
			// full doc info
			String docid = SpiderStringUtil.md5Encode(url);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(domain));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			//category breadcrumb
			category_package(brand ,retBody , doc);
			
			// description
		    desc_package(doc,retBody);
		    
		    //properties
			properties_package(retBody);
			
		}
		setOutput(context, retBody);
	}
	/**
	 * category breadcrumbs  封装
	 * @param category
	 * @param brand
	 * @param retBody 
	 * @param doc 
	 */
	private static void category_package(String brand, RetBody retBody, Document doc) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("div#prodBreadCrumb li:not(:first-child)");
//		System.out.println(doc);
		for (Element element : cateElements) {
			String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
			if (StringUtils.isNotBlank(cat)) {
				cats.add(cat);
				breads.add(cat);
			}
		}
		//cats
		retBody.setCategory(cats);
		
		// BreadCrumb
		breads.add(brand);
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
		Elements es = doc.select("div#ProductDescription span p , div#ProductDescription span ul li");
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
