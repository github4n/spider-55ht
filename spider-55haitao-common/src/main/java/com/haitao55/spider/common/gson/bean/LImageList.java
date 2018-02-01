package com.haitao55.spider.common.gson.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 
  * @ClassName: LImageList
  * @Description: spu图片节点的内部节点
  * @author songsong.xu
  * @date 2016年9月20日 下午2:13:57
  *
 */
public class LImageList implements Serializable{

	private static final long serialVersionUID = 3811083931203541608L;
	
	private List<Picture> l_image_list;

	public LImageList(List<Picture> l_image_list) {
		super();
		this.l_image_list = l_image_list;
	}

	public List<Picture> getL_image_list() {
		return l_image_list;
	}

	public void setL_image_list(List<Picture> l_image_list) {
		this.l_image_list = l_image_list;
	}

	@Override
	public String toString() {
		return "LImageList [l_image_list=" + l_image_list + "]";
	}
	

}
