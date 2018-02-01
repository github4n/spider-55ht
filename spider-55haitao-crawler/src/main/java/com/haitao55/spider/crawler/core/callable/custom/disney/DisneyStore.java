package com.haitao55.spider.crawler.core.callable.custom.disney;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * disney store 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月10日 上午11:11:57
 * @version 1.0
 */
public class DisneyStore extends AbstractSelect {
	private static final String IMAGE_SUFFIX = "?$yetidetail$";
	private static final String DOMAIN = "www.disneystore.com";
	private static final String PRODUCT_STOCK_FLAG = "Customize It";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.substringBetween(content, "productId: \"", "\"");

		// spu_stock_status
		int spu_stock_status = 0;

		// title
		String title = StringUtils.substringBetween(content, "pProductName : \"", "\"");

		// sale_price
		String sale_price = StringUtils.substringBetween(content, "pSalePrice : \"", "\"");

		// orign_price
		String orign_price = StringUtils.substringBetween(content, "pRegPrice : \"", "\"");

		// 代表非正常详情页，可能是多个单品组合，相当于列表页
		if (StringUtils.isEmpty(sale_price) && StringUtils.isEmpty(orign_price)) {
			return;
		}

		// unit
		String unit = StringUtils.substringBetween(content, "t_currency:\"", "\",");
		if (StringUtils.isBlank(unit)) {
			Elements unitElements = doc.select("span[itemprop=price]");
			unit = getCurrencyValue(unitElements.text());
		}

		// images
		List<Image> images = new ArrayList<Image>();
		Elements imagesElements = doc.select("div a.productThumb.thumbnailImage img");
		if (CollectionUtils.isEmpty(imagesElements)) {
			imagesElements = doc.select("img.mainImage");
		}
		if (CollectionUtils.isNotEmpty(imagesElements)) {
			for (Element element : imagesElements) {
				String image_url = element.attr("src");
				if (StringUtils.isNotBlank(image_url)) {
					image_url = StringUtils.substringBefore(image_url, "?");
					image_url = image_url + IMAGE_SUFFIX;
				}
				images.add(new Image(image_url));
			}
		}

		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		// style
		Set<String> styleSet = new HashSet<String>();

		// sku data
		String skuData = StringUtils.substringBetween(content, "var variantsJson = ", ";");
		JSONObject skuDataJSONObject = JSONObject.parseObject(skuData);
		if (MapUtils.isNotEmpty(skuDataJSONObject)) {
			JSONArray skuJSONArray = skuDataJSONObject.getJSONArray("items");
			if (CollectionUtils.isNotEmpty(skuJSONArray)) {
				for (Object object : skuJSONArray) {
					JSONObject skuJSONObject = (JSONObject) object;
					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJSONObject.getString("catEntryId");

					// color
					String color = "default";

					// size
					String size = skuJSONObject.getString("id");

					// stock
					boolean isStock = skuJSONObject.getBooleanValue("buyable");
					int stock_status = 0;
					int stock_number = 0;
					if (isStock) {
						stock_status = 1;
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

					// orign_price
					String salePrice = skuJSONObject.getString("price");
					if (StringUtils.isBlank(salePrice)) {
						salePrice = sale_price;
					}
					salePrice = salePrice.replaceAll("[$,]", "");
					// lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(Float.parseFloat(orign_price));
					lselectlist.setSale_price(Float.parseFloat(salePrice));
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);

					// style
					if (!styleSet.contains(color)) {
						// stylelist
						LStyleList lStyleList = new LStyleList();
						lStyleList.setDisplay(true);

						context.getUrl().getImages().put(skuId, images);
						// switch_img
						String switch_img = StringUtils.EMPTY;

						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(style_id);

						l_style_list.add(lStyleList);

						styleSet.add(color);
					}
				}
			}
			// 单品
			else {
				context.getUrl().getImages().put(productId, images);
				spu_stock_status = 1;
				// stock
				String product_stock = StringUtils.substringBetween(content, "var customButtonLabel = \"", "\";");
				if (StringUtils.isNotBlank(product_stock)
						&& StringUtils.endsWithIgnoreCase(PRODUCT_STOCK_FLAG, product_stock)) {
					spu_stock_status = 0;
				}
			}
		}

		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		retBody.setStock(new Stock(spu_stock_status));

		// price
		int save = (int) ((1 - (Float.parseFloat(sale_price) / Float.parseFloat(orign_price))) * 100);
		retBody.setPrice(new Price(Float.parseFloat(orign_price), save, Float.parseFloat(sale_price), unit));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		// brand
		String brand = "Disney";
		retBody.setBrand(new Brand(brand, "", "", ""));

		// title
		retBody.setTitle(new Title(title, "", "", ""));
		category_package(brand, content, retBody);

		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "");
		retBody.setProperties(propMap);
		// description
		desc_package(doc, retBody);

		setOutput(context, retBody);
	}

	/**
	 * 分类封装
	 * 
	 * @param brand
	 * @param content
	 * @param retBody
	 */
	private static void category_package(String brand, String content, RetBody retBody) {
		String category_data = StringUtils.substringBetween(content, "var tealiumCategory = \"", "\"");
		String[] split = StringUtils.split(category_data, ":");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (null != split && split.length > 0) {
			for (String str : split) {
				cats.add(str);
				breads.add(str);
			}
		}
		breads.add(brand);
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
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
		Elements es = doc.select(
				"div.longDescription h3,div.longDescription ul li,div.productSpecs h3,div.productSpecs ul li,div.additionalDescription h3,div.additionalDescription span");
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
}
