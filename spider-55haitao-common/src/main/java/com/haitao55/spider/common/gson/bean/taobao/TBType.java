package com.haitao55.spider.common.gson.bean.taobao;

import java.io.Serializable;

/**
 * 标识属于某个业务线结果输出
 * @author denghuan
 *
 */
public class TBType implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String type;

	public TBType() {
		super();
	}
	
	public TBType(String type) {
		super();
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "Type [type=" + type + "]";
	}
	
}
