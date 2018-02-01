package com.haitao55.spider.crawler.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.competitor.CtorRetBody;
import com.haitao55.spider.common.gson.bean.taobao.TBMerchantBody;
import com.haitao55.spider.common.gson.bean.taobao.TBRetBody;
import com.haitao55.spider.common.gson.bean.weibo.WeiBoBody;
import com.haitao55.spider.common.utils.CrawlerJSONResultUtils;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：封装根据一个Url抓取和解析后的产出结果，等待最终的输出
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:12:04
 * @version 1.0
 */
public class OutputObject {

    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private static final String JSON_BODY_KEY = "retbody";
	private static final String JSON_BODY_COMPARTIOTOR_KEY = "ctorRetbody";
	private static final String JSON_BODY_TAOBAO_KEY = "tbRetbody";
	private static final String JSON_BODY_TAOBAO_MERCHANT_KEY = "tbmcRetbody";
	private static final String JSON_BODY_WEIBO_KEY = "wbRetbody";
	/** 任务ID */
	private String taskId;
	/** 输出方式；默认输出到本地文件中 */
	private OutputChannel outputChannel = OutputChannel.FILE;
	/** 商品图片的数据集合 */
	private Map<String, List<Image>> images = new HashMap<String, List<Image>>();
	/** 由当前这条Url执行抓取之后产生的商品数据 */
	private Map<String, String> newItem = new ConcurrentHashMap<String, String>();

	private DocType docType;

	/**
	 * 这个OutputObject类中,持有Url对象,是因为在后期实际输出数据时,需要用到Url对象中的一些原始控制信息,
	 * 如下载图片时需要用到的代理IP信息等
	 */
	private Url url;

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public OutputChannel getOutputChannel() {
		return outputChannel;
	}

	public void setOutputChannel(OutputChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	/*
	 * public Set<Image> getImages() { return images; }
	 * 
	 * public void setImages(Set<Image> images) { this.images = images; }
	 */
	public Map<String, String> getNewItem() {
		return newItem;
	}
	public Map<String, List<Image>> getImages() {
		return images;
	}

	public void setImages(Map<String, List<Image>> images) {
		this.images = images;
	}

	public void putItemField(String key, String value) {
		this.newItem.put(key, value);
	}

	public Url getUrl() {
		return url;
	}

	public void setUrl(Url url) {
		this.url = url;
	}

	public DocType getDocType() {
		return docType;
	}

	public void setDocType(DocType docType) {
		this.docType = docType;
	}

	public String convertItem2Json() {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, String> entry : this.newItem.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}

		return jsonObject.toJSONString();
	}

	public String convertItem2Message() {
		String result = StringUtils.EMPTY;
		if (DocType.INSERT.equals(getDocType())) {
			result = itemInsert2Json();
		} else if (DocType.DELETE.equals(getDocType())) {
			result = itemDelete2Json();
		}else if(DocType.NOT_SELECT_REQUIRED_PROPERTY.equals(getDocType())){
			result = itemNotSelectRequiredPropertyJson();
		}
		return result;
	}
	
	private String itemNotSelectRequiredPropertyJson(){
		RetBody body = new RetBody();
		body.setProdUrl(new ProdUrl(url.getValue()));
		body.setStock(new Stock(0));
		CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
				DocType.NOT_SELECT_REQUIRED_PROPERTY.toString());
		return jsonResult.parseTo();
	}

	private String itemDelete2Json() {
		RetBody body = new RetBody();
		body.setProdUrl(new ProdUrl(url.getValue()));
		body.setStock(new Stock(0));
		CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
				DocType.DELETE.toString());
		return jsonResult.parseTo();
	}

	private String itemInsert2Json() {
		String retboby = newItem.get(JSON_BODY_KEY);
		//竞品数据
		String ctorRetbody = newItem.get(JSON_BODY_COMPARTIOTOR_KEY);
		//淘宝全球购
		String tbRetbody = newItem.get(JSON_BODY_TAOBAO_KEY);
		//淘宝商家
		String tbMerchantbody = newItem.get(JSON_BODY_TAOBAO_MERCHANT_KEY);
		//微博数据
		String wbRetbody = newItem.get(JSON_BODY_WEIBO_KEY);
		
		if (StringUtils.isNotBlank(retboby)) {
			RetBody body = JsonUtils.json2bean(retboby, RetBody.class);
			List<Picture> spuImage = new ArrayList<Picture>();
			Sku sku = body.getSku();
			if(sku != null){
				List<LStyleList> l_style_list = sku.getL_style_list();
				List<LSelectionList> l_selection_list = sku.getL_selection_list();
				if(CollectionUtils.isNotEmpty(l_selection_list)){
					buildLSelectionListSkuId(sku,l_selection_list);
				}
				if(l_style_list != null && l_style_list.size() > 0 ){
					for (LStyleList styleList : l_style_list) {
						String skuId = styleList.getGood_id();
						List<Image> images = getImages().get(skuId);
						
						if(images == null){
						    logger.info("skuId>> {},>>>>>>>images is null ",skuId ); 
							continue;
						}
						images.forEach(img -> {
						    logger.info("skuId>> {},>>>>>>>images>>>>  {} ",skuId ,img.toString());
                        });
						List<Picture> style_images = new ArrayList<Picture>();
						for (Image image : images) {
							style_images.add(new Picture(image.getOriginalUrl(), image.getRepertoryUrl()));
						}
						styleList.setStyle_images(style_images);
						if (styleList.isDisplay()) {
							spuImage.addAll(style_images);
						}
					}
				} else {
					for(List<Image> coll : getImages().values()){
						for (Image image : coll) {
							spuImage.add(new Picture(image.getOriginalUrl(), image.getRepertoryUrl()));
						}
					}
				}
			}
			
			LImageList lImageList = new LImageList(spuImage);
			body.setImage(lImageList);
			body.setSku(sku);
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
					DocType.INSERT.toString());
			//System.out.println(jsonResult.parseTo());
			return jsonResult.parseTo();
		}
		//竞品数据
		else if(StringUtils.isNotBlank(ctorRetbody)){
			CtorRetBody body = JsonUtils.json2bean(ctorRetbody, CtorRetBody.class);
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
					DocType.INSERT.toString());
			return jsonResult.parseTo();
		}
		//淘宝全球购数据
		else if(StringUtils.isNotBlank(tbRetbody)){
			TBRetBody tbBody = JsonUtils.json2bean(tbRetbody, TBRetBody.class);
			List<Picture> tbImage = new ArrayList<Picture>();
			for(List<Image> coll : getImages().values()){
				for (Image image : coll) {
					tbImage.add(new Picture(image.getRepertoryUrl()));
				}
			}
			LImageList lImageList = new LImageList(tbImage);
			tbBody.setImage(lImageList);
			
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, tbBody, this.getTaskId(),
					DocType.INSERT.toString());
			return jsonResult.parseTo();
		}else if(StringUtils.isNotBlank(tbMerchantbody)){
			TBMerchantBody body = JsonUtils.json2bean(tbMerchantbody, TBMerchantBody.class);
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
					DocType.INSERT.toString());
			return jsonResult.parseTo();
		}else if(StringUtils.isNotBlank(wbRetbody)){
			List<WeiBoBody> body = JsonUtils.json2bean(wbRetbody, ArrayList.class);
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, this.getTaskId(),
					DocType.INSERT.toString());
			return jsonResult.parseTo();
		}
		
		return StringUtils.EMPTY;
	}

	/**
	 * 创建l_selection_list 的每个LSelectionList skuid值
	 * @param sku
	 * @param l_selection_list
	 */
	private void buildLSelectionListSkuId(Sku sku, List<LSelectionList> l_selection_list) {
		List<LSelectionList> l_selection_list_new = new ArrayList<LSelectionList>();
		for (LSelectionList lSelectionList : l_selection_list) {
			String skuId = CrawlerJSONResultUtils.buildSkuid(lSelectionList);
			lSelectionList.setSku_id(skuId);
			l_selection_list_new.add(lSelectionList);
		}
		if(CollectionUtils.isNotEmpty(l_selection_list_new)){
			sku.setL_selection_list(l_selection_list_new);
		}
	}

	/**
	 * 把item数据转化成scd格式数据 待修改
	 * 
	 * @return
	 */
	public String convertItem2Scd() {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, String> entry : this.newItem.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}

		return jsonObject.toJSONString();
	}

}