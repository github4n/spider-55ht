package com.haitao55.spider.crawler.core.callable.custom.beautybay;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class BeautyBay extends AbstractSelect {
	private static final String stockFlag = "Out of Stock";
	@SuppressWarnings("serial")
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
		
		Pattern p = Pattern.compile("This product has restrictions and can");
		Matcher m = p.matcher(content);
		if (m.find()) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"beautybay.com itemUrl:" + context.getUrl().toString() + " This product has restrictions and can't be sent to your shipping country..");
		}
		
		Document doc = JsoupUtils.parse(content);
		int stock_status = 1;

		boolean spu_has_stock = false;

		boolean default_sku_flag = false;

		Elements elements = doc.select("ul.swatch-list.clearfix li");
		// 封装sku json
		JSONObject skuJson = new JSONObject();

		List<String> skuIdList = new ArrayList<String>();

		if (CollectionUtils.isNotEmpty(elements)) {
			for (Element element : elements) {
				Element select = element.select("img").get(0);
				// 封装每个sku的对应ｖａｌｕｅ值
				JSONObject skuValue = new JSONObject();
				String skuId = select.attr("data-sku");
				String options = element.attr("data-tip");
				String switch_img = select.attr("src");
				switch_img = replace_imgurl(switch_img);
				skuValue.put("Options", options);
				skuValue.put("switch_img", switch_img);
				skuJson.put(skuId, skuValue);
				skuIdList.add(skuId);
			}
		} else {
			elements = doc.select("select#group-sku option:not(:first-child)");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					// 封装每个sku的对应ｖａｌｕｅ值
					JSONObject skuValue = new JSONObject();
					String skuId = element.attr("value");
					String text = element.text();
					String options = StringUtils.substringBefore(text, "-");
					options = StringUtils.substringBefore(text, "–");
					options = options.replaceAll("[ ]", "");
					String switch_img = StringUtils.EMPTY;
					skuValue.put("Options", options);
					skuValue.put("switch_img", switch_img);
					skuJson.put(skuId, skuValue);
					skuIdList.add(skuId);
				}

			}
		}

		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();

		// single item
		if (null == skuJson || skuJson.size() == 0) {
			List<Image> pics = new ArrayList<Image>();
			elements = doc.select("ul.product-gallery.clearfix li a:has(img)");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String imageUrl = element.attr("href");
					imageUrl = replace_imgurl(imageUrl);
					if (StringUtils.isNotBlank(imageUrl)) {
						imageUrl = StringUtils.trim(imageUrl);
					}
					Image image = new Image(imageUrl);
					pics.add(image);
				}
			}else{
				elements = doc.select("div.image-container__inner.js-media a:has(img)");
				if (CollectionUtils.isNotEmpty(elements)) {
					for (Element element : elements) {
						String imageUrl = element.attr("href");
						imageUrl = replace_imgurl(imageUrl);
						if (StringUtils.isNotBlank(imageUrl)) {
							imageUrl = StringUtils.trim(imageUrl);
						}
						Image image = new Image(imageUrl);
						pics.add(image);
					}
				}
			}
			
			context.getUrl().getImages().put(context.getCurrentUrl(),pics);
			String salePrice = StringUtils.EMPTY;
			String origPrice = StringUtils.EMPTY;
			String save = StringUtils.EMPTY;
			elements = doc.select("span.js-price.qa-price");
			if (CollectionUtils.isNotEmpty(elements)) {
				salePrice = elements.get(0).text();
			}
			elements = doc.select("div.c-pp-info__previous-price.js-was-price");
			if (CollectionUtils.isNotEmpty(elements)) {
				origPrice = elements.get(0).text();
			}

			String unit = getCurrencyValue(salePrice);// 得到货币代码
			salePrice = salePrice.replaceAll("[£,$　]", "");

			if (StringUtils.isBlank(replace(origPrice))) {
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[£,$　]", "");
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

			context.put("Price", new Price(Float.valueOf(origPrice), Integer.parseInt(save),
					 Float.valueOf(salePrice), unit));

			elements = doc.select("div[itemprop=availability]");

			if (CollectionUtils.isNotEmpty(elements)) {
				String stockStatus = elements.get(0).text();
				if (stockFlag.equals(stockStatus)) {
					stock_status = 0;
				}

			}
		}
		// sku list
		else {
			
//			String path=context.getc
			JSONArray skuArray = new BeautyBayHandler().process(skuIdList, context.getUrl(),context.getCurrentUrl().toString());
			if (null != skuArray && skuArray.size() > 0) {
				for (Object object : skuArray) {
					JSONObject jsonObject = (JSONObject) object;
					LSelectionList lselectlist = new LSelectionList();
					String skuId = jsonObject.getString("skuId");
					float orignPrice = jsonObject.getFloatValue("origPrice");
					float salePrice = jsonObject.getFloatValue("salePrice");
					String unit = jsonObject.getString("unit");
					int save = jsonObject.getIntValue("save");
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(orignPrice);
					lselectlist.setPrice_unit(unit);
					lselectlist.setSale_price(salePrice);
					// lookfantastic not find number
					lselectlist.setStock_number(0);

					int sku_stock_status = 0;
					if (jsonObject.getBooleanValue("instock")) {
						sku_stock_status = 1;
						// 只要有一个ｓｋｕ 有库存， spu库存就!=0
						spu_has_stock = true;
					}
					if(!spu_has_stock){
						stock_status=0;
					}
					
					lselectlist.setStock_status(sku_stock_status);

					List<Selection> selections = new ArrayList<Selection>();

					// selectlist
					JSONObject skuValue = skuJson.getJSONObject(skuId);
					String style_id = skuValue.getString("Options");
					lselectlist.setStyle_id(style_id);

					lselectlist.setSelections(selections);
					l_selection_list.add(lselectlist);

					// stylelist
					LStyleList lStyleList = new LStyleList();
					if (!default_sku_flag) {
						context.put("Price",new Price(orignPrice,save,salePrice,unit));
						lStyleList.setDisplay(true);
						default_sku_flag = true;
					}
					lStyleList.setGood_id(skuId);
					
					String switch_img=skuValue.getString("switch_img");
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(style_id);
					lStyleList.setStyle_cate_name("Options");
					lStyleList.setStyle_name(style_id);
					l_style_list.add(lStyleList);
					
					Object picsObject = jsonObject.get("pics");
					@SuppressWarnings({ "unchecked", "rawtypes" })
					List<Image> pics=(List)picsObject;
					context.getUrl().getImages().put(skuId, pics);
				}
			}

		}
		context.put("Stock", new Stock(stock_status));
		
		Sku sku = new Sku();
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		context.put("Sku", sku);
		
		// description
		String desc=doc.select("dd#product-description").text();
		
		
		context.put("Description.en", new HashMap<String,Object>(){
			{
				put("en",desc);
			}
		});
		Map<String, Object> featureMap = new HashMap<String, Object>();
		elements= doc.select("dd#product-description p");
		int count = 1;
		if (elements != null && elements.size() > 0) {
			for (Element e : elements) {
				featureMap.put("feature-" + count, e.text());
				count++;
			}
		}
		elements= doc.select("dd#product-description li");
		if (elements != null && elements.size() > 0) {
			for (Element e : elements) {
				featureMap.put("feature-" + count, e.text());
				count++;
			}
		}
		context.put("FeatureList", featureMap);
		
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/53.0.2785.143 Chrome/53.0.2785.143 Safari/537.36");
		 headers.put("Cookie", "salesCode=Uk; currencyCode=GBP; RegionSetting=CurrencyId=1&DeliveryId=76&LanguageId=0&EnableCookie=True; ");
		return headers;
	}

	/**
	 * img src //a0.bb-cdn.com/assets/swatches/ansu0317f_sw.png?version=668
	 * -->http://a0.bb-cdn.com/assets/swatches/ansu0317f_sw.png?version=668
	 * 
	 * @param img
	 * @return
	 */
	private static String replace_imgurl(String img) {
		String url = StringUtils.EMPTY;
		if (StringUtils.isBlank(img)) {
			return url;
		}
		if (!StringUtils.containsIgnoreCase(img, "http:")) {
			url = "http:".concat(img);
		}
		if (!StringUtils.containsIgnoreCase(img, ".jpg")) {
			return url;
		}
		return url;
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
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.codeOf(currency).name();
		return unit;

	}

	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

	}
}
