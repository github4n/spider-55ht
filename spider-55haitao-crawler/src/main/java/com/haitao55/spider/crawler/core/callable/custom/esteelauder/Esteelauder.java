package com.haitao55.spider.crawler.core.callable.custom.esteelauder;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * Esteelauder雅思兰黛网站收录
 * date:2017-2-7
 * @author denghuan
 *
 */
public class Esteelauder extends AbstractSelect{

	private static final String domain = "www.esteelauder.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String fullTitle = doc.select(".product-full__description-section h3.product-full__title").text();
			String subTitle = doc.select(".product-full__description-section h4.product-full__subtitle").text();
			String product = StringUtils.substringBetween(content, "var page_data = ", "</script>");
			String productId = StringUtils.substringBetween(content, "productId\" content=\"", "\"");
			String salePrice = doc.select("span.product-full__price").text();
			List<Image> imageList = new ArrayList<Image>();
			Elements es = doc.select(".product-full__images .product-full__image img");
			for(Element e : es){
				String image = e.attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image("http://"+domain+image));
				}
			}
			
			if(CollectionUtils.isEmpty(imageList)){
				for(Element e : es){
					String image = e.attr("data-src");
					if(StringUtils.isNotBlank(image)){
						imageList.add(new Image("http://"+domain+image));
					}
				}
			}
			
			String unit = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(salePrice)){
				String currency = StringUtils.substring(salePrice, 0, 1);
				unit = Currency.codeOf(currency).name();
			}else{
				unit = "USD";
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
			rebody.setTitle(new Title(fullTitle+subTitle, ""));
			rebody.setBrand(new Brand("Esteelauder", ""));
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			if(StringUtils.isNotBlank(product)){
				JSONObject jsonObject = JSONObject.parseObject(product);
				String catalog_spp = jsonObject.getString("catalog-spp");
				JSONObject catalogSppJsonObject = JSONObject.parseObject(catalog_spp);
				JSONArray jsonArray = catalogSppJsonObject.getJSONArray("products");
				if(jsonArray != null && jsonArray.size() > 0){
					for(int i = 0;i < jsonArray.size(); i++){
						JSONObject productsObject =  jsonArray.getJSONObject(i);
						JSONArray skuJsonArray = productsObject.getJSONArray("skus");
						for(int j = 0;j < skuJsonArray.size(); j++){
							JSONObject skuObject =  skuJsonArray.getJSONObject(j);
							
							LSelectionList lSelectionList = new LSelectionList();
							String size = skuObject.getString("PRODUCT_SIZE");
							String price = skuObject.getString("PRICE");
							String inventory_status = skuObject.getString("INVENTORY_STATUS");
							String color = skuObject.getString("SHADENAME");
							String skuId = skuObject.getString("SKU_ID");
							String smImage = skuObject.getString("XL_SMOOSH");
							JSONArray imageJsonArray = skuObject.getJSONArray("MEDIUM_IMAGE");
							List<Image> skuImageList = new ArrayList<Image>();
							for(int s = 0;s < imageJsonArray.size(); s++){
								String image =  imageJsonArray.getString(i);
								if(StringUtils.isNotBlank(image)){
									skuImageList.add(new Image("http://"+domain+image));
								}
							}
							if(StringUtils.isNotBlank(smImage)){
								skuImageList.add(new Image("http://"+domain+smImage));
							}
							int stock_status = 0;
							if("1".equals(inventory_status) || "2".equals(inventory_status)){
								stock_status = 1;
							}
							if(StringUtils.isNotBlank(color) || 
									StringUtils.isNotBlank(size)){
								lSelectionList.setGoods_id(skuId);
								lSelectionList.setOrig_price(Float.parseFloat(price));
								lSelectionList.setSale_price(Float.parseFloat(price));
								lSelectionList.setStock_status(stock_status);
								lSelectionList.setPrice_unit(unit);
								List<Selection> selections = new ArrayList<>();
								if(StringUtils.isNotBlank(size)){
									Selection selection = new Selection();
									selection.setSelect_name("size");
									selection.setSelect_value(size);
									selections.add(selection);
								}
								lSelectionList.setSelections(selections);
								
								if(StringUtils.isBlank(color)){
									lSelectionList.setStyle_id("default");
									if(display){
										LStyleList lStyleList = new LStyleList();
										lStyleList.setDisplay(true);
										display = false;
										lStyleList.setGood_id(skuId);
										lStyleList.setStyle_cate_name("color");
										lStyleList.setStyle_cate_id(0);
										lStyleList.setStyle_switch_img("");
										lStyleList.setStyle_id("default");
										lStyleList.setStyle_name("default");
										if(StringUtils.isNotBlank(price)){
											rebody.setPrice(new Price(Float.parseFloat(price), 
													0, Float.parseFloat(price), unit));
										}
										context.getUrl().getImages().put(skuId, imageList);// picture
										l_style_list.add(lStyleList);
									}else{
										context.getUrl().getImages().put(skuId, skuImageList);// picture
									}
								}else{
									LStyleList lStyleList = new LStyleList();
									lStyleList.setGood_id(skuId);
									lStyleList.setStyle_cate_name("color");
									lStyleList.setStyle_cate_id(0);
									lStyleList.setStyle_switch_img("");
									lStyleList.setStyle_id(color);
									lStyleList.setStyle_name(color);
									lSelectionList.setStyle_id(color);
									if(display){
										lStyleList.setDisplay(true);
										display = false;
										if(StringUtils.isNotBlank(price)){
											rebody.setPrice(new Price(Float.parseFloat(price), 
													0, Float.parseFloat(price), unit));
										}
										context.getUrl().getImages().put(skuId, imageList);// picture
									}else{
										//skuImageList.addAll(imageList);
										context.getUrl().getImages().put(skuId, skuImageList);// picture
									}
									
									l_style_list.add(lStyleList);
								}
								l_selection_list.add(lSelectionList);
								
							}else{//无SKU
								if(StringUtils.isNotBlank(price)){
									rebody.setPrice(new Price(Float.parseFloat(price), 
											0, Float.parseFloat(price), unit));
								}
								context.getUrl().getImages().put(skuId, imageList);// picture
								rebody.setStock(new Stock(stock_status));
							}
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
				rebody.setStock(new Stock(spuStock));
			}
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			String url = context.getCurrentUrl();
			String cates = url.substring(url.indexOf("product-catalog/"), url.lastIndexOf("/"));
			if(StringUtils.isNotBlank(cates)){
				String[] sp = cates.split("/");
				if(sp.length >= 2){
					cats.add(sp[1]);
					cats.add(sp[2]);
					breads.add(sp[1]);
					breads.add(sp[2]);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			doc.select(".spp-product__details .spp-product__details-ingredients-wrapper").remove();
			String description = doc.select(".spp-product__details").text();
			featureMap.put("feature-1", description);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "women");
			rebody.setProperties(propMap);
			
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
}
