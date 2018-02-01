package com.haitao55.spider.crawler.core.callable.custom.clinique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * 
 * @author denghuan
 * clinique网站收录
 * date:2017-2-15
 */
public class Clinique extends AbstractSelect{

	private static final String domain = "www.clinique.com";
	private static final String BASE_URL = "http://www.clinique.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			String productId = StringUtils.substringBetween(content, "productId\" content=\"", "\"");
			String product = StringUtils.substringBetween(content, "var page_data =", "</script>");
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			boolean display = true;
			if(StringUtils.isNotBlank(product)){
				JSONObject jsonObject = JSONObject.parseObject(product);
				String catalog_spp = jsonObject.getString("catalog-spp");
				JSONObject catalogJsonObject = JSONObject.parseObject(catalog_spp);
				JSONArray jsonArray = catalogJsonObject.getJSONArray("products");
				if(jsonArray != null && jsonArray.size() > 0){
					for(int i = 0; i < jsonArray.size(); i++){
						JSONObject proJsonObject = jsonArray.getJSONObject(i);
						String title = proJsonObject.getString("PROD_RGN_NAME");
						String imageZoom = proJsonObject.getString("IMAGE_ZOOM");
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
						rebody.setBrand(new Brand("Clinique/倩碧", ""));
						
						JSONArray skuJsonArray = proJsonObject.getJSONArray("skus");
						for(int j = 0; j < skuJsonArray.size(); j++){
							LSelectionList lSelectionList = new LSelectionList();
							JSONObject skuJsonObject = skuJsonArray.getJSONObject(j);
							String salePrice = skuJsonObject.getString("PRICE");
							String image = skuJsonObject.getString("IMAGE_SPP");
							String instock = skuJsonObject.getString("INVENTORY_STATUS");
							String sizeVal = skuJsonObject.getString("PRODUCT_SIZE");
							String colorVal = skuJsonObject.getString("SHADENAME");
							String skuId = skuJsonObject.getString("SKU_ID");
							int stock_status = 0;
							if("1".equals(instock)){
								stock_status = 1;
							}
							
							lSelectionList.setGoods_id(skuId);
							lSelectionList.setOrig_price(Float.parseFloat(salePrice));
							lSelectionList.setSale_price(Float.parseFloat(salePrice));
							lSelectionList.setPrice_unit(unit);
							lSelectionList.setStock_status(stock_status);
							if(StringUtils.isNotBlank(colorVal)){
								lSelectionList.setStyle_id(colorVal);
							}else{
								lSelectionList.setStyle_id("default");
							}
							List<Selection> selections = new ArrayList<>();
							if(StringUtils.isNotBlank(sizeVal)){
								Selection selection = new Selection();
								selection.setSelect_name("size");
								selection.setSelect_value(sizeVal);
								selections.add(selection);
							}
							lSelectionList.setSelections(selections);
							
							if(!styleMap.containsKey(colorVal)){
								LStyleList lStyleList = new LStyleList();
								if(display){
									lStyleList.setDisplay(true);
									display = false;
								}
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_cate_name("color");
								if(StringUtils.isNotBlank(colorVal)){
									lStyleList.setStyle_id(colorVal);
									lStyleList.setStyle_name(colorVal);
								}else{
									lStyleList.setStyle_id("default");
									lStyleList.setStyle_name("default");
								}
								
								lStyleList.setStyle_switch_img("");
								l_style_list.add(lStyleList);
								List<Image> list = new ArrayList<>();
								if(StringUtils.isNotBlank(colorVal)){
									if(StringUtils.isNotBlank(image) && 
											!StringUtils.containsIgnoreCase(image, BASE_URL)){
										list.add(new Image(BASE_URL+image));
									}
								}else{
									if(StringUtils.isNotBlank(imageZoom) && 
											!StringUtils.containsIgnoreCase(imageZoom, BASE_URL)){
										list.add(new Image(BASE_URL+imageZoom));
									}
								}
								context.getUrl().getImages().put(skuId, list);// picture
							}
							styleMap.put(colorVal, colorVal);
							l_selection_list.add(lSelectionList);
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
				Elements cates = doc.select("ul.breadcrumbs li");
				for(Element e : cates){
					String cate = e.text();
					if(StringUtils.isNotBlank(cate)){
						cate = cate.replaceAll("[|\\s*]", "");
						cats.add(cate);
						breads.add(cate);
					}
				}
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				String  description = doc.select(".abstract").text();
				String  desc = doc.select(".how_to_use").text();
				rebody.setFeatureList(featureMap);
				descMap.put("en", description+desc);
				rebody.setDescription(descMap);
				Map<String, Object> propMap = new HashMap<String, Object>();
				propMap.put("s_gender", "");
				rebody.setProperties(propMap);
				rebody.setSku(sku);
			}
		}
		setOutput(context, rebody);
	}

}
