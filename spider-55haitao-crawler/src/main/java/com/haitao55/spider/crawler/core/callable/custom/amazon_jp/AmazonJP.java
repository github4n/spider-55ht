package com.haitao55.spider.crawler.core.callable.custom.amazon_jp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

public class AmazonJP extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	public static final String MAIN_DOMAIN = "amazon.co.jp";
	private static final String DOMAIN = "www." + MAIN_DOMAIN;
	public static final String ITEM_URL_TMT = "https://www.amazon.co.jp/dp/%s/?psc=1";
	public static final String UNIT = "JPY";
	public static final String JPY_SYMBOL = "￥";
	public static final String STOCK_PATTERN = "在庫あり|通常.*以内に発送|残り(\\d+)点";
	public static final String SELF_PATTERN  = ".*"+MAIN_DOMAIN+".*";
	public static final String COUNTRY = "US";
	public static final int nThreads = 10;
	private static final ExecutorService service = Executors.newFixedThreadPool(nThreads);
	public static final String get_ip = "http://lumtest.com/myip.json";
	
	@Override
	public void invoke(Context context) throws Exception {
		
		String orignalUrl = context.getUrl().getValue();
		String itemId = SpiderStringUtil.getAmazonItemId(orignalUrl);
		String reqUrl = String.format(ITEM_URL_TMT, itemId);
		String content =  "";
		for(int i= 0; i < 20; i++){
			LumHttpClient client = new LumHttpClient();
			String getIp = client.request(get_ip, null);
			String currentIp = StringUtils.substringBetween(getIp, "\"ip\":\"", "\",");
			content = client.request(reqUrl,getIndexHeaders(reqUrl));
			boolean isforbiden = false;
			if(StringUtils.contains(content, "Amazon CAPTCHA")){
				isforbiden = true;
			} else {
				currentIp = "";
			}
			logger.info("itemUrl={} , isforbiden= {} ,forbiddenIp = {} ",orignalUrl, isforbiden,currentIp);
			if(!isforbiden){
				break;
			}
		}
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			
			String docid = SpiderStringUtil.md5Encode(orignalUrl);
			String url_no = SpiderStringUtil.md5Encode(orignalUrl);
			rebody.setDOCID(docid);
			rebody.setSite(new Site(DOMAIN));
			rebody.setProdUrl(new ProdUrl(orignalUrl, System.currentTimeMillis(), url_no));
			
			Document doc = Jsoup.parse(content);
			
			Elements es = doc.select("#shipsFromSoldBy_feature_div > #merchant-info");
			if(es != null && es.size() > 0 ){
				String selfString = es.text();
				System.out.println("itemId = " + itemId +" , selfString="+ selfString);
				Pattern pShips = Pattern.compile(SELF_PATTERN, Pattern.CASE_INSENSITIVE);
				Matcher mShips = pShips.matcher(selfString);
				if(!mShips.find()){
					logger.error("this product {} is not self. ",  orignalUrl);
					throw new ParseException(CrawlerExceptionCode.OFFLINE, String.format("this product %s is not self. ", orignalUrl));
				}
			}
			//title
			String title = "";
			es = doc.select("#productTitle");
			if(es != null && es.size() > 0 ){
				title = es.get(0).text();
			}
			rebody.setTitle(new Title("","",title,""));
			
			//brands
			String brand = "";
			es = doc.select("#brand");
			if(es != null && es.size() > 0 ){
				brand = es.get(0).text();
			}
			rebody.setBrand(new Brand("","",brand,""));
			
			//cats 
			List<String> cats = Lists.newArrayList();
			//breads
			List<String> breads = Lists.newArrayList();
			es = doc.select("#wayfinding-breadcrumbs_feature_div > ul > li");
			String catsString = es.text();
			if(es != null && es.size() > 0 ){
				es.forEach( e -> {
					String clazz = e.attr("class");
					if(StringUtils.isNotBlank(clazz) && StringUtils.contains(clazz, "a-breadcrumb-divider")){
						return;
					}
					String cat = StringUtils.trim(e.text());
					cats.add(cat);
					breads.add(cat);
				}); 
			} else {
				//#SalesRank > ul > li > span.zg_hrsr_ladder
				es = doc.select("SalesRank > ul > li > span.zg_hrsr_ladder");
				catsString = es.text();
				if(es != null && es.size() > 0 ){
					String catString = StringUtils.trim(StringUtils.substring(es.get(0).text(), 1));
					String[] catArr = StringUtils.split(catString, ">");
					if(catArr != null && catArr.length > 0 ){
						for(String item : catArr){
							cats.add(item);
							breads.add(item);
						}
					}
					
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			
			
			//feature 
			Map<String, Object> featureMap = Maps.newHashMap();
			es = doc.select("#feature-bullets > ul > li > span.a-list-item");
			if (es != null && es.size() > 0) {
				int count = 1;
				for (Element e : es) {
					featureMap.put("feature-" + count, e.text());
					count++;
				}
			}
			rebody.setFeatureList(featureMap);
			
			//description
			Map<String, Object> descMap = Maps.newHashMap();
			es = doc.select("#productDescription");
			String description = "";
			if(es != null && es.size() > 0 ){
				description = es.get(0).text();
			}
			descMap.put("jp", description);
			rebody.setDescription(descMap);
			
			//properties
			Map<String, Object> propMap = Maps.newHashMap();
			//メンズ men
			//レディース women
			//ガールズ
			//ボーイズ
			String s_gender = "all";
			if(StringUtils.contains(catsString, "メンズ") || StringUtils.contains(catsString, "ボーイズ")){
				s_gender = "men";
			} else if(StringUtils.contains(catsString, "レディース") || StringUtils.contains(catsString, "ガールズ")){
				s_gender = "women";
			}
			propMap.put("s_gender", s_gender);
			es = doc.select("#detail-bullets_feature_div div.content > ul > li");
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
			
			
			//price
			float orig = 0f;
			float sale = 0f;
			es = doc.select("#priceblock_ourprice");
			if(es != null && es.size() > 0 ){
				String salePriceString = es.get(0).text();
				try{
					if(StringUtils.contains(salePriceString, "-")){
						throw new Exception("orignalUrl "+orignalUrl+" is price range " +salePriceString);
					}
					salePriceString = salePriceString.replace(JPY_SYMBOL, "").replace(",", "");
					Pattern pattern = Pattern.compile("((0(\\.\\d{1,10})?)|([1-9]\\d*(\\.\\d{1,10})?))");
					Matcher matcher = pattern.matcher(salePriceString);
					if(matcher.find()){
						salePriceString = matcher.group(1);
						if(StringUtils.isNotBlank(salePriceString)){
							sale = Float.parseFloat(salePriceString);
						}
					}
				}catch(Exception e){
					logger.error("Exception orignalUrl= {} ,salePriceString= {}", orignalUrl, salePriceString);
					e.printStackTrace();
				}
			}
			es = doc.select("#price_feature_div span.a-text-strike");
			if(es != null && es.size() > 0 ){
				String origPriceString = es.get(0).text();
				try{
					origPriceString = origPriceString.replace(JPY_SYMBOL, "").replace(",", "");
					Pattern pattern = Pattern.compile("((0(\\.\\d{1,10})?)|([1-9]\\d*(\\.\\d{1,10})?))");
					Matcher matcher = pattern.matcher(origPriceString);
					if(matcher.find()){
						origPriceString = matcher.group(1);
						if(StringUtils.isNotBlank(origPriceString)){
							orig = Float.parseFloat(origPriceString);
						}
					}
				}catch(Exception e){
					logger.error("Exception orignalUrl= {} , origPriceString= {} ", orignalUrl, origPriceString);
					e.printStackTrace();
				}
			}
			if(orig == 0f){
				orig = sale;
			}
			if(sale == 0f){
				throw new ParseException(CrawlerExceptionCode.OFFLINE, DOMAIN + " itemUrl : "+orignalUrl+" , sale is 0 ");
			}
			int save = Math.round((1 - sale / orig) * 100);// discount
			rebody.setPrice(new Price(orig, save, sale, UNIT));
			
			
			String currentAsinString = StringUtils.substringBetween(content, "\"currentAsin\" : \"", "\",");
			String parentAsinString = StringUtils.substringBetween(content, "\"parentAsin\" : \"", "\",");
			boolean multiSku = true;
			if(StringUtils.isBlank(currentAsinString) && StringUtils.isBlank(parentAsinString)){
				multiSku = false;
			}
			
			//sku
			List<LSelectionList> l_selection_list = Lists.newArrayList();
			List<LStyleList> l_style_list = Lists.newArrayList();
			
			int stockStatus = 0;
			if(!multiSku){
				
				//process one sku
				
				//stock 
				es = doc.select("#availability_feature_div > #availability > span.a-size-medium");
				String stockString = "";
				if(es != null && es.size() > 0 ){
					stockString = es.get(0).text();
				}
				Pattern pattern = Pattern.compile(STOCK_PATTERN);
				Matcher matcher = pattern.matcher(stockString);
				int stockNumber = 0;
				if(matcher.find()){
					String stockNumString = matcher.group(1);
					if(StringUtils.isNotBlank(stockNumString)){
						stockStatus = 2;
						stockNumber = Integer.valueOf(stockNumString);
					} else {
						stockStatus = 1;
					}
				}
				rebody.setStock(new Stock(stockStatus, stockNumber));
				
				//spu image
				List<Image> imgs = extractOneSkuImages(content);
				context.getUrl().getImages().put(itemId, imgs);
				
			} else {
				
				String imageData = StringUtils.substringBetween(content, "\"indexToColor\"", "</script>");
				JsonObject colorImages = new JsonObject();
				JsonArray visualDimensions = new JsonArray();
				String majorDimension = "";
				List<Image> imgs = Lists.newArrayList();
				if(StringUtils.isNotBlank(imageData)){
					String visualDimensionsString = StringUtils.substringBetween(imageData, "\"visualDimensions\":", ",\"productGroupID\"");
					String colorImagesString = StringUtils.substringBetween(imageData, "data[\"colorImages\"] = ", ";");
					try{
						colorImages = JsonUtils.json2bean(colorImagesString, JsonObject.class);
						if(colorImages != null && !colorImages.isJsonNull() && colorImages.entrySet().size() == 0 ){
							imgs = extractOneSkuImages(content);
						}
						visualDimensions = JsonUtils.json2bean(visualDimensionsString, JsonArray.class);
						if(visualDimensions != null && !visualDimensions.isJsonNull() && visualDimensions.size() > 0 ){
							majorDimension = visualDimensions.get(0).getAsString();
						}
						logger.info("itemUrl {} , visualDimensions {} ",orignalUrl, visualDimensions.toString());
						logger.info("itemUrl {} , colorImages {}",orignalUrl, colorImages.toString());
					}catch(Exception e){
						e.printStackTrace();
						logger.error("visualDimensionsString json 2 jsonarray  {}",visualDimensionsString);
						logger.error("colorImagesString json 2 JsonObject  {}",colorImagesString);
					}
				}
				
				// process multi skus
				String skuString = StringUtils.substringBetween(content, "\"shouldUseDPXTwisterData\"", ";");
				List<Callable<String>> calls = Lists.newArrayList();
				Map<String,String> skuMaps = Maps.newHashMap();
				if(StringUtils.isNotBlank(skuString)){
					skuString = "{\"shouldUseDPXTwisterData\"" +  skuString;
					JsonObject skuJson = JsonUtils.json2bean(skuString, JsonObject.class);
					logger.info("orignalUrl {} , skuJson : {}",skuJson.toString());
					JsonArray dimensions = skuJson.getAsJsonArray("dimensions");
					JsonArray dimensionsDisplay = skuJson.getAsJsonArray("dimensionsDisplay");
					JsonObject dimensionValuesDisplayData = skuJson.getAsJsonObject("dimensionValuesDisplayData");
					if(dimensionValuesDisplayData != null && !dimensionValuesDisplayData.isJsonNull()){
						for(Map.Entry<String, JsonElement>  entry : dimensionValuesDisplayData.entrySet()){
							String skuId = entry.getKey();
							calls.add(new SkuProcessor(skuId));
							JsonArray skuArr = entry.getValue().getAsJsonArray();
							if(skuArr != null && !skuArr.isJsonNull()){
								StringBuilder skuBuilder = new StringBuilder();
								for(int i=0 ; i < skuArr.size(); i++){
									String key  = dimensionsDisplay.get(i).getAsString();
									String value = skuArr.get(i).getAsString();
									String label = dimensions.get(i).getAsString();
									skuBuilder.append(label).append(":").append(key).append(":").append(value).append(";");
								}
								if(skuBuilder.length() > 1){
									skuBuilder = skuBuilder.deleteCharAt(skuBuilder.length() -1);
								}
								skuMaps.put(skuId, skuBuilder.toString());
							}
						}
						//System.out.println(skuMaps);
					}
				}
				
				List<Future<String>> futures = service.invokeAll(calls);
				boolean stockFlag = false;
				Map<String,LStyleList> styleMap = Maps.newHashMap();
				for(Future<String> f : futures){
					try {
						String result = f.get(60, TimeUnit.MILLISECONDS);
						if(StringUtils.isNotBlank(result)){
							String[] item = StringUtils.splitByWholeSeparatorPreserveAllTokens(result, ":");
							if(item != null && item.length > 0 ){
								String skuid = item[0];
								Integer itemStatus = Integer.valueOf(item[1]);
								Integer stockNumber = Integer.valueOf(item[2]);
								if(StringUtils.isBlank(item[3])){
									continue;
								}
								float salePrice = Float.parseFloat(item[3]);
								float origPrice = Float.parseFloat(item[4]);
								stockFlag = stockFlag || (itemStatus >= 1?true:false);
								
								//selectlist
								LSelectionList lselectlist = new LSelectionList();
								lselectlist.setGoods_id(skuid);
								lselectlist.setOrig_price(origPrice);
								lselectlist.setPrice_unit(UNIT);
								lselectlist.setSale_price(salePrice);
								lselectlist.setStock_number(stockNumber);
								lselectlist.setStock_status(itemStatus);
								List<Selection> selections = new ArrayList<Selection>();
								
								//stylelist
								LStyleList lStyleList = new LStyleList();
								if(StringUtils.equals(currentAsinString, skuid)){
									lStyleList.setDisplay(true);
								}
								
								String skuInfo = skuMaps.get(skuid);
								String[] skuInfoArr = StringUtils.split(skuInfo, ";");
								if(skuInfoArr != null && skuInfoArr.length > 0 ){
									for(String skuItem : skuInfoArr){
										String[] skuItemArr = StringUtils.split(skuItem, ":");
										if(skuItemArr != null && skuItemArr.length > 0){
											String label = skuItemArr[0];
											String key = skuItemArr[1];
											String value = skuItemArr[2];
											if(StringUtils.equals(label, majorDimension)){
												//selectlist
												lselectlist.setStyle_id(value);
												//stylelist
												lStyleList.setGood_id(skuid);
												lStyleList.setStyle_switch_img("");
												lStyleList.setStyle_cate_id(0);
												lStyleList.setStyle_id(value);
												lStyleList.setStyle_cate_name(key);
												lStyleList.setStyle_name(value);
												
												if(!styleMap.containsKey(value)){
													l_style_list.add(lStyleList);
													styleMap.put(value, lStyleList);
												} else {
													if(lStyleList.isDisplay()){
														LStyleList tmp = styleMap.get(value);
														l_style_list.remove(tmp);
														l_style_list.add(lStyleList);
													}
												}
												
												
											} else {
												Selection selection = new Selection();
												selection.setSelect_id(0);
												selection.setSelect_name(key);
												selection.setSelect_value(value);
												selections.add(selection);
											}
										}
									}
								}
								lselectlist.setSelections(selections);
								l_selection_list.add(lselectlist);
								
								//picture process
								if(imgs.size() > 0 ){
									context.getUrl().getImages().put(skuid, imgs);
								} else {
									StringBuilder imagekey = new StringBuilder();
									for(JsonElement je : visualDimensions) {
										String dim = je.getAsJsonPrimitive().getAsString();
										for(String skuItem : skuInfoArr){
											String label = StringUtils.substringBefore(skuItem, ":");
											String value = StringUtils.substringAfterLast(skuItem, ":");
											if(StringUtils.equals(label, dim)){
												imagekey.append(value).append(" ");
											}
										}
									}
									if(imagekey.length() > 1){
										imagekey = imagekey.deleteCharAt(imagekey.length() - 1);
									}
									JsonArray imageArr = colorImages.getAsJsonArray(imagekey.toString());
									List<Image> images = Lists.newArrayList();
									if(imageArr != null && imageArr.size() > 0 ){
										for(int i = 0 ; i < imageArr.size(); i++){
											JsonObject imageObj = imageArr.get(i).getAsJsonObject();
											String large = imageObj.getAsJsonPrimitive("large").getAsString();
											System.out.println("id : "+ skuid + " , large:"+large);
											Image image = new Image(large);
											images.add(image);
										}
									}
									context.getUrl().getImages().put(skuid, images);
								}
								
							}
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				}
				rebody.setStock(new Stock(stockFlag?1:0));
				
			}
			rebody.setSku(new Sku(l_selection_list, l_style_list));
		}
		System.out.println(rebody.parseTo());
		setOutput(context, rebody.parseTo());
	}

	public List<Image> extractOneSkuImages(String content) {
		String spuImageString = StringUtils.substringBetween(content, "\"ImageBlockATF\"", "</script");
		List<Image> imgs = Lists.newArrayList();
		//List<Picture> l_image_list = Lists.newArrayList();
		if(StringUtils.isNotBlank(spuImageString)){
			String imageArrString = StringUtils.substringBetween(spuImageString, "'initial': ", "}]},") + "}]";
			if(StringUtils.isNotBlank(imageArrString)){
				try{
					JsonArray imageArr = JsonUtils.json2bean(imageArrString, JsonArray.class);
					for(JsonElement je : imageArr){
						imgs.add(new Image(je.getAsJsonObject().getAsJsonPrimitive("large").getAsString()));
						//l_image_list.add(new Picture(je.getAsJsonObject().getAsJsonPrimitive("large").getAsString()));
					}
					//context.getUrl().getImages().put(itemId, imgs);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			if(imgs.size() == 0  ){
				String imageGalleryDataString = StringUtils.substringBetween(spuImageString, "'imageGalleryData' : ", "}],");
				if(StringUtils.isNotBlank(imageGalleryDataString)){
					JsonArray imageArr = JsonUtils.json2bean(imageGalleryDataString, JsonArray.class);
					for(JsonElement je : imageArr){
						imgs.add(new Image(je.getAsJsonObject().getAsJsonPrimitive("mainUrl").getAsString()));
						//l_image_list.add(new Picture(je.getAsJsonObject().getAsJsonPrimitive("mainUrl").getAsString()));
					}
					//context.getUrl().getImages().put(itemId, imgs);
				}
			}
			//rebody.setImage(new LImageList(l_image_list));
		}
		return imgs;
	}
	
	public static Map<String, Object> getIndexHeaders(String url) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Cache-Control", "max-age=0");
		headers.put("Connection", "keep-alive");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		return headers;
	}
	
	
	
	public static void main(String[] args) throws Exception{
		Pattern pattern = Pattern.compile("((0(\\.\\d{1,10})?)|([1-9]\\d*(\\.\\d{1,10})?))");
		Matcher matcher = pattern.matcher("￥ 30123.55");
		if(matcher.find()){
			System.out.print(matcher.group(1));
		}
		//メンズ men
		//レディース women
		//ガールズ
		//ボーイズ
		//https://www.amazon.co.jp/dp/B076H4L158/
		//https://www.amazon.co.jp/dp/B076HGZQLW/
		/*String url = "https://www.amazon.co.jp/dp/B00YQU3HSW//";//"https://www.amazon.co.jp/dp/B071JWWS67/";
		AmazonJP jp = new AmazonJP();
		Context context = new Context();
		context.setUrl(new Url(url));
		jp.invoke(context);*/
			
	}
}
