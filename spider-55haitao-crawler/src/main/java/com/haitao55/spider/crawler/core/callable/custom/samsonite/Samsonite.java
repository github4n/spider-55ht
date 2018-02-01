package com.haitao55.spider.crawler.core.callable.custom.samsonite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Samsonite 网站收录
 * @author denghuan
 *
 */
public class Samsonite extends AbstractSelect{

	//private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "shop.samsonite.com";
	
	@Override
	public void invoke(Context context) throws Exception {
			String content = this.getInputString(context);
			String url = context.getCurrentUrl();
			RetBody rebody = new RetBody();
			if(StringUtils.isNotBlank(content)){
				//String colorValue = StringUtils.substringBetween(content, "color=", "&");
				String params = StringUtils.substringBetween(content, "cq_params.product = {", "};");
				String productId = StringUtils.substringBetween(params, "id: '", "',");
				String pskuId = StringUtils.substringBetween(params, "sku: '", "'");
				Document docment = Jsoup.parse(content);
				String title = docment.select(".product-name-wrapper h1.product-name").text();
				String origPrice = docment.select("#product-content .product-price span.price-standard").text();
				String salePrice = docment.select("#product-content .product-price span.price-sales").text();
				String stock = docment.select(".availability-msg p.in-stock-msg").text();
				//String colorAttr = docment.select("ul li.attribute span").text();
				Elements esValue  = docment.select(".value ul li a");
				String colorValue = StringUtils.EMPTY;
				if(esValue.size() > 0){
				     colorValue = docment.select(".value ul li a").get(0).attr("title");
				}
				String currentSku = docment.select(".product-variations").attr("data-current");
				String color = StringUtils.substringBetween(currentSku, "displayName\":\"", "\",");
				//String colorValue = StringUtils.substringBetween(currentSku, "value\":\"", "\",");
				String size = docment.select(".size-box label").text();
				String sizeValue = docment.select(".size-box span").text();
				Elements esSkus = docment.select(".value ul li a");
				
				String unit = "";
				if(StringUtils.containsIgnoreCase(salePrice, "$")){
					String currency = StringUtils.substring(salePrice,0,1);
					unit = Currency.codeOf(currency).name();
					salePrice = salePrice.replace("$", "");
				}
				if(StringUtils.containsIgnoreCase(origPrice, "$")){
					origPrice = origPrice.replace("$", "");
				}
				
				if(StringUtils.isBlank(origPrice) && StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling samsonite saleprice error ,url"+context.getUrl().toString());
				}else if(StringUtils.isNotBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
					int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
					rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
				}else if(StringUtils.isBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
					rebody.setPrice(new Price(Float.parseFloat(salePrice), 0, Float.parseFloat(salePrice), unit));
				}
				
				int spuStock = 0;
				if(StringUtils.isNotBlank(stock)){
					if(StringUtils.containsIgnoreCase(stock, "In Stock")){
						spuStock = 1;
					} 
				}
				
				List<Image> images = new ArrayList<Image>();
				Elements es = docment.select("#thumbnails ul li.thumb a");
				if(es != null && es.size() > 0){
					for(Element e : es){
			        	String href = e.attr("href");
			        	if(StringUtils.isNotBlank(href)){
			        		Image image = new Image(href);
				        	images.add(image);
			        	}
			        }
		        }else{
		        	String href = docment.select(".quick-view-product-primary-image a").attr("href");
		        	if(StringUtils.isNotBlank(href)){
			        	Image image = new Image(href);
			        	images.add(image);
		        	}
		        }
				context.getUrl().getImages().put(productId, images);// picture
				
				Sku sku = new Sku();
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				
				if(esSkus != null && esSkus.size() > 0){
					for(int i = 0; i< esSkus.size(); i++){
						LSelectionList lSelectionList = new LSelectionList();
						LStyleList  lStyleList = new LStyleList();
						String skuUrl = esSkus.get(i).attr("href");
						String skuValue = esSkus.get(i).attr("title");
						if(StringUtils.isNotBlank(skuValue) && skuValue.equals(colorValue)){
							List<Selection> selList = new ArrayList<Selection>();
							Selection selection = new Selection();
							lSelectionList.setGoods_id(pskuId);
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setStock_status(spuStock);
							lSelectionList.setStyle_id(colorValue);
							if(StringUtils.containsIgnoreCase(size, ":")){
								size = size.replaceAll(":", "");
							}
							selection.setSelect_name(size);
							if(StringUtils.containsIgnoreCase(sizeValue, "\"")){
								sizeValue = sizeValue.replace("\"", "");
							}
							selection.setSelect_value(sizeValue);
							selList.add(selection);
							lStyleList.setDisplay(true);
							lStyleList.setStyle_switch_img("");
							lStyleList.setGood_id(pskuId);
							lStyleList.setStyle_cate_name(color);
							lStyleList.setStyle_id(colorValue);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_name(colorValue);
							context.getUrl().getImages().put(pskuId, images);// picture
							lSelectionList.setSelections(selList);
							if(StringUtils.isNotBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
								lSelectionList.setOrig_price(Float.parseFloat(origPrice));
								lSelectionList.setSale_price(Float.parseFloat(salePrice));
							}else if(StringUtils.isBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
								lSelectionList.setOrig_price(Float.parseFloat(salePrice));
								lSelectionList.setSale_price(Float.parseFloat(salePrice));
							}
							l_style_list.add(lStyleList);
							l_selection_list.add(lSelectionList);
							
						}else{
							Url currentUrl = new Url(skuUrl);
							currentUrl.setTask(context.getUrl().getTask());
							String skuHtml = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
							getSkuList(l_selection_list,l_style_list,lSelectionList,lStyleList,skuHtml,context);
						}
					}
				}
				sku.setL_selection_list(l_selection_list);
				sku.setL_style_list(l_style_list);
				String docid = SpiderStringUtil.md5Encode(url);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				rebody.setDOCID(docid);
				rebody.setSite(new Site(domain));
				rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
				rebody.setTitle(new Title(title, ""));
				//rebody.setBrand(new Brand(brand, ""));
				
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
				
				//String save = docment.select(".promotion-callout").text();
				Elements cates = docment.select("ol.breadcrumb li a");
				if(cates != null && cates.size() > 0){
					for(Element e :cates){
						String cate = e.text();
						if(StringUtils.isNotBlank(cate)){
							cats.add(cate);
							breads.add(cate);
						}
					}
				}
				String cat = "home/"+title;
				if(CollectionUtils.isEmpty(cats)){
					cats.add(cat);
					breads.add(cat);
				}else{
					if(cats.size() == 1){
						String category = cats.get(0);
						if("Home".equals(category)){
							cats.add(title);
							breads.add(title);
						}
					}
				}
				
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> propMap = new HashMap<String, Object>();
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				
				String description = docment.select("#tab-new2").text();
				String weight = StringUtils.substringBetween(description, "Weight:", ". ");
				String dimensions = StringUtils.substringBetween(description, "Dimensions:", "\" O");
				featureMap.put("feature-" + 1, description);
				if(StringUtils.isNotBlank(weight)){
					propMap.put("Weight", weight);
				}
				if(StringUtils.isNotBlank(dimensions)){
					propMap.put("Dimensions", dimensions);
				}
				descMap.put("en", description);
				
				rebody.setFeatureList(featureMap);
				rebody.setDescription(descMap);
				rebody.setProperties(propMap);
				rebody.setSku(sku);
		  }
			setOutput(context, rebody);
		
	}
	
	//封装SKU
	private void getSkuList(List<LSelectionList> l_selection_list,List<LStyleList> l_style_list,
			LSelectionList lSelectionList,LStyleList  lStyleList,String html,Context context){
		
		List<Selection> selList = new ArrayList<Selection>();
		Document docment = Jsoup.parse(html);
		String params = StringUtils.substringBetween(html, "cq_params.product = {", "};");
		String pskuId = StringUtils.substringBetween(params, "sku: '", "'");
		String currentSku = docment.select(".product-variations").attr("data-current");
		String color = StringUtils.substringBetween(currentSku, "displayName\":\"", "\",");
		String colorValue = StringUtils.substringBetween(currentSku, "value\":\"", "\",");
		String origPrice = docment.select("#product-content .product-price span.price-standard").text();
		String salePrice = docment.select("#product-content .product-price span.price-sales").text();
		String stock = docment.select(".availability-msg p.in-stock-msg").text();
		String size = docment.select(".size-box label").text();
		String sizeValue = docment.select(".size-box span").text();
		Elements es = docment.select("#thumbnails ul li.thumb a");
		
		String unit = "";
		if(StringUtils.containsIgnoreCase(salePrice, "$")){
			String currency = StringUtils.substring(salePrice,0,1);
			unit = Currency.codeOf(currency).name();
			salePrice = salePrice.replace("$", "");
		}
		if(StringUtils.containsIgnoreCase(origPrice, "$")){
			origPrice = origPrice.replace("$", "");
		}
		
		if(StringUtils.isBlank(origPrice) && StringUtils.isBlank(salePrice)){
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR, "Error while crawling samsonite saleprice error ,url"+context.getUrl().toString());
		}else if(StringUtils.isNotBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
			lSelectionList.setOrig_price(Float.parseFloat(origPrice));
			lSelectionList.setSale_price(Float.parseFloat(salePrice));
		}else if(StringUtils.isBlank(origPrice) && StringUtils.isNotBlank(salePrice)){
			lSelectionList.setOrig_price(Float.parseFloat(salePrice));
			lSelectionList.setSale_price(Float.parseFloat(salePrice));
		}
		lSelectionList.setPrice_unit(unit);
		int stockStatus = 0;
		if(StringUtils.isNotBlank(stock)){
			if(StringUtils.containsIgnoreCase(stock, "In Stock")){
				stockStatus = 1;
			} 
		}
		lSelectionList.setStock_status(stockStatus);
		lSelectionList.setGoods_id(pskuId);
		lSelectionList.setStyle_id(colorValue);
		Selection selection = new Selection();
		if(StringUtils.containsIgnoreCase(size, ":")){
			size = size.replaceAll(":", "");
		}
		if(StringUtils.containsIgnoreCase(sizeValue, "\"")){
			sizeValue = sizeValue.replace("\"", "");
		}
		selection.setSelect_name(size);
		selection.setSelect_value(sizeValue);
		selList.add(selection);
		lSelectionList.setSelections(selList);
		l_selection_list.add(lSelectionList);
		lStyleList.setGood_id(pskuId);
		lStyleList.setStyle_switch_img("");
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_cate_name(color);
		lStyleList.setStyle_id(colorValue);
		lStyleList.setStyle_name(colorValue);
		List<Image> images = new ArrayList<Image>();
		if(es != null && es.size() > 0){
			for(Element e : es){
	        	String href = e.attr("href");
	        	if(StringUtils.isNotBlank(href)){
		        	Image image = new Image(href);
		        	images.add(image);
	        	}
	        }
        }else{
        	String href = docment.select(".quick-view-product-primary-image a").attr("href");
        	if(StringUtils.isNotBlank(href)){
        		if(StringUtils.isNotBlank(href)){
	        		Image image = new Image(href);
	        		images.add(image);
        		}
        	}
        }
		context.getUrl().getImages().put(pskuId, images);// picture
		l_style_list.add(lStyleList);
	}

}
