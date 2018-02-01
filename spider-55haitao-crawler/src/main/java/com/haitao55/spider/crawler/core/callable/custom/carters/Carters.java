package com.haitao55.spider.crawler.core.callable.custom.carters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.utils.Constants;
public class Carters extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.carters.com";
	@Override
	public void invoke(Context context) throws Exception {
		try{
			String content = super.getInputString(context);
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content);
				String sku_id = context.getCurrentUrl().split(".html")[0].split("_")[1];
				String docid = SpiderStringUtil.md5Encode(domain+sku_id);
				String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
				Elements stock_info = document.select("div[id=primary-nohits]");
				if(!stock_info.isEmpty()){
					retbody.setStock(new Stock(0, 0));
					setOutput(context,retbody);
					return;
				}
				Elements breadcrumb_ret = document.select("div[class=breadcrumb]");
				if(breadcrumb_ret.isEmpty()&&document.select("div[id=primary]").isEmpty()){
					retbody.setStock(new Stock(0, 0));
					setOutput(context,retbody);
					return;
				}
				List<String> breadcrumb_list = new ArrayList<String>();
				for(Element li:breadcrumb_ret){
					if(!li.select("a").isEmpty()){
						if(li.select("a").hasText()){
							breadcrumb_list.add(li.select("a").text());
						}
					}
				}
				Elements main_part = document.select("div[id=primary]");
				if(!main_part.select("p[class=not-available]").isEmpty()||!main_part.select("div[class=product-set-item]").isEmpty()){
					retbody.setStock(new Stock(0, 0));
					setOutput(context,retbody);
					return;
				}
				Elements image_ret = main_part.select("a[class=product-image main-image]");
				List<Image> img_list = new ArrayList<Image>();
				img_list.add(new Image(image_ret.attr("href")));
				String title = document.select("h1[class=product-name]").get(0).text();
				String brand = "Carters";
				String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
//				Elements price_part = document.select("div[class=product-price]");
//				String orig_price = "";
//				String sale_price = "";
//				if(!price_part.select("span[class=price-standard]").isEmpty()){
//					Elements orig_price_ret = price_part.select("span[class=price-standard]");
//					if(!orig_price_ret.isEmpty()){
//						orig_price = orig_price_ret.get(0).text().split("\\$")[1];
//						sale_price = orig_price;
//						}
//					Elements sale_price_part = price_part.select("span[class=price-sales ]");
//					if(sale_price_part.hasText()){
//						sale_price = sale_price_part.get(0).text().replace("$", "").split("-")[1];
//						if(orig_price.isEmpty()){
//								orig_price = sale_price;
//							}
//						}
//					unit = "USD";
//					if(orig_price.contains("-")){
//						orig_price = orig_price.split("-")[1].replace("$", "");
//					}
//					if(sale_price.contains("-")){
//						sale_price = sale_price.split("-")[1].replace("$", "");
//					}
//					}
//				else{
//					sale_price = price_part.select("span[itemprop=price]").get(0).text().split("-")[1].replace("$", "");
//					orig_price = sale_price;
//					unit = "USD";
//				}
				
				String salePrice = document.select(".product-detail .product-price-container span.price-sales").text();
				String origPrice = document.select(".product-detail .product-price-container span.price-standard span.msrp").text();
				if(StringUtils.isBlank(salePrice)){
					throw new ParseException(CrawlerExceptionCode.OFFLINE,
							"carters.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
				}
				
				if(StringUtils.isBlank(origPrice)){
					origPrice = salePrice;
				}
				salePrice = pattern(salePrice);
				origPrice = pattern(origPrice);
				
				float sale_price_converted = Float.valueOf(salePrice);
				float orig_price_converted = Float.valueOf(origPrice);
				Elements product_variations = document.select("div[class~=product-variations.*]");
				int stock_flag = 0;
				List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
				List<LStyleList> l_style_list = new ArrayList<LStyleList>();
				if(!product_variations.isEmpty()){
					HashMap<String,Element> attr_dict = new HashMap<String, Element>();
					for(Element e:product_variations.select("li[class~=attribute.*]")){
						if(e.select("span[class~=label.*]").text().toLowerCase().contains("size")){
							attr_dict.put("size", e);
						}
						if(e.select("span[class~=label.*]").text().toLowerCase().contains("color")){
							attr_dict.put("color", e);
						}
						if(e.select("span[class~=label.*]").text().toLowerCase().contains("fit")){
							attr_dict.put("fit", e);
						}
					}
					if(attr_dict.containsKey("color")){ 
						LStyleList lStyleList = new LStyleList();
						lStyleList.setDisplay(true);
						lStyleList.setGood_id(sku_id);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id(attr_dict.get("color").select("span[class=selectedColor desktopvisible]").text());
						lStyleList.setStyle_name(attr_dict.get("color").select("span[class=selectedColor desktopvisible]").text());
						lStyleList.setStyle_switch_img("");
						context.getUrl().getImages().put(sku_id, img_list);
						l_style_list.add(lStyleList);
					}
					if(attr_dict.containsKey("size")){
						for(Element e:attr_dict.get("size").select("li[class=emptyswatch]")){
							 LSelectionList lSelectionList = new LSelectionList();
							 if(l_style_list.size()>0){
								 lSelectionList.setStyle_id(l_style_list.get(0).getStyle_id());
							 }
							 else{
								 lSelectionList.setStyle_id("0");
							 }
							 lSelectionList.setPrice_unit("USD");
							 lSelectionList.setSale_price(sale_price_converted);
							 lSelectionList.setOrig_price(orig_price_converted);
							 lSelectionList.setGoods_id(sku_id);
							 if(!e.attr("class").contains("unselectable")){
								 lSelectionList.setStock_status(1);
								 stock_flag =1;
							 }
							 lSelectionList.setStock_number(0);
							 Selection selection_size = new Selection();
							 selection_size.setSelect_id(0);
							 selection_size.setSelect_name("Size");
							 selection_size.setSelect_value(e.select("a").attr("title"));
							 List<Selection> selections = new LinkedList<Selection>();
							 selections.add(selection_size);
							 lSelectionList.setSelections(selections);
							 l_selection_list.add(lSelectionList);
						}
					}
				
			}
				Elements desc_ret = document.select("meta[name=description]");
				String description = "";
				if(!desc_ret.isEmpty()){
					description = desc_ret.attr("content");
				}
				int save = Math.round((1 - sale_price_converted / orig_price_converted) * 100);
		        retbody.setBrand(new Brand(brand, ""));
		        retbody.setBreadCrumb(breadcrumb_list);
		        retbody.setCategory(breadcrumb_list);
		        HashMap<String, Object> desc_map = new HashMap<String, Object>();
		        desc_map.put("en", description);
		        retbody.setDescription(desc_map);
		        retbody.setDOCID(docid);
		        retbody.setPrice(new Price(orig_price_converted, save, sale_price_converted, unit));
		        Sku sku = new Sku();
		        sku.setL_style_list(l_style_list);
		        sku.setL_selection_list(l_selection_list);
		        retbody.setSku(sku);
		        retbody.setStock(new Stock(stock_flag, 0));
		        retbody.setSite(new Site(domain));
		        retbody.setTitle(new Title(title, ""));
		        context.getUrl().getImages().put(sku_id, img_list);
		        retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
		        setOutput(context,retbody);
		  }
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Error while crawling url {} ,exception {}", context.getCurrentUrl(),e);
		}
	}
	
	private  String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+))");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}

}
