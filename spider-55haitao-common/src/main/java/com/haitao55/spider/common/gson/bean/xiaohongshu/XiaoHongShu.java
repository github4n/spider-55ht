package com.haitao55.spider.common.gson.bean.xiaohongshu;


import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.LImageList;

/**
 * 小红书 数据json节点
* Title:
* Description:
* Company: 55海淘
* @author denghuan 
* @date 2017年3月23日 上午11:10:56
* @version 1.0
 */
public class XiaoHongShu {
	private String productUrl;//url
	private String title;//标题
	private String text;//正文
	private String nickName;//昵称
	private LImageList Image;//图片
	private String headImage;//头像图片
	
	public XiaoHongShu() {
		super();
	}

	public XiaoHongShu(String productUrl, String title,String text, String nickName,LImageList image,String headImage) {
		super();
		this.productUrl = productUrl;
		this.title = title;
		this.text = text;
		this.nickName = nickName;
		this.Image = image;
		this.headImage = headImage;
	}

	
	public String getProductUrl() {
		return productUrl;
	}

	public void setProductUrl(String productUrl) {
		this.productUrl = productUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public LImageList getImage() {
		return Image;
	}

	public void setImage(LImageList image) {
		Image = image;
	}

	public String getHeadImage() {
		return headImage;
	}

	public void setHeadImage(String headImage) {
		this.headImage = headImage;
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static XiaoHongShu buildFrom(String json) {
		return JsonUtils.json2bean(json, XiaoHongShu.class);
	}

}