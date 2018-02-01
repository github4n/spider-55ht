package com.haitao55.spider.crawler.core.callable.custom.victoriassecret;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: VictoriasSecret
  * @Description: 维多利亚的秘密 详情信息页面
  * @author songsong.xu
  * @date 2016年11月8日 下午8:01:56
  *
 */
public class VictoriasSecret extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.victoriassecret.com";

	@Override
	public void invoke(Context context) throws Exception {
	    DetailUrlCleaningTool tool = DetailUrlCleaningTool.getInstance();
		Map<String,Object> headers = new HashMap<String,Object>();
		//headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; BIDUBrowser 2.x");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Accept-Language", "en-US,en;q=0.9");
		headers.put("Connection", "keep-alive");
		headers.put("Host", "www.victoriassecret.com");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
		headers.put("Cookie", "UID=29cc77b3-f0de-4260-8b9d-0c16e51212c7; vsSigninPrompt=true; lang=en-us; cmTPSet=Y; xdVisitorId=10859TPA6shQb-ikWZ7bTLtzP7mK3sH8NVdEoSHR2jRf83U1B29; AOS-5634ad978e4e59c8-5555e0e29d99f067043825d1186e38a4=b17e0944ba79a5ab758f0f2e36f123aefbd68cadd98fa1e37920c84b3f0a8e4a; VSSESSION=LdmNZaAQbnVI63Gi85cCIQDs2rgaZmTaLiuCpKKW079cq; X-Mapping-omgbnpna=D9C3D230B96362B0B053F454E4EC45A1; dcc2=LdmNZaAQb; RES_TRACKINGID=780819102675189; ResonanceSegment=1; RES_SESSIONID=503140629770778; vsPopUnder=true; AOS-03e176e6caccea8f-0e93b5d31d61f17d2dfdb31ef4832639=9486ad838f064a79efa7722e57c42c96dcd33facd907f7d7f17e971248ee1062; _ga=GA1.2.1508828197.1511492697; _gid=GA1.2.1209879388.1511840796; _uetsid=_uet705f53f7; utag_main=v_id:015febfc1e730013f2e718aae21504068008c06000bd0$_sn:7$_ss:0$_st:1511843545066$_pn:6%3Bexp-session$ses_id:1511840795955%3Bexp-session; atgRecVisitorId=10859TPA6shQb-ikWZ7bTLtzP7mK3sH8NVdEoSHR2jRf83U1B29; atgRecSessionId=6IEAvAPFGWE7DGwPHZ_AUqBHYUxQWjLIeMoUpWxtZd-nZDWUp72y!123506724!1120247709; dcc=Thebe;");
		/*String content = this.getInputString(context);*/
		String url = tool.cleanDetailUrl(context.getUrl().getValue());
		if(StringUtils.isBlank(url)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"victoriassecret.com itemUrl:"+context.getUrl().getValue()+" is null");
		}
		String content = Crawler.create().timeOut(30000).header(headers).retry(3).url(url).resultAsString();
		RetBody rebody = new RetBody();
		String productAssetId = StringUtils.substringBetween(content, "\"product_assetId\":", ",\"view\"");
		String assetId = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(productAssetId)){
			JsonArray arr = JsonUtils.json2bean(productAssetId, JsonArray.class);
			if(arr != null && arr.size() > 0){
				assetId = arr.get(0).getAsString();
			}
		}
		if(StringUtils.isBlank(assetId)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"victoriassecret.com itemUrl:"+url+" , assetId is null");
		}
		String product_name = StringUtils.substringBetween(content, "\"product_name\":", ",\"product_type\"");
		String title = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(product_name)){
			JsonArray arr = JsonUtils.json2bean(product_name, JsonArray.class);
			if(arr != null && arr.size() > 0){
				title = arr.get(0).getAsString();
			}
		}
		String altImages = StringUtils.substringBetween(content, "UU.save(\"pagedata.altImages\", ", ");");
		String selectors = StringUtils.substringBetween(content, "UU.save(\"pagedata.selectors\",", ");");
		String itemPrice = StringUtils.substringBetween(content, "UU.save(\"pagedata.itemPrice\",", ");");
		String atp = StringUtils.substringBetween(content, "UU.save(\"pagedata.atp\",", ");");
		
		//sku selects
		String defaultColor = StringUtils.EMPTY;
		boolean defaultColorDisplay = false;
		List<String> skuList = new ArrayList<String>();
		if(StringUtils.isNotBlank(selectors)){
			JsonObject selectorsObj = JsonUtils.json2bean(selectors, JsonObject.class);
			JsonObject currentObj = selectorsObj.getAsJsonObject(assetId);
			
			if(currentObj != null && currentObj.has("selected") && !currentObj.get("selected").isJsonNull()){
				JsonObject selected = currentObj.getAsJsonObject("selected");
				if(selected != null && !selected.isJsonNull() && selected.has("color")){
					defaultColor = selected.getAsJsonPrimitive("color").getAsString();
					defaultColorDisplay = true;
				}
			}
			String swatch = "http://dm.victoriassecret.com/p/s/125x125/#swatch#.jpg";
			if(currentObj != null){
				List<Image> imageList = new ArrayList<Image>();
				for(Map.Entry<String,JsonElement> entry : currentObj.entrySet()){
					String key = entry.getKey();
					JsonElement jsonElement = entry.getValue();
					if(jsonElement != null && jsonElement.isJsonObject()){
						JsonObject jsonObject = jsonElement.getAsJsonObject();
						if(jsonObject.has("label")){
							List<String> selecter = new ArrayList<String>();
							String keylabel = jsonObject.getAsJsonPrimitive("label").getAsString();
							boolean imageBased = jsonObject.getAsJsonPrimitive("imageBased").getAsBoolean();
							if(jsonObject != null && jsonObject.has("options") && !jsonObject.getAsJsonArray("options").isJsonNull()){
								JsonArray arr = jsonObject.getAsJsonArray("options");
								for(int i = 0; i < arr.size() ; i++){
									JsonObject obj = arr.get(i).getAsJsonObject();
									String value = obj.getAsJsonPrimitive("value").getAsString();
									String valueLabel = obj.getAsJsonPrimitive("label").getAsString();
									if(!imageBased && StringUtils.equals("color", key)){
										imageBased = true;
									}
									if(!defaultColorDisplay){
										defaultColor = value;
										defaultColorDisplay = true;
									}
									if(imageBased){
										String swatchUrl = StringUtils.EMPTY;
										if(obj.has("image")){
											String image = obj.getAsJsonPrimitive("image").getAsString();
											swatchUrl = swatch.replace("#swatch#", image);
											imageList.add(new Image(swatchUrl));
										}
										selecter.add(value+";"+keylabel+";"+valueLabel+";"+swatchUrl+";"+imageBased);
									} else {
										selecter.add(value+";"+keylabel+";"+valueLabel+";"+imageBased);
									}
								}
							}
							//自由组合获取所有sku项
							skuList = combinate(skuList,selecter);
						}
					}
				}
				context.getUrl().getImages().put(System.currentTimeMillis()+"", imageList);//thumb picture download
				
			} else {
				logger.error("get selectors {} from victoriassecret's url {}", selectors,url);
			}
		}
		//logger.info("get skuList {} from victoriassecret's url {}", Arrays.toString(skuList.toArray()),url);
		//image
		if(StringUtils.isNotBlank(altImages)){
			String pUrl = "http://dm.victoriassecret.com/p/504x672/#image#.jpg";
			String productUrl = "http://dm.victoriassecret.com/product/504x672/#image#.jpg";
			JsonObject altImagesObj = JsonUtils.json2bean(altImages, JsonObject.class);
			JsonObject currentObj = altImagesObj.getAsJsonObject(assetId);
			if(currentObj != null){
				for(Map.Entry<String,JsonElement> entry : currentObj.entrySet()){
					String key = entry.getKey();
					JsonElement jsonElement = entry.getValue();
					List<Image> images = new ArrayList<Image>();
					if(jsonElement != null && jsonElement.isJsonArray()){
						JsonArray arr = jsonElement.getAsJsonArray();
						for(int i=0; i < arr.size();i++){
							JsonObject obj = arr.get(i).getAsJsonObject();
							String image = obj.getAsJsonPrimitive("image").getAsString();
							if(StringUtils.contains(image, "tif/")){
							    images.add(new Image(pUrl.replace("#image#", image)));
							} else {
							    images.add(new Image(productUrl.replace("#image#", image)));
							}
						}
					}
					context.getUrl().getImages().put(key , images);// large picture download
				}
			} else {
				logger.error("get altImages {} from victoriassecret's url {}", altImages,url);
			}
		}
		//price
		Map<String,String> priceMap = new HashMap<String,String>();
		List<String> tmpSizeList = new ArrayList<String>();
		if(StringUtils.isNotBlank(itemPrice)){
			JsonArray itemPriceArr = JsonUtils.json2bean(itemPrice, JsonArray.class);
			if(itemPriceArr != null && itemPriceArr.size() > 0){
				for(int i =0; i < itemPriceArr.size();i++){
					JsonObject obj = itemPriceArr.get(i).getAsJsonObject();
					String asset_id = obj.getAsJsonPrimitive("assetId").getAsString();
					if(obj != null && !obj.isJsonNull() && StringUtils.equals(assetId, asset_id)){
						float salePrice = obj.getAsJsonPrimitive("salePrice").getAsFloat();
						BigDecimal bigDecimal = new BigDecimal(salePrice);
						float sale = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						float origPrice = obj.getAsJsonPrimitive("origPrice").getAsFloat();
						bigDecimal = new BigDecimal(origPrice);
						float orig = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						String unit = obj.getAsJsonPrimitive("currencyCode").getAsString();
						String itemNbr = obj.getAsJsonPrimitive("itemNbr").getAsString();
						JsonArray sizeCode = obj.getAsJsonArray("sizeCode");
						List<String> sizelist = new ArrayList<String>();
						if(sizeCode != null && sizeCode.size() > 0){
							for(int j = 0 ; j < sizeCode.size(); j++ ){
								String size = sizeCode.get(j).getAsJsonPrimitive().getAsString();
								sizelist.add(size);
							}
						}
						tmpSizeList.addAll(sizelist);
						JsonArray colorCode = obj.getAsJsonArray("colorCode");
						if(colorCode != null && colorCode.size() > 0){
							for(int j = 0 ; j < colorCode.size(); j++ ){
								String color = colorCode.get(j).getAsJsonPrimitive().getAsString();
								for(String size : sizelist){
									priceMap.put(color+";"+size.replace(".", ""), sale+";"+orig+";"+unit+";"+itemNbr+";"+color+";"+size);
								}
							}
						}
					}
				}
			}
		}
		
		//stock
		Map<String,List<String>> stockMap = new HashMap<String,List<String>>();
		if(StringUtils.isNotBlank(atp)){
			JsonObject atpObj = JsonUtils.json2bean(atp, JsonObject.class);
			JsonObject aptData = atpObj.getAsJsonObject("atpData");
			JsonArray arr1 = aptData.getAsJsonArray(assetId);
			if(arr1 != null && arr1.size() > 0){
				for(int i=0; i < arr1.size() ; i++){
					JsonArray arr2 = arr1.get(i).getAsJsonArray();
					String itemNum = arr2.get(0).getAsString();
					List<String> stList = stockMap.get(itemNum);
					if(null == stList){
						stList = new ArrayList<String>();
						stockMap.put(itemNum, stList);
					}
					String size = arr2.get(1).getAsString();
					String color = arr2.get(2).getAsString();
					String status = arr2.get(3).getAsString();
					stList.add(itemNum+";"+size+";"+color+";"+status);
				}
			}
		}
		//sku
		boolean stockFlag = false;
		Sku sku = new Sku();
		List<LSelectionList>  l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>(); 
		Map<String,String> style_map = new HashMap<String,String>();
		LSelectionList lSelectionList = null;
		LStyleList lStyleList = null;
		for(String itemSkus : skuList){
			String[] itemSkuArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(itemSkus, "||");
			lSelectionList = new LSelectionList();
			List<Selection> selections = new ArrayList<Selection>();
			StringBuilder priceKey = new StringBuilder();
			for(String item : itemSkuArr){
				String imageBase = StringUtils.substringAfterLast(item, ";");
				String[] itemArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(item, ";");
				String optionKey = itemArr[1];
				String optionValue = itemArr[2];
				if(StringUtils.equals(imageBase, "true")){
					String skuId = itemArr[0];
					if(StringUtils.isBlank(optionValue)){
						optionValue = skuId;
					}
					String swatchUrl = itemArr[3];
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setStyle_id(optionValue);
					
					String sku_id = style_map.get(skuId);
					if(StringUtils.isBlank(sku_id)){
						lStyleList = new LStyleList();
						lStyleList.setStyle_id(optionValue);
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(swatchUrl);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name(optionKey);
						lStyleList.setStyle_name(optionValue);
						l_style_list.add(lStyleList);
						style_map.put(skuId, skuId);
					}
					priceKey.append(skuId).append(";");
					
				} else {
					String value = itemArr[0];
					Selection selection = new Selection();
					selection.setSelect_id(0);
					selection.setSelect_name(optionKey);
					selection.setSelect_value(optionValue);
					selections.add(selection);
					priceKey.append(value);
				}
			}
			logger.info("get priceKey {} from victoriassecret's url {}",priceKey.toString(),url);
			//sku price special
			if(StringUtils.endsWith(priceKey, ";") && itemSkuArr.length == 1){
				priceKey.append(tmpSizeList.get(0));
			}
			String prices = priceMap.get(priceKey.toString());
			String itemNum = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(prices)){
				String[] priceArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(prices, ";");
				String salePrice = priceArr[0];
				String origPrice = priceArr[1];
				String unit = priceArr[2];
				itemNum = priceArr[3];
				float sale = 0;
				float orig = 0;
				if(StringUtils.isNotBlank(salePrice)){
					sale = Float.valueOf(salePrice);
				}
				if(StringUtils.isNotBlank(origPrice)){
					orig = Float.valueOf(origPrice);
				}
				if(orig == 0){
					orig = sale;
				}
				if(StringUtils.equalsIgnoreCase(lSelectionList.getGoods_id(), defaultColor)){
					int save = Math.round((1 - sale / orig) * 100);// discount
					rebody.setPrice(new Price(orig, save, sale, unit));
				}
				lSelectionList.setSale_price(sale);
				lSelectionList.setOrig_price(orig);
				lSelectionList.setPrice_unit(unit);
			} else {
				logger.error("Error while fetching url {} from victoriassecret",url);
				continue;//sku not exist
			}
			//sku stock
			List<String> stockList = stockMap.get(itemNum);
			int stockStatus = 1;
			if(stockList != null && stockList.size() > 0){
				for(String stItem : stockList){
					String[] st = StringUtils.splitByWholeSeparatorPreserveAllTokens(stItem, ";");
					String size = st[1];
					String color = st[2];
					String status = st[3];
					String[] skuProp = StringUtils.splitByWholeSeparatorPreserveAllTokens(priceKey.toString(), ";");
					if((Arrays.asList(skuProp).contains(size) && Arrays.asList(skuProp).contains(color))
							|| (StringUtils.contains(size, "ALL") && Arrays.asList(skuProp).contains(color))
							|| (Arrays.asList(skuProp).contains(size) && StringUtils.contains(color, "ALL")) ){
						if(StringUtils.equals(status, "D")){
							stockFlag = stockFlag || false;
							stockStatus = 0;
						} else {
							stockFlag = stockFlag || true;
						}
						break;
					} else if(StringUtils.contains(size, "ALL") && StringUtils.contains(color, "ALL")) {
						stockFlag = stockFlag || false;
						stockStatus = 0;
						break;
					}
				}
			} else {
				stockFlag = stockFlag || true;
			}
			if(StringUtils.equalsIgnoreCase(lSelectionList.getGoods_id(), defaultColor)){
				lStyleList.setDisplay(true);
			}
			lSelectionList.setStock_status(stockStatus);
			lSelectionList.setStock_number(0);
			lSelectionList.setSelections(selections);
			l_selection_list.add(lSelectionList);
			
		}
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		// full doc info
		
		Pattern pattern = Pattern.compile(".*ProductID=(\\d+)[&]{0,1}.*");
	    Matcher matcher = pattern.matcher(url);
	    String productId = "";
	    if(matcher.find()){
            productId = matcher.group(1);
	    }
		String docid = SpiderStringUtil.md5Encode(domain+productId);
		String url_no = docid;
		rebody.setDOCID(docid);
		rebody.setSite(new Site(domain));
		rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
		rebody.setTitle(new Title(title, "", "", ""));
		// stock
		int stockStatus = 0;
		if(stockFlag){
			stockStatus = 1;
		}
		rebody.setStock(new Stock(stockStatus));
		// images l_image_list
		// rebody.setImage(new LImageList(pics));
		// brand
		rebody.setBrand(new Brand("Victoria's Secret", "", "", ""));
		// Category
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("div#breadcrumbs > ul > li");
		for(Element ele : es){
			String cat = ele.text();
			if(StringUtils.isBlank(cat)){
				continue;
			}
			cats.add(cat);
			breads.add(cat);
		}
		rebody.setCategory(cats);
		// BreadCrumb
		rebody.setBreadCrumb(breads);
		
		
		// description
		Map<String, Object> descMap = new HashMap<String, Object>();
		es = d.select("div.long-description");
		String ownText = StringUtils.EMPTY;
		if(es != null && es.size() > 0 ){
			ownText = es.get(0).ownText();
			String description = es.text();
			descMap.put("en", description);
			rebody.setDescription(descMap);
		}
		//feature
		Map<String, Object> featureMap = new HashMap<String, Object>();
		featureMap.put("feature-1", ownText);
		es = d.select("div.long-description > ul > li");
		if( es != null && es.size() > 0){
			int count = 2;
			for(Element ele : es){
				featureMap.put("feature-" + count, ele.text());
				count++;
			}
		}
		rebody.setFeatureList(featureMap);

		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "women");
		rebody.setProperties(propMap);
		rebody.setSku(sku);
		setOutput(context, rebody);
		//System.out.println(rebody.parseTo());
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
	
	
	public static void main(String[] args) throws Exception {//https://www.victoriassecret.com/gifts/50-and-under/thermal-sleep-romper?ProductID=302507&CatalogueType=OLS
		/*String url = "http://dm.victoriassecret.com/product/504x672/V466490_OF_F.jpg";//http://dm.victoriassecret.com/product/504x672/V466490_OF_F.jpg
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
		headers.put("Upgrade-Insecure-Requests", "1");
		for(int i=0; i< 100 ; i++){
			String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).header(headers).resultAsString();
			System.out.println(content);
			System.out.println("=========================================");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
		/*Document d = JsoupUtils.parse(content);
		Elements es = d.select("div.long-description");
		for(Element e : es){
			System.out.println(e.ownText());
		}*/
		//System.out.println(es.text());
		//https://www.victoriassecret.com/clearance/pink-collegiate-collection/the-ohio-state-university-highlow-full-zip-hoodie-pink?ProductID=310814&CatalogueType=OLS
		VictoriasSecret vic = new VictoriasSecret();
		//https://www.victoriassecret.com/clearance/pink-collegiate-collection/university-of-nebraska-highlow-full-zip-hoodie-pink?ProductID=310820&CatalogueType=OLS
		Url url = new Url("https://www.victoriassecret.com/sleepwear/shop-all-sleep/the-sleepover-knit-pajama-set?ProductID=322101&CatalogueType=OLS");
		                 //https://www.victoriassecret.com/sleepwear/shop-all-sleep/the-sleepover-knit-pajama-set?ProductID=322101&CatalogueType=OLS
		                 //https://www.victoriassecret.com/sleepwear/shop-all-sleep/the-sleepover-knit-pajama-set?ProductID=318357&CatalogueType=OLS
		Context context = new Context();
		context.setUrl(url);
		vic.invoke(context);
	}

}
