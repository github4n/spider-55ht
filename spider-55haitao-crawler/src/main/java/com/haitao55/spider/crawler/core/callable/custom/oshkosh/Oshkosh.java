package com.haitao55.spider.crawler.core.callable.custom.oshkosh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
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
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;

/**
 * 
 * @author denghuan
 *
 */
public class Oshkosh extends AbstractSelect{

	private static final String domain = "www.oshkosh.com";
	private static final String BRAND = "Oshkosh";
	
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String productId = StringUtils.substringBetween(content, "ProductID',\"", "\"");
			String title = doc.select("h1.product-name").get(0).text();
			String unit = StringUtils.substringBetween(content, "currency\" content=\"", "\"");
			String salePrice = doc.select(".product-detail .product-price-container span.price-sales").text();
			String origPrice = doc.select(".product-detail .product-price-container span.price-standard span.msrp").text();
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,
						"oshkosh.com itemUrl:" + context.getUrl().toString() + "  this item is offline....");
			}
			
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
			rebody.setBrand(new Brand(BRAND, ""));
			
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			salePrice = pattern(salePrice);
			origPrice = pattern(origPrice);
			
			List<Image> img_list = new ArrayList<Image>();
			String image = doc.select("img.primary-image").attr("src");
			if(StringUtils.isNotBlank(image)){
				img_list.add(new Image(image));
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			Elements sizeEs = null;
			int count = doc.select(".product-variations ul li.attribute .value ul.size").size();
			if(count > 1){
				Element ses = doc.select(".product-variations ul li.attribute .value ul.size").get(0);
				sizeEs = ses.select("li");
			}else{
				sizeEs = doc.select(".product-variations ul li.attribute .value ul.size li");
			}
			
			int colorCount = doc.select(".product-variations ul li.attribute .value ul.color").size();
			Elements colorEs = null;
			if(colorCount > 1){
				Element ces = doc.select(".product-variations ul li.attribute .value ul.color").get(0);
				colorEs = ces.select("li");
			}else{
				colorEs = doc.select(".product-variations ul li.attribute .value ul.color li");
			}
			
			
			String defalutColor = doc.select(".product-variations ul li.attribute .value ul.color li.selected a").attr("title");
			
			if(CollectionUtils.isNotEmpty(colorEs)){
				for(Element color : colorEs){
					LStyleList lStyleList = new LStyleList();
					
					String colorVal = color.select("a").attr("title");
					String images = color.select("a").attr("data-lgimg");
					
					//String defalutColor = color.select(".selected a").attr("title");
					
					String skuImage = StringUtils.EMPTY;
					if(StringUtils.isNotBlank(images)){
						skuImage = StringUtils.substringBetween(images, "url\":\"", "\",");
					}
					
					String tempSkuId = color.select("a").attr("data-masterproductid");
					
					String colorSkuId = StringUtils.EMPTY;
					
					for(Element size : sizeEs){
						LSelectionList lSelectionList = new LSelectionList();
						String sizeVal = size.select("a").attr("title");
						String skuId = size.select("a").attr("data-masterproductid");
						String esStock = size.attr("class");
						lSelectionList.setGoods_id(skuId+colorVal+sizeVal);
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStyle_id(colorVal);
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
						lSelectionList.setSelections(selections);
						int stock_status = 0;
						if(!StringUtils.containsIgnoreCase(esStock, "unselectable")){
							stock_status = 1;
						}
						lSelectionList.setStock_status(stock_status);
						
						colorSkuId = skuId+colorVal+sizeVal;
						l_selection_list.add(lSelectionList);
					}	
					
					if(StringUtils.isBlank(colorSkuId)){
						colorSkuId = tempSkuId;
					}
					
					lStyleList.setGood_id(colorSkuId);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_cate_name("color");
					lStyleList.setStyle_id(colorVal);
					lStyleList.setStyle_name(colorVal);
					lStyleList.setStyle_switch_img("");
					if(StringUtils.isNotBlank(defalutColor) && 
							defalutColor.equals(colorVal)){
						lStyleList.setDisplay(true);
						context.getUrl().getImages().put(colorSkuId, img_list);
					}else{
						List<Image> skuImages = new ArrayList<Image>();
						skuImages.add(new Image(skuImage));
						if(StringUtils.isNotBlank(skuImage)){
							context.getUrl().getImages().put(colorSkuId, skuImages);
						}
						
					}
					l_style_list.add(lStyleList);
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
					rebody.setStock(new Stock(spuStock));
				}
				
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				Elements es = doc.select(".breadcrumb ul li a");
				for(int i = 0; i < es.size(); i++){
					String cate = es.get(i).text();
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
					}
				}
				if(CollectionUtils.isEmpty(es)){
					cats.add(title);
					breads.add(title);
				}
				
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				
				Elements desc_ret = doc.select("meta[name=description]");
				String description = "";
				if(!desc_ret.isEmpty()){
					description = desc_ret.attr("content");
				}
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
