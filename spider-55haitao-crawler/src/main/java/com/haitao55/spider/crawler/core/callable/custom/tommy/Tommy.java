package com.haitao55.spider.crawler.core.callable.custom.tommy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * Tommy网站收录
 * @author denghuan
 *
 */
public class Tommy extends AbstractSelect{

	private static final String domain = "usa.tommy.com";
	private static final String IMAGE_PREFIX ="http://shoptommy.scene7.com/is/image/ShopTommy/";
	private static final String IMAGE_SUFFIX ="?wid=700&hei=700&fmt=jpeg";
	private static final String[] ARRAY = {"FNT","BCK","DE1"};
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.POST.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document docment = Jsoup.parse(content);
			String error = docment.select("h2.inventoryOutStats").text();
			if(StringUtils.isNotBlank(error) &&
					StringUtils.containsIgnoreCase(error, "We're sorry, this product was so popular, we've sold out")){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			String productId = StringUtils.substringBetween(content, "productId = '", "'");
			String brand = StringUtils.substringBetween(content, "og:brand\" content=\"","\"");
			String title = docment.select(".productNameInner").text();
			String salePrice = docment.select("span.offerPrice").text();
			String origPrice = docment.select("span.listPrice").text();
			String unit = StringUtils.substringBetween(content, "commandContextCurrency\": \"", "\"");
			if(StringUtils.isBlank(unit)){
				unit = StringUtils.substringBetween(content, "og:price:currency\" content=\"", "\"");
			}
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(SpiderStringUtil.md5Encode(domain+productId));
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			
			 if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling Mytheresa saleprice error ,url"+context.getUrl().toString());
			 }
			 if(StringUtils.isNotBlank(origPrice)){
				 origPrice = origPrice.replaceAll("[$,]", "");
			 }
			 if(StringUtils.isNotBlank(salePrice)){
				 salePrice = salePrice.replaceAll("[$,]", "");
			 }
			
			 if(StringUtils.isBlank(origPrice)){
				 origPrice = salePrice;
			 }
			 
	
			String delafutColor = docment.select("#swatchcontainer ul li.active").attr("data-color-swatch");
			
			Map<String,String> map = Maps.newHashMap();
			Elements swatchEs = docment.select("#swatchcontainer ul li");
			for(Element e : swatchEs){
				String partNumber = e.attr("data-part-number");
				String colorSwatch = e.attr("data-color-swatch");
				String colorCode = e.attr("data-color-code");
				if(StringUtils.isNotBlank(partNumber) && StringUtils.isNotBlank(colorSwatch) && 
						StringUtils.isNotBlank(colorCode)){
					map.put(colorSwatch, partNumber+"_"+colorCode);
				}
			}
			
			 Sku sku = new Sku();
			 List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			 List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			 
			 String colorSize = StringUtils.substringBetween(content, "\"colorToSize\":", "}</div>");
			 if(StringUtils.isNotBlank(colorSize)){
				 JSONObject jsonObject = JSONObject.parseObject(colorSize);
				 Set<String> set = jsonObject.keySet();
				 Iterator<String> it = set.iterator();
				 while(it.hasNext()){
					 String colorVal = it.next();
					 String partNumber = map.get(colorVal);//图片码
					 String skuId = StringUtils.EMPTY;
					 JSONArray jsonArray = jsonObject.getJSONArray(colorVal);
					 for(int i = 0; i < jsonArray.size(); i++){
						 String sizeVal = jsonArray.getString(i);
						 LSelectionList lSelection = new LSelectionList();
						 lSelection.setGoods_id(colorVal+sizeVal);
						 lSelection.setOrig_price(Float.parseFloat(origPrice));
						 lSelection.setSale_price(Float.parseFloat(salePrice));
						 lSelection.setPrice_unit(unit);
						 lSelection.setStock_status(1);
						 lSelection.setStyle_id(colorVal);
						 List<Selection> selections = new ArrayList<>();
						 Selection selection = new Selection();
						 selection.setSelect_name("size");
						 selection.setSelect_value(sizeVal);
						 selections.add(selection);
						 lSelection.setSelections(selections);
						 skuId = colorVal+sizeVal;
						 l_selection_list.add(lSelection);
					 }
					 LStyleList lStyleList = new LStyleList();
					 lStyleList.setGood_id(skuId);
					 lStyleList.setStyle_cate_id(0);
					 lStyleList.setStyle_id(colorVal);
					 lStyleList.setStyle_cate_name("color");
					 lStyleList.setStyle_switch_img("");
					 lStyleList.setStyle_name(colorVal);
					 
					 if(StringUtils.isNotBlank(delafutColor) && 
							 colorVal.equals(delafutColor)){
						 lStyleList.setDisplay(true);
						 int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
						 rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
					 }
					 List<Image> imageList = new ArrayList<>();
					 for(String arr : ARRAY){
						 String imageUrl = IMAGE_PREFIX+partNumber+"_"+arr+IMAGE_SUFFIX;
						 boolean image_exists = image_exists(imageUrl,context);
						 if(image_exists){//main　为主图，肯定存在，减少发送请求次数
							 imageList.add(new Image(imageUrl));
						 }
					 }
					 context.getUrl().getImages().put(skuId, imageList);// picture

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
			 }
			 rebody.setStock(new Stock(spuStock));
			 
			 List<String> cats = new ArrayList<String>();
			 List<String> breads = new ArrayList<String>();
			 
			 Elements es = docment.select("#breadcrumb a");
			 for(Element e : es){
				 String cate = e.text();
				 if(StringUtils.isNotBlank(cate)){
					 cats.add(cate);
					 breads.add(cate);
				 }
			 }
			 rebody.setCategory(cats);
			 rebody.setBreadCrumb(breads);
			 
			 Map<String, Object> featureMap = new HashMap<String, Object>();
			 Map<String, Object> descMap = new HashMap<String, Object>();
			 Map<String, Object> propMap = new HashMap<String, Object>();
			 propMap.put("s_gender", "");
			 
			 String description = docment.select(".itemDescription").text();
			 //String descriptionHtml = docment.select(".itemDescription").html();
			 int count = 0;
			 if(StringUtils.isNotBlank(description)){
				 String[] dscBr = description.split("• ");
				 for(String br : dscBr){
					 if(StringUtils.isNotBlank(br)){
						 count ++;
						 featureMap.put("feature-"+count, br);
					 }
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", description);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
			 
		}
		setOutput(context, rebody);
	}
	
	/**
	 * 验证图片是否存在
	 * @param image_url
	 * @param context
	 * @return
	 */
	private boolean image_exists(String image_url, Context context) {
		try {
			Url url = context.getUrl();
			String proxyRegionId = url.getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				Crawler.create().timeOut(15000).url(image_url).proxy(false).resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String ip = proxy.getIp();
				int port = proxy.getPort();
				Crawler.create().timeOut(20000).url(image_url).proxy(true).proxyAddress(ip)
				.proxyPort(port).resultAsString();
			}
			return true;
		} catch (ClientProtocolException e) {
		} catch (HttpException e) {
			if(200!=e.getStatus()){
				return false;
			}
		} catch (IOException e) {
		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		context.setCurrentUrl("http://usa.tommy.com/en/women/SHIRTS-BLOUSES-WOMEN/stripe-dot-shirt-ww17797");
		Tommy tomm = new Tommy();
		tomm.invoke(context);
	}

}
