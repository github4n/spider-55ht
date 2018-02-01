package com.haitao55.spider.realtime.view;

/**
 * 
 * 功能：商品数据信息的展示层类
 * 
 * @author Arthur.Liu
 * @time 2016年9月7日 下午6:22:37
 * @version 1.0
 */
public class ItemView {
	/** 商品DOCID */
	private String docId;
	/** 商品的SKUID */
	private String skuId;
	/** 商品数据信息 */
	private String value;

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getSkuId() {
		return skuId;
	}

	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}