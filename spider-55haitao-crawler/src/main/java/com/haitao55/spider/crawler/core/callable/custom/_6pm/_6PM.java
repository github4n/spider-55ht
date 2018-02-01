package com.haitao55.spider.crawler.core.callable.custom._6pm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.misc.*;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * 
 * @ClassName: _6PM
 * @Description: 解析html内容并组装json数据
 * @author songsong.xu
 * @date 2016年9月20日 下午2:38:09
 *
 */
public class _6PM extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.6pm.com";
	private static final String IMAGE_PREFIX = "https://m.media-amazon.com/images/I/";
	private static final String IMAGE_SUFFIX = ".jpg";

	//6pm_v2版本 by denghuan
	private void  _6pm_V2(Context context,String content,RetBody rebody){
		String productJson = base64Decoder(content);
		if(StringUtils.isNotBlank(productJson)){
			JSONObject jsonObject = JSONObject.parseObject(productJson);
			String product = jsonObject.getString("product");
			JSONObject productJsonObject = JSONObject.parseObject(product);
			String detail = productJsonObject.getString("detail");
			JSONObject detailJsonObject = JSONObject.parseObject(detail);
			
			String productId = detailJsonObject.getString("productId");
			String brand = detailJsonObject.getString("brandName");
			String title = detailJsonObject.getString("productName");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			
			JSONArray styleJsonArr = detailJsonObject.getJSONArray("styles");
			for (int i = 0; i < styleJsonArr.size(); i++) {
				JSONObject styleJsonObj = styleJsonArr.getJSONObject(i);
				String colorValue = styleJsonObj.getString("color");
				String salePrice = styleJsonObj.getString("price");
				String origPrice = styleJsonObj.getString("originalPrice");
				JSONArray imagesJsonArray = styleJsonObj.getJSONArray("images");
				if (StringUtils.isBlank(salePrice)) {
					logger.error("Error while crawling 6pm saleprice,url {}", context.getUrl().toString());
				}

				String currency = StringUtils.substring(salePrice, 0, 1);
				String price_unit = Currency.codeOf(currency).name();

				if (StringUtils.isNotBlank(salePrice)) {
					salePrice = salePrice.replaceAll("[$ ]", "");
				}

				if (StringUtils.isBlank(origPrice)) {
					origPrice = salePrice;
				}
				origPrice = origPrice.replaceAll("[$ ]", "");

				String onSale = styleJsonObj.getString("onSale");
				String styleId = styleJsonObj.getString("styleId");

				String skuStyleId = StringUtils.EMPTY;

				JSONArray stocksJsonArray = styleJsonObj.getJSONArray("stocks");
				if (stocksJsonArray != null && stocksJsonArray.size() > 0) {
					for (int j = 0; j < stocksJsonArray.size(); j++) {
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject stocksJsonObj = stocksJsonArray.getJSONObject(j);
						String sizeValue = stocksJsonObj.getString("size");
						String sizeId = stocksJsonObj.getString("sizeId");
						String widthValue = stocksJsonObj.getString("width");
						String stockId = stocksJsonObj.getString("stockId");
						String onHand = stocksJsonObj.getString("onHand");
						String skuId = stocksJsonObj.getString("upc");
						skuStyleId = skuId;
						List<Selection> selections = new ArrayList<>();
						if (StringUtils.isNotBlank(sizeValue)) {
							Selection selection = new Selection();
							selection.setSelect_id(Long.parseLong(sizeId));
							selection.setSelect_name("size");
							selection.setSelect_value(sizeValue);
							selections.add(selection);
						}
						if (StringUtils.isNotBlank(widthValue)) {
							Selection selection = new Selection();
							selection.setSelect_id(Long.parseLong(stockId));
							selection.setSelect_name("width");
							selection.setSelect_value(widthValue);
							selections.add(selection);
						}

						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(price_unit);
						lSelectionList.setStock_number(Integer.valueOf(onHand));
						lSelectionList.setStock_status(2);
						lSelectionList.setStyle_id(colorValue);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
				} else {
					LSelectionList lSelectionList = new LSelectionList();
					lSelectionList.setGoods_id(styleId);
					lSelectionList.setOrig_price(Float.parseFloat(origPrice));
					lSelectionList.setSale_price(Float.parseFloat(salePrice));
					lSelectionList.setPrice_unit(price_unit);
					int stock_status = 0;
					if ("true".equals(onSale)) {
						stock_status = 1;
					}
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id(colorValue);
					List<Selection> selections = new ArrayList<>();
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
				}

				LStyleList lStyleList = new LStyleList();
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_name(colorValue);
				lStyleList.setStyle_id(colorValue);
				lStyleList.setStyle_switch_img("");
				if (StringUtils.isBlank(skuStyleId)) {
					skuStyleId = styleId;
				}
				lStyleList.setGood_id(skuStyleId);
				if(display){
					lStyleList.setDisplay(display);
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), price_unit));
					display = false;
				}

				List<Image> images = new ArrayList<>();
				for (int m = 0; m < imagesJsonArray.size(); m++) {
					JSONObject imageJsonObj = imagesJsonArray.getJSONObject(m);
					String imageId = imageJsonObj.getString("imageId");
					if (StringUtils.isNotBlank(imageId)) {
						String image = IMAGE_PREFIX + imageId + IMAGE_SUFFIX;
						images.add(new Image(image));
					}
				}
				context.getUrl().getImages().put(skuStyleId, images);// picture

				l_style_list.add(lStyleList);
			}
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			int spuStock = 0;
			if(l_selection_list != null 
					&& l_selection_list.size() > 0){
				for(LSelectionList ll : l_selection_list){
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2){
						spuStock = 2;
					}
				}
			}
			
			rebody.setStock(new Stock(spuStock));
			
			Document doc = Jsoup.parse(content);
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements es = doc.select(".SRGgm a");
			for(Element  e : es){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cate = cate.replace("« ", "");
					cats.add(cate);
					breads.add(cate);
				}
			}
			if(CollectionUtils.isEmpty(cats)){
				cats.add(title);
				breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select("._1Srfn").text();
			featureMap.put("feature-1", description);
			
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	
	@SuppressWarnings("restriction")
	private String base64Decoder(String content){
		String code = StringUtils.substringBetween(content, "window.__INITIAL_STATE__ =", "';window.ssc = 1;");
		BASE64Decoder decoder = new BASE64Decoder();  
        try {  
        	code = code.replace("'", "");
        	code = code.replaceAll("\\s*", "");
        	byte[] b  = decoder.decodeBuffer(code);  
        	return new String(b, "utf-8");
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return StringUtils.EMPTY;
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Pattern p = Pattern.compile("Found 0 Results...");
		Matcher m = p.matcher(content);
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"6pm.com itemUrl:"+context.getUrl().toString()+" not found..");
		}
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			String code = StringUtils.substringBetween(content, "_p || {}", "</script>");
			String styleId = StringUtils.substringBetween(code, "var styleId = ", ";");
			String productId = StringUtils.substringBetween(code, "var productId = ", ";");
			String title = StringUtils.substringBetween(code, "var productName = \"", "\";");
			
			/**
			 * 6pm改版 以下判断走新版本逻辑
			 */
			if(StringUtils.isBlank(productId) && StringUtils.isBlank(title)){
				_6pm_V2(context,content,rebody);
				return;
			}
			
			// String brandId = StringUtils.substringBetween(code,
			// "var brandId = ", ";");
			String brandName = StringUtils.substringBetween(code, "var brandName = \"", "\";");
			String gender = StringUtils.substringBetween(code, "var productGender = \"", "\";");
			String categores = StringUtils.substringBetween(code, "var zetaCategories = ", ";");
			String stockJSON = StringUtils.substringBetween(code, "var stockJSON = ", ";");
			String dimensions = StringUtils.substringBetween(code, "var dimensions = ", ";");
			// String dimToUnitToValJSON =
			// StringUtils.substringBetween(code,
			// "var dimToUnitToValJSON = ", ";");
			String dimensionIdToNameJson = StringUtils.substringBetween(code, "var dimensionIdToNameJson = ", ";");
			String valueIdToNameJSON = StringUtils.substringBetween(code, "var valueIdToNameJSON = ", ";");
			String colorNames = StringUtils.substringBetween(code, "var colorNames = ", ";");
			String colorPrices = StringUtils.substringBetween(code, "var colorPrices = ", ";");
			String styleIds = StringUtils.substringBetween(code, "var styleIds = ", ";");
			String colorIds = StringUtils.substringBetween(code, "var colorIds = ", ";");

			// pics
			// Set<Image> pics = getPicsByStyleId(code, styleId);
			JSONArray dimension = new JSONArray();
			if (StringUtils.isNotBlank(dimensions)) {
				dimension = JSONArray.parseArray(StringUtils.trim(dimensions));
			}
			// sku key
			JSONObject nameJson = new JSONObject();
			if (StringUtils.isNotBlank(dimensionIdToNameJson)) {
				nameJson = JSONObject.parseObject(StringUtils.trim(dimensionIdToNameJson));
			}
			// sku value
			JSONObject valueJson = new JSONObject();
			if (StringUtils.isNotBlank(valueIdToNameJSON)) {
				valueJson = JSONObject.parseObject(StringUtils.trim(valueIdToNameJSON));
			}
			// sku color id and name
			JSONObject colorIdNames = new JSONObject();
			if (StringUtils.isNotBlank(colorNames)) {
				colorIdNames = JSONObject.parseObject(StringUtils.trim(colorNames));
			}
			// sku color id and prices
			JSONObject colorIdPrices = new JSONObject();
			if (StringUtils.isNotBlank(colorPrices)) {
				colorIdPrices = JSONObject.parseObject(StringUtils.trim(colorPrices));
			}
			// color id and sku id
			JSONObject colorIdSkuId = new JSONObject();
			if (StringUtils.isNotBlank(styleIds)) {
				colorIdSkuId = JSONObject.parseObject(StringUtils.trim(styleIds));
			}
			// sku id and color id
			JSONObject skuIdColorId = new JSONObject();
			if (StringUtils.isNotBlank(colorIds)) {
				skuIdColorId = JSONObject.parseObject(StringUtils.trim(colorIds));
			}
			// selection list
			// JSONObject selectionObj = new JSONObject();
			// JSONArray selection_list = new JSONArray();
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			Set<String> colors = new HashSet<String>();// all colors
			Map<String, Integer> colorIdStock = new HashMap<String, Integer>();
			if (StringUtils.isNotBlank(stockJSON)) {
				JSONArray stockArr = JSONArray.parseArray(StringUtils.trim(stockJSON));
				if (stockArr != null) {
					for (int i = 0; i < stockArr.size(); i++) {
						// JSONObject sku = new JSONObject();
						LSelectionList lselectlist = new LSelectionList();
						JSONObject obj = stockArr.getJSONObject(i);
						String colorId = obj.getString("color");
						colors.add(colorId);
						int stock_number = obj.getIntValue("onHand");// stock
						Integer st = colorIdStock.get(colorId);
						if (st == null) {
							colorIdStock.put(colorId, stock_number);
						} else {
							colorIdStock.put(colorId, stock_number + st);
						}
						int stock_status = 0;
						if (stock_number > 0) {
							stock_status = 2;
						}
						int skuId = colorIdSkuId.getIntValue(colorId);// skuid
						JSONObject price = colorIdPrices.getJSONObject(colorId);
						float orig_price = 0;
						if(price != null && price.containsKey("wasInt")){
							orig_price = price.getFloatValue("wasInt");
						}
						float sale_price = 0;
						if(price != null && price.containsKey("nowInt")){
							sale_price = price.getFloatValue("nowInt");
							if(sale_price == 0){
								logger.error("Error while crawling 6pm saleprice,url {}",context.getUrl().toString());
								stock_status = 0;
							}
						}
						String price_unit = StringUtils.EMPTY;
						if(price != null && price.containsKey("now")){
							String currency = StringUtils.substring(price.getString("now"), 0, 1);
							price_unit = Currency.codeOf(currency).name();
						}
						if(orig_price == 0){
							orig_price = sale_price;
						}
						String colorValue = colorIdNames.getString(colorId);
						List<Selection> selections = new ArrayList<Selection>();
						for (int j = 0; j < dimension.size(); j++) {
							Selection select = new Selection();
							String key = dimension.getString(j);
							String valueId = obj.getString(key);
							String propName = nameJson.getString(key);
							String propValue = StringUtils.EMPTY;
							JSONObject val = valueJson.getJSONObject(valueId);
							if (val != null) {
								propValue = val.getString("value");
							}
							select.setSelect_id(Long.valueOf(valueId));
							select.setSelect_name(propName);
							select.setSelect_value(propValue);
							selections.add(select);
						}
						lselectlist.setSelections(selections);
						lselectlist.setGoods_id(skuId+"");
						lselectlist.setStyle_id(colorValue);
						lselectlist.setOrig_price(orig_price);
						lselectlist.setSale_price(sale_price);
						lselectlist.setPrice_unit(price_unit);
						lselectlist.setStock_number(stock_number);
						lselectlist.setStock_status(stock_status);
						l_selection_list.add(lselectlist);
					}
				}
			}
			sku.setL_selection_list(l_selection_list);
			// style list
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			for (String colorid : colors) {
				String colorname = colorIdNames.getString(colorid);
				int skuId = colorIdSkuId.getIntValue(colorid);
				List<Image> picsPerSku = getPicsByStyleId(content, skuId + "");
				context.getUrl().getImages().put(skuId + "", picsPerSku);// picture
																			// download
				LStyleList style = new LStyleList();
				style.setStyle_switch_img("");
				style.setStyle_id(colorname);
				style.setStyle_cate_id(0l);
				style.setStyle_cate_name("color");
				style.setStyle_name(colorname);
				// style.setStyle_images(picsPerSku);
				style.setGood_id(skuId+"");
				if (StringUtils.isNotBlank(styleId) && styleId.equals(skuId + "")) {
					style.setDisplay(true);
				}
				l_style_list.add(style);
			}
			sku.setL_style_list(l_style_list);

			// full doc info
			String docid = SpiderStringUtil.md5Encode(domain + productId);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));

			// price
			if(skuIdColorId != null && skuIdColorId.containsKey(styleId)){
				String colorId = skuIdColorId.getString(styleId);
				if (StringUtils.isNotBlank(colorId)) {
					JSONObject price = colorIdPrices.getJSONObject(colorId);
					float orig = 0;
					float sale  = 0;
					if(price != null){
						if(price.containsKey("wasInt")){
							orig = price.getFloatValue("wasInt");
						}
						sale = price.getFloatValue("nowInt");
					}
					if(orig < sale ){
						orig = sale;
					}
					String currency = StringUtils.substring(price.getString("now"), 0, 1);
					String unit = Currency.codeOf(currency).name();
					int save = Math.round((1 - sale / orig) * 100);// discount
					rebody.setPrice(new Price(orig, save, sale, unit));
					int stock = colorIdStock.get(colorId);
					int stockStatus = 0;
					if (stock > 0) {
						stockStatus = 2;
					}
					if(sale == 0){
						stockStatus = 0;
					}
					// stock
					rebody.setStock(new Stock(stockStatus));
				}
			}
			// images l_image_list
			// rebody.setImage(new LImageList(pics));
			// brand
			rebody.setBrand(new Brand(brandName, ""));
			// Category
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String[] arrCat = StringUtils.substringsBetween(categores, "\": \"", "\"");
			if (arrCat != null && arrCat.length > 0) {
				for (String c : arrCat) {
					String cat = Native2AsciiUtils.ascii2Native(c);
					if(StringUtils.isNotBlank(cat)){
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
			rebody.setCategory(cats);
			// BreadCrumb
			breads.add(brandName);
			rebody.setBreadCrumb(breads);
			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Document document = Jsoup.parse(content);
			Elements es = document.select("div.description > ul > li");
			StringBuilder sb = new StringBuilder();
			if (es != null && es.size() > 0) {
				int count = 1;
				for (Element e : es) {
					featureMap.put("feature-" + count, e.text());
					count++;
					sb.append(e.text());
				}
			}
			rebody.setFeatureList(featureMap);
			descMap.put("en", sb.toString());
			rebody.setDescription(descMap);

			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			es = document.select("div.description > ul > li.measurements > ul > li");
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
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	
	}

	public JSONObject put(String key, Object value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public JSONObject put(String key, String value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public JSONObject put(String key, int value) {
		JSONObject param = new JSONObject();
		param.put(key, value);
		return param;
	}

	public _6PM put(JSONObject obj, String key, String value) {
		obj.put(key, value);
		return this;
	}

	public _6PM put(JSONObject obj, String key, long value) {
		obj.put(key, value);
		return this;
	}

	public _6PM put(JSONObject obj, String key, float value) {
		obj.put(key, value);
		return this;
	}

	private List<Image> getPicsByStyleId(String content, String skuId) {
		List<Image> pics = new ArrayList<Image>();
		List<String> pic_key = new ArrayList<String>();
		pic_key.add("p");
		pic_key.add("1");
		pic_key.add("2");
		pic_key.add("3");
		pic_key.add("4");
		pic_key.add("5");
		pic_key.add("6");
		for (String key : pic_key) {
			String image2xp = StringUtils.substringBetween(content, "pImgs[" + skuId + "]['2x']['" + key + "'] = '",
					"';");
			if (StringUtils.isNotBlank(image2xp)) {
				Image image = new Image(image2xp);
				pics.add(image);
			}
		}
		return pics;
	}

	public static void main(String[] args) throws Exception {
		String url = "https://www.6pm.com/p/nike-flat-front-short-black-black-black/product/8916212/color/24150";
		Context context = new Context();
		_6PM pm = new _6PM();
		context.setCurrentUrl(url);
		pm.invoke(context);
		String content = Crawler.create().timeOut(10000).retry(3).url(context.getCurrentUrl()).resultAsString();
		String str = StringUtils.substringBetween(content, "window.__INITIAL_STATE__ =", "';window.ssc = 1;");
    	String result = null;  
        BASE64Decoder decoder = new BASE64Decoder();  
        try {  
        	str = str.replace("'", "");
        	str = str.replaceAll("\\s*", "");
        	byte[] b  = decoder.decodeBuffer(str);  
            result = new String(b, "utf-8");  
            System.out.println(result);
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
		
		
//    	String result = null;  
//        BASE64Decoder decoder = new BASE64Decoder();  
//        try {  
//        	byte[] b  = decoder.decodeBuffer("eyJhY2NvdW50Ijp7ImFjY291bnRJbmZvIjp7fX0sImFzayI6eyJsb2FkaW5nIjp");  
//            result = new String(b, "utf-8");  
//            System.out.println(result);
//        } catch (Exception e) {  
//            e.printStackTrace();  
//        } 
//		String html = Crawler.create().timeOut(10000).retry(3).url(url
//		// "http://www.6pm.com/ivanka-trump-kayden-4-black-patent"
//				).resultAsString();
//		Pattern p = Pattern.compile("Your Search For(.*?)Found 0 Results");
//		Matcher m = p.matcher(html);
//		if(m.find()){
//			String itemId = m.group(1);
//			System.out.println(itemId);
//		}
	}
}