package com.haitao55.spider.crawler.core.callable.custom.netaporter;

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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;

/**
 * date : 2017-4-5
 * Netaporter 网站收录
 * @author denghuan
 *
 */
public class Netaporter extends AbstractSelect{

	private static final String domain = "www.net-a-porter.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String brand = doc.select("a.designer-name span").text();
			String title = doc.select(".product-name").text();
			String productId = StringUtils.substringBetween(content, "productID\" content=\"", "\"");
			String unit = StringUtils.substringBetween(content, "currencyCode: \"", "\",");
			String salePrice = StringUtils.substringBetween(content, "data-price=\"", "\"");
			if(StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.OFFLINE,"itemUrl:"+context.getUrl().toString()+" not found..");
			}
			if(!StringUtils.equals(unit, "GBP")){
	            throw new ParseException(CrawlerExceptionCode.OFFLINE, context.getUrl().toString()+" Netaporter-price-unit-is-not-GBP...");
	        }
			
			//salePrice = salePrice.substring(0, salePrice.lastIndexOf("00"));
			java.text.DecimalFormat to= new java.text.DecimalFormat("0.00");
			salePrice = to.format(Float.parseFloat(salePrice)/100);
			
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
			Elements es = doc.select("ul.thumbnails li img");
			if(CollectionUtils.isNotEmpty(es)){
				for(Element e : es){
					String image = e.attr("src");
					if(StringUtils.isNotBlank(image)){
						image = image.replace("_xs", "_pp");
						imageList.add(new Image("https:"+image));
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			
			String skus = doc.select("select-dropdown.sku").attr("options");
			if(StringUtils.isNotBlank(skus)){
				//skus = skus.replaceAll("[&quot;]", "\"");
				JSONArray jsonArray = JSONObject.parseArray(skus);
				for(int i = 0; i < jsonArray.size(); i++){
					LSelectionList lSelectionList = new LSelectionList();
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String sizeVal = jsonObject.getString("displaySize");
					String skuId = jsonObject.getString("id");
					String instock = jsonObject.getString("stockLevel");
					int stock_status = 0;
					if(StringUtils.isNotBlank(instock) &&  
							!StringUtils.containsIgnoreCase(instock, "Out_of")){
						stock_status = 1;
					}
					
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setSale_price(Float.parseFloat(salePrice));
					lSelectionList.setOrig_price(Float.parseFloat(salePrice));
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id("default");
					List<Selection> selections = new ArrayList<>();
					if(StringUtils.isNotBlank(sizeVal)){
						Selection selection = new Selection();
						selection.setSelect_name("size");
						selection.setSelect_value(sizeVal);
						selections.add(selection);
					}
					lSelectionList.setSelections(selections);
					
					l_selection_list.add(lSelectionList);
					
					if(display){
						LStyleList lStyleList = new LStyleList();
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_switch_img("");
						lStyleList.setStyle_id("default");
						lStyleList.setStyle_name("default");
						lStyleList.setDisplay(true);
						l_style_list.add(lStyleList);
						display = false;
						rebody.setPrice(new Price(Float.parseFloat(salePrice), 
								0, Float.parseFloat(salePrice), unit));
						context.getUrl().getImages().put(skuId, imageList);// picture
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
				String instock = StringUtils.substringBetween(content, "data-stock=\"", "\"");
				if(StringUtils.isNotBlank(instock) && 
						!StringUtils.containsIgnoreCase(instock, "Out_of")){
					spuStock = 1;
				}
				rebody.setPrice(new Price(Float.parseFloat(salePrice), 
						0, Float.parseFloat(salePrice), unit));
				
				context.getUrl().getImages().put(productId, imageList);// picture
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String cates = StringUtils.substringBetween(content, "category\" content=\"", "\"");
			if(StringUtils.isNotBlank(cates)){
				String[] cs = cates.split("/");
				for(String c : cs){
					cats.add(c);
					breads.add(c);
				}
			}else{
				cats.add(title);
				breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = doc.select(".show-hide-content .wrapper").text();
			
			Elements fs = doc.select("ul.font-list-copy li");
			int count = 0;
			for(Element e : fs){
				String feature = e.text();
				if(StringUtils.isNotBlank(feature)){
					count++;
					featureMap.put("feature-"+count, feature);
				}
			}
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
