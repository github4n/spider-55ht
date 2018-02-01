package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
  * @ClassName: AmazonUS
  * @Description: 美國亞馬遜的解析處理器
  * @author songsong.xu
  * @date 2016年9月28日 上午9:37:46
  *
 */
public class CopyOfAmazonUS extends AbstractSelect{
	
	private static final String ITEM_URL_TEMPLATE = "https://www.amazon.com/dp/#itemId#/";
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String HTTPS = "https://";
	private static final String DOMAIN = "www.amazon.com";
	private static final String HOST   = HTTPS + DOMAIN;
	private static final String SEX_WOMEN = "women";
	private static final String SEX_MEN = "men";
	private static final String SUFFIX = "&psc=1&asinList=#skuId#&isFlushing=2&dpEnvironment=softlines&id=#skuId#&mType=full";

	@Override
	public void invoke(Context context) throws Exception {
		try{
			String content = super.getInputString(context);
			RetBody rebody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				String skuData = StringUtils.substringBetween(content, "window.DetailPage", "</script>");
				Sku sku = new Sku();
				String itemUrl = StringUtils.EMPTY;
				Map<String,SkuBean> skuResult = null;
				String current_asin =  null;
				if(StringUtils.isNotBlank(skuData)){
					String parent_asin = StringUtils.substringBetween(skuData, "\"parent_asin\":\"", "\",");
					if(StringUtils.isNotBlank(parent_asin)){
						itemUrl = ITEM_URL_TEMPLATE.replace("#itemId#", parent_asin);
					}
					String num_variation_dimensions = StringUtils.substringBetween(skuData, "\"num_variation_dimensions\":", ",");
					String stockUrl = StringUtils.substringBetween(skuData, "\"immutableURLPrefix\":\"", "\",");
					//String unselectedDimCount = StringUtils.substringBetween(skuData, "\"unselectedDimCount\":", ",");
					current_asin = StringUtils.substringBetween(skuData, "\"current_asin\":\"", "\",");
					//String selected_variation_values = StringUtils.substringBetween(skuData, "\"selected_variation_values\":", ",\"unselectedDimCount\"");
					String dimensions = StringUtils.substringBetween(skuData, "\"dimensions\":", ",\"prioritizeReqPrefetch\"");
					//String dimToAsinMapData = StringUtils.substringBetween(skuData, "\"dimToAsinMapData\":", ",");
					//String selected_variations = StringUtils.substringBetween(skuData, "\"selected_variations\":", ",\"jqupgrade\"");
					//String dimensionValuesDisplayData = StringUtils.substringBetween(skuData, "\"dimensionValuesDisplayData\":", ",");
					String variation_values = StringUtils.substringBetween(skuData, "\"variation_values\":", ",\"deviceType\"");
					//String dimensionsDisplay = StringUtils.substringBetween(skuData, "\"dimensionsDisplay\":", ",");
					String variationDisplayLabels = StringUtils.substringBetween(skuData, "\"variationDisplayLabels\":", ",\"twisterInitPrefetchMode\"");
					//String dimensionValuesData = StringUtils.substringBetween(skuData, "\"dimensionValuesData\":", ",\"reactId\"");
					String asin_variation_values = StringUtils.substringBetween(skuData, "\"asin_variation_values\":", ",\"contextMetaData\"");
					//String asinToDimIndexMapData = StringUtils.substringBetween(dataToReturn, "\"asinToDimIndexMapData\":", ",");
					
					String imageData = StringUtils.substringBetween(content, "\"indexToColor\"", "data[\"heroImage\"]");
					String images = StringUtils.EMPTY;
					String visualDimensions = StringUtils.EMPTY;
					if(StringUtils.isNotBlank(imageData)){
						visualDimensions = StringUtils.substringBetween(imageData, "\"visualDimensions\":", ",\"productGroupID\"");
						images = StringUtils.substringBetween(imageData, "data[\"colorImages\"] = ", ";");
					}
					
					
					Type typeList = new TypeToken<List<String>>(){}.getType();
					List<String> dimensionsList = JsonUtils.json2bean(dimensions, typeList);
					List<String> visualDimensionsList =  JsonUtils.json2bean(visualDimensions, typeList);
					
					Type typeMap = new TypeToken<Map<String,String>>(){}.getType();
					//Type type = new TypeToken<Map<String,Integer>>(){}.getType();
					//Map<String,Integer> selected_variation_valuesMap = JsonUtils.json2bean(selected_variation_values, type);
					
					
					Type typeMapList = new TypeToken<Map<String,List<String>>>(){}.getType();
					Map<String,List<String>> variation_valuesMapList = JsonUtils.json2bean(variation_values, typeMapList);
					
					Map<String,String> variationDisplayLabelsMap = JsonUtils.json2bean(variationDisplayLabels, typeMap);
					
					Type typeMapMap = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
					Map<String,Map<String,String>> asin_variation_valuesMapMap =  JsonUtils.json2bean(asin_variation_values, typeMapMap);
					
					
					List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
					List<LStyleList> l_style_list = new ArrayList<LStyleList>();
					Map<String,Url> skuUrls = new HashMap<String,Url>();
					if(asin_variation_valuesMapMap != null && asin_variation_valuesMapMap.size() > 0 ){
						for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
							String skuId = entry.getKey();
							skuUrls.put(skuId, new Url(HOST+stockUrl+SUFFIX.replace("#skuId#", skuId)));
						}
						//price stock
						skuResult = new AmazonPriceStockHandler().process(skuUrls);
						if(skuResult == null || skuResult.size() == 0 ){//無自營商品 或者失敗
							return;
						}
						for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
							String skuId = entry.getKey();
							SkuBean skuBean = skuResult.get(skuId);
							Map<String,String> valueMap = entry.getValue();
							//selectlist
							LSelectionList lselectlist = new LSelectionList();
							lselectlist.setGoods_id(skuId);
							if(skuBean != null){
								lselectlist.setOrig_price(Float.valueOf(skuBean.getOrig()));
								lselectlist.setPrice_unit(skuBean.getUnit());
								lselectlist.setSale_price(Float.valueOf(skuBean.getSale()));
								lselectlist.setStock_number(skuBean.getStockNum());
								lselectlist.setStock_status(skuBean.getStockStatus());
							} else {
								logger.info("sku price and stock is invalid,url {} skuId {}",context.getCurrentUrl(),skuId);
								continue;//無價格庫存 丟棄
							}
							List<Selection> selections = new ArrayList<Selection>();
							//stylelist
							LStyleList lStyleList = new LStyleList();
							
							//dimensions
							boolean colorFlag = false;
							boolean major = true;
							if(StringUtils.containsIgnoreCase(dimensions, "color")){
								colorFlag = true;
								major = false;
							}
							
							int numDim = Integer.valueOf(num_variation_dimensions);
							for(String dim : dimensionsList){
								//索引
								String index = valueMap.get(dim);
								//select labels
								String label = variationDisplayLabelsMap.get(dim);
								
								//select values
								List<String> variations = variation_valuesMapList.get(dim);
								String value = variations.get(Integer.valueOf(index));
								
								if(numDim > 1){
									
									if((colorFlag && StringUtils.containsIgnoreCase(dim, "color")) || major){
										//selectlist
										lselectlist.setStyle_id(value);
										//stylelist
										lStyleList.setGood_id(skuId);
										lStyleList.setStyle_switch_img("");
										lStyleList.setStyle_cate_id(0);
										lStyleList.setStyle_id(value);
										lStyleList.setDisplay(true);
										lStyleList.setStyle_cate_name(label);
										lStyleList.setStyle_name(value);
										l_style_list.add(lStyleList);
										colorFlag = false;
										major = false;
										continue;
									}
									Selection selection = new Selection();
									selection.setSelect_id(0);
									selection.setSelect_name(label);
									selection.setSelect_value(value);
									selections.add(selection);
								} else {
									//selectlist
									lselectlist.setStyle_id(value);
									//stylelist
									lStyleList.setGood_id(skuId);
									lStyleList.setStyle_switch_img("");
									lStyleList.setStyle_cate_id(0);
									lStyleList.setStyle_id(value);
									lStyleList.setDisplay(false);
									lStyleList.setStyle_cate_name(label);
									lStyleList.setStyle_name(value);
									l_style_list.add(lStyleList);
								}
							}
							// picture download
							StringBuilder imagekey = new StringBuilder();
							for(String imageKey : visualDimensionsList){
								String index = valueMap.get(imageKey);
								List<String> variations = variation_valuesMapList.get(imageKey);
								String value = variations.get(Integer.valueOf(index));
								imagekey.append(value).append(" ");
							}
							imagekey.delete(imagekey.length()-1, imagekey.length());
							List<Image> picsPerSku = getPicsByStyleId(images, StringUtils.trim(imagekey.toString()));
							context.getUrl().getImages().put(skuId, picsPerSku);
							
							lselectlist.setSelections(selections);
							l_selection_list.add(lselectlist);
						}
					}
					sku.setL_selection_list(l_selection_list);
					sku.setL_style_list(l_style_list);
					
				} 
				//無sku的情況
				else {
					Map<String,Url> skuUrls = new HashMap<String,Url>();
					String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
					skuUrls.put(skuId, new Url(context.getCurrentUrl()));
					skuResult = new AmazonPriceStockHandler().process(skuUrls);
					if(skuResult == null || skuResult.size() == 0 ){//無自營商品 或者失敗
						return;
					}
					String imageData = StringUtils.substringBetween(content, "'colorImages':", "</script>");
					String imagesStr = StringUtils.EMPTY;
					if(StringUtils.isNotBlank(imageData)){
						imagesStr = StringUtils.substringBetween(imageData, "'initial': ", "'colorToAsin':");
						if(StringUtils.isNotBlank(imagesStr)){
							imagesStr = StringUtils.substringBeforeLast(imagesStr, ",");
						}
						List<Image> picsPerSku = getPics(imagesStr);
						context.getUrl().getImages().put(skuId, picsPerSku);
					}
					
					
				}
				
				Document document = Jsoup.parse(content);
				Elements es = document.select("a#brand");
				String brand = StringUtils.EMPTY;
				if(es != null && es.size() > 0){
					String href = es.get(0).attr("href");
					brand = StringUtils.substringBetween(href, "/", "/");
					if(StringUtils.isNotBlank(brand) && StringUtils.contains(brand, "-")){
						brand = brand.replace("-", " ");
					}
				}
				
				// full doc info
				String docid = SpiderStringUtil.md5Encode(itemUrl);
				rebody.setDOCID(docid);
				rebody.setSite(new Site(DOMAIN));
				rebody.setProdUrl(new ProdUrl(itemUrl, System.currentTimeMillis(), docid));
				
				//title
				es = document.select("span#productTitle");
				String title = getText(es);
				rebody.setTitle(new Title(title, ""));
				
				// price stock
				if(StringUtils.isNotBlank(current_asin) && skuResult != null){
					SkuBean skuBean = skuResult.get(current_asin);
					if(skuBean != null){
						rebody.setPrice(new Price(Float.valueOf(skuBean.getOrig()), Integer.valueOf(skuBean.getSave()), Float.valueOf(skuBean.getSale()), skuBean.getUnit()));
						rebody.setStock(new Stock(skuBean.getStockStatus()));
					}
				}
				// brand
				String gender = StringUtils.EMPTY;
				rebody.setBrand(new Brand(brand, ""));
				// Category
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				es = document.select("div#wayfinding-breadcrumbs_feature_div > ul > li > span > a");
				if(es != null && es.size() > 0){
					for(Element e : es){
						String cat = e.text();
						cats.add(cat);
						breads.add(cat);
						gender = getSex(cat);
					}
				}
				rebody.setCategory(cats);
				// BreadCrumb
				rebody.setBreadCrumb(breads);
				// description
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				es = document.select("div#productDescription > p");
				StringBuilder sb = new StringBuilder();
				if (es != null && es.size() > 0) {
					int count = 1;
					for (Element e : es) {
						featureMap.put("feature-" + count, e.text());
						count++;
						sb.append(e.text());
					}
				}
				//FeatureList
				rebody.setFeatureList(featureMap);
				descMap.put("en", sb.toString());
				rebody.setDescription(descMap);
				//Properties
				Map<String, Object> propMap = new HashMap<String, Object>();
				if(StringUtils.isBlank(gender)){
					gender = getSex(title);
				}
				propMap.put("s_gender", gender);
				es = document.select("div#detailBullets_feature_div > ul > li > span");
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
				//Sku
				rebody.setSku(sku);
			}
			setOutput(context, rebody.parseTo());
			System.out.println(JsonUtils.bean2json(rebody));
		} catch (Throwable e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
	
	public String getText(Elements es){
		if(es != null && es.size() > 0){
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}
	
	public String getAttr(Elements es,String attrKey){
		if(es != null && es.size() > 0){
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}
	
	private List<Image> getPics(String content) {
		List<Image> images = new ArrayList<Image>();
		JsonArray arr  = JsonUtils.json2bean(content, JsonObject.class);
		if(arr != null && arr.size() > 0){
			boolean thumb = true;
			for(int i =0 ; i < arr.size(); i++){
				JsonObject jsonObject = arr.get(i).getAsJsonObject();
				Image image = new Image(StringUtils.replace(jsonObject.get("large").toString(), "\"", ""));
				images.add(image);
				if(thumb){
					image = new Image(StringUtils.replace(jsonObject.get("thumb").toString(), "\"", ""));
					images.add(image);
					thumb = false;
				}
			}
		}
		return images;
	}

	private List<Image> getPicsByStyleId(String content, String imagekey) {
		List<Image> images = new ArrayList<Image>();
		JsonObject obj = JsonUtils.json2bean(content, JsonObject.class);
		if(obj != null){
			JsonArray arr = obj.getAsJsonArray(imagekey);
			if(arr != null && arr.size() > 0){
				boolean thumb = true;
				for(int i =0 ; i < arr.size(); i++){
					JsonObject jsonObject = arr.get(i).getAsJsonObject();
					Image image = new Image(StringUtils.replace(jsonObject.get("large").toString(), "\"", ""));
					images.add(image);
					if(thumb){
						image = new Image(StringUtils.replace(jsonObject.get("thumb").toString(), "\"", ""));
						images.add(image);
						thumb = false;
					}
				}
			}
		}
		return images;
	}

	private String getSex(String cat) {
		String gender;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = SEX_WOMEN;
		} else {
			gender = SEX_MEN;
		}
		return gender;
	}
	
	public static void main(String[] args) {
		String content = HttpUtils.get("https://www.amazon.com/Adrianna-Papell-Womens-Floral-Sleeves/dp/B01KCJV7W8/ref=lp_1045024_1_1?s=apparel&ie=UTF8&qid=1475028221&sr=1-1&nodeID=1045024&th=1&psc=1");
		Document document = Jsoup.parse(content);
		//div#detailBullets_feature_div > ul > li > span
		Elements es = document.select("div#detailBullets_feature_div > ul > li > span");
		if(es != null && es.size() > 0){
			for(Element e : es){
				System.out.println(e.text());
			}
		}
	}
	

}
