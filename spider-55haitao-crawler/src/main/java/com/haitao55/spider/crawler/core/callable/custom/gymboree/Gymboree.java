package com.haitao55.spider.crawler.core.callable.custom.gymboree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * gymboree 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月6日 上午11:42:31
 * @version 1.0
 */
public class Gymboree extends AbstractSelect {
	private static final String DOMAIN = "www.gymboree.com";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		if(url.contains("shops-outfits")){
			return;//这个商品类目下的商品暂时都不支持
		}
		String content = this.getInputString(context);
		if(content.contains("We're sorry, no products were found for your search")){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+"item couldn't find any results matching..");
		}
		Document doc = this.getDocument(context);
		String product_data = StringUtils.substringBetween(content, "pageVarsPdict = ", "};");
		if (StringUtils.isNotBlank(product_data)) {
			product_data = product_data + "}";
		}else{
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+"item couldn't find any results matching..");
		}
		JSONObject productDataJSONObject = JSONObject.parseObject(product_data);
		JSONObject pageData = productDataJSONObject.getJSONObject("pageData");
		String breadCrumbs[] = pageData.getString("breadCrumbs").split(" > ");
		for(String breadCrumb : breadCrumbs){
			if(breadCrumb.contains("Outfit Shop")){
				return;//这个商品类目下的商品暂时都不支持
			}
		}
		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.substringBetween(content, "productId = \"", "\";");
		if (StringUtils.isBlank(productId)) {
			return;
		}
		// spu stock status
		int spu_stock_status = 0;
		// unit
		String unit = StringUtils.EMPTY;
		Elements unitElements = doc.select("div.product-price meta[itemprop=priceCurrency]");
		if (CollectionUtils.isNotEmpty(unitElements)) {
			unit = unitElements.attr("content");
		}

		// sku jsonarray
		JSONArray skuJSONArray = new JSONArray();
		List<String> sizeOptions = new ArrayList<String>();
		Elements imgEs = doc.select("ul.swatches.size li.selectable a");
	    for(Element  e : imgEs){
	    	sizeOptions.add(e.text());
	    }


		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		try {
			sku_jsonarray_package(content, skuJSONArray, sizeOptions,retBody,context,unit,l_selection_list,l_style_list);
		} catch (ClassCastException e) {
			return;
		}
		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		if(l_selection_list.size() > 0){
			spu_stock_status = 1;
		}

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(spu_stock_status));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId.concat(DOMAIN));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		// category
		category_package(context,pageData, retBody);

		// description
		desc_package(doc, retBody);

		setOutput(context, retBody);
	}

	/**
	 * 封装skujsonarray
	 * 
	 * @param content
	 * @param skuJSONArray
	 * @param titleJSONObject
	 * @param colorCodeList
	 */
	private static void sku_jsonarray_package(String content, JSONArray skuJSONArray,
			List<String> sizeOptions,RetBody retBody,Context context,String unit,List<LSelectionList> l_selection_list,List<LStyleList> l_style_list) throws ClassCastException {
		String product_data = StringUtils.substringBetween(content, "analyticsJSON =", "};");
		if (StringUtils.isNotBlank(product_data)) {
			product_data = product_data + "}";
		}else{
			throw new ParseException(CrawlerExceptionCode.CRAWLING_ERROR,"itemUrl:"+context.getUrl().toString()+"item Json data not found..");
		}
		JSONObject productDataJSONObject = JSONObject.parseObject(product_data);
		if (MapUtils.isNotEmpty(productDataJSONObject)) {
			JSONObject productJSONObject = productDataJSONObject.getJSONArray("PRODUCT").getJSONObject(0);
			if (MapUtils.isNotEmpty(productJSONObject)) {
				// title
				String title = productJSONObject.getString("productName");				
				retBody.setTitle(new Title(title, "", "", ""));
						// color name
						String colorName = productJSONObject.getString("variant");
						// swicth image
						String switchImage = productJSONObject.getString("productThumbnail");
						//image
						String image = productJSONObject.getString("productImage");
						// salePrice
						float salePrice = productJSONObject.getFloatValue("price");
						// orignPrice
						float orignPrice = productJSONObject.getFloatValue("regPrice");
						int save = (int) ((1 - (salePrice / orignPrice)) * 100);
						retBody.setPrice(new Price(orignPrice, save, salePrice, unit));
						String skuId = productJSONObject.getString("productID");
						String default_SkuId = null;
						// sizes
						if (CollectionUtils.isNotEmpty(sizeOptions)) {
							for (String size : sizeOptions) {
								LSelectionList lselectlist = new LSelectionList();
								List<Selection> selections = new ArrayList<Selection>();
								lselectlist.setGoods_id(size+colorName);
								lselectlist.setOrig_price(orignPrice);
								lselectlist.setSale_price(salePrice);
								lselectlist.setPrice_unit(unit);
								lselectlist.setStock_status(1);
								lselectlist.setStock_number(0);
								lselectlist.setStyle_id(colorName);
								lselectlist.setSelections(selections);
								l_selection_list.add(lselectlist);
								Selection selection = new Selection();
								selection.setSelect_id(0);
								selection.setSelect_name("Size");
								selection.setSelect_value(size);
								selections.add(selection);		
								if(default_SkuId == null){
									default_SkuId = size+colorName;
								}
							}
						}
						LStyleList lStyleList = new LStyleList();
						// images
						List<Image> images = new ArrayList<>();
						images.add(new Image(image));
						lStyleList.setDisplay(true);			
						context.getUrl().getImages().put(default_SkuId, images);

						// stylelist
						lStyleList.setGood_id(default_SkuId);
						lStyleList.setStyle_switch_img(switchImage);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(colorName);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(colorName);
						l_style_list.add(lStyleList);
			}

		}
	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param doc
	 * @param brand
	 * @param title
	 * @param retBody
	 */
	private static void category_package(Context context,JSONObject pageData, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String category = pageData.getString("category");
		String brand = pageData.getString("brand");
		String breadCrumbs[] = pageData.getString("breadCrumbs").split(" > ");
		for(String breadCrumb : breadCrumbs){
			cats.add(breadCrumb);
			breads.add(breadCrumb);
		}

		if (StringUtils.isNotBlank(category)) {
			cats.add(category);
		}				
		// BreadCrumb
		if (StringUtils.isNotBlank(brand)) {
			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			breads.add(brand);
		}
		if(CollectionUtils.isEmpty(cats)){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"item category/BreadCrumb data not found..");
		}
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
		
		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = pageData.getString("breadCrumbs");
		if(StringUtils.isNotBlank(gender)){
			if(gender.toLowerCase().contains("girl")){
				propMap.put("s_gender", "girl");
			}else if(gender.toLowerCase().contains("boy")){
				propMap.put("s_gender", "boy");
			}else{
				propMap.put("s_gender", "");
			}
		}else{
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,"itemUrl:"+context.getUrl().toString()+"item category/BreadCrumb data not found..");
		}		
		retBody.setProperties(propMap);
	}

	/***
	 * 描述 封装
	 * 
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("ul[itemprop=description] li");
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
		String desc = doc.select("p.tab-content").text();
		if(StringUtils.isNotBlank(desc)){
			descMap.put("en", desc);
		}else{
			descMap.put("en", sb.toString());
		}
		retBody.setFeatureList(featureMap);
		retBody.setDescription(descMap);
	}
}
