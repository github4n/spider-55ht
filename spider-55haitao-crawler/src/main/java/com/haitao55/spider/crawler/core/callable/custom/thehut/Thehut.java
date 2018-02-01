package com.haitao55.spider.crawler.core.callable.custom.thehut;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;


/**
 * Thehut网站收录
 * date :  2017-4-12
 * @author denghuan
 *
 */
public class Thehut extends AbstractSelect{

	private static final String domain = "www.thehut.com";
	private static final String IMAGE_URL = "https://s1.thcdn.com/";
	private String THEHUT_API ="https://www.thehut.com/variations.json?productId=#pid#&selected=1&variation1=1&option1=#skuId#";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product-title-wrap h1.product-title").text();
			String brand = StringUtils.substringBetween(content, "productBrand: \"", "\"");
			String salePrice = StringUtils.substringBetween(content, "productPrice':'", "'");
			String productId = StringUtils.substringBetween(content, "productID: \"", "\"");
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			
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
			
			String colorVal = doc.select("select#opts-2 option").attr("rel");
			
			List<Image> imageList = new ArrayList<>();
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements es = doc.select("select#opts-1 option");
			if(CollectionUtils.isNotEmpty(es)){
				boolean imageFlag = true;
				for(Element e : es){
					String sizeVal = e.text();
					String value = e.attr("value");
					if(StringUtils.isNotBlank(value) && 
							StringUtils.isNotBlank(sizeVal)){
						LSelectionList lSelectionList = new LSelectionList();
						String url = THEHUT_API.replace("#pid#", productId).replace("#skuId#", value);
						String rs = getContent(context,url);
						JSONObject jsonObject = JSONObject.parseObject(rs);
						String skuSalePrice = jsonObject.getString("price");
						String skuOrigPrice = jsonObject.getString("rrp");
						String instock = jsonObject.getString("availabilityPrefix");
						
						if(StringUtils.isNotBlank(skuSalePrice)){
							skuSalePrice = skuSalePrice.replace("&#163;", "").replace(",", "");
						}
						if(StringUtils.isBlank(skuOrigPrice)){
							skuOrigPrice = skuSalePrice;
						}
						skuOrigPrice = skuOrigPrice.replaceAll("[£, ]", "");
						
						int stock_status = 0;
						if(StringUtils.isNotBlank(instock) && 
								StringUtils.containsIgnoreCase(instock, "In stock")){
							stock_status = 1;
						}
						lSelectionList.setGoods_id(value);
						lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id(colorVal);
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(sizeVal)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(sizeVal);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
						
						if(imageFlag){
							JSONArray jsonArray = jsonObject.getJSONArray("images");
							for(int i = 0; i < jsonArray.size(); i++){
								JSONObject imageJsonObj = jsonArray.getJSONObject(i);
								String type = imageJsonObj.getString("type");
								if("zoom".equals(type)){
									String image = imageJsonObj.getString("name");
									imageList.add(new Image(IMAGE_URL+image));
								}
							}
							
							int save = Math.round((1 - Float.parseFloat(skuSalePrice) /  Float.parseFloat(skuOrigPrice)) * 100);// discount
							rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), save,  Float.parseFloat(skuSalePrice), unit));
							
							LStyleList lStyleList = new LStyleList();
							lStyleList.setGood_id(value);
							lStyleList.setDisplay(true);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							context.getUrl().getImages().put(value, imageList);// picture
							l_style_list.add(lStyleList);
							
							imageFlag = false;
						}
					}
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
				Elements imageEs = doc.select("ul.list-menu li.list-item a");
				for(Element e : imageEs){
					String image = e.attr("href").replaceAll("\\s*", "");
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
				}
				
				if(CollectionUtils.isEmpty(imageList)){
					String image = doc.select(".main-product-image a").attr("href");
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
				}
				
				context.getUrl().getImages().put(productId, imageList);// picture
				
				if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
				}
				salePrice = salePrice.replace(",", "");
				rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
				
				//String instock = StringUtils.substringBetween(content, "availability\" content=\"", "\"");
				String instock = doc.select("span.soldout").text();
				if(StringUtils.isNotBlank(instock) &&
						!StringUtils.containsIgnoreCase(instock, "Out of Stock")){
					spuStock = 1;
				}
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String gender = StringUtils.EMPTY;
			Elements cateEs = doc.select("ul.breadcrumbs_container li.breadcrumbs_item");
			for(int i = 1; i < cateEs.size(); i++){
				String cate = cateEs.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
					if(cate.equalsIgnoreCase("women")){
						gender = "women";
					}else if(cate.equalsIgnoreCase("men")){
						gender = "men";
					}
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".column__left").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			
			rebody.setSku(sku);
		 }
		setOutput(context, rebody);
	}
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context))
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).retry(3).url(url).header(getHeaders(context))
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/57.0.2987.98 Chrome/57.0.2987.98 Safari/537.36");
		 headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
		 headers.put("ADRUM", "isAjax:true");
		 headers.put("Host", "www.thehut.com");
		 headers.put("Origin", "https://www.thehut.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		 headers.put("Referer", context.getCurrentUrl());
		return headers;
	}
	
}
