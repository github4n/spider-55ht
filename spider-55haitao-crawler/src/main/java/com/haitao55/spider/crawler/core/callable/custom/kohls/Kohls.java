package com.haitao55.spider.crawler.core.callable.custom.kohls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * kohl's　详情页数据封装
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年12月30日 下午2:45:43
* @version 1.0
 */
public class Kohls extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String swatchImage = "http://media.kohlsimg.com/is/image/kohls/{}_sw?wid=30&hei=30";
	private static final String largeImage = "http://media.kohlsimg.com/is/image/kohls/{}?wid=1000&hei=1000&op_sharpen=1";
	private static final String PLACEHOLDER = "\\{\\}";
	private static final String domain = "www.kohls.com";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl().toString();
		String request_url = StringUtils.replacePattern(url, " ", "%20");
		String content = StringUtils.EMPTY;
		
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(request_url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(request_url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		//item 相关数据json
		String item_data = StringUtils.substringBetween(content, "var productJsonData = ", ";	</script>");
		
		if(StringUtils.isBlank(item_data)){
			logger.info("kohls.com itemUrl: {} , offline",url);
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"kohls.com itemUrl:" + context.getUrl().toString() + " not found..");
		}
		
		RetBody retBody = new RetBody();
		Sku sku = new Sku();
		
		//相关数据解析
		JSONObject itemJson = JSONObject.parseObject(item_data);
		
		//productItemJson  productid   image　等获取
		JSONObject productItemJson = itemJson.getJSONObject("productItem");
		
		//productDetailsJson productid stock title
		JSONObject productDetailsJson = productItemJson.getJSONObject("productDetails");
		
		//productId
		String productId = productDetailsJson.getString("productId");
		
		//title
		String title = productDetailsJson.getString("displayName");

		//brand
		String brand = StringUtils.substringBetween(content, "$env.brand = '\"", "\"';");
		
		//gender
		String gender = StringUtils.substringBetween(content, "$env.gender = '\"", "\"';");
		
		//category
		String category = StringUtils.substringBetween(content, "$env.category = '", "';");
		
		//stock
		int spu_stock_status = 0;
//		boolean stock = productDetailsJson.getBooleanValue("isInStoreSkuAvailable");
//		if(!stock){
//			spu_stock_status = 0;
//		}
		
		//spu image
		List<Image> pics = new ArrayList<Image>();
		JSONObject imageJsonObject = productItemJson.getJSONObject("media");
		//第一张图片
		JSONObject firstImageJson = imageJsonObject.getJSONObject("defaultImage");
		String first_image = firstImageJson.getString("largeImage");
		pics.add(new Image(first_image));
		//副图
		JSONArray imageJsonArray = imageJsonObject.getJSONArray("alternateImages");
		if(null!=imageJsonArray && imageJsonArray.size()>0){
			for (Object object : imageJsonArray) {
				JSONObject imageJson = (JSONObject)object;
				String image_url = imageJson.getString("largeImage");
				pics.add(new Image(image_url));
			}
		}
		
		//desc
		JSONObject descElements = productItemJson.getJSONObject("accordions");
		String desc = descElements.getJSONObject("productDetails").getString("content");
		
		//default color
		JSONObject defaultColorJson = productItemJson.getJSONObject("variants");
		String default_color = defaultColorJson.getString("preSelectedColor");
		
		
		JSONArray skuJsonArray = productItemJson.getJSONArray("skuDetails");
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		//style 
		JSONObject styleJsonObject = new JSONObject();
		
		boolean is_set_spu_price = false;
		
		//　iteartor skuarray
		if(null!=skuJsonArray && skuJsonArray.size()>0){
//			System.out.println(skuJsonArray.toString());
			for (Object object : skuJsonArray) {
				//trans JSONObject
				JSONObject skuJsonObejct=(JSONObject)object;
				
				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				//skuId
				String skuId = skuJsonObejct.getString("skuId");
				//stock
				int stock_status = 0;
				int stock_number=0;
				boolean is_no_stock = skuJsonObejct.getBooleanValue("inventoryStatus");
				if(is_no_stock){
					stock_status=1;
					spu_stock_status =1;
				}
				
				String color=skuJsonObejct.getString("color");
				String size=skuJsonObejct.getString("size2");
				
				//price
				String origPrice = skuJsonObejct.getString("regularPrice");
				String salePrice = skuJsonObejct.getString("salePrice");
				if(StringUtils.isBlank(salePrice)){
					salePrice = origPrice;
				}
				String unit = getCurrencyValue(salePrice);// 得到货币代码
				String save=StringUtils.EMPTY;
				salePrice = salePrice.replaceAll("[$,]", "");
				origPrice = origPrice.replaceAll("[$,]", "");

				if (StringUtils.isBlank(origPrice)) {
					origPrice = salePrice;
				}
				if (StringUtils.isBlank(salePrice)) {
					salePrice = origPrice;
				}
				if (StringUtils.isBlank(origPrice)
						|| Float.valueOf(origPrice) < Float.valueOf(salePrice)) {
					origPrice = salePrice;
				}
				if (StringUtils.isBlank(save)) {
					save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(origPrice)) * 100)
							+ "";// discount
				}
				
				//spu price
				if(StringUtils.isNotBlank(default_color)){
					if(StringUtils.equals(default_color, color)){
						retBody.setPrice(
								new Price(Float.valueOf(origPrice), Integer.parseInt(save), Float.valueOf(salePrice), unit));
					}
				}else{
					if(!is_set_spu_price){
						retBody.setPrice(
								new Price(Float.valueOf(origPrice), Integer.parseInt(save), Float.valueOf(salePrice), unit));
						is_set_spu_price = true;
					}
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
			
			if(null != styleJsonObject  && styleJsonObject.size()>0){
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String style_id = entry.getKey();
					
					JSONObject jsonObject = (JSONObject)entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					//skuId
					String skuId = jsonObject.getString("skuId");
					String switch_img=StringUtils.EMPTY;
					if(StringUtils.isNotBlank(default_color)){
						if(StringUtils.equals(default_color, style_id)){
							lStyleList.setDisplay(true);
						}
					}else{
						lStyleList.setDisplay(true);
					}
					
					StringBuffer buffer = new StringBuffer();
					buffer.append(productId).append("_").append(style_id);
					String concat_str = buffer.toString();
					concat_str = StringUtils.replacePattern(StringUtils.trim(concat_str), " ", "_");
					
					//switch_img
					switch_img = StringUtils.replacePattern(swatchImage, PLACEHOLDER, concat_str);
					
					
					//images
					List<Image> sku_pics = new ArrayList<Image>();
					if(StringUtils.isNotBlank(default_color) && StringUtils.equals(default_color, style_id)){
						sku_pics = pics;
					}else if(StringUtils.isNotBlank(default_color) && !StringUtils.equals(default_color, style_id)){
						String image_url = StringUtils.replacePattern(largeImage, PLACEHOLDER, concat_str);
						sku_pics.add(new Image(image_url));
					}
					
					//防止只有size
					else if(StringUtils.isBlank(default_color)){
						sku_pics = pics;
						switch_img = StringUtils.EMPTY;
					}
					
					context.getUrl().getImages().put(skuId, sku_pics);
					
					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(style_id);
					
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
		category_package(category,brand ,retBody);
		
		// description
	    desc_package(desc,retBody);
	    
	    //properties
		properties_package(retBody,gender);
		
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
		String[] category_data = StringUtils.split(category, ">");
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
	private static void desc_package(String desc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		//desc trans doc
		Document doc = JsoupUtils.parse(desc);
		Elements es = doc.select("p");
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
				Element nextElementSibling = e.nextElementSibling();
				if(null!=nextElementSibling){
					Elements select = nextElementSibling.select("li");
					for (Element liElement : select) {
						String liText = liElement.text();
						if(StringUtils.isNotBlank(liText)){
							featureMap.put("feature-" + count, liText);
							count++;
							sb.append(liText);
						}
					}
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
		gender = getSex(gender);
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

}
