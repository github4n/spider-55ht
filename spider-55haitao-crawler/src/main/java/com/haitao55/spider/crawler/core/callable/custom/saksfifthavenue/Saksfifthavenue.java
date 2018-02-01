package com.haitao55.spider.crawler.core.callable.custom.saksfifthavenue;

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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * Saksfifthavenue网站收录
 * 
 * @author denghuan
 *
 */
public class Saksfifthavenue extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private String imageUrl = "http://s7d9.scene7.com/is/image/saks/{###}?wid=492&hei=656";
	private static final String domain = "www.saksfifthavenue.com";
	private static final String SAKS_API = "http://10.128.0.6/mk/saks.action?url=";
	

	@Override
	public void invoke(Context context) throws Exception {	
		String url = context.getUrl().getValue();
		String parentUrl = context.getUrl().getParentUrl();
		if (StringUtils.containsIgnoreCase(url, "<>")) {
			url = url.replace("<>", "%3C%3E");
		}
		
		LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US",true);
		String content = luminatiHttpClient.request(url,getHeaders(parentUrl));
		context.setHtmlPageSource(content);
		
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String outStock = doc.select(".product__sold-out-message").text();
			if (StringUtils.isNotBlank(outStock) && "Sold Out".equals(outStock)) {
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"itemUrl:" + context.getUrl().toString() + " not found..");
			}
			 String productDetails = StringUtils.substringBetween(content, "{\"ProductDetails", "</script>");
			 
			//String productDetails = StringUtils.substringBetween(content, "{\"ProductDetails\":", "}</script>");

			if (StringUtils.isNotBlank(productDetails)) {
				JSONObject proJsonObject = JSON.parseObject("{\"ProductDetails"+productDetails);
				String productDetail = proJsonObject.getString("ProductDetails");
				JSONObject jsonObject = JSON.parseObject(productDetail);
				 
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
					if (StringUtils.isNotBlank(mainImage) && StringUtils.containsIgnoreCase(mainImage, "saks/")) {
						mainImage = mainImage.replace("saks/", "");
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
						if("soldout".equals(status_alias)){
							continue;
						}
						if ("-1".equals(color_id) && "-1".equals(size_id)) {
							continue;
						}

						LSelectionList lSelectionList = new LSelectionList();
						String skuId = skuObject.getString("sku_id");
						String price = skuObject.getString("price");
						JSONObject pirceJsonObject = JSON.parseObject(price);

						String list_price_label = pirceJsonObject.getString("list_price_label");
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

						if (StringUtils.isNotBlank(list_price_label) && "Was".equals(list_price_label)) {
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
		//System.out.print(rebody.parseTo());
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

	private static Map<String, Object> getHeaders(String parentUrl) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.saksfifthavenue.com");
		headers.put("Referer", parentUrl);
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		//headers.put("Cookie", setCookie());
		return headers;
	}
	
	public static void main(String[] args) throws Exception {
//		 String content = Crawler.create().timeOut(30000).url(SAKS_API+"https://www.saksfifthavenue.com/main/ProductDetail.jsp?PRODUCT%3C%3Eprd_id=845524447040467")
		Saksfifthavenue sk = new Saksfifthavenue();
		Context  con = new Context();
		con.setRunInRealTime(true);
		con.setUrl(new Url("https://www.saksfifthavenue.com/main/ProductDetail.jsp?PRODUCT<>prd_id=845524447126510"));
		sk.invoke(con);
	}
}
