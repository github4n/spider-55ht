package com.haitao55.spider.crawler.core.callable.custom.matchesfashion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
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
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * date: 2016.12.28
 * @author denghuan
 * matchesfashion.com收录
 *
 */
public class Matchesfashion  extends AbstractSelect{

	private static final String domain = "www.matchesfashion.com";

	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		Map<String, Object> headers = new HashMap<>();
		headers.put("Cookie", setCookie());
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		 if(StringUtils.isBlank(proxyRegionId)){
			 content = Crawler.create().timeOut(60000).header(headers).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue()).proxy(false).resultAsString();
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 content = Crawler.create().timeOut(60000).header(headers).url(context.getCurrentUrl()).proxy(true).method(HttpMethod.GET.getValue()).proxyAddress(proxyAddress)
					 .proxyPort(proxyPort).resultAsString();
		 }
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String default_outfit_code = StringUtils.substringBetween(content, "data-default-outfit-code=\"", "\"");
			String dataJson = StringUtils.substringBetween(content, "data-stl-json='", "'");
			String brand = doc.select(".pdp__description-wrapper .pdp__header h1.pdp-headline a").text();
			String title = doc.select(".pdp__description-wrapper .pdp__header h1.pdp-headline span.pdp-description").text();
			String salePrice = doc.select(".pdp__description-wrapper .pdp__header p.pdp-price").text();
			String origPrice = doc.select(".pdp__description-wrapper .pdp__header p.pdp-price strike").text();
			String pdbSalePrice = doc.select(".pdp__description-wrapper .pdp__header p.pdp-price span.pdp-price__hilite").text();

			String unit = StringUtils.EMPTY;
            if(StringUtils.isNotBlank(pdbSalePrice) 
					&& StringUtils.isNotBlank(origPrice)){
				unit = getCurrencyValue(origPrice);// 得到货币代码
				pdbSalePrice = pdbSalePrice.replaceAll("[£, ]", "");
				origPrice = origPrice.replaceAll("[£, ]", "");
				pdbSalePrice = matcher(pdbSalePrice);
				int save = Math.round((1 - Float.valueOf(pdbSalePrice) / Float.valueOf(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.valueOf(origPrice), save, Float.valueOf(pdbSalePrice), unit));
			}else if(StringUtils.isNotBlank(salePrice) && 
					StringUtils.containsIgnoreCase(salePrice, "£")){
				unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[£, ]", "");
				rebody.setPrice(new Price(Float.valueOf(salePrice), 0, Float.valueOf(salePrice), unit));
			}
            
            String productId = StringUtils.substringBetween(content,"ProductID': \"","\"");
            
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			List<Image> imgList = new ArrayList<>();
			Elements es = doc.select(".thumbs-gallery .thumbs-gallery__thumb img");
			 for(Element ig : es){
				 String image = ig.attr("src");
				 if(StringUtils.isNotBlank(image)){
					 imgList.add(new Image("http:"+image));
				 }
			 }
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean flag = true;
			if(StringUtils.isNotBlank(default_outfit_code)){
				JSONObject jsonObject =  JSON.parseObject(dataJson);
				String outfitCode = jsonObject.getString(default_outfit_code);
				JSONObject outfitJsonObject =  JSON.parseObject(outfitCode);
				JSONArray jsonArray = outfitJsonObject.getJSONArray("products");
				for(int i = 0; i < 1; i++){
					LStyleList lStyleList = new LStyleList();
					JSONObject productsObject =  jsonArray.getJSONObject(i);
					String product = productsObject.getString("product");
					JSONObject skuJsonProduct =  JSON.parseObject(product);
					JSONArray optionsJsonArray = skuJsonProduct.getJSONArray("variantOptions");
					String sskuId = StringUtils.EMPTY;
					
					for(int j = 0; j < optionsJsonArray.size(); j++){
						LSelectionList lSelectionList = new LSelectionList();
						JSONObject optionsObject =  optionsJsonArray.getJSONObject(j);
						String skuId = optionsObject.getString("code");
						String sizeData = optionsObject.getString("sizeData");
						String instock = optionsObject.getString("stockLevelCode");
						int stock_status = 0;
						if(StringUtils.isNotBlank(instock) && 
								!instock.equals("outOfStock")){
							stock_status = 1;
							if(flag){
								sskuId = skuId;
								flag = false;
							}
						}
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setPrice_unit(unit);
						
						if(StringUtils.isNotBlank(pdbSalePrice) && 
								StringUtils.isNotBlank(origPrice)){
							lSelectionList.setOrig_price(Float.valueOf(origPrice));
							lSelectionList.setSale_price(Float.valueOf(pdbSalePrice));
						}else if(StringUtils.isNotBlank(salePrice)){
							lSelectionList.setOrig_price(Float.valueOf(salePrice));
							lSelectionList.setSale_price(Float.valueOf(salePrice));
						}
						
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id("default");
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeData);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
					 lStyleList.setGood_id(sskuId);
					 lStyleList.setDisplay(true);
					 lStyleList.setStyle_cate_name("color");
					 lStyleList.setStyle_cate_id(0);
					 lStyleList.setStyle_name("default");
					 lStyleList.setStyle_id("default");
					 lStyleList.setStyle_switch_img("");
					 context.getUrl().getImages().put(sskuId, imgList);// picture
					 l_style_list.add(lStyleList);
					
				}
				
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
			}else{
				if(StringUtils.isNotBlank(productId)){
					context.getUrl().getImages().put(productId, imgList);// picture
				}
			}
			rebody.setStock(new Stock(spuStock));
			
			
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 Elements cateEs = doc.select("#breadcrumb ul li a");
			 Map<String, Object> propMap = new HashMap<String, Object>();
			 for(int i = 0; i < cateEs.size()-2; i++){
				 String cate = cateEs.get(i).text();
				 if(StringUtils.isNotBlank(cate)){
					 cats.add(cate);
					 breads.add(cate);
				 }
				 if(StringUtils.containsIgnoreCase(cate, "womens")){
					 propMap.put("s_gender", "Womens");
				 }else if(StringUtils.containsIgnoreCase(cate, "mens")){
					 propMap.put("s_gender", "mens");
				 }
			 }
			 rebody.setCategory(cats);
			 rebody.setBreadCrumb(breads);
			 
			 Map<String, Object> featureMap = new HashMap<String, Object>();
			 Map<String, Object> descMap = new HashMap<String, Object>();
			 
			 boolean isExist = doc.select(".scroller-content").isEmpty();
			 String desc = StringUtils.EMPTY;
			 if(isExist){
			 }else{
				 desc = doc.select(".scroller-content").get(0).text();
			 }
			 Elements featureEs= doc.select("ul.pdp-accordion__body__size-list li");
			 int count = 0;
			 for(int i = 0; i < featureEs.size()-1; i++){
				 String feature = featureEs.get(i).text();
				 if(StringUtils.isNotBlank(feature)){
					 count ++;
					 featureMap.put("feature-"+count, feature);
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", desc);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.codeOf(currency).name();
		return unit;

	}
	
	private String matcher(String price){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private String setCookie(){
		String cookie = "loggedIn=false; first-visit=true; signed-up-for-updates=true; uxt_1379=1; country=USA; saleRegion=US; _cs_v=0; _cs_r=0; pc=%5B%7B%221093966000001%22%3A1%7D%5D; gender=womens; billingCurrency=GBP; JSESSIONID=p3~4806E9F3598183C7C07D29C1BD33185E; language=en_US; _ga=GA1.2.2023313673.1482489039; sailthru_content=2feb21fef47659953616a790a4c8ca67; sailthru_visitor=d70cf219-ae7b-4098-ab8a-0175f1174c3f;";
		return cookie;
	}
}
