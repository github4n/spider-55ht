package com.haitao55.spider.crawler.core.callable.custom.vitacost;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * 
 * @author denghuan
 *
 */
public class Vitacost extends AbstractSelect{

	private static final String DOMAIN = "www.vitacost.com";
	private static final String BASEW_DOMAIN = "https://www.vitacost.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url);
		
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String title = doc.select("#pdTitleBlock h1").text();
			String brand = StringUtils.substringBetween(content, "vPBrandName = '", "'");
			String productId = doc.select("#bb-productID").attr("value");
			String origPrice = StringUtils.substringBetween(content, "Retail price: $", "<");
			String salePrice = StringUtils.substringBetween(content, "vPPrice = '", "'");
			String instock = doc.select(".pBuyMsgOOS").text();
			String unit = StringUtils.substringBetween(content, "currencyCode=\"", "\"");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(DOMAIN+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(DOMAIN));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			 if(StringUtils.isBlank(origPrice)){
				 origPrice = salePrice;
			 }
			 
			 if(StringUtils.isNotBlank(origPrice)){
				 origPrice = origPrice.replaceAll("[$,]", "");
			 }
			 
			 
			List<Image> imageList = new ArrayList<Image>();
			String image = doc.select("#productImage a img").attr("src");
			if(StringUtils.isNotBlank(image)){
				imageList.add(new Image(BASEW_DOMAIN+image));
			}
			
			
			List<JSONObject> params = new ArrayList<>();
			
			Elements pdpEs = doc.select("#pdpVariations li");
			if(pdpEs != null && pdpEs.size() > 1){
				for (Element e : pdpEs) {
					String label = e.select("label").text();
					if (StringUtils.containsIgnoreCase(label, "Size")) {
						Elements sizeUrls  = e.select("select option");
						for(Element sizeUrl : sizeUrls){
							String surl = sizeUrl.attr("value");
							String value = sizeUrl.text();
							if(StringUtils.isNotBlank(surl)){
								JSONObject paramJsonObject = new JSONObject();
								paramJsonObject.put("url", BASEW_DOMAIN+surl);
								paramJsonObject.put("value", value);
								params.add(paramJsonObject);
							}
						}
					}
				}
			}else if(pdpEs != null && pdpEs.size() == 1){
				String label = pdpEs.select("label").text();
				if(StringUtils.isNotBlank(label)){
					label = StringUtils.substringBefore(label, ":");
				}
				Elements Urls  = pdpEs.select("select option");
				for(Element e : Urls){
					String value = e.attr("value");
					String text = e.text();
					if(StringUtils.isNotBlank(value)){
						JSONObject paramJsonObject = new JSONObject();
						paramJsonObject.put("url", BASEW_DOMAIN+value);
						paramJsonObject.put("value", text);
						params.add(paramJsonObject);
					}
				}
			}
			// skuJsonArray
			JSONArray skuJsonArray = new JSONArray();
			
			skuJsonArray = new VitacostHandler().process(params, context.getUrl(), skuJsonArray);
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			
			String stySkuId = StringUtils.EMPTY; 
			if(skuJsonArray != null){
				for(Object object : skuJsonArray){
					JSONObject skuJsonObj = (JSONObject) object;
					String sizeVal = skuJsonObj.getString("size");
					String skuId = skuJsonObj.getString("skuId");
					String stock_status = skuJsonObj.getString("stock_status");
					String sale_price = skuJsonObj.getString("sale_price");
					String orign_price = skuJsonObj.getString("orign_price");
					@SuppressWarnings("unchecked")
					Map<String,Integer> flavors = (Map<String,Integer>)skuJsonObj.get("flavor");
					
					if(MapUtils.isNotEmpty(flavors)){
						Set<String> set = flavors.keySet();
						Iterator<String> it = set.iterator();
						while(it.hasNext()){
							String key = it.next();
							Integer stock = flavors.get(key);
							int sku_in_stock = 0;
							if(stock != null && Integer.parseInt(stock_status) == 0){
								sku_in_stock = stock;
							}else{
								sku_in_stock = Integer.parseInt(stock_status);
							}
							LSelectionList lSelectionList = new LSelectionList();
							String lselSkuId = skuId+key;
							lSelectionList.setGoods_id(lselSkuId);
							lSelectionList.setOrig_price(Float.parseFloat(orign_price));
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setSale_price(Float.parseFloat(sale_price));
							lSelectionList.setStock_status(sku_in_stock);
							lSelectionList.setStyle_id("default");
							List<Selection> selections = new ArrayList<>();
							if(StringUtils.isNotBlank(sizeVal)){
								Selection selection = new Selection();
								selection.setSelect_name("size");
								selection.setSelect_value(sizeVal);
								selections.add(selection);
							}
							Selection selection = new Selection();
							selection.setSelect_name("flavor");
							selection.setSelect_value(key);
							selections.add(selection);
							lSelectionList.setSelections(selections);
							l_selection_list.add(lSelectionList);
							stySkuId = lselSkuId;
						}
					}else if(StringUtils.isNotBlank(sizeVal)){
						LSelectionList lSelectionList = new LSelectionList();
						String lselSkuId = skuId+sizeVal;
						lSelectionList.setGoods_id(lselSkuId);
						lSelectionList.setOrig_price(Float.parseFloat(orign_price));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setSale_price(Float.parseFloat(sale_price));
						lSelectionList.setStock_status(Integer.parseInt(stock_status));
						lSelectionList.setStyle_id("default");
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
						stySkuId = lselSkuId;
					}
					
				}
				
				if(CollectionUtils.isNotEmpty(l_selection_list)){
					LStyleList  lStyleList = new LStyleList();
					lStyleList.setGood_id(stySkuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id("default");
					lStyleList.setStyle_switch_img("");
					lStyleList.setStyle_name("default");
					lStyleList.setDisplay(true);
					l_style_list.add(lStyleList);
					context.getUrl().getImages().put(stySkuId, imageList);// picture
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
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
					
					if(!StringUtils.equalsIgnoreCase(instock, "Out of stock")){
						spuStock = 1;
					}
					
				}
				
				rebody.setStock(new Stock(spuStock));
				
				
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				Elements breadcrumbEs = doc.select(".bordered .bcs a");
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
				String description = doc.select("#detailsTabContent").text();
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
		 headers.put("Host", "www.vitacost.com");
		return headers;
	}
	
	
	private static Map<String,Object> getHeaders1(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Accept-Encoding", "gzip, deflate");
		 headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		 headers.put("Connection", "keep-alive");
		// headers.put("Content-Length", "1000000");
		 headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		 headers.put("Host", "waimai.meituan.com");
		 headers.put("Origin", "http://waimai.meituan.com");
		 
		 headers.put("Referer", "http://waimai.meituan.com/home/wm78pfx9q9gy");
		 headers.put("Cookie", "_lxsdk_cuid=15e7fe2ecadc8-02bef2056c6e21-24414032-100200-15e7fe2ecad1a; ci=45; rvct=45%2C10; uuid=0f5aed120232e06e8106.1505384130.0.0.0; oc=_qwPkH6a11iIXai2vQN4S213ff7ZjNi-aFvvS1KYCiCx1kg_W3ZLW1hVrDSG8Mt4CDDGEY2l5lBkRIwZlfRMrvTXSf8aMmAVeMlhhDzuP9W3QL4cxor3D0hqzVP6yv5JBJxJ3WWsqEwENH0z4a2zpbV0TqtlG6FwS-B5Sa4qUik; __utma=211559370.878110411.1505384132.1505384132.1505384132.1; __utmc=211559370; __utmz=211559370.1505384132.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); __utmv=211559370.|1=city=shanghai=1; w_uuid=vtiwxb5fkM7QTNe_pxPt-jJW4cpktV0EbQxx1SiaiJ1IFg4K_rFYJkFLCj1cSk-C; mtcdn=K; u=236857876; lsu=; lt=i55vIJh6lCdm_j9zu_vMUeLMDU8AAAAAmgQAAJ569wEYOjI1SAH2REhcEJkCvJgqXPECBvXCMucHkWgCn1gHiYukOLrwfR7WG2fzHA; n=\"%E9%BA%A6%E4%BC%A6_%E6%AC%A2%E6%AC%A2\"; cookie_phone=18321641593; w_cid=500103; w_cpy_cn=\"%E6%B8%9D%E4%B8%AD%E5%8C%BA\"; w_cpy=yuzhongqu; waddrname=\"%E5%A4%A7%E5%9D%AA\"; w_geoid=wm78pfx9q9gy; w_ah=\"29.545207880437374,106.52303483337164,%E5%A4%A7%E5%9D%AA\"; JSESSIONID=1q4tzm0sql3pyostt5fwzby1l; _ga=GA1.3.878110411.1505384132; _gid=GA1.3.716731662.1505384608; _gat=1; w_visitid=9b55c9a9-15d1-475f-aade-5d72d0709696; __mta=50787734.1505384608728.1505386115084.1505386492854.13; w_utmz=\"utm_campaign=baidu&utm_source=1522&utm_medium=(none)&utm_content=(none)&utm_term=(none)");
		 
		return headers;
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		
		
			Map<String,Object> payload = new HashMap<>();
			payload.put("classify_type", "cate_all");
			payload.put("sort_type", "0");
			payload.put("price_type", "0");
			payload.put("support_online_pay", "0");
			payload.put("support_invoice", "0");
			payload.put("support_logistic", "0");
			payload.put("page_offset", "21");
			payload.put("page_size", "20");
			payload.put("uuid", "vtiwxb5fkM7QTNe_pxPt-jJW4cpktV0EbQxx1SiaiJ1IFg4K_rFYJkFLCj1cSk-C");
			payload.put("platform", "1");
			payload.put("partner", "4");
			payload.put("originUrl", "http%3A%2F%2Fwaimai.meituan.com%2Fhome%2Fwm78pfx9q9gy");
			payload.put("_token", "eJx90luPmkAUB/DvwkNfJAvDXDHZNKDooqtrXcVLYxoEVBYRuanY9Lt3Bht5qyHxl8N/zhwGfkuZ7UttoPIflaUi58YqhgzrEEEAZclragQjAkRtmzldqf0TQBXIUIMbUZnyAq8QIlPCNvLjJkVoI2uIXyJj84h0KIpzW1Gubhi74UschEXpnl68JFYOSRwo15iy8 6mp/q kuT/pL XRfzLc OzG 5Pr1s39MtvopQnZeYFrwBrmsQ3jWdiU4KgDCggvCNBSBA1hDWxIKhJBNUnVVaTCqKGsOFjGRN8LNNlQHTaEAtiVbDugIFgncV8MsLqLCaCjywVrPti0YHVWaI2BA21hui5jDDBekhaZ uHp9pzN4oF62ej7DnOg3UH1szLtAf5gUbiQPm/ zxYDWpy13Z4TmNYFl APeqLaPEvmvM3JLWlYHAbf3lFer0b8 mEHTst9qkx6zib97O5NfHNKzzvCbGSKBt 7Rapb8J3ZZempjXQPIZGd/uW9cKJHQbvkRlYI3PhuL2KGMYlD8bdaN/KyurNjFbmPv8yDD/u6dWalZ3R0Aru5WVeZUtl4YYJmyObDN2xkaf3Sf jt4z6C8d2DWc/  yBa3XsoG00/WGU98J3LAcanzfCFDW4DrB3ctQVNad9c1kdT2PnYE7WfE9jiwYhHygAl/WkxbSMv/BOqqPLfbWbbT1dOUxh0IJDf05X467TJSB9i4/JhaatnZ94LqUZxpf1W6Ws1sf8VfrzF0Ob8rQ=");
		
			String html = Crawler.create().timeOut(30000).header(getHeaders1()).payload(payload).url("http://waimai.meituan.com/ajax/poilist?_token=eJx9kVtvm0AQhf8LD30JCuyF3cVSVAHGKfUlxDHEcRVVGLADNrHBYBui/vfubqPwVoTEx9nDzJnhQ6m8RBkAnV9UVeoTZ0M3EMMmxQZDqhL3mqFTCClUlXUVDpXBL4B0oPKjV6HMucAVQlRK2Kv675Bi/KpCzG/h8bhFeavr40DTLlFWRNltkWZ1E73fxodCezsUqXYpKDturmZpbltF/Y/7e1MXv+OoOEbZ9v1uHWVJ801Ip0NTxekdMCBUeNNiIZoSZKoIGCavSLAukEkEAqlEKJBIRD1igYZEQyCWSHtkApFAQ1YAEoWKZTcCe+xVqvfYG6hogWUcSr6QwR7NLzRFBixCUl3WFXEolHWhQCSmQKIulXmR+IwSYUDSIBsjXSATY0KxEmoKA5TFTBEHSq/sBoFY6E4slD+jr8VCHmDohZ/TQt2Qb9xSf1pP/A8pAyX9eZ3lcV1eOiuY+2zv3LAnyIL9IrivAteP7Qs6bglxD7t8nG+ey8RGE21TlrZ7hDHD0867VqPM97J0srNTd2o/h9GoJZZ1PqWz4S67OTftD3v3Ym9PuWUlxchsV6zJpmM37c7+tK3WxjLKDizAHhlHM0uvH5e5b+WtC0MvssLt4mkELu3ewevd/NFqJnUSuiGytO6KNZxsu9bwq8MVOpnnnMHkIaocJ81X+4fH68W754FScF5tbhiaENNwShOfu2CzWMem9jb30xaNk4C+zIZhAV5A3j3pS4A2fgn8WaIBpi1Po24UrPb6nfLnLzKq8kI=").method(HttpMethod.POST.getValue())
					.resultAsString();
			
			System.out.println(html);
		}
		
	
}
