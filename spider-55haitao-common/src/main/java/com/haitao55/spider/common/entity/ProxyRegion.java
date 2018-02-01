package com.haitao55.spider.common.entity;

public enum ProxyRegion {
	 CN("CN","中国"),
	 US("US","美国"),
	 AU("AU","澳大利亚"),
	 AR("AR","阿根廷"),
	 BR("BR","巴西");

	private final String code;
	private final String region;
	
	public String getCode() {
		return code;
	}

	public String getRegion() {
		return region;
	}

	private ProxyRegion(String code,String region) {
		this.code = code;
		this.region = region;
	}
	
	public static ProxyRegion codeOf(String code,String region) {
		for (ProxyRegion ts : values()) {
			if (ts.getCode().equals(code)&&ts.getRegion().equals(region)) {
				return ts;
			}
		}

		return null;
	}
	
	
}
