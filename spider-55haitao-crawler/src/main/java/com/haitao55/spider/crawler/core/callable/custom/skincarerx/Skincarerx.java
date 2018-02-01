package com.haitao55.spider.crawler.core.callable.custom.skincarerx;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年4月19日 下午1:55:54  
 */
public class Skincarerx extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.skincarerx.com";
	public String SKINCARE_API = "https://www.skincarerx.com/variations.json?productId=";
	private static final String CSS_BREADS = "ul.breadcrumbs_container li a";
	private static final String CSS_DESCRIPTION = "div.product-info p";
	private static final String CSS_DETAIL = "div#technicaldetails div.product-more-details table tr";
	private static final String CSS_INVENTORY = "span.product-stock-message";
	private static final String CSS_IMAGES = "ul.product-large-view-thumbs>li.n-unit>a";
	
	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Sale", "Women's Final Sale" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Sale", "Men's Final Sale" };

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		String content = super.getInputString(context);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content);
			String productID = StringUtils.substringBetween(content, "productID: \"", "\"");
			String title = StringUtils.substringBetween(content, "productTitle: \"", "\",");
			String brand = StringUtils.substringBetween(content, "productBrand: \"", "\"");
			String salePrice = StringUtils.substringBetween(content, "productPrice':'", "'");
			String unit = StringUtils.substringBetween(content, "currencyType: '", "'");
			String origPrice = document.select(".price-rrp span").text();
			if (StringUtils.isBlank(salePrice)) {
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"Skincarerx itemUrl:"+context.getUrl().toString()+" not found..");
			}
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$]", "");
			
			
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);

			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			CrawlerUtils.setDescription(featureMap, descMap, document, CSS_DESCRIPTION, CSS_DETAIL);

			//int save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
			//String spu_Unit = CrawlerUtils.getUnit(productPriceStr, currentUrl, logger);
			int spu_stock_status = 0;
			
			//设置spu库存
			Elements es = document.select(CSS_INVENTORY);
			if(CollectionUtils.isNotEmpty(es)){
				String inventoryMsg = StringUtils.trim(es.get(0).text());
				if(StringUtils.containsIgnoreCase(inventoryMsg, "In stock"))
					spu_stock_status = 1;
			}

			Map<String, Object> properties = new HashMap<>();
			// 设置性别
			CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);

			// 设置图片
			List<String> imgs = new ArrayList<>();
			Elements epictures = document.select(CSS_IMAGES);
			if(CollectionUtils.isNotEmpty(epictures)){
				for(Element e : epictures) {
					imgs.add(StringUtils.trimToEmpty(e.absUrl("href")));
				}
			}
			LImageList image_list = CrawlerUtils.getImageList(imgs);
			

			Sku sku = new Sku();
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			boolean display = true;
			Elements skuEs = document.select(".variation-dropdowns select option");
			for(Element e : skuEs){
				String skuId = e.attr("value");
				String colorVal = e.text();
				if(StringUtils.isNotBlank(skuId) && 
						StringUtils.isNotBlank(colorVal)){
					String url = SKINCARE_API+productID+"&option1="+skuId;
					String html = crawlerUrl(context,url);
					if(StringUtils.isNotBlank(html)){
						LSelectionList lSelectionList = new LSelectionList();
						String instock = StringUtils.substringBetween(html, "availabilityPrefix\":\"", "\"");
						String skuSalePrice = StringUtils.substringBetween(html, "price\":\"&#36;", "\"");
						String skuOrigPrice = StringUtils.substringBetween(html, "rrp\":", ",");
						
						JSONObject jsonObject = JSONObject.parseObject(html);
						JSONArray jsonArray = jsonObject.getJSONArray("images");
						List<Image> images = new ArrayList<>();
						for(int i = 0 ; i < jsonArray.size(); i++){
							JSONObject imageJson = jsonArray.getJSONObject(i);
							String zoom = imageJson.getString("type");
							String name = imageJson.getString("name");
							if(StringUtils.isNotBlank(zoom) && 
									StringUtils.equals(zoom, "zoom")){
								images.add(new Image("https://s4.thcdn.com/"+name));
								break;
							}
						}
						
						int stock_status = 0;
						if(StringUtils.isNotBlank(instock) && 
								"In stock".equals(instock)){
							stock_status = 1;
						}
						if(StringUtils.isBlank(skuOrigPrice)){
							skuOrigPrice = skuSalePrice;
						}
						skuOrigPrice = skuOrigPrice.replaceAll("[$]", "");
						skuSalePrice = skuSalePrice.replaceAll("[$]", "");
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(Float.parseFloat(skuOrigPrice));
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStyle_id(colorVal);
						List<Selection> selections = new ArrayList<>();
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
						
						LStyleList lStyleList = new LStyleList();
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id(colorVal);
						lStyleList.setStyle_name(colorVal);
						lStyleList.setStyle_switch_img("");
						if(display){
							lStyleList.setDisplay(true);
							display = false;
						}
						context.getUrl().getImages().put(skuId, images);
						l_style_list.add(lStyleList);
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
				spuStock = spu_stock_status;
				context.getUrl().getImages().put(productID, CrawlerUtils.convertToImageList(imgs));
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				retbody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
			}
			
			retbody.setStock(new Stock(spuStock));
			
			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand(brand, "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setImage(image_list);
			retbody.setSku(sku);
			retbody.setProperties(properties);
		}
		setOutput(context, retbody);
	}
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		Map<String, Object> payload = new HashMap<>();
		payload.put("selected", "1");
		payload.put("variation1", "4");
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).payload(payload).method(HttpMethod.POST.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).payload(payload).method(HttpMethod.POST.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
