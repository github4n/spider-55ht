package com.haitao55.spider.common.gson.bean.weibo;


import com.haitao55.spider.common.gson.JsonUtils;

/**
 * 新浪微薄信息字段
 * @author denghuan
 *
 */
public class WeiBoBody {

	private String DOCID;
	private String url;//博主Url
	private String pageUrl;
	private String blogName;//博主名
	private boolean isRstyleMe = false;
	private String followCount;//关注数量
	private String fansCount;//博主粉丝数
	private String wbCount;//博主发布微博数量
	private String containStyleMeUrl;//含有styleMe Url
	private String description;//销量
	
	public WeiBoBody() {
		super();
	}

	
	public WeiBoBody(String dOCID, String url,String pageUrl,String blogName, boolean isRstyleMe, String followCount,
			String fansCount, String wbCount, String containStyleMeUrl, String description) {
		super();
		DOCID = dOCID;
		this.url = url;
		this.pageUrl = pageUrl;
		this.blogName = blogName;
		this.isRstyleMe = isRstyleMe;
		this.followCount = followCount;
		this.fansCount = fansCount;
		this.wbCount = wbCount;
		this.containStyleMeUrl = containStyleMeUrl;
		this.description = description;
	}


	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getContainStyleMeUrl() {
		return containStyleMeUrl;
	}

	public void setContainStyleMeUrl(String containStyleMeUrl) {
		this.containStyleMeUrl = containStyleMeUrl;
	}

	public String getDOCID() {
		return DOCID;
	}
	public void setDOCID(String dOCID) {
		DOCID = dOCID;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getBlogName() {
		return blogName;
	}

	public void setBlogName(String blogName) {
		this.blogName = blogName;
	}

	public boolean isRstyleMe() {
		return isRstyleMe;
	}

	public void setRstyleMe(boolean isRstyleMe) {
		this.isRstyleMe = isRstyleMe;
	}

	public String getFollowCount() {
		return followCount;
	}

	public void setFollowCount(String followCount) {
		this.followCount = followCount;
	}

	public String getFansCount() {
		return fansCount;
	}

	public void setFansCount(String fansCount) {
		this.fansCount = fansCount;
	}

	public String getWbCount() {
		return wbCount;
	}

	public void setWbCount(String wbCount) {
		this.wbCount = wbCount;
	}


	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String parseTo() {
		return JsonUtils.bean2json(this);
	}

	public static WeiBoBody buildFrom(String json) {
		return JsonUtils.json2bean(json, WeiBoBody.class);
	}
}
