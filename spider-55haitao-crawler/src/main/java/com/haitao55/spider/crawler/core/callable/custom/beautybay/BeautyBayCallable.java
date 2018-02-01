package com.haitao55.spider.crawler.core.callable.custom.beautybay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

class BeautyBayCallable implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String skuId;
	private Url url;
	private String path;
	@SuppressWarnings("serial")
	private static final Map<String, Object> headers = new ConcurrentHashMap<String, Object>() {
		{
			put("X-Requested-With", "XMLHttpRequest");
			put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			put("Cookie", "salesCode=Uk; currencyCode=GBP; RegionSetting=CurrencyId=1&DeliveryId=76&LanguageId=0&EnableCookie=True;");
		}
	};

	public BeautyBayCallable(String skuId, Url url, String path) {
		this.skuId = skuId;
		this.url = url;
		this.path = path;
	}

	@Override
	public JSONObject call() {
		JSONObject jsonObject = new JSONObject();
		Map<String, Object> params = new ConcurrentHashMap<String, Object>();
		try {
			String content = StringUtils.EMPTY;
			String proxyRegionId = url.getTask().getProxyRegionId();
			params.put("SelectedColourSku", skuId);
			 if(StringUtils.isBlank(proxyRegionId)){
				 content = Crawler.create().timeOut(15000).header(headers).payload(params).url(path).method(HttpMethod.POST.getValue()).proxy(false).resultAsString();
			 }else{
				 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
						 true);
				 String proxyAddress=proxy.getIp();
				 int proxyPort=proxy.getPort();
				 content = Crawler.create().timeOut(15000).header(headers).payload(params).url(path).proxy(true).method(HttpMethod.POST.getValue()).proxyAddress(proxyAddress)
						 .proxyPort(proxyPort).resultAsString();
			 }
			
			if (StringUtils.isNotBlank(content)) {
				jsonObject = JSONObject.parseObject(content);
				String salePrice = jsonObject.getString("Price");
				String origPrice = jsonObject.getString("WasPrice");

				String save = StringUtils.EMPTY;

				String unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[£,$ ]", "");

				if (StringUtils.isBlank(replace(origPrice))) {
					origPrice = salePrice;
				}
				origPrice = origPrice.replaceAll("[£,$ ]", "");
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

				JSONObject productDetail = jsonObject.getJSONObject("ProductDetail");
				Boolean instock = productDetail.getBoolean("InStock");

				List<Image> pics = new ArrayList<Image>();

				JSONArray imgeJSONArray = jsonObject.getJSONArray("Gallery");
				if (null != imgeJSONArray && imgeJSONArray.size() > 0) {
					for (Object object : imgeJSONArray) {
						JSONObject imageJSONObject = (JSONObject) object;
						String imageurl = imageJSONObject.getString("Large");
						imageurl = replace_imgurl(imageurl);
						if (StringUtils.isNotBlank(imageurl)) {
							pics.add(new Image(imageurl));
						}
					}
				}

				// sku data package
				jsonObject.put("salePrice", Float.valueOf(salePrice));
				jsonObject.put("origPrice", Float.valueOf(origPrice));
				jsonObject.put("save", Integer.parseInt(save));
				jsonObject.put("unit", unit);
				jsonObject.put("instock", instock);
				jsonObject.put("pics", pics);
				jsonObject.put("skuId", skuId);
			}
		} catch (Exception e) {
			logger.error("BeautyBayCallable get sku data error", e);
		}
		return jsonObject;
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

	public String getText(Elements es) {
		if (es != null && es.size() > 0) {
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}

	public String getAttr(Elements es, String attrKey) {
		if (es != null && es.size() > 0) {
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	private String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

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

}
