package com.haitao55.spider.common.gson.bean;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.utils.ItemEnum;

/**
 * 
 * @ClassName: CrawlerJSONResultV2
 * @Description: 爬取结果的json根
 * @author songsong.xu
 * @date 2016年9月20日 下午5:13:53
 *
 */
public class CrawlerJSONResultV2 {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private String message;
	private int retcode;
	private String format;
	private DOC doc;
	private String taskId;
	private String docType;//INSERT DELETE
	public CrawlerJSONResultV2(){}
	
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getRetcode() {
		return retcode;
	}

	public void setRetcode(int retcode) {
		this.retcode = retcode;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}
	
	public DOC getDoc() {
		return doc;
	}

	public void setDoc(DOC doc) {
		this.doc = doc;
	}

	/**
	 * TODO:不但要检查当前对象的子对象,还要检查当前对象的子对象的子对象,以及更深层次
	 * 
	 * @return
	 */
	public boolean isValid() {
		
		if (StringUtils.isBlank(this.getTaskId())) {
			logger.error("taskId is null");
			return false;
		}
		
		DOC doc = this.getDoc();
		if (Objects.isNull(doc)) {
			logger.error("retbody is null");
			return false;
		}
	
		
		SkuList retBody = doc.getSkuList();
		if (Objects.isNull(retBody)) {
			logger.error("skuList is null");
			return false;
		}
		
		ProdUrl prod = retBody.getProdUrl();
		if (Objects.isNull(prod)) {
			logger.error("prod url is null");
			return false;
		}else if(StringUtils.isBlank(prod.getUrl()) && 
				StringUtils.isBlank(prod.getUrl_no())){
			logger.error("url & url_no is null");
			return false;
		}
		
		if(StringUtils.isNotBlank(this.getDocType())){
			if(this.getDocType().equals(ItemEnum.DocType.DELETE)){
				logger.info("V2 item docType is DELETE..");
				return true;//当docType为DELETE类型跳过检查
			}
		}
		
		String docID = doc.getDOCID();
		if(StringUtils.isBlank(docID)){
			logger.error("docID is null");
			return false;
		}

		Site site = retBody.getSite();
		if (Objects.isNull(site)) {
			logger.error("site is null");
			return false;
		}else if(StringUtils.isBlank(site.getHost())){
			logger.error("host is null");
			return false;
		}

		Title title = retBody.getTitle();
		if (Objects.isNull(title)) {
			logger.error("title is null....");
			return false;
		}else if(StringUtils.isBlank(title.getEn()) 
				&& StringUtils.isBlank(title.getCn())
				&& StringUtils.isBlank(title.getDe())
				&& StringUtils.isBlank(title.getJp())){
			logger.error("title attribute[en,cn,de,jp] is null....");
			return false;
		}

		Price price = retBody.getPrice();
		if (Objects.isNull(price) || StringUtils.isBlank(price.getUnit())) {
			logger.error("price or unit is null....");
			return false;
		}
		//售价大于原始价格
        if(price.getSale() > price.getOrig()){
        	logger.error("salePrice > OrigPrice error..");
			return false;
        }
		
		Stock stock = retBody.getStock();
		if (Objects.isNull(stock)) {
			logger.error("stock is null....");
			return false;
		}

		Brand brand = retBody.getBrand();
		if (Objects.isNull(brand)) {
			logger.error("brand is null....");
			return false;
		}else if(StringUtils.isBlank(brand.getEn()) 
				&& StringUtils.isBlank(brand.getCn())
				&& StringUtils.isBlank(brand.getDe())
				&& StringUtils.isBlank(brand.getJp())){
			logger.error("brand attribute[en,cn,de,jp] is null....");
			return false;
		}
		
		SKUImages skuImge = retBody.getSKUImages();
		if(Objects.isNull(skuImge)){
			logger.error("skuImge is null....");
			return false;
		}

		List<String> breadList = retBody.getBreadCrumb();
		if (CollectionUtils.isEmpty(breadList)) {
			logger.error("breadCrumb is null....");
			return false;
		}

		List<String> categoryList = retBody.getCategory();
		if (CollectionUtils.isEmpty(categoryList)) {
			logger.error("Category is null....");
			return false;
		}

		Map<String, Object> pMap = retBody.getProperties();
		if (MapUtils.isEmpty(pMap)) {
			logger.error("Properties is null....");
			return false;
		}

		Map<String, Object> featureMap = retBody.getFeatureList();
		if (MapUtils.isEmpty(featureMap)) {
			logger.error("FeatureList is null....");
			return false;
		}

		Map<String, Object> descriptionMap = retBody.getDescription();
		if (MapUtils.isEmpty(descriptionMap)) {
			logger.error("Description is null....");
			return false;
		}

		return true;
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static CrawlerJSONResultV2 buildFrom(String json) {
		return JsonUtils.json2bean(json, CrawlerJSONResultV2.class);
	}
}