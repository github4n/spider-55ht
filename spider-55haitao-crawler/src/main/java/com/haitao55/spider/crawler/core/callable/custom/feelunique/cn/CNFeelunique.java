package com.haitao55.spider.crawler.core.callable.custom.feelunique.cn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * cn feelunique 网站爬取 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月29日 下午3:27:45
 * @version 1.0
 */
public class CNFeelunique extends AbstractSelect{
	private static final String INSTOCK = "目前有货";
	private static final String domain = "cn.feelunique.com";
	/**
	 * stock status 1 有货， 2暂时缺货 3暂时售完下架
	 * 
	 * @param args
	 */
	public void invoke(Context context) throws Exception {
		String content = StringUtils.EMPTY;
		
		boolean isRunInRealTime = context.isRunInRealTime();
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders(context));
			context.setHtmlPageSource(content);
		}else{
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders(context));
//			String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
//			if(StringUtils.isBlank(proxyRegionId)){
//				content = Crawler.create().timeOut(30000).url(context.getCurrentUrl().toString()).header(getHeaders(context)).method(HttpMethod.GET.getValue())
//						.resultAsString();
//			}else{
//				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
//				String proxyAddress=proxy.getIp();
//				int proxyPort=proxy.getPort();
//				content = Crawler.create().timeOut(30000).url(context.getCurrentUrl().toString()).header(getHeaders(context)).method(HttpMethod.GET.getValue())
//						.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
//			}
		}
		RetBody rebody = new RetBody();
		if(StringUtils.isNotBlank(content)){
			Document doc = JsoupUtils.parse(content);
			boolean defaultSkuFlag = false;
			Sku sku = new Sku();
			String default_skuId = StringUtils.EMPTY;
			// brand
			String title = doc.select("div.item h1").get(0).text();
			// brand
			String brand = getBrand(title);
			
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(SpiderStringUtil.md5Encode(context.getCurrentUrl()));
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			rebody.setTitle(new Title(title, ""));
			rebody.setBrand(new Brand(brand, ""));
			
			
			//unit
			String unit = StringUtils.EMPTY;
			String salePriceUnit = StringUtils.substringBetween(content, "currentprice\": \"", "\"");
			if(StringUtils.isNotBlank(salePriceUnit)){
				unit = getCurrencyValue(salePriceUnit);
			}
			
			// productid
			String productId = StringUtils.substringBetween(content, "var productIds = '", "';");

			//stock flag value
			boolean soldOut = false;
			Elements stockElements = doc.select("span.stock-level");

			// isInStock
			String isInStock = StringUtils.substringBetween(content, "\"is_in_stock\":", ",");
			if (StringUtils.equalsIgnoreCase("true", isInStock)) {
				soldOut = true;
			}
			//product main image
			Elements imageElements = doc.select("img#J_picImg");

			// img
			Map<String, List<Image>> imageMap = new HashMap<String, List<Image>>();

			Map<String, String> switch_image_map = new HashMap<String, String>();

			//product id list
			List<String> productList = new ArrayList<String>();
			Elements elements = doc.select("ul.property-list.clearfix li");
			if (CollectionUtils.isNotEmpty(elements)) {
				for (Element element : elements) {
					String id = element.attr("data-id");
					productList.add(id);
					Elements swicthImageElements = element.select("img");
					if(CollectionUtils.isNotEmpty(swicthImageElements)){
						switch_image_map.put(id, swicthImageElements.attr("src"));
					}
				}
			}
			//sku jsonarray 
			JSONArray skuJSONArray = new JSONArray();
			new CNFeeluniqueHandler().process(skuJSONArray, productList, context);
			
			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			// stock number map
			Map<String, Integer> stockMap = new HashMap<String, Integer>();

			// Style
			Map<String, JSONObject> styleMap = new HashMap<String, JSONObject>();

			if (null != skuJSONArray && skuJSONArray.size() > 0) {

				// selection list
				for (Object object : skuJSONArray) {
					JSONObject skuJsonObject = (JSONObject)object;
					LSelectionList lselectlist = new LSelectionList();
					String skuId = skuJsonObject.getString("skuId");
					String goodsId = skuJsonObject.getString("productId");
					String style_id = skuJsonObject.getString("color");
					float origPrice = skuJsonObject.getFloatValue("orign_price");
					float salePrice = skuJsonObject.getFloatValue("sale_price");
					int save = skuJsonObject.getIntValue("save");
					int stock_status = skuJsonObject.getIntValue("stock_status");
					int stock_number = 0;


					lselectlist.setGoods_id(skuId);
					lselectlist.setStyle_id(style_id);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setOrig_price(origPrice);
					lselectlist.setPrice_unit(unit);
					lselectlist.setSale_price(salePrice);

					// selections
					List<Selection> selections = new ArrayList<Selection>();
					lselectlist.setSelections(selections);

					l_selection_list.add(lselectlist);

					// default skuId 网站特殊性， 取第一个sku为默认sku
					if (!defaultSkuFlag) {
						if(StringUtils.equalsIgnoreCase(productId, goodsId)){
							default_skuId = skuId;
							defaultSkuFlag = true;
						}
					}

					// stock
					if (null == stockMap.get(skuId)) {
						stockMap.put(skuId, stock_number + stock_status);
					} else {
						stockMap.put(skuId, stock_number + stock_status + stockMap.get(skuId));
					}

					// spu price
					if (StringUtils.equalsIgnoreCase(default_skuId, skuId)) {
						rebody.setPrice(new Price(origPrice, save, salePrice, unit));
					}

					styleMap.put(skuId, skuJsonObject);
				}

				// style list
				if (null != styleMap && styleMap.size() > 0) {
					for (Map.Entry<String, JSONObject> entry : styleMap.entrySet()) {
						LStyleList lStyleList = new LStyleList();
						String skuId = entry.getKey();
						JSONObject skuJson = entry.getValue();
						String goodIds = skuJson.getString("productId");
						String style_cate_name = "Color";
						String color = skuJson.getString("color");
						String switch_img = StringUtils.EMPTY;
						if (null != switch_image_map.get(goodIds)) {
							switch_img = switch_image_map.get(goodIds);
						}
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(color);
						lStyleList.setStyle_cate_name(style_cate_name);
						lStyleList.setStyle_name(color);

						boolean display = false;
						if (StringUtils.equalsIgnoreCase(default_skuId, skuId)) {
							display = true;
						}
						lStyleList.setDisplay(display);

						// images
						List<Image> list = imageMap.get(skuId);
						if(null==list||list.size()==0){
							String imageUrl = StringUtils.EMPTY;
							if(CollectionUtils.isNotEmpty(imageElements)){
								imageUrl = imageElements.attr("src");
							}
							list=new ArrayList<Image>();
							list.add(new Image(imageUrl));
						}
						context.getUrl().getImages().put(skuId, list);

						l_style_list.add(lStyleList);
					}
				}

			}

			// 单ｓｋｕ
			else {
				// price
				String save = StringUtils.EMPTY;
				String salePrice = StringUtils.substringBetween(content, "currentprice\": \"", "\"");

				String origPrice = StringUtils.substringBetween(content, "originprice\": \"", "\"");

				unit = getCurrencyValue(salePrice);// 得到货币代码
				salePrice = salePrice.replaceAll("[$,£ ]", "");

				if (StringUtils.isBlank(replace(origPrice))) {
					origPrice = salePrice;
				}
				origPrice = origPrice.replaceAll("[$,£ ]", "");
				if (StringUtils.isBlank(replace(salePrice))) {
					salePrice = origPrice;
				}
				if (StringUtils.isBlank(origPrice)
						|| Float.valueOf(replace(origPrice)) < Float.valueOf(replace(salePrice))) {
					origPrice = salePrice;
				}
				if (StringUtils.isBlank(save)) {
					save = Math.round((1 - Float.valueOf(replace(salePrice)) / Float.valueOf(replace(origPrice))) * 100)
							+ "";// discount
				}
				rebody.setPrice(new Price(Float.parseFloat(origPrice), StringUtils.isBlank(save) ? 0 : Integer.parseInt(save),
						Float.parseFloat(salePrice), unit));

				List<Image> pics = new ArrayList<Image>();

				// image
				Elements selectedImages = doc.select("div.thumbnails ul li.selected a");
				if (CollectionUtils.isNotEmpty(selectedImages)) {
					for (Element element : selectedImages) {
						String src = element.attr("href");
						pics.add(new Image(src));
					}
				}else{
					String imageUrl = StringUtils.EMPTY;
					if(CollectionUtils.isNotEmpty(imageElements)){
						imageUrl = imageElements.attr("src");
					}
					pics.add(new Image(imageUrl));
				}

				// images context
				context.getUrl().getImages().put(productId, pics);
			}

			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			
			// stock
			int stock_status = 0;
			if(l_selection_list != null 
					&& l_selection_list.size() > 0){
				for(LSelectionList ll : l_selection_list){
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						stock_status = 1;
						break;
					}
					if (sku_stock == 2){
						stock_status = 2;
					}
				}
			} else {
				if (soldOut) {
					stock_status = 1;
				}
			}

			// stock
			rebody.setStock(new Stock(stock_status));

			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements breadcrumbEs = doc.select("div#breadcrumb > ul li:not(:first-child)");
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
			String description = doc.select("div#product-description-tab div.tab-content").text();
			featureMap.put("feature-1", description);
			
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			rebody.setProperties(propMap);
			// sku
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}


	private static String replace(String dest) {
		if (StringUtils.isBlank(dest)) {
			return StringUtils.EMPTY;
		}
		if (null == dest) {
			return StringUtils.EMPTY;
		}
		return StringUtils.trim(dest.replaceAll("\\\\n|\\\\t|\\\\r|\\$", ""));

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
		unit = Currency.codeOf(currency).name();
		return unit;

	}

	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "cn.feelunique.com");
		 headers.put("Referer", context.getUrl().getParentUrl());
		 headers.put("Accept-Encoding", "gzip, deflate, sdch");
		 headers.put("Cookie", " D_SID=192.243.119.27:DxlfonMJJUoIesv2gfRD8BzxkWLfN6bjovSPqqhBwNA; D_UID=5076FC3F-90DD-3ED5-9DF1-0C8CA0875C35; D_HID=pBI8DGhuNhFWTj/Ag/GCU4Lwa/VNJQz1q79onPdhBCs;feeluniqueCurr=GBP;");
		return headers;
	}
	
	private static String getBrand(String title) {
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<title.length();i++){
			char c = title.charAt(i);
			int v = (int)c; 
			if(v >=19968 && v <= 171941){
				break ;
			}
			buffer.append(c);
		}
		if(StringUtils.equals(String.valueOf(buffer.charAt(buffer.length()-1)), " ")){
			buffer.deleteCharAt(buffer.length()-1);
		}
		return buffer.toString();
	}
}
