package com.haitao55.spider.crawler.core.callable.custom.asos;

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
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * Asos网站收录
 * @author denghuan
 *
 */
public class Asos extends AbstractSelect{

	private static final String domain = "www.asos.com";
	
	private String ASOS_URL ="http://www.asos.com/api/product/catalogue/v2/stockprice?productIds=###&store=COM&currency=GBP";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String skus = StringUtils.substringBetween(content, "view('", "',");
			String title = StringUtils.substringBetween(skus, "name\":\"", "\"");
			String brand = StringUtils.substringBetween(skus, "brandName\":\"", "\"");
			String productId = StringUtils.substringBetween(skus, "id\":", ",");
			String gender = StringUtils.substringBetween(content, "gender: '", "'");
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\"");
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
			
			Map<String,AsosVO> asosMap = new HashMap<>();
			if(StringUtils.isNotBlank(productId)){
				 String apiUrl = ASOS_URL.replace("###", productId);
				 Url currentUrl = new Url(apiUrl);
				 currentUrl.setTask(context.getUrl().getTask());
				 String skuJson = HttpUtils.get(currentUrl,HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES,false);
				 if(StringUtils.isNotBlank(skuJson)){
					 JSONArray skuJsonArray = JSONArray.parseArray(skuJson);
					 for(int i = 0; i < skuJsonArray.size(); i++){
							JSONObject skusObject =  skuJsonArray.getJSONObject(i);
							
							JSONArray variantsJsonArray = JSONArray.parseArray(skusObject.getString("variants"));
							
							for(int j = 0; j < variantsJsonArray.size(); j++){
								JSONObject variantsObject =  variantsJsonArray.getJSONObject(j);
								AsosVO asosVO = new AsosVO();
								String variantId = variantsObject.getString("variantId");
								String skuId = variantsObject.getString("sku");
								String isInStock = variantsObject.getString("isInStock");
								String price = variantsObject.getString("price");
								JSONObject priceObject = JSONObject.parseObject(price);
								String currentPrice = priceObject.getString("current");
								String previousPrice = priceObject.getString("previous");
								JSONObject currentPriceObject = JSONObject.parseObject(currentPrice);
								String salePrice = currentPriceObject.getString("value");
								JSONObject previousPriceObject = JSONObject.parseObject(previousPrice);
								String origPirce = previousPriceObject.getString("value");
								asosVO.setIsInStock(isInStock);
								asosVO.setOrigPirce(origPirce);
								asosVO.setSalePrice(salePrice);
								asosVO.setSkuId(skuId);
								asosMap.put(variantId, asosVO);
							}
					 }
				 }
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			boolean display = true;
			Map<String,String> styleMap = new HashMap<String,String>();
			if(StringUtils.isNotBlank(skus)){
				JSONObject jsonObject = JSONObject.parseObject(skus);
				JSONArray imageJsonArray = jsonObject.getJSONArray("images");
				List<Image> imgList = new ArrayList<>();
				for(int j = 0; j < imageJsonArray.size(); j++){
					JSONObject imageObject =  imageJsonArray.getJSONObject(j);
					String image = imageObject.getString("url");
					if(StringUtils.isNotBlank(image)){
						imgList.add(new Image(image));
					}
				}
				boolean flag = jsonObject.containsKey("variants");
				if(flag){
					JSONArray jsonArray = jsonObject.getJSONArray("variants");
					if(jsonArray != null){
						for(int i = 0; i < jsonArray.size(); i++){
							LSelectionList lSelectionList = new LSelectionList();
							
							List<Selection> selections = new ArrayList<>();
							JSONObject skuObject =  jsonArray.getJSONObject(i);
							String variantId = skuObject.getString("variantId");
							String size = StringUtils.EMPTY;
							if(skuObject.containsKey("size")){
								Selection selection = new Selection();
								size = skuObject.getString("size");
								selection.setSelect_name("size");
								selection.setSelect_value(size);
								selections.add(selection);
							}
							String colour =  StringUtils.EMPTY;
							if(skuObject.containsKey("colour")){
								colour = skuObject.getString("colour");
								lSelectionList.setStyle_id(colour);
							}
							AsosVO asosVO = asosMap.get(variantId);
							if(asosVO != null){
								String skuId = asosVO.getSkuId();
								String origPirce = asosVO.getOrigPirce();
								String salePirce = asosVO.getSalePrice();
								lSelectionList.setPrice_unit(unit);
								lSelectionList.setGoods_id(skuId);
								if(!"0".equals(origPirce)){
									lSelectionList.setOrig_price(Float.parseFloat(origPirce));
								}else{
									lSelectionList.setOrig_price(Float.parseFloat(salePirce));
								}
								lSelectionList.setSale_price(Float.parseFloat(asosVO.getSalePrice()));
								
								int stock_status = 0;
								if(!"false".equals(asosVO.getIsInStock())){
									stock_status = 1;
								}
								lSelectionList.setStock_status(stock_status);
								lSelectionList.setSelections(selections);
								
								if(!styleMap.containsKey(colour)){
									LStyleList lStyleList = new LStyleList();
									setStyleList(lStyleList,colour,asosVO.getSkuId());
									if(stock_status > 0){
										if(display){
											lStyleList.setDisplay(true);
											display = false;
											if(StringUtils.isNotBlank(origPirce) 
													&& StringUtils.isNotBlank(salePirce)){
												int save = Math.round((1 - Float.parseFloat(salePirce) / Float.parseFloat(origPirce)) * 100);// discount
												rebody.setPrice(new Price(Float.parseFloat(origPirce), save, Float.parseFloat(salePirce), unit));
											}else if(StringUtils.isNotBlank(salePirce) 
													&& (StringUtils.isBlank(origPirce) ||  origPirce.equals("0"))){
												rebody.setPrice(new Price(Float.parseFloat(salePirce), 0, Float.parseFloat(salePirce), unit));
											}
										}
									}
									context.getUrl().getImages().put(skuId, imgList);// picture
									l_style_list.add(lStyleList);
								}
								styleMap.put(colour, colour);
							}
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
					
				}
				
			 rebody.setStock(new Stock(spuStock));
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cates = doc.select("#breadcrumb ul li a");
			for(int i = 1; i< cates.size(); i++){
				String cat = cates.get(i).text();
				if(StringUtils.isNotBlank(cat)){
					cats.add(cat);
					breads.add(cat);
				}
			}
			if(CollectionUtils.isEmpty(breads)){
				cats.add("home");
				cats.add(title);
				breads.add("home");
				breads.add(title);
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", gender);
			
			String description =doc.select(".product-description ul li").text();
			int count = 0;
			Elements featureEs =doc.select(".size-and-fit span");
			 for(Element e : featureEs){
				 String feature = e.text();
				 if(StringUtils.isNotBlank(feature)){
					 count ++;
					 featureMap.put("feature-"+count, feature);
				 }
			 }
			 rebody.setProperties(propMap);
			 rebody.setFeatureList(featureMap);
			 descMap.put("en", description);
			 rebody.setDescription(descMap);
			 rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}

	private void setStyleList(LStyleList lStyleList,String color,String skuId){
		lStyleList.setGood_id(skuId);
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_switch_img("");
		lStyleList.setStyle_cate_name("color");	
		lStyleList.setStyle_id(color);
		lStyleList.setStyle_name(color);
		
	}
	
	public static void main(String[] args) throws Exception {
//		Context context = new Context();
//		context.setCurrentUrl("http://www.asos.com/adidas/adidas-originals-white-samba-og-trainers/prd/7287150?aab=aa");
//		Asos as = new Asos();
//		as.invoke(context);
	}
}
