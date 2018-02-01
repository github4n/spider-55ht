package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import com.haitao55.spider.crawler.core.model.xobject.XDocument;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: Nordstrom
  * @Description: Nordstrom
  * @author songsong.xu
  * @date 2016年10月19日 下午6:29:40
  *
 */
public class NordstromViaBrowser extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "shop.nordstrom.com";
	private static final String chromeDriver = System.setProperty("webdriver.chrome.driver","/usr/local/bin/chromedriver");

	@Override
	public void invoke(Context context) throws Exception {
		 String url = context.getUrl().toString();
		 ChromeOptions options = new ChromeOptions();
		 options.addArguments("no-sandbox");
		 options.addArguments("start-maximized"); 
		 options.setBinary("/usr/bin/google-chrome-stable");
		 DesiredCapabilities capabilities = new DesiredCapabilities();
		 capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		 ChromeDriver dr = new ChromeDriver(capabilities);
		 dr.manage().window().maximize();
		 dr.get(url);
		 dr.manage().addCookie(new Cookie("test", "test", ".nordstrom.com", "/", new Date()));
		 waitForComplete(dr);
		 logger.info(" url {} from nordstrom,title {}",url,dr.getTitle());
		 String content = dr.getPageSource();
		 if(StringUtils.isNotBlank(content)){
			 Document doc = JsoupUtils.parse(content);
			 XDocument xdoc = new XDocument(url, doc);
			 context.setCurrentDoc(xdoc);
		 }
		 if(dr != null){
			/*Set<Cookie> cookieSet = dr.manage().getCookies();
			for (Cookie cookie : cookieSet) {
				System.out.println(cookie.getName() + "=" + cookie.getValue()+ ";");
			}*/
			 dr.close();
			 dr.quit();
		 }
		 
		//http://shop.nordstrom.com/s/lucky-brand-hayden-stretch-skinny-jeans/4472703
		//String content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).url(context.getUrl().toString()).resultAsString();
		//String content = this.getInputString(context);// 404 throw exception offline
		String productDesktop = StringUtils.substringBetween(content, "ProductDesktop", "</script>");
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(productDesktop)){
			//String productId = StringUtils.substringBetween(productDesktop, "\"Id\":", ",");
			String title = StringUtils.substringBetween(productDesktop, "\"Name\":\"", "\",");
			String description = StringUtils.substringBetween(productDesktop, "\"Description\":\"", "\",");
			String features = StringUtils.substringBetween(productDesktop, "\"Features\":", ",\"IsBeauty\":");
			String brand = StringUtils.substringBetween(productDesktop, "\"Brand\":{\"Name\":\"", "\",");
			String gender = StringUtils.substringBetween(productDesktop, "\"Gender\":\"", "\",");
			String cat2Name = StringUtils.substringBetween(productDesktop, "\"ProductTypeName\":\"", "\",");
			String cat1Name = StringUtils.substringBetween(productDesktop, "\"ProductTypeParentName\":\"", "\",");
			String priceStr = StringUtils.substringBetween(productDesktop, "\"Price\"", "\"ValueStatement\"");
			
			String defaultColor = StringUtils.substringBetween(productDesktop, "\"DefaultColor\":\"", "\",");
			Pattern p = Pattern.compile(".*],\"IsAvailable\":(.*?),\"IsBackOrdered\":");
			Matcher m = p.matcher(productDesktop);
			String stock = StringUtils.EMPTY;
			int stockStatus = 0;
			if(m.find()){
				stock = m.group(1);
				if(StringUtils.equals(stock, "false")){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,"nordstrom.com itemUrl: "+context.getUrl().toString()+" stock is 0");
				} else {
					stockStatus = 1;
				}
			}
			String skuStr = StringUtils.substringBetween(productDesktop, "\"Skus\":", ",\"IsPythonItem\"");
			String styleMedia = StringUtils.substringBetween(productDesktop, "\"StyleMedia\":", ",\"BuyAndSave\"");
			//String defaultMedia = StringUtils.substringBetween(productDesktop, "\"DefaultMedia\":", ",\"DefaultColor\"");
			String galleryMedia = StringUtils.substringBetween(productDesktop, "\"GalleryMedia\":", ",\"LowPrice\"");
			Map<String,List<Integer>> galleryMap = new HashMap<String,List<Integer>>();
			if(StringUtils.isNotBlank(galleryMedia) && !StringUtils.equals("null", galleryMedia)){
				JsonArray arr = JsonUtils.json2bean(galleryMedia, JsonArray.class);
				if(arr != null && arr.size() > 0){
					for(int i = 0; i < arr.size() ; i++){
						JsonObject obj = arr.get(i).getAsJsonObject();
						if(obj != null){
							String colorValue = "defaultColor";
							if(!obj.get("Color").isJsonNull()){
								colorValue = obj.getAsJsonPrimitive("Color").getAsString();
							}
							JsonArray idsArr = obj.getAsJsonArray("Ids");
							List<Integer> ids = new ArrayList<Integer>();
							if(idsArr != null && idsArr.size() > 0){
								for(int j = 0; j < idsArr.size() ; j++){
									int id = idsArr.get(j).getAsJsonPrimitive().getAsInt();
									ids.add(id);
								}
							}
							galleryMap.put(colorValue, ids);
						}
					}
				}
				
			}
			Map<Integer,JsonObject> styleMap = new HashMap<Integer,JsonObject>();
			if(StringUtils.isNotBlank(styleMedia) && !StringUtils.equals("null", styleMedia)){
				JsonArray arr = JsonUtils.json2bean(styleMedia, JsonArray.class);
				if(arr != null && arr.size() > 0){
					for(int i = 0 ; i < arr.size() ; i++ ){
						JsonObject obj = arr.get(i).getAsJsonObject();
						int id = obj.getAsJsonPrimitive("Id").getAsInt();
						styleMap.put(id, obj);
					}
				}
				
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			if(StringUtils.isNotBlank(skuStr) && !StringUtils.equals(skuStr, "[]")){
				JsonArray arr = JsonUtils.json2bean(skuStr, JsonArray.class);
				if(arr != null && arr.size() > 0){
					boolean skuDisplay = true;
					for(int i = 0 ; i < arr.size() ; i++ ){
						JsonObject obj = arr.get(i).getAsJsonObject();
						int skuId= obj.getAsJsonPrimitive("Id").getAsInt();
						boolean isAvailable = obj.getAsJsonPrimitive("IsAvailable").getAsBoolean();
						String salePrice = obj.getAsJsonPrimitive("Price").getAsString();
						String origPrice = obj.getAsJsonPrimitive("DisplayOriginalPrice").getAsString();
						String color = StringUtils.EMPTY;
						if(!obj.get("Color").isJsonNull()){
							color = obj.getAsJsonPrimitive("Color").getAsString();
						}
						String size = StringUtils.EMPTY;
						if(!obj.get("Size").isJsonNull()){
							size = obj.getAsJsonPrimitive("Size").getAsString();
						}
						String width = StringUtils.EMPTY;
						if(!obj.get("Width").isJsonNull()){
							width = obj.getAsJsonPrimitive("Width").getAsString();
						}
						//String percent = obj.getAsJsonPrimitive("PercentOff").getAsString();
						//boolean isDefault = obj.getAsJsonPrimitive("IsDefault").getAsBoolean();
						if(StringUtils.isBlank(salePrice)){
							continue;
						}
						String unit = Currency.codeOf(StringUtils.substring(salePrice, 0, 1)).name();
						if(StringUtils.equals("$0.00", origPrice)){
							String spuOrigPrice = StringUtils.substringBetween(priceStr, "\"OriginalPrice\":\"", "\",");
							String isOriginalPriceRange =StringUtils.substringBetween(priceStr, "\"IsOriginalPriceRange\":", ",");
							if(StringUtils.equals("false", isOriginalPriceRange)){
								origPrice = spuOrigPrice;
							}
						}
						if(StringUtils.contains(origPrice, ",") || StringUtils.contains(origPrice, "$")){
							origPrice = replace(origPrice);
						}
						if(StringUtils.contains(salePrice, ",") || StringUtils.contains(salePrice, "$")){
							salePrice = replace(salePrice);
						}
						float orig = Float.valueOf(origPrice);
						float sale = Float.valueOf(salePrice);
						if(sale == 0){
							continue ;
						}
						if(orig == 0){
							orig = sale;
						}
						int stock_status = 0;
						int stockNum = 0;
						if(isAvailable){
							stock_status = 1;
						}
						LSelectionList lselectlist = new LSelectionList();
						lselectlist.setGoods_id(skuId+"");
						lselectlist.setOrig_price(orig);
						lselectlist.setPrice_unit(unit);
						lselectlist.setSale_price(sale);
						lselectlist.setStock_number(stockNum);
						lselectlist.setStock_status(stock_status);
						
						LStyleList lStyleList = new LStyleList();
						lStyleList.setGood_id(skuId+"");
						lStyleList.setStyle_switch_img("");
						if(!StringUtils.contains(color, "No Color")){
							lselectlist.setStyle_id(color);
							List<Selection> selections = new ArrayList<Selection>();
							if(StringUtils.isNotBlank(size)){
								Selection selection = new Selection();
								selection.setSelect_id(0);
								selection.setSelect_name("Size");
								selection.setSelect_value(size);
								selections.add(selection);
							}
							if(StringUtils.isNotBlank(width)){
								Selection selection = new Selection();
								selection.setSelect_id(0);
								selection.setSelect_name("Width");
								selection.setSelect_value(width);
								selections.add(selection);
							}
							lselectlist.setSelections(selections);
							l_selection_list.add(lselectlist);
							
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("Color");
							lStyleList.setStyle_name(color);
							lStyleList.setStyle_id(color);
							if(skuDisplay && color.equalsIgnoreCase(defaultColor)){
								lStyleList.setDisplay(true);
								setPrice(rebody, unit, orig, sale);
								skuDisplay = false;
							}
							l_style_list.add(lStyleList);
						}  else if (StringUtils.isNotBlank(size)) {
							lselectlist.setStyle_id(size);
							List<Selection> selections = new ArrayList<Selection>();
							if(StringUtils.isNotBlank(width)){
								Selection selection = new Selection();
								selection.setSelect_id(0);
								selection.setSelect_name("Width");
								selection.setSelect_value(width);
								selections.add(selection);
							}
							lselectlist.setSelections(selections);
							l_selection_list.add(lselectlist);
							
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("Size");
							lStyleList.setStyle_name(size);
							lStyleList.setStyle_id(size);
							if(skuDisplay){
								lStyleList.setDisplay(true);
								setPrice(rebody, unit, orig, sale);
								skuDisplay = false;
							}
							l_style_list.add(lStyleList);
							
						} else {
							setPrice(rebody, unit, orig, sale);
						}
						if(StringUtils.contains(color, "No Color")){
							color = "defaultColor";
						}
						List<Integer> ids = galleryMap.get(color);
						List<Image> images = new ArrayList<Image>();
						getPics(styleMap, ids, images);
						if( images.size() == 0 ){
							color = "defaultColor";
							ids = galleryMap.get(color);
							getPics(styleMap, ids, images);
						}
						context.getUrl().getImages().put(skuId+"", images);
					}
				}
			} else {
				float orig = 0;
				float sale = 0;
				int save = 0;
				String unit = StringUtils.EMPTY;
				if(StringUtils.isNotBlank(priceStr)){
					String origPrice = StringUtils.substringBetween(priceStr, "\"OriginalPrice\":\"", "\",");
					String salePrice = StringUtils.substringBetween(priceStr, "\"CurrentPrice\":\"", "\",");
					if(StringUtils.isBlank(salePrice)){
						logger.error("Error while fetching url {} and price is null",url);
						throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while fetching salePrice with url {} from nordstrom");
					}
					unit = Currency.codeOf(StringUtils.substring(salePrice, 0, 1)).name();
					if(StringUtils.isBlank(origPrice)){
						origPrice = salePrice;
					}
					if(StringUtils.contains(origPrice, ",") || StringUtils.contains(origPrice, "$")){
						origPrice = replace(origPrice);
					}
					if(StringUtils.contains(salePrice, ",") || StringUtils.contains(salePrice, "$")){
						salePrice = replace(salePrice);
					}
					orig = Float.valueOf(origPrice);
					sale = Float.valueOf(salePrice);
					String percent = StringUtils.substringBetween(priceStr, "\"PercentageOff\":\"", "\",");
					if(StringUtils.isNotBlank(percent)){
						percent = percent.replace("%", "");
						save = Integer.valueOf(percent);
					}
					rebody.setPrice(new Price(orig, save, sale, unit));
				}
			}
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			// full doc info
			String docid = SpiderStringUtil.md5Encode(url);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(SpiderStringUtil.replaceHtmlToSpace(title), ""));
			
			// stock
			rebody.setStock(new Stock(stockStatus));
			// images l_image_list
			// rebody.setImage(new LImageList(pics));
			// brand
			rebody.setBrand(new Brand(SpiderStringUtil.replaceHtmlToSpace(brand), "", "", ""));
			// Category
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			cats.add(cat1Name);
			cats.add(cat2Name);
			rebody.setCategory(cats);
			// BreadCrumb
			breads.add(cat1Name);
			breads.add(cat2Name);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			if(StringUtils.isNotBlank(features)){
				Type typeList = new TypeToken<List<String>>(){}.getType();
				List<String> list = JsonUtils.json2bean(features, typeList);
				if(list != null){
					int count = 1;
					for(String feature : list){
						featureMap.put("feature-" + count, feature);
						count++;
					}
				}
			}
			rebody.setFeatureList(featureMap);
			// description
			Map<String, Object> descMap = new HashMap<String, Object>();
			descMap.put("en", SpiderStringUtil.replaceHtmlToSpace(description));
			rebody.setDescription(descMap);

			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		} else {
			logger.error("Error while fetching url {} from nordstrom",url);
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while fetching ProductDesktop with url {} from nordstrom");
		}
		setOutput(context, rebody);
		//System.out.println(rebody.parseTo());
	}

	private void getPics(Map<Integer, JsonObject> styleMap, List<Integer> ids,
			List<Image> images) {
		if(ids != null && ids.size() > 0){
			for(Integer id : ids){
				JsonObject jsonObject = styleMap.get(id);
				if(jsonObject != null){
					String origImage = jsonObject.getAsJsonObject("ImageMediaUri").getAsJsonPrimitive("Large").getAsString();
					//boolean isDef = jsonObject.getAsJsonPrimitive("IsDefault").getAsBoolean();
					//int colorId = jsonObject.getAsJsonPrimitive("ColorId").getAsInt();
					images.add(new Image(origImage));
				}
			}
		}
	}
	
	private int waitForComplete(WebDriver webDriver) {
		String stateJs = " return document.readyState; ";
		JavascriptExecutor js = ((JavascriptExecutor) webDriver);
		String onload = js.executeScript(stateJs).toString();
		int ii = 10;
		int statu = 0;
		while (!"complete".equalsIgnoreCase(onload) && ii >= 0) {
			onload = js.executeScript(stateJs).toString();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("firefox url >>>" + webDriver.getCurrentUrl()+ " >>>statu>>>>>>>>>>>" + onload);
			ii--;
			// 浏览器如果超过时间限制就刷新,webDriver.navigate().refresh();
			if (ii % 5 == 0) {// 刷新浏览器
				webDriver.navigate().refresh();
			}
			if (ii == 0) {
				statu = -1;
			}
		}
		try {
			statu = 1;
		} catch (Exception e) {
			e.printStackTrace();
			return statu;
		}
		return statu;
	}

	private void setPrice(RetBody rebody, String unit, float orig, float sale) {
		BigDecimal a = new BigDecimal(sale/orig);
		BigDecimal roundOff = a.setScale(1, BigDecimal.ROUND_HALF_EVEN);
		int save = Math.round((1 - roundOff.floatValue()) * 100);
		rebody.setPrice(new Price(orig, save, sale, unit));
	}
	
	private String replace(String dest){
		if(StringUtils.isBlank(dest)){
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$|,", ""));
	}
	
	public static void main(String[] args) throws Exception  {
		NordstromViaBrowser n = new NordstromViaBrowser();
		Context context = new Context();
		//http://shop.nordstrom.com/s/clinique-dramatically-different-moisturizing-lotion-travel-size/3523265
		Url url = new Url("http://shop.nordstrom.com/s/bobbi-brown-skin-moisture-compact-foundation/4329773");
		context.setUrl(url);
		n.invoke(context);
		
		/*String url = "http://shop.nordstrom.com/s/clinique-dramatically-different-moisturizing-lotion-collection-limited-edition-nordstrom-exclusive-73-value/4531555";
		
		String html = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).url(url).resultAsString();
		Pattern p = Pattern.compile("}],\"IsAvailable\":(.*?),\"IsBackOrdered\":");
		Matcher m = p.matcher(html);
		if(m.find()){
			System.out.println( m.group());
		}*/
		
	}

}
