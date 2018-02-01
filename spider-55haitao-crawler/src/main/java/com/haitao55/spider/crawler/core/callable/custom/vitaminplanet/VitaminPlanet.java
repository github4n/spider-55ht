package com.haitao55.spider.crawler.core.callable.custom.vitaminplanet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class VitaminPlanet extends AbstractSelect {
	private static final String domain = "www.vitaminplanet.cn";

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Connection", "keep-alive");
		return headers;
	}	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String content = "";
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		Proxy proxy = null;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		if(content.toLowerCase().contains("gold collagen")){
			content.replaceAll("gold collagen", "");
		}	
		Document doc = JsoupUtils.parse(content);

		RetBody retBody = new RetBody();
		// DOCID & Site & ProdUrl
		String ProductId = StringUtils.substringBetween(content, "var liProductId = '", "';").trim();
		if (StringUtils.isNotBlank(ProductId)) {
			String docid = SpiderStringUtil.md5Encode(domain.concat(ProductId));
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(domain));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		}
		// Brand & Title
		String pageSize = doc.select("div.mid input#hndBrandMenuTotalCount").get(0).attr("value"); 
		String title = doc.select("div.prod_content h3").html();
		String brand = null;
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("fiPageNo", "1");
		payload.put("fiPageSize", pageSize);
		payload.put("fsSortOrder", "inDisplayOrder asc");
		String html = Crawler.create().timeOut(30000).url("https://www.vitaminplanet.cn/cow-gate-br-114.aspx").method(HttpMethod.POST.getValue())
				.payload(payload).resultAsString();
		Document  brandDoc = Jsoup.parse(html);
		Elements es = brandDoc.select("div h4 a img");
		for(Element e : es){
			String brandName = e.attr("src").split("Brand_")[1].split(".jpg")[0];	
			if(title.toLowerCase().contains(brandName.toLowerCase())){
				brand = brandName;
				break;
			}
		}	
		if(StringUtil.isBlank(brand)){
			brand = title.split(" ")[0];
		}
		if (StringUtils.isNotBlank(title)) {		
			retBody.setBrand(new Brand(brand, "", "", ""));
			retBody.setTitle(new Title(title, "", "", ""));
		}
		// Description
		String description = doc.select("div.prod_content p.MsoNormal span").html();
		if (StringUtils.isNotBlank(description)) {
			Map<String, Object> descMap = new HashMap<String, Object>();
			descMap.put("en", description);
			retBody.setDescription(descMap);
		}
		// Price
		// String currency = doc.select("div.price_fig
		// meta[itemprop=priceCurrency]").attr("content"); //always GBP not true
		// need check
		String currency = Currency.CNY.name();
		String defaultPrice = doc.select("div.price_fig meta[itemprop=price]").attr("content");
		String salePrice = doc.select("div.price_fig span#spanPrice_" + ProductId).html();// like
																							// &yen;369.00
																							// need
																							// format
																							// it
		String currencySymbol = salePrice.split(defaultPrice)[0];
		// Format price to remove currencySymbol like &yen;
		salePrice = salePrice.replaceAll(currencySymbol, "").trim();
		String originPrice = doc.select("div.price_fig span del").html();
		originPrice = originPrice.replaceAll(currencySymbol, "").trim();
		if (StringUtils.isNotBlank(salePrice)) {
			if (StringUtils.isBlank(originPrice)) {
				originPrice = salePrice;
			}
			int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(originPrice)) * 100);// discount
			retBody.setPrice(new Price(Float.valueOf(originPrice), save, Float.valueOf(salePrice), currency));
		}
		// Stock
		retBody.setStock(new Stock(1));
		// BreadCrumb & Category
		List<String> breads = new ArrayList<String>();
		Elements breadcrumbElements = doc.select("li.breadcrumb-item span");
		if (CollectionUtils.isNotEmpty(breadcrumbElements)) {
			for (Element e : breadcrumbElements) {
				String breadcrumb = e.html();
				if (StringUtils.containsIgnoreCase(breadcrumb, "home")) {
					continue;
				}
				breads.add(breadcrumb);
			}
			retBody.setCategory(breads);

			breads.add(brand);
			retBody.setBreadCrumb(breads);
		}
		// Image
		List<Image> image_list = new ArrayList<Image>();
		Elements epictures = doc.select("img.img-responsive.drift-demo-trigger");
		if (CollectionUtils.isNotEmpty(epictures)) {
			for (Element e : epictures) {
				image_list.add(new Image(StringUtils.trimToEmpty(e.absUrl("src"))));
			}
		}

		// skuJsonArray
		JSONArray skuJsonArray = new JSONArray();
		Elements skuElements = doc.select("select#drpProdVariant_" + ProductId + " option");
		String defaultSkuId = null;
		String defaultSkuSize = null;
		if (CollectionUtils.isNotEmpty(skuElements)) {
			int count = 0;// 在解析页面的时候，默认的sku的价格已经显示在第一个，无须再发送请求得到价格，可以过滤掉减少请求。
			for (Element e : skuElements) {
				String skuId = e.attr("value");
				if (count == 0) {
					defaultSkuId = skuId;
					defaultSkuSize = e.html();
					count++;
					continue;
				} else {
					String skuAddress = "https://www.vitaminplanet.cn/products/getproductvariantdetail?fiProductVariantId="
							+ skuId + "&fiProductId=" + ProductId;
					String skuContent = StringUtils.EMPTY;
					if (StringUtils.isBlank(proxyRegionId)) {
						skuContent = Crawler.create().timeOut(30000).url(skuAddress).header(getHeaders())
								.method(HttpMethod.GET.getValue()).resultAsString();
					} else {
						skuContent = Crawler.create().timeOut(30000).url(skuAddress).header(getHeaders())
								.method(HttpMethod.GET.getValue()).proxy(true).proxyAddress(proxy.getIp())
								.proxyPort(proxy.getPort()).resultAsString();
					}
					if (StringUtils.isNotBlank(content)) {
						JSONObject skuJsonData = JSONObject.parseObject(skuContent);
						JSONObject skuJson = new JSONObject();
						JSONObject priceJson = skuJsonData.getJSONObject("loProductVariant").getJSONArray("Currencies")
								.getJSONObject(0);
						String orig_Price = priceJson.getString("StrikePrice");
						String sale_Price = priceJson.getString("SalePrice");
						if(Double.parseDouble(orig_Price) < 0){
							orig_Price = sale_Price;
						}
						JSONObject skuInfoJson = skuJsonData.getJSONObject("loProductVariant").getJSONArray("Variants")
								.getJSONObject(0);
						String vaule = skuInfoJson.getString("VariantValue");
						skuJson.put("skuId",
								skuJsonData.getJSONObject("loProductVariant").getString("ProductVariantId"));
						skuJson.put("stock_status", 1);
						skuJson.put("sale_price", sale_Price);
						skuJson.put("orign_price", orig_Price);
						skuJson.put("size", vaule);
						skuJsonArray.add(skuJson);
					}
				}
			}
			JSONObject defaultSkuJson = new JSONObject();
			defaultSkuJson.put("skuId", defaultSkuId);
			defaultSkuJson.put("stock_status", 1);
			defaultSkuJson.put("sale_price", salePrice);
			defaultSkuJson.put("orign_price", originPrice);
			defaultSkuJson.put("size", defaultSkuSize);
			skuJsonArray.add(defaultSkuJson);
		}

		context.getUrl().getImages().put(defaultSkuId, image_list);
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		String defaultStyle_id = null;
		if (skuJsonArray != null) {
			for (Object object : skuJsonArray) {
				JSONObject skuJsonObj = (JSONObject) object;
				String sizeVal = skuJsonObj.getString("size");
				String skuId = skuJsonObj.getString("skuId");
				String stock_status = skuJsonObj.getString("stock_status");
				String sale_price = skuJsonObj.getString("sale_price");
				String orign_price = skuJsonObj.getString("orign_price");
				if (skuId.equals(defaultSkuId)) {
					defaultStyle_id = sizeVal;
				}
				if (StringUtils.isNotBlank(sizeVal)) {
					LSelectionList lSelectionList = new LSelectionList();
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setSku_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(orign_price));
					lSelectionList.setPrice_unit(currency);
					lSelectionList.setSale_price(Float.parseFloat(sale_price));
					lSelectionList.setStock_status(Integer.parseInt(stock_status));
					lSelectionList.setStyle_id(sizeVal);
					List<Selection> selections = new ArrayList<>();
					Selection selection = new Selection();
					selection.setSelect_name("size");
					selection.setSelect_value(sizeVal);
					selections.add(selection);
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
				}

			}

			if (CollectionUtils.isNotEmpty(l_selection_list)) {
				LStyleList lStyleList = new LStyleList();
				lStyleList.setGood_id(defaultSkuId);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(defaultStyle_id);
				lStyleList.setStyle_switch_img("");
				lStyleList.setStyle_name(defaultStyle_id);
				lStyleList.setDisplay(true);
				l_style_list.add(lStyleList);
			}
		}
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);
		setOutput(context, retBody);
	}

}
