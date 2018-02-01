package com.haitao55.spider.crawler.core.callable.custom.katespade;

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
import com.haitao55.spider.common.http.Crawler;
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
  * @ClassName: KateSpade
  * @Description: kate详情页面
  * @author songsong.xu
  * @date 2016年11月19日 下午3:43:48
  *
 */
public class KateSpade_bak extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.katespade.com";
	private final String SKU_URL_PREFIX = "https://www.katespade.com/on/demandware.store/Sites-Shop-Site/en_US/Product-Variation?Quantity=1&format=ajax&instart_disable_injection=true";
	//&pid=S944638KS&dwvar_S944638KS_size=6&dwvar_S944638KS_color=001
	private final String SURPRISE_SKU_URL_PREFIX ="http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Variation?Quantity=1&format=ajax";
			//?pid=WKRU4112&dwvar_WKRU4112_color=706&dwvar_WKRU4112_size=UNS&Quantity=1&format=ajax
	public static final int nThreads = 3;
	@Override
	public void invoke(Context context) throws Exception {
		//String content = super.getInputString(context);
		String content = Crawler.create().timeOut(60000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(context.getUrl().getValue()).resultAsString();
		String url = context.getUrl().toString();
		RetBody rebody = new RetBody();
		//<div class="availability-msg"><p class="not-available-msg out-of-stock">out of stock</p>
		Pattern pattern = Pattern.compile("<div[^>]*class=\"availability-msg\">[^<]*<p[^>]*class=\"not-available-msg[^>]*out-of-stock\">out of stock</p>");//<span[^>]*class=\"price-sales\">(.*?)</span>
		Matcher matcher = pattern.matcher(content);
		if(matcher.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"katespade.com itemUrl: "+context.getUrl().toString()+" is offline..");
		}
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("#product-content > div.product-variations");
		String defaultColorValue = StringUtils.EMPTY;
		String defaultSizeValue = StringUtils.EMPTY;
		String itemId = StringUtils.EMPTY;
		if(es != null && es.size() > 0 ){
			String data = JsoupUtils.attr(es, "data-current");
			Type type = new TypeToken<JsonObject>() {private static final long serialVersionUID = 1L;}.getType();
			JsonObject defObj = JsonUtils.json2bean(data, type);
			if(defObj != null && !defObj.isJsonNull() && defObj.has("color") && defObj.has("size")){
				JsonObject color = defObj.getAsJsonObject("color");
				JsonObject size = defObj.getAsJsonObject("size");
				if(color != null){
					defaultColorValue = color.getAsJsonPrimitive("value").getAsString();
					//color.getAsJsonPrimitive("displayValue").getAsString();
				}
				if(size != null){
					defaultSizeValue = size.getAsJsonPrimitive("value").getAsString();
					//size.getAsJsonPrimitive("displayValue").getAsString();
				}
			}
			itemId = JsoupUtils.attr(es, "data-master");
		} else {
			logger.error("Error while fetching product-variations from katespade.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"katespade.com category url :"+context.getUrl().toString()+" product-variations is not found.");
		}
		String datalayer = StringUtils.substringBetween(content, "var cmBrand = ", "</script>");
		String title = StringUtils.substringBetween(datalayer, "var cmProductName = \"", "\";");
		String brand = "kate spade NEW YORK";
		//#product-content > div.product-variations > ul > li.attribute.clearfix
		es = d.select("#product-content > div.product-variations > ul > li.attribute.clearfix");
		List<String> skuList = new ArrayList<String>();
		List<Image> swatchList = new ArrayList<Image>();
		if(es != null && es.size() > 0 ){
			for(Element e : es){
				String label = JsoupUtils.text(e.select("span.label"));
				//#product-content > div.product-variations > ul > li:nth-child(1) > div > ul > li.selected
				Elements eles = e.select("div.value > ul > li");
				List<String> selecter = new ArrayList<String>();
				//String skuId = StringUtils.EMPTY;
				boolean imageBased = false;//COLOR
				if(StringUtils.contains(label, "COLOR")){
					imageBased = true;
				}
				if(eles != null && eles.size() > 0){
					for(Element ele : eles){
						String clazz = JsoupUtils.attr(ele, "class");
						if(StringUtils.contains(clazz, "visually-hidden")){
							continue;
						}
						String key = StringUtils.EMPTY;
						String value = StringUtils.EMPTY;
						String swatchUrl = StringUtils.EMPTY;
						String name = StringUtils.EMPTY;
						if(imageBased){
							name = JsoupUtils.text(ele.select("span.title"));
							key = JsoupUtils.attr(ele, "data-name");
							value = JsoupUtils.attr(ele, "data-value");
							swatchUrl = JsoupUtils.attr(ele.select("a > img"), "src");
							selecter.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
							swatchList.add(new Image(swatchUrl));
						} else {
							String href  = JsoupUtils.attr(ele.select("a"), "href");
							if(StringUtils.isNotBlank(href)){
								String paramKV = StringUtils.substringAfterLast(href, "&");
								key = StringUtils.substringBefore(paramKV, "=");
								value = StringUtils.substringAfter(paramKV, "=");
								name = JsoupUtils.attr(ele.select("a"), "title");
							} else {
								key = "dwvar_"+itemId+"_size";
								value = defaultSizeValue;
							}
							selecter.add(value+";"+label+";"+name+";"+swatchUrl+";"+imageBased+";"+key);
						}
					}
					//all skus
					skuList = combinate(skuList,selecter);
				}
			}
		}
		//switch image
		context.getUrl().getImages().put(System.currentTimeMillis()+"", swatchList);
		//组装所有的skuurl
		Map<String,Url> skuUrlMap = new HashMap<String,Url>();
		for(String item : skuList){
			String[] skuProp  = StringUtils.split(item, "||");
			StringBuilder skuUrlBuilder = null;
			if(StringUtils.contains(url, "surprise.katespade.com")){
				skuUrlBuilder = new StringBuilder(SURPRISE_SKU_URL_PREFIX);
			} else {
				skuUrlBuilder = new StringBuilder(SKU_URL_PREFIX);
			}
			skuUrlBuilder.append("&pid=").append(itemId);
			StringBuilder  skuKey = new StringBuilder();
			if(skuProp != null && skuProp.length > 0){
				for(String prop : skuProp){
					String key = StringUtils.substringAfterLast(prop, ";");
					String value = StringUtils.substringBefore(prop, ";");
					skuUrlBuilder.append("&"+key).append("="+value);
					skuKey.append(value).append(";");
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
			logger.error("Error while fetching spuprice from katespade.com's detail page,url {}",context.getUrl().toString());
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"katespade.com category url :"+context.getUrl().toString()+" spuprice is null.");
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
		es = d.select("#main > div > ol > li > a");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if(es != null && es.size() > 0){
			for(Element ele :es ){
				String text = ele.text();
				if(StringUtils.isBlank(text)){
					continue;
				}
				cats.add(text);
				breads.add(text);
			}
		} else {
			cats.add("home");
			cats.add(title);
			breads.add("home");
			breads.add(title);
		}
		rebody.setCategory(cats);
		// BreadCrumb
		rebody.setBreadCrumb(breads);
		// description
		Map<String, Object> descMap = new HashMap<String, Object>();
		es = d.select("#small-details");
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
		es = d.select("#small-description > div.short-left > ul > li");
		int count = 1;
		Map<String, Object> featureMap = new HashMap<String, Object>();
		if(es != null && es.size() > 0){
			for(Element ele : es){
				String text = JsoupUtils.text(ele);
				if(StringUtils.isBlank(text)){
					continue;
				}
				featureMap.put("feature-" + count, ele.text());
				count++;
			}
		}
		es = d.select("#small-description > div.short-right > ul > li");
		if(es != null && es.size() > 0){
			for(Element ele : es){
				String text = JsoupUtils.text(ele);
				if(StringUtils.isBlank(text)){
					continue;
				}
				featureMap.put("feature-" + count, ele.text());
				count++;
			}
		}
		rebody.setFeatureList(featureMap);

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "women");
		rebody.setProperties(propMap);
		rebody.setSku(sku);
		//setOutput(context, rebody);
		System.out.println(rebody.parseTo());
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
				//String content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).url(skuUrl.getValue()).resultAsString();
				//http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=WKRU3974
				//<span class="price-standard">$429.00</span>
				//<span class="price-sales">$129.00</span>
				//sale
				Pattern p = Pattern.compile("<span[^>]*class=\"price-sales\">(.*?)</span>");
				Matcher m = p.matcher(content);
				String salePrice = StringUtils.EMPTY;
				if(m.find()){
					salePrice = StringUtils.replace(m.group(1), ",", "");
				}
				//orig
				p = Pattern.compile("<span[^>]*class=\"price-standard\">(.*?)</span>");
				m = p.matcher(content);
				String origPrice = StringUtils.EMPTY;
				if(m.find()){
					origPrice = StringUtils.replace(m.group(1), ",", "");
				}
				//stock
				p = Pattern.compile("<div[^>]*class=\"availability-msg\"[^>]*>[^<]*<p[^>]*>[^<]*out of stock[^<]*<");
				m = p.matcher(content);
				String stockStatus = StringUtils.EMPTY;
				if(m.find()){
					stockStatus = "0";
				} else {
					stockStatus = "1";
				}
				
				p = Pattern.compile("<li[^>]*class=\"thumb[^>]*>[^<]*<a[^>]*href=\"(.*?)\"");
				m = p.matcher(content);
				StringBuilder sb = new StringBuilder();
				while(m.find()){
					String imageUrl = m.group(1);
					if(StringUtils.contains(imageUrl, "?")){
						String prefix = StringUtils.substringBefore(imageUrl, "?");
						imageUrl = prefix+"?$large$";
					}
					sb.append(imageUrl).append("|");
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

	public static void main(String[] args) throws Exception {
		//http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=098689980096
		//http://surprise.katespade.com/on/demandware.store/Sites-KateSale-Site/en_US/Product-Show?pid=WKRU4142
		//https://www.katespade.com/products/pax-heels/S944638KS.html
		KateSpade shan = new KateSpade();
		Context context = new Context();
		//https://www.katespade.com/products/pax-heels/S944638KS.html
		//https://www.katespade.com/products/pleated-cape-dress/NJMU6852.html
		//http://www.katespade.com/products/watercolor-leopard-rug/RUG127873.html
		//http://www.katespade.com/products/embroidered-medallion-pillow/259MDLNRU.html
		//http://www.katespade.com/products/dot-stamp-oblong-pillow/259DOTSRU.html
		//http://www.katespade.com/products/splatter-paint-rug/RUG133381.html
		//http://www.katespade.com/products/mariner-stripe-rug/RUG133294.html
		context.setUrl(new Url("https://www.katespade.com/products/milford-heels/S5150011P.html"));
		shan.invoke(context);
		
		/*String url = "https://www.amazon.co.jp/dp/B0026R3PFW/";
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Cookie", "lc-acbjp=en_US;");
		String content = Crawler.create().method("get").header(headers).timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
		System.out.println(content);
		
		if(StringUtils.contains(content, "Ships from and sold by Amazon.co.jp")){
			System.out.println("true");
		}*/
		//lc-acbjp=en_US;
		
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
		/*String url = "https://www.katespade.com/on/demandware.store/Sites-Shop-Site/en_US/Product-Variation?pid=NJMU6852&dwvar_NJMU6852_color=639&dwvar_NJMU6852_size=0&Quantity=1&format=ajax&instart_disable_injection=true";
		//https://www.katespade.com/products/pleated-cape-dress/716454013557.html
		String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
		//<span class="price-sales">$350.00</span>
		Pattern p = Pattern.compile("<span[^>]*class=\"price-sales\">(.*?)</span>");
		Matcher m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group(1));
		}
		//out of stock
		
		p = Pattern.compile("<div[^>]*class=\"availability-msg\">[^<]*<p[^>]*>[^<]*out of stock[^<]*<");
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
        /*String url = "https://www.katespade.com/on/demandware.store/Sites-Shop-Site/en_US/Product-Variation?Quantity=1&format=ajax&instart_disable_injection=true&pid=PXRU5931-2";
		String content = Crawler.create().timeOut(60000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(url).resultAsString();
		Pattern p = Pattern.compile("<div[^>]*class=\"availability-msg\"[^>]*>[^<]*<p[^>]*>[^<]*out of stock[^<]*<");
        Matcher m = p.matcher(content);
        String stockStatus = StringUtils.EMPTY;
        if(m.find()){
            stockStatus = "0";
        } else {
            stockStatus = "1";
        }
        System.out.println(stockStatus ); */
		
	}
}