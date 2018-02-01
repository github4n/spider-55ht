package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
  * @ClassName: LStyleList
  * @Description: sku的图片引用
  * @author songsong.xu
  * @date 2016年9月20日 下午5:12:24
  *
 */
public class LStyleList implements Serializable{
	
	private static final long serialVersionUID = 3337278096898851471L;
	private String style_switch_img;
	private String good_id;
	private String style_id;
	private long   style_cate_id;
	private String style_cate_name;
	private String style_name;
	private boolean display = false;
	private List<Picture> style_images;
	
	public LStyleList(){}
	public LStyleList(String style_switch_img, String good_id, String style_id,
			long style_cate_id, String style_cate_name, String style_name,
			boolean display, List<Picture> style_images) {
		super();
		this.style_switch_img = style_switch_img;
		this.good_id = good_id;
		this.style_id = style_id;
		this.style_cate_id = style_cate_id;
		this.style_cate_name = style_cate_name;
		this.style_name = style_name;
		this.display = display;
		this.style_images = style_images;
	}



	public String getStyle_switch_img() {
		return style_switch_img;
	}

	public void setStyle_switch_img(String style_switch_img) {
		this.style_switch_img = style_switch_img;
	}
	public String getGood_id() {
		return good_id;
	}
	public void setGood_id(String good_id) {
		this.good_id = good_id;
	}
	public String getStyle_id() {
		return style_id;
	}

	public void setStyle_id(String style_id) {
		this.style_id = style_id;
	}

	public long getStyle_cate_id() {
		return style_cate_id;
	}

	public void setStyle_cate_id(long style_cate_id) {
		this.style_cate_id = style_cate_id;
	}

	public String getStyle_cate_name() {
		return style_cate_name;
	}

	public void setStyle_cate_name(String style_cate_name) {
		this.style_cate_name = style_cate_name;
	}

	public String getStyle_name() {
		return style_name;
	}

	public void setStyle_name(String style_name) {
		this.style_name = style_name;
	}

	public List<Picture> getStyle_images() {
		return style_images;
	}

	public void setStyle_images(List<Picture> style_images) {
		this.style_images = style_images;
	}

	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return "LStyleList [style_switch_img=" + style_switch_img
				+ ", good_id=" + good_id + ", style_id=" + style_id
				+ ", style_cate_id=" + style_cate_id + ", style_cate_name="
				+ style_cate_name + ", style_name=" + style_name + ", display="
				+ display + ", style_images=" + style_images + "]";
	}
	
}
