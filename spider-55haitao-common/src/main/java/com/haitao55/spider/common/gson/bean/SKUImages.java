package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.List;

public class SKUImages implements Serializable{

	private static final long serialVersionUID = 7554970081079038549L;
	private List<Picture> SmallImages;
	private List<Picture> BigImages;
	
	public SKUImages(List<Picture> smallImages, List<Picture> bigImages) {
		super();
		SmallImages = smallImages;
		BigImages = bigImages;
	}
	public List<Picture> getSmallImages() {
		return SmallImages;
	}
	public void setSmallImages(List<Picture> smallImages) {
		SmallImages = smallImages;
	}
	public List<Picture> getBigImages() {
		return BigImages;
	}
	public void setBigImages(List<Picture> bigImages) {
		BigImages = bigImages;
	}
	@Override
	public String toString() {
		return "SKUImages [SmallImages=" + SmallImages + ", BigImages="
				+ BigImages + "]";
	}
	
}
