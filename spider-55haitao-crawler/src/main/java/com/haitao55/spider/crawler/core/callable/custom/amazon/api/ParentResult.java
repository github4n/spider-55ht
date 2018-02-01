package com.haitao55.spider.crawler.core.callable.custom.amazon.api;

import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.context.Context;

public class ParentResult {
	
	private String parentAsin;
	private String itemId;
	private RetBody rebody;
	private Context context;
	
	public ParentResult(String parentAsin, String itemId, RetBody rebody,
			Context context) {
		super();
		this.parentAsin = parentAsin;
		this.itemId = itemId;
		this.rebody = rebody;
		this.context = context;
	}
	public String getParentAsin() {
		return parentAsin;
	}
	public void setParentAsin(String parentAsin) {
		this.parentAsin = parentAsin;
	}
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public RetBody getRebody() {
		return rebody;
	}
	public void setRebody(RetBody rebody) {
		this.rebody = rebody;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}

}
