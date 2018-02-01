package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

public class AmazonCNPage extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(AmazonCNPage.class);
	public static final String MAIN_DOMAIN = "amazon.cn";
	private static final String DOMAIN = "www." + MAIN_DOMAIN;
	public static final String ITEM_URL_TMT = "https://www.amazon.cn/dp/%s/?th=1&psc=1";
	public static final String UNIT = "CNY";
	public static final String JPY_SYMBOL = "￥";
	public static final String STOCK_PATTERN = "库存中仅剩 (\\d+) 件|现在有货|通常在.*发货";
	public static final String COUNTRY = "CN";
	public static final int nThreads = 10;

	// 用来暂时调试的日志，稳定后可以去掉
	private static final Logger LOGGER_HTML_PAGE_SOURCE = LoggerFactory.getLogger("realtime_html_page_source");

	@Override
	public void invoke(Context context) throws Exception {
		String orignalUrl = context.getUrl().getValue();

		System.out.println("debug in AmazonCN.invoke method by-System.out.println, orignalUrl==" + orignalUrl);
		logger.info("debug in AmazonCN.invoke method by-logger, orignalUrl:{}", orignalUrl);
		LOGGER_HTML_PAGE_SOURCE.info("debug in AmazonCN.invoke method by-LOGGER_HTML_PAGE_SOURCE, orignalUrl:{}",
				orignalUrl);

		String itemId = SpiderStringUtil.getAmazonItemId(orignalUrl);
		if (itemId == null) {
			itemId = getAmazonCN_ItemId(orignalUrl);
		}

		// 一格式化就失去了sku选项信息
		// orignalUrl = String.format(ITEM_URL_TMT, itemId);

		if (!context.isRunInRealTime()) {
			logger.error("AmazonCN callable is not running in realtime!!!");
			return;// 如果不是在‘实时核价’的环境中运行，则不执行任何功能
		}

		String content = StringUtils.EMPTY;
		try {// 经过一番测试对比，最后确定以“使用Luminati并且部署在google云环境”的方式运行中亚海外购核价功能
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient(AmazonCNPage.COUNTRY, true);
			content = luminatiHttpClient.request(orignalUrl, AmazonCNPage.getHeaders());
			context.setHtmlPageSource(content);
		} catch (Exception e) {
			context.setHtmlPageSource(content);
			throw e;
		}

		if (StringUtils.isBlank(content)) {
			logger.error("AmazonCN callable got none content!!!");
			return;// 如果没抓取到html源码数据，则什么也解析不了，于是程序不再继续往下运行
		}

		RetBody rebody = new RetBody();

		String docid = SpiderStringUtil.md5Encode(DOMAIN + "_" + itemId);
		String url_no = SpiderStringUtil.md5Encode(orignalUrl);
		rebody.setDOCID(docid);
		rebody.setSite(new Site(DOMAIN));
		rebody.setProdUrl(new ProdUrl(orignalUrl, System.currentTimeMillis(), url_no));

		Document doc = Jsoup.parse(content);

		// 检查一下，是不是页面上有“尺寸”属性但是却未选择具体的属性值，如果是这样，抛异常，让实时核价框架接收到异常之后再去处理（封装数据）
		String sizeLabelCss = "div#variation_size_name > div.a-row.a-spacing-micro > label.a-form-label";
		Elements sizeLabelElements = doc.select(sizeLabelCss);
		String sizeValueCss = "div#variation_size_name select[name=dropdown_selected_size_name] > option.dropdownSelect";
		Elements sizeValueElements = doc.select(sizeValueCss);
		if (sizeLabelElements != null && !sizeLabelElements.isEmpty()) {
			if (sizeValueElements == null || sizeValueElements.isEmpty()) {
				throw new AmazonCnRealtimeException(AmazonCnRealtimeExceptionCode.NOT_SELECT_REQUIRED_PROPERTY, "");
			}
		}

		// title
		String title = "";
		Elements es = doc.select("#productTitle");
		if (es != null && es.size() > 0) {
			title = es.get(0).text();
		}
		rebody.setTitle(new Title("", title, "", ""));

		// brands
		String brand = "";
		es = doc.select("#brand");
		if (es != null && es.size() > 0) {
			brand = es.get(0).text();
		}
		rebody.setBrand(new Brand("", brand, "", ""));

		// cats
		List<String> cats = Lists.newArrayList();
		// breads
		List<String> breads = Lists.newArrayList();
		es = doc.select("#wayfinding-breadcrumbs_feature_div > ul > li");
		String catsString = es.text();
		if (es != null && es.size() > 0) {
			es.forEach(e -> {
				String clazz = e.attr("class");
				if (StringUtils.isNotBlank(clazz) && StringUtils.contains(clazz, "a-breadcrumb-divider")) {
					return;
				}
				String cat = StringUtils.trim(e.text());
				cats.add(cat);
				breads.add(cat);
			});
		} else {
			es = doc.select("SalesRank > ul > li > span.zg_hrsr_ladder");
			catsString = es.text();
			if (es != null && es.size() > 0) {
				String catString = StringUtils.trim(StringUtils.substring(es.get(0).text(), 1));
				String[] catArr = StringUtils.split(catString, ">");
				if (catArr != null && catArr.length > 0) {
					for (String item : catArr) {
						cats.add(item);
						breads.add(item);
					}
				}

			}
		}
		rebody.setCategory(cats);
		rebody.setBreadCrumb(breads);

		// feature
		Map<String, Object> featureMap = Maps.newHashMap();
		es = doc.select("#feature-bullets > ul > li > span.a-list-item");
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				featureMap.put("feature-" + count, e.text());
				count++;
			}
		}
		rebody.setFeatureList(featureMap);

		// description
		Map<String, Object> descMap = Maps.newHashMap();
		es = doc.select("#productDescription");
		String description = "";
		if (es != null && es.size() > 0) {
			description = es.get(0).text();
		}
		descMap.put("cn", description);
		rebody.setDescription(descMap);

		// properties
		Map<String, Object> propMap = Maps.newHashMap();
		String s_gender = "all";
		if (StringUtils.contains(catsString, "男")) {
			s_gender = "men";
		} else if (StringUtils.contains(catsString, "女")) {
			s_gender = "women";
		}
		propMap.put("s_gender", s_gender);
		es = doc.select("#detail-bullets_feature_div div.content > ul > li");
		if (es != null && es.size() > 0) {
			for (Element e : es) {
				String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
				String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
				if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
					propMap.put(key, value);
				}
			}
		}
		rebody.setProperties(propMap);

		// price
		float orig = 0f;
		float sale = 0f;
		es = doc.select("#priceblock_ourprice");
		if (CollectionUtils.isEmpty(es))
			es = doc.select("#priceblock_saleprice");
		if (es != null && es.size() > 0) {
			String salePriceString = es.get(0).text();
			salePriceString = salePriceString.replace(JPY_SYMBOL, "").replace(",", "");
			if (StringUtils.isNotBlank(salePriceString)) {
				sale = Float.parseFloat(salePriceString);
			}
		}
		es = doc.select("#price_feature_div span.a-text-strike");
		if (es != null && es.size() > 0) {
			String origPriceString = es.get(0).text();
			origPriceString = origPriceString.replace(JPY_SYMBOL, "").replace(",", "");
			if (StringUtils.isNotBlank(origPriceString)) {
				orig = Float.parseFloat(origPriceString);
			}
		}
		if (orig == 0f) {
			orig = sale;
		}
		if (sale == 0f) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					DOMAIN + " itemUrl : " + orignalUrl + " , sale is 0 ");
		}
		int save = Math.round((1 - sale / orig) * 100);// discount
		rebody.setPrice(new Price(orig, save, sale, UNIT));

		// stock: include 'stock status' and 'stock number'
		int stockStatus = 0;
		int stockNumber = 0;

		String stockString = "";
		es = doc.select("#ddmAvailabilityMessage > span");
		if (es != null && es.size() > 0) {
			stockString = es.get(0).text();
		}

		Pattern pattern = Pattern.compile(STOCK_PATTERN);
		Matcher matcher = pattern.matcher(stockString);
		if (matcher.find()) {
			String stockNumString = matcher.group(1);
			if (StringUtils.isNotBlank(stockNumString)) {
				stockStatus = 2;
				stockNumber = Integer.valueOf(stockNumString);
			} else {
				stockStatus = 1;
			}
		}
		rebody.setStock(new Stock(stockStatus, stockNumber));

		// spu images
		List<Image> imgs = Lists.newArrayList();
		es = doc.select("div#altImages > ul > li img");
		if (CollectionUtils.isNotEmpty(es)) {
			for (Element e : es) {
				String imgSrcUrl = e.attr("src");
				imgSrcUrl = StringUtils.replace(imgSrcUrl, "_US40_.jpg", "_US400_.jpg");// 小图片放大
				imgSrcUrl = StringUtils.replace(imgSrcUrl, "_SR38,50_.jpg", "_SR380,500_.jpg");// 小图片放大
				imgs.add(new Image(imgSrcUrl));
			}
		}

		// sku
		List<LStyleList> l_style_list = Lists.newArrayList();
		LStyleList lStyleList = new LStyleList();
		String colorValue = "default";
		Elements colorValueElements = doc.select("div#variation_color_name > div.a-row > span.selection");
		if (colorValueElements != null && !colorValueElements.isEmpty()) {
			colorValue = colorValueElements.get(0).text();
		}
		String styleGoodId = "0";
		lStyleList.setGood_id(styleGoodId);
		lStyleList.setDisplay(true);
		lStyleList.setStyle_cate_name("color");
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_name(colorValue);
		lStyleList.setStyle_id(colorValue);
		lStyleList.setStyle_switch_img("");
		l_style_list.add(lStyleList);
		context.getUrl().getImages().put(styleGoodId, imgs);

		List<LSelectionList> l_selection_list = Lists.newArrayList();

		LSelectionList lselectlist = new LSelectionList();
		lselectlist.setGoods_id("");
		lselectlist.setOrig_price(orig);
		lselectlist.setSale_price(sale);
		lselectlist.setPrice_unit(UNIT);
		lselectlist.setStock_status(stockStatus);
		lselectlist.setStock_number(0);
		lselectlist.setStyle_id(colorValue);

		List<Selection> selections = new ArrayList<Selection>();
		if (sizeValueElements != null && !sizeValueElements.isEmpty()) {
			String sizeValue = sizeValueElements.get(0).text();
			Selection selection = new Selection();
			selection.setSelect_id(0);
			selection.setSelect_name("Size");
			selection.setSelect_value(sizeValue);
			selections.add(selection);
		}
		lselectlist.setSelections(selections);

		l_selection_list.add(lselectlist);

		rebody.setSku(new Sku(l_selection_list, l_style_list));

		System.out.println(rebody.parseTo());
		setOutput(context, rebody.parseTo());
	}

	public static String getAmazonCN_ItemId(String url) {
		Pattern p = Pattern.compile("www.*/gp/product/(.*?)\\?");
		Matcher m = p.matcher(url);
		if (m.find()) {
			return m.group(1);
		}
		String itemId = StringUtils.substringBetween(url, "/dp/", "?");
		if (StringUtils.isBlank(itemId))
			itemId = StringUtils.substringAfterLast(url, "/dp/");
		if (StringUtils.isBlank(itemId))
			itemId = StringUtils.substringAfterLast(url, "/gp/product/");
		return itemId;
	}

	@SuppressWarnings("unused")
	private static String test() {
		// String keywords[] = { "/dp/", "/gp/product/" };
		// for (String key : keywords) {
		//
		// }
		return null;
	}

	public static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.91 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.amazon.cn");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		return headers;
	}

	public static void main(String[] args) throws Exception {
		String url = "";
		url = "https://www.amazon.cn/%E7%8E%A9%E5%85%B7%E7%95%8C%E5%A5%A5%E6%96%AF%E5%8D%A1-2017-TOTY-STEM%E6%95%99%E8%82%B2-%E7%BE%8E%E5%9B%BDHEXBUG-%E8%B5%AB%E5%AE%9DVEX%E6%9C%BA%E5%99%A8%E4%BA%BA%E5%8F%A4%E5%85%B8%E7%B3%BB%E5%88%97-%E7%AB%B9%E8%9C%BB%E8%9C%93-%E6%8B%BC%E6%90%AD%E7%8E%A9%E5%85%B7-%E7%9B%8A%E6%99%BA%E7%8E%A9%E5%85%B7-%E9%80%81%E7%A4%BC%E7%8E%A9%E5%85%B7-%E8%B6%85%E9%AB%98%E6%80%A7%E4%BB%B7%E6%AF%94/dp/B01BX4F6X2/ref=sr_1_1?s=toys-and-games&srs=1494170071&ie=UTF8&qid=1510901302&sr=1-1&dpID=41JjaDOY7uL&preST=_SY300_QL70_&dpSrc=srch&th=1";
		// url =
		// "https://www.amazon.cn/Tommy-Hilfiger-Men-s-Core-Flag-Crew-Neck-Tee/dp/B01MU5XLBC/ref=sr_1_17?s=apparel&ie=UTF8&qid=1510891949&sr=1-17&nodeID=2154530051&psd=1&th=1";
		// url =
		// "https://www.amazon.cn/Champion-%E5%86%A0%E5%86%9B-%E9%95%BF%E8%A2%96T%E6%81%A4-C3-J426-%E7%94%B7%E5%BC%8F-%E7%81%B0%E8%89%B2-L/dp/B01JYZ6TGK/ref=sr_1_14?s=apparel&ie=UTF8&qid=1510891949&sr=1-14&nodeID=2154530051&psd=1&th=1&psc=1";
		url = "https://www.amazon.cn/adrianna-papell-%E5%A5%B3%E5%A3%AB%E9%95%BF%E6%AC%BE%E4%B8%B2%E7%8F%A0%E8%BF%9E%E8%A1%A3%E8%A3%99%E7%A2%8E-SLV-Antique-Bronze-12/dp/B0735NCV9Y/ref=sr_1_1?s=apparel&ie=UTF8&qid=1510990682&sr=1-1&nodeID=91622071&psd=1&dpID=41DnX%252BOg3KL&preST=_SX342_QL70_&dpSrc=srch&th=1&psc=1";
		AmazonCNPage jp = new AmazonCNPage();
		Context context = new Context();
		context.setRunInRealTime(true);
		context.setUrl(new Url(url));
		jp.invoke(context);
	}
}