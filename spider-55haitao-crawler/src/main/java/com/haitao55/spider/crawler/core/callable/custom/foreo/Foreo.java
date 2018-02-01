package com.haitao55.spider.crawler.core.callable.custom.foreo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Foreo网站收录
 * 
 * @author denghuan
 *
 */
public class Foreo extends AbstractSelect {

	private static final String domain = "www.foreo.com";

	@Override
	public void invoke(Context context) throws Exception {
		// String content = this.getInputString(context);
		String url = context.getCurrentUrl();
	    String content = crawler_result(context,url);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String error = doc.select(".field-items .field-item p").text();
			if (StringUtils.isNotBlank(error) && StringUtils.containsIgnoreCase(error, "product is not available")) {
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"itemUrl:" + context.getUrl().toString() + " not found..");
			}
			String productId = StringUtils.substringBetween(content, "product_id\" value=\"", "\"");
			String title = doc.select("h1.node-title").text();
			String salePrice = StringUtils.substringBetween(content, "price\":\"", "\"");
			String unit = StringUtils.substringBetween(content, "priceCurrency\":\"", "\"");
			String stock = doc.select("input.buy-me-now").attr("value");
			String desc = StringUtils.substringBetween(content, "og:description\" content=\"", "\"");
			String Ids = StringUtils.substringBetween(content, "commerce-cart-add-to-cart-form-", " ");
			String origPrice = doc.select(".price-block tr.commerce-price-savings-formatter-list td.price-amount")
					.text();

			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			if (StringUtils.isNotBlank(Ids)) {
				String[] proId = Ids.split("-");
				if (proId != null) {
					for (String id : proId) {
						String currentUrl = StringUtils.EMPTY;
						if (StringUtils.containsIgnoreCase(url, "?")) {
							String purl = url.substring(0, url.indexOf("?"));
							currentUrl = purl + "?id=" + id;
						} else {
							currentUrl = url + "?id=" + id;
						}
						//String skuHtml = luminatiHttpClient.request(currentUrl, getHeaders());
						 String skuHtml = crawler_result(context,currentUrl);
						// currentUrl.setTask(context.getUrl().getTask());
						// String skuHtml =
						// HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT,
						// HttpUtils.DEFAULT_RETRY_TIMES,false);
						if (StringUtils.isNotBlank(skuHtml)) {
							getLSelectList(l_selection_list, l_style_list, unit, context, productId, skuHtml);
						}
					}
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			int spuStock = 0;
			if (l_selection_list != null && l_selection_list.size() > 0) {
				for (LSelectionList ll : l_selection_list) {
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2) {
						spuStock = 2;
					}
				}
			} else {
				if (StringUtils.isNotBlank(stock) && !StringUtils.containsIgnoreCase(stock, "BUY NOW")) {
					spuStock = 1;
				}
				if (StringUtils.isNotBlank(salePrice) && StringUtils.isNotBlank(origPrice)) {
					if (StringUtils.containsIgnoreCase(origPrice, "$")) {
						origPrice = origPrice.replace("$", "");
					}
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
				} else if (StringUtils.isNotBlank(salePrice) && StringUtils.isBlank(origPrice)) {
					rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
				}
				List<Image> imgList = new ArrayList<>();
				Elements es = doc.select(".image-container-ecommerce ul li.field-item img");
				for (Element e : es) {
					String image = e.attr("src");
					if (StringUtils.isNotBlank(image)) {
						imgList.add(new Image(image));
					}
				}
				context.getUrl().getImages().put(productId, imgList);// picture
			}

			rebody.setStock(new Stock(spuStock));

			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(SpiderStringUtil.md5Encode(context.getCurrentUrl()));
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Foreo", ""));

			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String categorys = StringUtils.substringBetween(content, "category\":\"", "\"");
			categorys = categorys.replaceAll("\\\\/", "_");
			if (StringUtils.isNotBlank(categorys)) {
				String[] sp = categorys.split("_");
				if (sp != null) {
					for (String cate : sp) {
						cats.add(cate);
						breads.add(cate);
					}
				}
			}
			if (CollectionUtils.isEmpty(cats)) {
				cats.add(title);
				breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);

			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Map<String, Object> propMap = new HashMap<String, Object>();
			if (StringUtils.containsIgnoreCase(title, "men")) {
				propMap.put("s_gender", "men");
			} else if (StringUtils.containsIgnoreCase(title, "women")) {
				propMap.put("s_gender", "women");
			} else {
				propMap.put("s_gender", "");
			}

			if (StringUtils.isNotBlank(desc)) {
				featureMap.put("feature-1", desc);
			}
			rebody.setProperties(propMap);
			rebody.setFeatureList(featureMap);
			descMap.put("en", desc);
			rebody.setDescription(descMap);

			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

	private String crawler_result(Context context, String url)
			throws ClientProtocolException, HttpException, IOException {
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		String content = StringUtils.EMPTY;
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).header(getHeaders()).url(url).proxy(false).resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).proxy(true)
					.proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.foreo.com");
		headers.put("cookie",
				"country_code=US; Drupal_visitor_lang=en; has_js=1; refff=; _uetsid=_uet352c4e7f; _dc_gtm_UA-698703-21=1; _GAcookieID=558816155.1505973139; _ym_uid=1505973140316229491; _ym_isad=2; _conv_v=vi:1505973137647-0.6587614460723386*sc:1*cs:1505973138*fs:1505973138*pv:1*exp:{}; _conv_s=si:1*pv:1; wisepops=%7B%22version%22%3A2%2C%22uid%22%3A%2227967%22%2C%22ucrn%22%3A41%2C%22last_req_date%22%3A%222017-09-21T05%3A52%3A20.239Z%22%2C%22popins%22%3A%7B%2290572%22%3A%7B%22display_count%22%3A1%2C%22display_date%22%3A%222017-09-21T05%3A52%3A33.799Z%22%7D%7D%7D; wisepops_session=%7B%22new%22%3A1%2C%22req_count%22%3A1%2C%22popins%22%3A%5B90572%5D%7D; _ga=GA1.2.558816155.1505973139; _gid=GA1.2.898206976.1505973139; _gat_UA-698703-21=1; _gali=close-wisepop-90572");
		return headers;
	}

	private void getLSelectList(List<LSelectionList> l_selection_list, List<LStyleList> l_style_list, String unit,
			Context context, String pskuId, String skuHtml) {
		LSelectionList lSelectionList = new LSelectionList();
		LStyleList lStyleList = new LStyleList();
		List<Selection> selections = new ArrayList<>();
		Document document = Jsoup.parse(skuHtml);
		String skuSalePrice = StringUtils.substringBetween(skuHtml, "price\":\"", "\"");
		String origPrice = document.select(".price-block tr.commerce-price-savings-formatter-list td.price-amount")
				.text();

		String skuId = StringUtils.substringBetween(skuHtml, "product_id\" value=\"", "\"");
		String stock = document.select("input.buy-me-now").attr("value");
		String selectedColor = StringUtils.substringBetween(skuHtml, "description-selected", "color-label");
		String styleColor = StringUtils.substringBetween(selectedColor, "title=\"", "\"");
		styleColor = styleColor.replaceAll("&lt;br&gt;", "");
		lSelectionList.setGoods_id(skuId);
		if (StringUtils.isNotBlank(origPrice)) {
			if (StringUtils.containsIgnoreCase(origPrice, "$")) {
				origPrice = origPrice.replace("$", "");
			}
			lSelectionList.setOrig_price(Float.parseFloat(origPrice));
		} else {
			lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
		}
		lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
		lSelectionList.setPrice_unit(unit);
		int stock_status = 0;
		if (StringUtils.isNotBlank(stock) && StringUtils.containsIgnoreCase(stock, "BUY NOW")) {
			stock_status = 1;
		}
		lSelectionList.setStock_status(stock_status);
		lSelectionList.setStyle_id(styleColor);
		lSelectionList.setSelections(selections);
		if (pskuId.equals(skuId)) {
			lStyleList.setDisplay(true);
		}

		lStyleList.setGood_id(skuId);
		lStyleList.setStyle_cate_name("color");
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_id(styleColor);
		lStyleList.setStyle_name(styleColor);
		lStyleList.setStyle_switch_img("");
		List<Image> imgList = new ArrayList<>();
		Elements es = document.select(".image-container-ecommerce ul li.field-item img");
		for (Element e : es) {
			String image = e.attr("src");
			if (StringUtils.isNotBlank(image)) {
				imgList.add(new Image(image));
			}
		}
		context.getUrl().getImages().put(skuId, imgList);// picture
		l_selection_list.add(lSelectionList);
		l_style_list.add(lStyleList);
	}
}
