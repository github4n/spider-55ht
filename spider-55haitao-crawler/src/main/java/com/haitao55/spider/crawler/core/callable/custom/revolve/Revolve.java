package com.haitao55.spider.crawler.core.callable.custom.revolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.JsoupUtils;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * Revolve收录
 * date : 2017-3-30
 * @author denghuan
 *
 */
public class Revolve extends AbstractSelect{
	
	private static final String domain = "www.revolve.com";
	private static final String BASE_URL = "http://www.revolve.com";

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc  = JsoupUtils.parse(content);
			String brand = StringUtils.substringBetween(content, "brandName = '", "';");
			String title = StringUtils.substringBetween(content, "br_data.prod_name = \"", "\";");
			String unit = StringUtils.substringBetween(content, "currency: '", "'");
			String instock = StringUtils.substringBetween(content, "availability\" content=\"", "\"");
			String productId = StringUtils.substringBetween(content, "productID\" content=\"", "\"");
			String salePrice = doc.select(".prices--md .prices__retail").text();
			if(StringUtils.isBlank(salePrice)){
				salePrice = doc.select(".prices .u-margin-r--xs").text();
			}
			String origPrice = doc.select(".prices--md .prices__retail--strikethrough").text();
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			
			String	docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			List<Image> images = new ArrayList<>();
			Elements imageEs = doc.select("#js-primary-slideshow__pager a.js-primary-slideshow__pager-thumb");
			for(Element e : imageEs){
				String image = e.attr("data-zoom-image");
				if(StringUtils.isNotBlank(image)){
					images.add(new Image(image));
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements es = doc.select("ul.ui-list li.product-swatches__swatch");
			if(CollectionUtils.isNotEmpty(es)){
					String selectedColor = doc.select("ul.ui-list li.is-toggled button img").attr("alt");
					for(Element e : es){
						String colorVal = e.select("button img").attr("alt");
						String skuUrl = e.attr("onclick");
						if(StringUtils.isBlank(skuUrl) || StringUtils.isBlank(colorVal)){
							break;
						}
						skuUrl = StringUtils.substringBetween(skuUrl, "colorClicked('", "')");
						String html = getContent(context,BASE_URL+skuUrl);
						Document skuDoc = JsoupUtils.parse(html);
						boolean display = false;
						if(colorVal.equals(selectedColor)){
							display = true;
						}
						getSkus(l_selection_list,l_style_list,skuDoc,colorVal,unit,display,rebody,context,html);
					}
			}else{
				//String colorVal = es.get(0).select("button image").attr("alt");
				String colorVal = doc.select("span.selectedColor").text();
				if(StringUtils.isNotBlank(colorVal)){
					getSkus(l_selection_list,l_style_list,doc,colorVal,unit,true,rebody,context,content);
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
				if(StringUtils.isNotBlank(salePrice)){
					salePrice = salePrice.replaceAll("[$, ]", "");
					origPrice = origPrice.replaceAll("[$, ]", "");
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), 
							save, Float.parseFloat(salePrice), unit));
				}
				context.getUrl().getImages().put(productId, images);// picture
				if("InStock".equals(instock)){
					spuStock = 1;
				}
			}
			rebody.setStock(new Stock(spuStock));
		
			String gender = StringUtils.EMPTY;
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select(".crumbs__text");
			for(Element e : cates){
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
			String  description = doc.select("ul.product-details__list").text();
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

	/**
	 * 封装SKU
	 * @param l_selection_list
	 * @param l_style_list
	 * @param doc
	 * @param colorVal
	 * @param unit
	 * @param display
	 * @param rebody
	 * @param context
	 */
	private void getSkus(List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,
			Document doc,String colorVal,String unit,boolean display,RetBody rebody,Context context,String html){
		
		List<Image> images = new ArrayList<>();
		Elements imageEs = doc.select("#js-primary-slideshow__pager a.js-primary-slideshow__pager-thumb");
		for(Element e : imageEs){
			String image = e.attr("data-zoom-image");
			if(StringUtils.isNotBlank(image)){
				images.add(new Image(image));
			}
		}
		
		String salePrice = doc.select(".prices .u-margin-r--xs").text();
		String origPrice = doc.select(".prices--md .prices__retail--strikethrough").text();
		if(StringUtils.isBlank(salePrice)){
			salePrice = doc.select(".prices--md span.prices__retail").text();
		}
		if(StringUtils.isBlank(origPrice)){
			origPrice = salePrice;
		}
		salePrice = salePrice.replaceAll("[$, ]", "");
		origPrice = origPrice.replaceAll("[$, ]", "");
		
		String lSkuId = StringUtils.EMPTY;
		LStyleList lStyleList = new LStyleList();
		if(display){
			lStyleList.setDisplay(display);
			if(StringUtils.isNotBlank(salePrice)){
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), 
						save, Float.parseFloat(salePrice), unit));
			}
		}
		
		Elements es = doc.select("#size-ul .size-options__item .size-options__radio");
		if(CollectionUtils.isNotEmpty(es)){
			for(Element e : es){
				LSelectionList lSelectionList = new LSelectionList();
				String instock = e.attr("data-qty");
				String sizeVal = e.attr("data-size");
				String skuId = colorVal+sizeVal;
				lSelectionList.setGoods_id(skuId);
				lSelectionList.setPrice_unit(unit);
				lSelectionList.setSale_price(Float.parseFloat(salePrice));
				lSelectionList.setOrig_price(Float.parseFloat(origPrice));
				int stock_status = 0;
				if(StringUtils.isNotBlank(instock) && 
						Integer.parseInt(instock) > 0){
					stock_status = 1;
				}
				lSkuId = skuId;
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
			}
		}else{
			LSelectionList lSelectionList = new LSelectionList();
			String instock = StringUtils.substringBetween(html, "availability\" content=\"", "\"");
			lSelectionList.setGoods_id(colorVal);
			lSelectionList.setPrice_unit(unit);
			lSelectionList.setSale_price(Float.parseFloat(salePrice));
			lSelectionList.setOrig_price(Float.parseFloat(origPrice));
			int stock_status = 0;
			if("InStock".equals(instock)){
				stock_status = 1;
			}
			lSelectionList.setStock_status(stock_status);
			lSelectionList.setStyle_id(colorVal);
			List<Selection> selections = new ArrayList<>();
			lSelectionList.setSelections(selections);
			l_selection_list.add(lSelectionList);
		}
		if(StringUtils.isBlank(lSkuId)){
			lSkuId = colorVal;
		}
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_cate_name("color");
		lStyleList.setStyle_id(colorVal);
		lStyleList.setStyle_name(colorVal);
		lStyleList.setStyle_switch_img("");
		lStyleList.setGood_id(String.valueOf(lSkuId));
		context.getUrl().getImages().put(lSkuId, images);// picture
		l_style_list.add(lStyleList);
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
