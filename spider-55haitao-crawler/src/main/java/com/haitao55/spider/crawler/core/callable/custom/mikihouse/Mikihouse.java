package com.haitao55.spider.crawler.core.callable.custom.mikihouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class Mikihouse extends AbstractSelect {
	private static String domain = "www.mikihouse.jp";
	private static String image_url_preffix = "http://www.mikihouse.jp/ciao/hskhsk/itsub/";
	private static String image_url_preffix2="http://www.mikihouse.jp/ciao/hskhsk/itdetailbig/";

	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		Document doc = context.getCurrentDoc().getDoc();

		String url=context.getCurrentUrl().toString();
		
		String default_sku = StringUtils.EMPTY;

		RetBody retBody = new RetBody();

		String productId = StringUtils.EMPTY;

		Elements elements = doc.select("input#Rsm_It_c");
		if (CollectionUtils.isNotEmpty(elements)) {
			productId = elements.attr("value");
		}

		String skuData = StringUtils.substringBetween(content, "itemsJson = eval('(", ")');");

		if (StringUtils.isNotBlank(skuData)) {
			boolean sku_flag = false;

			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// Style
			Map<String, LSelectionList> styleMap = new HashMap<String, LSelectionList>();

			//stock Map
			Map<String,Integer> stockMap=new HashMap<String,Integer>();
			
			
			// image
			List<Image> pics = new ArrayList<Image>();

			elements = doc.select("img.js-openSubWindow");
			if (CollectionUtils.isNotEmpty(elements)) {
				String imageUrl = elements.attr("data-url");
				imageUrl = StringUtils.substringAfter(imageUrl, "pict=");
				imageUrl = image_url_preffix.concat(imageUrl);
				pics.add(new Image(imageUrl));
			}

			elements = doc.select("div.details_check_pict_wrapper img:first-child");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String imageUrl = element.attr("alt");
					if (StringUtils.isBlank(imageUrl)) {
						continue;
					}
					imageUrl = image_url_preffix2.concat(imageUrl).concat(".jpg");
					pics.add(new Image(imageUrl));
				}
			}

			
			JSONObject skuJsonObject = JSONObject.parseObject(skuData);
			// selection list
			for (Map.Entry<String, Object> skuMap : skuJsonObject.entrySet()) {
				String skuId = skuMap.getKey();
				Object value = skuMap.getValue();
				@SuppressWarnings("unchecked")
				Map<String, Object> skuValue = (Map<String, Object>) value;

				LSelectionList lselectlist = new LSelectionList();

				String color = (String) skuValue.get("ItStock_ColorTxt");
				String sizeorage = (String) skuValue.get("ItStock_SizeTxt");

				// price
				String orignPrice = StringUtils.EMPTY;// orign_price
				String salePrice = (String) skuValue.get("ByPrice");// sale_price
				salePrice = salePrice.replaceAll("[,円 ]", "");
				if (StringUtils.isBlank(orignPrice)) {
					orignPrice = salePrice;
				}
				Float orign_price = Float.parseFloat(orignPrice);
				Float sale_price = Float.parseFloat(salePrice);
				if (orign_price < sale_price) {
					orign_price = sale_price;
				}
				String price_unit = Currency.codeOf("J￥").name();// unit
				int save = Math.round((1 - sale_price / orign_price) * 100);

				// stock
				int stock_number = Integer.parseInt((String) skuValue.get("ZanStock"));// stock
				Integer stock_status = 0;
				if (stock_number > 0) {
					stock_status = 2;
				}

				if("−−−".equals(color)&&"−−−".equals(sizeorage)){
					//single item
					retBody.setStock(new Stock(stock_status));
					retBody.setPrice(new Price(orign_price, save, sale_price, price_unit));
					context.getUrl().getImages().put(productId, pics);
					continue;
				}
				
				
				List<Selection> selections = new ArrayList<Selection>();

				String style_id = color;
				if (StringUtils.isBlank(color)||StringUtils.equalsIgnoreCase(style_id, "−−−")) {
					style_id = sizeorage;
				} else {
					// selections
					if (StringUtils.isNotBlank(sizeorage)&&!StringUtils.equalsIgnoreCase(sizeorage, "−−−")) {
						Selection selection = new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("サイズ(対象年齢)");
						selection.setSelect_value(sizeorage);
						selections.add(selection);
					}
				}
				
				//stock map 
				Integer number = stockMap.get(style_id);
				if(null==number){
					stockMap.put(style_id, stock_number);
				}else{
					stockMap.put(style_id,number + stock_number);
				}

				lselectlist.setGoods_id(skuId);
				lselectlist.setStyle_id(style_id);
				lselectlist.setOrig_price(orign_price);
				lselectlist.setSale_price(sale_price);
				lselectlist.setPrice_unit(price_unit);
				lselectlist.setStock_number(stock_number);
				lselectlist.setStock_status(stock_status);

				lselectlist.setSelections(selections);

				styleMap.put(style_id, lselectlist);
				
				if (!sku_flag) {
					if(stock_number>0){
						retBody.setPrice(new Price(orign_price, save, sale_price, price_unit));
						default_sku = style_id;
						sku_flag = true;
					}
				}

				l_selection_list.add(lselectlist);
			}

			
			// style list
			if (styleMap.size() > 0) {
				for (Map.Entry<String, LSelectionList> entry : styleMap.entrySet()) {
					LStyleList lStyleList = new LStyleList();
					LSelectionList lSelectionList = entry.getValue();
					String style_cate_name = StringUtils.EMPTY;
					if (lSelectionList.getSelections().size() > 0) {
						style_cate_name = "カラー";
					} else {
						style_cate_name = "サイズ(対象年齢)";
					}
					String skuId = lSelectionList.getGoods_id();
					String color = lSelectionList.getStyle_id();
					String switch_img = StringUtils.EMPTY;
					lStyleList.setGood_id(skuId);
					lStyleList.setStyle_switch_img(switch_img);
					lStyleList.setStyle_cate_id(0);
					lStyleList.setStyle_id(color);
					lStyleList.setStyle_cate_name(style_cate_name);
					lStyleList.setStyle_name(color);

					boolean display = false;
					if (StringUtils.equalsIgnoreCase(default_sku, color)) {
						display = true;
					}
					lStyleList.setDisplay(display);
					
					context.getUrl().getImages().put(skuId, pics);

					l_style_list.add(lStyleList);
				}
			}

			Sku sku = new Sku();
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			// sku
			retBody.setSku(sku);
			
			if(stockMap.size()>0){
				int stock_status=0;
				for (Map.Entry<String, Integer> entry : stockMap.entrySet()) {
					String style_id = entry.getKey();
					if(StringUtils.equals(default_sku, style_id)){
						Integer number=entry.getValue();
						if(number>0){
							stock_status=2;
						}
						retBody.setStock(new Stock(stock_status));
					}
				}
			}

			String title = StringUtils.EMPTY;

			elements = doc.select("h1.detailH1");
			if (CollectionUtils.isNotEmpty(elements)) {
				title = elements.text();
				title = title.replaceAll("[ ■]", "");
			}

			retBody.setTitle(new Title("", "", title, ""));

			// full doc info
			String docid = SpiderStringUtil.md5Encode(url);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(domain));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

			String brand = StringUtils.EMPTY;
			boolean brandFlag = false;

			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cateElements = doc.select("div#itinfo_top_cate  a:not(:first-child)");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				for (Element element : cateElements) {
					String cat = Native2AsciiUtils.ascii2Native(element.text());
					if (!brandFlag) {
						brand = cat;
						brandFlag = true;
					}
					if (StringUtils.isNotBlank(cat)) {
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
			retBody.setCategory(cats);
			// BreadCrumb
			breads.add(brand);
			retBody.setBreadCrumb(breads);

			// brand
			retBody.setBrand(new Brand("", "", brand, ""));

			// desc
			String desc = StringUtils.EMPTY;
			elements = doc.select("div.cms_container + div,div#details_check_container + div");
			if (CollectionUtils.isNotEmpty(elements)) {
				desc = elements.text();
			}
			
			Map<String, Object> featureMap = new HashMap<String, Object>();
			retBody.setFeatureList(featureMap);
			Map<String, Object> description = new HashMap<String, Object>();
			description.put("en", desc);
			retBody.setDescription(description);

			String gender = StringUtils.EMPTY;
			Map<String, Object> propMap = new HashMap<String, Object>();
			List<List<Object>> propattr = new ArrayList<List<Object>>();
			// gender = getSex(sb.toString());
			// gender = getSex(title);
			// gender = getSex(cats.toString());
			propMap.put("s_gender", gender);
			elements = doc.select("div.iteminfo_spec.js-wookmark-container ul li");
			if (elements != null && elements.size() > 0) {
				for (Element e : elements) {
					List<Object> proList = new ArrayList<Object>();
					List<List<String>> list = new ArrayList<List<String>>();
					String keyValue = StringUtils.EMPTY;
					Elements key = e.select("h3");
					if (CollectionUtils.isEmpty(key)) {
						continue;
					}
					keyValue = key.get(0).text();
					keyValue = keyValue.replaceAll("[ ■]", "");
					Elements valueList = e.select("div");
					if (CollectionUtils.isNotEmpty(valueList)) {
						for (Element element : valueList) {
							List<String> tempList = new ArrayList<String>();
							tempList.add(element.text());
							list.add(tempList);
						}
					}
					proList.add(keyValue);
					proList.add(list);
					propattr.add(proList);
				}
			}
			propMap.put("attr", propattr);

			retBody.setProperties(propMap);
		}
		
		setOutput(context, retBody);
		
	}

}
