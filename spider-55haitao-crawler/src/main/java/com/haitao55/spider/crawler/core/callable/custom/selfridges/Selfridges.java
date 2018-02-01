package com.haitao55.spider.crawler.core.callable.custom.selfridges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class Selfridges extends AbstractSelect {
	private static String skuStockUrl = "http://www.selfridges.com//GB/en/webapp/wcs/stores/servlet/AjaxStockStatusView?";
	private static String skuPriceUrl = "http://www.selfridges.com/GB/en/webapp/wcs/stores/servlet/AjaxMarkdownPriceView?productId=";
	private static String skuimageUrl = "http://images.selfridges.com/is/image//selfridges?req=set,json&imageset=%7B&&%7D&defaultImage=";
	private static String skuStockUrlParam = "attr=Colour&attrval=";
	private static String colorParam1 = "_M";
	private static String colorParam2 = "_IMGSET";
	private static String colorParam3 = "_360";
	private static String colorParam4 = "_VID01-AVS";

	@Override
	public void invoke(Context context) throws Exception {
		Sku sku = new Sku();
		String content = super.getInputString(context);
		Document doc = this.getDocument(context);

		Pattern p = Pattern.compile("Sorry, this product is currently out of stock");
		Matcher m = p.matcher(content);
		if (m.find()) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"selfridges.com itemUrl:" + context.getUrl().toString() + " not found..");
		}
		if (StringUtils.isNotBlank(content)) {
			String default_color = StringUtils.EMPTY;
			String colorImageFlag = StringUtils.EMPTY;
			boolean singleItem = false;
			boolean onlySizeFlag = false;
			String productId = StringUtils.substringBetween(content, "\"product_id\" : \"", "\",");
			List<String> colorList = new ArrayList<String>();
			// stock json 请求地址
			Set<String> skuStockUrlList = new HashSet<String>();
			// image json 请求地址
			List<String> skuImageUrlList = new ArrayList<String>();

			// 颜色对应小图map
			Map<String, String> colorSwitchImgMap = new HashMap<String, String>();

			// 颜色对应图片
			Map<String, List<Image>> colorMap = new HashMap<String, List<Image>>();

			// price JSONObject
			JSONObject priceJson = new JSONObject();

			Elements elements = doc.select("form[name=OrderItemAddForm]");
			if (CollectionUtils.isNotEmpty(elements)) {
				default_color = elements.get(0).attr("data-preselect");
				if (StringUtils.isBlank(default_color)) {
					default_color = StringUtils.substringBetween(
							StringUtils.substringBetween(content, "\"product_image\" : \"", "?"), "_", "_");
				}
				// default 统一转换
				default_color = upcaseAndtrimString(default_color);
			}
			elements = doc.select("input[name=wcid]");
			if (CollectionUtils.isNotEmpty(elements)) {
				colorImageFlag = elements.get(0).attr("value");
			}

			/** 封装不同商详页，stock,price,image请求ｕｒｌ start */
			elements = doc.select("fieldset.att1.colour>ul>li");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String color = element.select("input").attr("value");
					skuImageAndColorListPackage(colorImageFlag, element, color, colorList, colorSwitchImgMap,
							skuImageUrlList);
					color = replaceColorValue(element.select("input").attr("value"));
					StringBuffer buffer = new StringBuffer();
					Elements sizeElements = doc.select("fieldset.att2.size select.dk option:not(:first-child)");
					Elements sizeElements2 = doc.select("fieldset.att2.size ul li");
					if (CollectionUtils.isEmpty(sizeElements) && CollectionUtils.isEmpty(sizeElements2)) {
						buffer.append(skuStockUrl).append("&productId=").append(productId).append("&storeId=10052");
					} else {
						buffer.append(skuStockUrl).append(skuStockUrlParam).append(color).append("&productId=")
								.append(productId).append("&storeId=10052");
					}
					skuStockUrlList.add(buffer.toString());
				}
			} else {

				// other form
				elements = doc.select("fieldset.att1.colour select.dk option:not(:first-child)");
				if (CollectionUtils.isNotEmpty(elements)) {
					skuStockUrlList.add(skuStockUrl.concat("&productId=" + productId).concat("&storeId=10052"));
					for (Element element : elements) {
						String color = replaceColorValue(StringUtils.upperCase(element.attr("value")));
						skuImageAndColorListPackage(colorImageFlag, element, color, colorList, colorSwitchImgMap,
								skuImageUrlList);
					}
				}
			}

			if (CollectionUtils.isEmpty(elements)) {// only size
				elements = doc.select("fieldset.att1.size select.dk option:not(:first-child)");
				if (CollectionUtils.isNotEmpty(elements)) {
					skuStockUrlList.add(skuStockUrl.concat("&productId=" + productId).concat("&storeId=10052"));
					StringBuffer imageParamBuffer = new StringBuffer();
					imageParamBuffer.append(colorImageFlag).append(colorParam1).append(",").append(colorImageFlag)
							.append(colorParam2).append(",").append(colorImageFlag).append(colorParam3).append(",")
							.append(colorImageFlag).append(colorParam4);
					skuImageUrlList.add(skuimageUrl.replaceAll("&&", imageParamBuffer.toString()));
					onlySizeFlag = true;
					default_color = upcaseAndtrimString(elements.get(0).attr("value"));
				}
				if (CollectionUtils.isEmpty(elements)) {// 单品页
					skuStockUrlList.add(skuStockUrl.concat("&productId=" + productId).concat("&storeId=10052"));
					StringBuffer imageParamBuffer = new StringBuffer();
					imageParamBuffer.append(colorImageFlag).append(colorParam1).append(",").append(colorImageFlag)
							.append(colorParam2).append(",").append(colorImageFlag).append(colorParam3).append(",")
							.append(colorImageFlag).append(colorParam4);
					skuImageUrlList.add(skuimageUrl.replaceAll("&&", imageParamBuffer.toString()));
					singleItem = true;
				}
			}
			/** 封装不同商详页，stock,price,image请求ｕｒｌ end */

			JSONArray skuStockArray = new JSONArray();
			if (skuStockUrlList.size() > 0) {

				// packeage skudata {color,size,price,image}

				/** stock array */
				skuStockArray = new SelfridgesStockHandler().process(skuStockUrlList, onlySizeFlag);
				skuStockArray = sortJsonArray(skuStockArray);

				/** image map */
				// image sku data
				colorMap = new SelfridgesImageHandler().process(skuImageUrlList);

				// sku price
				String priceSkuData = HttpUtils.get(skuPriceUrl.concat(productId).concat("&storeId=10052"));
				priceJson = JSONObject.parseObject(priceSkuData);
			}

			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style map
			Map<String, JSONObject> styleMap = new HashMap<String, JSONObject>();

			// stock number map
			Map<String, Integer> stockMap = new HashMap<String, Integer>();
			if (!singleItem) {

				for (Object object : skuStockArray) {
					LSelectionList lselectlist = new LSelectionList();
					JSONObject skuData = (JSONObject) object;

					String skuId = String.valueOf(skuData.get("sku"));
					lselectlist.setGoods_id(skuId);

					String style_id = String.valueOf(skuData.get("value"));
					// 用于判断 是否sku存在多属性
					boolean selectionFlag = false;
					if (null != skuData.get("colorName")) {
						selectionFlag = true;
						style_id = String.valueOf(skuData.get("colorValue"));
					}
					lselectlist.setStyle_id(style_id);

					// stock
					int stock_status = 0;
					int stock_number = 0;
					boolean instock = (boolean) skuData.get("inStock");
					if (instock) {
						stock_status = 1;
					}
					stock_number = (int) skuData.get("qty");
					if (stock_number > 0) {
						stock_status = 2;
					}
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);

					// stock number map
					String tempStyle_id = StringUtils.upperCase(transToSkuImageUrlParam(style_id));
					if (null == stockMap.get(tempStyle_id)) {
						stockMap.put(tempStyle_id, stock_number);
					} else {
						stockMap.put(tempStyle_id, stock_number + stockMap.get(tempStyle_id));
					}

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					if (selectionFlag) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name(String.valueOf(skuData.get("name")));
						selection.setSelect_value(String.valueOf(skuData.get("value")));
						selections.add(selection);
					}
					lselectlist.setSelections(selections);

					JSONObject priceObject = new JSONObject();
					// price
					if (null != priceJson) {
						priceObject = (JSONObject) priceJson.get(skuId);
					}
					String salePrice = priceObject.getString("now");
					String origPrice = priceObject.getString("was");
					String save = StringUtils.EMPTY;
					String unit = getCurrencyValue(salePrice);// 得到货币代码
					if(!StringUtils.equals(unit, "GBP")){
			            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" Selfridges-price-unit-is-not-GBP...");
			        }
					salePrice = salePrice.replaceAll("[$,£]", "");

					if (StringUtils.isBlank(replace(origPrice))) {
						origPrice = salePrice;
					}
					origPrice = origPrice.replaceAll("[$,£]", "");
					if (StringUtils.isBlank(replace(salePrice))) {
						salePrice = origPrice;
					}
					if (StringUtils.isBlank(origPrice)
							|| Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
						origPrice = salePrice;
					}
					if (StringUtils.isBlank(save)) {
						save = Math.round(
								(1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100) + "";// discount
					}
					lselectlist.setOrig_price(Float.valueOf(origPrice));
					lselectlist.setPrice_unit(unit);
					lselectlist.setSale_price(Float.valueOf(salePrice));

					if (StringUtils.equalsIgnoreCase(transToSkuImageUrlParam(default_color),
							transToSkuImageUrlParam(style_id))) {
						context.put("Price",
								new Price(Float.valueOf(origPrice),
										StringUtils.isBlank(save) ? 0 : Integer.parseInt(save),
										Float.valueOf(salePrice), unit));
					}

					l_selection_list.add(lselectlist);

					// stylemap package
					styleMap.put(style_id, skuData);
				}

				if (null != styleMap && styleMap.size() > 0) {
					for (Map.Entry<String, JSONObject> entry : styleMap.entrySet()) {
						LStyleList lStyleList = new LStyleList();
						String style_id = entry.getKey();
						JSONObject skuData = entry.getValue();
						String style_cate_name = String.valueOf(skuData.get("name"));
						// 用于判断 是否sku存在多属性
						if (null != skuData.get("colorName")) {
							style_cate_name = String.valueOf(skuData.get("colorName"));
						}
						String good_id = String.valueOf(skuData.get("sku"));
						String switch_img = StringUtils.EMPTY;
						if (null != colorSwitchImgMap.get(StringUtils.upperCase(style_id))) {
							switch_img = colorSwitchImgMap.get(StringUtils.upperCase(style_id));
						}
						if (null != colorSwitchImgMap.get(style_id)) {
							switch_img = colorSwitchImgMap.get(style_id);
						}
						lStyleList.setGood_id(good_id);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name(style_cate_name);
						lStyleList.setStyle_name(style_id);

						boolean display = false;
						if (StringUtils.equalsIgnoreCase(default_color, upcaseAndtrimString(style_id))) {
							display = true;
						}
						lStyleList.setDisplay(display);

						// images
						List<Image> list = colorMap.get(StringUtils.upperCase(transToSkuImageUrlParam(style_id)));
						if (null == list) {
							list = new ArrayList<Image>();
							for (Map.Entry<String, List<Image>> imageEntry : colorMap.entrySet()) {
								list.addAll(imageEntry.getValue());
							}
						}

						context.getUrl().getImages().put(good_id, list);

						l_style_list.add(lStyleList);
					}
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			// sku
			context.put("Sku", sku);
			// spu stock
			int stock_status = 0;
			if (!singleItem) {
				if (null != stockMap && stockMap.size() > 0) {
					Integer number = stockMap.get(StringUtils.upperCase(transToSkuImageUrlParam(default_color)));
					if (null == number) {
						stock_status = 1;
					}
					else if(number==0){
						//default sku stock_number=0, seach other sku
						if(stockMap.size()>1){
							for (Map.Entry<String, Integer> entry: stockMap.entrySet()) {
								number+=entry.getValue();
							}
						}
					}
					
					else if (number > 0) {
						stock_status = 2;
					}
				}
			} else {
				// single item stock
				if (skuStockArray.size() == 0) {
					stock_status = 0;
				} else {
					Object singleItemStock = skuStockArray.get(0);
					if (null != singleItemStock) {
						JSONObject singleItemStockJsonObject = (JSONObject) singleItemStock;
						Integer stock_number = singleItemStockJsonObject.getInteger("qty");
						if (stock_number > 0) {
							stock_status = 1;
						}
					}
				}

				// single item image
				List<Image> pics = new ArrayList<Image>();
				if (colorMap.size() > 0) {
					for (Map.Entry<String, List<Image>> entry : colorMap.entrySet()) {
						List<Image> value = entry.getValue();
						for (Image image : value) {
							pics.add(image);
						}
					}
				}
				context.getUrl().getImages().put(productId, pics);
			}
			// is available to united states
			elements = doc.select("div#delResMessage");
			if (CollectionUtils.isNotEmpty(elements)) {
				String text = elements.get(0).text();
				if (StringUtils.containsIgnoreCase(text, "Not available")) {
					if (StringUtils.containsIgnoreCase(text, "United States")) {
						stock_status = 0;
					}
				}
			}
			// ret.setStock(new Stock(stock_status));
			context.put("Stock", new Stock(stock_status));

			// is price is null when default color stock is null
			if (null == context.get("Price") || singleItem) {
				String salePrice = StringUtils.EMPTY;
				String origPrice = StringUtils.EMPTY;
				Elements salePriceElements = doc.select("p.price");
				if (CollectionUtils.isNotEmpty(salePriceElements)) {
					salePrice = salePriceElements.get(0).text();
				}
				Elements orignPriceElements = doc.select("p.wasPrice");
				if (CollectionUtils.isNotEmpty(orignPriceElements)) {
					origPrice = orignPriceElements.get(0).text();
				}
				String save = StringUtils.EMPTY;
				String unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[$,£]", "");

				if (StringUtils.isBlank(replace(origPrice))) {
					origPrice = salePrice;
				}
				origPrice = origPrice.replaceAll("[$,£]", "");
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
				context.put("Price", new Price(Float.valueOf(origPrice),
						StringUtils.isBlank(save) ? 0 : Integer.parseInt(save), Float.valueOf(salePrice), unit));
			}
		}

	}

	/**
	 * 转换大写， 去除存在空格（包括字符串中间空格）
	 * 
	 * @param default_color
	 * @return
	 */
	private static String upcaseAndtrimString(String default_color) {
		String temp = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(default_color)) {
			temp = StringUtils.upperCase(default_color).replaceAll("[ ]", "");
		}
		return temp;
	}

	/**
	 * 封裝 colorList ，skuImage請求ｕｒｌ
	 * 
	 * @param color
	 * @param colorList
	 * @param colorSwitchImgMap
	 * @param skuImageUrlList
	 */
	private static void skuImageAndColorListPackage(String colorParam, Element element, String color,
			List<String> colorList, Map<String, String> colorSwitchImgMap, List<String> skuImageUrlList) {
		colorList.add(color);
		String colorSwitchImg = element.select("img").attr("src");
		if (StringUtils.isNotBlank(colorSwitchImg)) {
			colorSwitchImg = colorSwitchImg.substring(0, element.select("img").attr("src").indexOf("?"));
		}
		if (StringUtils.isBlank(colorSwitchImg)) {
			colorSwitchImg = element.attr("data-img").substring(0, element.attr("data-img").indexOf("?"));
		}
		colorSwitchImgMap.put(color, colorSwitchImg);
		color = transToSkuImageUrlParam(color);
		StringBuffer imageParamBuffer = new StringBuffer();
		imageParamBuffer.append(colorParam).append("_").append(color).append(colorParam1).append(",").append(colorParam)
				.append("_").append(color).append(colorParam2).append(",").append(colorParam).append("_").append(color)
				.append(colorParam3).append(",").append(colorParam).append("_").append(color).append(colorParam4);
		skuImageUrlList.add(skuimageUrl.replaceAll("&&", imageParamBuffer.toString()));
	}

	/**
	 * 转换 请求 图片地址 ｃｏｌｏｒ形式
	 * 
	 * @param color
	 * @return
	 */
	private static String transToSkuImageUrlParam(String color) {
		String temp = StringUtils.EMPTY;
		if (StringUtils.isBlank(color)) {
			return temp;
		}
		color = StringUtils.upperCase(color.replaceAll("[ ]", ""));
		color = StringUtils.upperCase(color.replaceAll("[+]", ""));
		color = StringUtils.upperCase(color.replaceAll("[.]", ""));
		color = StringUtils.upperCase(color.replaceAll("[/]", ""));
		color = StringUtils.upperCase(color.replaceAll("[-]", ""));
		color = StringUtils.upperCase(color.replaceAll("[&]", ""));
		return color;
	}

	/**
	 * replace color value Pitch black -->Pitch+black
	 * 
	 * @param attr
	 * @return
	 */
	private static String replaceColorValue(String attr) {
		String color = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(attr, " ")) {
			color = attr.replaceAll("[ ]", "+");
		} else {
			color = attr;
		}
		return color;
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

	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		if (null == dest) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

	}

	public static JSONArray sortJsonArray(JSONArray array) {
		List<Object> jsons = new ArrayList<Object>();
		try {
			for (int i = 0; i < array.size(); i++) {

				jsons.add(array.get(i));

			}
			Collections.sort(jsons, new Comparator<Object>() {
				@Override
				public int compare(Object lhs, Object rhs) {
					try {
						JSONObject jsonLhs = (JSONObject) lhs;

						// 針對 下拉類型 ｓｋｕ 商品 進行判斷
						if (StringUtils.isBlank(jsonLhs.getString("colorValue"))) {
							String lid = jsonLhs.getString("value");

							JSONObject jsonRhs = (JSONObject) rhs;
							String rid = jsonRhs.getString("value");
							// Here you could parse string id to integer and
							// then
							// compare.
							return lid.compareTo(rid);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new JSONArray(jsons);
	}

}
