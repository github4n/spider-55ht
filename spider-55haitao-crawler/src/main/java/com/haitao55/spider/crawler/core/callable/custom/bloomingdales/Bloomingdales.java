package com.haitao55.spider.crawler.core.callable.custom.bloomingdales;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

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
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;

/**
 * date 2016-12-8
 * 
 * @author denghuan
 *
 */
public class Bloomingdales extends AbstractSelect {

	private static final String domain = "www.bloomingdales.com";
	private static final String BASE_IMAGE_URL = "https://images.bloomingdales.com/is/image/BLM/products/";

	@Override
	public void invoke(Context context) throws Exception {

		boolean isRunInRealTime = context.isRunInRealTime();
		String content = StringUtils.EMPTY;
		if (isRunInRealTime) {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US","55ht_zone_us");
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
		} else {
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US","55ht_zone_us");
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
		}

		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			String productDetail = StringUtils.substringBetween(content, "type=\"application/json\">", "</script>");
			String gender = StringUtils.substringBetween(content, "GENDER_AGE\":[\"", "\"]");
			String productId = StringUtils.substringBetween(content, "productId\" type=\"hidden\" value=\"", "\"");
			
			String unit = StringUtils.substringBetween(content, "currencyCode\":\"", "\"");
			String title = StringUtils.EMPTY;
			String description = StringUtils.EMPTY;
			
			Sku sku = new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();

			Map<String, String> styleMap = new HashMap<String, String>();
			if (StringUtils.isNotBlank(productDetail)) {
				JSONObject jsonObject = JSONObject.parseObject(productDetail);
				String product = jsonObject.getString("product");
				JSONObject productjsonObject = JSONObject.parseObject(product);
				String brand = productjsonObject.getString("brand");
				description = productjsonObject.getString("longDescription");
				title = productjsonObject.getString("productDescription");
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
				String selectedColor = productjsonObject.getString("primaryColor");
				String imageSource = productjsonObject.getString("imageSource");
				String images = productjsonObject.getString("colorwayAdditionalImages");

				JSONObject imagesJsonObject = JSONObject.parseObject(images);

				String skuObject = productjsonObject.getString("sizeColorTypeByUPC");
				JSONObject skusObject = JSONObject.parseObject(skuObject);
				if (skusObject != null) {
					Set<String> keys = skusObject.keySet();
					Iterator<String> it = keys.iterator();
					while (it.hasNext()) {

						String key = it.next();

						LSelectionList lSelectionList = new LSelectionList();

						List<Selection> selections = new ArrayList<>();
						String productSku = skusObject.getString(key);
						JSONObject sizeColorObject = JSONObject.parseObject(productSku);
						String origPrice = StringUtils.EMPTY;
						if (sizeColorObject.containsKey("ORIGINAL_PRICE")) {
							origPrice = sizeColorObject.getString("ORIGINAL_PRICE");
						}
						String skuId = sizeColorObject.getString("UPC_CODE");
						String salePrice = sizeColorObject.getString("RETAIL_PRICE");
						String image = sizeColorObject.getString("UPC_COLORWAY_PRIMARY_IMG");
						String color = sizeColorObject.getString("COLOR");
						String size = sizeColorObject.getString("SIZE");

						List<Image> imgList = new ArrayList<>();
						if (StringUtils.isNotBlank(image)) {
							imgList.add(new Image(BASE_IMAGE_URL + image));
						}

						if (StringUtils.isNotBlank(color)) {
							String skuImage = imagesJsonObject.getString(color);
							if (StringUtils.isNotBlank(skuImage)) {
								String[] sps = skuImage.split(",");
								if (sps != null) {
									for (String img : sps) {
										imgList.add(new Image(BASE_IMAGE_URL + img));
									}
								}
							}
						}

						if (CollectionUtils.isEmpty(imgList)) {
							if (StringUtils.isNotBlank(imageSource)) {
								imgList.add(new Image(BASE_IMAGE_URL + imageSource));
							}
						}

						lSelectionList.setGoods_id(skuId);
						lSelectionList.setPrice_unit(unit);
						if (StringUtils.isNotBlank(origPrice)) {
							lSelectionList.setOrig_price(Float.parseFloat(origPrice));
						} else {
							lSelectionList.setOrig_price(Float.parseFloat(salePrice));
						}

						lSelectionList.setSale_price(Float.parseFloat(salePrice));
						lSelectionList.setStock_status(1);

						if (StringUtils.isNotBlank(color)) {
							lSelectionList.setStyle_id(color);

						}
						if (StringUtils.isNotBlank(size)) {
							Selection selection = new Selection();
							selection.setSelect_name("size");
							selection.setSelect_value(size);
							selections.add(selection);
						}
						if (!styleMap.containsKey(color)) {
							LStyleList lStyleList = new LStyleList();
							setStyleList(lStyleList, color, skuId);

							if (StringUtils.isNotBlank(selectedColor) && selectedColor.equals(color)) {
								lStyleList.setDisplay(true);
								if (StringUtils.isNotBlank(salePrice) && StringUtils.isNotBlank(origPrice)) {
									int save = Math.round(
											(1 - Float.parseFloat(salePrice) / Float.parseFloat(origPrice)) * 100);// discount
									rebody.setPrice(new Price(Float.parseFloat(origPrice), save,
											Float.parseFloat(salePrice), unit));
								} else if (StringUtils.isNotBlank(salePrice) && StringUtils.isBlank(origPrice)) {
									rebody.setPrice(new Price(Float.parseFloat(salePrice), 0,
											Float.parseFloat(salePrice), unit));
								}

							}
							context.getUrl().getImages().put(skuId, imgList);// picture
							l_style_list.add(lStyleList);
						}

						styleMap.put(color, color);

						lSelectionList.setSelections(selections);

						l_selection_list.add(lSelectionList);

					}
				}
			}

			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			int spuStock = 0;
			if (l_selection_list != null && l_selection_list.size() > 0) {
				for (LSelectionList ll : l_selection_list) {
					int sku_stock = ll.getStock_status();
					if (sku_stock == 1) {
						spuStock = 1;
						break;
					}
					if (sku_stock == 2) {
						spuStock = 2;
					}
				}
			} else {

			}

			rebody.setStock(new Stock(spuStock));

			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			if (StringUtils.isNotBlank(gender)) {
				cats.add(gender);
				breads.add(gender);
			}
			cats.add(title);
			breads.add(title);
			rebody.setCategory(cats);
			rebody.setBreadCrumb(breads);

			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Map<String, Object> propMap = new HashMap<String, Object>();
			if (StringUtils.containsIgnoreCase(gender, "women")) {
				propMap.put("s_gender", "women");
			} else {
				propMap.put("s_gender", "all");
			}

			featureMap.put("feature-0", description);

			rebody.setProperties(propMap);
			rebody.setFeatureList(featureMap);
			descMap.put("en", description);
			rebody.setDescription(descMap);
			rebody.setSku(sku);

		}
		setOutput(context, rebody);
	}

	private void setStyleList(LStyleList lStyleList, String color, String skuId) {
		lStyleList.setGood_id(skuId);
		lStyleList.setStyle_cate_id(0);
		lStyleList.setStyle_switch_img("");
		lStyleList.setStyle_cate_name("color");
		lStyleList.setStyle_id(color);
		lStyleList.setStyle_name(color);

	}

	private void getImage(String images, List<Image> imgList, String fristImage) {
		if (StringUtils.isNotBlank(images)) {
			String[] sp = images.split(",");
			for (String s : sp) {
				if (!s.equals(fristImage)) {
					imgList.add(new Image(BASE_IMAGE_URL + s));
				}
			}
		}
	}

	private String crawlerUrl(Context context, String url) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put(":authority", "www.bloomingdales.com");
		headers.put(":method", "GET");
		headers.put(":scheme", "https");
		headers.put("accept-encoding", "gzip, deflate, br");
		headers.put("cookie",
				"shippingCountry=US; SignedIn=0; GCs=CartItem1_92_03_87_UserName1_92_4_02_; SEED=8205726406149280231; ak_bmsc=DC8A36D023CA546BD2832CF2CCD11DF5419E72EF82320000CE270D5ADF53F64D~plTieUjhJNKMXNyiLGkUXhuiiRPuqAk6BafUV34A4T8dFwmgeqHAm3f1AzgF2Fhnhy4/e3vl4X6nnyn3wMQ0q6ZGtRIfOqp+5I9IItZLrBK7Amn4eG77AY0UPwW7R/L9HyNXYiRjDrCqit8MsyvikexM4S24Q4OpOPhdkP+KpZPhn9LMymbikpjU28HKZ/6kaOS8Wa8V+DNAGEyubWdnWfgo1LebONekJhVeqCq9iPF+z5Pvggpo9ChH7T/78VRPYHT9COEUnnMhaXYyxCPKxKD2AGGapGs7DfwnaVBfUJUdjAr5O7gKVSvVJqg66KijyBQPHXHSMsOcwn186y3Z1m3yAX/1HQC5/WImlSmfEwc2A=; CMAVID=none; xdVisitorId=1201JKZdN_Aiy6_Bx87_ZjwJOKgKLkfEi4KRQcx-5ZMtp8k5EA7; _cavisit=15fc3660234|; _caid=933ae41f-ac68-4c04-ac5a-2a0959b6c1a9; _4c_mc_=9a4932aa636b0743852f2c3617c5940d; dca=RTP; TS0111b70e=0146ac7c6794c7639aeaa105f740d3442beb3cd1e63de00c8de5988769b0f0db259507e0250663fab932e1ff3b86e7e1ab8c93bb97; cmTPSet=Y; mt.urlQuery=(cm_sp:NAVIGATION-_-TOP_NAV-_-16958-FEATURED-DESIGNERS-COACH,id:'1004772'); SEGMENT=%7B%22EXPERIMENT%22%3A%5B2816%5D%7D; _msuuid_qljqreuvj0=5128DE89-6A2E-48B2-A7FB-C3DDAC374A86; mercury=true; mbox=session#1510811627509-462861#1510813765|em-disabled#true#1510813433|check#true#1510811965; RTD=af58e20af567f0af5bc40af59a60af5b870af5fb20af59f70af5a790; fs_nocache_guid=54B7E582B7A1F24B7143B96A003CC382; FORWARDPAGE_KEY=https%3A%2F%2Fwww.bloomingdales.com%2Fshop%2Fproduct%2Fcoach-1941-quilting-dinky-crossbody%3FID%3D2766542; mt.v=2.869094987.1510811684633; utag_main=v_id:015fc364b728001c563e0110bf1402085001e07d0086e$_sn:1$_ss:0$_st:1510813840866$ses_id:1510811678506%3Bexp-session$_pn:9%3Bexp-session; _uetsid=_uet5c016320; _ga=GA1.2.625239229.1510811762; _gid=GA1.2.1775276674.1510811762; CoreM_State=23~-1~-1~-1~-1~3~3~5~3~3~7~7~|~11933347~6B926494~|~~|~~|~0~1||||||~|~1510812041698~1510811852906~|~~|~~|~~|~~|~~|~~|~; CoreM_State_Content=6~|~52696639D7B3106B~EF3F10B1FEB01DB8~|~0~1; MISCGCs=USERPC1_92_850013_87_USERLL1_92_33.4486%2C-112.07333_87_USERST1_92_AZ3_87_USERDMA1_92_7533_87_DT1_92_PC3_87_DSW1_92_2803_87_DSH1_92_1753_87_DBN1_92_Chrome3_87_DMN1_92_623_87_BOPSPICKUPSTORE1_92_4_02_; QuantumMetricUserID=2b6f051469107932d583e68018adedf3; QuantumMetricSessionID=614f72f278ea6bb2fa09a67f194648a8; atgRecVisitorId=1201JKZdN_Aiy6_Bx87_ZjwJOKgKLkfEi4KRQcx-5ZMtp8k5EA7; atgRecSessionId=GUXDZgA-X3Z08CiztOPdv2gvpRS4Ct-3Hb1L-0h6-yQWF73FtGDQ!-2135026725!1379992591; _sp_id.c359=2d0ec9ad-5bfe-4a25-aa51-572c03781bfa.1510811853.1.1510812045.1510811853.fc5ed766-8dd9-4030-a0ea-231b5509948b; _sp_ses.c359=*; BCOMGC=636464088453967314%5E764328ea-92ca-e711-815b-0a8bf2e57af8%5E774328ea-92ca-e711-815b-0a8bf2e57af8%5E0%5E192.243.119.27; TS0132ea28=0146ac7c677e1050b001b8ca6a7039c0f511b1d13b4e05013b9d6bb3457838b4fd885e3287; akavpau_www_www1_bcom=1510812346~id=e0e4351bc55a8aa02c40aa411105b131; bm_sv=D5AF40D1A1BD623E33CA50A389F5578B~GZvmCDrFTDF3QzBa8MwAaoaU7o/+MJ5VUlOjkmcfR6DUZlB6Z/EQJ5cHCEjjPQAveKze+8j4pbjrADy9v+ElVsvewXSCn2gaw7jcVSIOE081Y5u4ItXZP1w7TxJUWBt16W37xt0txcuV+YWOEO7WlLsF6k+7NgWgRyTggG548PI=; rr_rcs=eF5j4cotK8lM4TM1ttQ11DVkKU32SDJLskyzSDPVNbFIM9E1sUwy0U01S0nWTTIxNU8xMjQ0NE41BACQDA5B");
		return headers;
	}

	public static void main(String[] args) {
		LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", true);
		String content = luminatiHttpClient.request(
				"https://www.bloomingdales.com/shop/product/mcm-visetos-medium-stark-backpack?ID=1635555&CategoryID=1000059",
				getHeaders());
		System.out.println(content);
	}

}
