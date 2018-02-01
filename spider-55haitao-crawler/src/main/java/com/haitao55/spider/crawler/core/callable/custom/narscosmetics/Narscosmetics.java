package com.haitao55.spider.crawler.core.callable.custom.narscosmetics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * date:2017-03-21
 * Narscosmetics 收录
 * @author denghuan
 *
 */
public class Narscosmetics extends AbstractSelect{

	private static final String domain = "www.narscosmetics.com";
	private static final String NAARSCOSMET_SUFFIX ="&Quantity=1&format=ajax";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.product-name").text();
			//String title = StringUtils.substringBetween(content, "productName':'", "'");
			String defaultColor = StringUtils.substringBetween(content, "color\":\"", "\",");
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\",");
			String salePrice = StringUtils.substringBetween(content, "amount':'USD ", "'");
			String instock = StringUtils.substringBetween(content, ",\"stock\":", ",");
			String productID = StringUtils.substringBetween(content, "productID':'", "'");
			
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productID)){
				docid = SpiderStringUtil.md5Encode(domain+productID);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Nars Cosmetics", ""));
			
			List<Image> list = new ArrayList<>();
			Elements imageEs = doc.select("ul.product-image-slider li a img");
			for(Element e : imageEs){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					list.add(new Image(image));
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements es = doc.select("select#product-color-select option");
			if(es != null && es.size() > 0){
				for(Element e : es){
					String url = e.attr("data-href");
					if(StringUtils.isNotBlank(url)){
						String html = getContent(context,url+NAARSCOSMET_SUFFIX);
						Document docment = Jsoup.parse(html);
						String product = StringUtils.substringBetween(html, "window.universal_variable.product =", "};");
						if(StringUtils.isNotBlank(product)){
							product = product+"}";
							LSelectionList lSelectionList = new LSelectionList();
							LStyleList lStyleList = new LStyleList();
							System.out.println(product);
							JSONObject jsonObject = JSONObject.parseObject(product);
							String skuId = jsonObject.getString("sku_code");
							String color = jsonObject.getString("color");
							String stock = jsonObject.getString("stock");
							String skuSalePrice = jsonObject.getString("unit_sale_price");
							int stock_status = 0;
							if(StringUtils.isNotBlank(stock) && Integer.parseInt(stock) > 0){
								stock_status = 1;
							}
							
							lSelectionList.setGoods_id(skuId);
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
							lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
							lSelectionList.setStock_status(stock_status);
							lSelectionList.setStyle_id(color);
							List<Selection> selections = new ArrayList<>();
							lSelectionList.setSelections(selections);
							
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(color);
							lStyleList.setStyle_name(color);
							lStyleList.setStyle_switch_img("");
							if(color.equals(defaultColor)){
								lStyleList.setDisplay(true);
								rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
										0, Float.parseFloat(skuSalePrice), unit));
							}
							
							Elements skuImageEs = docment.select("ul.product-image-slider li a img");
							List<Image> imageList = new ArrayList<>();
							for(Element images : skuImageEs){
								String image = images.attr("src");
								if(StringUtils.isNotBlank(image)){
									imageList.add(new Image(image));
								}
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
							l_selection_list.add(lSelectionList);
							l_style_list.add(lStyleList);
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
				if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,
							"narscosmetics.com itemUrl:" + context.getUrl().toString() + " not found..");
				}
				rebody.setPrice(new Price(Float.parseFloat(salePrice), 
						0, Float.parseFloat(salePrice), unit));
				if(StringUtils.isNotBlank(instock) && 
						Integer.parseInt(instock) > 0){
					spuStock = 1;
				}
				context.getUrl().getImages().put(productID, list);// picture
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			
			String cate = StringUtils.substringBetween(content, "category\":\"", "\"");
			String subCate = StringUtils.substringBetween(content, "subcategory\":\"", "\"");
			if(StringUtils.isNotBlank(cate)){
				cats.add(cate);
				breads.add(cate);
			}
			if(StringUtils.isNotBlank(subCate)){
				cats.add(subCate);
				breads.add(subCate);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = doc.select(".scroll-pane").text();
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
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
