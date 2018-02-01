package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
  * @ClassName: DOC
  * @Description: 表示一個文檔
  * @author songsong.xu
  * @date 2016年10月17日 下午6:58:18
  *
 */
public class DOC implements Serializable {

	private static final long serialVersionUID = 2686198253328499362L;
	
	
	private String DOCID;
	private SkuList SkuList;
	public DOC(String dOCID,
			com.haitao55.spider.common.gson.bean.SkuList skuList) {
		super();
		DOCID = dOCID;
		SkuList = skuList;
	}
	public String getDOCID() {
		return DOCID;
	}
	public void setDOCID(String dOCID) {
		DOCID = dOCID;
	}
	public SkuList getSkuList() {
		return SkuList;
	}
	public void setSkuList(SkuList skuList) {
		SkuList = skuList;
	}
	@Override
	public String toString() {
		return "DOC [DOCID=" + DOCID + ", SkuList=" + SkuList + "]";
	}
	
}
