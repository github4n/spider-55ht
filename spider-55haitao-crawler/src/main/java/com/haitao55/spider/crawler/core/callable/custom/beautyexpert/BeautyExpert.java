package com.haitao55.spider.crawler.core.callable.custom.beautyexpert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * 
 * @author denghuan
 *
 */
public class BeautyExpert extends AbstractSelect{

	private static final String domain = "www.beautyexpert.com";
	private String beauty_api = "https://www.beautyexpert.com/variations.json?productId=";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.product-title").text();
			String productId = StringUtils.substringBetween(content, "productID: \"", "\"");
			String brand = StringUtils.substringBetween(content, "productBrand: \"", "\"");
			String unit = StringUtils.substringBetween(content, "currencyType: '", "'");
			String salePrice = StringUtils.substringBetween(content, "productPrice: \"&#163;", "\"");
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"beautyexpert.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
			}
			
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
			rebody.setBrand(new Brand(brand, ""));
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[,]", "");
			}
			rebody.setPrice(new Price(Float.parseFloat(salePrice), 0,
					Float.parseFloat(salePrice), unit));
			
			
			List<Image> imageList = new ArrayList<>();
			Elements es = doc.select("ul.jZoom_ul li.list-item a");
			es.forEach(e ->{
				String image = e.attr("href");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image.trim()));
				}
			});
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			boolean display = true;
			Elements colorEs = doc.select(".js-fieldSet-variations select option");
			for(Element e : colorEs){
				String skuId = e.attr("value");
				String colorVal = e.text();
				if(StringUtils.isBlank(skuId)){
					continue;
				}
				
				String beautyUrl = beauty_api+productId+"&selected=1&variation1=4&option1="+skuId;
				
				String rs = crawlerUrl(context,beautyUrl);
				if(StringUtils.isNotBlank(rs)){
					LStyleList lStyleList = new LStyleList();
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject jsonObject = JSONObject.parseObject(rs);
					String skuSalePrice = jsonObject.getString("price");
					skuSalePrice = skuSalePrice.replace("&#163;", "");
					String instock = jsonObject.getString("availabilityPrefix");
					int stock_status = 0;
					if("In stock".equals(instock)){
						stock_status = 1;
					}
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
					lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id(colorVal);
					List<Selection> selections = new ArrayList<>();
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
					
					
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(colorVal);
					lStyleList.setStyle_name(colorVal);
					lStyleList.setStyle_switch_img("");
					if(display){
						lStyleList.setDisplay(display);
						display = false;
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
			}else{
				context.getUrl().getImages().put(productId, imageList);// picture
				
				String instock = doc.select(".product-stock-message").text();
				if(StringUtils.isNotBlank(instock) && 
						StringUtils.equals(instock, "In stock")){
					spuStock = 1;
				}
			}
			
			rebody.setStock(new Stock(spuStock));
			

			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements breadcrumbEs = doc.select(".breadcrumbs_container li.breadcrumbs_item");
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
			String description = doc.select(".product-info").text();
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
	
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String content = Crawler.create().timeOut(30000).url("https://www.beautyexpert.com/variations.json?productId=11234137&option1=25846&selected=1&variation1=4").method(HttpMethod.GET.getValue())
		.resultAsString();
		
		System.out.println(content);
		
	}

}
