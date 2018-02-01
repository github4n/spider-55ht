package com.haitao55.spider.common.gson.bean.competitor;

import java.io.Serializable;

/**
 * 
 * @ClassName: Mall
 * @Description: json中的商城节点
 * @author 赵新落
 * @date 2017年2月21日 下午2:15:44
 *
 */
public class Mall implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2173810092375227384L;
	private String mall;
	
	public Mall() {
		super();
	}
	
	public Mall(String mall) {
		super();
		this.mall = mall;
	}

	public String getMall() {
		return mall;
	}
	public void setMall(String mall) {
		this.mall = mall;
	}

	@Override
	public String toString() {
		return "Mall [mall=" + mall + "]";
	}

}