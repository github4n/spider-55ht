package com.haitao55.spider.crawler.core.callable.custom.katespade;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * 
  * @ClassName: KateSpade
  * @Description: kate详情页面
  * @author songsong.xu ,updateTime denghuan
  * @date 2016年11月19日 下午3:43:48 updateTime by 2017-08-30
  *
 */
public class KateSpade extends AbstractSelect {
	private static final String domain = "www.katespade.com";
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String  content = "";
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(url, getHeaders());
			context.setHtmlPageSource(content);
		}else{
			content = crawlerUrl(context, url);
		}
		
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String productId = doc.select("#uisProductID").attr("value");
			String title = doc.select("h1.product-name").text();
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("kate spade NEW YORK", ""));
			
			String origPrice = doc.select("#product-content .product-price span.price-standard").text();
			String salePrice = doc.select("#product-content .product-price span.price-sales").text();
	
			String unit = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(salePrice)){
				String currency = StringUtils.substring(salePrice, 0, 1);
				unit = Currency.codeOf(currency).name();//unit
				salePrice = salePrice.replaceAll("[$ ,]", "");
			}
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$ ,]", "");
			
			String defaultColor = doc.select("ul.Color li.selected span.title").text();
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements colorEs = doc.select("ul.Color li");
			for(Element e : colorEs){
				LStyleList lStyleList = new LStyleList();
				String dataValue = e.attr("data-value");
				String colorVal = e.select("span.title").text();
				String colorUrl = e.select("a").attr("href");
				if(StringUtils.isBlank(colorUrl)){
					continue;
				}
				String skuId = StringUtils.EMPTY;
				String html = StringUtils.EMPTY;
				boolean flag = true;
				if(StringUtils.isNotBlank(colorUrl)){
					if(colorVal.equals(defaultColor)){
						html = content;
					}else{
						if (isRunInRealTime) {
							LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
							html = luminatiHttpClient.request(colorUrl, getHeaders());
						}else{
							html = crawlerUrl(context,colorUrl);
						}
					}
					if(StringUtils.isNotBlank(html)){
						Document skuDoc = Jsoup.parse(html);
						String skuOrigPrice = skuDoc.select("#product-content .product-price span.price-standard").text();
						String skuSalePrice = skuDoc.select("#product-content .product-price span.price-sales").text();
						
						if(StringUtils.isNotBlank(skuSalePrice)){
							skuSalePrice = skuSalePrice.replaceAll("[$ ,]", "");
						}
						if(StringUtils.isBlank(skuOrigPrice)){
							skuOrigPrice = skuSalePrice;
						}
						skuOrigPrice = skuOrigPrice.replaceAll("[$ ,]", "");
						
						Elements sizeEs = skuDoc.select("ul.size li.emptyswatch");
						if(CollectionUtils.isNotEmpty(sizeEs)){
							for(Element se : sizeEs){
								String sizeVal = se.select("a").text();
								if(StringUtils.isBlank(sizeVal)){
									continue;
								}
								flag = false;
								LSelectionList lSelectionList = new LSelectionList();
								String sizeHtml = se.html();
								int stock_status = 1;
								if(StringUtils.isNotBlank(sizeHtml) && 
										StringUtils.containsIgnoreCase(sizeHtml, "unselectable")){
									stock_status = 0;
								}
								skuId = dataValue+sizeVal;
								lSelectionList.setGoods_id(skuId);
								lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
								lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
								lSelectionList.setPrice_unit(unit);
								lSelectionList.setStock_status(stock_status);
								lSelectionList.setStyle_id(colorVal);
								List<Selection> selections = new ArrayList<>();
								Selection selection = new Selection();
								selection.setSelect_name("size");
								selection.setSelect_value(sizeVal);
								selections.add(selection);
								lSelectionList.setSelections(selections);
								l_selection_list.add(lSelectionList);
							}
						}
						if(StringUtils.isBlank(skuId)){
							skuId = dataValue;
						}
						if(flag){
							String outStock = skuDoc.select(".out-of-stock").text();
							int stock_status = 1;
							if(StringUtils.isNotBlank(outStock) && 
									StringUtils.containsIgnoreCase(outStock, "out of stock")){
								stock_status = 0;
							}
							LSelectionList lSelectionList = new LSelectionList();
							lSelectionList.setGoods_id(skuId);
							lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
							lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setStock_status(stock_status);
							lSelectionList.setStyle_id(colorVal);
							List<Selection> selections = new ArrayList<>();
							lSelectionList.setSelections(selections);
							l_selection_list.add(lSelectionList);
						}
						
						List<Image> images = new ArrayList<>();
						Elements imgEs = skuDoc.select("#thumbnail-carousel li a.thumbnail-link");
						for(Element ig : imgEs){
							String image = ig.attr("href");
							if(StringUtils.isNotBlank(image)){
								images.add(new Image(image));
							}
						}
				
						context.getUrl().getImages().put(skuId, images);// picture
					}
				
				}
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(colorVal);
				lStyleList.setStyle_name(colorVal);
				lStyleList.setStyle_switch_img("");
				if(colorVal.equals(defaultColor)){
					lStyleList.setDisplay(true);
				}
				
				l_style_list.add(lStyleList);
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
			Elements breadcrumbEs = doc.select("ol.breadcrumb li a");
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
			String description = doc.select("#small-details").text();
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
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue()).header(getHeaders())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).method(HttpMethod.GET.getValue()).header(getHeaders())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "www.katespade.com");
		 return headers;
	}
}