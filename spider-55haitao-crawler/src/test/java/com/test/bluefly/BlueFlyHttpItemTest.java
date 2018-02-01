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
package com.test.bluefly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * @ClassName: BlueFlyHttpItemTest
 * @Description: TODO
 * @author: zhoushuo
 * @date: 2016年10月26日 下午2:47:51
 */
public class BlueFlyHttpItemTest extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.bluefly.com";

	private static final String CSS_TITLE = "div.mz-breadcrumbs span.mz-breadcrumb-current";
	private static final String CSS_BRAND = ".mz-productbrand>a";
	private static final String CSS_BREADS = "div.mz-breadcrumbs a.mz-breadcrumb-link";
	private static final String CSS_DESCRIPTION = "div.mz-productdetail-description";
	private static final String CSS_DETAIL = "ul.mz-productdetail-props>li";
	private static final String CSS_PICTURE = "div.mz-productimages-thumbs a.mz-productimages-thumb";
	private static final String CSS_OR_PRICE = "div.mz-pricestack div.is-crossedout";
	private static final String CSS_SALE_PRICE = "div.mz-pricestack div.is-saleprice";
	private static final String CSS_INVENTORY = ".mz-productoptions-sizebox";
	private static final String CSS_COLOR_TYPE = "ul.product-color-list>li>a";
	private static final String CSS_COLOR = "div.colorList span.mz-productoptions-optionvalue";
	private static final String CSS_SIZE = "div.mz-productoptions-valuecontainer span.mz-productoptions-sizebox";
	private static final String CSS_ONLY_SKU_INVENTORY = ".mz-productselection-inventoryalert";

	private static final String IMG_SRC = "data-zoom-image";
	private static final String ATTR_INVENTORY = "data-inventory";
	private static final int TIME_OUT = 30000; //切换颜色重发请求的超时时间(毫秒)
	private static final int RETRY = 2;//切换颜色重发请求的重试次数
	
	//男性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women", "Women's Beauty & Fragrance", "Girls",
			"Baby Girl Clothing" };
	//女性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men", "Men's Grooming and Cologne", "Boys", "Baby Boy Clothing" };

	public static void main(String[] args) {
		BlueFlyHttpItemTest bf = new BlueFlyHttpItemTest();
		Context context = new Context();
		//No Color ,无size的情况
		context.setCurrentUrl("http://www.bluefly.com/iwc-iwc-mens-pilots-watches-watch/p/401565101");
		context.setCurrentUrl("http://www.bluefly.com/labelthread-tassel-poncho/p/387329801");
		context.setCurrentUrl("http://www.bluefly.com/nars-nars-sheer-lipstick-pago-pago/p/394846701");
		context.setCurrentUrl("http://www.bluefly.com/gant-gant-shirtdress/p/404741201");
		//正常情况，颜色，size都全
		context.setCurrentUrl("http://www.bluefly.com/prada-prada-saffiano-leather-logo-belt/p/397900001");
		context.setCurrentUrl("http://www.bluefly.com/sperry-sperry-leeward-2eye-chambray-boat-shoe/p/411941802");
		context.setCurrentUrl("http://www.bluefly.com/superior-300-thread-count-100-egyptian-cotton-striped-bedskirt/p/395510304");
		context.setCurrentUrl("http://www.bluefly.com/brookstone-brookstone-e1-colorwave-earbuds/p/403603501");
		//无size的情况
		context.setCurrentUrl("http://www.bluefly.com/jane-iredale-cosmetics-jane-iredale-pressed-powder-refillable-gold-compact/p/376302501");
		context.setCurrentUrl("http://www.bluefly.com/brandt-hoffman-brandt-hoffman-forsyth-mens-watch/p/380867504");
		context.setCurrentUrl("http://www.bluefly.com/after-mrkt-after-mrkt-striped-mini-skirt/p/402182401");
		context.setCurrentUrl("http://www.bluefly.com/labelthread-cozy-mix-cardigan/p/415834901");
		context.setCurrentUrl("http://www.bluefly.com/la-perla-studio-la-perla-new-project-bra/p/423325601");
		context.setCurrentUrl("http://www.bluefly.com/stuart-weitzman-stuart-weitzman-byway-open-toe-suede-slides-sandal/p/406673701");
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
				
				String unit = "";
				String title = "";
				String brand = "";
				int inventoryStatus = 0;
				float original_price = 0f;
				float sale_price = 0f;
				int save = 0;
				String style_cate_name = "COLOR";
				int one_sku_stock_number = 0;

				String productId = getProductId(context.getCurrentUrl());
				String docid = SpiderStringUtil.md5Encode(domain + productId);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());

				Elements etitle = document.select(CSS_TITLE);
				if (CollectionUtils.isNotEmpty(etitle)) {
					title = etitle.get(0).text().trim();
				}
				Elements eBrand = document.select(CSS_BRAND);
				if (CollectionUtils.isNotEmpty(eBrand)) {
					brand = eBrand.get(0).text().trim();
				}
				List<String> breads = new ArrayList<String>();
				List<String> categories = new ArrayList<String>();
				Elements ebread = document.select(CSS_BREADS);
				for (Element e : ebread) {
					if ("home".equals(e.text().trim().toLowerCase())) {
						breads.add(e.text());
						continue;
					}
					breads.add(e.text());
					categories.add(e.text());
				}
				breads.add(title);

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

				List<Picture> pl = new ArrayList<>();
				Elements ePictures = document.select(CSS_PICTURE);
				for (Element e : ePictures) {
					pl.add(new Picture(e.absUrl(IMG_SRC), ""));
				}
				LImageList image_list = new LImageList(pl);

				// 设置spu价格
				Elements tempSalePrice = document.select(CSS_SALE_PRICE);
				if(CollectionUtils.isEmpty(tempSalePrice))
					tempSalePrice = document.select("div.mz-price");
				if(CollectionUtils.isNotEmpty(tempSalePrice)){
					Element eSale = tempSalePrice.get(0);
					if(tempSalePrice.size() > 1)
						eSale = tempSalePrice.get(1);
					Map<String, Object> map = formatPrice(eSale.ownText().trim());
					sale_price = (float) map.get("price");
					unit = map.get("unit").toString();
				}
				Elements tempOriginalPrice = document.select(CSS_OR_PRICE);
				if(CollectionUtils.isNotEmpty(tempOriginalPrice)){
					Map<String, Object> map = formatPrice(tempOriginalPrice.get(0).ownText().trim());
					original_price = (Float) map.get("price");
				} else{
					original_price = sale_price;
				}
				if(original_price != 0)
					save = Math.round((1 - sale_price / original_price) * 100);
				
				//设置spu库存状态
				Elements eInventoryStatus = document.select(CSS_INVENTORY);
				if(CollectionUtils.isNotEmpty(eInventoryStatus)){
					for(Element e : eInventoryStatus){
						if(StringUtils.isNotBlank(e.attr(ATTR_INVENTORY))) {
							//当前SKU库存为0则进入下一次循环，如果所有的SKU都为0，那么就代表没有库存
							if("0".equals(e.attr(e.attr(ATTR_INVENTORY))))
								continue;
							//只要有一个SKU库存不为0则代表有库存，设置库存状态为2，并结束循环
							inventoryStatus = 2;
							break;
						}
					}
				} else {
					//没有Size的情况
					String inventory = null;
					eInventoryStatus = document.select(CSS_ONLY_SKU_INVENTORY);
					if(CollectionUtils.isNotEmpty(eInventoryStatus))
					    inventory = this.getNumberFromString(eInventoryStatus.get(0).ownText().trim());
					else 
						inventory = this.getNumberFromString(StringUtils.substringBetween(content, "onlineStockAvailable", ","));
					if(StringUtils.isNotBlank(inventory)){
				    	one_sku_stock_number = Integer.parseInt(inventory);
				    	if(one_sku_stock_number > 0)
				    		inventoryStatus = 2;
				    }
				}

				// 设置sku
				List<BlueFlySku> colorList = new ArrayList<>();
				Elements eColor = document.select(CSS_COLOR_TYPE);
				if(CollectionUtils.isNotEmpty(eColor)){
					for(Element e : eColor) {
						combineColorList(colorList, null, null, e, null);
					}
				} else {
					combineColorList(colorList, context.getCurrentUrl(), document, null, setHeader(content));
				}
				List<LStyleList> l_style_list = new ArrayList<>();
				List<LSelectionList> l_selection_list = new ArrayList<>();
				if(CollectionUtils.isNotEmpty(colorList)){
					for(BlueFlySku bfs : colorList){
						//设置style_list
						LStyleList style = new LStyleList();
						style.setStyle_switch_img(bfs.getSwitchStyleImg());
						style.setStyle_id(bfs.getColor());
						style.setGood_id(bfs.getGoodId());
						if(bfs.getGoodId().contains(productId))
							style.setDisplay(true);
						else 
							style.setDisplay(false);
						style.setStyle_cate_id(0);
						style.setStyle_cate_name(style_cate_name);
						style.setStyle_name(bfs.getColor());
						List<Picture> skuPics = new ArrayList<>();
						for(String img : bfs.getImgUrls()){
							Picture p = new Picture(img, "");
							skuPics.add(p);
						}
						List<Image> picsSku = getPicsByStyleId(skuPics);
//						context.getUrl().getImages().put(productId + "", picsSku);
						l_style_list.add(style);
						
						//设置selection_list
						if(CollectionUtils.isNotEmpty(bfs.getSizeList())) {
							for(BlueFlySize size : bfs.getSizeList()){
								List<Selection> slist = new ArrayList<>();
								Selection sec = new Selection(0, size.getName(), "SIZE");//
								slist.add(sec);
								
								LSelectionList selection = new LSelectionList();
								selection.setGoods_id(size.getGoodId());
								selection.setOrig_price(size.getOriginal_price());
								selection.setSale_price(size.getSale_price());
								selection.setPrice_unit(unit);
								selection.setStyle_id(bfs.getColor());
								selection.setSelections(slist);
								selection.setStock_number(size.getInventory());
								selection.setStock_status(2);
								l_selection_list.add(selection);
							}
						} else {//没有可选择的尺码的情况
							LSelectionList selection = new LSelectionList();
							List<Selection> slist = new ArrayList<>();
							selection.setGoods_id(bfs.getGoodId());
							selection.setSelections(slist);
							selection.setOrig_price(original_price);
							selection.setSale_price(sale_price);
							selection.setPrice_unit(unit);
							selection.setStyle_id(bfs.getColor());
							if(one_sku_stock_number != 0){
								selection.setStock_number(one_sku_stock_number);
								selection.setStock_status(2);
							} else {
								selection.setStock_number(0);
								selection.setStock_status(0);
							}
							l_selection_list.add(selection);
						}
					}
				}
				
				//设置性别
				Map<String, Object> properties = new HashMap<>();
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
				if (properties.get("s_gender") == null) {
					properties.put("s_gender", "");
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
		if(tempPrice.contains("-")){
			tempPrice = StringUtils.substringBefore(tempPrice, "-").trim();
		}
		Map<String, Object> map = new HashMap<>();
		if(StringUtils.isBlank(tempPrice))
			return map;
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
	
	private String getProductId(String currentUrl){
		if (currentUrl.endsWith("/")) {
			currentUrl = currentUrl.substring(0, currentUrl.length() - 1);
		}
		return currentUrl.substring(currentUrl.lastIndexOf("/") + 1);
	}
	
	private void combineColorList(List<BlueFlySku> colorList,String currentUrl, Document document, Element e, Map<String, String> header) throws UnirestException{
		BlueFlySku sku = new BlueFlySku();
		List<BlueFlySize> blist = new ArrayList<>();
		String url = "";
		Document doc = null;
		if(document != null) {
			url = currentUrl;
			doc = document;
		} else {
			url = e.absUrl("href");
			String colorContent = HttpUtils.get(url, 30000, 1, null);
			if(StringUtils.isNotBlank(colorContent)) {
				header = setHeader(colorContent);
				doc = Jsoup.parse(colorContent, url);
			} else {
				System.out.println("请求出错");
				return;
			}
		}
		
		Elements colors = doc.select(CSS_COLOR);
		String style_id = "";
		if(CollectionUtils.isNotEmpty(colors)){
			style_id = colors.get(0).ownText().trim();
			sku.setColor(style_id);
			sku.setGoodId(getProductId(url));
		}
		List<String> imgUrls = new ArrayList<>();
		Elements imgs = doc.select(CSS_PICTURE);
		for (Element eimg : imgs) {
			imgUrls.add(eimg.absUrl(IMG_SRC));
		}
		sku.setImgUrls(imgUrls);
		Elements eswitch = null;
		if(e != null) {
			eswitch = e.getElementsByTag("div");
			if(CollectionUtils.isEmpty(eswitch))
				eswitch = e.getElementsByTag("img");
		}
		else
			eswitch = doc.select("ul.product-color-list>li>a div");
		if(CollectionUtils.isNotEmpty(eswitch)){
			String switchImg = eswitch.get(0).attr("style");
			if(StringUtils.isNotBlank(switchImg))
				switchImg = "http:"+StringUtils.substringBetween(switchImg, "(", ")").replace("'", "");
			else
				switchImg = eswitch.get(0).absUrl("src");
			sku.setSwitchStyleImg(switchImg);
		} else {
			sku.setSwitchStyleImg("");
		}
		
		Elements eSize = doc.select(CSS_SIZE);
		if(CollectionUtils.isNotEmpty(eSize)){
		    int count = 0;
			for(Element sizeBox : eSize){
				BlueFlySize bs = new BlueFlySize();
				String inven = sizeBox.attr(ATTR_INVENTORY).trim();
				String id = sizeBox.attr("data-value").trim();
				String type = sizeBox.attr("data-mz-product-option").trim();
				String name = sizeBox.ownText().trim();
				
				//调用接口获取每个sku对应的价格
				JsonObject obj = new JsonObject();
				JsonArray arr  = new JsonArray();
				JsonObject internal = new JsonObject();
				internal.addProperty("attributeFQN", type);
				internal.addProperty("value", Integer.valueOf(id));
				arr.add(internal);
				obj.add("options", arr);
				String requestUrl = "http://www.bluefly.com/api/commerce/catalog/storefront/products/"+getProductId(url)+"/configure?includeOptionDetails=true&quantity=1";
				String result = "";
				try {
//					result = Crawler.create().url(requestUrl).method("POST").timeOut(TIME_OUT).retry(RETRY)
//								.header(header).payload(obj.toString()).resultAsString();
					result = Unirest.post(requestUrl).headers(header).body(obj.toString()).asString().getBody();
					JSONObject  json = JSON.parseObject(result);
					JSONObject price = (JSONObject) json.get("price");
					String orig = "";
					String sale = "";
					Object orig_obj = price.get("msrp");
					Object sale_obj = price.get("salePrice");
					if(sale_obj == null)
						sale_obj = price.get("price");
					if(orig_obj != null)
						orig = orig_obj.toString();
					if(sale_obj != null)
						sale = sale_obj.toString();
					bs.setOriginal_price(formatNum(orig));
					bs.setSale_price(formatNum(sale));
				}catch(Exception e1){
					
				}
				
				bs.setId(id);
				bs.setGoodId(this.getProductId(url)+id);
				bs.setName(name);
				bs.setInventory(Integer.parseInt(inven));
				blist.add(bs);
				if(count==0) {
					sku.setGoodId(bs.getGoodId());
					count++;
				}
			}
		}
		sku.setSizeList(blist);
		colorList.add(sku);
	}
	
	//组装请求头信息，为获取sku调用接口做准备，若不设置这几个头信息，请求会报401未授权错误
	private Map<String, String> setHeader(String content){
		Map<String, String> header = new HashMap<>();
		String appClaims = StringUtils.substringBetween(content, "x-vol-app-claims", ",")
				.replace(":", "").replace("}", "").replace("\"", "").trim();
		String userClaims = StringUtils.substringBetween(content, "x-vol-user-claims", ",")
				.replace(":", "").replace("}", "").replace("\"", "").trim();
		header.put("Content-type", "application/json");
		header.put("x-vol-app-claims", appClaims);
		header.put("x-vol-user-claims", userClaims);
		return header;
	}
	
	private float formatNum(String num){
		float number = 0f;
		if(StringUtils.isNotBlank(num)){
			num = num.trim();
			try {
				number = Float.parseFloat(num);
			} catch(NumberFormatException e) {
				System.err.println(num+" Can not be cast to float.");
				e.printStackTrace();
			}
		}
		return ((float)Math.round(number*100))/100; //四舍五入法保留两位小数
	}
	
	private float formatNum(float num){
		return ((float)Math.round(num*100))/100; //四舍五入法保留两位小数
	}
	
	private String getNumberFromString(String str){
		if(StringUtils.isBlank(str))
			return str;
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) 
			return matcher.group();
		else 
			return null;
	}
}
