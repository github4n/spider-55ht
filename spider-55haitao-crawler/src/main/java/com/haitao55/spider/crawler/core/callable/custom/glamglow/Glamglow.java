package com.haitao55.spider.crawler.core.callable.custom.glamglow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.HttpClientUtil;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * Glamglow网站收录
 * @author denghuan
 *
 */
public class Glamglow extends AbstractSelect{

	private static final String domain = "www.glamglow.com";
	private static final String BASE_URL = "https://www.glamglow.com";
	private static final String PIFFIX_IMAGE_URL ="https://www.glamglow.com/media/export/cms/products/415x415/gg_prod_";
	private static final String SUFIXX_IMAGE_URL ="_415x415_";

	
	private String crawlerUrl(Context context,String url,String referer) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders(referer)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Map<String, String> params = new HashMap<>();
			content = HttpClientUtil.sendHttpsRequestByPost(url,params);
		}
		return content;
	}
	
	private static Map<String, Object> getHeaders(String referer) {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.75 Chrome/62.0.3202.75 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Accept-Language", "en-US,en;q=0.8");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Connection", "keep-alive");
		headers.put("Cache-Control:", "max-age=0");
		headers.put("Host", "www.glamglow.com");
		headers.put("Referer", referer);
		headers.put("cookie","ak_bmsc=7E8DEBD5A317C0CDC083D4F67F7D18F8CC01891EF81400004900095A31F45454~plI/AXJDbEEhGhD04hEUySlggzPMASLnwGPN7ROYACqTZHYii5TgnNx4O8QGCcygo5dALz7jV9romdwOGs1IVWR96qOqB/HgsFOk+vnNnC/NaXsVXHlX5JulBvLtu8mDLwi5g0FKyhcauRYpNrrVqgDkFkfFsHNq1KQCGGB1TiXT9F3nUdLyDPI7f80gNi7tgAZac/hMT2EySjvwgQGIvueNkhtHct9/u+N31EVZhnwdU=; FE_USER_CART=csr_logged_in%3A0%26first_name%3A%26full_name%3A%26item_count%3A0%26pc_email_optin%3A0%26region_id%3A%26signed_in%3A0; SESSION=45680184-706d09be85511347c115af85b8fe420aaa2050a295690919b4e60a1b79c3659b; ngsession=c0f3771b33977494; ngglobal=c0f3771b37360833; __qca=P0-814883994-1510539344729; AMCVS_esteelauder%40AdobeOrg=1; client.isMobile=0; has_js=1; candid_userid=872e7dd8-4c4e-41bc-87a7-e628d771b68f; hide_popup_offer=yes; btcartcookie=; LPVID=Y1NWM5M2UwMmMwNTMzYTFm; LPSID-48719195=4Z9hAymuR9uTSiSmPEuAng; csrftoken=f1a42dbe878b900b0967e09302b4467bc1d2dc19%2C8dcfe4a387da583822929954d353fae91ee1c4cc%2C1510539371; LOCALE=en_US; bm_sv=0DDDA828DD26C7EC4306C53D35CE1D68~tUsZXNtIeH6I59Ne4yz2BA6LO73NxQ++SvHOoTblY0yAEpHAp7PsYtUq/vv49h9Zj2VekOeDA+AVudduFWgJ9a6OIZHpQK2heDoLVcxP6gPdNTlK3f/s6GptnLjqVUB8agVx4hc0coZYymZt39Qk+GbZzy8nkNhn0EWOCG70tC0=; __pr.NaN=e4iheuv4nw; Auser=0%7C0%7C0%7C0%7C0%7C0%7C0%7C0%7C0-null; __pr.12e3=2xgoalub5p; tms_generic_enable_bobo=1; _uetsid=_uet6cb7fc5d; xyz_cr_100255_et_112==NaN&cr=100255&et=112&ap=; s_cc=true; AMCV_esteelauder%40AdobeOrg=-227196251%7CMCMID%7C15792490026878437403189171401448174057%7CMCOPTOUT-1510546573s%7CNONE%7CMCAID%7CNONE");
		return headers;
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl  = context.getCurrentUrl();
		String referer = context.getUrl().getParentUrl();
		Map<String, String> params = new HashMap<>();
		String content = HttpClientUtil.sendHttpsRequestByPost(currentUrl,params);
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select("h3.product__title").text();
			String salePrice = doc.select(".product__price").text();
			String productId = StringUtils.substringBetween(content, "PRODUCT_ID\":\"", "\"");
			
			String unit = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(salePrice)){
				String cy = StringUtils.substring(salePrice, 0, 1);
				unit = Currency.codeOf(cy).name();
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
			rebody.setBrand(new Brand("GlamGlow", ""));
			
			List<Image> list = new ArrayList<>();
			Elements es  = doc.select(".product-gallery__main img.js-gallery-img-large");
			for(Element  e : es){
				String image = e.attr("data-zoom-image");
				//String index = e.attr("data-gallery-index");
				if(StringUtils.isNotBlank(image)){
					image = image.replaceAll("623x623", "415x415");
					list.add(new Image(BASE_URL+image));
				}
			}
			
			String data = StringUtils.substringBetween(content, "var page_data = ", "</script>");
			if(StringUtils.isNotBlank(data)){
				JSONObject jsonObject = JSONObject.parseObject(data);
				String catalogSpp = jsonObject.getString("catalog-spp");
				JSONObject cataJsonObject = JSONObject.parseObject(catalogSpp);
				JSONArray jsonArray = cataJsonObject.getJSONArray("products");
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject skuJsonObject = jsonArray.getJSONObject(i);
					Sku sku = new Sku();
					List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
					List<LStyleList> l_style_list = new ArrayList<LStyleList>();
					boolean display = true;
					
					JSONArray skusJsonArray = skuJsonObject.getJSONArray("skus");
					for(int j = 0; j < skusJsonArray.size(); j++){
						JSONObject skuJson = skusJsonArray.getJSONObject(j);
						LSelectionList lSelectionList = new LSelectionList();
						
						String skuSalePrice = skuJson.getString("PRICE");
						String skuId = skuJson.getString("SKU_ID");
						String sizeVal = skuJson.getString("PRODUCT_SIZE");
						String instock = skuJson.getString("INVENTORY_STATUS");
						String colorVal = skuJson.getString("SHADENAME");
						String code = skuJson.getString("PRODUCT_CODE");
						
						List<Image> imageList = new ArrayList<>();
						for(int index = 0 ;index < list.size(); index++){
							String image = PIFFIX_IMAGE_URL+code+SUFIXX_IMAGE_URL+index+".jpg";
							imageList.add(new Image(image));
						}
						
						int stock_status = 0;
						if("1".equals(instock)){
							stock_status = 1;
						}
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setSale_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setOrig_price(Float.parseFloat(skuSalePrice));
						lSelectionList.setPrice_unit(unit);
						lSelectionList.setStock_status(stock_status);
						
						List<Selection> selections = new ArrayList<>();
						if(StringUtils.isNotBlank(sizeVal)){
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(sizeVal);
							selections.add(selection);
						}
						lSelectionList.setSelections(selections);
						
						if(StringUtils.isNotBlank(colorVal)){
							lSelectionList.setStyle_id(colorVal);
							LStyleList lStyleList = new LStyleList();
							lStyleList.setGood_id(skuId);
							lStyleList.setStyle_cate_id(0);
							lStyleList.setStyle_cate_name("color");
							lStyleList.setStyle_id(colorVal);
							lStyleList.setStyle_name(colorVal);
							lStyleList.setStyle_switch_img("");
							if(display){
								lStyleList.setDisplay(true);
								display = false;
								rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
										0, Float.parseFloat(skuSalePrice), unit));
							}
							context.getUrl().getImages().put(skuId, imageList);// picture
							l_style_list.add(lStyleList);
						}else{
							lSelectionList.setStyle_id("default");
						}
						
						if(StringUtils.isBlank(colorVal)){
							if(display){
								LStyleList lStyleList = new LStyleList();
								lStyleList.setGood_id(skuId);
								lStyleList.setStyle_cate_id(0);
								lStyleList.setStyle_cate_name("color");
								lStyleList.setStyle_id("default");
								lStyleList.setStyle_name("default");
								lStyleList.setStyle_switch_img("");
								lStyleList.setDisplay(true);
								context.getUrl().getImages().put(skuId, imageList);// picture
								display = false;
								rebody.setPrice(new Price(Float.parseFloat(skuSalePrice), 
										0, Float.parseFloat(skuSalePrice), unit));
								l_style_list.add(lStyleList);
							}
						}
						l_selection_list.add(lSelectionList);
						
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
						rebody.setPrice(new Price(Float.parseFloat(salePrice), 
								0, Float.parseFloat(salePrice), unit));
						context.getUrl().getImages().put(productId, list);// picture
						spuStock = 1;
					}
					rebody.setStock(new Stock(spuStock));
					
					String cate = StringUtils.substringBetween(content, "categoryName\":\"", "\"");
					
					List<String> cats = new ArrayList<String>();
					List<String> breads = new ArrayList<String>();
					
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
					}else{
						cats.add(title);
						breads.add(title);
					}
					rebody.setCategory(cats);
					rebody.setBreadCrumb(breads);
					
					Map<String, Object> featureMap = new HashMap<String, Object>();
					Map<String, Object> descMap = new HashMap<String, Object>();
					String  description = doc.select(".how-to-use__wrap .how-to-use__content").text();
					featureMap.put("feature-1", description);
							
					rebody.setFeatureList(featureMap);
					descMap.put("en", description);
					rebody.setDescription(descMap);
					Map<String, Object> propMap = new HashMap<String, Object>();
					propMap.put("s_gender", "");
					rebody.setProperties(propMap);
					
					rebody.setSku(sku);
				}
			}
		}
		setOutput(context, rebody);
	}
}
