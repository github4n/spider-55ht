package com.haitao55.spider.crawler.core.callable.custom.coach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.haitao55.spider.crawler.utils.Constants;

public class Coach_bak extends AbstractSelect{

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.coach.com";

	
	public void invoke(Context context) throws Exception {
		
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url);
		
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "productID\": \"", "\"");
			String unit = StringUtils.substringBetween(content, "priceCurrency\": \"", "\"");
			String title = doc.select(".product-name-desc").text();
			
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
			rebody.setBrand(new Brand("COACH", ""));
			
			String origPrice = doc.select(".product-info-master span.strike-through").text();
			String salePrice = doc.select(".product-info-master span.sales").text();
			
			salePrice = pattern(salePrice);
			origPrice = pattern(origPrice);
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$ ]", "");
			}
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$ ]", "");
		
			int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
			rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			
			List<Image> imageList = new ArrayList<>();
			
			Elements imgEs = doc.select("ul.swiper-wrapper li.swiper-slide");
			for(Element e : imgEs){
				String image = e.attr("data-regular-size");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
//			Elements primaryImg = doc.select(".product-images .product-l-images img.primary-image");
//			
//			for(Element e : primaryImg){
//				String image = e.attr("data-large-image");
//				if(StringUtils.isNotBlank(image)){
//					imageList.add(new Image(image));
//				}
//			}
			
			String defulteColor = doc.select(".pdp-main__swatches li.active img.pdpSwatch").attr("title");

			String instock = doc.select(".in-stock-msg").text();
			
			String skuId = StringUtils.EMPTY;
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			if(StringUtils.isNotBlank(defulteColor)){
				String sizeId = StringUtils.EMPTY;
				Elements sizeEs = doc.select("select.size option");
				if(CollectionUtils.isNotEmpty(sizeEs)){
					for(Element  e : sizeEs){
						String sizeVal = e.text();
						if(StringUtils.containsIgnoreCase(sizeVal, "SELECT")){
							continue;
						}
						LSelectionList lSelectionList = new LSelectionList();
						int stock_status = 0;
						if(StringUtils.isNotBlank(sizeVal) && 
								!StringUtils.containsIgnoreCase(sizeVal, "Unavailable")){
							stock_status = 1;
						}else{
							sizeVal = StringUtils.substringBefore(sizeVal, " Unavailable");
						}
						sizeId = sizeVal;
						setLselectionList(lSelectionList,productId,defulteColor,salePrice,
								origPrice,salePrice,unit,sizeVal);
					
						lSelectionList.setStock_status(stock_status);
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
				}else{
					LSelectionList lSelectionList = new LSelectionList();
					setLselectionList(lSelectionList,productId,defulteColor,salePrice,
							origPrice,salePrice,unit,"");
					int stock_status = 0;
					if(StringUtils.isNotBlank(instock) && 
							StringUtils.containsIgnoreCase(instock, "In Stock")){
						stock_status = 1;
					}
					lSelectionList.setStock_status(stock_status);
					List<Selection> selections = new ArrayList<>();
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
				}
				
				if(StringUtils.isNotBlank(sizeId)){
					skuId = productId+defulteColor+sizeId;
				}else{
					skuId = productId+defulteColor;
				}
				
				LStyleList  lStyleList = new LStyleList();
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(defulteColor);
				lStyleList.setStyle_switch_img("");
				lStyleList.setStyle_name(defulteColor);
				lStyleList.setDisplay(true);
				l_style_list.add(lStyleList);
				
				Elements  es = doc.select(".product-variations ul.swatches li a");
				if(es != null && es.size() > 1){
					for(Element  e : es){
						String pdpUrl = e.attr("data-pdpurl");
						if(StringUtils.isNotBlank(pdpUrl) && 
								!pdpUrl.equals(url)){
							String html = crawlerUrl(context,pdpUrl);
							if(StringUtils.isNotBlank(html)){
								crawlerSku(html,l_selection_list,l_style_list,context);
							}
						}
					}
				}
			}
			if(StringUtils.isBlank(skuId)){
				skuId = productId;
			}
			context.getUrl().getImages().put(skuId, imageList);// picture
			
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
			Elements breadcrumbEs = doc.select(".breadcrumb li a");
			for(Element e : breadcrumbEs){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			
			if(CollectionUtils.isEmpty(breads)){
				cats.add(title);
				breads.add(title);
			}
			
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = StringUtils.substringBetween(content, "class=\"panel-body\">", "</p>");
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
	
	private void crawlerSku(String content,List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,Context context){
		Document doc = Jsoup.parse(content);
		
		String productId = StringUtils.substringBetween(content, "configData.productId = \"", "\"");
		String unit = StringUtils.substringBetween(content, "currency = \"", "\"");
		String activityOrigPrice = doc.select(".needle-wrapper .product-price .sales-price-container span.standardprice").text();
		String activitySalePrice = doc.select(".needle-wrapper .product-price .sales-price-container span.salesprice").text();
		
		String salePrice = doc.select("#pricesContainer .product-price span.price-sales").text();
		
		if(StringUtils.isNotBlank(activityOrigPrice)){
			activityOrigPrice = activityOrigPrice.replaceAll("[$ ]", "");
		}
		
		if(StringUtils.isNotBlank(activityOrigPrice)){
			activitySalePrice = activitySalePrice.replaceAll("[$ ]", "");
		}
		if(StringUtils.isNotBlank(salePrice)){
			salePrice = salePrice.replaceAll("[$ ]", "");
		}
		String defulteColor = doc.select(".product-variations ul.swatches li.selected a").attr("title");
		
		List<Image> imageList = new ArrayList<>();
		String mainImage = doc.select("#primaryImage").attr("data-large-image");
		if(StringUtils.isNotBlank(mainImage)){
			imageList.add(new Image(mainImage));
		}
		
		Elements imgEs = doc.select(".alternate-images .a-image img");
		for(Element e : imgEs){
			String image = e.attr("data-large-image");
			if(StringUtils.isNotBlank(image)){
				imageList.add(new Image(image));
			}
		}
		
		Elements primaryImg = doc.select(".product-images .product-l-images img.primary-image");
		
		for(Element e : primaryImg){
			String image = e.attr("data-large-image");
			if(StringUtils.isNotBlank(image)){
				imageList.add(new Image(image));
			}
		}
		
		
		String skuId = StringUtils.EMPTY;
		String instock = doc.select(".in-stock-msg").text();
		if(StringUtils.isNotBlank(defulteColor)){
			Elements sizeEs = doc.select("select.size option");
			String sizeId = StringUtils.EMPTY;
			if(CollectionUtils.isNotEmpty(sizeEs)){
				for(Element  e : sizeEs){
					String sizeVal = e.text();
					if(StringUtils.containsIgnoreCase(sizeVal, "SELECT")){
						continue;
					}
					LSelectionList lSelectionList = new LSelectionList();
			
					int stock_status = 0;
					if(StringUtils.isNotBlank(sizeVal) && 
							!StringUtils.containsIgnoreCase(sizeVal, "Unavailable")){
						stock_status = 1;
					}else{
						sizeVal = StringUtils.substringBefore(sizeVal, " Unavailable");
					}
					setLselectionList(lSelectionList,productId,defulteColor,activitySalePrice,
							activityOrigPrice,salePrice,unit,sizeVal);
					sizeId = sizeVal;
					lSelectionList.setStock_status(stock_status);
					List<Selection> selections = new ArrayList<>();
					Selection selection = new Selection();
					selection.setSelect_name("size");
					selection.setSelect_value(sizeVal);
					selections.add(selection);
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
				}
			}else{
				LSelectionList lSelectionList = new LSelectionList();
				setLselectionList(lSelectionList,productId,defulteColor,activitySalePrice,
						activityOrigPrice,salePrice,unit,"");
				int stock_status = 0;
				if(StringUtils.isNotBlank(instock) && 
						StringUtils.containsIgnoreCase(instock, "In Stock")){
					stock_status = 1;
				}
				lSelectionList.setStyle_id(defulteColor);
				lSelectionList.setStock_status(stock_status);
				List<Selection> selections = new ArrayList<>();
				lSelectionList.setSelections(selections);
				l_selection_list.add(lSelectionList);
			}
			if(StringUtils.isNotBlank(sizeId)){
				skuId = productId+defulteColor+sizeId;
			}else{
				skuId = productId+defulteColor;
			}
			
			LStyleList  lStyleList = new LStyleList();
			lStyleList.setGood_id(skuId);
			lStyleList.setStyle_cate_id(0);
			lStyleList.setStyle_cate_name("color");
			lStyleList.setStyle_id(defulteColor);
			lStyleList.setStyle_switch_img("");
			lStyleList.setStyle_name(defulteColor);
			lStyleList.setDisplay(false);
			l_style_list.add(lStyleList);
		}
		context.getUrl().getImages().put(skuId, imageList);// picture
	}
	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "www.coach.com");
		return headers;
	}
	
	private void setLselectionList(LSelectionList lSelectionList,String productId,String defulteColor,
			String activitySalePrice,String activityOrigPrice,String salePrice,String unit,String sizeVal){
		lSelectionList.setGoods_id(productId+defulteColor+sizeVal);
		if(StringUtils.isNotBlank(activitySalePrice) && 
				StringUtils.isNotBlank(activitySalePrice)){
			lSelectionList.setOrig_price(Float.parseFloat(activityOrigPrice));
			lSelectionList.setSale_price(Float.parseFloat(activitySalePrice));
		}else if(StringUtils.isNotBlank(salePrice)){
			lSelectionList.setOrig_price(Float.parseFloat(salePrice));
			lSelectionList.setSale_price(Float.parseFloat(salePrice));
		}
		lSelectionList.setStyle_id(defulteColor);
		lSelectionList.setPrice_unit(unit);
	}
	
	private  String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		Context context = new Context();
		Coach fo = new Coach();
		context.setCurrentUrl("http://www.coach.com/coach-fisherman-clog-with-rhinestone-buckle/G1307.html?dwvar_color=BLK");
	    fo.invoke(context);
	}
	
}
