package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

/**
 * 
 * @ClassName: Brand
 * @Description: json中的品牌节点
 * @author songsong.xu
 * @date 2016年9月20日 下午2:15:44
 *
 */
public class Brand implements Serializable {
	private static final long serialVersionUID = -8328842303020156009L;
	private String en;
	private String cn;
	private String jp;
	private String de;

	public Brand(String en, String cn) {
		super();
		this.en = en;
		this.cn = cn;
	}
	public Brand(String en, String cn, String jp, String de) {
		super();
		this.en = en;
		this.cn = cn;
		this.jp = jp;
		this.de = de;
	}



	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getJp() {
		return jp;
	}

	public void setJp(String jp) {
		this.jp = jp;
	}

	public String getDe() {
		return de;
	}

	public void setDe(String de) {
		this.de = de;
	}

	@Override
	public String toString() {
		return "Brand [en=" + en + ", cn=" + cn + "]";
	}
}