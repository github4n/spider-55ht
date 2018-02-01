package com.haitao55.spider.crawler.core.callable.custom.puma;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * 2017-8-11
 * @author denghuan
 *
 */
public class Puma extends AbstractSelect{

	private static final String domain = "us.puma.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		String content = crawlerUrl(context,url);
		
		RetBody rebody = new RetBody();
		
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String title = StringUtils.substringBetween(content, "productName\":\"", "\",");
			String productId = StringUtils.substringBetween(content, "configData.productId = \"", "\"");
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\"");
			String brand = StringUtils.substringBetween(content, "brand\":\"", "\"");
			
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
			
			String salePrice = doc.select(".price-sales").text();
			String origPrice = doc.select(".price-standard").text().trim();
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$,]", "");
			}
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$,]", "");
			
			String defalutColor = doc.select("ul.color li.selected a").attr("title");
			
			
			List<JSONObject> params = new ArrayList<>();
			
			Elements es = doc.select("ul.color li a");
			for(Element e : es){
				String skuUrl = e.attr("href");
				if(StringUtils.isNotBlank(skuUrl)){
					JSONObject paramJsonObject = new JSONObject();
					paramJsonObject.put("url", skuUrl);
					params.add(paramJsonObject);
				}
			}
			
			// skuJsonArray
			JSONArray skuJsonArray = new JSONArray();
			
			skuJsonArray = new PumaHandler().process(params, context.getUrl(), skuJsonArray);
			
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			
			if(skuJsonArray != null){
				for(Object object : skuJsonArray){
					JSONObject skuJsonObj = (JSONObject) object;
					String stySkuId = StringUtils.EMPTY; 
					String color = skuJsonObj.getString("color");
					String skuId = skuJsonObj.getString("skuId");
					String stock_status = skuJsonObj.getString("stock_status");
					String sale_price = skuJsonObj.getString("sale_price");
					String orign_price = skuJsonObj.getString("orign_price");
					@SuppressWarnings("unchecked")
					List<Image> images = (List<Image>)skuJsonObj.get("images");
					@SuppressWarnings("unchecked")
					List<String> sizes = (List<String>)skuJsonObj.get("sizes");
					
					if(CollectionUtils.isNotEmpty(sizes)){
						for(String size : sizes){
							LSelectionList lSelectionList = new LSelectionList();
							String lselSkuId = skuId+size;
							lSelectionList.setGoods_id(lselSkuId);
							lSelectionList.setOrig_price(Float.parseFloat(orign_price));
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setSale_price(Float.parseFloat(sale_price));
							lSelectionList.setStock_status(Integer.parseInt(stock_status));
							lSelectionList.setStyle_id(color);
							List<Selection> selections = new ArrayList<>();
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(size);
							selections.add(selection);
							lSelectionList.setSelections(selections);
							l_selection_list.add(lSelectionList);
							stySkuId = lselSkuId;
						}
					}
					
					LStyleList  lStyleList = new LStyleList();
					lStyleList.setGood_id(stySkuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(color);
					lStyleList.setStyle_switch_img("");
					lStyleList.setStyle_name(color);
					if(StringUtils.isNotBlank(color) && defalutColor.equals(color)){
						lStyleList.setDisplay(true);
					}
					l_style_list.add(lStyleList);
					
					context.getUrl().getImages().put(stySkuId, images);// picture
					
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
			Elements breadcrumbEs = doc.select("ol.breadcrumb li a span");
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
			String description = doc.select("#product-details").text();
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
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "us.puma.com");
		headers.put("Cookie", "dw=1; dw=1; __cfduid=dd9aeb19ede1a78ab26e812725fb40d4d1501138055; cqcid=ab02eBCq22LWZLtlTfLbCbHTc1; dwanonymous_7254072e2668c23dc3bf6cca213a6657=ab02eBCq22LWZLtlTfLbCbHTc1; AMCVS_C2D31CFE5330AFE50A490D45%40AdobeOrg=1; TruView_visitor=ae697c6c-0b93-42e2-ad81-28d947452af0; TruView_uab=46; TruView_session=7641de39-5d60-4684-87ff-d9e3834ff8a8; pt_s_3cc35021=vt=1502864903926&cad=; pt_3cc35021=uid=bqMktSrlPWspsinhQo1wIA&nid=0&vid=ZdPJTPxH1oj1GPXYHtF3qA&vn=2&pvn=1&sact=1502864905644&to_flag=0&pl=GJNBbH9ugujz46Ywo1oJVQ*pt*1502864903926; TruView_tssession=1502867314006; dw=1; liveagent_oref=; liveagent_sid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; liveagent_vc=2; liveagent_ptid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; PopupFlag=Puma True; _blka_v=0ae705f1-a5fd-463a-8194-ec31c4097f00; _blka_uab=83; _CT_RS_=Recording; _blka_lpd=10; _blka_lt=y; _blka_t=y; _blka_pd=2; dwac_bc6ZEiaaieGqMaaaddtsaoIDP5=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80%3D|dw-only|||USD|false|US%2FEastern|true; sid=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80; dwsid=fD6xrcGWSaJAzu5bWlZFcP5aGDpa1Ke86i9-EnecAxgi6LmQNT058lX_sAYPiCCqEfsMrBsW29S4I-Usp5qblw==; AMCV_C2D31CFE5330AFE50A490D45%40AdobeOrg=-1176276602%7CMCIDTS%7C17429%7CMCMID%7C32783341557131236263529480666784243745%7CMCAAMLH-1506393649%7C9%7CMCAAMB-1506404397%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1505806797s%7CNONE%7CMCAID%7C2C3F736C0519519B-4000060D6006A946;");
		return headers;
	}
	
}
