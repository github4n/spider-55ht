package com.haitao55.spider.crawler.core.callable.custom.yslbeautyus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonObject;
import com.haitao55.spider.common.gson.JsonUtils;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: Yslbeautyus
  * @Description: detail page parser
  * @author songsong.xu
  * @date 2016年11月28日 下午6:13:06
  *
 */
public class Yslbeautyus20170525 extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.yslbeautyus.com";
	private final String SKU_URL_PREFIX = "http://www.yslbeautyus.com/on/demandware.store/Sites-ysl-us-Site/en_US/Product-Variation?quantity=1&action=variationChanged&format=ajax";
	//&pid=823YSL&dwvar_823YSL_color=Rosy+Contouring
	
	public static final int nThreads = 1;
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		//String content = Crawler.create().timeOut(60000).retry(3)/*.proxy(true).proxyAddress("104.196.30.199").proxyPort(3128)*/.url(context.getUrl().toString()).resultAsString();
		String url = context.getUrl().getValue();
		RetBody rebody = new RetBody();
		String itemId = StringUtils.EMPTY;
		//<meta itemprop="productId" content="9081YSL">
		Pattern p = Pattern.compile("<meta[^>]*itemprop=\"productId\"[^>]*content=\"(.*?)\"[^>]*>");
		Matcher m = p.matcher(content);
		if(m.find()){
			itemId = m.group(1);
		} else {
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"yslbeautyus.com itemUrl: "+context.getUrl().toString()+" parse error");
		}
		String defaultColorValue = StringUtils.EMPTY;
		String defaultSizeValue = StringUtils.EMPTY;
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("div#product_content > div.product-variations");
		if(es != null && es.size() > 0 ){
			String data = JsoupUtils.attr(es, "data-current");
			Type type = new TypeToken<JsonObject>() {private static final long serialVersionUID = 1L;}.getType();
			JsonObject defObj = JsonUtils.json2bean(data, type);
			if(defObj != null && !defObj.isJsonNull() && (defObj.has("color") || defObj.has("size"))){
				JsonObject color = defObj.getAsJsonObject("color");
				JsonObject size = defObj.getAsJsonObject("size");
				if(color != null  && !color.isJsonNull()){
					defaultColorValue = color.getAsJsonPrimitive("value").getAsString();
					//color.getAsJsonPrimitive("displayValue").getAsString();
				}
				if(size != null && !size.isJsonNull()){
					defaultSizeValue = size.getAsJsonPrimitive("value").getAsString();
					//size.getAsJsonPrimitive("displayValue").getAsString();
				}
			}
		} else {
			logger.error("Error while fetching product-variations from yslbeautyus.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"yslbeautyus.com category url :"+context.getUrl().toString()+" product-variations is not found.");
		}
		
		d = JsoupUtils.parse(content);
		es = d.select("#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > h1");
		String title = JsoupUtils.text(es);
		//#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta
		es = d.select("#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta");
		String brand = JsoupUtils.attr(es, "content");
		//#product-content > div.product-variations > ul > li.attribute.clearfix
		es = d.select("div#product_content > div.product-variations > ul > li");
		List<String> colorList = new ArrayList<String>();
		List<String> sizeList = new ArrayList<String>();
		List<Image> swatchList = new ArrayList<Image>();
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String clazz = JsoupUtils.attr(e, "class");
				if(StringUtils.contains(clazz, "hidden")){
					continue;
				}
				boolean imageBased = false;
				//color
				if(StringUtils.isNotBlank(defaultColorValue)){
					imageBased = true;
					String label = JsoupUtils.text(e.select("span > span"));
					if(StringUtils.contains(label, ":")){
						label = StringUtils.substringBefore(label, ":");
					}
					//#swatch_carousel > div > ul > li > div > a
					//#swatch_carousel > div > ul > li > div.selected
					Elements eles = e.select("div > div > ul > li > div > a");
					if(eles != null && eles.size() > 0){
						for(Element ele : eles){
							String name = JsoupUtils.attr(ele, "title");
							String key = StringUtils.EMPTY;
							String value = StringUtils.EMPTY;
							String skuParamUrl = JsoupUtils.attr(ele.select("a"), "href");
							if(StringUtils.isNotBlank(skuParamUrl)){
								skuParamUrl = skuParamUrl.replace("&amp;", "&");
							}
							String[] skuPrampArr = StringUtils.split(StringUtils.substringAfter(skuParamUrl, "?"), "&");
							if(skuPrampArr != null && skuPrampArr.length > 0){
								for(String skuParam : skuPrampArr){
									if(StringUtils.containsIgnoreCase(skuParam, "color")){
										key = StringUtils.substringBefore(skuParam, "=");
										value = StringUtils.substringAfter(skuParam, "=");
										if(StringUtils.isBlank(value)){
											value = escapeURIPathParam(defaultColorValue);
										}
									}
								}
							}
							String swatchUrl = JsoupUtils.attr(ele.select("img"), "data-src");
							colorList.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
						}
					}
				} else {
					imageBased = true;
					String value = "default";
					String label = "color";
					String name = "default";
					String swatchUrl = StringUtils.EMPTY;
					String key = "default";
					colorList.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
				}
				//size
				if(StringUtils.isNotBlank(defaultSizeValue)){
					imageBased = false;
					String label = JsoupUtils.text(e.select("label"));
					if(StringUtils.contains(label, ":")){
						label = StringUtils.substringBefore(label, ":");
					}
					Elements eles = d.select("select.variation-select");
					String swatchUrl = StringUtils.EMPTY;
					if(eles != null && eles.size() > 0){
						for(Element ele : eles){
							String key = JsoupUtils.attr(ele, "name");
							Elements elements = ele.select("option");
							for(Element element : elements){
								String name = JsoupUtils.text(element);
								String value = escapeURIPathParam(name);
								sizeList.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
							}
						}
					}
				}
				
			}
		}
		
		//all skus
		List<String> skuList = combinate(colorList,sizeList);
		
		//switch image
		context.getUrl().getImages().put(System.currentTimeMillis()+"", swatchList);
		//组装所有的skuurl
		Map<String,Url> skuUrlMap = new HashMap<String,Url>();
		for(String item : skuList){
			String[] skuProp  = StringUtils.split(item, "||");
			StringBuilder skuUrlBuilder = new StringBuilder(SKU_URL_PREFIX);
			skuUrlBuilder.append("&pid=").append(itemId);
			StringBuilder  skuKey = new StringBuilder();
			if(skuProp != null && skuProp.length > 0){
				for(String prop : skuProp){
					String key = StringUtils.substringAfterLast(prop, ";");
					String value = StringUtils.substringBefore(prop, ";");
					skuKey.append(value).append(";");
					if(StringUtils.equals(key, "default")){
						continue;
					}
					skuUrlBuilder.append("&"+key).append("="+value);
				}
				if(skuKey.length() > 0){
					skuKey.deleteCharAt(skuKey.length() -1);
				}
			}
			Url skuUrl = new Url(skuUrlBuilder.toString());
			skuUrl.setTask(context.getUrl().getTask());
			skuUrlMap.put(skuKey.toString(), skuUrl);
		}
		//multi threads request for price and stock
		Map<String,String> priceStockMap = getPriceStock(skuUrlMap);
		
		//sku 组装
		boolean stockFlag = false;
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		Map<String,String> style_map = new HashMap<String,String>();
		boolean defaultDisplay = true;
		float defaultOrig = 0;
		float defaultSale = 0;
		String defaultUnit = StringUtils.EMPTY;
		for(String itemSkus : skuList){
			String[] itemSkuArr = StringUtils.split(itemSkus, "||");
			LSelectionList lSelectionList = new LSelectionList();
			List<Selection> selections = new ArrayList<Selection>();
			StringBuilder priceKey = new StringBuilder();
			String skuId = StringUtils.EMPTY;
			for(String item : itemSkuArr){
				String[] itemArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(item, ";");
				String optionKey = itemArr[1];
				String optionValue = itemArr[2];
				String imageBase = itemArr[4];
				if(StringUtils.equals(imageBase, "true")){
					skuId = itemArr[0];
					priceKey.append(skuId).append(";");
					String swatchUrl = itemArr[3];
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setStyle_id(optionValue);
					String sku_id = style_map.get(skuId);
					if(StringUtils.isBlank(sku_id)){
						LStyleList lStyleList = new LStyleList();
						lStyleList.setStyle_id(optionValue);
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(swatchUrl);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name(optionKey);
						lStyleList.setStyle_name(optionValue);
						if(defaultDisplay){//
							lStyleList.setDisplay(true);
						}
						l_style_list.add(lStyleList);
						style_map.put(skuId, skuId);
					}
				} else {
					String value = itemArr[0];
					if(StringUtils.isNotBlank(optionValue)){
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name(optionKey);
						selection.setSelect_value(optionValue);
						selections.add(selection);
					}
					priceKey.append(value);
				}
			}
			if(priceKey.length() > 0 && StringUtils.endsWith(priceKey.toString(), ";")){
				priceKey.delete(priceKey.length()-1, priceKey.length());
			}
			//sku price stock
			String priceStock = priceStockMap.get(priceKey.toString());
			if(priceStock == null){
				continue;
			}
			float sale = 0;
			float orig = 0;
			String unit = StringUtils.EMPTY;
			int stockStatus = 0;
			String[] arr = StringUtils.splitByWholeSeparatorPreserveAllTokens(priceStock, ";");
			if(arr != null && arr.length > 0 ){
				String salePrice =  arr[0];
				String origPrice = arr[1];
				String stock = arr[2];
				String[] imageArr = StringUtils.split(arr[3], "|");
				if(StringUtils.isNotBlank(salePrice)){
					String currency = StringUtils.substring(salePrice, 0, 1);
					unit = Currency.codeOf(currency).name();
					sale = Float.valueOf(StringUtils.substring(salePrice, 1));
				}
				if(StringUtils.isNotBlank(origPrice)){
					orig = Float.valueOf(StringUtils.substring(origPrice, 1));
				}
				if(orig == 0){
					orig = sale;
				}
				if(defaultDisplay){
					defaultOrig = orig;
					defaultSale = sale;
					defaultUnit = unit;
					defaultDisplay = false;
				}
				if(StringUtils.equals("1", stock)){
					stockStatus = 1;
					stockFlag = stockFlag || true;
				} else {
					stockFlag = stockFlag || false;
				}
				
				if(imageArr != null && imageArr.length > 0 ){
					List<Image> pics = new ArrayList<Image>();
					for(String imageUrl : imageArr){
						if (StringUtils.isNotBlank(imageUrl)) {
							Image image = new Image(imageUrl);
							pics.add(image);
						}
					}
					context.getUrl().getImages().put(skuId, pics);// picture download
				}
			}
			
			lSelectionList.setSale_price(sale);
			lSelectionList.setOrig_price(orig);
			lSelectionList.setPrice_unit(unit);
			lSelectionList.setStock_status(stockStatus);
			lSelectionList.setStock_number(0);
			lSelectionList.setSelections(selections);
			l_selection_list.add(lSelectionList);
		}
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);

		// full doc info
		String docid = SpiderStringUtil.md5Encode(url);
		String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
		rebody.setDOCID(docid);
		rebody.setSite(new Site(domain));
		rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
		//title 
		rebody.setTitle(new Title(title, "", "", ""));
		// price 
		if(defaultOrig == 0 || defaultSale == 0){
			logger.error("Error while fetching spuprice from yslbeautyus.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"yslbeautyus.com category url :"+context.getUrl().toString()+" spuprice is null.");
		}
		int save = Math.round((1 - defaultSale / defaultOrig) * 100);// discount
		rebody.setPrice(new Price(defaultOrig, save, defaultSale, defaultUnit));
		// stock
		int stockStatus = 0;
		if(stockFlag){
			stockStatus = 1;
		}
		rebody.setStock(new Stock(stockStatus));
		// images l_image_list
		// rebody.setImage(new LImageList(pics));
		// brand
		rebody.setBrand(new Brand(brand , "","",""));
		// Category
		//body > div.main.skincare_or_rouge > div > ul.breadcrumb > li > a > span
		es = d.select("ul.breadcrumb > li");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if(es != null && es.size() > 0){
			for(Element ele :es ){
				String prop = JsoupUtils.attr(ele, "itemprop");
				if(!StringUtils.contains(prop, "itemListElement")){
					continue;
				}
				String text = JsoupUtils.text(ele.select("a > span"));
				if(StringUtils.isBlank(text)){
					continue;
				}
				cats.add(text);
				breads.add(text);
			}
		}
		rebody.setCategory(cats);
		// BreadCrumb
		rebody.setBreadCrumb(breads);
		// description
		Map<String, Object> descMap = new HashMap<String, Object>();
		es = d.select("div.product_detail_description");
		StringBuilder sb = new StringBuilder();
		if(es != null && es.size() > 0){
			for(Element ele : es){
				String text = JsoupUtils.text(ele);
				if(StringUtils.isBlank(text)){
					continue;
				}
				sb.append(text);
			}
		}
		descMap.put("en", sb.toString());
		rebody.setDescription(descMap);
		//feature
		Map<String, Object> featureMap = new HashMap<String, Object>();
		rebody.setFeatureList(featureMap);

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "women");
		rebody.setProperties(propMap);
		rebody.setSku(sku);
		setOutput(context, rebody);
		//System.out.println(rebody.parseTo());
	}

	private Map<String,String> getPriceStock(Map<String, Url> skuUrlMap) {
		Map<String,String> result = new HashMap<String,String>();
		 ExecutorService service = Executors.newFixedThreadPool(nThreads);
		try {
			List<PriceStockCall> calls = new ArrayList<PriceStockCall>();
			for(Map.Entry<String, Url> entry: skuUrlMap.entrySet()){
				String skuId = entry.getKey();
				Url skuUrl = entry.getValue();
				calls.add(new PriceStockCall(skuId, skuUrl));
			}
			List<Future<Map<String,String>>> futures = service.invokeAll(calls);
			for(Future<Map<String,String>> f : futures){
				try {
					Map<String,String> map = f.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
					result.putAll(map);
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			service.shutdownNow();
		}
		return result;
	}
	
	public List<String> combinate(List<String> l1,List<String> l2){
		List<String> newList = new ArrayList<String>();
		if(l1 == null || l2 == null){
			return new ArrayList<String>();
		}
		if(l1.size() == 0 && l2.size() > 0){
			newList.addAll(l2);
			return newList;
		}
		if(l1.size() > 0 && l2.size() == 0){
			newList.addAll(l1);
			return newList;
		}
		for(int i=0; i < l1.size(); i++){
			for(int j=0; j<l2.size();j++){
				newList.add(l1.get(i)+"||"+l2.get(j));
			}
		}
		return newList;
	}

	class PriceStockCall implements Callable<Map<String,String>> {
		
		private String skuId;
		private Url skuUrl;
		
		private PriceStockCall(String skuId,Url skuUrl){
			this.skuId = skuId;
			this.skuUrl = skuUrl;
		}

		@Override
		public Map<String,String> call() throws Exception {
			Map<String,String> result = new HashMap<String,String>();
			try{
				String content = HttpUtils.get(skuUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES, false);
				//System.out.println(skuUrl.getValue());
				//String content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).url(skuUrl.getValue()).resultAsString();
				if(StringUtils.isNotBlank(content)){
					content = StringUtils.replacePattern(content, "\\\r|\\\n|\\\t", "");
				}
				/*<p class="product_price price_sale b-product_price-sale" title="Sale Price" data-pricevalue="90.0" itemprop="price">
				<span class="product_price_title b-product_price-title"></span>
				$90.00
				</p>*/
				String salePrice = StringUtils.EMPTY;
				Pattern p = Pattern.compile("<span[^>]*class=\"product_price_currency\"[^>]*>[^<]*</span>(.*?)</p>");
				Matcher m = p.matcher(content);
				if(m.find()){
					salePrice = StringUtils.replace(m.group(1), ",", "");
				}
				
				
				//#dwfrm_product_addtocart_d0wywhapvdqs > fieldset > div.price.b-price
				/*Elements es = d.select("div.product_add_to_cart > form > fieldset > div.price.b-price");
				if(es != null && es.size() > 0 ){
					salePrice = StringUtils.trim(es.get(0).text());
					if(StringUtils.contains(salePrice, ",")){
						salePrice = StringUtils.replace(salePrice, ",", "");
					}
				}*/
				
				//orig
				p = Pattern.compile("<span[^>]*class=\"price-standard\">(.*?)</span>");
				m = p.matcher(content);
				String origPrice = StringUtils.EMPTY;
				if(m.find()){
					origPrice = StringUtils.replace(m.group(1), ",", "");
				}
				//stock
				p = Pattern.compile("<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*>[^<]*In Stock[^<]*<|<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*class=\"product_limit_threshold\">[^<]*Only \\d+ available[^<]*<");
				m = p.matcher(content);
				String stockStatus = StringUtils.EMPTY;
				if(m.find()){
					stockStatus = "1";
				} else {
					stockStatus = "0";
				}
				
				/*p = Pattern.compile("<div[^>]*class=\"jcarousel-clip jcarousel-clip-horizontal[^>]*>[^<]*<ul[^>]*class=\"contentcarousel_list\">[^<]*<li[^>]*class=\"thumb[^>]*selected[^>]*>[^<]*<a[^>]*href=(.*?) target[^>]*>");
				m = p.matcher(content);
				String imageUrl = StringUtils.EMPTY;
				if(m.find()){
					imageUrl = m.group(1);
					if(StringUtils.contains(imageUrl, "&amp;")){
						imageUrl = StringUtils.replace(imageUrl, "&amp;", "&");
					}
				}*/
				
				//#main-image-container > div.carousel.contentcarousel.horizontal_carousel > div > ul.contentcarousel_list > li.image contentcarousel_list_item > a
				Document d = JsoupUtils.parse(content);
				Elements es = d.select("ul.contentcarousel_list > li.image.contentcarousel_list_item > a");
				StringBuilder sb = new StringBuilder();
				for(Element e : es){
					String src = JsoupUtils.attr(e, "data-tablet-src");
					sb.append(src).append("|");
					//System.out.println("src:"+src);
				}
				if(sb.length() > 0){
					sb.deleteCharAt(sb.length()-1);
				}
				result.put(skuId, salePrice+";"+origPrice+";"+stockStatus+";"+sb.toString());
			}catch(Throwable e){
				e.printStackTrace();
			}
			return result;
		}
	}
	
	public static String escapeURIPathParam(String input) {
		StringBuilder resultStr = new StringBuilder();
		for (char ch : input.toCharArray()) {
			if (isUnsafe(ch)) {
				resultStr.append('%');
				resultStr.append(toHex(ch / 16));
				resultStr.append(toHex(ch % 16));
			} else {
				resultStr.append(ch);
			}
		}
		return resultStr.toString();
	}

	private static char toHex(int ch) {
		return (char) (ch < 10 ? '0' + ch : 'a' + ch - 10);
	}

	private static boolean isUnsafe(char ch) {
		if (ch > 128 || ch < 0)
			return true;
		return " %.$&+,/:;=?@<>#%".indexOf(ch) >= 0;
	}

	public static void main(String[] args) throws Exception {
		
		Yslbeautyus20170525 shan = new Yslbeautyus20170525();
		Context context = new Context();
		//http://www.yslbeautyus.com/or-rouge-cleansing-cream/490YSL.html
		//http://www.yslbeautyus.com/rouge-volupte-shine-oil-in-stick-holiday-2016/912YSL.html
		//http://www.yslbeautyus.com/mon-paris-body-lotion/4781YSL.html
		//http://www.yslbeautyus.com/pour-homme/537YSL.html
		context.setUrl(new Url("http://www.yslbeautyus.com/forever-light-creator-cc-primer/890YSL.html"));
		//http://www.yslbeautyus.com/forever-light-creator-cc-primer/890YSL.html
		shan.invoke(context);
		//variation-select
		//String url = "http://www.yslbeautyus.com/on/demandware.store/Sites-ysl-us-Site/en_US/Product-Variation?pid=490YSL&dwvar_490YSL_size=5%2e0%20oz%2f150%20ML&quantity=1&action=variationChanged&format=ajax";
		/*String url = "http://www.yslbeautyus.com/or-rouge-cleansing-cream/490YSL.html";
		Map<String,Object> headers = new HashMap<String,Object>();
		
		String content = Crawler.create().method("get").header(headers).timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta");
		String brand = JsoupUtils.attr(es, "content");
		System.out.println(brand);*/
		
		/*if(StringUtils.isNotBlank(content)){
			content = StringUtils.replacePattern(content, "\\\r|\\\n|\\\t", "");
		}
		Pattern p = Pattern.compile("<div[^>]*class=\"jcarousel-clip jcarousel-clip-horizontal[^>]*>[^<]*<ul[^>]*class=\"contentcarousel_list\">[^<]*<li[^>]*class=\"thumb[^>]*selected[^>]*>[^<]*<a[^>]*href=(.*?) target[^>]*>");
		Matcher m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group(1));
		} */
		//                                    5%2e0%20oz%2f150%20ML 
		/*System.out.println(URLDecoder.decode("5%2e0%20oz%2f150%20ML"));
		System.out.println(escapeURIPathParam("5.0 oz/150 ML"));
		System.out.println(0xFF);
		System.out.println(Integer.toBinaryString(0xFF));
		System.out.println((int)'a' );
		System.out.println(Integer.parseUnsignedInt("11111111111111111111111111111110", 2));
		System.out.println('5'/ 16);
		System.out.println(toHex('5'/ 16));
		System.out.println(0x0F);*/
		
		//System.out.println(BCConvert.bj2qj("¥").equals("￥"));
		/*String url = "http://www.6pm.com/pendleton-petite-malena-shirt-blue-large-check";
		
		String html = Crawler.create().timeOut(10000).retry(3).url(url
		// "http://www.6pm.com/ivanka-trump-kayden-4-black-patent"
				).resultAsString();
		Document d = JsoupUtils.parse(html);
		Elements es = d.select("span#stock");
		System.out.println(es.get(0).text());
		Pattern p = Pattern.compile("Your Search For(.*?)Found 0 Results");
		Matcher m = p.matcher(html);
		if(m.find()){
			String itemId = m.group(1);
			System.out.println(itemId);
		}*/
		/*String url = "http://www.yslbeautyus.com/on/demandware.store/Sites-ysl-us-Site/en_US/Product-Variation?pid=1006YSL&dwvar_1006YSL_color=Indiscreet+Purple&quantity=1&action=variationChanged&format=ajax";
		String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
		Document d = JsoupUtils.parse(content);
		//#main-image-container > div.carousel.contentcarousel.horizontal_carousel > div > ul.contentcarousel_list > li.image contentcarousel_list_item > a
		Elements es = d.select("ul.contentcarousel_list > li.image.contentcarousel_list_item > a");
		System.out.println(es.html());
		for(Element e : es){
			System.out.println(JsoupUtils.attr(e, "data-tablet-src"));
		}*/
		
		/*Pattern p = Pattern.compile("<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*>[^<]*In Stock[^<]*<|<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*class=\"product_limit_threshold\">[^<]*Only \\d+ available[^<]*<");
		Matcher m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group());
		}*/
		//out of stock
		
		/*p = Pattern.compile("<div[^>]*class=\"availability-msg\">[^<]*<p[^>]*>[^<]*out of stock[^<]*<");
		m = p.matcher(content);
		if(m.find()){
			System.out.println("stock=0");
		} else {
			System.out.println("stock=2");
		}
		//<li class="thumb
		p = Pattern.compile("<li[^>]*class=\"thumb[^>]*>[^<]*<a[^>]*href=\"(.*?)\"");
		m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group(1));
		}*/
	}
}