package com.haitao55.spider.crawler.core.callable.custom.rebeccaminkoff;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * @Description:
 * @author: denghuan
 * @date: 2017年10月18日 下午5:53:47
 */
public class Rebeccaminkoff extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.rebeccaminkoff.com";

	
	@Override
	public void invoke(Context context) throws Exception {
		String content = CurlCrawlerUtil.get(context.getCurrentUrl());
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.product-title").text();
			String origPrice = doc.select("span.product-price--compare-at-price").text();
			String salePrice = StringUtils.substringBetween(content, "\"price\":\"", "\"");
			String productId = StringUtils.substringBetween(content, "productId\":", ",");
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\"");
			
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
			rebody.setBrand(new Brand("Rebecca Minkoff", ""));
			
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$,]", "");
			
			Map<String,String> imageMap = new HashMap<>();
			String product = StringUtils.substringBetween(content, "product: ","};");
			if(StringUtils.isNotBlank(product)){
				JSONObject jsonObject = JSONObject.parseObject(product);
				JSONArray jsonArray = jsonObject.getJSONArray("variants");
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject skuJsonObj = jsonArray.getJSONObject(i);
					String color = skuJsonObj.getString("option1");
					String featured_image = skuJsonObj.getString("featured_image");
					if(StringUtils.isNotBlank(featured_image)){
						JSONObject imageJsonObj = JSONObject.parseObject(featured_image);
						String image = imageJsonObj.getString("src");
						if(StringUtils.isNotBlank(color) && StringUtils.isNotBlank(featured_image)){
							imageMap.put(color.toLowerCase(), image);
						}
					}
				}
			}
			
			List<Image> imgList = new ArrayList<>();
			Elements es = doc.select(".product-images .product-image img");
			for(Element e : es){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					imgList.add(new Image("https:"+image));
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			
			String defalutColor = doc.select(".product-qty-size--size .size-select .is-active").attr("data-size-group");
			Elements colorEs = doc.select(".product-qty-size--size .size-select .size-select--group");
			for(Element e : colorEs){
				String colorVal = e.attr("data-size-group");
				Elements skuEs = e.select(".size-select--option");
				String skuId = StringUtils.EMPTY;
				for(Element se : skuEs){
					LSelectionList lSelectionList = new LSelectionList();
					String skuoOrigPrice = se.attr("data-variant-compare-price");
					String skuSalePrice = se.attr("data-monetate-variant-price");
					skuId = se.attr("data-variant-sku");
					String instock = se.attr("data-variant-available");
					String sizeVal = se.select("span").text();
					if(StringUtils.isBlank(skuoOrigPrice)){
						skuoOrigPrice = skuSalePrice;
					}
					skuoOrigPrice = skuoOrigPrice.replaceAll("[$,]", "");
					
					int stock_status = 0;
					if("true".equals(instock)){
						stock_status = 1;
					}
					
					lSelectionList.setGoods_id(skuId);
					lSelectionList.setOrig_price(Float.parseFloat(skuoOrigPrice));
					lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
					lSelectionList.setPrice_unit(unit);
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setStyle_id(colorVal);
					List<Selection> selections = new ArrayList<>();
					Selection selection = new Selection();
					selection.setSelect_name("size");
					selection.setSelect_value(sizeVal);
					selections.add(selection);
					lSelectionList.setSelections(selections);
					l_selection_list.add(lSelectionList);
				}
				
				if(StringUtils.isBlank(skuId)){
					skuId = productId;
				}
				
				LStyleList  lStyleList = new LStyleList();
				lStyleList.setGood_id(skuId);
				lStyleList.setStyle_cate_id(0);
				lStyleList.setStyle_cate_name("color");
				lStyleList.setStyle_id(colorVal);
				lStyleList.setStyle_switch_img("");
				lStyleList.setStyle_name(colorVal);
				if(StringUtils.isNotBlank(colorVal) && defalutColor.equals(colorVal)){
					lStyleList.setDisplay(true);
				}
				
				List<Image> imageList = new ArrayList<>();
				
				if(colorEs.size() == 1){
					if(CollectionUtils.isEmpty(imageList)){
						imageList = imgList;
					}
				}else{
					String image = imageMap.get(colorVal.toLowerCase());
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image(image));
					}
				}
				context.getUrl().getImages().put(skuId, imageList);// picture
				
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
			}else{
				String instock = StringUtils.substringBetween(content, "available: ", ",");
				if(StringUtils.isNotBlank(instock) && !"false".equals(instock)){
					spuStock = 1;
				}
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPrice), save, Float.parseFloat(salePrice), unit));
				

				context.getUrl().getImages().put(productId, imgList);// picture
				
			}
			rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements breadcrumbEs = doc.select(".breadcrumb a");
			for(Element e : breadcrumbEs){
				String cate = e.text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			
			if(CollectionUtils.isEmpty(breads)){
				cats.add(title);
				breads.add(title);
			}
			
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String description = doc.select(".product-description-inner p").text();
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