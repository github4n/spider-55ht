/**   
 * Copyright © 2016 公司名. All rights reserved.
 * 
 * @Title: BlueFlyHttpItemTest.java 
 * @Prject: spider-55haitao-crawler
 * @Package: com.test.bluefly 
 * @Description: TODO
 * @author: zhoushuo   
 * @date: 2016年10月26日 下午2:47:51 
 * @version: V1.0   
 */
package com.test.ralphlauren;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.crawler.exception.CrawlerException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class RalphlaurenItemTest extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.ralphlauren.com";

	private static final String CSS_PRODUCT_ID = "div.prod-style span.style-num";
	private static final String CSS_TITLE = "h1.prod-title";
	private static final String CSS_BRAND = "div.prod-brand-logo>img";
	private static final String CSS_BREADS = "div.breadcrumbs>a";
	private static final String CSS_DESCRIPTION = "div#longDescDiv>p>span";
	private static final String CSS_DETAIL = "div.prod-details div.detail ul>li";
	private static final String CSS_COLOR_DEFAULT = "ul#color-swatches li:first-child";
	private static final String CSS_OR_PRICE = "div.prod-price span.reg-price";
	private static final String CSS_SALE_PRICE = "div.prod-price";
	private static final String CSS_SWITCH_IMG = "ul#color-swatches";
	private static final String CSS_OFFLINE = "div#oosMessage";

	private static final String OFFLINE_FLAG = "We no longer have the item you are looking for";

	private static final String[] FEMALE_KEY_WORD = { "Women", "Girls", "Gifts for Her" };
	private static final String[] MALE_KEY_WORD = { "Men", "Boys", "Gifts for Him" };

	private String productId = "";

	public static void main(String[] args) {
		RalphlaurenItemTest bf = new RalphlaurenItemTest();
		Context context = new Context();
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=104288706");
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=108780756");
		// 特殊情况，自制款式
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=42445576");
		// 特殊情况，无法显示
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=57851106");
		// 有原价，无库存的情况
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=87040896&ab=cs_ymal");
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=66768476");
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=107715956&parentPage=family");
		// 图片格式问题：不存在v400图片的情况
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=4275297");
		// 下架商品
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=84775646");
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=101915516");
		// 许多sku的情况，包括无库存页面不显示的情况
		// context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=87980726");
		 //各种特殊价格情况
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=45200546");
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=96854926");
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=83820706");
		 //问题测试链接
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=90496096");
//		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=102436456");
//		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=106623796");
		 context.setCurrentUrl("http://www.ralphlauren.com/product/index.jsp?productId=106624286");
		try {
			bf.invoke(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invoke(Context context) throws Exception {
		try {
			// String content = super.getInputString(context);
			String content = HttpUtils.get(context.getCurrentUrl(), 30000, 1, null);
			RetBody rebody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content, context.getCurrentUrl());
				// 商品下架处理
				// Elements offline = document.select(CSS_OFFLINE);
				// if(CollectionUtils.isNotEmpty(offline)){
				// if(trimStringIgnoreNull(offline.get(0).text()).contains(OFFLINE_FLAG)){
				// System.err.println("该商品已下架！");
				// logger.info("this goods is offline and url is {}",
				// context.getCurrentUrl());
				// throw new ParseException(CrawlerExceptionCode.OFFLINE, "this
				// goods is offline and url is "+context.getCurrentUrl());
				// }
				// }
				/**
				 * 之所以把title放在最前面解析，是因为title是必需字段，可以用来过滤无效商品，如果解析错误则无需再继续往下解析，
				 * 从而提高程序的性能，并且title的解析相对其他字段更简单一些。
				 */
				String title = "";
				Elements etitle = document.select(CSS_TITLE);
				if (CollectionUtils.isNotEmpty(etitle)) {
					title = etitle.get(0).text().trim();
				} else {
					// 此处仅仅是为了方便在日志中统计特殊商品的个数，对程序功能并无用处
					if (CollectionUtils.isNotEmpty(document.select("div.prodbanner img"))) {
						logger.error("discarded because of special home goods, url {}", context.getCurrentUrl());
					}
					// 此处仅仅是为了方便在日志中统计特殊商品的个数，对程序功能并无用处
					if (CollectionUtils.isNotEmpty(document.select("table#productDescription h1"))) {
						logger.error("discarded because of custom-made goods, url {}", context.getCurrentUrl());
					}
					logger.error("Error while fetching url {} because of no title.", context.getCurrentUrl());
					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,
							"Error while fetching title with url " + context.getCurrentUrl() + " from ralphlauren.");
				}

				Set<String> switch_imgs = new HashSet<>();
				Map<String, List<String>> colorImgs = this.getColorImgs(content);
				List<RalphlaurenSku> skuList = this.getSkuList(content, context, document, switch_imgs,
						colorImgs.keySet());

				// 让框架能够下载switch_img
				List<Picture> switchPictures = new ArrayList<>();
				if (CollectionUtils.isNotEmpty(switch_imgs)) {
					for (String img : switch_imgs) {
						Picture p = new Picture(img, "");
						switchPictures.add(p);
					}
				}
				List<Image> switchImgs = getPicsByStyleId(switchPictures);
				// context.getUrl().getImages().put(UUID.randomUUID().toString(),
				// switchImgs);

				String unit = "";
				String brand = "";
				int inventoryStatus = 0; // spu库存状态
				float original_price = 0f;
				float sale_price = 0f;
				int save = 0;
				String style_cate_name = "COLOR";

				// Elements productids =
				// document.getElementsByAttributeValue("itemprop",
				// "productID");
				// Elements productids = document.select(CSS_PRODUCT_ID);
				// if(CollectionUtils.isNotEmpty(productids))
				// productId = productids.get(0).ownText().trim();
				if (CollectionUtils.isNotEmpty(skuList))
					productId = skuList.get(0).getProductId();
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());

				Elements eBrand = document.select(CSS_BRAND);
				if (CollectionUtils.isNotEmpty(eBrand)) {
					brand = eBrand.get(0).attr("alt").trim();
				}
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
						if (CollectionUtils.isNotEmpty(e.getElementsByAttributeValue("itemprop", "productID")))
							continue;
						featureMap.put("feature-" + count, e.text().trim());
						count++;
						sb.append(e.text().trim()).append(". ");
					}
				}
				descMap.put("en", sb.toString());

				// 设置spu图片
				List<Picture> pl = new ArrayList<>();
				Elements defaultColor = document.select(CSS_COLOR_DEFAULT);
				if (CollectionUtils.isNotEmpty(defaultColor)) {
					String default_colorId = defaultColor.get(0).attr("data-value");
					for (RalphlaurenSku sku : skuList) {
						sku.setDefault_color(default_colorId);
					}
					for (String imgUrl : colorImgs.get(default_colorId)) {
						pl.add(new Picture(imgUrl, ""));
					}
				}
				LImageList image_list = new LImageList(pl);

				// 设置spu价格
				Elements tempSalePrice = document.select(CSS_SALE_PRICE);
				if (CollectionUtils.isNotEmpty(tempSalePrice)) {
					Elements priceElements = tempSalePrice.get(0).getElementsByAttributeValue("itemprop", "price");
					if (CollectionUtils.isEmpty(priceElements))
						priceElements = tempSalePrice.get(0).getElementsByAttributeValue("itemprop", "lowPrice");
					if (CollectionUtils.isNotEmpty(priceElements)) {
						Map<String, Object> map = formatPrice(priceElements.get(0).ownText().trim());
						sale_price = (float) map.get("price");
						unit = map.get("unit").toString();
					}
				}
				Elements tempOriginalPrice = document.select(CSS_OR_PRICE);
				if (CollectionUtils.isNotEmpty(tempOriginalPrice)) {
					Map<String, Object> map = formatPrice(tempOriginalPrice.get(0).text().trim());
					original_price = (float) map.get("price");
				} else {
					original_price = sale_price;
				}

				// 设置spu库存状态
				for (RalphlaurenSku sku : skuList) {
					if ("IN_STOCK".equals(trimStringIgnoreNull(sku.getAvail()))) {
						inventoryStatus = 2;
						break;
					}
				}
				// 设置性别
				Map<String, Object> properties = new HashMap<>();
				// 先从分类里面找
				if (CollectionUtils.isNotEmpty(categories)) {
					gender: for (String cat : categories) {
						for (String male_key : MALE_KEY_WORD) {
							if (male_key.equals(cat.trim())) {
								properties.put("s_gender", "men");
								break gender;
							}
						}
						for (String female_key : FEMALE_KEY_WORD) {
							if (female_key.equals(cat.trim())) {
								properties.put("s_gender", "women");
								break gender;
							}
						}
					}
				}
				// 如果分类里面没找到则从品牌里面找
				if (properties.get("s_gender") == null) {
					if (brand.contains("Boy"))
						properties.put("s_gender", "men");
					else if (brand.contains("Girl"))
						properties.put("s_gender", "women");
				}
				/**
				 * 后来在页面源代码中发现，许多页面可以直接取到性别， 但是一来并非所有的页面都能用这种办法获取(如Home类别中的Gift
				 * For Her这种办法就无法取到) 二来因为程序已经通过预发布环境测试，为了减少程序的改动，避免造成不必要的错误
				 * 故而前面获取性别的方法不变，只在前两种办法中都没有获取到的情况下，才采用该方法
				 */
				if (properties.get("s_gender") == null) {
					String gender = StringUtils.substringBetween(content, "s.pageName='", "';");
					if (StringUtils.isNotBlank(gender)) {
						if (gender.contains("Boy") || gender.contains("Men"))
							properties.put("s_gender", "men");
						else if (gender.contains("Girl") || gender.contains("Women"))
							properties.put("s_gender", "women");
						else
							properties.put("s_gender", "");
					}
				}

				// 设置sku
				List<LStyleList> l_style_list = new ArrayList<>();
				List<LSelectionList> l_selection_list = new ArrayList<>();
				final float original_price_final = original_price;
				if(CollectionUtils.isEmpty(skuList)) {
					logger.error("skuList is empty and url is {}",context.getCurrentUrl());
					throw new CrawlerException(CrawlerExceptionCode.PARSE_ERROR, "skuList is empty");
				}
				for (RalphlaurenSku sku : skuList) {
					List<Selection> slist = new ArrayList<>();
					Selection sec = new Selection(0, sku.getSizeDesc(), "Size");
					slist.add(sec);
					LSelectionList selection = new LSelectionList();
					selection.setStyle_id(sku.getColorDesc());
					selection.setGoods_id(sku.getSkuId());
					Map<String, Object> map = formatPrice(sku.getPrice());
					selection.setPrice_unit((String) map.get("unit"));
					selection.setSale_price((float) map.get("price"));
					if(original_price_final > selection.getSale_price())
						selection.setOrig_price(original_price_final);
					else
						selection.setOrig_price((float) map.get("price"));
					if (original_price != 0 && original_price < selection.getSale_price())
						selection.setOrig_price(selection.getSale_price());
					selection.setStock_number(sku.getQuantityOnHand());
					selection.setStock_status(sku.getStatus());
					selection.setSelections(slist);
					l_selection_list.add(selection);
					
					if (!checkStyleIdExist(sku, l_style_list)) {
						LStyleList style = new LStyleList();
						style.setStyle_id(trimStringIgnoreNull(sku.getColorDesc()));
						if (sku.getColorId().equals(sku.getDefault_color())) {
							style.setDisplay(true);
							original_price = selection.getOrig_price();
							sale_price = selection.getSale_price();
							if (original_price != 0)
								save = Math.round((1 - sale_price / original_price) * 100);
						}
						style.setGood_id(sku.getSkuId());
						style.setStyle_cate_id(0);
						style.setStyle_cate_name(style_cate_name);
						style.setStyle_name(sku.getColorDesc());
						style.setStyle_switch_img(sku.getSwitch_img());
						List<Picture> stylePictures = new ArrayList<>();
						List<String> imgs = colorImgs.get(sku.getColorId());
						if (CollectionUtils.isNotEmpty(imgs)) {
							for (String img : colorImgs.get(sku.getColorId())) {
								Picture p = new Picture(img, "");
								stylePictures.add(p);
							}
						}
						List<Image> styleImgs = getPicsByStyleId(stylePictures);
						// context.getUrl().getImages().put(sku.getSkuId() + "",
						// styleImgs);
						l_style_list.add(style);
					}
				}

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
				rebody.setPrice(new Price(original_price, save, sale_price, unit));
				rebody.setStock(new Stock(inventoryStatus));
				rebody.setSku(new Sku(l_selection_list, l_style_list));
				rebody.setProperties(properties);
			}
			// setOutput(context, rebody.parseTo());
			System.err.println(rebody.parseTo());
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(), e);
		}
	}

	private Map<String, Object> formatPrice(String tempPrice) {
		tempPrice = trimStringIgnoreNull(tempPrice);
		if (tempPrice.contains("-")) {
//			tempPrice = StringUtils.substringBefore(tempPrice, "-").trim();
			tempPrice = null;
		}
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isBlank(tempPrice)) {
			map.put("unit", "");
			map.put("price", 0f);
			return map;
		}
		tempPrice = tempPrice.trim();
		String currency = StringUtils.substring(tempPrice, 0, 1);
		String unit = Currency.codeOf(currency).name();
		tempPrice = StringUtils.substring(tempPrice, 1).replace(",", "");
		map.put("unit", unit);
		try {
			float price = Float.parseFloat(tempPrice);
			price = formatNum(price);
			map.put("price", price);
		} catch (NumberFormatException e) {
			logger.error("Format Price Error.", e);
		}
		return map;
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

	private float formatNum(float num) {
		return ((float) Math.round(num * 100)) / 100; // 四舍五入法保留两位小数
	}

	private Map<String, List<String>> getColorImgs(String content) {
		String str = StringUtils.substringBetween(content, "var isTablet", "</script>");
		String[] imgs = StringUtils.substringsBetween(str, "imageObj = new Object();", "imageObj.vid");
		Map<String, List<String>> colorImgs = new HashMap<>();
		if (imgs == null)
			return colorImgs;
		for (String s : imgs) {
			String colorId = StringUtils.substringBetween(s, "imageObj.cId = \"", "\";");
			if (colorId != null) {
				List<String> list = new ArrayList<>();
				String suffix = "v400.jpg";
				String[] pictures = StringUtils.substringsBetween(s, "v400", "v400.jpg");
				if (pictures == null) {
					pictures = StringUtils.substringsBetween(s, "enh", "dt.jpg");
					String[] pic2 = StringUtils.substringsBetween(s, "main", "v360x480.jpg");
					if (pic2 != null)
						pictures = (String[]) ArrayUtils.addAll(pictures, pic2);
					suffix = "dt.jpg";
				}
				if (pictures == null) {
					pictures = StringUtils.substringsBetween(s, "reg", "v360x480.jpg");
					String[] pic3 = StringUtils.substringsBetween(s, "main", "v360x480.jpg");
					if (pic3 != null)
						pictures = (String[]) ArrayUtils.addAll(pictures, pic3);
					suffix = "v360x480.jpg";
				}
				if (pictures != null) {
					String standardImg = null;
					for (String img : pictures) {
						img = img.replace(":", "").replace("http//", "http://").replace("https//", "https://")
								.replace("=", "").replace("'", "").replace("\"", "").trim().concat(suffix);
						if(img.contains("lifestyle"))
							list.add(0, img);
						else
							list.add(img);
						if(img.contains("standard"))
							standardImg = img;
					}
					if(CollectionUtils.isNotEmpty(list)){
						if(!list.get(0).contains("lifestyle") && StringUtils.isNotBlank(standardImg)){
							list.remove(standardImg);
							list.add(0, standardImg);
						}
					}
				}
				colorImgs.put(colorId.trim(), list);
			}
		}
		return colorImgs;
	}

	private List<RalphlaurenSku> getSkuList(String content, Context context, Document doc, Set<String> switch_imgs,
			Set<String> colorIds) {
		String skus = StringUtils.substringBetween(content, "var itemMap", "</script>");
		String[] sk = StringUtils.substringsBetween(skus, "itemMap", "};");
		List<RalphlaurenSku> skuList = new ArrayList<>();
		if (sk == null)
			return skuList;
		for (String s : sk) {
			String colorId = trimStringIgnoreNull(StringUtils.substringBetween(s, "cId: '", "',"));
			if (colorIds.contains(colorId)) {
				RalphlaurenSku sku = new RalphlaurenSku() {
					{
						setProductId(StringUtils.substringBetween(s, "pid: '", "',"));
						setSkuId(StringUtils.substringBetween(s, "sku: ", ","));
						setColorId(colorId);
						setColorDesc(StringUtils.substringBetween(s, "cDesc: \"", "\","));
						setSizeId(StringUtils.substringBetween(s, "sId: '", "',"));
						setSizeDesc(StringUtils.substringBetween(s, "sDesc: \"", "\","));
						setAvail(StringUtils.substringBetween(s, "avail: '", "',"));
						setPrice(StringUtils.substringBetween(s, "price: '", "',"));
						setReleaseDate(StringUtils.substringBetween(s, "releaseDate: '", "',"));
						setJdaStyle(StringUtils.substringBetween(s, "jdaStyle: '", "'"));
					}
				};
				String inventory = StringUtils.substringBetween(s, "quantityOnHand: ", ",");
				try {
					if (StringUtils.isNotBlank(inventory)) {
						inventory = inventory.replace("'", "");
						sku.setQuantityOnHand(Integer.parseInt(inventory));
						if (sku.getQuantityOnHand() != 0)
							sku.setStatus(2);
					}
				} catch (NumberFormatException e) {
					logger.error("format inventory error from url {}", context.getCurrentUrl());
					e.printStackTrace();
				}

				// 根据colorId，设置switch_img
				Elements eUl = doc.select(CSS_SWITCH_IMG);
				if (CollectionUtils.isNotEmpty(eUl)) {
					Elements eli = eUl.get(0).getElementsByAttributeValue("data-value", sku.getColorId());
					if (CollectionUtils.isNotEmpty(eli)) {
						Elements imgs = eli.get(0).getElementsByTag("img");
						if (CollectionUtils.isNotEmpty(imgs)) {
							String switchImg = trimStringIgnoreNull(imgs.get(0).absUrl("src"));
							sku.setSwitch_img(switchImg);
							if (StringUtils.isNotBlank(switchImg))
								switch_imgs.add(switchImg);
						}
					}
				}
				if (StringUtils.isBlank(sku.getSwitch_img()))
					sku.setSwitch_img("");
				skuList.add(sku);
			}
		}
		return skuList;
	}

	// 避免直接使用trim()方法发生空指针异常
	private String trimStringIgnoreNull(String string) {
		if (string == null)
			return ""; // 这里不返回null的原因是，在本类中组装json时，如果取不到值默认返回空字符串而不是null
		return string.trim();
	}

	private boolean checkStyleIdExist(RalphlaurenSku sku, List<LStyleList> l_style_list) {
		for (LStyleList lsl : l_style_list) {
			if (trimStringIgnoreNull(sku.getColorDesc()).equals(lsl.getStyle_id()))
				return true;
		}
		return false;
	}
}
