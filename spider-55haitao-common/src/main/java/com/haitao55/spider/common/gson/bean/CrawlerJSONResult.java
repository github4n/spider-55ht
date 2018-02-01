package com.haitao55.spider.common.gson.bean;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.competitor.CtorRetBody;
import com.haitao55.spider.common.gson.bean.taobao.TBMerchantBody;
import com.haitao55.spider.common.gson.bean.taobao.TBRetBody;
import com.haitao55.spider.common.gson.bean.weibo.WeiBoBody;
import com.haitao55.spider.common.utils.ItemEnum;

/**
 * 
 * @ClassName: CrawlerJSONResult
 * @Description: 爬取结果的json根
 * @author songsong.xu
 * @date 2016年9月20日 下午5:13:53
 *
 */
public class CrawlerJSONResult {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private String message;
	private int retcode;
	private RetBody retbody;
	private CtorRetBody ctorRetbody;
	private HaiTunCunRetBody htcRetBody;
	private TBRetBody tbRetBody;
	private TBMerchantBody tbMerchantBody;
	private List<WeiBoBody> wbRetBody;
	private String taskId;
	private String docType;// INSERT DELETE

	public CrawlerJSONResult() {
	}

	public CrawlerJSONResult(String message, int retcode, RetBody retbody, String taskId, String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.retbody = retbody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public CrawlerJSONResult(String message, int retcode, RetBody retbody, String taskId) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.retbody = retbody;
		this.taskId = taskId;
	}

	public CrawlerJSONResult(String message, int retcode, CtorRetBody ctorRetbody, String taskId, String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.ctorRetbody = ctorRetbody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public CrawlerJSONResult(String message, int retcode, HaiTunCunRetBody htcRetBody, String taskId, String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.htcRetBody = htcRetBody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public CrawlerJSONResult(String message, int retcode, TBRetBody tbRetBody, String taskId, String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.tbRetBody = tbRetBody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public CrawlerJSONResult(String message, int retcode, TBMerchantBody tbMerchantBody, String taskId,
			String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.tbMerchantBody = tbMerchantBody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public CrawlerJSONResult(String message, int retcode, List<WeiBoBody> wbRetBody, String taskId, String docType) {
		super();
		this.message = message;
		this.retcode = retcode;
		this.wbRetBody = wbRetBody;
		this.taskId = taskId;
		this.docType = docType;
	}

	public List<WeiBoBody> getWbRetBody() {
		return wbRetBody;
	}

	public void setWbRetBody(List<WeiBoBody> wbRetBody) {
		this.wbRetBody = wbRetBody;
	}

	public TBRetBody getTbRetBody() {
		return tbRetBody;
	}

	public void setTbRetBody(TBRetBody tbRetBody) {
		this.tbRetBody = tbRetBody;
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

	public RetBody getRetbody() {
		return retbody;
	}

	public void setRetbody(RetBody retbody) {
		this.retbody = retbody;
	}

	public CtorRetBody getCtorRetbody() {
		return ctorRetbody;
	}

	public void setCtorRetbody(CtorRetBody ctorRetbody) {
		this.ctorRetbody = ctorRetbody;
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

	public HaiTunCunRetBody getHtcRetBody() {
		return htcRetBody;
	}

	public void setHtcRetBody(HaiTunCunRetBody htcRetBody) {
		this.htcRetBody = htcRetBody;
	}

	public TBMerchantBody getTbMerchantBody() {
		return tbMerchantBody;
	}

	public void setTbMerchantBody(TBMerchantBody tbMerchantBody) {
		this.tbMerchantBody = tbMerchantBody;
	}

	// 实时核价需要使用字段， 其他情况下不设置值，对序列化结果无影响
	private String fromTag;

	public String getFromTag() {
		return fromTag;
	}

	public void setFromTag(String fromTag) {
		this.fromTag = fromTag;
	}

	// 实时核价需要使用字段， 其他情况下不设置值，对序列化结果无影响
	private int rtReturnCode;

	public int getRtReturnCode() {
		return rtReturnCode;
	}

	public void setRtReturnCode(int rtReturnCode) {
		this.rtReturnCode = rtReturnCode;
	}

	@Override
	public String toString() {
		return "CrawlerJSONResult [message=" + message + ", retcode=" + retcode + ", retbody=" + retbody + ", taskId="
				+ taskId + ", docType=" + docType + "]";
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

		if (StringUtils.isNotBlank(this.getDocType())) {
			if (this.getDocType().equals(ItemEnum.DocType.DELETE.toString())) {
				logger.info("item docType is DELETE..");
				return true;// 当docType为DELETE类型跳过检查
			}else if (this.getDocType().equals(ItemEnum.DocType.NOT_SELECT_REQUIRED_PROPERTY.toString())) {
				logger.info("item docType is NOT_SELECT_REQUIRED_PROPERTY..");
				return true;// 当docType为NOT_SELECT_REQUIRED_PROPERTY类型跳过检查
			}
		}

		RetBody retBody = this.getRetbody();
		if (Objects.isNull(retBody)) {
			logger.error("retbody is null");
			return false;
		}

		ProdUrl prod = retBody.getProdUrl();
		if (Objects.isNull(prod)) {
			logger.error("prod url is null");
			return false;
		} else if (StringUtils.isBlank(prod.getUrl()) && StringUtils.isBlank(prod.getUrl_no())) {
			logger.error("url & url_no is null");
			return false;
		}

		String docId = retBody.getDOCID();
		if (StringUtils.isBlank(docId)) {
			logger.error("docID is null");
			return false;
		}

		Site site = retBody.getSite();
		if (Objects.isNull(site)) {
			logger.error("site is null");
			return false;
		} else if (StringUtils.isBlank(site.getHost())) {
			logger.error("host is null");
			return false;
		}

		Title title = retBody.getTitle();
		if (Objects.isNull(title)) {
			logger.error("title is null....");
			return false;
		} else if (StringUtils.isBlank(title.getEn()) && StringUtils.isBlank(title.getCn())
				&& StringUtils.isBlank(title.getDe()) && StringUtils.isBlank(title.getJp())) {
			logger.error("title attribute[en,cn,de,jp] is null....");
			return false;
		}

		Price price = retBody.getPrice();
		if (Objects.isNull(price) || StringUtils.isBlank(price.getUnit())) {
			logger.error("price or unit is null....");
			return false;
		}

		Stock stock = retBody.getStock();
		if (Objects.isNull(stock)) {
			logger.error("stock is null....");
			return false;
		}

		// Brand brand = retBody.getBrand();
		// if (Objects.isNull(brand)) {
		// logger.error("brand is null....");
		// return false;
		// }else if(StringUtils.isBlank(brand.getEn())
		// && StringUtils.isBlank(brand.getCn())
		// && StringUtils.isBlank(brand.getDe())
		// && StringUtils.isBlank(brand.getJp())){
		// logger.error("brand attribute[en,cn,de,jp] is null....");
		// return false;
		// }

		LImageList lImge = retBody.getImage();
		if (Objects.isNull(lImge)) {
			logger.error("lImge is null....");
			return false;
		}

		List<Picture> pictureList = lImge.getL_image_list();
		if (CollectionUtils.isEmpty(pictureList)) {
			logger.error("spu Picture is null....");
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

		/**
		 * 以下三个字段可以为空，不做判断 Map<String, Object> pMap = retBody.getProperties();
		 * if (MapUtils.isEmpty(pMap)) { logger.error("Properties is null....");
		 * return false; }
		 * 
		 * Map<String, Object> featureMap = retBody.getFeatureList(); if
		 * (MapUtils.isEmpty(featureMap)) { logger.error(
		 * "FeatureList is null...."); return false; }
		 * 
		 * Map<String, Object> descriptionMap = retBody.getDescription(); if
		 * (MapUtils.isEmpty(descriptionMap)) { logger.error(
		 * "Description is null...."); return false; }
		 */

		Sku sku = retBody.getSku();
		if (Objects.isNull(sku)) {
			logger.error("sku is null....");
			return false;
		}
		List<LStyleList> lStyleList = sku.getL_style_list();
		if (CollectionUtils.isNotEmpty(lStyleList)) {
			for (LStyleList lStyle : lStyleList) {
				List<Picture> pictures = lStyle.getStyle_images();
				if (CollectionUtils.isEmpty(pictures)) {
					logger.error("LStyleList -> pictures is null....");
					return false;
				} else {
					Picture pic = pictures.get(0);
					if (Objects.isNull(pic)) {
						logger.error("Picture is null....");
						return false;
					} else if (StringUtils.isBlank(pic.getSrc()) || StringUtils.isBlank(pic.getCdn())) {
						logger.error("Picture src or cdn is null....");
						return false;
					}
				}
			}
		}

		return true;
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static CrawlerJSONResult buildFrom(String json) {
		return JsonUtils.json2bean(json, CrawlerJSONResult.class);
	}
}