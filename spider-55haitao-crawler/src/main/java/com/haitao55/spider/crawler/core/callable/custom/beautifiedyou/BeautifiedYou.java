package com.haitao55.spider.crawler.core.callable.custom.beautifiedyou;

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
 * BeautifiedYou 网站收录
 * date : 2017-3-29
 * @author denghuan
 *
 */
public class BeautifiedYou extends AbstractSelect{

	private static final String domain = "www.beautifiedyou.com";
	
	private static final String BY_API = "https://www.beautifiedyou.com/remote.php?";

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".ProductMain h1").text();
			String brand = doc.select("h4.BrandName a").text();
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			String salePrice = doc.select("span.VariationProductPrice").text();
			String productId = StringUtils.substringBetween(content, "product_id\" value=\"", "\"");
			
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
			
			List<Image> imageList = new ArrayList<>();
			Elements es  = doc.select(".ProductTinyImageList ul li a img");
			for(Element e : es){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					image = image.replace("60.79", "450.800");
					imageList.add(new Image(image));
				}
			}
			String attr = doc.select("select.validation").attr("name");
			
			boolean isColor = true;
			String skuName = doc.select(".productAttributeLabel label span.name").text();
			if(StringUtils.isNotBlank(skuName)){
				if(StringUtils.containsIgnoreCase(skuName, "size")){
					isColor = false;
				}
			}
			boolean display = true;
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements skuEs = doc.select("select.validation option");
			if(CollectionUtils.isNotEmpty(skuEs)){
				for(Element e : skuEs){
					String name = e.text();
					String selectedColor = e.select("[selected=selected]").text();
					String skuId = e.attr("value");
					if(StringUtils.isNotBlank(skuId)){
						String prodDetailUrl = BY_API+"product_id="+productId+"&action=add&variation_id=&"+attr+"="+skuId+"&w=getProductAttributeDetails"; 
						String html = getByApiInterface(context,prodDetailUrl);
						if(StringUtils.isNotBlank(html)){
							LSelectionList lSelectionList = new LSelectionList();
							JSONObject jsonObject = JSONObject.parseObject(html);
							String details = jsonObject.getString("details");
							JSONObject detailsJsonObject = JSONObject.parseObject(details);
							String instock = detailsJsonObject.getString("instock");
							String sId = detailsJsonObject.getString("sku");
							String skuSalePrice = detailsJsonObject.getString("price");
							String baseImage = detailsJsonObject.getString("baseImage");
							String image = detailsJsonObject.getString("image");
							
							List<Image> images = new ArrayList<>();
							if(StringUtils.isNotBlank(image)){
								images.add(new Image(image));
							}
							if(StringUtils.isNotBlank(baseImage)){
								images.add(new Image(baseImage));
							}
							
							if(StringUtils.isNotBlank(skuSalePrice)){
								skuSalePrice = skuSalePrice.replaceAll("[$, ]", "");
							}
							int stock_status = 0;
							if("true".equals(instock)){
								stock_status = 1;
							}
							
							lSelectionList.setGoods_id(sId);
							lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
							lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setStock_status(stock_status);
							List<Selection> selections = new ArrayList<>();
							if(isColor){
								LStyleList lStyleList = new LStyleList();
								lStyleList.setGood_id(sId);
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_switch_img("");
								lStyleList.setStyle_id(name);
								lStyleList.setStyle_name(name);
								if(StringUtils.isNotBlank(selectedColor)){
									lStyleList.setDisplay(true);
									rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
											0, Float.parseFloat(skuSalePrice), unit));
									context.getUrl().getImages().put(sId, imageList);// picture
								}else{
									context.getUrl().getImages().put(sId, images);// picture
								}
								lSelectionList.setStyle_id(name);
								l_style_list.add(lStyleList);
							}else{
								lSelectionList.setStyle_id("default");
								Selection selection = new Selection();
								selection.setSelect_name("size");
								selection.setSelect_value(name);
								selections.add(selection);
								if(display){
									LStyleList lStyleList = new LStyleList();
									lStyleList.setGood_id(sId);
									lStyleList.setStyle_cate_id(0);
									lStyleList.setStyle_cate_name("color");
									lStyleList.setStyle_switch_img("");
									lStyleList.setStyle_id("default");
									lStyleList.setStyle_name("default");
									lStyleList.setDisplay(true);
									display = false;
									rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
											0, Float.parseFloat(skuSalePrice), unit));
									context.getUrl().getImages().put(sId, imageList);// picture
									l_style_list.add(lStyleList);
								}
							}
							lSelectionList.setSelections(selections);
							l_selection_list.add(lSelectionList);
							
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
				if(StringUtils.isNotBlank(salePrice)){
					salePrice = salePrice.replaceAll("[$, ]", "");
					rebody.setPrice(new Price(Float.parseFloat(salePrice), 
							0, Float.parseFloat(salePrice), unit));
				}
				context.getUrl().getImages().put(productId, imageList);// picture
				String cart = doc.select(".add-to-cart").attr("value");
				if(StringUtils.isNotBlank(cart) && 
						StringUtils.containsIgnoreCase(cart, "Add To Cart")){
					spuStock = 1;
				}
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select("#ProductBreadcrumb ul li a");
			for(int i = 0; i < cates.size(); i++){
				String cate = cates.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = doc.select(".ProductDescriptionContainer").text();
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
	
	private String getByApiInterface(Context context,String url) throws ClientProtocolException, HttpException, IOException{
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
