package com.haitao55.spider.crawler.core.callable.custom.bergdorfgoodman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;

import sun.misc.BASE64Encoder; 


@SuppressWarnings("restriction")
public class Bergdorfgoodman extends AbstractSelect{

	private static final String domain = "www.bergdorfgoodman.com";
	private static final String BERGDORF_SERVICE_API = "http://www.bergdorfgoodman.com/product.service?";
	private String IMAGE_URL = "http://bergdorfgoodman.scene7.com/is/image/bergdorfgoodman/{id}?&wid=400&height=500";
	
	@Override
	public void invoke(Context context) throws Exception {
		 String  content= getContent(context,context.getCurrentUrl());
		//String content = this.getInputString(context);
//		 final Map<String, Object> headers = new HashMap<String, Object>();
//		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
//		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		 headers.put("Accept-Encoding", "gzip, deflate, sdch");
//		 headers.put("Cookie", "D_SID=180.168.59.6:0gQa1xg/mPSVADGe4QlO6VkD8XUClxT8DC+vyi424Tw; TLTSID=BF9307E207AF1007C27A858D180B9BF8; TLTUID=BF9307E207AF1007C27A858D180B9BF8; AGA=\"\"; W2A=3456958474.57185.0000; WelcomeMatFirstTimeBorderFreeUser=here; sr_browser_id=c7617a38-14ea-4a9f-9d58-90f63446e9ac; WID=3821194792; DYN_USER_ID=3821194792; DYN_USER_CONFIRM=2a9db34c395df154984018e7b5ea1ce8; page_number=1; __qca=P0-1053767627-1489383723913; s_pers=%20productnum%3D1%7C1491975723954%3B; s_sq=%5B%5BB%5D%5D; __cmbU=ABJeb18d_ysI55I8Ny60S78-j4KX5VciFATzr48wcC_GYH9GVm0xlpgoFLgRk-whnsfNK4Nq9lj7KCauNHrTH-L-fFQwDoRHcw; JSESSIONID=gveW2wp-zx0gNAhp1vJGSRR1; clientipaddr=180.168.59.6; __cmbDomTm=0; __cmbTpvTm=329; inside-usnm01=74704589-5a9df30b51ef413ddd2d7994585339ce4600fcfab870595bc48281ee7cbf7f49-0-0; __cmbCk=PMRGK2LEEI5CEQKCJJSWEMJZMQ3WSWTGOV4HMOCDKRKWM3TSNVDEET2PGVJWUMSHGBJG4WLPLJGUYRSUN5GFUZLXK42UGNSJNE3U6RKQOBZVAZC7IZUTIQL2G42FI5DWKRPXQVCQFVLG4N2LNR4USODRJU3WYMC2PI2UIMLSM4RCYITTNR2CEORRGQ4DSMZYHA4DONBUGE4CYITDMJ2CEORRGI3TGMBZFQRHI4BCHI3TMNRVG42X2===; dtSa=-; dtLatC=3; mbox=PC#1489383707984-704748.24_12#1497165515|session#1489387832286-274254#1489391375|check#true#1489389575; firstTimeUser=here; TS717e29=1da13d713d8738f6707cf95ed76715fd89ae81d674661a5c58c647788524428acf87fd56286023f23e0eb126ec2531803e0eb12660ac0ec5f5b38c97d72b81c0b43462a8219997374bddfa06219396cda46d2518c917fa831a9db3d6; s_fid=4FD57B44C69DBB64-11ACF13D0799044B; s_cc=true; utag_main=v_id:015ac62fd7ea003c782fcd235d6205068001f0600086e$_sn:2$_ss:0$_st:1489391316080$drawer_clickthrough:true%3Bexp-1489470121705$_pn:3%3Bexp-session$ses_id:1489387832819%3Bexp-session$_prevpage:product%20detail%3Bexp-1489393115937; D_PID=DADCA7A1-3FD9-3082-9D04-EB98987A046B; D_IID=F04F1A05-9572-3FFC-A57D-02C7AC73774C; D_UID=81824671-4990-3E68-8067-E0ED24700139; D_HID=FFgos2clMuWt/R9AUf7w1QCu4/CuqpTsrA4gUX3A7xo; D_ZID=176C33E1-6713-3DDC-A679-A89B9CC53193; D_ZUID=B3524741-0677-3148-AECB-439314747E18; rr_rcs=eF4FwcERgCAMBMAPL3u5GSE5SDqwDQIy48OfWr-7abu_55oqbUdWczFnZnFBaygq9PSOI8T7GLXAghV6WkBXFXQhFzltavyhWhKE; _br_uid_2=uid%3D9319313148330%3Av%3D11.8%3Ats%3D1489383708984%3Ahc%3D8; dtCookie=C163181FB10B648E1B6569B77A3D3B74|d3d3LmJlcmdkb3JmZ29vZG1hbi5jb218MQ; s_sess=%20s_ppvl%3Dhttp%25253A%252F%252Fwww.bergdorfgoodman.com%252FCategories%252FDresses%252Fcat80001_cat000009_cat000002%252Fc.cat%252C18%252C18%252C1039%252C1366%252C615%252C1366%252C768%252C1%252CL%3B%20s_ppv%3Dhttp%25253A%252F%252Fwww.bergdorfgoodman.com%252FOscar-de-la-Renta-Sequined-Illusion-Tulle-Cocktail-Dress-Black-Dresses%252Fprod124241848_cat80001__%252Fp.prod%25253Ficid%25253D%252526searchType%25253DEndecaDrivenCat%252526rte%25253D%252525252Fcategory.jsp%252525253FitemId%252525253Dcat80001%2525252526pageSize%252525253D30%2525252526No%252525253D0%2525252526refinements%252525253D%252526eItemId%25253Dprod124241848%252526cmCat%25253Dproduct%252C53%252C53%252C880%252C1366%252C615%252C1366%252C768%252C1%252CL%3B; CChipCookie=956366858.20480.0000; dtPC=189514077_629h5");
//		 headers.put("Referer", context.getUrl().getValue());
//		 headers.put("Upgrade-Insecure-Requests", "1");
//		 String content = Crawler.create().timeOut(60000).retry(3)/*.proxy(true).proxyAddress("104.196.30.199").proxyPort(3128)*/.url(context.getUrl().toString()).header(headers).resultAsString();
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String title = doc.select(".product-details-source h1.product-name span").text();
			String brand = doc.select(".product-details-source h1.product-name span.product-designer a").text();
			if(StringUtils.isBlank(brand)){
				doc.select(".product-details-source h1.product-name span").remove();
				brand = doc.select(".product-details-source h1.product-name").text();
			}
			
			Map<String,String> imageMap = new HashMap<>();
			Elements es = doc.select("ul#color-pickers li.color-picker");
			for(Element  e : es){
				String image = e.attr("data-sku-img");
				String dataColorName = e.attr("data-color-name");
				if(StringUtils.isNotBlank(image) && 
						StringUtils.isNotBlank(dataColorName)){
					String iId = StringUtils.substringBetween(image, "{\"m*\":\"", "\"");
					imageMap.put(dataColorName, iId);
				}
			}
			
			Elements images =  doc.select(".product-thumbnails ul.list-inline li .alt-img-wrap img");
			List<Image> imageList = new ArrayList<>();
			for(Element  is : images){
				String image = is.attr("data-main-url");
				if(StringUtils.isNotBlank(image) && 
						!StringUtils.containsIgnoreCase(image, "http")){
					imageList.add(new Image("http:"+image));
				}else{
					imageList.add(new Image(image));
				}
			}
			
			if(CollectionUtils.isEmpty(imageList) && 
					MapUtils.isEmpty(imageMap)){
				String image = doc.select(".prod-img .img-wrap img").attr("src");
				if(StringUtils.isNotBlank(image)){
					imageList.add(new Image(image));
				}
			}
			
			
			String salePrice = StringUtils.substringBetween(content, "product_price\":[\"", "\"");
			doc.select(".price-adornments-elim-suites ins.sale-text").remove();
			String origPirce = doc.select(".price-adornments-elim-suites span.item-price").text();
			String unit = StringUtils.substringBetween(content, "order_currency_code\":\"", "\"");
			
			if(StringUtils.isNotBlank(origPirce)){
				origPirce = origPirce.replaceAll("[$,s\\ ]", "");
				origPirce = pattern(origPirce);
			}
			
			String promoPrice = doc.select("span.promo-price").text();
			if(StringUtils.isNotBlank(promoPrice)){
				promoPrice = promoPrice.replaceAll("[$, ]", "");
				salePrice = promoPrice;
			}
			
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$, ]", "");
			}
			
			if(StringUtils.isBlank(origPirce)){
				origPirce = salePrice;
			}
			
			if(StringUtils.isNotBlank(salePrice)){
				int save = Math.round((1 - Float.parseFloat(salePrice) / Float.parseFloat(origPirce)) * 100);// discount
				rebody.setPrice(new Price(Float.parseFloat(origPirce), 
						save, Float.parseFloat(salePrice), unit));
			}
			
			String productId = StringUtils.substringBetween(content, "product_id\":[\"", "\"");
			
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
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			Map<String,String> styleMap = new HashMap<>();
			boolean display = true;
			if(StringUtils.isNotBlank(productId)){
				String base64 = base64(productId);
				String url = BERGDORF_SERVICE_API+base64;
				
				String html = getContent(context,url);
				
				JSONObject jsonObject = JSONObject.parseObject(html);
				String productSizeAndColor = jsonObject.getString("ProductSizeAndColor");
				JSONObject productSizeJsonObject = JSONObject.parseObject(productSizeAndColor);
				String productSizeAndColorJSON = productSizeJsonObject.getString("productSizeAndColorJSON");
				JSONArray jsonArray = JSONArray.parseArray(productSizeAndColorJSON);
				for(int i = 0; i < jsonArray.size(); i++){
					JSONObject skuJsonObject = jsonArray.getJSONObject(i);
					JSONArray skuJsonArray = skuJsonObject.getJSONArray("skus");
					for(int j = 0; j < skuJsonArray.size(); j++){
						JSONObject skusJSON = skuJsonArray.getJSONObject(j);
						LSelectionList lSelectionList = new LSelectionList();
						//String defaultSkuColor = skusJSON.getString("defaultSkuColor");
						//String stockLevel = skusJSON.getString("stockLevel");
						String colorVal = skusJSON.getString("color");
						if(StringUtils.isNotBlank(colorVal) && 
								StringUtils.containsIgnoreCase(colorVal, "?")){
							colorVal = colorVal.substring(0, colorVal.indexOf("?"));
						}
						String sizeVal = skusJSON.getString("size");
						if(StringUtils.isBlank(colorVal) && StringUtils.isBlank(sizeVal)){
							continue;
						}
						String skuId = skusJSON.getString("sku");
						String instock = skusJSON.getString("backOrderFlag");
						String status = StringUtils.EMPTY;
						if(skusJSON.containsKey("status")){
							status = skusJSON.getString("status");
						}
						lSelectionList.setGoods_id(skuId);
						lSelectionList.setOrig_price(Float.parseFloat(origPirce));
						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setPrice_unit(unit);
						
						if(StringUtils.isNotBlank(instock) && 
								(!"true".equals(instock) || "In Stock".equals(status))){
							lSelectionList.setStock_status(1);
						}
						
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
							String imageId = imageMap.get(colorVal);
							if(StringUtils.isNotBlank(imageId)){
								String image = IMAGE_URL.replace("{id}", imageId);
								list.add(new Image(image));
							}
							
							list.addAll(imageList);
							
							context.getUrl().getImages().put(skuId, list);// picture
						}
						styleMap.put(colorVal, colorVal);
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
					
				}else{
					context.getUrl().getImages().put(productId, imageList);// picture
					String instok = StringUtils.substringBetween(content, "product_inventory_status\":[\"", "\"");
					if(StringUtils.isNotBlank(instok) && "Instock".endsWith(instok)){
						spuStock = 1;
					}
				
				}
				
				
				rebody.setStock(new Stock(spuStock));
				
				List<String> cats = new ArrayList<String>();
				List<String> breads = new ArrayList<String>();
				Elements cates = doc.select("ul.breadcrumbs li.bcClick a");
				for(Element e : cates){
					String cate = e.text();
					if(StringUtils.isNotBlank(cate)){
						cats.add(cate);
						breads.add(cate);
					}
				}
				
				if(CollectionUtils.isEmpty(cates)){
					cats.add(title);
					breads.add(title);
				}
				
				rebody.setCategory(cats);
				rebody.setBreadCrumb(breads);
				
				
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				String  description = doc.select(".product-details-info").text();
				Elements featureEs = doc.select(".productCutline ul li");
			    int count = 0;
				if(featureEs != null &&  featureEs.size() > 0){
					for(Element e : featureEs){
						String text = e.text();
						if(StringUtils.isNotBlank(text)){
							 count ++;
							 featureMap.put("feature-"+count, text);
						}
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
	private  String pattern(String price){
		Pattern pattern = Pattern.compile("(\\d+(.\\d+))");
		Matcher matcher = pattern.matcher(price);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private String base64(String productId){
		 String data_string = "{\"ProductSizeAndColor\":{\"productIds\":\""+productId+"\"}}";
		 byte[] bt = data_string.getBytes();
		 String base64 = (new BASE64Encoder()).encodeBuffer(bt);
		 return "data=$b64$"+base64.trim()+"&sid=getSizeAndColorData&bid=ProductSizeAndColor&timestamp="+new Date().getTime()+"";
	}
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders(context)).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(Context context){
		final Map<String, Object> headers = new HashMap<String, Object>();
		 headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
		 headers.put("Accept", "*/*");
		 headers.put("Accept-Encoding", "gzip");
		 headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		 headers.put("Host", "www.bergdorfgoodman.com");
		 headers.put("X-Requested-With", "XMLHttpRequest");
		 headers.put("Origin", "http://www.bergdorfgoodman.com");
		 headers.put("Referer", context.getUrl().getValue());
		 headers.put("X-Distil-Ajax", "fuwbycuzuxzcazvbzc");
		 headers.put("Cookie", "D_SID=180.168.59.6:0gQa1xg/mPSVADGe4QlO6VkD8XUClxT8DC+vyi424Tw; TLTSID=BF9307E207AF1007C27A858D180B9BF8; TLTUID=BF9307E207AF1007C27A858D180B9BF8; AGA=\"\"; W2A=3456958474.57185.0000; WelcomeMatFirstTimeBorderFreeUser=here; sr_browser_id=c7617a38-14ea-4a9f-9d58-90f63446e9ac; WID=3821194792; DYN_USER_ID=3821194792; DYN_USER_CONFIRM=2a9db34c395df154984018e7b5ea1ce8; page_number=1; __qca=P0-1053767627-1489383723913; s_pers=%20productnum%3D1%7C1491975723954%3B; s_sq=%5B%5BB%5D%5D; __cmbU=ABJeb18d_ysI55I8Ny60S78-j4KX5VciFATzr48wcC_GYH9GVm0xlpgoFLgRk-whnsfNK4Nq9lj7KCauNHrTH-L-fFQwDoRHcw; JSESSIONID=gveW2wp-zx0gNAhp1vJGSRR1; dtSa=-; dtLatC=1; mbox=PC#1489383707984-704748.24_12#1497165901|session#1489387832286-274254#1489391761|check#true#1489389961; firstTimeUser=here; s_cc=true; utag_main=v_id:015ac62fd7ea003c782fcd235d6205068001f0600086e$_sn:2$_ss:0$_st:1489391701807$drawer_clickthrough:true%3Bexp-1489470121705$_pn:5%3Bexp-session$ses_id:1489387832819%3Bexp-session$_prevpage:product%20detail%3Bexp-1489393501666; rr_rcs=eF4NyzEOgDAIAMClk38hKQUK_MBvFGoTBzf1_Xr7lfLkHuQjszewkA58WACvTjBIZIlMmxzb9d7nZNIKyOZk7hXxP6rQmMQ_07kSfg; _br_uid_2=uid%3D9319313148330%3Av%3D11.8%3Ats%3D1489383708984%3Ahc%3D10; __cmbDomTm=0; __cmbTpvTm=321; inside-usnm01=74704589-5a9df30b51ef413ddd2d7994585339ce4600fcfab870595bc48281ee7cbf7f49-0-0; dtCookie=C163181FB10B648E1B6569B77A3D3B74|d3d3LmJlcmdkb3JmZ29vZG1hbi5jb218MQ; dtPC=-; s_fid=4FD57B44C69DBB64-11ACF13D0799044B; CChipCookie=939589642.20480.0000; TS717e29=109892027b6afd945f8a78e1255dca2b89ae81d674661a5c58c64f148524428a22f1bb83286023f23e0eb126ec2531803e0eb12660ac0ec5f5b38c97d72b81c0b43462a8219997374bddfa06219396cda46d2518c917fa8316b9cd7a; s_sess=%20s_ppvl%3Dhttp%25253A%252F%252Fwww.bergdorfgoodman.com%252FOscar-de-la-Renta-Sequined-Illusion-Tulle-Cocktail-Dress-Black-Dresses%252Fprod124241848_cat80001__%252Fp.prod%252C43%252C43%252C721%252C1366%252C615%252C1366%252C768%252C1%252CL%3B%20s_ppv%3Dhttp%25253A%252F%252Fwww.bergdorfgoodman.com%252FCategories%252FDresses%252Fcat80001_cat000009_cat000002%252Fc.cat%252C18%252C18%252C1039%252C1366%252C615%252C1366%252C768%252C1%252CL%3B; clientipaddr=180.168.59.6; D_PID=DADCA7A1-3FD9-3082-9D04-EB98987A046B; D_IID=F04F1A05-9572-3FFC-A57D-02C7AC73774C; D_UID=81824671-4990-3E68-8067-E0ED24700139; D_HID=FFgos2clMuWt/R9AUf7w1QCu4/CuqpTsrA4gUX3A7xo; D_ZID=176C33E1-6713-3DDC-A679-A89B9CC53193; D_ZUID=B3524741-0677-3148-AECB-439314747E18");
		return headers;
	}
	
	public static void main(String[] args) throws Exception {
		String url = "http://www.bergdorfgoodman.com/Kiehl-s-Since-1851-Limited-Edition-Kate-Moross-Collection-Travel-Essentials/prod132070254_cat20017__/p.prod";
		Bergdorfgoodman shan = new Bergdorfgoodman();
		Context context = new Context();
		context.setUrl(new Url(url));
		context.setCurrentUrl(url);
		shan.invoke(context);
	}

}
