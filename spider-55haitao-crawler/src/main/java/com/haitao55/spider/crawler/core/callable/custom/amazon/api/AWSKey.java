package com.haitao55.spider.crawler.core.callable.custom.amazon.api;


public class AWSKey {
	
	private String accessKeyId;
	private String secretKey;
	private String associateTag;
	
	
	public AWSKey(String accessKeyId, String secretKey, String associateTag) {
		super();
		this.accessKeyId = accessKeyId;
		this.secretKey = secretKey;
		this.associateTag = associateTag;
	}

	public String getAccessKeyId() {
		return accessKeyId;
	}

	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getAssociateTag() {
		return associateTag;
	}

	public void setAssociateTag(String associateTag) {
		this.associateTag = associateTag;
	}
	
	
}


