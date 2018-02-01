package com.haitao55.spider.data.service.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.common.utils.SpiderStringUtil;

/**
 * 清洗URL,获取URL中的productId
 * @author denghuan
 * date : 2017-06-12
 */
public class CleaningUrlUtil {

	private static final String CARTERS_DOMAIN = "www.carters.com";
	private static final String BERGDORFGOODMAN_DOMAIN = "www.bergdorfgoodman.com";
	private static final String ESCENTUAL_DOMAIN = "www.escentual.com";
	private static final String BACKCOUNTRY_DOMAIN = "www.backcountry.com";
	private static final String ZH_ASHFORD_DOMAIN = "zh.ashford.com";
	private static final String _6PM_DOMAIN = "www.6pm.com";
	private static final String _FOOTLOCKER_DOMAIN = "www.footlocker.com";
	private static final String _ASOS_DOMAIN = "www.asos.com";
	private static final String _SHOPSPRING_DOMAIN = "www.shopspring.com";
	private static final String _RALPHLAUREN_DOMAIN = "www.ralphlauren.com";
	private static final String _LEVI_DOMAIN = "www.levi.com";
	
	public static String cleanUrl(String url){
		String docId = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(url, CARTERS_DOMAIN)){
			url = subString(url);
			String productId = StringUtils.substringBetween(url, "V_", ".html");
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringBetween(url, "VC_", ".html");
			}
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(CARTERS_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, BERGDORFGOODMAN_DOMAIN)){
			url = subString(url);
			Pattern pattern =Pattern.compile("(prod\\d+)");
			Matcher matcher = pattern.matcher(url);
			if(matcher.find()){
				String productId =  matcher.group(1);
				if(StringUtils.isNotBlank(productId)){
					docId = SpiderStringUtil.md5Encode(BERGDORFGOODMAN_DOMAIN+productId);
				}
			}
		}else if(StringUtils.containsIgnoreCase(url, ESCENTUAL_DOMAIN)){
			String temp = url;
			if (temp.endsWith("/")) {
				temp = temp.substring(0, temp.length() - 1);
			}
			String productId = temp.substring(temp.lastIndexOf("/") + 1);
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(ESCENTUAL_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, BACKCOUNTRY_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "skid=", "-");
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(productId + BACKCOUNTRY_DOMAIN);
			}
		}else if(StringUtils.containsIgnoreCase(url, ZH_ASHFORD_DOMAIN)){
			String productId = url.substring(url.lastIndexOf("/")+1, url.lastIndexOf(".pid"));
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(ZH_ASHFORD_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _6PM_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "product/", "/color");
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_6PM_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _FOOTLOCKER_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "model:", "/");
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_FOOTLOCKER_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _ASOS_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "prd/", "/");
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringBetween(url, "iid=", "&");
			}
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringAfter(url, "prd/");
			}
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringAfter(url, "iid=");
			}
			
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_ASOS_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _SHOPSPRING_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "products/", "?");
			if(StringUtils.isBlank(productId)){
				 productId = StringUtils.substringAfter(url, "products/");
			}
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_SHOPSPRING_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _RALPHLAUREN_DOMAIN)){
			String productId = StringUtils.substringBetween(url, "productId=", "&");
			if(StringUtils.isBlank(productId)){
				productId = StringUtils.substringAfter(url, "productId=");
			}
			if(StringUtils.isBlank(productId)){
				Pattern p = Pattern.compile("product/(\\d+)");
				Matcher m = p.matcher(url);
				while(m.find()){
					productId = m.group(1);
				}
			}
			
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_RALPHLAUREN_DOMAIN+productId);
			}
		}else if(StringUtils.containsIgnoreCase(url, _LEVI_DOMAIN)){
			if(url.contains("?")){
				url = url.substring(0, url.indexOf("?"));
			}
			String productId = StringUtils.substringAfter(url, "p/");
			if(StringUtils.isNotBlank(productId)){
				docId = SpiderStringUtil.md5Encode(_LEVI_DOMAIN+productId);
			}
		}
		
		return docId;
	}
	
	private static String subString(String url){
		if(StringUtils.containsIgnoreCase(url, "?")){
			url = StringUtils.substring(url, 0, url.indexOf("?"));
		}
		return url;
	}
	
	public static void main(String[] args) {
		String url = "http://www.levi.com//US/en_US/levi/p/198890033";
		//String rs = CleaningUrlUtil.cleanUrl(url);
		if(url.contains("?")){
			url = url.substring(0, url.indexOf("?"));
		}
		String productId = StringUtils.substringAfter(url, "p/");
		
		
		 //String productId = StringUtils.substringBetween(url, "products/", "?");
		//String productId = StringUtils.substringBetween(url, "iid=", "&");
		
//		String url = "http://www.bergdorfgoodman.com/Alice-Olivia-Aurelia-Off-the-Shoulder-Floral-Gown-White-Gowns/prod128680144_cat368009__/p.prod?icid=&searchType=EndecaDrivenCat&rte=%252Fcategory.jsp%253FitemId%253Dcat368009%2526pageSize%253D30%2526No%253D0%2526refinements%253D&eItemId=prod128680144&cmCat=product";
//		String rs = CleaningUrlUtil.cleanUrl(url);
//		
		
		System.out.println(productId);
	}
}
