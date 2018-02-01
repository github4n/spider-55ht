package com.haitao55.spider.crawler.core.callable.custom.jimmyjazz;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * date:2017-3-15
 * Jimmyjazz.com网站收录
 * @author denghuan
 *
 */
public class Jimmyjazz extends AbstractSelect{

	private static final String domain = "www.jimmyjazz.com";
	private static final String PREFIX = "http://www.jimmyjazz.com/request/style-data/";
	private static final String SUFFIX = "?path=";
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke(Context context) throws Exception {
		String content = getSkuContent(context,context.getCurrentUrl());
		//String content = this.getInputString(context);
		String attr = StringUtils.substringAfter(context.getCurrentUrl(), domain);
		
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String brand = doc.select(".product_title span.brand").text();
			String title = doc.select(".product_title span.name").text();
			String salePrice = doc.select(".product_price_content span.product_price").text();
			String origPrice = doc.select(".product_price_content span.product_price_orig").text();
			String productId = StringUtils.substringBetween(content, "productid','", "'");
			String selectedColor = doc.select(".picolorselected").text();
			
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
			
			String unit = StringUtils.EMPTY;
			
			if(StringUtils.isNotBlank(salePrice)){
				String currency = StringUtils.substring(salePrice, 0, 1);
				unit = Currency.codeOf(currency).name();
				salePrice = pattern(salePrice);
			}
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}else{
				origPrice = pattern(origPrice);
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Elements es = doc.select(".pcolorpadding .swatch span");
			
			if(es != null && es.size() > 0){
				for(Element e : es){
					String value = e.attr("class");
					if(StringUtils.isNotBlank(value)){
						value = URLEncoder.encode(value);
						String url = PREFIX+value+SUFFIX+attr;
						String skuContent = getSkuContent(context,url);
						if(StringUtils.isNotBlank(skuContent)){
							LStyleList lStyleList = new LStyleList();
							JSONObject jsonObject = JSONObject.parseObject(skuContent);
							
							String stock = jsonObject.getString("in_stock");
							
							String colorSkuId = StringUtils.EMPTY;
							String colorVal = jsonObject.getString("color");
							JSONArray jsonArray = jsonObject.getJSONArray("sizes");
							if(jsonArray != null && jsonArray.size() > 0){
								for(int i = 0; i < jsonArray.size(); i++){
									LSelectionList lSelectionList = new LSelectionList();
									JSONObject skuJson = jsonArray.getJSONObject(i);
									String sizeVal = skuJson.getString("size");
									String skuId = skuJson.getString("sku");
									String instock = skuJson.getString("available");
									String selected = skuJson.getString("selected");
									if("true".equals(selected)){
										colorSkuId = skuId;
									}
									int stock_status = 0;
									if(StringUtils.isNotBlank(instock) && 
											"true".equals(instock)){
										stock_status = 1;
									}
									lSelectionList.setGoods_id(skuId);
									lSelectionList.setOrig_price(Float.parseFloat(origPrice));
									lSelectionList.setSale_price(Float.parseFloat(salePrice));
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
							}else{
								int stock_status = 0;
								if(StringUtils.isNotBlank(stock) && 
										"true".equals(stock)){
									stock_status = 1;
								}
								colorSkuId = "000011";
								LSelectionList lSelectionList = new LSelectionList();
								lSelectionList.setGoods_id(colorSkuId);
								lSelectionList.setOrig_price(Float.parseFloat(origPrice));
								lSelectionList.setSale_price(Float.parseFloat(salePrice));
								lSelectionList.setPrice_unit(unit);
								lSelectionList.setStock_status(stock_status);
								lSelectionList.setStyle_id(colorVal);
								List<Selection> selections = new ArrayList<>();
								lSelectionList.setSelections(selections);
								l_selection_list.add(lSelectionList);
							}
							
							List<Image> imageList = new ArrayList<>();
							
							String images = jsonObject.getString("images");
							JSONObject imageJsonObject = JSONObject.parseObject(images);
							JSONArray imageArray = imageJsonObject.getJSONArray("1000");
							for(int j = 0; j < imageArray.size(); j++){
								JSONObject imageJson = imageArray.getJSONObject(j);
								String image = imageJson.getString("image");
								if(StringUtils.isNotBlank(image)){
									imageList.add(new Image(image));
								}
							}
							
							lStyleList.setGood_id(colorSkuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							if(colorVal.equals(selectedColor)){
								lStyleList.setDisplay(true);
								int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
								rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
							}
							
							context.getUrl().getImages().put(colorSkuId, imageList);// picture
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
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String gender = StringUtils.EMPTY;
			String category = StringUtils.substringBetween(content, "category\":\"", "\"");
			if(StringUtils.isNotBlank(category)){
				cats.add(category);
				breads.add(category);
			}
			Elements breadcrumbs = doc.select(".breadcrumb h2 a");
			for(int i = 1; i < breadcrumbs.size()-1; i++){
				String cate = breadcrumbs.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
					if(cate.equalsIgnoreCase("women")){
						gender = "women";
					}else if(cate.equals("men")){
						gender = "men";
					}
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			
			String description = doc.select(".product_desc").text();
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
	
	private String getSkuContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
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
	
	private  static String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?)");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}

}
