package com.haitao55.spider.crawler.core.callable.custom.saksoff5th;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * Saksoff5th打折网网站收录
 * 
 * @author denghuan
 *
 */
public class Saksoff5th extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private String imageUrl = "http://s7d9.scene7.com/is/image/saksoff5th/{###}?wid=492&hei=656";
	private static final String domain = "www.saksoff5th.com";
	// 104.154.77.103
	private static final String SAKS_API = "http://10.128.0.6/mk/saks.action?url=";

	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String parentUrl = context.getUrl().getParentUrl();
		if (StringUtils.containsIgnoreCase(url, "<>")) {
			url = url.replace("<>", "%3C%3E");
		}
		
		LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US",true);
		String content = luminatiHttpClient.request(url, getHeaders(parentUrl));
		context.setHtmlPageSource(content);

		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			// String productDetails = StringUtils.substringBetween(content, "
			// type=\"application/json\">", "</script>");
			 String productDetails = StringUtils.substringBetween(content, "{\"ProductDetails", "</script>");
			Document doc = Jsoup.parse(content);
			String outStock = doc.select(".product__sold-out-message").text();
			if (StringUtils.isNotBlank(outStock) && "Sold Out".equals(outStock)) {
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"itemUrl:" + context.getUrl().toString() + " not found..");
			}
			if (StringUtils.isNotBlank(productDetails)) {
				JSONObject proJsonObject = JSON.parseObject("{\"ProductDetails"+productDetails);
				String productDetail = proJsonObject.getString("ProductDetails");
				JSONObject jsonObject = JSON.parseObject(productDetail);
				// String productDetail =
				// jsonObject.getString("ProductDetails");
				// JSONObject jsonObjectProduct =
				// JSON.parseObject(productDetail);
				String mainProducts = jsonObject.getString("main_products");
				JSONArray jsonArray = JSONArray.parseArray(mainProducts);
				Sku sku = new Sku();
				Map<String, String> skuMap = new HashMap<String, String>();
				Map<String, SaksStockVo> stockMap = new HashMap<String, SaksStockVo>();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				for (int i = 0; i < jsonArray.size(); i++) {
					JSONObject productsObject = jsonArray.getJSONObject(i);
					Map<String, SaksColorVo> colorMap = getColors(productsObject);
					Map<String, SaksSizeVo> sizeMap = getSizes(productsObject);
					// String jsonBradName =
					// productsObject.getString("brand_name");
					String addBag = productsObject.getString("add_to_bag_submit_btn");
					String isStock = StringUtils.substringBetween(addBag, "enabled\":", ",");// 品牌

					String brand = doc.select(".product-overview__brand a").text();
					String title = doc.select("h1.product-overview__short-description").text();
					// String brand = StringUtils.substringBetween(jsonBradName,
					// "label\":\"", "\",");//品牌
					// String title =
					// productsObject.getString("short_description");//标题
					String product_id = productsObject.getString("product_id");// 商品Id
					String description = productsObject.getString("description");// 商品描述
					String desc = Jsoup.parse(description).text();

					String productPrice = productsObject.getString("price");// 标题
					JSONObject productPirceJsonObject = JSON.parseObject(productPrice);
					// String enabled =
					// productPirceJsonObject.getString("enabled");
					String spu_list_price_label = productPirceJsonObject.getString("list_price_label");
					String spu_list_local_currency_value = StringUtils.EMPTY;

					if (StringUtils.isNotBlank(spu_list_price_label) && "Was".equals(spu_list_price_label)) {
						String spu_list_price = productPirceJsonObject.getString("list_price");
						JSONObject spu_list_priceJsonObject = JSON.parseObject(spu_list_price);
						spu_list_local_currency_value = spu_list_priceJsonObject.getString("local_currency_value");
						if (StringUtils.containsIgnoreCase(spu_list_local_currency_value, "-")) {
							spu_list_local_currency_value = spu_list_local_currency_value.substring(0,
									spu_list_local_currency_value.indexOf("-"));
						}
					}

					String salPriceObject = productPirceJsonObject.getString("sale_price");
					JSONObject salePirceJson = JSON.parseObject(salPriceObject);
					String pro_currency_price = salePirceJson.getString("local_currency_value");
					String proUnitCode = salePirceJson.getString("local_currency_code");
					if (StringUtils.containsIgnoreCase(pro_currency_price, "-")) {
						pro_currency_price = pro_currency_price.substring(0, pro_currency_price.indexOf("-"));
					}

					String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
					String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
					rebody.setDOCID(docid);
					rebody.setSite(new Site(domain));
					rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
					rebody.setTitle(new Title(title, ""));
					rebody.setBrand(new Brand(brand, ""));
					Map<String, Object> featureMap = new HashMap<String, Object>();
					Map<String, Object> descMap = new HashMap<String, Object>();
					featureMap.put("feature-1", desc);
					rebody.setFeatureList(featureMap);
					descMap.put("en", desc);
					rebody.setDescription(descMap);
					List<String> cats = new ArrayList<String>();
					List<String> breads = new ArrayList<String>();
					String cateContent = StringUtils.substringBetween(content, "is-selected", "<ul");
					String category = StringUtils.EMPTY;
					if (StringUtils.isNotBlank(cateContent)) {
						Document docment = Jsoup.parse(cateContent);
						category = docment.select("a").text();
						if (StringUtils.isNotBlank(category)) {
							cats.add(category);
							breads.add(category);
						}
					} else {
						cats.add("home");
						cats.add(title);
						breads.add("home");
						breads.add(title);
					}

					rebody.setCategory(cats);
					rebody.setBreadCrumb(breads);
					String gender = "";
					Map<String, Object> propMap = new HashMap<String, Object>();
					if (StringUtils.isNotBlank(category) && StringUtils.containsIgnoreCase(category, "men")) {
						gender = "men";
					} else if (StringUtils.containsIgnoreCase(category, "women")) {
						gender = "women";
					} else {
						gender = "";
					}
					propMap.put("s_gender", gender);
					rebody.setProperties(propMap);

					List<Image> imageList = new ArrayList<Image>();
					String media = productsObject.getString("media");
					String mainImage = StringUtils.substringBetween(media, "image_set\":\"", "\",");
					if (StringUtils.isNotBlank(mainImage) && StringUtils.containsIgnoreCase(mainImage, "saksoff5th/")) {
						mainImage = mainImage.replace("saksoff5th/", "");
						String image = imageUrl.replace("{###}", mainImage);
						imageList.add(new Image(image));
					}

					JSONObject mediaObject = JSON.parseObject(media);
					JSONArray images = mediaObject.getJSONArray("images");// 图片
					for (int j = 0; j < images.size(); j++) {
						String imageJson = images.get(j).toString();
						String image = imageUrl.replace("{###}", imageJson);
						Image ig = new Image(image);
						imageList.add(ig);
					}

					String skus = productsObject.getString("skus");// 商品Id
					JSONObject skusObject = JSON.parseObject(skus);
					JSONArray skusJsonArr = skusObject.getJSONArray("skus");
					for (int j = 0; j < skusJsonArr.size(); j++) {
						SaksStockVo skasStockVo = new SaksStockVo();
						JSONObject skuObject = skusJsonArr.getJSONObject(j);
						String color_id = skuObject.getString("color_id");
						String size_id = skuObject.getString("size_id");
						String status_alias = skuObject.getString("status_alias");
						if ("soldout".equals(status_alias)) {
							continue;
						}
						if ("-1".equals(color_id) && "-1".equals(size_id)) {
							continue;
						}

						LSelectionList lSelectionList = new LSelectionList();
						String skuId = skuObject.getString("sku_id");
						String price = skuObject.getString("price");
						JSONObject pirceJsonObject = JSON.parseObject(price);

						String list_local_currency_value = StringUtils.EMPTY;

						String sale_price = pirceJsonObject.getString("sale_price");
						JSONObject salePirceJsonObject = JSON.parseObject(sale_price);

						String currency_price_ = salePirceJsonObject.getString("local_currency_value");
						String unitCode = salePirceJsonObject.getString("local_currency_code");
						if ("DUMMY".equals(currency_price_)) {
							currency_price_ = pro_currency_price;
						}
						if ("DUMMY".equals(unitCode)) {
							unitCode = proUnitCode;
						}

						// String list_price_label =
						// pirceJsonObject.getString("list_price_label");
						// if(StringUtils.isNotBlank(list_price_label) &&
						// "Was".equals(list_price_label)){}
						if (pirceJsonObject.containsKey("list_price")) {
							String list_price = pirceJsonObject.getString("list_price");
							JSONObject list_priceJsonObject = JSON.parseObject(list_price);
							list_local_currency_value = list_priceJsonObject.getString("local_currency_value");
						}

						Float local_currency_price = Float.parseFloat(currency_price_);

						if (StringUtils.isNotBlank(list_local_currency_value)) {
							lSelectionList.setOrig_price(Float.parseFloat(list_local_currency_value));
						} else {
							lSelectionList.setOrig_price(local_currency_price);
						}

						// String status_alias =
						// skuObject.getString("status_alias");
						// String status_label =
						// skuObject.getString("status_label");

						lSelectionList.setGoods_id(skuId);
						lSelectionList.setPrice_unit(unitCode);

						skasStockVo.setSalePrice(local_currency_price);
						if (StringUtils.isNotBlank(list_local_currency_value)) {
							skasStockVo.setOrigPrice(Float.parseFloat(list_local_currency_value));
						}

						lSelectionList.setSale_price(local_currency_price);

						List<Selection> selections = new ArrayList<Selection>();
						int stock = 0;
						if (!"-1".equals(color_id) && !"-1".equals(size_id)) {//
							SaksColorVo saksColor = colorMap.get(color_id);
							SaksSizeVo saksSize = sizeMap.get(size_id);
							if (saksColor != null && saksSize != null) {
								skuMap.put(color_id, skuId);

								Selection selection = new Selection();
								lSelectionList.setStyle_id(saksColor.getLabel());
								selection.setSelect_name("size");
								selection.setSelect_value(saksSize.getValue());
								selections.add(selection);
								if (StringUtils.isNotBlank(status_alias)
										&& (!"soldout".equals(status_alias) && !"waitlist".equals(status_alias))) {// 代表有库存不确定库存数
									stock = 1;
									skasStockVo.setStock(stock);
									stockMap.put(color_id, skasStockVo);
								}
							}
						} else if (!"-1".equals(color_id) && "-1".equals(size_id)) {
							SaksColorVo saksColor = colorMap.get(color_id);
							if (saksColor != null) {
								skuMap.put(color_id, skuId);
								lSelectionList.setStyle_id(saksColor.getLabel());
								if (StringUtils.isNotBlank(status_alias)
										&& (!"soldout".equals(status_alias) && !"waitlist".equals(status_alias))) {
									stock = 1;
									skasStockVo.setStock(stock);
									stockMap.put(color_id, skasStockVo);
								}
							}
						} else if ("-1".equals(color_id) && !"-1".equals(size_id)) {
							SaksSizeVo saksSize = sizeMap.get(size_id);
							if (saksSize != null) {
								skuMap.put(size_id, skuId);
								lSelectionList.setStyle_id(saksSize.getValue());
								if (StringUtils.isNotBlank(status_alias)
										&& (!"soldout".equals(status_alias) && !"waitlist".equals(status_alias))) {
									stock = 1;
									skasStockVo.setStock(stock);
									stockMap.put(size_id, skasStockVo);
								}

							}
						}
						lSelectionList.setStock_status(stock);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}

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
						if ("true".equals(isStock)) {
							spuStock = 1;
						}
					}
					rebody.setStock(new Stock(spuStock));

					boolean display = true;
					boolean notStock = true;
					if (MapUtils.isEmpty(colorMap) && MapUtils.isEmpty(sizeMap)) {
						context.getUrl().getImages().put(product_id, imageList);// picture
						if (StringUtils.isNotBlank(pro_currency_price)) {
							rebody.setPrice(new Price(Float.parseFloat(pro_currency_price), 0,
									Float.parseFloat(pro_currency_price), proUnitCode));
						}
					}

					if (MapUtils.isNotEmpty(colorMap)) {
						Set<Entry<String, SaksColorVo>> entry = colorMap.entrySet();
						Iterator<Entry<String, SaksColorVo>> it = entry.iterator();
						while (it.hasNext()) {
							List<Image> skuImageList = new ArrayList<Image>();
							LStyleList lStyleList = new LStyleList();
							Entry<String, SaksColorVo> saks = it.next();
							SaksColorVo color = saks.getValue();
							lStyleList.setStyle_switch_img("");
							lStyleList.setStyle_cate_id(0);
							lStyleList.setGood_id(skuMap.get(saks.getKey()));
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(color.getLabel());
							lStyleList.setStyle_name(color.getLabel());
							String colorImage = imageUrl.replace("{###}", color.getColorImgUrl());
							Image image = new Image(colorImage);
							skuImageList.clear();
							skuImageList.add(image);

							SaksStockVo saksStock = stockMap.get(saks.getKey());

							if (saksStock != null && saksStock.getStock() != 0) {
								Float salePrice = saksStock.getSalePrice();
								Float origPrice = saksStock.getOrigPrice();
								if (display) {
									lStyleList.setDisplay(true);
									if (colorMap.size() > 1) {
										skuImageList.addAll(imageList);
										context.getUrl().getImages().put(skuMap.get(saks.getKey()), skuImageList);// picture
									} else {
										context.getUrl().getImages().put(skuMap.get(saks.getKey()), imageList);// picture
									}
									display = false;
									if (salePrice != null && origPrice != null) {
										int save = Math.round((1 - salePrice / origPrice) * 100);// discount
										rebody.setPrice(new Price(origPrice, save, salePrice, proUnitCode));
									} else if (salePrice != null && (origPrice == null || origPrice == 0)) {
										if (salePrice > 0) {
											rebody.setPrice(new Price(salePrice, 0, salePrice, proUnitCode));
										}
									}
									l_style_list.add(lStyleList);
									continue;
								}
							}
							if (spuStock == 0) {
								if (notStock) {
									lStyleList.setDisplay(true);
									notStock = false;
								}
								if (StringUtils.isNotBlank(pro_currency_price)) {
									rebody.setPrice(new Price(Float.parseFloat(pro_currency_price), 0,
											Float.parseFloat(pro_currency_price), proUnitCode));
								}
							}
							skuImageList.addAll(imageList);
							context.getUrl().getImages().put(skuMap.get(saks.getKey()), skuImageList);// picture
							l_style_list.add(lStyleList);
						}
					} else if (MapUtils.isNotEmpty(sizeMap)) {
						Set<Entry<String, SaksSizeVo>> entry = sizeMap.entrySet();
						Iterator<Entry<String, SaksSizeVo>> it = entry.iterator();
						while (it.hasNext()) {
							LStyleList lStyleList = new LStyleList();
							Entry<String, SaksSizeVo> saks = it.next();
							SaksSizeVo size = saks.getValue();
							lStyleList.setStyle_switch_img("");
							lStyleList.setStyle_cate_id(0);
							lStyleList.setGood_id(skuMap.get(saks.getKey()));
							lStyleList.setStyle_cate_name("size");
							lStyleList.setStyle_id(size.getValue());
							lStyleList.setStyle_name(size.getValue());
							SaksStockVo saksStock = stockMap.get(saks.getKey());
							if (saksStock != null && saksStock.getStock() != 0) {
								Float salePrice = saksStock.getSalePrice();
								Float origPrice = saksStock.getOrigPrice();
								if (display) {
									lStyleList.setDisplay(true);
									display = false;
									if (salePrice != null && origPrice != null) {
										int save = Math.round((1 - salePrice / origPrice) * 100);// discount
										rebody.setPrice(new Price(origPrice, save, salePrice, proUnitCode));
									} else if (salePrice != null && (origPrice == null || origPrice == 0)) {
										if (salePrice > 0) {
											rebody.setPrice(new Price(salePrice, 0, salePrice, proUnitCode));
										}
									}
								}
							}
							if (spuStock == 0) {
								if (notStock) {
									lStyleList.setDisplay(true);
									notStock = false;
								}
								if (StringUtils.isNotBlank(pro_currency_price)) {
									rebody.setPrice(new Price(Float.parseFloat(pro_currency_price), 0,
											Float.parseFloat(pro_currency_price), proUnitCode));
								}
							}
							context.getUrl().getImages().put(skuMap.get(saks.getKey()), imageList);// picture
							l_style_list.add(lStyleList);
						}
					}
				}
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				rebody.setSku(sku);
			}
		}
		setOutput(context, rebody);
	}

	private Map<String, SaksColorVo> getColors(JSONObject productsObject) {
		Map<String, SaksColorVo> colorMap = new HashMap<String, SaksColorVo>();
		String colors = productsObject.getString("colors");
		JSONObject jsonObject = JSON.parseObject(colors);
		String isEnabled = jsonObject.getString("enabled");
		if (StringUtils.isNotBlank(isEnabled) && "true".equals(isEnabled)) {
			JSONArray colorsJsonArr = jsonObject.getJSONArray("colors");
			for (int i = 0; i < colorsJsonArr.size(); i++) {
				SaksColorVo saksColorVo = new SaksColorVo();
				JSONObject colorObject = colorsJsonArr.getJSONObject(i);
				saksColorVo.setLabel(colorObject.getString("label"));
				saksColorVo.setColorImgUrl(colorObject.getString("colorize_image_url"));
				saksColorVo.setIsSoldout(colorObject.getString("is_soldout"));
				saksColorVo.setIsWaitlistable(colorObject.getString("is_waitlistable"));
				colorMap.put(colorObject.getString("id"), saksColorVo);
			}
		}
		return colorMap;
	}

	private Map<String, SaksSizeVo> getSizes(JSONObject productsObject) {
		Map<String, SaksSizeVo> sizeMap = new HashMap<String, SaksSizeVo>();
		String colors = productsObject.getString("sizes");
		JSONObject jsonObject = JSON.parseObject(colors);
		String isEnabled = jsonObject.getString("enabled");
		if (StringUtils.isNotBlank(isEnabled) && "true".equals(isEnabled)) {
			JSONArray sizesJsonArr = jsonObject.getJSONArray("sizes");
			for (int i = 0; i < sizesJsonArr.size(); i++) {
				JSONObject sizeObject = sizesJsonArr.getJSONObject(i);
				SaksSizeVo saksSizeVo = new SaksSizeVo();
				saksSizeVo.setValue(sizeObject.getString("value"));
				saksSizeVo.setIsSoldout(sizeObject.getString("is_soldout"));
				saksSizeVo.setIsWaitlistable(sizeObject.getString("is_waitlistable"));
				sizeMap.put(sizeObject.getString("id"), saksSizeVo);
			}
		}
		return sizeMap;
	}

	private static String setCookie() {
		String cookie = "PSSALE=DEFAULT; s_vi=[CS]v1|2BBFC6A8852A03E2-4000010540081232[CE]; _qsst_s=1470110363764; cm=Typed%2FBookmarkedTyped%2FBookmarkedundefined; fsr.s=%7B%22v2%22%3A-2%2C%22v1%22%3A1%2C%22rid%22%3A%22de35430-94969414-c2bd-1614-d631a%22%2C%22cp%22%3A%7B%22LoggedIn%22%3A%220%22%2C%22Order_Number%22%3A%220%22%2C%22Loyalty%22%3A%220%22%2C%22PaymentMethod%22%3A%220%22%7D%2C%22to%22%3A10%2C%22c%22%3A%22http%3A%2F%2Fwww.saksfifthavenue.com%2Fmain%2FProductDetail.jsp%22%2C%22pv%22%3A2%2C%22lc%22%3A%7B%22d1%22%3A%7B%22v%22%3A2%2C%22s%22%3Afalse%7D%7D%2C%22cd%22%3A1%2C%22sd%22%3A1%7D; E4X_CURRENCY=USD; PSMS_EXP=2015-12-12 10:07:30; usy46gabsosd=sakscsa__186902665_1470110551648_1470109965637_7417; VIP_PRICE=OFF; sakscsauvt=7d556e63be644f0f85fe6d9767a1e8ab_1467977052492_186902665_1470109965637_2; __cmbTpvTm=383; E4X_COUNTRY=US; rr_rcs=eF5jYSlN9jA3TbG0SElK0TW1MDXSNTEzMtRNSjFO0U1NMTUxSU5OM0gxMeXKLSvJTOGxMNU11DUEAIKcDjg; qb_ss_status=BN7V:Ml&Orj; wlcme=true; PSSALE_EXP=2016-11-28T06:05:03; PS=DEFAULT; sakscsaDBID=14_null; _cavisit=1564961b34e|; _bcvm_vrid_4405020731204665754=717976135557928474T9404A6E759CD8F85A5D70717D9367DED0963C91B63DA6F2C08182266BB6D9A737E18CAE091E48A0A001B33E6B27780831451C7D3A84698A2856CD7092975524C; t013pv=2; skswlcme=true; sf_wdt_customer_id=3ej495i5w; _qubitTracker_s=1448865344691.625675; _bcvm_vid_4405020731204665754=717997464882030170TA07EAD9A28B90220FFCB907F55A43897B4EF0A3A3192C487D119C4DBB34E892B192948CAECE0AA5FE6E5349678CE2C69FC707928B343A12EFF11F220D06C6289; _qst_s=2; v55=%5B%5B%2730NOV2015%253A02%253A35%253A41%27%2C%271448865341925%27%5D%5D; TS54c0f7=ff3393595c1828a0415b2cb4f0a6da71baf55c5b562d702458381e1b55627810e5ef4f1860ac0ec5a4ec577a866d39d0b12f65515283d66affffffff2396c135d4d3511daf1a552cc2aced9040fda161d4d3511d63b6a6986ecb234d7d8b0044d4d3511d48de5bd5c2aced909cbcf6a071256871; c38=saks.com%3Aproduct%20detail%3Atory%20burch%3Athea%20woven%20satchel; _qubitTracker=1467977051015.766497; s_cc=true; s_eVar3=mhpmainm7s3l%7D; TLTSID=8606B122586410583E82C206E1EBBBF1; _qb_se=BN7V:Orj&VZJaGv3; v11=%5B%5B%27Google%27%2C%271448865341924%27%5D%5D; s_ppvl=saks.com%253Aproduct%2520detail%253Atory%2520burch%253Athea%2520woven%2520satchel%2C24%2C24%2C722%2C1280%2C702%2C1280%2C800%2C2%2CP; AKAI=3:1470114983; cache_buster_cookie=1470109963195; _caid=aee51edd-025e-4ed6-a52b-ab1beca70652; sf_siterefer=salesfloor; _ga=GA1.2.1115772862.1467977040; TLTHID=FA855854B30010B30031F091B7666880; sr_exp_rvo=0; v0=1; PS_EXP=2016-11-28T06:05:03; sessionID=1470109952452d9PAQ7T7Ln1TRRg5lEBDq8RcNkTuAVvi7vpUSrvgAwxAQQzKkCNkTDQH; saksBagNumberOfItems=0; _qst=%5B2%2C0%5D; EML1145A=TRUE; sakscsakey=f88749057d9d42b78939df0160696b3f; sr_locale=us; JSESSIONID=NtBLY4hf9lgxngMSTV3HYtSLj3Pw1vhJLLRB5SB1sD6LfcSL3G21!138045752; sf_change_page=true; __cmbDomTm=0; bc_pv_end=717997469065710654TD25459630FE587496A78487CFB4DB24BEA9080CBBC0F56C39801A3648D10EB9ED8C25C5223DADAD831B95E087240B10799A6266966A39CDD17210FECB656D0FD; t013-block=true; _sp_id.7a58=338f8db4405da4bb.1467977052.3.1470110542.1467978776; _sp_ses.7a58=*; ss_opts=BN7V:B&B|_g:VZJYeRl&VZJZ/Yc&C&C; SA74=35bC_M1lzZQyg2DkvwK8hHOaAKlyoWCCdZXvqHjG0aab2bAXlrjafHQ; _qPageNum_saks=1; PSMS=DEFAULT; s_ppv=saks.com%253Aproduct%2520detail%253Atory%2520burch%253Athea%2520woven%2520satchel%2C24%2C24%2C722%2C491%2C702%2C1280%2C800%2C2%2CL; qb_permanent=1467977051015.766497:4:1:4:4:0::0:1:0:BXf41d:BXf41d:::::27.115.111.22:shanghai:7391:china:CN:31.2307:121.473:unknown:156073:shanghai:10698:segments~::Orj; _qsst=1470110364074; saksBrowserWarnings=true; v50=%5B%5B%27natural%2520search%27%2C%271448865341926%27%5D%5D; sr_browser_id=93294629-9cc5-47ea-9d37-a205f5f68afc; qb_session=1:1:14:; PSC=null; mbox=PC#1467977025773-821861.24_2#1475294355|session#1470109947197-10314#1470112215|check#true#1470110415; s_fid=7CF176D3624632AC-3AF53877A17CCF5A; __cmbU=ABJeb19e3N49kewgbWX4yyElqZtnPYxsRBnI7tg5KBVAPUsiDT6-0MwB8npXzh3U5Oix8tQm90vcX3JWCpBpe6_J0OGfIzzIlQ; TS54c0f7=ff3393595c1828a0415b2cb4f0a6da71baf55c5b562d702458381e1b55627810e5ef4f1860ac0ec5a4ec577a866d39d0b12f65515283d66affffffff2396c135d4d3511daf1a552cc2aced9040fda161d4d3511d63b6a6986ecb234d7d8b0044d4d3511d48de5bd5c2aced909cbcf6a071256871; JSESSIONID=NtBLY4hf9lgxngMSTV3HYtSLj3Pw1vhJLLRB5SB1sD6LfcSL3G21!138045752; PSSALE=DEFAULT; PS=DEFAULT; PSMS_EXP=2015-12-12 10:07:30; TLTHID=FA855854B30010B30031F091B7666880; PSSALE_EXP=2016-11-28T06:05:03; PS_EXP=2016-11-28T06:05:03; saksBagNumberOfItems=0; PSMS=DEFAULT";
		return cookie;
	}

	private static Map<String, Object> getHeaders(String parentUrl) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.saksoff5th.com");
		headers.put("Referer", parentUrl);
		headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put("Cookie", setCookie());
		return headers;
	}
}
