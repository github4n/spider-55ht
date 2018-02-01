package com.haitao55.spider.crawler.core.callable.custom.colehaan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月28日 上午11:30:18  
 */
public class ColeHaan extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.colehaan.com";

	private static final String CSS_TITLE = "h1.product-name";
	private static final String CSS_BREADS = "ol.breadcrumb li a";
	private static final String CSS_DESCRIPTION = "div#details div.details-tab-details p";
	private static final String CSS_DETAIL = "div#details div.details-tab-details li";
	private static final String CSS_DEFAULT_COLOR = "div.product-variations ul.all-attributes li ul.color li.selected a";

	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Sale", "Women's Final Sale" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Sale", "Men's Final Sale" };

	public static final int nThreads = 6;//线程数
	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		String content = super.getInputString(context);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);
			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = StringUtils.EMPTY;
			Elements es_productIds = document.getElementsByAttributeValue("itemprop", "productID");
			if (CollectionUtils.isNotEmpty(es_productIds)) {
				productID = StringUtils.trimToEmpty(es_productIds.get(0).text());
			}
			if (StringUtils.isBlank(productID)) {
				logger.error("get productID error");
				return;
			}
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);

			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Elements eDetails = document.select(CSS_DETAIL);
			int count = 1;
			StringBuilder sb = new StringBuilder();
			Elements eDescriptions = document.select(CSS_DESCRIPTION);
			if (CollectionUtils.isEmpty(eDescriptions))
				eDescriptions = document.select("div#details div.details-tab-details h2");
			if (CollectionUtils.isNotEmpty(eDescriptions)) {
				for (Element e : eDescriptions) {
					String decs = StringUtils.trim(e.text());
					if (StringUtils.isNotBlank(decs)) {
						featureMap.put("feature-" + count, decs);
						count++;
						sb.append(decs);
					}
				}
			}
			if (CollectionUtils.isNotEmpty(eDetails)) {
				for (Element e : eDetails) {
					featureMap.put("feature-" + count, e.ownText().trim());
					count++;
					sb.append(e.text().trim()).append(" ");
				}
			}
			descMap.put("en", sb.toString());

			float spu_orig_price = 0f;
			float spu_sale_price = 0f;
			int save = 0;
			String spu_Unit = "";
			int spu_stock_status = 0;

			Map<String, Object> properties = new HashMap<>();
			// 设置性别
			CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);

			// 设置sku
			List<ColorImgs> colors = new ArrayList<>();
			List<ColeHaanSku> skus = new ArrayList<>();
			String defaultcolor = setColorList(document, colors);
			setSkus(document, currentUrl, logger, defaultcolor, productID, skus);

			Elements ecolors = document.select("div.product-variations ul.all-attributes li ul.color li.emptyswatch a");
			List<String> colorUrls = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(ecolors)) {
				for (Element e : ecolors) {
					String url = e.absUrl("href");
					url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(url);
					colorUrls.add(url);
				}
				Map<String, Document> map = getColorDocument(colorUrls);
				for(Entry<String, Document> entry : map.entrySet()){
					Document current_document = entry.getValue();
					String current_color = setColorList(current_document, colors);
					String current_color_productID = StringUtils.EMPTY;
					Elements current_color_productids = current_document.getElementsByAttributeValue("itemprop", "productID");
					if (CollectionUtils.isNotEmpty(current_color_productids)) {
						current_color_productID = StringUtils.trimToEmpty(current_color_productids.get(0).text());
					}
					if (StringUtils.isBlank(current_color_productID)) {
						logger.error("get current_color_productID error");
						return;
					}
					setSkus(current_document, currentUrl, logger, current_color, current_color_productID, skus);
				}
			}
			
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			
			for(ColeHaanSku sku : skus){
				if (sku.getStatus() > 0)
					spu_stock_status = 1;
				List<Selection> slist = new ArrayList<>();
				LSelectionList selection = new LSelectionList();
				if (StringUtils.isNotBlank(sku.getSize())) {
					Selection size_selection = new Selection(0, sku.getSize(), "Size");
					slist.add(size_selection);
				}
				if (StringUtils.isNotBlank(sku.getWidth())) {
					Selection width_selection = new Selection(0, sku.getWidth(), "Width");
					slist.add(width_selection);
				}

				selection.setGoods_id(sku.getSkuId());
				selection.setPrice_unit(sku.getUnit());
				selection.setSale_price(sku.getSale_price());
				selection.setOrig_price(sku.getOrig_price());
				if(selection.getOrig_price() < selection.getSale_price())
					selection.setOrig_price(selection.getSale_price());
				selection.setStock_number(sku.getInventory());
				selection.setStock_status(sku.getStatus());
				selection.setStyle_id(sku.getColor());
				selection.setSelections(slist);
				l_selection_list.add(selection);
				
				if(!checkStyleExist(sku, l_style_list)){
					ColorImgs colorImgs = getColorImgsByColor(colors, sku.getColor());
					LStyleList style = new LStyleList();
					if(defaultcolor.equals(sku.getColor())){
						style.setDisplay(true);
						spu_sale_price = sku.getSale_price();
						spu_orig_price = sku.getOrig_price();
						spu_Unit = sku.getUnit();
						if(spu_orig_price < spu_sale_price)
							spu_orig_price = spu_sale_price;
						if (spu_orig_price != 0)
							save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
						
					}
					style.setGood_id(StringUtils.trimToEmpty(sku.getSkuId()));
					style.setStyle_cate_id(0);
					style.setStyle_cate_name("COLOR");
					style.setStyle_id(StringUtils.trimToEmpty(sku.getColor()));
					style.setStyle_name(StringUtils.trimToEmpty(sku.getColor()));
					style.setStyle_switch_img(colorImgs.getSwitch_img());
					context.getUrl().getImages().put(sku.getSkuId(), CrawlerUtils.convertToImageList(colorImgs.getImgs()));
					l_style_list.add(style);
				}
				
			}
			
			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand("Cole Haan", "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setPrice(new Price(spu_orig_price, save, spu_sale_price,
			 spu_Unit));
			retbody.setStock(new Stock(spu_stock_status));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		setOutput(context, retbody);
	}

	private String setColorList(Document document, List<ColorImgs> colors) {
		String defaultColor = "";
		String default_switch_img = "";
		Elements e_default_color = document.select(CSS_DEFAULT_COLOR);
		if (CollectionUtils.isNotEmpty(e_default_color)) {
			defaultColor = e_default_color.get(0).attr("title");
			default_switch_img = "http://www.colehaan.com"
					+ StringUtils.substringBetween(e_default_color.get(0).attr("style"), "background: url(", ");");
		}
		ColorImgs defaultColorImgs = new ColorImgs();
		defaultColorImgs.setColor(defaultColor);
		defaultColorImgs.setSwitch_img(default_switch_img);
		Elements e_imgs = document.select("img.productthumbnail");
		if (CollectionUtils.isNotEmpty(e_imgs)) {
			List<String> defaultImgs = new ArrayList<>();
			for (Element e : e_imgs) {
				defaultImgs.add(StringUtils.trim(e.absUrl("src")));
			}
			defaultColorImgs.setImgs(defaultImgs);
		}
		colors.add(defaultColorImgs);
		return defaultColor;
	}
	
	private ColorImgs getColorImgsByColor(List<ColorImgs> colors, String color){
		if(CollectionUtils.isNotEmpty(colors)){
			for(ColorImgs temp : colors){
				if(color.equals(temp.getColor()))
					return temp;
			}
		}
		return null;
	}

	private void setSkus(Document document, String currentUrl, Logger logger, String color, String productID,
			List<ColeHaanSku> skus) {
		Elements ewidth = document.select("select#va-width>option:not(:first-child)");
		String style = document.select("li.dd-size").get(0).attr("style");
		//只有颜色的商品:无size、无width
		if(CollectionUtils.isEmpty(ewidth) && "display: none;".equals(style)) {
			ColeHaanSku sku = new ColeHaanSku();
			Elements eorigprice = document.select("div#product-content div.product-price span.price-standard");
			String origPriceStr = "";
			if (CollectionUtils.isNotEmpty(eorigprice)) {
				origPriceStr = eorigprice.get(0).text();
			}
			Elements esaleprice = document.select("div#product-content div.product-price span.price-sales>span");
			if(CollectionUtils.isEmpty(esaleprice))
				esaleprice = document.select("div#product-content div.product-price span.price-sales");
			String salePriceStr = "";
			if (CollectionUtils.isNotEmpty(esaleprice)) {
				salePriceStr = esaleprice.get(0).text();
			}
			
			String unit = CrawlerUtils.getUnit(salePriceStr, currentUrl, logger);
			float saleprice = CrawlerUtils.getPrice(salePriceStr, currentUrl, logger);
			float origprice = 0f;
			if(StringUtils.isNotBlank(origPriceStr))
				origprice = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
			sku.setUnit(unit);
			sku.setSale_price(saleprice);
			sku.setOrig_price(origprice);
			sku.setColor(color);
			sku.setSkuId(sku.getColor().replace(" ", ""));
			sku.setInventory(0);
			sku.setStatus(1);
			skus.add(sku);
		}
		
		// 有color、size和width的情况
		if (CollectionUtils.isNotEmpty(ewidth) && !"display: none;".equals(style)) {
			for (Element ew : ewidth) {
				String width = StringUtils.trim(ew.ownText().replace("Sold Out", ""));
				String color_param = color.replace(" ", "+");
				String url_sku = "http://www.colehaan.com/on/demandware.store/Sites-ColeHaan_US-Site/en_US/Product-Variation?pid="
						+ productID + "&dwvar_" + productID + "_width="
						+ width + "&dwvar_" + productID + "_color=" + color_param
						+ "&Quantity=1&format=ajax&format=ajax";
				String skuStr = HttpUtils.get(url_sku);
				Document doc = Jsoup.parse(skuStr);
				Elements eorigprice = doc.select("div.product-price span.price-standard");
				String origPriceStr = "";
				if (CollectionUtils.isNotEmpty(eorigprice)) {
					origPriceStr = eorigprice.get(0).text();
				}
				Elements esaleprice = doc.select("div.product-price span.price-sales>span");
				if(CollectionUtils.isEmpty(esaleprice))
					esaleprice = doc.select("div.product-price span.price-sales");
				String salePriceStr = "";
				if (CollectionUtils.isNotEmpty(esaleprice)) {
					salePriceStr = esaleprice.get(0).text();
				}
				
				String unit = CrawlerUtils.getUnit(salePriceStr, currentUrl, logger);
				float saleprice = CrawlerUtils.getPrice(salePriceStr, currentUrl, logger);
				float origprice = 0f;
				if(StringUtils.isNotBlank(origPriceStr))
					origprice = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
				Elements e_size = doc.select("select#va-size>option:not(:first-child)");
				if (CollectionUtils.isNotEmpty(e_size)) {
					for (Element e : e_size) {
						ColeHaanSku sku = new ColeHaanSku();
						sku.setUnit(unit);
						sku.setSale_price(saleprice);
						sku.setOrig_price(origprice);
						sku.setColor(color);
						sku.setWidth(width);
						sku.setSize(StringUtils.trim(e.text().replace("Sold Out", "")));
						sku.setSkuId((sku.getColor()+sku.getSize()+sku.getWidth()).replace(" ", ""));
						if (e.text().contains("Sold Out")) {
							sku.setStatus(0);
							sku.setInventory(0);
						} else {
							sku.setStatus(1);
							sku.setInventory(0);
						}
						skus.add(sku);
					}
				}
			}
		}
		if(CollectionUtils.isEmpty(ewidth) && !"display: none;".equals(style)) {
			Elements eorigprice = document.select("div#product-content div.product-price span.price-standard");
			String origPriceStr = "";
			if (CollectionUtils.isNotEmpty(eorigprice)) {
				origPriceStr = eorigprice.get(0).text();
			}
			Elements esaleprice = document.select("div#product-content div.product-price span.price-sales>span");
			if(CollectionUtils.isEmpty(esaleprice))
				esaleprice = document.select("div#product-content div.product-price span.price-sales");
			String salePriceStr = "";
			if (CollectionUtils.isNotEmpty(esaleprice)) {
				salePriceStr = esaleprice.get(0).text();
			}
			
			String unit = CrawlerUtils.getUnit(salePriceStr, currentUrl, logger);
			float saleprice = CrawlerUtils.getPrice(salePriceStr, currentUrl, logger);
			float origprice = 0f;
			if(StringUtils.isNotBlank(origPriceStr))
				origprice = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
			Elements e_size = document.select("select#va-size>option:not(:first-child)");
			if (CollectionUtils.isNotEmpty(e_size)) {
				for (Element e : e_size) {
					ColeHaanSku sku = new ColeHaanSku();
					sku.setUnit(unit);
					sku.setSale_price(saleprice);
					sku.setOrig_price(origprice);
					sku.setColor(color);
					sku.setSize(StringUtils.trim(e.text().replace("Sold Out", "")));
					sku.setSkuId((sku.getColor()+sku.getSize()).replace(" ", ""));
					if (e.text().contains("Sold Out")) {
						sku.setStatus(0);
						sku.setInventory(0);
					} else {
						sku.setStatus(1);
						sku.setInventory(0);
					}
					skus.add(sku);
				}
			}
		}
	}
	
	private boolean checkStyleExist(ColeHaanSku sku, List<LStyleList> l_style_list) {
		for(LStyleList style : l_style_list){
			if(sku.getColor().equals(style.getStyle_id()))
				return true;
		}
		return false;
	}
	
	private Map<String,Document> getColorDocument(List<String> urls){
		Map<String,Document> resultMap = new HashMap<>();
		ExecutorService service = Executors.newFixedThreadPool(nThreads);
		List<ColeHaanCall> calls = new ArrayList<>();
		for (String url : urls) {
			calls.add(new ColeHaanCall(url));
		}
		try {
			List<Future<Map<String, Document>>> futures = service.invokeAll(calls);
			for(Future<Map<String, Document>> future : futures){
				Map<String,Document> map = future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				resultMap.putAll(map);
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			service.shutdownNow();
		}
		return null;
	}
}
