package com.haitao55.spider.common.dos;

import java.io.Serializable;

/**
 * 
  * @ClassName: 商品图片的实体类
  * @Description: 实体类
  * @author songsong.xu
  * @date 2016年11月16日 下午6:27:57
  *
 */
public class ImageDO implements Serializable {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String id;
    
	private String  doc_id;
	
	private String src;
	
	private String src_key;
	
	private String cdn;
	
	private String cdn_key;
	
	private long create_time;
	
	private long update_time;
	
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDoc_id() {
		return doc_id;
	}

	public void setDoc_id(String doc_id) {
		this.doc_id = doc_id;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getSrc_key() {
		return src_key;
	}

	public void setSrc_key(String src_key) {
		this.src_key = src_key;
	}

	public String getCdn() {
		return cdn;
	}

	public void setCdn(String cdn) {
		this.cdn = cdn;
	}

	public String getCdn_key() {
		return cdn_key;
	}

	public void setCdn_key(String cdn_key) {
		this.cdn_key = cdn_key;
	}

	public long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(long create_time) {
		this.create_time = create_time;
	}

	public long getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(long update_time) {
		this.update_time = update_time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}