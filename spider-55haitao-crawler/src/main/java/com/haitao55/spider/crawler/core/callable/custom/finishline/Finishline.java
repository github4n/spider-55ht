package com.haitao55.spider.crawler.core.callable.custom.finishline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * finishline 详情页封装 Title: Description: Company: 55海淘
 * 
 * @author wangyi
 * @date 2017年10月18日 下午3:48:53
 * @version 2.0
 */
public class Finishline extends AbstractSelect {

	private static final String domain = "www.finishline.com";
	// private static final String vps = "asdl.55haitao.com/pool?pool=9998";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String SEX_WOMEN2 = "girl";
	private static final String SEX_MEN2 = "boy";
	private static final String baseUri = "https://www.finishline.com";
	private static final List<String> TITLES = new ArrayList<String>();
	static {
		TITLES.add("New Arrivals-menu-dropdown lengthyMenu");
		TITLES.add("Men-menu-dropdown  ");
		TITLES.add("Women-menu-dropdown  ");
		TITLES.add("Kids-menu-dropdown  ");
		TITLES.add("Sale-menu-dropdown  ");
		TITLES.add("Fan Gear-menu-dropdown  ");
		TITLES.add("brands-menu-dropdown  ");
	}

	@Override
	public void invoke(Context context) throws Exception {

		String sourceUrl = context.getCurrentUrl();
		String url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(sourceUrl);
		if(StringUtils.isBlank(url)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"Finishline.com itemUrl: "+sourceUrl+" ,  url rule is error.");
		}
		Map<String, Object> headers = new HashMap<String, Object>();

		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "textml,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");

		String content = "";
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		Proxy proxy = null;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(60000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		// context.put(input, content);
		Document doc = JsoupUtils.parse(content, baseUri);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// brand
		String brand = StringUtils.substringBetween(content, "FL.setup.brand = \"", "\";");

		// productId
		String productId = StringUtils.EMPTY;

		// default sku id
		String default_sku_id = StringUtils.EMPTY;

		// spu stock
		int stock_status = 0;

		// price
		boolean spu_price_flag = false;

		// category
		String category = StringUtils.substringBetween(content, "s.eVar4=\"", "\";");

		String gender = StringUtils.EMPTY;
		Elements genderElements = doc.select("div#sizeChart");
		if (CollectionUtils.isNotEmpty(genderElements)) {
			gender = genderElements.attr("data-subgender");
			if (StringUtils.isEmpty(gender)) {
				gender = genderElements.attr("data-gender");
			}
		}

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("h1#title");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = StringEscapeUtils.unescapeHtml(titleElements.text());
			productId = titleElements.attr("data-productitemid");
		}

		// price
		JSONObject priceJsonObject = new JSONObject();
		price_json_package(priceJsonObject, doc);

		// list add color对应id,用于发送请求获取对应颜色图片
		List<String> list = new ArrayList<String>();

		// color
		JSONObject colorJsonObject = new JSONObject();
		default_sku_id = color_json_package(colorJsonObject, doc, default_sku_id, list);
		if (StringUtils.containsIgnoreCase(content, "s.products=\";")) {
			default_sku_id = StringUtils.substringBetween(content, "s.products=\";", "\";");
		}
		// size
		JSONObject sizeJsonObject = new JSONObject();
		size_json_package(sizeJsonObject, doc);

		// color size price组合sku
		JSONArray skuJsonArray = new JSONArray();
		sku_jsonarray_package(priceJsonObject, colorJsonObject, sizeJsonObject, skuJsonArray);

		// callable 调用获取图片方法，获取对应颜色图片
		JSONObject picsJsonObject = new JSONObject();
		picsJsonObject = new FinishlineHandler().process(list, proxy);

		// json数据封装
		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// style
		JSONObject styleJsonObject = new JSONObject();

		// iteartor skuarray
		if (null != skuJsonArray && skuJsonArray.size() > 0) {
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObejct = (JSONObject) object;

				// selectlist
				LSelectionList lselectlist = new LSelectionList();
				// skuId
				String skuId = skuJsonObejct.getString("skuId");
				// stock
				int sku_stock_status = skuJsonObejct.getIntValue("stock_status");
				int stock_number = 0;

				// spu stock status
				if (sku_stock_status > 0) {
					stock_status = 1;
				}

				// price
				float sale_price = skuJsonObejct.getFloatValue("sale_price");
				float orign_price = skuJsonObejct.getFloatValue("orign_price");
				String unit = skuJsonObejct.getString("unit");
				int save = skuJsonObejct.getIntValue("save");

				String color_id = skuJsonObejct.getString("productId");
				// spu price
				if (!spu_price_flag) {
					if (default_sku_id.equals(color_id)) {
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
						spu_price_flag = true;
					}
				}

				String sku_color = skuJsonObejct.getString("color");
				String sku_size = skuJsonObejct.getString("size");

				// selections
				List<Selection> selections = new ArrayList<Selection>();
				if (StringUtils.isNotBlank(sku_size)) {
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name("Size");
					selection.setSelect_value(sku_size);
					selections.add(selection);
				}

				// lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(unit);
				lselectlist.setStock_status(sku_stock_status);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStyle_id(sku_color);
				lselectlist.setSelections(selections);

				// l_selection_list
				l_selection_list.add(lselectlist);

				// style json
				styleJsonObject.put(sku_color, skuJsonObejct);
			}
		}

		// stylelist 封装
		if (null != styleJsonObject && styleJsonObject.size() > 0) {
			for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
				// String style_id = entry.getKey();
				JSONObject jsonObject = (JSONObject) entry.getValue();
				// stylelist
				LStyleList lStyleList = new LStyleList();
				// skuId
				String skuId = jsonObject.getString("skuId");
				String switch_img = jsonObject.getString("switch_img");
				String color_id = jsonObject.getString("productId");
				if (default_sku_id.equals(color_id)) {
					lStyleList.setDisplay(true);
				}

				// item image upc

				String sku_color = jsonObject.getString("color");
				// stylelist
				lStyleList.setGood_id(color_id);
				lStyleList.setStyle_switch_img(switch_img);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_id(sku_color);
				lStyleList.setStyle_cate_name("Color");
				lStyleList.setStyle_name(sku_color);

				// images
				// List<Image> pics = new ArrayList<Image>();
				@SuppressWarnings("unchecked")
				List<Image> pics = (List<Image>) picsJsonObject.get(color_id);
				context.getUrl().getImages().put(color_id, pics);
				// l_style_list
				l_style_list.add(lStyleList);
			}
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(stock_status));

		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));
		;

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(domain.concat(productId));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(domain));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// category breadcrumb
		category_package(category, brand, retBody);

		// description
		desc_package(doc, retBody);

		// properties
		properties_package(retBody, gender);

		setOutput(context, retBody);
	}

	/**
	 * sku jsonarray 封装
	 * 
	 * @param priceJsonObject
	 * @param colorJsonObject
	 * @param sizeJsonObject
	 * @param skuJsonArray
	 */
	private static void sku_jsonarray_package(JSONObject priceJsonObject, JSONObject colorJsonObject,
			JSONObject sizeJsonObject, JSONArray skuJsonArray) {
		if (null != colorJsonObject && colorJsonObject.size() > 0) {
			for (Map.Entry<String, Object> entry : colorJsonObject.entrySet()) {
				String skuId = entry.getKey();
				JSONObject colorJson = (JSONObject) entry.getValue();
				String color = colorJson.getString("color");
				String switch_img = colorJson.getString("switch_img");
				// price
				JSONObject skuPriceJson = priceJsonObject.getJSONObject(skuId);
				float sale_price = skuPriceJson.getFloatValue("sale_price");
				float orign_price = skuPriceJson.getFloatValue("orign_price");
				String unit = skuPriceJson.getString("unit");
				int save = skuPriceJson.getIntValue("save");

				// 颜色对应size jsonarray
				JSONArray sizeJsonArray = sizeJsonObject.getJSONArray(skuId);
				if (null != sizeJsonArray && sizeJsonArray.size() > 0) {
					for (Object object : sizeJsonArray) {
						// sku json
						JSONObject skuJsonObject = new JSONObject();

						JSONObject sizeJson = (JSONObject) object;
						String stock_status = sizeJson.getString("stock_status");
						String size = sizeJson.getString("size");
						String data_sku = sizeJson.getString("data_sku");

						skuJsonObject.put("productId", skuId);
						skuJsonObject.put("skuId", data_sku);
						skuJsonObject.put("color", color);
						skuJsonObject.put("size", size);
						skuJsonObject.put("stock_status", stock_status);
						skuJsonObject.put("sale_price", sale_price);
						skuJsonObject.put("orign_price", orign_price);
						skuJsonObject.put("unit", unit);
						skuJsonObject.put("save", save);
						skuJsonObject.put("switch_img", switch_img);

						skuJsonArray.add(skuJsonObject);
					}
				}

			}
		}
	}

	/**
	 * size json 封装
	 * 
	 * @param sizeJsonObject
	 * @param doc
	 */
	private static void size_json_package(JSONObject sizeJsonObject, Document doc) {
		Elements sizeElements = doc.select("div#alternateSizes div");
		if (CollectionUtils.isNotEmpty(sizeElements)) {
			for (Element element : sizeElements) {
				JSONArray jsonArray = new JSONArray();
				String skuId = element.attr("id");
				if (StringUtils.isNotBlank(skuId)) {
					skuId = StringUtils.substringAfter(skuId, "sizes_");

					Elements skuSizeElements = element.select("div.size");
					if (CollectionUtils.isNotEmpty(skuSizeElements)) {
						for (Element element2 : skuSizeElements) {
							JSONObject skuSizeJsonObject = new JSONObject();
							int stock_status = 1;
							String stock_status_flag = element2.attr("class");
							if (StringUtils.containsIgnoreCase(stock_status_flag, "unavailable")) {
								stock_status = 0;
							}
							String data_sku = element2.attr("data-sku");

							String size = element2.text();
							skuSizeJsonObject.put("stock_status", stock_status);
							skuSizeJsonObject.put("size", size);
							skuSizeJsonObject.put("data_sku", data_sku);

							jsonArray.add(skuSizeJsonObject);
						}
					}
				}
				sizeJsonObject.put(skuId, jsonArray);
			}
		}
	}

	/**
	 * color json 封装
	 * 
	 * @param colorJsonObject
	 * @param doc
	 * @param default_sku_id
	 * @param list
	 */
	private static String color_json_package(JSONObject colorJsonObject, Document doc, String default_sku_id,
			List<String> list) {
		Elements colorElements = doc.select("div#alternateColors a");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				JSONObject jsonObject = new JSONObject();
				String skuId = element.attr("data-productid");

				// default sku
				String attr = element.attr("class");
				if (StringUtils.equalsIgnoreCase("selected", attr)) {
					default_sku_id = skuId;
				}

				// list add
				list.add(skuId);

				Elements switchElements = element.select("div.image.altColorShoe img");
				String switch_img = StringUtils.EMPTY;
				String color = StringUtils.EMPTY;
				if (CollectionUtils.isNotEmpty(switchElements)) {
					switch_img = switchElements.attr("src");
					color = switchElements.attr("alt");
				}

				if (StringUtils.isBlank(color)) {
					color = skuId;
				}
				jsonObject.put("color", color);
				jsonObject.put("switch_img", switch_img);

				colorJsonObject.put(skuId, jsonObject);
			}
		}
		return default_sku_id;
	}

	/**
	 * price json 封装
	 * 
	 * @param priceJsonObject
	 * @param doc
	 */
	private static void price_json_package(JSONObject priceJsonObject, Document doc) {
		Elements priceElements = doc.select("div#altPrices div.priceList");
		if (CollectionUtils.isNotEmpty(priceElements)) {
			for (Element element : priceElements) {
				JSONObject jsonObject = new JSONObject();
				String skuId = element.attr("id");
				if (StringUtils.isNotBlank(skuId)) {
					skuId = StringUtils.substringAfter(skuId, "prices_");
				}
				// price
				sku_price_package(element, jsonObject);
				priceJsonObject.put(skuId, jsonObject);
			}
		}
	}

	/**
	 * sku 价格 json封装
	 * 
	 * @param element
	 * @param jsonObject
	 */
	private static void sku_price_package(Element element, JSONObject jsonObject) {
		Elements skuPriceElements = element.select("span.fullPrice");
		Elements skuSalePriceElements = element.select("span.nowPrice");
		Elements skuOrignPriceElements = element.select("span.wasPrice");
		String sale_price = StringUtils.EMPTY;
		String orign_price = StringUtils.EMPTY;
		String unit = StringUtils.EMPTY;
		String save = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(skuPriceElements)) {
			sale_price = skuPriceElements.text();
		} else if (CollectionUtils.isNotEmpty(skuSalePriceElements)
				&& CollectionUtils.isNotEmpty(skuOrignPriceElements)) {
			sale_price = skuSalePriceElements.text();
			orign_price = skuOrignPriceElements.text();
		} else {
			Elements priceElements = element.select("div.productPrice");
			sale_price = priceElements.select("span.maskedFullPrice").text();
		}

		unit = getCurrencyValue(sale_price);
		sale_price = sale_price.replaceAll("[$,]", "");
		orign_price = orign_price.replaceAll("[$,]", "");

		if (StringUtils.isBlank(orign_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(sale_price)) {
			sale_price = orign_price;
		}
		if (StringUtils.isBlank(orign_price) || Float.valueOf(orign_price) < Float.valueOf(sale_price)) {
			orign_price = sale_price;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(sale_price) / Float.valueOf(orign_price)) * 100) + "";// discount
		}

		jsonObject.put("sale_price", sale_price);
		jsonObject.put("orign_price", orign_price);
		jsonObject.put("unit", unit);
		jsonObject.put("save", save);
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

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param category
	 * @param brand
	 * @param retBody
	 */
	private static void category_package(String category, String brand, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		String[] category_data = StringUtils.split(category, ">");
		if (null != category_data && category_data.length > 0) {
			for (String cat : category_data) {
				if (StringUtils.containsIgnoreCase(cat, "home")) {
					continue;
				}
				cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(cat));
				if (StringUtils.isNotBlank(cat)) {
					cats.add(cat);
					breads.add(cat);
				}
			}
		}
		retBody.setCategory(cats);

		// BreadCrumb
		if (StringUtils.isNotBlank(brand)) {
			breads.add(brand);
		}
		retBody.setBreadCrumb(breads);
	}

	/***
	 * 描述 封装
	 * 
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("div#productDescription p , div#productDescription ul li");
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
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	/**
	 * properties 封装
	 * 
	 * @param gender
	 * @param retBody
	 * @param gender2
	 */
	private static void properties_package(RetBody retBody, String gender2) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = getSex(gender2);

		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getTitle().getEn());
		}

		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN2)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN2)) {
			gender = "men";
		}
		return gender;
	}
}
