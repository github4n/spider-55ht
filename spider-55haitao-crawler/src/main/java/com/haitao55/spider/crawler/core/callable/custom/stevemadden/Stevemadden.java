package com.haitao55.spider.crawler.core.callable.custom.stevemadden;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * Stevemadden网站收录
 * @author denghuan
 * date : 2017-4-7
 */
public class Stevemadden extends AbstractSelect{

	private static final String domain = "www.stevemadden.com";
	private static final String IMAGE_URL ="http://s7d9.scene7.com/is/image/SteveMadden/";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(context.getCurrentUrl()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "productID\" content=\"", "\"");
			String title = doc.select("h1.item-name").text();
			String salePrice = doc.select(".productSalePrice").text();
			if(StringUtils.isBlank(salePrice)){
				salePrice = doc.select(".productPrice").text();
			}
			String origPrice = doc.select(".strike").text();
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
//			salePrice = salePrice.replaceAll("[$, ]", "");
//			origPrice = origPrice.replaceAll("[$, ]", "");
			
			String docid = SpiderStringUtil.md5Encode(domain+productId);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand("Steve Madden", ""));
			
			String selectedColor = StringUtils.substringBetween(content, "selectedRevColor = '", "'");
			
			HashMap<String,List<Image>> imageMap = new HashMap<String,List<Image>>();
			String thumbsAndStuff = StringUtils.substringBetween(content, "thumbsAndStuff = ", ";");
			if(StringUtils.isNotBlank(thumbsAndStuff)){
				JSONObject jsonObject = JSONObject.parseObject(thumbsAndStuff);
				Set<String> set = jsonObject.keySet();
				Iterator<String> it = set.iterator();
				while(it.hasNext()){
					List<Image> imageList = new ArrayList<>();
					String key = it.next();
					String imageKey = jsonObject.getString(key);
					JSONObject imageJsonObject = JSONObject.parseObject(imageKey);
					JSONArray jsonArray = imageJsonObject.getJSONArray("alts");
					for(int i = 0; i < jsonArray.size(); i++){
						String image = jsonArray.getString(i);
						imageList.add(new Image(IMAGE_URL+image));
					}
					imageMap.put(key,imageList);
				}
			}
			
			Sku sku = new Sku();
			Map<String,String> styleMap = new HashMap<>();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			String prodcut = StringUtils.substringBetween(content, "variantMatrices["+productId+"] = ", "};")+"}";
			if(StringUtils.isNotBlank(prodcut)){
				JSONObject jsonObject = JSONObject.parseObject(prodcut);
				JSONArray jsonArray = jsonObject.getJSONArray("variants");
				for(int i = 0; i < jsonArray.size(); i++){
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject skuJsonObj = jsonArray.getJSONObject(i);
					String sizeVal = skuJsonObj.getString("SIZE_NAME");
					String skuId = skuJsonObj.getString("VARIANT_ID");
					String colorVal = skuJsonObj.getString("COLOR_NAME");
					String skuPrice = skuJsonObj.getString("priceFormatted");
					String instock = skuJsonObj.getString("neverOutOfStock");
					int stock_status = 0;
					if(StringUtils.isNotBlank(instock) && 
							"false".equals(instock)){
						stock_status = 1;
					}
					String skuOrigPrice = StringUtils.EMPTY;
					if(skuJsonObj.containsKey("displayMsrp")){
						skuOrigPrice = skuJsonObj.getString("displayMsrp");
					}
					if(StringUtils.isBlank(skuOrigPrice)){
						skuOrigPrice = skuPrice;
					}
					String currency = StringUtils.substring(skuPrice, 0, 1);
					String unit = Currency.codeOf(currency).name();
					skuPrice = skuPrice.replaceAll("[$, ]", "");
					skuOrigPrice = skuOrigPrice.replaceAll("[$, ]", "");
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setSale_price(Float.parseFloat(skuPrice));
					lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
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
					
					if(StringUtils.isNotBlank(colorVal)){
						if(!styleMap.containsKey(colorVal)){
							LStyleList lStyleList = new LStyleList();
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("Color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							if(colorVal.equals(selectedColor)){
								lStyleList.setDisplay(true);
								 int save = Math.round((1 - Float.parseFloat(skuPrice) / Float.parseFloat(skuOrigPrice)) * 100);// discount
								 rebody.setPrice(new Price(Float.parseFloat(skuOrigPrice), save, Float.parseFloat(skuPrice), unit));
							}
							l_style_list.add(lStyleList);
							List<Image> imageList = imageMap.get(colorVal);
							context.getUrl().getImages().put(skuId, imageList);// picture
						}
					}
					styleMap.put(colorVal, colorVal);
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
			
			String gender = StringUtils.EMPTY;
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements es = doc.select("ul.breadcrumb li.crumb a");
			for(Element e : es){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
					if(StringUtils.containsIgnoreCase(cate, "women")){
						gender = "women";
					}else if(StringUtils.containsIgnoreCase(cate, "men")){
						gender = "men";
					}
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = doc.select(".detailsWrap").text();
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

}
