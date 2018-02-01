package com.haitao55.spider.crawler.core.callable.custom.neimanmarcus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Task;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;
import sun.misc.BASE64Encoder;

/**
 * neimanmarcus 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月15日 下午1:49:59
 * @version 1.0
 */
public class Neimanmarcus extends AbstractSelect {
    
    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
    public static final String AJAX_SERVICE_URL = "http://www.neimanmarcus.com/en-cn/ajax.service?instart_disable_injection=true ";
	public static final String SERVICE_URL = "http://www.neimanmarcus.com/product.service?instart_disable_injection=true";
	public static final String COMMON_DATA = "{\"ProductSizeAndColor\":{\"productIds\":\"prod()\"}}";
	public static final String INSTOCK = "In Stock";
	public static final String IMAGE_URL = "http://neimanmarcus.scene7.com/is/image/NeimanMarcus/{}_m?&wid=1200&height=1500";
	public static final String IMAGE_PREFFIX = "http://images.neimanmarcus.com";
	public static final String DOMAIN = "www.neimanmarcus.com";

	private static Map<String,Object> getHeaders2(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/61.0.3163.100 Chrome/61.0.3163.100 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.neimanmarcus.com");
		headers.put("Cookie", "ctm={'pgv':4490201203174407|'vst':2006557924053684|'vstr':7798205533126455|'intr':1500028046978|'v':1|'lvst':10098}; TLTSID=F110DAD8B7A410B7DEB180C804F11A7A; TLTUID=F110DAD8B7A410B7DEB180C804F11A7A; W2A=3222077450.10330.0000; CChipCookie=2030108682.61525.0000; TS01a14dbc=01d3b151d520356ac73ee9bb9c78597f8d036e07f51a26aae92ca39d36ba8e36438541bb75920d8e55bdb8ccb46a10332fe1190547d4f15306715afc9917caef96eff8f136493b9fb6a4e23bfef69508acaaaa8444; i10c.sid=1508730471714; sr_cr4_exp=2; sr_mc_exp=2; ToolTip1=/category/myfavorites/AddToFavorites-V4.html%7C2; sr_browser_id=44afcd15-0fae-4a50-a839-4500290d5600; prod161420017=NaN|95|1012~prod161420017~131~prod161420017~1113~~1112~~140~1~141~95.00; prod161430011=NaN|65|1012~prod161430011~131~prod161430011~1113~~1112~~140~1~141~65.00; DYN_USER_ID=18380900806; DYN_USER_CONFIRM=8be649e4aade3959b30928250adf1cd5; page_number=1; s_pers=%20productnum%3D1%7C1511322629316%3B; ipdp=ATG8; sr_exp_ssc=0; JSESSIONID=p4pshNi5rXqSKFWywNOAv2Gn; dt_personalize_data=%7B%22lastUpdatedDate%22%3A%22Mon+Oct+23+00%3A44%3A28+CDT+2017%22%2C%22bestCustomer%22%3A%22n%22%2C%22plcc%22%3A%22n%22%2C%22customerJourneySegment%22%3A%220%22%2C%22inCircleLevel%22%3A%220%22%2C%22promos%22%3A%5B%5D%2C%22customerScore%22%3A%220.08%22%2C%22emailSubscriber%22%3A%22n%22%7D; dt_favorite_store=\"\"; profile_data=%7B%22currencyPreference%22%3A%22USD%22%2C%22countryPreference%22%3A%22US%22%2C%22securityStatus%22%3A%22Anonymous%22%2C%22cartItemCount%22%3A2%7D; tms_data={DT-2017.03}a3HwxssoiZzaMm5Pj2Bv6L13Gv8Ad/WZkJm2zLBiJ23IkTynauoH3eSO/dVQy1wxuW7LWrxg/pbmtNrowk1Fh4zdC+THEm0891EES7+4ZvKx7Ah1VDCfGK4+JFiaIExQL4B1FS86Y9qSnBwxm6LaHQfckQkH0E/O276LS9pM3mn8YkZW478e3M6AH7ufLbAOsqaDFq15UbPPf5QesAKhjf+sqLsVWyaB1xGJX5nIaEHyjb4U9RpYAc2hfynX90Fx4aZZclaffm1jHrx0UQH7bVPWqV87QCrTRJ7CXsqxgP/RPsoflV3rPx7eex+OqUYjC+TqNXlQ9GzLzpcXIwSncQLGwYHvGCt9owPoEzZpjCHLui0xfzEbwY7h285T5FFsQ2fo0AzUR8k4sDP962VGcqJEFdUcJEkGQvLEwRdqyqEk9o+TrY0dMEG07sq2hxgx7jDkpmeuSHsXIOe7PlwQumgtmOBIW4RzT7+BsQASRtJB3UNQuUBYRDV5lmEOfW6jGtLRESNsxJKsiUdMeezw7MHtplUxwMQL207oSu4cUdHgC7ShQ6uWq4dYTkebJ9S09+0p/Pil9hPENsJoAaP2PCnp+9kOwUHpli3UBaJPmZIzr/8b23kAncW/XmLNijqOu/R1aCLBwDeIBl1aaCZVadcvW1it/ytGzBwh/Lt1M99Xl7wvGtkfKksqirVSxWjKiwRyQbKFreLiXMM3ImsZf7/ZYJOZewDzwD5u8Bs8A4kBwkayxgl2ijxTqNpMqqLYeaLHR2jEAr1IbKzn0sMVxmPx8uqUkCcDZ02zvDMp34/fQImT/J3JRzJ6cH94DbSuHldCVGSHEo123jqexOQd/6lXbkjt1x4cwjtlq0S6zUMe+wx7Wc0Rj4U4D/IqdmYb; pomni=DT; mbox=PC#1508730475837-783320.28_69#1516513475|check#true#1508737535|session#1508737474607-363754#1508739335; firstTimeUser=here; rr_rcs=eF4NxLsNgDAMBcAmFbtYcrBf7GzAGvk4EgUdMD9ccSk941g1lqF1suKZlKG09_gbwOrRSou5Xe99ThVxymA3YTWHCKnRXivjA95vEtQ; mp_neiman_marcus_mixpanel=%7B%22distinct_id%22%3A%20%2215f475824849fd-09b41607404f07-29054e75-100200-15f47582485927%22%7D; s_fid=3C61FE0284E25655-29912A64D9EE4AEB; s_cc=true; utag_main=v_id:015a36a67d8b0066a4c93323983402085011b07d0086e$_sn:10$_ss:1$_st:1508739278289$_pn:1%3Bexp-session$ses_id:1508737475596%3Bexp-session$_prevpage:product%20detail%3Bexp-1508741077413; _br_uid_2=uid%3D6110756001129%3Av%3D11.8%3Ats%3D1487851094984%3Ahc%3D16; br_ab=A; br_df=D; s_vi=[CS]v1|2C4D6C5E850110FA-4000010820025841[CE]; __CT_Data=gpv=16&apv_9593_www09=12&cpv_9593_www09=12&rpv_9593_www09=9; _uetsid=_uet3bdc3fba; _ga=GA1.2.471316892.1490683751; _gid=GA1.2.891760431.1508730485; _loop_ga=GA1.2.a743be8e-e8fc-463f-a69f-a8d45f925712; _loop_ga_gid=GA1.2.144864578.1508730487; load_times=8.03_15.69; QuantumMetricUserID=e401ad316d4e91f73120dff7486bfb7b; QuantumMetricSessionID=096156f5ede45482a1a72dda256f22b4; AGA=");
		return headers;
	}
	
	@SuppressWarnings("unchecked")
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String param = getServiceUrlRequestParamData(url);
		String service_content = crawler_package(context, getPayload(param), getHeaders(), SERVICE_URL,
				HttpMethod.POST.getValue());

		//String content = crawler_package(context, null, null, url, HttpMethod.GET.getValue());
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).header(getHeaders2()).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).header(getHeaders2()).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		// String content = StringUtils.EMPTY;
		String countryCode = StringUtils.substringBetween(content, "countryCode = '", "',");
		logger.info("url {} countryCode {}",url,countryCode);
		
		Document doc = JsoupUtils.parse(content);

		JSONObject parseObject = JSONObject.parseObject(service_content);

		JSONObject jsonObject = parseObject.getJSONObject("ProductSizeAndColor");
		if (null != jsonObject && !jsonObject.isEmpty()) {
			RetBody retBody = new RetBody();
			Sku sku = new Sku();

			// spu stock status
			int spu_stock_status = 0;
			// spu stock status
			JSONObject spuStockJSONObject = new JSONObject();
			// productId
			String productId = jsonObject.getString("productIds");
			// title
			JSONObject titleJSONObject = new JSONObject();
			// brand
			String brand = StringUtils.EMPTY;
			Elements brandElements = doc.select("span[itemprop=brand]");
			if (CollectionUtils.isNotEmpty(brandElements)) {
				brand = brandElements.get(0).text();
			}
			// default color jsonobject
			JSONObject defaultColorJSONObject = new JSONObject();
			// sku jsonarray
			JSONArray skuJSONArray = new JSONArray();
			sku_jsonarray_package(skuJSONArray, jsonObject, titleJSONObject, defaultColorJSONObject,
					spuStockJSONObject);

			// default color
			String default_color = defaultColorJSONObject.getString("default_color");
			// title
			String title = titleJSONObject.getString("title");

			// price jsonobject package
			JSONObject priceJSONObject = new JSONObject();
			price_jsonobject_package(doc, priceJSONObject,context);

			// image package
			JSONObject imageJSONObject = new JSONObject();
			// switch image
			JSONObject swicthImageJSONObject = new JSONObject();
			image_jsonobject_package(doc, imageJSONObject, swicthImageJSONObject);

			// sku iterator
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// style
			JSONObject styleJsonObject = new JSONObject();

			if (null != skuJSONArray && skuJSONArray.size() > 0) {
				for (Object object : skuJSONArray) {
					JSONObject skuJsonObejct = (JSONObject) object;

					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJsonObejct.getString("skuId");

					// color
					String color = skuJsonObejct.getString("color");

					// size
					String size = skuJsonObejct.getString("size");

					// stock
					int stock_status = skuJsonObejct.getIntValue("stock_status");
					int stock_number = 0;
					if (stock_status > 0) {
						spu_stock_status = 1;
					}

					// style_id
					String style_id = color;

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					if (StringUtils.isNotBlank(size)) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(size);
						selections.add(selection);
					}

					// lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(priceJSONObject.getFloatValue("orign_price"));
					lselectlist.setSale_price(priceJSONObject.getFloatValue("sale_price"));
					lselectlist.setPrice_unit(priceJSONObject.getString("unit"));
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style json
					styleJsonObject.put(style_id, skuJsonObejct);
				}
				boolean style_display = false;
				// stylelist
				if (!styleJsonObject.isEmpty()) {
					for (Map.Entry<String, Object> entry : styleJsonObject.entrySet()) {
						String style_id = entry.getKey();

						JSONObject styleJSONObject = (JSONObject) entry.getValue();

						// stylelist
						LStyleList lStyleList = new LStyleList();
						// skuId
						String skuId = styleJSONObject.getString("skuId");

						// color
						String color = styleJSONObject.getString("color");
						if (null != default_color) {
							if (StringUtils.containsIgnoreCase(
									StringUtils.upperCase(StringUtils.replacePattern(default_color, "[ ]", "")),
									StringUtils.upperCase(StringUtils.replacePattern(style_id, "[ ]", "")))) {
								lStyleList.setDisplay(true);
							}
						} else {
							if (!style_display) {
								lStyleList.setDisplay(true);
								style_display = true;
							}
						}

						// switch_img
						String switch_img = swicthImageJSONObject
								.getString(StringUtils.upperCase(StringUtils.replacePattern(color, "[ ]", "")));
						if (StringUtils.isBlank(switch_img)) {
							switch_img = StringUtils.EMPTY;
						}

						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(style_id);

						// images
						List<Image> sku_pics = (List<Image>) imageJSONObject
								.get(StringUtils.upperCase(StringUtils.replacePattern(color, "[ ]", "")));
						if (null == sku_pics || sku_pics.size() == 0) {
							sku_pics = (List<Image>) imageJSONObject.get("default");
						}

						context.getUrl().getImages().put(skuId, sku_pics);
						// l_style_list
						l_style_list.add(lStyleList);

					}
				}
			}
			// 单品
			else {
				List<Image> sku_pics = (List<Image>) imageJSONObject.get("default");
				context.getUrl().getImages().put(productId, sku_pics);
				// stock
				spu_stock_status = spuStockJSONObject.getIntValue("stock_status");

			}
			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			retBody.setSku(sku);

			// stock
			retBody.setStock(new Stock(spu_stock_status));

			// price
			retBody.setPrice(
					new Price(priceJSONObject.getFloatValue("orign_price"), priceJSONObject.getIntValue("save"),
							priceJSONObject.getFloatValue("sale_price"), priceJSONObject.getString("unit")));

			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			;

			// title
			retBody.setTitle(new Title(title, "", "", ""));

			// category breadcrumb
			category_package(content, brand, retBody,title);
			// description
			desc_package(doc, retBody);
			//
			// //properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			retBody.setProperties(propMap);
			//System.out.println("retBody:"+retBody.parseTo()); 
			setOutput(context, retBody);
		}

	}

	/**
	 * sku jsonarray 封装
	 * 
	 * @param skuJSONArray
	 * @param jsonObject
	 * @param defaultColorJSONObject
	 * @param titleJSONObject
	 * @param spuStockJSONObject
	 */
	private static void sku_jsonarray_package(JSONArray skuJSONArray, JSONObject jsonObject, JSONObject titleJSONObject,
			JSONObject defaultColorJSONObject, JSONObject spuStockJSONObject) {
		String sku_data = jsonObject.getString("productSizeAndColorJSON");
		JSONArray parseArray = JSONObject.parseArray(sku_data);
		if (null != parseArray && parseArray.size() > 0) {
			JSONObject skuJSONObject = (JSONObject) parseArray.get(0);
			String title = skuJSONObject.getString("productName");
			titleJSONObject.put("title", title);
			if (null != skuJSONObject && !skuJSONObject.isEmpty()) {
				JSONArray jsonArray = skuJSONObject.getJSONArray("skus");
				if (null != jsonArray && jsonArray.size() > 0) {
					for (Object object : jsonArray) {
						// 接收解析数据 jsonobject
						JSONObject jsonObjectToAccept = new JSONObject();

						JSONObject jsonObjectToParse = (JSONObject) object;

						// is default color
						boolean default_color = jsonObjectToParse.getBooleanValue("defaultSkuColor");
						// color
						String color = jsonObjectToParse.getString("color");
						if (StringUtils.isNotBlank(color)) {
							color = StringUtils.substringBefore(color, "?");
						}
						if (default_color) {
							defaultColorJSONObject.put("default_color",
									StringUtils.upperCase(StringUtils.replacePattern(color, "[ ]", "")));
						}
						// size
						String size = jsonObjectToParse.getString("size");

						// skuId
						String skuId = jsonObjectToParse.getString("sku");
						// stock status
						int stock_status = 0;
						String stock_flag = jsonObjectToParse.getString("stockLevel");
						if (StringUtils.isNotBlank(stock_flag) && Integer.valueOf(stock_flag) > 0) {
							stock_status = 1;
						}
						// 默认单品 不封装 skujsonarray
						if (null == color && null == size) {
							spuStockJSONObject.put("stock_status", stock_status);
							break;
						}

						jsonObjectToAccept.put("skuId", skuId);
						jsonObjectToAccept.put("color", color);
						jsonObjectToAccept.put("size", size);
						jsonObjectToAccept.put("stock_status", stock_status);
						skuJSONArray.add(jsonObjectToAccept);
					}
				}
			}
		}
	}

	/**
	 * price 封装
	 * 
	 * @param doc
	 * @param priceJSONObject
	 */
	private static void price_jsonobject_package(Document doc, JSONObject priceJSONObject,Context context) {
		Elements orignPriceElements = doc.select("div.price-adornments-elim-suites");
		Elements salePriceElements = doc.select("input[name=sale0]");
		String sale_price = salePriceElements.attr("value");
		logger.info("url {} ,salePrice {}",context.getCurrentUrl(),sale_price);
		String orign_price = StringUtils.EMPTY;
		String save = StringUtils.EMPTY;
		String unit = StringUtils.substringBetween(doc.toString(), "order_currency_code\":\"", "\",");
		if(!StringUtils.equals(unit, "USD")){
            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" Neimanmarcus-price-unit-is-not-USD...");
        }
		/*if(StringUtils.isBlank(unit)){
			unit = "USD";
		}*/
		if (CollectionUtils.isNotEmpty(orignPriceElements)) {
			orign_price = orignPriceElements.select("span[class=item-price]").text();
			logger.info("url {} , orign_price {}",context.getCurrentUrl(),orign_price);
		} else {
			orign_price = salePriceElements.attr("value");
		}
		
		Elements lastSelfPriceElements = doc.select("p.line-item-promo-elim-suites span[itemprop=price]");
		if (CollectionUtils.isNotEmpty(lastSelfPriceElements)) {
			sale_price = lastSelfPriceElements.text();
		}

		sale_price = sale_price.replaceAll("[$,  ]", "");
		orign_price = orign_price.replaceAll("[$,  ]", "");
		if(StringUtils.contains(sale_price, "USD")){
		    sale_price = sale_price.replace("USD", "");
		}
		if(StringUtils.contains(orign_price, "USD")){
		    orign_price = orign_price.replace("USD", "");
        }
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
		priceJSONObject.put("sale_price", sale_price);
		priceJSONObject.put("orign_price", orign_price);
		priceJSONObject.put("save", save);
		priceJSONObject.put("unit", unit);

	}

	/**
	 * image jsonobject 封装
	 * 
	 * @param doc
	 * @param imageJSONObject
	 * @param swicthImageJSONObject
	 */
	private static void image_jsonobject_package(Document doc, JSONObject imageJSONObject,
			JSONObject swicthImageJSONObject) {
		Elements colorElements = doc.select("ul#color-pickers li");
		List<Image> otherImages = new ArrayList<Image>();
		if (CollectionUtils.isNotEmpty(colorElements)) {
			Elements otherImageElements = doc.select("ul[class=list-inline] li:not(:first-child)");
			other_image_package(otherImages, otherImageElements);
			for (Element element : colorElements) {
				List<Image> images = new ArrayList<Image>();
				Elements select = element.select("a img");
				if(CollectionUtils.isEmpty(select)){
					select = element.select("img");
				}
				String color_image_param = select.attr("data-color-img-params");
				String color = select.attr("title");
				if (StringUtils.isNotBlank(color_image_param)) {
					color_image_param = StringUtils.substringBetween(color_image_param, "src=", "&");
					String image_url = StringUtils.replacePattern(IMAGE_URL, "\\{\\}", color_image_param);
					images.add(new Image(image_url));
				}
				// images addall
				if (CollectionUtils.isNotEmpty(otherImages)) {
					images.addAll(otherImages);
				}
				imageJSONObject.put(StringUtils.upperCase(StringUtils.replacePattern(color, "[ ]", "")), images);
				// switch image
				String switch_image = select.attr("src");
				if (!StringUtils.contains(switch_image, "http")) {
					switch_image = IMAGE_PREFFIX + switch_image;
				}
				swicthImageJSONObject.put(StringUtils.upperCase(StringUtils.replacePattern(color, "[ ]", "")),
						switch_image);
			}
		} else {
			List<Image> images = new ArrayList<Image>();
			Elements otherImageElements = doc.select("ul[class=list-inline] li");
			other_image_package(images, otherImageElements);
			if (CollectionUtils.isEmpty(otherImageElements)) {
				Elements select = doc.select("div.img-wrap img");
				if (CollectionUtils.isNotEmpty(select)) {
					for (Element element : select) {
						String attr = element.attr("data-zoom-url");
						if (!StringUtils.contains(attr, "http")) {
							attr = "http:" + attr;
						}
						images.add(new Image(attr));
					}
				}
			}
			imageJSONObject.put("default", images);
		}

	}

	/**
	 * 图片封装
	 * 
	 * @param otherImages
	 * @param otherImageElements
	 */
	private static void other_image_package(List<Image> otherImages, Elements otherImageElements) {
		if (CollectionUtils.isNotEmpty(otherImageElements)) {
			for (Element element : otherImageElements) {
				Elements select = element.select("div img");
				String attr = select.attr("data-zoom-url");
				if (!StringUtils.contains(attr, "http")) {
					attr = "http:" + attr;
				}
				otherImages.add(new Image(attr));
			}
		}
	}

	/**
	 * 获取请求参数接口的参数 data
	 * 
	 * @param url
	 * @return
	 */
	private static String getServiceUrlRequestParamData(String url) {
		if (StringUtils.isNotBlank(url)) {
			String productId = StringUtils.substringBetween(url, "prod", "_");
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringBetween(url, "prod", "/");
			}
			String data = StringUtils.replacePattern(COMMON_DATA, "\\(\\)", productId);
			String base64Str = base64Encode(data);
			base64Str = "$b64$" + base64Str;
			return base64Str;
		}
		return null;
	}

	/**
	 * base编码
	 * 
	 * @param data
	 * @return
	 */
	private static String base64Encode(String data) {
		BASE64Encoder encoder = new BASE64Encoder();
		String encode = encoder.encode(data.getBytes());
		return encode;
	}

	/**
	 * category 封装
	 * 
	 * @param content
	 * @param brand
	 * @param retBody
	 */
	private static void category_package(String content, String brand, RetBody retBody,String title) {
		String data = StringUtils.substringBetween(content, "window.utag_data=", "};");
		data = data+"}";
		JSONObject jsonObject = JSONObject.parseObject(data);
		if (null != jsonObject && !jsonObject.isEmpty()) {
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			JSONArray jsonArray = jsonObject.getJSONArray("bread_crumb");
			if (null != jsonArray && jsonArray.size() > 0) {
				for (Object object : jsonArray) {
					String cat = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(object.toString()));
					if (StringUtils.isNotBlank(cat)) {
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
			
			if(CollectionUtils.isEmpty(cats)){
				cats.add(title);
			}
			retBody.setCategory(cats);
			// BreadCrumb
			if (StringUtils.isNotBlank(brand)) {
				breads.add(brand);
			}
			retBody.setBreadCrumb(breads);
		}
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
		Elements es = doc.select("p#MpsShortSkuDeskTop,div.productCutline ul li ,div.aboutDesignerCopy");
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

	private Map<String, Object> getPayload(String param) {
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("data", param);
		payload.put("sid", "getSizeAndColorData");
		payload.put("bid", "ProductSizeAndColor");
		return payload;
	}

	private Map<String, Object> getHeaders() {
		Map<String, Object> headers = new HashMap<String, Object>();
		//原先是赵新落开发的，2017.5.23发现有CAD、SGD、CNY多币种出现，怀疑是如下代码导致的。所以暂时注释掉。
		//徐松松 2017.5.24 今后还需要进一步优化。
		//headers.put("Cookie","D_SID=192.243.119.27:ktv05/kM5944hhpHeBWoVQNg/u+tgjfEpcwSc3mjMl8; D_UID=00DF1175-B02B-3AB1-9E0F-E825739FB2FC; D_HID=TRqZifCO2NfBKlXLVOFDX5Aq7WFPuaR+0z5LPSPhmVs;");
		headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		return headers;
	}

	private String crawler_package(Context context,Map<String, Object> payload, Map<String, Object> headers, 
			String url, String method) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		Task task = context.getUrl().getTask();
		if(task != null){
		    String proxyRegionId = task.getProxyRegionId();
		    if (StringUtils.isBlank(proxyRegionId)) {
		        logger.info("do not use proxy ,url {}",url);
		        if(null!= payload && null != headers){
		            content = Crawler.create().timeOut(15000).url(url).payload(payload).header(headers).method(method)
		                    .resultAsString();
		        }else{
		            content = Crawler.create().timeOut(15000).url(url).method(method)
		                    .resultAsString();
		        }
		    } else {
		        Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
		        String proxyAddress = proxy.getIp();
		        int proxyPort = proxy.getPort();
		        logger.info("use proxy proxyAddress {} proxyPort {},url {}",proxyAddress,proxyPort,url);
		        if(null!= payload && null != headers){
		            content = Crawler.create().timeOut(15000).url(url).payload(payload).header(headers).method(method)
		                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		        }else{
		            content = Crawler.create().timeOut(15000).url(url).method(method)
		                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		        }
		    }
		}
		/*if(null!= payload && null != headers){
            content = Crawler.create().timeOut(15000).url(url).payload(payload).header(headers).method(method)
                    .proxy(true).proxyAddress("128.199.118.208").proxyPort(3128).resultAsString();
        } else {
            content = Crawler.create().timeOut(15000).url(url).method(method)
                    .proxy(true).proxyAddress("128.199.118.208").proxyPort(3128).resultAsString();
        }*/
		return content;
	}
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
	    Neimanmarcus neiman = new Neimanmarcus();
	    Context context = new Context();
        Url url = new Url("http://www.neimanmarcus.com/Kenzo-Light-Brushed-Cotton-Eye-Sweatshirt-Light-Gray/prod189230069_cat64720744__/p.prod");
        context.setUrl(url);
        context.setCurrentUrl(url.getValue());
	    neiman.invoke(context);
  }
}
