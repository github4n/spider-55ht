package com.haitao55.spider.crawler.core.callable.custom.saksoff5th;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: Saksfifthavenue
  * @author denghuan
  * @date 2016年10月31日 下午2:19:57
  *
 */
public class Saksoff5thSelectAllPages extends SelectUrls{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final int PAGE_NAO = 60;
	
	@Override
	public void invoke(Context context) throws Exception {
		try {
			String currentUrl = context.getCurrentUrl();
			String content = crawlerUrl(context,currentUrl);
			List<String> newUrlValues = new ArrayList<String>();
			if(StringUtils.isNotBlank(content)){
				String pageTotal = StringUtils.substringBetween(content, "total_results\":\"", "\"}");
				String conts = "";
				if(StringUtils.isNotBlank(pageTotal)){
					if(StringUtils.containsIgnoreCase(currentUrl, "?")){
						conts = "&Nao=";
					}else{
						conts = "?Nao=";
					}
					Double page  = Double.valueOf(pageTotal) / Double.valueOf(60);
					int pageNumber=  (int)Math.ceil(page);
					for(int i = 0 ;i < pageNumber; i++){
						newUrlValues.add(currentUrl+conts+PAGE_NAO * i);
					}
					//newUrlValues.add(currentUrl);//将请求的url添加进去
				}
			}
			Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
			context.getUrl().getNewUrls().addAll(value);
		} catch (Exception e) {
			logger.error("Saksoff5th crawling list url {} ,exception {}", context.getCurrentUrl(),e);
		}
		
	}
	private String crawlerUrl(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.saksoff5th.com");
		headers.put("Cookie", "f5_cspm=1234; __cmbU=ABJeb1_2DAx1yWHkIbU5DQVCRIoQJ9td4tbsxl0GPMV_sfw7ze33YrR15vxJhaMdifTbMBsblFsix8rhtGNOO3E7LSwsWV_UgQ; EndecaNumberOfItems=LOW; s_fid=1C7F9FF681E0389B-0BC771E1F20421CE; previousSort=default:New Arrivals; TS01478360=01fb51fadc5151759b56b2af636931fc01ea21f8098f172d720d1e948838e36d6f11c5b6921fe946f1cceac4d7aca6aa8c7fd75700b4414f58c04857836c7d551f1772631fdf5302d66995ece3ac0f18366a0afe63f84774bf1d4e1b1c77928d4c6ffd4e44; TS01b3990f=01fb51fadc0f03f7f67e020945b49b6c2656bcd3afd2fd0075d6a9f1cb0736062cfb8c84491e66fec20013285c88b9f04f38dc1b7eea57d8647bf7ce4746768341a3e053bef7440cba06012bc511b6cdf8e3fea34155929853aff31fca65640bfcdf48782caa844a30dbbfc13986270646b5c8f63f; f5_cspm=1234; saksBrowserWarnings=true; _abck=4B4A66A439EABF89DF8B3EC3F18AC52CA5FE105AB00C0000354EAA59214C4843~0~nq30e/NglYDJtcBjD+Ved+3X3qJvtM8YO5JQ8Li/wJE=~-1~-1; AMCVS_5B7B123F5245ADFC0A490D45%40AdobeOrg=1; TS01c36232=01ba79e615f082cb99278201f8eb061a9c4f332841c03a04fb4f3744ac7d4632a6ff247ae4dc1c46a5c3d124969d98c63eeecbf2734bc7cede1d8b4f46117c327b25588637ec5aeaeca2e3d3d7c580d04f80f91716; BIGipServerprod_fe_prod-so5-web-vip.digital.hbc.com_ltmpool_80=3057785516.20480.0000; v11=%5B%5B%27AFF001%2520lbHlpxiEbDY%27%2C%271504333378394%27%5D%2C%5B%27json.cn%27%2C%271505368552217%27%5D%5D; v55=%5B%5B%2702SEP2017%253A02%253A22%253A58%27%2C%271504333378395%27%5D%2C%5B%2714SEP2017%253A01%253A55%253A52%27%2C%271505368552223%27%5D%5D; v50=%5B%5B%27affiliates%27%2C%271504333378396%27%5D%2C%5B%27referral%27%2C%271505368552227%27%5D%5D; sr_browser_id=44afcd15-0fae-4a50-a839-4500290d5600; s_tbm14=1; s_cpc=0; bm_sz=A19EB0A65096AFAB56188B83E23840FB~QAAQtVisxsVdDqBeAQAAJR6dp6bBd8Fdlp+Uj6RZ9qE9V034+Q/hromSEkf3IkvR5YSFE3KvQsRtcID74WouFydeQZE0i+Izmg3PrWzqEMx1tCrKQAvcLyEAd8TgXRd0Jkt4lLjzE53FrbLhMQeN8Zx/D5DqQd7sWlBk/j8xtVBEd8RSD7e568fTXCTUTM60oEzT; ak_bmsc=4F60C85541244BB4B7A17E8874B497D6C6AC58B5D13B000060A3C459B262CA43~pltGYEX2xkZTq9PWM+KE6lar+E4buULrdhRY0rdiRYgkAP19oWXmyjAQhC2pwN2KoHcoHerojMCJ6wzlqKxOXwc4wfL9EXGnByjSGp/jVZnKfUbVmGV5w7sdMQZSHlHzPTk2KWDhZ8zgUcwlC0qmd9rfOu2a+m0H8IKewiEdGzdDxHpVCiD39EH7KRSBBV/1uuC0HOtUcu0Ppi9s7aOO6LoyXVpJ6eP2zW5h9yqkBFi1U=; _cavisit=15ea81e4596|; _caid=d4087b2b-e4c8-46cc-90d0-b1c5ec287d76; JSESSIONID=RS1sZGjYKzG2w85HZxyjBZ5xV8BvQTrvrvt0M29y1QF0DktMl1Zk!1640175221; saksBagNumberOfItems=0; E4X_COUNTRY=US; E4X_CURRENCY=USD; BROWSER_GUID=32c77a0b-380e-480d-ba3f-5b4e65506433; TS0192147a=01fb51fadc05b954160aca6c1fa0387a046a6e0f8ad4e1578da7832b1a7dfad5641af8b1c3bb6aee982b0a7ef1f3ecf966b0d4983aa1f28da12fb826cb2591e15ee00070905affa5cf9e153189ae696b1b11b01dd98aef0a15f6a0d6e9969668f6daa4293734de8adbf4994652ba4a669610064ab71ecf393d8ffbfcbf447a326c5520a87cca3b4ab2dd7d522d6ca3bbe487b203da3e1d43c46c56bfdc2d9a7405437368cf; AMCV_5B7B123F5245ADFC0A490D45%40AdobeOrg=2096510701%7CMCIDTS%7C17432%7CMCMID%7C28690333666309034504203295349042846609%7CMCAAMLH-1506655458%7C9%7CMCAAMB-1506664035%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1506066435s%7CNONE%7CMCSYNCSOP%7C411-17439%7CvVersion%7C2.0.0; s_sq=%5B%5BB%5D%5D; _sp_id.2b02=2e092279-ed18-4327-8566-6b279ccccba9.1489396795.8.1506059969.1505368890.41f75b3a-6d30-40a9-9532-c6919ba9df29; _sp_ses.2b02=*; TS0137785d=01ba79e6151905d80f4905106b2a1559b067e6808a87992f4ec79ab3bff203c2b1de5cfde5efcb0695e9470953209c0c1dbc3ce9d93cd6deb6d9dfccd6067a3ed22e1f55e2cd7f561ec702bd628d29fab25b277cb6; rr_rcs=eF5jYSlN9kizTE0zN01M0jU3szDUNTEwNdE1SkoFEsmmpmlJqYlmiakpXLllJZkpfKbGZrqGuoYAmgcOyQ; TS01660e15=01fb51fadcdecea423336a56a5b01235156e72f8ae5ea2131b71a5da179ebf6294591ad04964255b285365140b2472fde1d72e1250c491dac446e9a3b7ec43b46f838cf82d749a36ceed7609c6eb57b77e4af8a75ff3681c87b37f17dbb9030ac20e20b4bc; bm_sv=9273C6F618D4C835D0EE4026D05FAEED~L2tbt1IglnftkGRgTD7tc3F1aEuEQ0+qwsdWoVCtvtUNokG0bJ/nGszn7VTNzVzu0DxaA6JMuYTd3r015HEntMGis62ugjDhToGgq2k2G2N1EAoVqKI+cagzoQmUIy9iiNQ5i8m263CjgSOHp/N7k6DnCvI8OH8PmXgdcqlDKPs=; mbox=PC#1477904582897-122024.28_26#1507270539|session#d5a5451f19f94c4a8b640579b78af58c#1506062799|check#true#1506060999; EML1145A=TRUE; sessionID=1506050654920Q7GTVT6XpR1EAW2KLlSfAZfJFw1611OgZHGm2xFyKtne2tneQ0Hygr1o; _bcvm_vid_4405020731204665754=718356838093738099TCFBA498F172651207731666A7A8ACFFC9F87C0F577C5C99AA5844B729DF1557E62211CE9193C9340AAC282FBB125F2C983B4B441C5349C4ED9720ED6D4530761; _bcvm_vrid_4405020731204665754=718075410698619881TF1109F0F7041C22A23079B330DE2A9D175CAAC5DD0C424E72D3D353413CC4CD542D330D75990CD896FA0FB321661414F6B693ABD33F22233524847FC8768EF36; c38=off%205th%3Ashoesbags%3Ashoes%3Apumps; pn=3; v0=1; cm=undefinedSaks%20Networkundefined; s_cc=true; s_ppvl=off%25205th%253Ashoesbags%253Ashoes%253Apumps%2C2%2C2%2C229%2C1301%2C229%2C1366%2C768%2C1%2CP; s_ppv=off%25205th%253Ashoesbags%253Ashoes%253Apumps%2C2%2C2%2C229%2C1301%2C229%2C1366%2C768%2C1%2CP; bc_pv_end=718356974504307739T96AA899C0D095A801992B224D2D6DF86CCC9A8156815C064C0E8E96793BC85B6D2E9360AD1EF395BF089C1B76BFC6936CC008F5F1E1A31E33226067573531EFD; _4c_=dVJNT%2BM6FP0rIy9Y0caOE39UqlDh8SRGUGCYfeXENyRDElex09JB%2FHeu27IoM6Mu4vPh5N7T80a2NfRkxnIq8KfzVNHsnLzAzpPZGxkaGx8bMiOZzDjTopRaFhIUK0zBKM%2BAlxqkZUDOyev%2BPVKwnNM8Fez9nNjG3rrn%2Fx8%2B38K%2B2FSqoq1cHw1vZBxa9NUhrGdJst1up968eFdVeainpeuSp9qBn5jeTi7Nsz%2FA5GHs1j7xtVsnq2Q5yX6HivfJEibil%2BF9f7E0bi4ofhuHeQZ7gxuTyrQ%2BTn0%2FWBhWy7ErYED64NqZNuwQIViUpRv7cAAPZtdBH1Z3EGqH2ZAlkk%2FgfeP61c1%2FSIy9harpwaIQYOgwx3ixGNzW7z9wVQ%2Bug2%2B5QtZF9bbpx1cEA1QwDHsPIt8EwNPp9siXzkae6SmfppPNlCNXYXyk4tro1EirBNO8kMqKMheWZ1ylZSV1XPXu54%2FV5fXi6n55zNhjyDh8gDK0Ox%2F2YVdNFWqzgX6EfeIFOpIoIOhhe5imbTaQsOT704RNReIzplPKmKZSKkkvFo%2BXc3bWNXaeKqEp51wIwSmespxmKeWpznmm8agygbU7Wzxez2M31rEkcaXWlaaNi2I7sUbxD8NczdgGgn35UiGOVLy5rxD%2FQ9YoH%2FL%2Ft0mL2Faojnp61FnKqOaKcZVHHcc49PhzllMbxwjQ1ny6zIme51KmGerD9mg4Ef5ifX%2F%2FAA%3D%3D");
		return headers;
	}
	
}
