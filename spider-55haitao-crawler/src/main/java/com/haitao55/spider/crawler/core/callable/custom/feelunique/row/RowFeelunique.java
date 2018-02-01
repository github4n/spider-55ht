package com.haitao55.spider.crawler.core.callable.custom.feelunique.row;

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

public class RowFeelunique extends AbstractSelect {
	private static final String stockFlag = "In Stock";
	private static final String domain = "row.feelunique.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(context.getCurrentUrl().toString()).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		Document doc = JsoupUtils.parse(content);
		
		Pattern p = Pattern.compile("Sorry, this product is no longer available");
		 Matcher m = p.matcher(content);
		 if (m.find()) {
		  throw new ParseException(CrawlerExceptionCode.OFFLINE,"row.feelunique.com itemUrl:" + context.getUrl().toString() + " not　found..");
		 }
		
		boolean defaultSkuFlag = false;
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		String default_skuId = StringUtils.EMPTY;
		String productData = StringUtils.substringBetween(content, "var google_custom_params = {", "};");
		// brand
		String brand = StringUtils.substringBetween(productData, "\"pbrand\":\"", "\"");
		retBody.setBrand(new Brand(brand, "", "", ""));

		// productid
		String productId = StringUtils.substringBetween(productData, "\"prodid\":", ",\"");

		Elements elements = doc.select("div.sub-products div");

		List<JSONObject> skuData = new ArrayList<JSONObject>();

		// img
		Map<String, List<Image>> imageMap = new HashMap<String, List<Image>>();

		Map<String, String> switch_image_map = new HashMap<String, String>();

		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				skuDataPackage(doc,element, skuData, imageMap, switch_image_map);
			}
		}

		// price
		String salePrice = StringUtils.EMPTY;
		String origPrice = StringUtils.EMPTY;
		String save = StringUtils.EMPTY;
		String unit = StringUtils.EMPTY;
		Elements priceElements = doc.select("span.Price");
		if (CollectionUtils.isEmpty(priceElements)) {
			return;
		}
		salePrice = priceElements.text();
		priceElements = doc.select("span.Price-details span.rrp");
		origPrice = null == priceElements ? StringUtils.EMPTY : priceElements.text();

		unit = getCurrencyValue(salePrice);// 得到货币代码
		if(!StringUtils.equals(unit, "GBP")){
            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" rowFeelunique-price-unit-is-not-GBP...");
        }
		
		salePrice = salePrice.replaceAll("[$,£ ,RRP,From]", "");

		if (StringUtils.isBlank(replace(origPrice))) {
			origPrice = salePrice;
		}
		origPrice = origPrice.replaceAll("[$,£ ,RRP]", "");
		if (StringUtils.isBlank(replace(salePrice))) {
			salePrice = origPrice;
		}
		if (StringUtils.isBlank(origPrice) || Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
			origPrice = salePrice;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100) + "";// discount
		}

		//images
		List<Image> pics = new ArrayList<Image>();

		// image
		Elements selectedImages = doc.select("div.thumbnails ul li a");
		if (CollectionUtils.isNotEmpty(selectedImages)) {
			for (Element element : selectedImages) {
				String imageUrl = element.attr("href");
				if (!StringUtils.containsIgnoreCase(imageUrl, "http:")) {
					imageUrl = "http:".concat(imageUrl);
				}
				pics.add(new Image(imageUrl));
			}
		} else {
			Elements mianImageElements = doc.select("div.product-detail-preview img");
			String imageUrl = StringUtils.EMPTY;
			if (CollectionUtils.isNotEmpty(mianImageElements)) {
				imageUrl = mianImageElements.attr("src");
				if (!StringUtils.containsIgnoreCase(imageUrl, "http:")) {
					imageUrl = "http:".concat(imageUrl);
				}
			}
			pics.add(new Image(imageUrl));
		}
		
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// stock number map
		Map<String, Integer> stockMap = new HashMap<String, Integer>();

		// Style
		Map<String, JSONObject> styleMap = new HashMap<String, JSONObject>();

		if (null != skuData && skuData.size() > 0) {

			// selection list
			for (JSONObject skuJsonObject : skuData) {
				LSelectionList lselectlist = new LSelectionList();
				String skuId = skuJsonObject.getString("skuId");
				String style_id = skuJsonObject.getString("color");
				int stock_status = skuJsonObject.getIntValue("stock_status");
				int stock_number = skuJsonObject.getIntValue("stock_number");

				float sku_origPrice = skuJsonObject.getFloatValue("origPrice");
				float sku_salePrice = skuJsonObject.getFloatValue("salePrice");
				String sku_unit = skuJsonObject.getString("unit");
				int sku_save = skuJsonObject.getIntValue("save");
				
				lselectlist.setGoods_id(skuId);
				lselectlist.setStyle_id(style_id);
				lselectlist.setStock_status(stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setOrig_price(sku_origPrice);
				lselectlist.setPrice_unit(sku_unit);
				lselectlist.setSale_price(sku_salePrice);

				// selections
				List<Selection> selections = new ArrayList<Selection>();
				lselectlist.setSelections(selections);

				l_selection_list.add(lselectlist);

				// default skuId 网站特殊性， 取第一个sku为默认sku
				if (!defaultSkuFlag) {
					default_skuId = skuId;
					defaultSkuFlag = true;
				}

				// stock
				if (null == stockMap.get(skuId)) {
					stockMap.put(skuId, stock_number + stock_status);
				} else {
					stockMap.put(skuId, stock_number + stock_status + stockMap.get(skuId));
				}

				// spu price
				if (StringUtils.equalsIgnoreCase(default_skuId, skuId)) {
					retBody.setPrice(new Price(Float.valueOf(sku_origPrice), sku_save, Float.valueOf(sku_salePrice), sku_unit));
				}
				
				styleMap.put(skuId, skuJsonObject);
			}

			Elements cate_name_elements = doc.select("span.product-options-label");
			String cate_name = StringUtils.EMPTY;
			if (CollectionUtils.isNotEmpty(cate_name_elements)) {
				cate_name = cate_name_elements.text();
			}
			cate_name = cate_name.replaceAll("[: ]", "");
			if (StringUtils.isBlank(cate_name)) {
				cate_name = "Colour";
			}
			// style list
			if (null != styleMap && styleMap.size() > 0) {
				for (Map.Entry<String, JSONObject> entry : styleMap.entrySet()) {
					LStyleList lStyleList = new LStyleList();
					String skuId = entry.getKey();
					JSONObject skuJson = entry.getValue();
					String style_cate_name = "Colour";
					String color = skuJson.getString("color");
					String switch_img = StringUtils.EMPTY;
					if (null != switch_image_map.get(skuId)) {
						switch_img = switch_image_map.get(skuId);
					}
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(color);
					lStyleList.setStyle_cate_name(style_cate_name);
					lStyleList.setStyle_name(color);

					boolean display = false;
					if (StringUtils.equalsIgnoreCase(default_skuId, skuId)) {
						display = true;
					}
					lStyleList.setDisplay(display);

					// images
					List<Image> list = imageMap.get(skuId);
					if (null == list || list.size() == 0) {
						list = pics;
					}
					context.getUrl().getImages().put(skuId, list);

					l_style_list.add(lStyleList);
				}
			}

		}

		// 单ｓｋｕ
		else {
			// images context
			context.getUrl().getImages().put(productId, pics);
			retBody.setPrice(new Price(Float.parseFloat(origPrice),
					StringUtils.isBlank(save) ? 0 : Integer.parseInt(save), Float.parseFloat(salePrice), unit));
		}


		// stock
		int stock_status = 0;
		if (null != stockMap && stockMap.size() > 0) {
			Integer number = stockMap.get(default_skuId);
			if (null == number) {
				stock_status = 1;
			} else if (number > 0) {
				stock_status = 1;
			}
		} else {
			Elements stockElements = doc.select("span.stock-level");
			if (CollectionUtils.isNotEmpty(stockElements)) {
				String stockDesc = stockElements.text();
				if (StringUtils.equalsIgnoreCase(stockFlag, stockDesc)) {
					stock_status = 1;
				}
			}

		}

		// stock
		retBody.setStock(new Stock(stock_status));
		context.put("Stock", new Stock(stock_status));

		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		//sku
		retBody.setSku(sku);
		
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("div.item h1");
		if(CollectionUtils.isNotEmpty(titleElements)){
			title = titleElements.get(0).text();
		}
		//title
		retBody.setTitle(new Title(StringEscapeUtils.unescapeHtml(title), "", "", ""));
		
		// full doc info
		String url = context.getCurrentUrl().toString();
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		
		//category breadcrumb
		category_package(doc,retBody,brand);
		
		//brand 
		retBody.setBrand(new Brand(brand, "", "", ""));
		
		// description
		desc_package(doc,retBody);
		
		
		//properties
		properties_package(retBody);
		
		setOutput(context, retBody);
	}

	/**
	 * sku 相关数据封装
	 * 
	 * @param element
	 * @param doc
	 * @param skuData
	 * @param imageMap
	 * @param switch_image_map
	 */
	private static void skuDataPackage(Document doc , Element element, List<JSONObject> skuData, Map<String, List<Image>> imageMap,
			Map<String, String> switch_image_map) {
		JSONObject sku = new JSONObject();

		List<Image> pics = new ArrayList<Image>();

		// get sku data
		Elements skuElements = element.select("label");
		String switch_img = StringUtils.EMPTY;
		String skuId = StringUtils.EMPTY;
		String goodsId = StringUtils.EMPTY;
		String color = StringUtils.EMPTY;
		int stock_status = 0;
		int stock_number = 0;
		if (CollectionUtils.isNotEmpty(skuElements)) {
			Element skuElement = skuElements.get(0);
			color = skuElement.attr("data-option");
			goodsId = skuElement.attr("data-sub-sku");
			skuId = skuElement.attr("data-sub-sku");

			switch_img = skuElement.attr("style");
			switch_img = StringUtils.substringBetween(switch_img, "('//", "')");
			if (!StringUtils.containsIgnoreCase(switch_img, "http://")) {
				switch_img = "http://".concat(switch_img);
			}
			if (!StringUtils.containsIgnoreCase(switch_img, ".jpg")) {
				switch_img = StringUtils.EMPTY;
			}
			switch_image_map.put(skuId, switch_img);

			String stock = skuElement.attr("data-stock");
			stock_status = stockStatus(stock);

			sku.put("skuId", skuId);
			sku.put("goodsId", goodsId);
			sku.put("color", color);
			sku.put("stock_status", stock_status);
			sku.put("stock_number", stock_number);

			skuData.add(sku);

			String imageContent = skuElement.attr("data-image-main");

			if (StringUtils.isNotBlank(imageContent) && !StringUtils.containsIgnoreCase(imageContent, "http:")) {
				imageContent = "http:".concat(imageContent);
			}
			if (StringUtils.isNotBlank(imageContent)) {
				pics.add(new Image(imageContent));
			}

			imageMap.put(skuId, pics);
			
			// price
			String salePrice = StringUtils.EMPTY;
			String origPrice = StringUtils.EMPTY;
			String save = StringUtils.EMPTY;
			String unit = StringUtils.EMPTY;
			
			salePrice = skuElement.attr("data-subprice");
			if(StringUtils.isBlank(salePrice)){
				salePrice = doc.select("span.Price").text();
			}
			Elements priceElements = doc.select("span.Price-details span.rrp");
			origPrice = null == priceElements ? StringUtils.EMPTY : priceElements.text();

			unit = getCurrencyValue(salePrice);// 得到货币代码
			salePrice = salePrice.replaceAll("[$,£ ,RRP,From]", "");

			if (StringUtils.isBlank(replace(origPrice))) {
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$,£ ,RRP]", "");
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

			sku.put("salePrice", Float.valueOf(salePrice));
			sku.put("origPrice", Float.valueOf(origPrice));
			sku.put("unit", unit);
			sku.put("save", StringUtils.isBlank(save) ? 0 : Integer.parseInt(save));
		}
	}

	/**
	 * 校验ｓｋｕ 库存状态
	 * 
	 * @param stock
	 * @return
	 */
	private static int stockStatus(String stock) {
		int status = 0;
		if (StringUtils.isEmpty(stock)) {
			return status;
		}

		switch (stock) {
		case "In stock":
			status = 1;
			break;

		default:
			status = 0;
			break;
		}
		return status;
	}

	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		if (null == dest) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

	}

	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		val = val.replaceAll("[From, ]", "");
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		unit = Currency.codeOf(currency).name();
		return unit;

	}

	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("Cookie", " D_SID=192.243.119.27:DxlfonMJJUoIesv2gfRD8BzxkWLfN6bjovSPqqhBwNA; D_UID=5076FC3F-90DD-3ED5-9DF1-0C8CA0875C35; D_HID=pBI8DGhuNhFWTj/Ag/GCU4Lwa/VNJQz1q79onPdhBCs;feeluniqueCurr=GBP;");
		return headers;
	}
	
	/**
	 *　分类　封装
	 * @param doc
	 * @param retBody
	 * @param brand 
	 */
	private static void category_package(Document doc, RetBody retBody, String brand) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements cateElements = doc.select("div#breadcrumb > ul li:not(:first-child)");
		if (CollectionUtils.isNotEmpty(cateElements)) {
			for (Element element : cateElements) {
				String cat =StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		retBody.setCategory(cats);
		
		// BreadCrumb
		breads.add(brand);
		retBody.setBreadCrumb(breads);
	}
	
	/***
	 * 描述　　封装
	 * @param doc
	 * @param retBody
	 */
	private void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		Elements es = doc.select("div#product-description-tab div.tab-content");
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
	private void properties_package(RetBody retBody) {
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
}
