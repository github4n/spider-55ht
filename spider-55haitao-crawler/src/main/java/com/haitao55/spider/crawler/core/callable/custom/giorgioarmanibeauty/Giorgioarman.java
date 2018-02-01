package com.haitao55.spider.crawler.core.callable.custom.giorgioarmanibeauty;

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

/**
 * Giorgioarman网站收录
 * date : 2017-3-17
 * @author denghuan
 *
 */
public class Giorgioarman extends AbstractSelect{

	private static final String domain = "www.giorgioarmanibeauty-usa.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select("h1.product_name").text();
			String subtitle = doc.select("h2.product_subtitle").text();
			
			String docid = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title + " " + subtitle, ""));
			
			String unit = StringUtils.substringBetween(content, "priceCurrency\" content=\"", "\"");
			String selectColor = doc.select("ul.pdp_product_swatch_list li.selected a").attr("data-desc");
			String selectSize = StringUtils.substringBetween(content, "selected=\"selected\">", "</option>");
			if(StringUtils.isNotBlank(selectSize)){
				selectSize = selectSize.replaceAll("[\\s]", "");
			}
			String productContent = StringUtils.substringBetween(content, "app.pageContextObject = ", ";}");
			Elements es = doc.select("#thumbnails ul li.thumb a");
			List<Image> list = new ArrayList<>();
			if(es != null && es.size() > 0){
				for(Element e : es){
					String image = e.attr("href");
					if(StringUtils.isNotBlank(image) && !"#".equals(image)){
						list.add(new Image(image));
					}
				}
			}
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			if(StringUtils.isNotBlank(productContent)){
				JSONObject jsonObject = JSONObject.parseObject(productContent);
				String currentProduct = jsonObject.getString("currentProduct");
				String eeProductsOnPage = jsonObject.getString("eeProductsOnPage");
				JSONObject currentJsonObject = JSONObject.parseObject(currentProduct);
				JSONObject eeProductsJsonObject = JSONObject.parseObject(eeProductsOnPage);
				JSONArray jsonArray = currentJsonObject.getJSONArray("variants");
				for(int i = 0; i < jsonArray.size(); i++){
					LSelectionList lSelectionList = new LSelectionList();
					String skuIdKey = jsonArray.getString(i);
					String product = eeProductsJsonObject.getString(skuIdKey);
					JSONObject productJsonObject = JSONObject.parseObject(product);
					String salerPrice = productJsonObject.getString("price");
					String imageUrl = productJsonObject.getString("imageUrl");
					String availability = productJsonObject.getString("availability");
					String color = productJsonObject.getString("color");
					String size = productJsonObject.getString("size");
					String variantID = productJsonObject.getString("variantID");
					String brand = productJsonObject.getString("brand");
					List<Image> skuImageList = new ArrayList<>();
					if(StringUtils.isNotBlank(imageUrl)){
						skuImageList.add(new Image(imageUrl));
						skuImageList.addAll(list);
					}
					
					int stock_status = 0;
					if("true".equals(availability)){
						stock_status = 1;
					}
					lSelectionList.setGoods_id(variantID);
					lSelectionList.setStock_status(stock_status);
					lSelectionList.setSale_price(Float.parseFloat(salerPrice));
					lSelectionList.setOrig_price(Float.parseFloat(salerPrice));
					lSelectionList.setPrice_unit(unit);
					
					if(StringUtils.isNotBlank(color)){
						lSelectionList.setStyle_id(color);
						List<Selection> selections = new ArrayList<>();
						lSelectionList.setSelections(selections);
						LStyleList lStyleList = new LStyleList();
						if(color.equals(selectColor)){
							lStyleList.setDisplay(true);
							rebody.setPrice(new Price(Float.parseFloat(salerPrice), 
									0, Float.parseFloat(salerPrice), unit));
							rebody.setBrand(new Brand(brand, ""));
						}
						lStyleList.setGood_id(variantID);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_cate_name("color");
						lStyleList.setStyle_id(color);
						lStyleList.setStyle_name(color);
						lStyleList.setStyle_switch_img("");
						context.getUrl().getImages().put(variantID, skuImageList);// picture
						l_style_list.add(lStyleList);
					}else{
						lSelectionList.setStyle_id("default");
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(size)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(size);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
					}
					if(StringUtils.isNotBlank(size)){
						String rpSize = size.replaceAll("[\\s]", "");
						if(rpSize.equals(selectSize)){
							LStyleList lStyleList = new LStyleList();
							lStyleList.setDisplay(true);
							lStyleList.setGood_id(variantID);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id("default");
							lStyleList.setStyle_name("default");
							lStyleList.setStyle_switch_img("");
							context.getUrl().getImages().put(variantID, skuImageList);// picture
							l_style_list.add(lStyleList);
							rebody.setPrice(new Price(Float.parseFloat(salerPrice), 
									0, Float.parseFloat(salerPrice), unit));
							rebody.setBrand(new Brand(brand, ""));
						}
					}
					l_selection_list.add(lSelectionList);
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
			Elements cates = doc.select("ul.breadcrumb li a span");
			for(int i = 1; i < cates.size()-1; i++){
				String cate = cates.get(i).text();
				if(StringUtils.isNotBlank(cate)){
					cats.add(cate);
					breads.add(cate);
				}
			}
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			String  description = doc.select("#tab_details").text();
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
