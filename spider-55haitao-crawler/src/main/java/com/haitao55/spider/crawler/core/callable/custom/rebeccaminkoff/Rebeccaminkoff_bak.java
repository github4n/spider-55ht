package com.haitao55.spider.crawler.core.callable.custom.rebeccaminkoff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * @Description:
 * @author: zhoushuo
 * @date: 2016年11月22日 下午5:53:47
 */
public class Rebeccaminkoff_bak extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.rebeccaminkoff.com";

	private static final String KEY_UNIT = "unit";
	private static final String KEY_PRICE = "price";

	private static final String CSS_TITLE = "div.product-name span.h1";
	private static final String CSS_BREADS = "div.breadcrumbs>ul>li>a";
	private static final String CSS_DESCRIPTION = "div.tab-content p";
	private static final String CSS_DETAIL = "div.tab-content ul>li";
	private static final String CSS_PICTURE = "ul.product-image-thumbs>li>a img";
	private static final String CSS_ORIGINAL_PRICE = "div.product-essential div.price-info p.old-price span.price";
	private static final String CSS_SALE_PRICE = "div.product-essential div.price-info p.special-price span.price";
	private static final String CSS_ONLY_ONE_PRICE = "div.product-essential div.price-info span.regular-price";
	private static final String CSS_SKU_ORIGINAL_PRICE = "p.old-price span.price";
	private static final String CSS_SKU_SALE_PRICE = "p.special-price span.price";
	private static final String CSS_SKU_ONLY_ONE_PRICE = "span.regular-price";
	private static final String CSS_INVENTORY = "div.product-essential div.item-availability";

	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, context.getCurrentUrl());
			String title = "";
			String brand = "Rebecca Minkoff";
			String productId = "";
			int inventoryStatus = 0; // spu库存状态
			Map<String, Object> original_price = null;
			Map<String, Object> sale_price = null;
			int save = 0;
			String style_cate_name = "COLOR";
			List<String> switch_imgs = new ArrayList<>();

			// 设置标题
			Elements etitle = document.select(CSS_TITLE);
			if (CollectionUtils.isNotEmpty(etitle)) {
				title = etitle.get(0).text().trim();
			} else {// 过滤掉没有Title的商品
				logger.error("Error while fetching url {} because of no title.", context.getCurrentUrl());
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
						"Error while fetching title with url " + context.getCurrentUrl() + " from rebeccaminkoff.");
			}

			// 获取productId
			Elements productIdEms = document.getElementsByAttributeValue("name", "product");
			for (Element e : productIdEms) {
				if (StringUtils.isNotBlank(e.attr("value"))) {
					productId = e.attr("value");
					break;
				}
			}
			if (StringUtils.isBlank(productId)) {
				logger.error("get productId error for url {}", context.getCurrentUrl());
				productId = UUID.randomUUID().toString();
			}

			String docid = SpiderStringUtil.md5Encode(domain + productId);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			Elements ebread = document.select(CSS_BREADS);
			for (Element e : ebread) {
				breads.add(e.text());
				categories.add(e.text());
			}
			breads.add(title);
			categories.add(title);

			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			StringBuilder sb = new StringBuilder();
			Elements eDescriptions = document.select(CSS_DESCRIPTION);
			int count = 1;
			if (CollectionUtils.isNotEmpty(eDescriptions)) {
				for (Element e : eDescriptions) {
					featureMap.put("feature-" + count, e.text().trim());
					count++;
					sb.append(e.text().trim()).append(".");
				}
			}
			Elements eDetails = document.select(CSS_DETAIL);
			if (CollectionUtils.isNotEmpty(eDetails)) {
				for (Element e : eDetails) {
					featureMap.put("feature-" + count, e.text().trim());
					count++;
					sb.append(e.text().trim()).append(". ");
				}
			}
			descMap.put("en", sb.toString());

			// 设置spu图片
			List<Picture> pl = new ArrayList<>();
			Elements ePictures = document.select(CSS_PICTURE);
			for (Element e : ePictures) {
				pl.add(new Picture(e.absUrl("src"), ""));
			}
			LImageList image_list = new LImageList(pl);

			// 设置spu价格
			Elements tempOriginalPrice = document.select(CSS_ORIGINAL_PRICE);
			if (CollectionUtils.isEmpty(tempOriginalPrice))
				tempOriginalPrice = document.select(CSS_ONLY_ONE_PRICE);
			if (CollectionUtils.isNotEmpty(tempOriginalPrice)) {
				String tempOrigPrice = trimStringIgnoreNull(tempOriginalPrice.get(0).text());
				if (StringUtils.isNotBlank(tempOrigPrice)) {
					original_price = formatPrice(tempOrigPrice, context.getCurrentUrl());
				}
			} else {
				logger.error("can not select original price by css, url is {}", context.getCurrentUrl());
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
						"Error while fetching title with url " + context.getCurrentUrl() + " from rebeccaminkoff.");
			}
			Elements tempSalePrice = document.select(CSS_SALE_PRICE);
			if (CollectionUtils.isNotEmpty(tempSalePrice)) {
				String tempSaPrice = trimStringIgnoreNull(tempSalePrice.get(0).text());
				if (StringUtils.isNotBlank(tempSaPrice)) {
					sale_price = formatPrice(tempSaPrice, context.getCurrentUrl());
				}
			} else {
				sale_price = original_price;
			}
			if(original_price != null && sale_price != null){
				if((float) original_price.get(KEY_PRICE) < (float) sale_price.get(KEY_PRICE))
					original_price = sale_price;
			}
			if (original_price != null)
				save = Math
						.round((1 - (float) sale_price.get(KEY_PRICE) / (float) original_price.get(KEY_PRICE)) * 100);

			// 设置spu库存状态
			Elements eInventory = document.select(CSS_INVENTORY);
			if (CollectionUtils.isNotEmpty(eInventory)) {
				if ("In Stock".equals(trimStringIgnoreNull(eInventory.get(0).ownText())))
					inventoryStatus = 1;
			}

			// 设置性别
			Map<String, Object> properties = new HashMap<>();
			properties.put("s_gender", "women");

			// 设置sku
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();

			String skuString = StringUtils.substringBetween(content, "var spConfig", "</script>");
			skuString = StringUtils.substringBetween(skuString, "(", ")");
			if (StringUtils.isBlank(skuString)) {
//				String color = document.getElementById("product-color").attr("data-color");
//				LStyleList style = new LStyleList();
//				style.setDisplay(true);
//				style.setGood_id(productId);
//				style.setStyle_cate_id(0);
//				style.setStyle_cate_name(style_cate_name);
//				style.setStyle_name(color);
//				style.setStyle_id(color);
//				style.setStyle_switch_img("");
				List<Image> styleImgs = getPicsByStyleId(pl);
				context.getUrl().getImages().put(productId + "", styleImgs);
//				l_style_list.add(style);

//				List<Selection> slist = new ArrayList<>();
//				LSelectionList selection = new LSelectionList();
//				selection.setGoods_id(productId);
//				selection.setOrig_price((float) original_price.get(KEY_PRICE));
//				selection.setPrice_unit(original_price.get(KEY_UNIT).toString());
//				selection.setSale_price((float) sale_price.get(KEY_PRICE));
//				selection.setSelections(slist);
//				selection.setStock_number(0);
//				selection.setStock_status(inventoryStatus);
//				selection.setStyle_id(color);
//				l_selection_list.add(selection);
			}
			if (StringUtils.isNotBlank(skuString)) {
				JSONObject json = new JSONObject();
				json = JSONObject.parseObject(StringUtils.trim(skuString));
				/**
				 * 该网站目前被确认的属性代号： “221”代表颜色Color； "223"代表数字类型的Size：Size 00-31；
				 * "224"代表字母类型的Size：Size XXS-L;
				 */
				JSONObject attributes = json.getJSONObject("attributes");
				if(attributes == null)
					return;
				JSONArray colorsJson = null;
				try {
					colorsJson = attributes.getJSONObject("221").getJSONArray("options");
				} catch (Exception e) {
					logger.error("get color error for url {}",context.getCurrentUrl());
					return;
				}
				JSONObject size = attributes.getJSONObject("223");
				if (size == null)
					size = attributes.getJSONObject("224");
				JSONArray sizesJson = null;
				if (size != null)
					sizesJson = size.getJSONArray("options");
				JSONObject imagesJson = json.getJSONObject("swatchImages");
				JSONObject pricesJson = json.getJSONObject("prices");

				List<RebeccaminkoffSku> skus = new ArrayList<>();
				// 解析color
				List<Color> colors = new ArrayList<>();
				int colorLength = colorsJson.size();
				for (int i = 0; i < colorLength; i++) {
					JSONObject colorJson = colorsJson.getJSONObject(i);
					JSONArray products = colorJson.getJSONArray("products");
					Color color = new Color();
					color.setId(colorJson.getString("id"));
					color.setValue(colorJson.getString("label"));
					String img = colorJson.getJSONObject("swatch").getString("img");
					if ("false".equals(img))
						img = "";
					color.setSwitchImg(img);
					if (StringUtils.isNotBlank(img))
						switch_imgs.add(img);
					List<String> skuids = new ArrayList<>();
					int len = products.size();
					for (int j = 0; j < len; j++) {
						skuids.add(products.getString(j));
						RebeccaminkoffSku sku = new RebeccaminkoffSku();
						sku.setSkuid(trimStringIgnoreNull(products.getString(j)));
						sku.setColorId(color.getId());
						sku.setColorName(color.getValue());
						skus.add(sku);
					}
					color.setSkuids(skuids);
					colors.add(color);
				}
				if (sizesJson != null) {
					// 解析size
					List<RebeccaminkoffSku> tempskus = new ArrayList<>();
					List<Size> sizes = new ArrayList<>();
					int sizeLength = sizesJson.size();
					for (int i = 0; i < sizeLength; i++) {
						JSONObject sizeJson = sizesJson.getJSONObject(i);
						JSONArray products = sizeJson.getJSONArray("products");
						Size s = new Size();
						s.setId(sizeJson.getString("id"));
						s.setValue(sizeJson.getString("label"));
						List<String> skuids = new ArrayList<>();
						int len = products.size();
						for (int j = 0; j < len; j++) {
							skuids.add(products.getString(j));
							RebeccaminkoffSku sku = new RebeccaminkoffSku();
							sku.setSkuid(trimStringIgnoreNull(products.getString(j)));
							sku.setSizeId(s.getId());
							sku.setSizeName(s.getValue());
							tempskus.add(sku);
						}
						s.setSkuids(skuids);
						sizes.add(s);
					}
					// 封装sku
					for (RebeccaminkoffSku sku : skus) {
						for (RebeccaminkoffSku sizeSku : tempskus) {
							if (sku.getSkuid().equals(sizeSku.getSkuid())) {
								sku.setSizeId(sizeSku.getSizeId());
								sku.setSizeName(sizeSku.getSizeName());
								break;
							}
						}
					}
				}

				for (RebeccaminkoffSku sku : skus) {
					List<String> imgs = new ArrayList<>();
					JSONArray imgsJson = imagesJson.getJSONObject(sku.getSkuid()).getJSONArray("galleryImages");
					int len = imgsJson.size();
					for (int i = 0; i < len; i++) {
						String img = imgsJson.getJSONObject(i).getString("url");
						imgs.add(img);
					}
					sku.setImgs(imgs);
					for (Color color : colors) {
						if (color.getImgs() == null && color.getSkuids().contains(sku.getSkuid())) {
							color.setImgs(imgs);
							break;
						}
					}

					Document doc = Jsoup.parse(trimStringIgnoreNull(pricesJson.getString(sku.getSkuid())));
					Elements eoriginal = doc.select(CSS_SKU_ORIGINAL_PRICE);
					if (CollectionUtils.isEmpty(eoriginal))
						eoriginal = doc.select(CSS_SKU_ONLY_ONE_PRICE);
					Elements esale = doc.select(CSS_SKU_SALE_PRICE);
					if (CollectionUtils.isEmpty(esale))
						esale = doc.select(CSS_SKU_ONLY_ONE_PRICE);
					if (CollectionUtils.isNotEmpty(eoriginal)) {
						Map<String, Object> map = formatPrice(eoriginal.get(0).text(), context.getCurrentUrl());
						sku.setUnit(map.get(KEY_UNIT).toString());
						sku.setOriginalPrice((float) map.get(KEY_PRICE));
					}
					if (CollectionUtils.isNotEmpty(esale)) {
						Map<String, Object> map = formatPrice(esale.get(0).text(), context.getCurrentUrl());
						sku.setSalePrice((float) map.get(KEY_PRICE));
					}
				}

				// 设置style_list
				int num = 0;
				for (Color color : colors) {
					LStyleList style = new LStyleList();
//					style.setStyle_switch_img(color.getSwitchImg());
					style.setStyle_switch_img("");
					style.setGood_id(color.getSkuids().get(0));
					style.setStyle_id(color.getValue());
					style.setStyle_cate_id(0);
					style.setStyle_cate_name(style_cate_name);
					style.setStyle_name(color.getValue());
					List<Picture> stylePictures = new ArrayList<>();
					List<String> imgs = color.getImgs();
					if (CollectionUtils.isNotEmpty(imgs)) {
						for (String img : imgs) {
							Picture p = new Picture(img, "");
							stylePictures.add(p);
						}
					}
					if (num == 0) {
						image_list = new LImageList(stylePictures);
						style.setDisplay(true);
					} else
						style.setDisplay(false);
					List<Image> styleImgs = getPicsByStyleId(stylePictures);
					context.getUrl().getImages().put(color.getSkuids().get(0) + "", styleImgs);
					l_style_list.add(style);
					num++;
				}
				// 设置selection_list
				for (RebeccaminkoffSku sku : skus) {
					List<Selection> slist = new ArrayList<>();
					if (sizesJson != null) {
						Selection sec = new Selection(0, sku.getSizeName(), "Size");
						slist.add(sec);
					}
					LSelectionList selection = new LSelectionList();
					selection.setGoods_id(sku.getSkuid());
					selection.setOrig_price(sku.getOriginalPrice());
					selection.setPrice_unit(sku.getUnit());
					selection.setSale_price(sku.getSalePrice());
					if(selection.getOrig_price() < selection.getSale_price())
						selection.setOrig_price(selection.getSale_price());
					selection.setSelections(slist);
					selection.setStock_number(sku.getInventory());
					selection.setStock_status(inventoryStatus);
					selection.setStyle_id(sku.getColorName());
					l_selection_list.add(selection);
				}
			}

			// 将switch_imgs添加到可下载列表
			List<Picture> switchPictures = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(switch_imgs)) {
				for (String img : switch_imgs) {
					Picture p = new Picture(img, "");
					switchPictures.add(p);
				}
			}
			//由于部分switch_img是背景色，无法统一，所以决定舍弃switch_img
//			context.getUrl().getImages().put(UUID.randomUUID().toString(), getPicsByStyleId(switchPictures));

			// 设置rebody
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title.trim(), "", "", ""));
			rebody.setBrand(new Brand(brand, "", "", ""));
			rebody.setBreadCrumb(breads);
			rebody.setCategory(categories);
			rebody.setFeatureList(featureMap);
			rebody.setDescription(descMap);
			rebody.setImage(image_list);
			rebody.setPrice(new Price((float) original_price.get(KEY_PRICE), save, (float) sale_price.get(KEY_PRICE),
					original_price.get(KEY_UNIT).toString()));
			rebody.setStock(new Stock(inventoryStatus));
			rebody.setSku(new Sku(l_selection_list, l_style_list));
			rebody.setProperties(properties);
		}
		setOutput(context, rebody);
	}

	// 避免直接使用trim()方法发生空指针异常
	private String trimStringIgnoreNull(String string) {
		if (string == null)
			return StringUtils.EMPTY; // 这里不返回null的原因是，根据业务场景，不希望返回null
		return string.trim();
	}

	// 获取价格
	private Map<String, Object> formatPrice(String tempPrice, String url) {
		tempPrice = trimStringIgnoreNull(tempPrice);
		if (tempPrice.contains("-")) {
			tempPrice = trimStringIgnoreNull(StringUtils.substringBefore(tempPrice, "-"));
		}
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isBlank(tempPrice)) {
			logger.error("input price is null and url:{}", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url + " from rebeccaminkoff.");
		}
		String currency = StringUtils.substring(tempPrice, 0, 1);
		Currency cuy = Currency.codeOf(currency);
		if (cuy == null) {
			logger.error("currency is null and url:{}", url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url + " from rebeccaminkoff.");
		}
		map.put(KEY_UNIT, cuy.name());
		tempPrice = StringUtils.substring(tempPrice, 1).replace(",", "");
		try {
			float price = Float.parseFloat(tempPrice);
			price = formatNum(price);
			map.put(KEY_PRICE, price);
		} catch (NumberFormatException e) {
			logger.error("format price error and url is {},because of {}", url, e.getMessage());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
					"Error while fetching price with url " + url + " from rebeccaminkoff.");
		}
		return map;
	}

	private float formatNum(float num) {
		return ((float) Math.round(num * 100)) / 100; // 四舍五入法保留两位小数
	}

	private List<Image> getPicsByStyleId(List<Picture> list) {
		List<Image> pics = new ArrayList<Image>();
		if (list != null && list.size() > 0) {
			for (Picture pic : list) {
				String imageUrl = pic.getSrc();
				if (StringUtils.isNotBlank(imageUrl)) {
					Image image = new Image(imageUrl);
					pics.add(image);
				}
			}
		}
		return pics;
	}

}