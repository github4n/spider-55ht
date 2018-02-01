package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * 
  * @ClassName: Title
  * @Description: 爬取结果json的标题节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:07:23
  *
 */
public class Title implements Serializable{

	private static final long serialVersionUID = 6102540524318373439L;
	
	private String en;
	private String cn;
	private String jp;
	private String de;
	
	public Title(String en, String cn) {
		super();
		this.en = en;
		this.cn = cn;
	}
	public Title(String en, String cn, String jp, String de) {
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
		return "Title [en=" + en + ", cn=" + cn + ", jp=" + jp + ", de=" + de
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cn == null) ? 0 : cn.hashCode());
		result = prime * result + ((de == null) ? 0 : de.hashCode());
		result = prime * result + ((en == null) ? 0 : en.hashCode());
		result = prime * result + ((jp == null) ? 0 : jp.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Title other = (Title) obj;
		
		if(StringUtils.isNotBlank(this.getCn()) && 
				StringUtils.isNotBlank(other.getCn())){
			if(!this.getCn().equals(other.getCn())){
				return false;
			}
		}
		
		if(StringUtils.isNotBlank(this.getDe()) && 
				StringUtils.isNotBlank(other.getDe())){
			if(!this.getDe().equals(other.getDe())){
				return false;
			}
		}
		
		if(StringUtils.isNotBlank(this.getEn()) && 
				StringUtils.isNotBlank(other.getEn())){
			if(!this.getEn().equals(other.getEn())){
				return false;
			}
		}
		
		if(StringUtils.isNotBlank(this.getJp()) && 
				StringUtils.isNotBlank(other.getJp())){
			if(!this.getJp().equals(other.getJp())){
				return false;
			}
		}
		return true;	
	}
}
