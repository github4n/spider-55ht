package com.haitao55.spider.crawler.core.callable.custom.unineed;

import java.util.ArrayList;
import java.util.Date;
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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * 
 * unineed.com网站收录
 * date : 2017-01-12
 * @author denghuan
 *
 */
public class Unineed extends AbstractSelect{

	private static final String domain = "www.unineed.com";

	@Override
	public void invoke(Context context) throws Exception {
		String curl = context.getCurrentUrl();
		String cates = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(curl, "?")){
			cates = curl.substring(curl.indexOf("?")+1);
		}
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		
		Sku sku = new Sku();
		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product-name h1").text();
			String origPrice = doc.select(".stock_delivery .price-box p.old-price span.price").text();
			String salePrice = doc.select(".stock_delivery .price-box p.special-price span.price").text();
			String regularPrice = doc.select(".stock_delivery .price-box span.regular-price span").text();
			String brand = StringUtils.substringBetween(content, "<p>Brand:", "<");
			if(StringUtils.isBlank(brand)){
				brand = StringUtils.substringBetween(content, "<td class=\"data\">", "<");
			}
			String unit = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(salePrice) && 
					StringUtils.isNotBlank(origPrice)){
				unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[£, ]", "");
				origPrice = origPrice.replaceAll("[£, ]", "");
				int save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.valueOf(origPrice), save, Float.valueOf(salePrice), unit));
			}else if(StringUtils.isBlank(salePrice) && 
					StringUtils.isNotBlank(regularPrice)){
				unit = getCurrencyValue(regularPrice);// 得到货币代码
				regularPrice = regularPrice.replaceAll("[£, ]", "");
				rebody.setPrice(new Price(Float.valueOf(regularPrice), 0, Float.valueOf(regularPrice), unit));
			}
			
			int stock_status = 0;
			String stock = doc.select("p.availability span").text();
			if(StringUtils.isNotBlank(stock) && 
					StringUtils.containsIgnoreCase(stock, "In stock")){
				stock_status = 1;
			}
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			Elements es = doc.select(".product-essential .product-img-box .jcarousel-clip ul li.jcarousel-item a");
			List<Image> images = new ArrayList<>();
			for(Element e : es){
				String image = e.attr("href");
				if(StringUtils.isNotBlank(image)){
					images.add(new Image(image));
				}
			}
			String options = StringUtils.substringBetween(content, "options\":", "}},");
			if(StringUtils.isNotBlank(options)){
				boolean display = true;
				JSONArray jsonArray = JSONArray.parseArray(options);
				for(int i = 0;i < jsonArray.size(); i++){
					LSelectionList lSelectionList = new LSelectionList();
					LStyleList lStyleList = new LStyleList();
					JSONObject optionsObject =  jsonArray.getJSONObject(i);
					String skuId = optionsObject.getString("id");
					String size = optionsObject.getString("label");
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setPrice_unit(unit);
					if(StringUtils.isNotBlank(salePrice) && 
							StringUtils.isNotBlank(origPrice)){
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setOrig_price(Float.parseFloat(origPrice));
					}else if(StringUtils.isBlank(salePrice) && 
							StringUtils.isNotBlank(regularPrice)){
						lSelectionList.setSale_price(Float.parseFloat(regularPrice));
						lSelectionList.setOrig_price(Float.parseFloat(regularPrice));
					}
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id("default");
					List<Selection> selections = new ArrayList<>();
					Selection selection = new Selection();
					selection.setSelect_name("size");
					selection.setSelect_value(size);
					selections.add(selection);
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
					if(display){
						 display = false;
						 lStyleList.setGood_id(skuId);
						 lStyleList.setDisplay(true);
						 lStyleList.setStyle_cate_name("color");
						 lStyleList.setStyle_cate_id(0);
						 lStyleList.setStyle_name("default");
						 lStyleList.setStyle_id("default");
						 lStyleList.setStyle_switch_img("");
						 context.getUrl().getImages().put(skuId, images);// picture
						 l_style_list.add(lStyleList);
					}
				}
			}
			
			if(CollectionUtils.isEmpty(l_selection_list)){
				context.getUrl().getImages().put(String.valueOf(new Date().getTime()), images);// picture
			}
			rebody.setStock(new Stock(stock_status));
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			if(StringUtils.isNotBlank(cates)){
				if(StringUtils.containsIgnoreCase(cates, ">")){
					String[] cate = cates.split(">");
					for(String s : cate){
						 cats.add(s);
						 breads.add(s);
					}
				}else{
					 cats.add(cates);
					 breads.add(cates);
				}
			}else{
				 cats.add("home");
				 cats.add(title);
				 breads.add("home");
				 breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			String desc = doc.select(".addtional-info .tabcontent .product-tabs-content-inner").text();
			if(StringUtils.isBlank(desc)){
				desc = doc.select(".short-description").text();
			}
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			featureMap.put("feature-1", desc);
			
			rebody.setProperties(propMap);
			rebody.setFeatureList(featureMap);
			descMap.put("en", desc);
			rebody.setDescription(descMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		if (StringUtils.isBlank(currency)) {
			return unit;
		}
		unit = Currency.codeOf(currency).name();
		return unit;

	}
}
