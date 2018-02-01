package com.haitao55.spider.crawler.core.callable.custom.c21stores;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.utils.Currency;

public class C21storesUtils {
	private static String priceSplit="–";
	private static String priceSplit2="-";
	/**
	 * 封装color 对应图片
	 * @param colorPrice
	 * @param imageElements
	 * @param color
	 */
	public static void colorImagesPackage(Map<String, List<String>> colorImages, Elements imageElements, String color) {
		List<String> list=new ArrayList<String>();
		for (Element element : imageElements) {
			String imageUrl=element.attr("src");
			list.add(imageUrl);
		}
		colorImages.put(color.replaceAll("[+]", " "), list);
	}
	
	/**
	 * 封装color 对应价格
	 * @param colorPrice
	 * @param imageElements
	 */
	
	public static void colorPricePackage(Map<String, Map<String,Object>> colorPrice,Document doc,String color) {
		Map<String,Object> priceMap=new HashMap<String,Object>();
		boolean priceboo=false;
		String salePrice=StringUtils.EMPTY;
		String orignPrice=StringUtils.EMPTY;
		String save=StringUtils.EMPTY;
		Elements salePriceElement = doc.select("p[class=product-details__price product-details__price--sell]");
		Elements orignPriceElement = doc.select("p[class=product-details__price product-details__price--msrp]");
		Elements saveElement = doc.select("p[class=product-details__price product-details__price--percent-saved]");
		if(CollectionUtils.isNotEmpty(salePriceElement)){
			salePrice=salePriceElement.get(0).text();
		}
		if(CollectionUtils.isNotEmpty(orignPriceElement)){
			orignPrice=orignPriceElement.get(0).text();
		}
		if(CollectionUtils.isNotEmpty(saveElement)){
			save=saveElement.get(0).text();
		}
		if(StringUtils.isBlank(orignPrice)){
			orignPrice=salePrice;
		}
		String currency=salePrice.substring(0, 1);
		
		salePrice = salePrice.replaceAll("[$,]", "");
		orignPrice = orignPrice.replaceAll("[$,]", "");
		//price a-b
		if(StringUtils.contains(salePrice, priceSplit)){// saleprice  a–b   choise b
			salePrice=salePrice.split(priceSplit)[1].trim();
			priceboo=true;
		}
		if(StringUtils.contains(orignPrice, priceSplit)){// orignPrice  a–b   choise a
			orignPrice=orignPrice.split(priceSplit)[0].trim();
			priceboo=true;
		}
		
		if(StringUtils.contains(salePrice, priceSplit2)){// saleprice  a-b   choise b
			salePrice=salePrice.split(priceSplit2)[1].trim();
			priceboo=true;
		}
		if(StringUtils.contains(orignPrice, priceSplit2)){// orignPrice  a-b   choise a
			orignPrice=orignPrice.split(priceSplit2)[0].trim();
			priceboo=true;
		}
		Float orign_price=Float.parseFloat(orignPrice);
		Float sale_price=Float.parseFloat(salePrice);
		
		if(priceboo){
			save =String.valueOf(Math.round((1 - sale_price / orign_price) * 100));// discount
		}
		
		if(orign_price<sale_price){
			orign_price=sale_price;
		}
		String price_unit = Currency.codeOf(currency).name();
		save=save.replaceAll("[Save,%]", "").trim();
		priceMap.put("sale_price", sale_price);
		priceMap.put("orign_price", orign_price);
		priceMap.put("save", StringUtils.isBlank(save)?0:Integer.parseInt(save));
		priceMap.put("price_unit",price_unit);
		colorPrice.put(color.replaceAll("[+]", " "), priceMap);
	}
	
	/**
	 * 封装sku
	 * @param element
	 * @param skuList
	 * @param color
	 */
	public static void skuPackage(Element element, List<Map<String, Object>> skuList,String color) {
		Map<String, Object> skuMap=new HashMap<String,Object>();
		String size=StringUtils.EMPTY;
		String sku_id = StringUtils.EMPTY;
		int sku_status=1;
		int stock__number=0;
		//size
		Elements select = element.select("p>input");
		if(CollectionUtils.isNotEmpty(select)){
			sku_id = select.attr("value");
			String attr = select.attr("data-product-details-size-button");
			
			if(StringUtils.isNotBlank(attr)){
				size=StringUtils.substringBetween(attr, ":\"", "\"}");
			}
		}
		//stock
		Elements stockSelect = element.select("div p");
		if(CollectionUtils.isNotEmpty(stockSelect)){
			String text = stockSelect.get(0).text();
			Pattern p = Pattern.compile("Only (.*) Left");
			Matcher m = p.matcher(text);
			if(m.find()){
				String number = m.group(1);
				stock__number=Integer.parseInt(number);
			}
		}
		skuMap.put("skuId",sku_id);
		skuMap.put("color",color.replaceAll("[+]", " "));
		skuMap.put("size",size);
		if(stock__number!=0){
			sku_status=2;
		}
		skuMap.put("stock_status",sku_status);
		skuMap.put("stock_number",stock__number);
		skuList.add(skuMap);
		
	}
	
	/**
	 * 封装对应color  对应 skuId, 与color-size 具体sku不同,代表color特有sku值
	 * @param colorIdSkuId
	 * @param doc
	 * @param color
	 */
	public static void colorIdSkuIdPackage(Map<String, String> colorIdSkuId, Document doc, String color,List<String> colorList) {
		Elements select = doc.select("div.product-detail-container.view");
		String colorSkuId=StringUtils.EMPTY;
		if(CollectionUtils.isNotEmpty(select)){
			String attr = select.attr("data-analytics");
			Type attrType = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> map = JsonUtils.json2bean(attr, attrType);
			Map<String,String> ansMap = (Map<String, String>) map.get("payload");
			String sku =StringUtils.EMPTY;
			try {
				
				sku = ansMap.get("sku");
			} catch (Exception e) {
			}
//			for (String colorId : colorList) {
//				if(StringUtils.containsIgnoreCase(sku, colorId)){
//					sku.replace(colorId, "");
//				}
//			}
			if(StringUtils.isNotBlank(sku)){
				colorSkuId=sku.substring(0, sku.lastIndexOf("-"));
			}
		}
		colorIdSkuId.put(color.replaceAll("[+]", " "), colorSkuId);
	}
}
