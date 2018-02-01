package com.haitao55.spider.crawler.core.callable.custom.nautica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * nautica 详情收录 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月17日 上午11:34:17
 * @version 1.0
 */
public class Nautica_bak extends AbstractSelect {
	private static final String REQUEST_URL_WITH_SIZE = "http://www.nautica.com/on/demandware.store/Sites-nau-Site/default/Product-Variation?pid={}&dwvar_{}_color=()&dwvar_{}_size=[]&Quantity=1&delayDisplayRender=true&productSet=false&altView=false&format=ajax";
	private static final String REQUEST_URL_NO_SIZE = "http://www.nautica.com/on/demandware.store/Sites-nau-Site/default/Product-Variation?pid={}&dwvar_{}_color=()&Quantity=1&delayDisplayRender=true&productSet=false&altView=false&format=ajax";
	private static final String IMAGE_JSON_URL = "http://cdn.fluidretail.net/customers/c1500/{}/{}_pdp/js/data.js";
	private static final String DOMAIN = "www.nautica.com";
	private static final String IMAGE_PREFFIX = "variation_";
	private static final String IMAGE_SUFFIX = "_view";
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);

		Document doc = this.getDocument(context);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.EMPTY;
		Elements productElements = doc.select("span[itemprop=productID]");
		if (CollectionUtils.isNotEmpty(productElements)) {
			productId = productElements.get(0).text();
		}

		// brand
		String brand = "nautica";

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("h1[itemprop=name]");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}

		// spu stockstatus
		int spuStockStatus = 0;
		// defaultcolorid
		String defaultColorId = StringUtils.substringBetween(url, "_color=", "&");
		if (StringUtils.isBlank(defaultColorId)) {
			defaultColorId = StringUtils.substringAfter(url, "_color=");
		}
		if(StringUtils.isBlank(defaultColorId)){
			Elements defaultColorElements = doc.select("ul.swatches.Color li.selected a");
			if(CollectionUtils.isNotEmpty(defaultColorElements)){
				defaultColorId = defaultColorElements.get(0).attr("data-colorid");
			}
		}
		
		// color JSONObject
		JSONObject colorJSONObject = new JSONObject();
		// size JSONObject
		JSONObject sizeJSONObject = new JSONObject();
		// switchimage JSONObject
		JSONObject switchImgJSONObject = new JSONObject();
		// sku 属性对应请求链接
		List<String> requestUrls = new ArrayList<String>();
		addToRequestUrls(productId, requestUrls, colorJSONObject, sizeJSONObject, switchImgJSONObject, doc);

		// color images
		JSONObject colorImageJSONObject = new JSONObject();
		colorImagesPackage(productId, colorImageJSONObject);

		// sku jsonarray
		JSONArray skuJSONArray = new JSONArray();

		new NauticaHandler().process(requestUrls, skuJSONArray, context);

		@SuppressWarnings("unchecked")
		List<String> images = (List<String>) colorImageJSONObject.get("images");

		// sku
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		// style
		Set<String> styleSet = new HashSet<String>();
		if (CollectionUtils.isNotEmpty(skuJSONArray)) {
			for (Object object : skuJSONArray) {
				JSONObject skuJSONObject = (JSONObject) object;
				// selectlist
				LSelectionList lselectlist = new LSelectionList();

				// skuId
				String skuId = skuJSONObject.getString("skuId");

				// color
				String colorId = skuJSONObject.getString("colorId");
				String color = colorJSONObject.getString(colorId);
				// size
				String sizeId = skuJSONObject.getString("sizeId");
				String size = sizeJSONObject.getString(sizeId);

				// stock
				int stock_status = skuJSONObject.getIntValue("stock_status");
				int stock_number = 0;
				if (stock_status > 0) {
					spuStockStatus = 1;
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
				float orign_price = skuJSONObject.getFloatValue("orignPrice");
				float sale_price = skuJSONObject.getFloatValue("salePrice");
				String unit = skuJSONObject.getString("unit");
				// lselectlist
				lselectlist.setGoods_id(skuId);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
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
					// images
					if (StringUtils.equals(defaultColorId, colorId)) {
						lStyleList.setDisplay(true);
						int save = (int) ((1 - (sale_price / orign_price)) * 100);
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					List<Image> styleImages = getImagesAndSort(colorId, images);
					
					context.getUrl().getImages().put(skuId, styleImages);

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
		// sku
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(spuStockStatus));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));
		// title
		retBody.setTitle(new Title(title, "", "", ""));
		category_package(brand, title, doc, retBody);
		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		String gender = getSex(retBody.getCategory().toString());
		propMap.put("s_gender", gender);
		retBody.setProperties(propMap);
		// description
		desc_package(doc, retBody);
		
		setOutput(context, retBody);

	}

	/**
	 * 遍历images 获取对应colorId 图片地址，并进行排序 A B C ...
	 * 
	 * @param colorId
	 * @param images
	 * @return
	 */
	private static List<Image> getImagesAndSort(String colorId, List<String> images) {
		List<Image> styleImages = new ArrayList<Image>();
		List<String> imageUrls = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(images)) {
			for (String imageUrl : images) {
				String substringBetween = StringUtils.substringBetween(imageUrl, IMAGE_PREFFIX, IMAGE_SUFFIX);
				if (StringUtils.equals(colorId, substringBetween)) {
					imageUrls.add(imageUrl);
				}
			}
		}

		if (CollectionUtils.isNotEmpty(imageUrls)) {
			sortStyleImages(styleImages, imageUrls);
		}

		return styleImages;
	}

	/**
	 * imageUrls 排序 加入到styleImages
	 * 
	 * @param styleImages
	 * @param imageUrls
	 */
	private static void sortStyleImages(List<Image> styleImages, List<String> imageUrls) {
		Set<String> setImages = new TreeSet<String>();
		setImages.addAll(imageUrls);
		Iterator<String> iterator = setImages.iterator();
		while (iterator.hasNext()) {
			styleImages.add(new Image(iterator.next()));
		}

	}

	/**
	 * 
	 * @param productId
	 * @param colorImageJSONObject
	 */
	private static void colorImagesPackage(String productId, JSONObject colorImageJSONObject) {
		String imageJSONUrl = StringUtils.replacePattern(IMAGE_JSON_URL, "\\{\\}", productId);
		String imageData = HttpUtils.get(imageJSONUrl);
		if (StringUtils.isNotBlank(imageData)) {
			String imageJSONData = StringUtils.substringBetween(imageData, "initialize({product_view:", "})");
			imageJSONData = StringUtils.substringBefore(imageJSONData, ",custom_template");
			JSONObject imageJSONObject = JSONObject.parseObject(imageJSONData);
			JSONObject jsonObject = imageJSONObject.getJSONObject("product_view");
			if (MapUtils.isNotEmpty(jsonObject)) {
				// images 封装对应colorId images
				JSONArray imageJSONArray = jsonObject.getJSONArray("image");

				// images
				List<String> images = new ArrayList<String>();
				if (CollectionUtils.isNotEmpty(imageJSONArray)) {
					for (Object object : imageJSONArray) {
						JSONObject imageJSONObjectTemp = (JSONObject) object;
						String imageUrl = imageJSONObjectTemp.getString("url");
						if (StringUtils.contains(imageUrl, "2200x2200")
								&& StringUtils.contains(imageUrl, "touchzoom")) {
							imageUrl = StringUtils.replacePattern(imageUrl, "../../../", "http://cdn.fluidretail.net/");
							images.add(imageUrl);
						}
					}
				}
				colorImageJSONObject.put("images", images);
			}
		}
	}

	/**
	 * 获取商品 对应属性对应url，用作封装request_url
	 * 
	 * @param productId
	 * @param requestUrls
	 * @param sizeJSONObject
	 * @param colorJSONObject
	 * @param switchImgJSONObject
	 * @param doc
	 */
	private static void addToRequestUrls(String productId, List<String> requestUrls, JSONObject colorJSONObject,
			JSONObject sizeJSONObject, JSONObject switchImgJSONObject, Document doc) {
		Elements colorElements = doc.select("ul.swatches.Color li a");
		Elements sizeElements = doc.select("select#va-size option");
		if (CollectionUtils.isNotEmpty(colorElements)) {
			for (Element element : colorElements) {
				String colorId = element.attr("data-colorid");
				String color = element.attr("title");
				// color jsonobject put
				colorJSONObject.put(colorId, color);
				String switchImg = element.attr("style");
				if (StringUtils.isNotBlank(switchImg)) {
					switchImg = StringUtils.substringBetween(switchImg, "url(", ")");
				}
				// switch img jsonobject put
				switchImgJSONObject.put(colorId, switchImg);

				if (CollectionUtils.isNotEmpty(sizeElements)) {
					// color size 组合
					for (Element element2 : sizeElements) {
						String sizeValue = element2.attr("value");
						String sizeName = element2.text();
						if (StringUtils.isBlank(sizeName) && StringUtils.isBlank(sizeValue)) {
							continue;
						}
						String sizeId = StringUtils.substringBetween(sizeValue, "_size=", "&");
						if (StringUtils.isBlank(sizeId)) {
							sizeId = StringUtils.substringAfter(sizeValue, "_size=");
						}

						// size JSONObject put
						sizeJSONObject.put(sizeId, sizeName);

						// request url
						String replacePattern = StringUtils.replacePattern(REQUEST_URL_WITH_SIZE, "\\{\\}", productId);
						replacePattern = StringUtils.replacePattern(replacePattern, "\\(\\)", colorId);
						replacePattern = StringUtils.replacePattern(replacePattern, "\\[\\]", sizeId);

						requestUrls.add(replacePattern);
					}
				} else {
					// request url
					String replacePattern = StringUtils.replacePattern(REQUEST_URL_NO_SIZE, "\\{\\}", productId);
					replacePattern = StringUtils.replacePattern(replacePattern, "\\(\\)", colorId);
					requestUrls.add(replacePattern);
				}

			}
		}
	}

	/**
	 * category
	 * 
	 * @param brand
	 * @param title
	 * @param doc
	 * @param retBody
	 */
	private static void category_package(String brand, String title, Document doc, RetBody retBody) {
		Elements categoryElements = doc.select("div.breadcrumb a:not(:first-child)");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element element : categoryElements) {
				String cate = element.text();
				cats.add(cate);
				breads.add(cate);
			}
		}
		cats.add(title);
		breads.add(title);
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
				"div.tab-content.description , div.tab-content.description div.bulletDescription ul.bullets li");
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

	private static String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if (StringUtils.containsIgnoreCase(cat, SEX_WOMEN)) {
			gender = "women";
		} else if (StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		}
		return gender;
	}

}
