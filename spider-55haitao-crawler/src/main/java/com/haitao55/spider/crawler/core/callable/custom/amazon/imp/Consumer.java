package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
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
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.SpringUtils;

public class Consumer implements Runnable{

	
	private HTQueue<String> queue;
	private static AtomicLong count = new AtomicLong(0);
	private boolean stop = false;
	private static final int nThreads = 10;
	private static ExecutorService service = Executors.newFixedThreadPool(nThreads);;
	private static Long taskId = 1482470886504l;
	private SpiderKafkaProducer producer = SpringUtils.getBean("producer");
	private static final String topic = "spider_55haitao_product";
	private ProxyPool pool;
	
	public  Consumer(HTQueue<String> queue,ProxyPool pool){
		this.queue = queue;
		this.pool = pool;
	}
	
	
	@Override
	public void run() {
		while(!stop || queue.getTotalCount() > 0){
			String url = StringUtils.EMPTY;
			String json = StringUtils.EMPTY;
			try {
				
				json = queue.get();
				if(StringUtils.isBlank(json)){
					Thread.sleep(2000);
					continue;
				}
				
				JsonObject retbody = JsonUtils.json2bean(json, JsonObject.class);
				RetBody ret = new RetBody();
				if(retbody != null ){
					String title = retbody.getAsJsonObject("d_name").getAsJsonPrimitive("en").getAsString();
					url = retbody.getAsJsonObject("d_product_url").getAsJsonPrimitive("url").getAsString();
					if(!StringUtils.startsWith(url, "https://")){
						url = StringUtils.replace(url, "http://", "https://");
					}
					String docId = SpiderStringUtil.md5Encode(url);
					String orig = retbody.getAsJsonObject("d_price").getAsJsonPrimitive("orig").getAsString();
					int save = retbody.getAsJsonObject("d_price").getAsJsonPrimitive("save").getAsInt();
					String sale = retbody.getAsJsonObject("d_price").getAsJsonPrimitive("sale").getAsString();
					String unit = retbody.getAsJsonObject("d_price").getAsJsonPrimitive("unit").getAsString();
					int status = retbody.getAsJsonObject("d_stock").getAsJsonPrimitive("status").getAsInt();
					String brand = retbody.getAsJsonObject("d_brand").getAsJsonPrimitive("en").getAsString();
					if(status == 0){
						continue;
					}
					
					//JsonArray l_image_list_arr = retbody.getAsJsonObject("d_image").getAsJsonArray("l_image_list");
					
					JsonArray jsonArr = retbody.getAsJsonArray("l_bread_crumb_list");
					List<String> breads = new ArrayList<String>();
					if(jsonArr != null){
						for(int i=0; i < jsonArr.size(); i++){
							breads.add(jsonArr.get(i).getAsString());
						}
					} else {
						breads.add("home");
						breads.add(title);
					}
					
					JsonObject d_meta_data = retbody.getAsJsonObject("d_meta_data");
					JsonArray d_category = null;
					JsonPrimitive jsonPri = null;
					JsonObject jsonFeat = null;
					if(d_meta_data != null){
						d_category = d_meta_data.getAsJsonArray("d_category");
						jsonPri = d_meta_data.getAsJsonPrimitive("s_gender");
						jsonFeat = d_meta_data.getAsJsonObject("feature_list");
					}
					List<String> cats = new ArrayList<String>();
					if(d_category != null){
						for(int i=0; i < d_category.size(); i++){
							cats.add(jsonArr.get(i).getAsString());
						}
					} else {
						cats.add("home");
						cats.add(title);
					}
					//gender
					String gender = "all";
					if(jsonPri != null && !jsonPri.isJsonNull()){
						gender = jsonPri.getAsString();
					}
					Map<String, Object> properties = new HashMap<String,Object>();
					properties.put("s_gender", gender);
					//feature
					Map<String, Object> featureList = new HashMap<String, Object>();
					if(jsonFeat != null){
						Type type = new TypeToken<Map<String, Object>>(){}.getType();
						featureList = JsonUtils.json2bean(JsonUtils.bean2json(jsonFeat), type);
					}
					
					Map<String, Object> desc = new HashMap<String,Object>();
					int is_html = retbody.getAsJsonObject("d_description").getAsJsonPrimitive("is_html").getAsInt();
					String description = retbody.getAsJsonObject("d_description").getAsJsonPrimitive("en").getAsString();
					if(is_html == 1 ){
						description = Jsoup.parse(description).text();
					}
					desc.put("en", description);
					
					Sku sku = new Sku();
					JsonArray l_selection_list_arr = retbody.getAsJsonArray("l_selection_list");
					JsonArray l_style_list_arr = retbody.getAsJsonArray("l_style_list");
					List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
					Map<String,LSelectionList> style_id_selection = new HashMap<String,LSelectionList>();
					String defaultStyleId = StringUtils.EMPTY;
					for(int i =0; i < l_selection_list_arr.size(); i++){
						JsonObject item = l_selection_list_arr.get(i).getAsJsonObject();
						String style_id = item.getAsJsonPrimitive("style_id").getAsString();
						String good_id = style_id;
						if(item.getAsJsonPrimitive("goods_id") != null && !item.getAsJsonPrimitive("goods_id").isJsonNull()){
							good_id = item.getAsJsonPrimitive("goods_id").getAsString();
						}
						String price_unit = item.getAsJsonPrimitive("price_unit").getAsString();
						int stock_status = item.getAsJsonPrimitive("stock_status").getAsInt();
						String orig_price = item.getAsJsonPrimitive("orig_price").getAsString();
						String sale_price = item.getAsJsonPrimitive("sale_price").getAsString();
						int stock_number = item.getAsJsonPrimitive("stock_number").getAsInt();
						JsonArray selectionArr = item.getAsJsonArray("selection");
						if(orig_price == orig && sale_price == sale){
							defaultStyleId = style_id;
						}
						
						LSelectionList lSelectionList = new LSelectionList();
						lSelectionList.setGoods_id(good_id);
						lSelectionList.setStyle_id(style_id);
						lSelectionList.setOrig_price(Float.valueOf(orig_price));
						lSelectionList.setPrice_unit(price_unit);
						lSelectionList.setSale_price(Float.valueOf(sale_price));
						lSelectionList.setStock_status(stock_status);
						lSelectionList.setStock_number(stock_number);
						List<Selection> selections = new ArrayList<Selection>();
						for(int j =0 ; j < selectionArr.size(); j++){
							JsonObject selectObj = selectionArr.get(j).getAsJsonObject();
							int select_id = selectObj.getAsJsonPrimitive("select_id").getAsInt();
							String select_name = selectObj.getAsJsonPrimitive("select_name").getAsString();
							String select_value = selectObj.getAsJsonPrimitive("select_value").getAsString();
							selections.add(new Selection(select_id, select_value, select_name));
						}
						style_id_selection.put(style_id, lSelectionList);
						lSelectionList.setSelections(selections);
						l_selection_list.add(lSelectionList);
					}
					sku.setL_selection_list(l_selection_list);
					List<LStyleList> l_style_list = new ArrayList<LStyleList>();
					List<Future<Map<String, List<Image>>>> skuFutures = new ArrayList<Future<Map<String, List<Image>>>>();
					for(int i =0; i < l_style_list_arr.size(); i++){
						JsonObject item = l_style_list_arr.get(i).getAsJsonObject();
						String style_switch_img = item.getAsJsonPrimitive("style_switch_img").getAsString();
						//download switch image 
						if(StringUtils.isNotBlank(style_switch_img)){
							Map<String, List<Image>> switch_imgs = new HashMap<String, List<Image>>();
							List<Image> imageList = new ArrayList<Image>();
							imageList.add(new Image(style_switch_img));
							switch_imgs.put(System.currentTimeMillis()+"", imageList);
							service.submit(new ImageCall(url, switch_imgs, pool));
						}
						String style_id = item.getAsJsonPrimitive("style_id").getAsString();
						int style_cate_id = item.getAsJsonPrimitive("style_cate_id").getAsInt();
						String style_name = item.getAsJsonPrimitive("style_name").getAsString();
						String style_cate_name = item.getAsJsonPrimitive("style_cate_name").getAsString();
						JsonArray styleImages_arr = item.getAsJsonArray("style_images");
						
						LSelectionList lSelectionList = style_id_selection.get(style_id);
						LStyleList lStyleList = new LStyleList();
						lStyleList.setStyle_switch_img(style_switch_img);
						lStyleList.setStyle_id(style_id);
						lStyleList.setGood_id(lSelectionList.getGoods_id());
						lStyleList.setStyle_cate_id(style_cate_id);
						lStyleList.setStyle_cate_name(style_cate_name);
						lStyleList.setStyle_name(style_name);
						if(StringUtils.equals(style_id, defaultStyleId)){
							lStyleList.setDisplay(true);	
						}
						List<Picture> style_images = new ArrayList<Picture>();
						Map<String, List<Image>> images = new HashMap<String, List<Image>>();
						List<Image> imageList = new ArrayList<Image>();
						if(styleImages_arr != null){
							for(int j =0 ; j < styleImages_arr.size(); j++){
								String imageUrl = styleImages_arr.get(j).getAsJsonPrimitive().getAsString();
								//style_images.add(new Picture(imageUrl, ""));
								imageList.add(new Image(imageUrl));
							}
						}
						images.put(style_id, imageList);
						Future<Map<String, List<Image>>> f = service.submit(new ImageCall(url, images, pool));
						skuFutures.add(f);
						lStyleList.setStyle_images(style_images);
						l_style_list.add(lStyleList);
						
					}
					sku.setL_style_list(l_style_list);
					
					//sku image
					if(skuFutures.size() > 0){
						//Future<Map<String, List<Image>>> f =  service.submit(new ImageCall(url, images, pool));
						Map<String, List<Image>> result = new HashMap<String, List<Image>>();
						for(Future<Map<String, List<Image>>> f : skuFutures){
							Map<String, List<Image>> skuImages = f.get();
							result.putAll(skuImages);
						}
						for(LStyleList lStyleList : l_style_list){
							List<Image> skuImageList = result.get(lStyleList.getStyle_id());
							if(skuImageList == null){
								continue;
							}
							for(Image img : skuImageList){
								lStyleList.getStyle_images().add(new Picture(img.getOriginalUrl(), img.getRepertoryUrl()));
							}
							
						}
					}
					
					//spu image
					Map<String, List<Image>> images = new HashMap<String, List<Image>>();
					List<Picture> l_image_list = new ArrayList<Picture>();
					JsonArray spu_image_arr = retbody.getAsJsonObject("d_image").getAsJsonArray("l_image_list");
					List<Image> imageList = new ArrayList<Image>();
					if(spu_image_arr != null){
						for(int i = 0 ; i < spu_image_arr.size(); i++){
							String imgUrl = spu_image_arr.get(i).getAsJsonPrimitive().getAsString();
							imageList.add(new Image(imgUrl));
						}
					}
					String skuId = System.currentTimeMillis()+"";
					images.put(skuId, imageList);
					Future<Map<String, List<Image>>> f =  service.submit(new ImageCall(url, images, pool));
					Map<String, List<Image>> result = f.get();
					List<Image> imgList = result.get(skuId);
					if(imgList != null){
						for(Image img : imgList){
							l_image_list.add(new Picture(img.getOriginalUrl(), img.getRepertoryUrl()));
						}
					}
					LImageList lImageList = new LImageList(l_image_list);
					
					ret.setDOCID(docId);
					ret.setSite(new Site("www.amazon.com"));
					ret.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), docId));
					ret.setTitle(new Title(title, "", "", ""));
					ret.setPrice(new Price(Float.valueOf(orig), save, Float.valueOf(sale), unit));
					ret.setStock(new Stock(status));
					ret.setBrand(new Brand(brand, "", "", ""));
					ret.setBreadCrumb(breads);
					ret.setCategory(cats);
					ret.setImage(lImageList);
					ret.setProperties(properties);
					ret.setFeatureList(featureList);
					ret.setDescription(desc);
					ret.setSku(sku);
					CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, check(ret), taskId+"",DocType.INSERT.toString());
					String crawlResult = jsonResult.parseTo();
					System.out.println("crawlResult:"+crawlResult);
					SpiderKafkaResult kafkaResult = producer.sendbyCallBack(topic, crawlResult);
					if(kafkaResult != null){
						System.out.println("send a message offset :"+kafkaResult.getOffset());
					}
					if (count.incrementAndGet() % 1000 == 0) {
		                System.out.println("Consumer process : " + count.get());
		            }
				}
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println("error url:"+url+",json:"+json);
			}
		}
		System.out.println(Thread.currentThread().getName()+"=============stoped============");
	}
	
	
	protected RetBody check(RetBody retBody) {
		List<LSelectionList> l_selection_list = retBody.getSku().getL_selection_list();
		List<LStyleList> l_style_list = retBody.getSku().getL_style_list();
		LStyleList defaultLStyleList = null;
		//find display = true
		for(LStyleList lStyleList : l_style_list){
			boolean display = lStyleList.isDisplay();
			if(display){
				defaultLStyleList = lStyleList;
				break;
			}
		}
		//可能无默认的 LStyleList 全部都是 display = false 
		if(l_style_list.size() > 0 && defaultLStyleList == null){
			defaultLStyleList = l_style_list.get(0);
			defaultLStyleList.setDisplay(true);
		}
		//check stock is or not
		boolean stockFlag = false;
		if(defaultLStyleList != null){
			for(LSelectionList selectionList : l_selection_list){
				if(StringUtils.equals(selectionList.getStyle_id(), defaultLStyleList.getStyle_id())){
					int status = selectionList.getStock_status();
					if(status > 0){
						stockFlag = stockFlag || true;
						//虽然默认sku有库存，仍然检查spu属性是否符合条件 不符合条件 主动修正 
						if(retBody.getStock() == null || retBody.getStock().getStatus() == 0 ){
							retBody.setStock(new Stock(selectionList.getStock_status()));
							float orig =  selectionList.getOrig_price();
							float sale =  selectionList.getSale_price();
							String unit = selectionList.getPrice_unit();
							int save = Math.round((1 - sale / orig) * 100);// discount
							retBody.setPrice(new Price(orig, save, sale, unit));
						}
						if(retBody.getPrice() == null ){
							float orig =  selectionList.getOrig_price();
							float sale =  selectionList.getSale_price();
							String unit = selectionList.getPrice_unit();
							int save = Math.round((1 - sale / orig) * 100);// discount
							retBody.setPrice(new Price(orig, save, sale, unit));
						}
						break;
					} else {
						stockFlag = stockFlag || false;
					}
				}
			}
		}
		//adjust display sku while stock is not
		if(!stockFlag && defaultLStyleList != null){
			for(LStyleList lStyleList : l_style_list){
				boolean stock = false;
				LSelectionList lSelectionList = null;
				for(LSelectionList selectionList : l_selection_list){
					if(StringUtils.equals(selectionList.getStyle_id(), lStyleList.getStyle_id())){
						int status = selectionList.getStock_status();
						if(status > 0){
							stock = stock || true;
							lSelectionList = selectionList;
							break;
						} else {
							stock = stock || false;
						}
					}
				}
				if(stock){
					float orig = lSelectionList.getOrig_price();
					float sale = lSelectionList.getSale_price();
					String unit = lSelectionList.getPrice_unit();
					int save = Math.round((1 - sale / orig) * 100);// discount
					retBody.setPrice(new Price(orig, save, sale, unit));
					lStyleList.setDisplay(true);
					defaultLStyleList.setDisplay(false);
					retBody.setStock(new Stock(lSelectionList.getStock_status()));
					break;
				}
			}
		}
		return retBody;
	}
	
	
	public void stop(){
		queue.notified();
		this.stop = true;
	}
	
	
	
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String url = "http://ecx.images-amazon.com/images/I/41uw5jdjtxL.jpg";
		byte[] imageData = Crawler.create().retry(1).timeOut(30000).proxy(true).proxyAddress("35.185.19.209").proxyPort(3128)
				.url(url).method(HttpMethod.GET.getValue())
				.resultAsBytes();
		FileOutputStream fos = new FileOutputStream("/data/41uw5jdjtxL.jpg");
		fos.write(imageData);
		fos.flush();
		fos.close();
	}

}
