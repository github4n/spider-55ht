package com.haitao55.spider.crawler.core.callable.custom.dermstore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;


/**
 * Dermstore网站收录
 * date:2017-4-24
 * @author denghuan
 *
 */

public class Dermstore extends AbstractSelect{

	private static final String domain = "www.dermstore.com";
	private String IMAGE_URL ="http://media.dermstore.com/catalog/#brandId#/500x500/#porId#.jpg";
	private boolean debug = false;
	
	@Override
	public void invoke(Context context) throws Exception {
	    String url = context.getUrl().getValue();
	    String content = StringUtils.EMPTY;
	    if(debug){
	       content = Crawler.create().timeOut(10000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).url(context.getUrl().toString()).resultAsString();
	    } else {
	        String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
            if(StringUtils.isNotBlank(proxyRegionId)){
                Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
                String proxyAddress=proxy.getIp();
                int proxyPort=proxy.getPort();
                content = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
                        .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
            } else {
                content = super.getInputString(context);
            }
	    }
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String brand = doc.select(".brand").text();
			String title = doc.select(".prod-name").text();
			String productId = StringUtils.substringBetween(content, "prodID\":", ",");
			String origPrice = StringUtils.substringBetween(content, "List price: <strike>", "</strike>");
			String salePrice = StringUtils.substringBetween(content, "price\" content=\"", "\"");
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			
			origPrice = origPrice.replaceAll("[$, ]", "");
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(productId)){
				docid = SpiderStringUtil.md5Encode(domain+productId);
			}else{
				docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			}
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
			String selectedSize  = doc.select("#sizeOptions a.selected").text();
			if(StringUtils.isNotBlank(selectedSize)){
				selectedSize = selectedSize.replace("\\s*", "");
			}
			List<Image> imageList = new ArrayList<Image>();
			Elements imageEs = doc.select("#prod-gallery a");
			for(Element e : imageEs){
				String image = e.attr("data-image");
				if(StringUtils.isNotBlank(image) && 
						!StringUtils.containsIgnoreCase(image, "video_icon")){
					imageList.add(new Image(image));
				}
			}
			
			if(CollectionUtils.isEmpty(imageEs)){
				Elements images = doc.select("#prod-media img#fea-photo-img");
				for(Element e : images){
					String image = e.attr("src");
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<String,String>();
			//boolean display = true;
			
			String selectColor = doc.select("#color-swatches a.selected img").attr("alt");
			Elements colorEs = doc.select("#color-swatches .owl-item a");
			for(Element e : colorEs){
				String durl = e.attr("href");
				String colorVal = e.select("img").attr("alt");
				String html = getContnet("http://"+domain+durl,context);
				if(StringUtils.isNotBlank(html)){
					LSelectionList lSelectionList = new LSelectionList();
					LStyleList lStyleList = new LStyleList();
					setSku(l_selection_list, lSelectionList, l_style_list, lStyleList, html,colorVal,selectColor,unit,context);
				}
			}
			if(CollectionUtils.isEmpty(colorEs)){
				Elements es  = doc.select("#sizeOptions script");
				for(Element e : es){
					String sizeJson = e.toString();
					if(StringUtils.isNotBlank(sizeJson)){
						String json = StringUtils.substringBetween(sizeJson, "console.log(", ");");
						JSONObject jsonObject = JSONObject.parseObject(json);
						String all = jsonObject.getString("all");
						String producId = jsonObject.getString("c");
						JSONObject allObject = JSONObject.parseObject(all);
						Set<String> set = allObject.keySet();
						Iterator<String> it = set.iterator();
						while(it.hasNext()){
							String key = it.next();
							String keyJson = allObject.getString(key);
							JSONObject keyObject = JSONObject.parseObject(keyJson);
							String skuSalePrice = keyObject.getString("prod_price");
							String colorVal = keyObject.getString("prod_color");
							String prod_size = keyObject.getString("prod_size");
							String prod_measure = keyObject.getString("prod_measure");
							String brand_id = keyObject.getString("brand_id");
							String inventory_status = keyObject.getString("prod_stock_available");
							String prod_id = keyObject.getString("prod_id");
							String prod_status = keyObject.getString("prod_status");
							String variation = StringUtils.EMPTY;
							if(keyObject.containsKey("variation")){
								variation = keyObject.getString("variation");
							}
							if(StringUtils.isBlank(variation)){
								variation = prod_size;
							}
							
							String skuId = prod_id+colorVal+prod_size;
							String sizeVal = prod_size+prod_measure;
							int stock_status = 0;
							if(StringUtils.isNotBlank(inventory_status) && 
									Integer.parseInt(inventory_status) > 0){
								stock_status = 1;
							}
							if(StringUtils.isNotBlank(prod_status) && 
									prod_status.equals("3")){
								stock_status = 1;
							}
							
							if(!styleMap.containsKey(sizeVal)){
								if(StringUtils.isNotBlank(sizeVal)){
									LSelectionList lSelectionList = new LSelectionList();
									lSelectionList.setGoods_id(skuId);
									lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
									lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
									lSelectionList.setStock_status(stock_status);
									lSelectionList.setPrice_unit(unit);
									List<Selection> selections = new ArrayList<>();
									lSelectionList.setSelections(selections);
									lSelectionList.setStyle_id(sizeVal);
									l_selection_list.add(lSelectionList);
									
									LStyleList lStyleList = new LStyleList();
									
									lStyleList.setGood_id(skuId);
									lStyleList.setStyle_cate_name("size");
									lStyleList.setStyle_cate_id(0);
									lStyleList.setStyle_switch_img("");
									lStyleList.setStyle_id(sizeVal);
									lStyleList.setStyle_name(sizeVal);
									l_style_list.add(lStyleList);
									if(StringUtils.isNotBlank(selectedSize)
											&& StringUtils.containsIgnoreCase(selectedSize, variation)){
										lStyleList.setDisplay(true);
										if(StringUtils.isNotBlank(skuSalePrice)){
											rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
													0, Float.parseFloat(skuSalePrice), unit));
										}
											context.getUrl().getImages().put(skuId, imageList);// picture
									}else{
										List<Image> skuImageList = new ArrayList<Image>();
										String image = IMAGE_URL.replace("#brandId#", brand_id).replace("#porId#", producId);
										skuImageList.add(new Image(image));
										context.getUrl().getImages().put(skuId, skuImageList);// picture
									}		
								}
							  }
								styleMap.put(sizeVal, sizeVal);
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
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
				String instock = StringUtils.substringBetween(content, "availability\" content=\"", "\"");
				if(StringUtils.isNotBlank(instock) && 
						instock.equals("instock")){
					spuStock = 1;
				}
				String stock = doc.select(".warning").text();
				if(StringUtils.isNotBlank(stock) && 
						StringUtils.containsIgnoreCase(stock, "Item is expected to")){
					spuStock = 1;
				}
				
				context.getUrl().getImages().put(productId, imageList);// picture
				
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String gender = StringUtils.EMPTY;
			Elements es = doc.select("ol.breadcrumb li a");
			for(int i = 1; i < es.size(); i++){
				String cate = es.get(i).text();
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
			String description = doc.select("#collapseDetails .panel-body").text();
			
			Elements features = doc.select("#collapseGlance .panel-body .prd");
		    int count = 0;
		    if(features != null &&  features.size() > 0){
		    	for(Element e : features){
		    		String key = e.select("span").text();
		    		String value = e.select("a").text();
		    		if(StringUtils.isNotBlank(key)){
		    			count ++;
		    			featureMap.put("feature-"+count, key+value);
		    		}
		    	}
		    }
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			rebody.setProperties(propMap);
			rebody.setSku(sku);
			}
			setOutput(context, rebody);
			if(debug){
			    System.out.println(rebody.parseTo() ); 
			}
		}
	
	private String getContnet(String url,Context context) throws ClientProtocolException, HttpException, IOException{
		String html = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(debug){
		    html = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
		            .proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).resultAsString();
		} else {
		    if(StringUtils.isBlank(proxyRegionId)){
	            html = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
	                    .resultAsString();
	        }else{
	            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
	            String proxyAddress=proxy.getIp();
	            int proxyPort=proxy.getPort();
	            html = Crawler.create().timeOut(60000).url(url).method(HttpMethod.GET.getValue())
	                    .proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
	        }
		}
		return html;
	}
	
	private void setSku(List<LSelectionList> l_selection_list,LSelectionList lSelectionList,List<LStyleList> l_style_list,
			LStyleList lStyleList,String html,String colorVal,String selectedColor,String unit,Context context){
		Document doc = Jsoup.parse(html);
		String skuId = StringUtils.substringBetween(html, "prodID\":", ",");
		String instock = StringUtils.substringBetween(html, "availability\" content=\"", "\"");
		String salePrice = StringUtils.substringBetween(html, "amount\" content=\"", "\"");
		int stock_status = 0;
		if(StringUtils.isNotBlank(instock) && 
				instock.equals("instock")){
			stock_status = 1;
		}
		String stock = doc.select(".warning").text();
		if(StringUtils.isNotBlank(stock) && 
				StringUtils.containsIgnoreCase(stock, "Item is expected to")){
			stock_status = 1;
		}
		lSelectionList.setGoods_id(skuId);
		lSelectionList.setOrig_price(Float.parseFloat(salePrice));
		lSelectionList.setSale_price(Float.parseFloat(salePrice));
		lSelectionList.setPrice_unit(unit);
		lSelectionList.setStock_status(stock_status);
		lSelectionList.setStyle_id(colorVal);
		List<Selection> selections = new ArrayList<>();
		lSelectionList.setSelections(selections);
		
		lStyleList.setGood_id(skuId);
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_cate_name("color");
		lStyleList.setStyle_id(colorVal);
		lStyleList.setStyle_switch_img("");
		lStyleList.setStyle_id(colorVal);
		lStyleList.setStyle_name(colorVal);
		if(colorVal.equals(selectedColor)){
			lStyleList.setDisplay(true);
		}
		List<Image> imageList = new ArrayList<Image>();
		Elements imageEs = doc.select("#prod-gallery a");
		for(Element e : imageEs){
			String image = e.attr("data-image");
			if(StringUtils.isNotBlank(image) && 
					!StringUtils.containsIgnoreCase(image, "video_icon")){
				imageList.add(new Image(image));
			}
		}
		if(CollectionUtils.isEmpty(imageEs)){
			Elements images = doc.select("#prod-media img#fea-photo-img");
			for(Element e : images){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
		}
		
		context.getUrl().getImages().put(skuId, imageList);// picture
		
		l_selection_list.add(lSelectionList);
		l_style_list.add(lStyleList);
	}
	
  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }
  public static void main(String[] args) throws Exception {
	   /* Dermstore store = new Dermstore();
	    store.setDebug(true);
        Context context = new Context();
        context.setUrl(new Url("http://www.dermstore.com/product_Lemongrass+Love+Hydration+Spray_68344.htm"));
        store.invoke(context);*/
      String content = Crawler.create().url("http://www.dermstore.com/product_Stay+All+Day+Waterproof+Brow+Color_35650.htm").timeOut(10000).retry(3).proxy(true).proxyAddress("47.88.25.82").proxyPort(3128).resultAsString();
      System.out.println(content);
      
  }
	
}
