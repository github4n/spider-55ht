package com.haitao55.spider.crawler.core.callable.custom.allbeauty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * Allbeauty 网站收录
 * 2016-11-26
 * @author denghuan
 *
 */
public class Allbeauty extends AbstractSelect{
	
	private static final String domain = "www.allbeauty.com";

	@Override
	public void invoke(Context context) throws Exception {
		Map<String, Object> headers = new HashMap<>();
		headers.put("Cookie", setCookie());
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).header(headers).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).header(headers).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String salePirce = doc.select("p.productPrice span.ourPrice b").text();
			String origPirce= doc.select("p.productPrice span.productRRP span.strike").text();
			String brand = StringUtils.substringBetween(content, "brand_name\":\"","\"");
			String productId = StringUtils.substringBetween(content, "product\":{\"sku\":",",");
			String title = doc.select(".font-body b").text();
			String subTitle = doc.select("span.productType").text();
			
			String isStock = doc.select("#addButtonSingle").attr("value");
			
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
			rebody.setTitle(new Title(title+" " +subTitle, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			if(StringUtils.isBlank(salePirce)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"Allbeauty.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
			}
			
			if(StringUtils.isBlank(origPirce)){
				origPirce = salePirce;
			}
			String	unit = StringUtils.EMPTY;
			if(StringUtils.containsIgnoreCase(origPirce, "£")){
				unit = Currency.codeOf("£").name();
			}
			if(StringUtils.isBlank(unit)){
				unit = "GBP";
			}
			salePirce = pattern(salePirce);
			origPirce = pattern(origPirce);
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select("span#breadcrumb a");
			if(CollectionUtils.isNotEmpty(cates)){
				for(int i = 0; i < cates.size(); i++){
					String cat = cates.get(i).text();
					if(StringUtils.isNotBlank(cat)){
						cats.add(cat);
						breads.add(cat);
					}
				}
			}else{
				cats.add(title);
				breads.add(title);
			}
		
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			List<Image> imageList = new ArrayList<Image>();
			Elements imagesEs = doc.select("#product-image-zoom img#product-image-default");
			for(Element e : imagesEs){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select("p.my5").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			Elements skuEs = doc.select("ul#colour-picker li");
			if(CollectionUtils.isNotEmpty(skuEs)){
				for(Element e : skuEs){
					LSelectionList lSelectionList = new LSelectionList();
					LStyleList  lStyleList = new LStyleList();
					
					String colorVal = e.attr("data-product-type");
					String skuSalePrice = e.attr("data-our-price");
					String skuOrigPrice = e.attr("data-product-rrp");
					String skuId = e.attr("data-product-id");
					String switchImg = e.select("img").attr("src");
					
					if(StringUtils.isNotBlank(colorVal)){
						colorVal = colorVal.replaceAll("[\\s*]", "");
					}
					
					 skuOrigPrice = pattern(skuOrigPrice);
					 skuSalePrice = pattern(skuSalePrice);
					
					if(StringUtils.isNotBlank(skuOrigPrice)){
						lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
					}else{
						lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
					}
					
					lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					
					List<Selection> selections = new ArrayList<>();
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setStyle_id(colorVal);
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setSelections(selections);
					int stockStatus = 0;
					if(StringUtils.isNotBlank(isStock) && 
							StringUtils.containsIgnoreCase(isStock, "Add to Bag")){
						stockStatus = 1;
					}
					if(display){
						lStyleList.setDisplay(true);
						display = false;
						if(StringUtils.isNotBlank(skuOrigPrice) 
								&& StringUtils.isNotBlank(skuSalePrice)){
							int save = Math.round((1 - Float.parseFloat(skuSalePrice) / Float.parseFloat(skuOrigPrice)) * 100);// discount
							rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), 
									save, Float.parseFloat(skuSalePrice), unit));
						}else if(StringUtils.isBlank(skuOrigPrice) 
								&& StringUtils.isNotBlank(skuSalePrice)){
							rebody.setPrice(new Price(Float.parseFloat(salePirce), 
									0, Float.parseFloat(salePirce), unit));
						}
						
					}
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_switch_img(switchImg);
					lStyleList.setStyle_id(colorVal);
					lStyleList.setStyle_name(colorVal);
					context.getUrl().getImages().put(skuId, imageList);// picture
					lSelectionList.setStock_status(stockStatus);
					l_selection_list.add(lSelectionList);
					l_style_list.add(lStyleList);
				}
			}
			
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
				if(StringUtils.isNotBlank(isStock) && 
						StringUtils.containsIgnoreCase(isStock, "Add to Bag")){
					spuStock = 1;
				}
				if(StringUtils.isNotBlank(origPirce) 
						&& StringUtils.isNotBlank(salePirce)){
					int save = Math.round((1 - Float.parseFloat(salePirce) / Float.parseFloat(origPirce)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPirce), 
							save, Float.parseFloat(salePirce), unit));
				}else if(StringUtils.isBlank(origPirce) 
						&& StringUtils.isNotBlank(salePirce)){
					rebody.setPrice(new Price(Float.parseFloat(salePirce), 
							0, Float.parseFloat(salePirce), unit));
				}
				
				context.getUrl().getImages().put(productId, imageList);// picture
				
			}
			
			rebody.setStock(new Stock(spuStock));
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
		
	}
	
	private static String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private static String setCookie(){
		String cookie ="locale=GBP%2C48%2C0%2CEN; awinsource=uk; basket=%7B%226013%22%3A3%7D; PHPSESSID=gants1lospoum1mllfnn5aag82; SERVERID=server153; _dc_gtm_UA-200096-1=1; _ga=GA1.2.1990824704.1479874547; __atuvc=52%7C51%2C78%7C6%2C0%7C7%2C0%7C8%2C3%7C9; __atuvs=58b38bf35cd2779a002; __asc=05686bec15a7d5ab4d594d1318c; __auc=34761af61588f6592d528bb8291; __btr_id=f70422c1-c1fa-4b9f-81b9-a96935198be1; frontend=1; __zlcmid=dkg1Ovs22NfWKW";
		return cookie;
	}
}
