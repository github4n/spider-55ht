package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: Nordstrom
  * @Description: Nordstrom
  * @author songsong.xu
  * @date 2016年10月19日 下午6:29:40
  *
 */
public class Nordstrom extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "shop.nordstrom.com";

	@Override
	public void invoke(Context context) throws Exception {
		//http://shop.nordstrom.com/s/lucky-brand-hayden-stretch-skinny-jeans/4472703
		String sourceUrl = context.getUrl().getValue();
		String url = DetailUrlCleaningTool.getInstance().cleanDetailUrl(sourceUrl);
		if(StringUtils.isBlank(url)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"nordstrom.com itemUrl: "+sourceUrl+" ,  url rule is error.");
		}
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-insecure-requests", "1");
		headers.put("Connection", "keep-alive");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		headers.put("Cache-Control", "max-age=0");
		boolean isRunInRealTime = context.isRunInRealTime();
		String content = StringUtils.EMPTY;
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US","55ht_zone_us");
			content = luminatiHttpClient.request(url,headers);
			context.setHtmlPageSource(content);
		}else{
			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();		
			if (proxyRegionId != null) {
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String ip = proxy.getIp();
				int port = proxy.getPort();
				content = Crawler.create().timeOut(15000).retry(2).proxy(true).proxyAddress(ip).proxyPort(port).url(url).header(headers).resultAsString();
			} else {
				content = Crawler.create().timeOut(15000).url(url).retry(2).header(headers).resultAsString();
			}
		}

		String productDesktop = StringUtils.substringBetween(content, "ProductDesktop", "</script>");
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(productDesktop)){
			//String productId = StringUtils.substringBetween(productDesktop, "\"Id\":", ",");
			String title = StringUtils.substringBetween(productDesktop, "\"Name\":\"", "\",");
			String description = StringUtils.substringBetween(productDesktop, "\"Description\":\"", "\",");
			String features = StringUtils.substringBetween(productDesktop, "\"Features\":", ",\"Ingredients\"");
			String brandStr = StringUtils.substringBetween(productDesktop, "\"Brand\":", ",\"ImsProductTypeId\":");
			String brand = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(brandStr)){
				brand = StringUtils.substringBetween(brandStr, "\"Name\":\"", "\",");
			}
			String gender = StringUtils.substringBetween(productDesktop, "\"Gender\":\"", "\",");
			String cat2Name = StringUtils.substringBetween(productDesktop, "\"ProductTypeName\":\"", "\",");
			String cat1Name = StringUtils.substringBetween(productDesktop, "\"ProductTypeParentName\":\"", "\",");
			String priceStr = StringUtils.substringBetween(productDesktop, "\"Price\"", "\"ValueStatement\"");
			
			String defaultColor = StringUtils.substringBetween(productDesktop, "\"DefaultColor\":\"", "\",");
			Pattern p = Pattern.compile("\"IsAvailable\":(.*?),\"IsInStoreOnlyBridal\":");
			Matcher m = p.matcher(productDesktop);
			String stock = StringUtils.EMPTY;
			int stockStatus = 0;
			if(m.find()){
				stock = m.group(1);
				if(StringUtils.equals(stock, "false")){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,"nordstrom.com itemUrl: "+ url +" stock is 0");
				} else {
					stockStatus = 1;
				}
			}
			String skuStr = StringUtils.substringBetween(productDesktop, "\"Skus\":", ",\"IsAvailableForPreOrder\"");
			String styleMedia = StringUtils.substringBetween(productDesktop, "\"StyleMedia\":", ",\"GalleryMedia\"");
			//String defaultMedia = StringUtils.substringBetween(productDesktop, "\"DefaultMedia\":", ",\"DefaultColor\"");
			String galleryMedia = StringUtils.substringBetween(productDesktop, "\"GalleryMedia\":", ",\"LowPrice\"");
			Map<String,List<Integer>> galleryMap = new HashMap<String,List<Integer>>();
			if(StringUtils.isNotBlank(galleryMedia) && !StringUtils.equals("null", galleryMedia)){
				JsonArray arr = null;
				try{
					arr = JsonUtils.json2bean(galleryMedia, JsonArray.class);
				}catch(Exception e){
					//e.printStackTrace();
					//System.out.println(galleryMedia);
					galleryMedia = StringUtils.substringBetween(productDesktop, "\"GalleryMedia\":", ",\"AgeGroups\"");
					arr = JsonUtils.json2bean(galleryMedia, JsonArray.class);
				}
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
			Set<String> set = new HashSet<String>();
			if(StringUtils.isNotBlank(skuStr) && !StringUtils.equals(skuStr, "[]")){
				JsonArray arr = JsonUtils.json2bean(skuStr, JsonArray.class);
				if(arr != null && arr.size() > 0){
					boolean skuDisplay = true;
					for(int i = 0 ; i < arr.size() ; i++ ){
						JsonObject obj = arr.get(i).getAsJsonObject();
						int skuId= obj.getAsJsonPrimitive("Id").getAsInt();
						boolean isAvailable = obj.getAsJsonPrimitive("IsAvailable").getAsBoolean();
						String promoPrice = "";
						
						if(obj != null && obj.has("PromoPrice") &&  !obj.get("PromoPrice").isJsonNull()){
							promoPrice = obj.getAsJsonPrimitive("PromoPrice").getAsString(); //活動
						}
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
						logger.info("www.nordstrom.com url {},saleprice ##{}##",url,salePrice);
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
						
						if(StringUtils.contains(promoPrice, ",") || StringUtils.contains(promoPrice, "$")){
							salePrice = replace(promoPrice);
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
						
						if(StringUtils.contains(color, "No Color")){
                            color = "defaultColor";
                        }
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
                        
                        if(!set.contains(color)){
                            LStyleList lStyleList = new LStyleList();
                            lStyleList.setGood_id(skuId+"");
                            lStyleList.setStyle_switch_img("");
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
                            set.add(color);
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
			String url_no = docid;
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
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
			if(StringUtils.isNotBlank(features) && !StringUtils.equals(features, "null")){
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
		Nordstrom n = new Nordstrom();
		Context context = new Context();
		context.setRunInRealTime(true);
		//http://shop.nordstrom.com/s/clinique-dramatically-different-moisturizing-lotion-travel-size/3523265
		Url url = new Url("https://shop.nordstrom.com/s/estee-lauder-party-glamour-collection-purchase-with-any-estee-lauder-purchase/4810515");
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
