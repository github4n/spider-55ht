package com.haitao55.spider.crawler.core.callable.custom.maccosmetics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * maccosmetics 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月13日 下午5:16:17
 * @version 1.0
 */
public class Maccosmetics extends AbstractSelect {
	private static final String SERVICE_URL = "https://www.maccosmetics.com/rpc/jsonrpc.tmpl";
	
	
	private static final String SERVICE_URL_PARAM = "[{\"method\":\"prodcat.querykey\",\"params\":[{\"products\":[\"()\"],\"query_key\":\"catalog-mpp-volatile\"}],\"id\":1}]";
	private static final String IMAGE_PREFFIX = "http://www.maccosmetics.com/";
	private static final String INSTOCK = "1";
	private static final String DOMAIN = "www.maccosmetics.com";
	private static final String BRAND = "MAC Cosmetics";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	@SuppressWarnings("unchecked")
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String referer = context.getUrl().getParentUrl();
		//String content = this.getInputString(context);
		if(!url.contains("https")){
			url = url.replace("http:", "https:");
		}
		
		String content = StringUtils.EMPTY;
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(url, getProdcutHeaders(referer));
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				content = Crawler.create().timeOut(15000).header(getProdcutHeaders(referer)).url(url).method(HttpMethod.POST.getValue())
						.resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String proxyAddress=proxy.getIp();
				int proxyPort=proxy.getPort();
				content = Crawler.create().timeOut(30000).url(url).header(getProdcutHeaders(referer)).method(HttpMethod.POST.getValue())
						.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
			}
		}
		
		Document doc = this.getDocument(context);

		if (StringUtils.isNotBlank(content)) {
			Elements productElements = doc.select("a.product__add-to-faves.add-to-favorites.js-add-to-favorites");
			String product_id_temp = StringUtils.EMPTY;
			if (CollectionUtils.isNotEmpty(productElements)) {
				String attr = productElements.attr("data-product");
				product_id_temp = StringUtils.substringBetween(attr, "prodid\": \"", "\",");
			}
			String product_data = StringUtils.substringBetween(content, "var page_data = ", "</script");
			JSONObject itemJSONObject = JSONObject.parseObject(product_data);
			if (MapUtils.isEmpty(itemJSONObject)) {
				return;
			}
			JSONObject productInfoJSONObject = itemJSONObject.getJSONObject("consolidated-products");
			if (MapUtils.isEmpty(productInfoJSONObject)) {
				return;
			}
			JSONArray productsJSONArray = productInfoJSONObject.getJSONArray("products");
			// 存在多个product的jsonObject 需要判断定位到productjsonobject
			JSONObject productJSONObject = new JSONObject();
			if (null != productsJSONArray && productsJSONArray.size() > 0) {
				for (Object object : productsJSONArray) {
					JSONObject jsonObject = (JSONObject) object;
					String productId_temp = jsonObject.getString("PRODUCT_ID");
					if (StringUtils.equalsIgnoreCase(product_id_temp, productId_temp)) {
						productJSONObject = jsonObject;
						break;
					}
				}
			}

			RetBody retBody = new RetBody();
			Sku sku = new Sku();
			// 默认 skuId
			String default_sku_id = StringUtils.EMPTY;
			// productId
			String productId = productJSONObject.getString("PRODUCT_ID");
			// spu stock status
			int spu_stock_status = 0;
			// unit
			String unit = StringUtils.EMPTY;

			// title
			String title = StringUtils.substringBetween(content, "\"name\": \"", "\",");
			// brand
			String brand = StringUtils.substringBetween(StringUtils.substringBetween(content, "\"brand\": {", "},"),
					"\"name\": \"", "\",");
			;
			if (StringUtils.isBlank(brand)) {
				brand = BRAND;
			}

			// category
			List<String> category = new ArrayList<String>();
			category_package(itemJSONObject, category);

			// 得到 productJSONObject 用作数据解析 封装
			// 默认sku
			JSONObject defaultSkuJSONObject = productJSONObject.getJSONObject("defaultSku");
			if (!defaultSkuJSONObject.isEmpty()) {
				String skuId = defaultSkuJSONObject.getString("SKU_ID");
				if (StringUtils.isNotBlank(skuId)) {
					default_sku_id = skuId;
				}
				String price_temp = defaultSkuJSONObject.getString("formattedPrice2");
				if (StringUtils.isNotBlank(price_temp)) {
					unit = getCurrencyValue(price_temp);
				}
			}
			// unit 健壮判断
			if (StringUtils.isBlank(unit)) {
				unit = Currency.USD.name();
			}

			// default skuId 健壮判断
			if (StringUtils.isBlank(default_sku_id)) {
				default_sku_id = StringUtils.substringBetween(content, "\"sku\": \"", "\",");
			}

			JSONObject skuStockJSONObject = new JSONObject();
			skuStockJSONObjectPackage(context,skuStockJSONObject,productId);
			
			// image jsonobject
			JSONObject imageJSONObject = new JSONObject();
			// 封装sku
			JSONArray skuJSONArray = new JSONArray();
			sku_jsonarray_package(productJSONObject, skuJSONArray, imageJSONObject,skuStockJSONObject);

			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style
			JSONObject styleJsonObject = new JSONObject();
			if (skuJSONArray.size() > 0) {
				for (Object object : skuJSONArray) {
					JSONObject skuJSONObject = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJSONObject.getString("skuId");
					// stock
					int stock_number = 0;
					int stock_status = skuJSONObject.getIntValue("stock_status");
					// spu stock status
					if (stock_status > 0) {
						spu_stock_status = 1;
					}

					// price
					float orign_price = skuJSONObject.getFloatValue("orign_price");
					float sale_price = skuJSONObject.getFloatValue("sale_price");

					// spu price
					if (StringUtils.endsWithIgnoreCase(default_sku_id, skuId)) {
						int save = skuJSONObject.getIntValue("save");
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					String sku_color = skuJSONObject.getString("color");
					if (StringUtils.isBlank(sku_color)) {
						sku_color = "default";
					}
					String sku_size = skuJSONObject.getString("size");

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
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(sku_color);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style json
					styleJsonObject.put(skuId, skuJSONObject);
				}
			}
			// stylelist 封装
			if (null != styleJsonObject && styleJsonObject.size() > 0) {
				for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
					String skuId = entry.getKey();
					JSONObject jsonObject = (JSONObject) entry.getValue();
					// stylelist
					LStyleList lStyleList = new LStyleList();
					// skuId
					String sku_color = jsonObject.getString("color");
					if (StringUtils.isBlank(sku_color)) {
						sku_color = "default";
					}
					String switch_img = jsonObject.getString("image_url");
					if (StringUtils.isBlank(switch_img)) {
						switch_img = StringUtils.EMPTY;
					}
					if (StringUtils.endsWithIgnoreCase(default_sku_id, skuId)) {
						lStyleList.setDisplay(true);
					}

					// stylelist
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(sku_color);
					lStyleList.setStyle_cate_name("Color");
					lStyleList.setStyle_name(sku_color);

					// images
					context.getUrl().getImages().put(skuId, (List<Image>) imageJSONObject.get(skuId));
					// l_style_list
					l_style_list.add(lStyleList);
				}
			}

			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			retBody.setSku(sku);

			// stock
			retBody.setStock(new Stock(spu_stock_status));
			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			// title
			retBody.setTitle(new Title(title, "", "", ""));
			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			category_package(brand, category, retBody);

			properties_package(retBody);

			desc_package(doc, retBody);

			setOutput(context, retBody);
		}
	}

	private void skuStockJSONObjectPackage(Context context, JSONObject skuStockJSONObject, String productId) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> payload = new HashMap<String,Object>();
		String replacePattern = StringUtils.replacePattern(SERVICE_URL_PARAM, "\\(\\)", productId);
		payload.put("JSONRPC", replacePattern);
		String service_content = StringUtils.EMPTY;
		try {
			service_content = crawler_package(context,SERVICE_URL,payload);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//String service_content = crawler_package(context,SERVICE_URL,payload);
		if(StringUtils.isNotBlank(service_content)){
			JSONArray parseArray = JSONArray.parseArray(service_content);
			if(CollectionUtils.isNotEmpty(parseArray)){
				for (Object object : parseArray) {
					JSONObject jsonObject = (JSONObject)object;
					JSONObject resultJSONObject = jsonObject.getJSONObject("result");
					JSONObject valueJSONObject = resultJSONObject.getJSONObject("value");
					JSONArray productsJSONArray = valueJSONObject.getJSONArray("products");
					if(CollectionUtils.isNotEmpty(productsJSONArray)){
						for (Object object2 : productsJSONArray) {
							JSONObject jsonObject2 = (JSONObject)object2;
							JSONArray skusJSONArray = jsonObject2.getJSONArray("skus");
							if(CollectionUtils.isNotEmpty(skusJSONArray)){
								for (Object object3 : skusJSONArray) {
									JSONObject jsonObject3 = (JSONObject)object3;
									String skuId = jsonObject3.getString("SKU_ID");
									String isOrderable = jsonObject3.getString("isOrderable");
									skuStockJSONObject.put(skuId, isOrderable);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 封装 skujsonarray
	 * 
	 * @param productJSONObject
	 * @param skuJSONArray
	 * @param imageJSONObject
	 * @param skuStockJSONObject 
	 */
	private static void sku_jsonarray_package(JSONObject productJSONObject, JSONArray skuJSONArray,
			JSONObject imageJSONObject, JSONObject skuStockJSONObject) {
		// sku jsonarray
		JSONArray jsonArray = productJSONObject.getJSONArray("skus");
		if (null != jsonArray && jsonArray.size() > 0) {
			for (Object object : jsonArray) {
				// jsonobject 用作封装sku数据
				JSONObject skuJSONObject = new JSONObject();
				// images
				List<Image> pics = new ArrayList<Image>();
				// 获取数据
				JSONObject jsonObject = (JSONObject) object;
				JSONArray skuImageJSONArray = jsonObject.getJSONArray("LARGE_IMAGE");
				if (null != skuImageJSONArray && skuImageJSONArray.size() > 0) {
					for (Object object2 : skuImageJSONArray) {
						String image_url = (String) object2;
						if (!StringUtils.contains(image_url, "http")) {
							image_url = IMAGE_PREFFIX + image_url;
							pics.add(new Image(image_url));
						}
					}
				}
				// price
				// sale price
				float sale_price = jsonObject.getFloatValue("PRICE");
				// orign price
				String orign_price = jsonObject.getString("PRICE2");
				if (StringUtils.isBlank(orign_price) || StringUtils.equals("null", orign_price)) {
					orign_price = "0";
				}
				if (Float.parseFloat(orign_price) < sale_price) {
					orign_price = sale_price + "";
				}
				int save = Math.round((1 - (sale_price / Float.parseFloat(orign_price))) * 100);

				// skuId
				String skuId = jsonObject.getString("SKU_ID");
				// stock status
				int stock_status = 0;
//				String isOrderable = jsonObject.getString("isOrderable");
				String isOrderable = skuStockJSONObject.getString(skuId);
				if (StringUtils.equals(INSTOCK, isOrderable)) {
					stock_status = 1;
				}

				// switch_image
				String switch_image = jsonObject.getString("IMAGE_SMOOSH");
				if (StringUtils.isBlank(switch_image)) {
					switch_image = jsonObject.getString("SKU_MULTISHADED_IMAGE");
				}
				if (!StringUtils.contains(switch_image, "http")) {
					switch_image = IMAGE_PREFFIX + switch_image;
				}

				// size
				String size = jsonObject.getString("PRODUCT_SIZE");

				// color
				String color = jsonObject.getString("SHADENAME");
				if (StringUtils.isBlank(color)) {
					color = jsonObject.getString("SHADE_DESCRIPTION");
				}
				if(StringUtils.isNotBlank(color)){
					if(color.length() > 50){
						color = color.substring(0, 20);
					}
				}

				skuJSONObject.put("skuId", skuId);
				skuJSONObject.put("color", color);
				skuJSONObject.put("size", size);
				skuJSONObject.put("stock_status", stock_status);
				skuJSONObject.put("sale_price", sale_price);
				skuJSONObject.put("orign_price", orign_price);
				skuJSONObject.put("save", save);
				skuJSONObject.put("switch_image", switch_image);

				skuJSONArray.add(skuJSONObject);

				// image jsonobject 封装
				imageJSONObject.put(skuId, pics);
			}
		}
	}

	/**
	 * category 封装
	 * 
	 * @param jSONObject
	 * @param category
	 */
	private static void category_package(JSONObject jSONObject, List<String> category) {
		// category
		JSONObject categoryjsonObject = jSONObject.getJSONObject("consolidated-categories");
		JSONArray categoryJSONArray = categoryjsonObject.getJSONArray("categories");
		if (null != categoryJSONArray && categoryJSONArray.size() > 0) {
			for (Object object : categoryJSONArray) {
				JSONObject jsonObject = (JSONObject) object;
				JSONArray jsonArray = jsonObject.getJSONArray("children");
				if (null != jsonArray && jsonArray.size() > 0) {
					for (Object object2 : jsonArray) {
						JSONObject jsonObject2 = (JSONObject) object2;
						category.add(jsonObject2.getString("CATEGORY_NAME"));
					}
				}
			}
		}

	}

	/**
	 * category breadcrumbs 封装
	 * 
	 * @param doc
	 * @param brand
	 * @param category
	 * @param retBody
	 */
	private static void category_package(String brand, List<String> category, RetBody retBody) {
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (null != category && category.size() != 0) {
			for (String string : category) {
				String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(string));
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

	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc
				.select("div.product__description-group.product__description--full div.product__description-group-body , "
						+ "div.product__description-group.product__description--benefits.active div.product__description-group-body ul li");
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

	private static void properties_package(RetBody retBody) {
		String gender = getSex(retBody.getTitle().getEn());
		if (StringUtils.isBlank(gender)) {
			gender = getSex(retBody.getCategory().toString());
		}

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
	}

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
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
	
	private  static Map<String,Object> getProdcutHeaders(String url){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.62 Chrome/62.0.3202.62 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.maccosmetics.com");
		headers.put("Referer", url);
		headers.put("Cookie", "MP_CONFIG=%7B%22language_id%22%3A48%2C%22country_id%22%3A1%7D; PSN=%7B%7D; ngglobal=c0f3771b46308333; AMCV_esteelauder=MCAID%7C2C3F736C0519519B-4000060D6006A946; SESSION=549965836-9a135779031832f79c8f6491d23b2920cce6d25040aa5e20a690e26935aa238f; __olapicU=1508406639652; __sonar=17025581201070500149; s_sq=%5B%5BB%5D%5D; ngsession=675a4c7d57789566; ak_bmsc=D8D37B75906DECDD9DD189A0E996DE81A5FE86DF4237000040FAFF59C0A4D60B~plFy623QRkXJst8c4qZ+L3oqjq3ihIFp9SDZJfP6GM1xQ/wWtJuwe2Kxc3/xY3sxOfM2/D3oC2JospFNW/LacWlsU0T8d/v23gGuXSlkRGMOzf9EDoE01Tj8idxgf44MNJsoQcEqzE3FJgZr6Zpn4m6GbyVwx1sjAosgZp7E8+ACUgApPIxBbrE7/Ac5g4UjcZSDSL2tOgEOI9TmzHZWPql8G6eHy6Q0RQIKN5sVyDL00=; FE_USER_CART=available%3A%26csr_logged_in%3A0%26current_available%3A%26first_name%3A%26full_name%3A%26is_loyalty_member%3A0%26is_pro%3A0%26is_rewards_eligible%3A0%26item_count%3A%26loyalty_level%3A0%26loyalty_level_name%3A%26next_level%3A1%26next_level_name%3ASeduced%26pc_email_optin%3A0%26points%3A%26points_to_next_level%3A0%26region_id%3A%26signed_in%3A0; client.isMobile=0; has_js=1; Auser=0%7C0%7C0%7C0%7C0%7C0%7C0%7C0%7C0-null; LPVID=Y1NWM5M2UwMmMwNTMzYTFm; LPSID-48719195=jUJwQpWWSKO_98yaKrRBaQ; btcartcookie=; LOCALE=en_US; bm_sv=E6859E097CDE8D53C3731367059A4890~C3CjrbhAklh1DzONJKdDGoPuxTZ95N/0E8kdOekkUjI+06+S1A2VImmYL8HxhzPwibhRFUIbaSD9NFjR7yFnKNCtCw0pHb1Wnsn8pnWH3S0pglhsGUbZLevBvgknj0BD/ur5Rvvgx7eJdcdrqpuNl7zt/zczZYwdmPaD5rkL7+U=; tms_generic_enable_bobo=1; s_pers=%20v21%3DSPP%2520%257C%2520Lip%2520Conditioner%2520%2528Tube%2529%7C1509950102833%3B; s_cc=true; s_sess=%20v24%3Dnoclick%3B%20event16%3Dnoclick%3B%20COLLECTIONS%3Dnocol%3B; _uetsid=_uet362d2bed");
		return headers;
	}
	
	
	private  static Map<String,Object> getHeaders(String url){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/61.0.3163.100 Chrome/61.0.3163.100 Safari/537.36");
		headers.put("Accept", "*/*");
		headers.put("X-Requested-With", "XMLHttpRequest");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Origin", "https://www.maccosmetics.com");
		headers.put("Host", "www.maccosmetics.com");
		headers.put("Referer", url);
		headers.put("Cookie", "PSN=%7B%7D; ngglobal=c0f3771b46308333; AMCV_esteelauder=MCAID%7C2C3F736C0519519B-4000060D6006A946; ngsession=c0f3771b79972738; __sonar=17025581201070500149; MP_CONFIG=%7B%22language_id%22%3A48%2C%22country_id%22%3A1%7D; SESSION=549965836-9a135779031832f79c8f6491d23b2920cce6d25040aa5e20a690e26935aa238f; s_sq=%5B%5BB%5D%5D; ak_bmsc=264037A59436AA972D520FEB3B2C99C7B81A5BDC065F00006C75E85996CB3E17~pl2inFsMF8G5J8qPp/2y6K7UH1D1xe0nP56aXZwkLqa2qh5ak7mOgGaLii2GMoJKLnXSNCAUh9l+u6LfH2L8rKFYsTEVYb/dR/ZiSIQ7TydB5jPyLITIkordbWNYFtP0vm9xKyVJ/+KJTZJIQpCmsHlZyj5NJgamvtXvmxaKKqFJsNBJTp1ZikI2pcTwX0wV63WbEINQArOx4IgTuNQKxYPSmu5qDIeh/pHbYmoKHdgAA=; Auser=0%7C0%7C0%7C0%7C0%7C0%7C0%7C0%7C0-null; FE_USER_CART=available%3A%26csr_logged_in%3A0%26current_available%3A%26first_name%3A%26full_name%3A%26is_loyalty_member%3A0%26is_pro%3A0%26is_rewards_eligible%3A0%26item_count%3A%26loyalty_level%3A0%26loyalty_level_name%3A%26next_level%3A1%26next_level_name%3ASeduced%26pc_email_optin%3A0%26points%3A%26points_to_next_level%3A0%26region_id%3A%26signed_in%3A0; LOCALE=en_US; __olapicU=1508406639652; btcartcookie=; LPVID=Y1NWM5M2UwMmMwNTMzYTFm; LPSID-48719195=ruGZ25uFQ6KEwdxPP2Me9w; tms_generic_enable_bobo=1; s_pers=%20v21%3DSPP%2520%257C%2520Lipstick%2520%252F%2520Little%2520M%25C2%25B7A%25C2%25B7C%7C1508408442674%3B; s_cc=true; s_sess=%20v24%3Dnoclick%3B%20event16%3Dnoclick%3B%20COLLECTIONS%3Dnocol%3B; _uetsid=_uet9ab13a4b; client.isMobile=0; has_js=1; bm_sv=68BE1F2FE70C4E3DA7965D6FE48515BA~OEoeARwJBn1AeeCginVNIcYqFCpqAQ4R/fJIVHl5FodPSvh70y64LNgvhiqyC+05nRiAaHgD3Skrpkuao9pqMEnpVk7sue1gwyK4o3/pq2/L2pvJQM2KBctwoX8UM+IizFc16LjiNqh2AOYD0jaKBY8x0ZWTx8YjcCW5EqLT4UI=");
		return headers;
	}
	
	private String crawler_package(Context context,String url,Map<String,Object> payload) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(url).header(getHeaders(context.getCurrentUrl())).payload(payload).method(HttpMethod.POST.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(context.getCurrentUrl())).payload(payload).method(HttpMethod.POST.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "https://www.maccosmetics.com/product/13827/49693/products/skincare/travel-size/lipstick-little-mac#/shade/Velvet_Teddy";
		Map<String, Object> payload = new HashMap<>();
		String replacePattern = "[{\"method\":\"prodcat.querykey\",\"params\":[{\"products\":[\"PROD49693\"],\"query_key\":\"catalog-mpp-volatile\"}],\"id\":1}]";
		payload.put("JSONRPC", replacePattern);
		System.out.println(replacePattern);
		String content = Crawler.create().timeOut(15000).url("https://www.maccosmetics.com/rpc/jsonrpc.tmpl").header(getHeaders(url)).payload(payload)
				.method(HttpMethod.POST.getValue()).proxy(true).proxyAddress("104.196.30.199").proxyPort(22088)
				.resultAsString();
		System.out.println(content);		
	}
}
