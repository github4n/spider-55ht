package com.haitao55.spider.crawler.core.callable.custom.theory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;


public class Theory extends AbstractSelect{

	private static final String domain = "www.theory.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content ="";
		String currentUrl  = context.getCurrentUrl();
		String referer = context.getUrl().getParentUrl();
		String sourceUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		if(StringUtils.isBlank(sourceUrl)){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"theory.com itemUrl: "+currentUrl+" ,  url rule is error.");
		}
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(sourceUrl, getHeaders(referer));
			context.setHtmlPageSource(content);
		}else{
			content = crawlerUrl(context,sourceUrl);
		}
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product-detail .pdp-product-name").text();
			String brand = StringUtils.substringBetween(content, "brand\":\"", "\"");
			String unit = StringUtils.substringBetween(content, "currencyCode = '", "'");
			String productId = doc.select(".pdp-details-info").attr("data-masterid");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"theory.com itemUrl: "+sourceUrl+" ,  url productId is error.");
			}
			String url_no = SpiderStringUtil.md5Encode(sourceUrl);
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(sourceUrl, System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			setLselectList(l_selection_list,l_style_list,content,unit,context,true,rebody);
			
//			String origPrice = doc.select(".price-standard").text();
//			String salePrice = doc.select(".price-sales").text();
//			String spuInstock = doc.select("span.bold").text();			
			
//			int skuStock = 0;
//			if(StringUtils.isNotBlank(spuInstock) && 
//					!StringUtils.containsIgnoreCase(spuInstock, "is sold out")){
//				skuStock = 1;
//			}
//			
//			String defaultColor = doc.select(".pdp-variation-attributes ul.swatches li.selected a img").attr("alt");
//			
//			
//			Map<String,String> sizeMap = new HashMap<>();
//			Elements sizeEs = doc.select("ul.size li.size-swatch a");
//			for(Element e : sizeEs){
//				String sizeUrl = e.attr("href");
//				String sizeVal = e.select("a span").text();
//				if(StringUtils.isNotBlank(sizeUrl) && 
//						StringUtils.isNotBlank(sizeVal)){
//					sizeMap.put(sizeVal, sizeUrl);
//				}
//			}
//			
//			JSONArray  stockJsonArray = new JSONArray();
//			if(MapUtils.isNotEmpty(sizeMap)){
//				new TheoryHandler().process(sizeMap, context.getUrl(), stockJsonArray);
//			}
//			
//			List<Image> imageList = new ArrayList<>();
//			Elements imgEs = doc.select(".pdp-slider .pdp-primary-picture img");
//			imgEs.forEach(e->{
//				imageList.add(new Image("http://"+e.attr("src")));
//			});
//			
			
//			if(StringUtils.isNotBlank(defaultColor)){
//				String skuId = StringUtils.EMPTY;
//				LStyleList lStyleList = new LStyleList();
//				if(stockJsonArray != null && stockJsonArray.size() > 0){
//					for(int i = 0; i < stockJsonArray.size(); i++){
//						JSONObject jsonObject = stockJsonArray.getJSONObject(i);
//						Set<String> set = jsonObject.keySet();
//						Iterator<String> it = set.iterator();
//						while(it.hasNext()){
//							String key = it.next();
//							skuId = defaultColor+key;
//							String instock = jsonObject.getString(key);
//							LSelectionList lSelectionList = new LSelectionList();
//							lSelectionList.setOrig_price(Float.parseFloat(origPrice));
//							lSelectionList.setSale_price(Float.parseFloat(salePrice));
//							lSelectionList.setPrice_unit(unit);
//							lSelectionList.setStock_status(Integer.parseInt(instock));
//							lSelectionList.setStyle_id(defaultColor);
//							lSelectionList.setGoods_id(skuId);
//							List<Selection> selections = new ArrayList<>();
//							Selection selection = new Selection();
//							selection.setSelect_name("size");
//							selection.setSelect_value(key);
//							selections.add(selection);
//							lSelectionList.setSelections(selections);
//							l_selection_list.add(lSelectionList);
//						}
//					}
//					
//				}else{
//					skuId = defaultColor;
//					LSelectionList lSelectionList = new LSelectionList();
//					lSelectionList.setGoods_id(defaultColor);
//					lSelectionList.setOrig_price(Float.parseFloat(origPrice));
//					lSelectionList.setSale_price(Float.parseFloat(salePrice));
//					lSelectionList.setPrice_unit(unit);
//					lSelectionList.setStock_status(skuStock);
//					lSelectionList.setStyle_id(defaultColor);
//					List<Selection> selections = new ArrayList<>();
//					lSelectionList.setSelections(selections);
//					l_selection_list.add(lSelectionList);
//				}
//				
//				
//				lStyleList.setStyle_cate_id(0);
//				lStyleList.setStyle_cate_name("color");
//				lStyleList.setStyle_id(defaultColor);
//				lStyleList.setStyle_switch_img("");
//				lStyleList.setStyle_name(defaultColor);
//				lStyleList.setGood_id(skuId);
//				lStyleList.setDisplay(true);
//				context.getUrl().getImages().put(skuId, imageList);// picture
//				l_style_list.add(lStyleList);
//			}
			
			List<String> urls = new ArrayList<>();
			//doc.select(".pdp-variation-attributes ul.swatches li.selected").remove();//去掉选中的URL
			String defaultColor = StringUtils.substringBetween(content, "variant\":\"", "\"");
			Elements es = doc.select("ul.pdp-variation-attributes li.attribute ul.color li a");
			for(Element e :es){
				String colorUrl = e.attr("href");
				String colorVal = e.select("img").attr("alt");
				if(StringUtils.isNotBlank(colorUrl) && 
						!defaultColor.equals(colorVal)){
					urls.add(colorUrl);
				}
			}
			
			if(CollectionUtils.isNotEmpty(urls)){
				urls.forEach (url->{
					String html = "";
					try {
						if (isRunInRealTime) {
							LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
							html = luminatiHttpClient.request(url, getHeaders(referer));
						}else{
							html = crawlerUrl(context,url);
						}
						
						if(StringUtils.isNotBlank(html)){
							setLselectList(l_selection_list,l_style_list,html,unit,context,false,rebody);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				});
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
			Elements breadcrumbEs = doc.select(".pdp-breadcrumb-wrapper .breadcrumb a.breadcrumb-element");
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
			String description = doc.select("p.pdp-details-info").text();
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
	
	private void setLselectList(List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,
			String content,String unit,Context context,boolean isStock,RetBody rebody){
		Document doc = Jsoup.parse(content);
		
		String origPrice = doc.select(".desktop-only .product-price .price-standard").text();
		String salePrice = doc.select(".desktop-only .product-price .price-sales").text();
		if(StringUtils.isNotBlank(salePrice)){
			salePrice = pattern(salePrice);
		}
		if(StringUtils.isBlank(origPrice)){
			origPrice = salePrice;
		}
		origPrice = pattern(origPrice);
		
		if(StringUtils.isBlank(salePrice) && StringUtils.isBlank(origPrice)){
			return;
		}
		
		if(isStock){
			int save = Math
					.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
			rebody.setPrice(new Price(Float.parseFloat(origPrice), save,
					Float.parseFloat(salePrice), unit));
		}
		
		String spuInstock = doc.select("span.bold").text();
		
		int skuStock = 1;
		if(StringUtils.isNotBlank(spuInstock) && 
				StringUtils.containsIgnoreCase(spuInstock, "is sold out")){
			skuStock = 0;
		}
		
		String defaultColor = doc.select(".pdp-variation-attributes ul.swatches li.selected a img").attr("alt");
		if(StringUtils.isBlank(defaultColor)){
			defaultColor = StringUtils.substringBetween(content, "variant\":\"", "\"");
		}
		Map<String,String> sizeMap = new HashMap<>();
		Elements sizeEs = doc.select("ul.size li.size-swatch a");
		for(Element e : sizeEs){
			String sizeUrl = e.attr("href");
			String sizeVal = e.select("span").text();
			if(StringUtils.isNotBlank(sizeUrl) && 
					StringUtils.isNotBlank(sizeVal)){
				sizeMap.put(sizeVal, sizeUrl);
			}
		}
		
		JSONArray  stockJsonArray = new JSONArray();
		if(MapUtils.isNotEmpty(sizeMap)){
			new TheoryHandler().process(sizeMap, context.getUrl(), stockJsonArray);
		}
		
		List<Image> imageList = new ArrayList<>();
		Elements imgEs = doc.select(".pdp-slider .pdp-primary-picture img");
		imgEs.forEach(e->{
			imageList.add(new Image("http:"+e.attr("src")));
		});
		
		if(StringUtils.isNotBlank(defaultColor)){
			String skuId = StringUtils.EMPTY;
			LStyleList lStyleList = new LStyleList();
			if(stockJsonArray != null && stockJsonArray.size() > 0){
				for(int i = 0; i < stockJsonArray.size(); i++){
					JSONObject jsonObject = stockJsonArray.getJSONObject(i);
					Set<String> set = jsonObject.keySet();
					Iterator<String> it = set.iterator();
					while(it.hasNext()){
						String key = it.next();
						skuId = defaultColor+key;
						String instock = jsonObject.getString(key);
						LSelectionList lSelectionList = new LSelectionList();
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(Integer.parseInt(instock));
						lSelectionList.setStyle_id(defaultColor);
						lSelectionList.setGoods_id(skuId);
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(key);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
				}
			}else{
				skuId = defaultColor;
				LSelectionList lSelectionList = new LSelectionList();
				lSelectionList.setGoods_id(defaultColor);
				lSelectionList.setOrig_price(Float.parseFloat(origPrice));
				lSelectionList.setSale_price(Float.parseFloat(salePrice));
				lSelectionList.setPrice_unit(unit);
				lSelectionList.setStock_status(skuStock);
				lSelectionList.setStyle_id(defaultColor);
				List<Selection> selections = new ArrayList<>();
				lSelectionList.setSelections(selections);
				l_selection_list.add(lSelectionList);
			}
			
			lStyleList.setStyle_cate_id(0);
			lStyleList.setStyle_cate_name("color");
			lStyleList.setStyle_id(defaultColor);
			lStyleList.setStyle_switch_img("");
			lStyleList.setStyle_name(defaultColor);
			lStyleList.setGood_id(skuId);
			lStyleList.setDisplay(isStock);
			context.getUrl().getImages().put(skuId, imageList);// picture
			l_style_list.add(lStyleList);
		}
	}
	
	private String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
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
	
	private static Map<String, Object> getHeaders(String referer) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.75 Chrome/62.0.3202.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.theory.com");
		headers.put("Referer", referer);
		headers.put("cookie","__cfduid=d93ce31324d0eb2776d120da671fe777e1510292470; dwac_bcCM6iaagVSqEaaadr8UJrigw0=Z3Kwqt2TdUvA-833nTw_XtGTFxdrvY7ATVU%3D|dw-only|||USD|false|US%2FEastern|true; cqcid=abEOJH2tNdZud5YO9yKENWVQJc; sid=Z3Kwqt2TdUvA-833nTw_XtGTFxdrvY7ATVU; dwanonymous_d7f948559b2a8e9db6182d915a93ea03=abEOJH2tNdZud5YO9yKENWVQJc; dwsid=8teX1DCOImy-91ksrRl2QEQ2MGmGoNnnM1kytYr1_TPN3EJw-CFE1sVl0qysGPWz-FEAN7Xa3Z014oBCHjMLnw==; dw=1; dw_cookies_accepted=1; sr_browser_id=44afcd15-0fae-4a50-a839-4500290d5600; sr_pik_session_id=be84a2d8-1aa7-795b-3513-18153bf39ed4; newSession=new; _uetsid=_uet4933208e; _ga=GA1.2.1964719823.1510292472; _gid=GA1.2.2102611376.1510292472; __cq_bc=%7B%22aado-theory2_US%22%3A%5B%7B%22id%22%3A%22H1071103%22%7D%2C%7B%22id%22%3A%22H05AC007%22%2C%22sku%22%3A%22190789110127%22%7D%5D%7D; __cq_seg=0~0.15!1~0.45!2~-0.03!3~-0.06!4~-0.53!5~0.67!6~0.09!7~-0.09!8~-0.00!9~-0.16!f0~15~9; __cq_uuid=41de96c0-a6ef-11e6-92ed-47e9ea91daa6; _sr_sp_id.5eb1=23000d73-7a1d-4e49-9483-c4e2deae7460.1510292473.1.1510292584.1510292473.c9baba3f-ce76-41be-aa66-226739ab1aee; _sr_sp_ses.5eb1=*");
		return headers;
	}
	
}
