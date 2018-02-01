package com.haitao55.spider.crawler.core.callable.custom.saksfifthavenue;


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
public class SaksfifthavenueSelectAllPages extends SelectUrls{
	
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
					for(int i = 1 ;i < pageNumber; i++){
						newUrlValues.add(currentUrl+conts+PAGE_NAO * i);
					}
					newUrlValues.add(currentUrl);//将请求的url添加进去
				}
			}
			Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
			context.getUrl().getNewUrls().addAll(value);
		} catch (Exception e) {
			logger.error("saksfifthavenue crawling list url {} ,exception {}", context.getCurrentUrl(),e);
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
		headers.put("Host", "www.saksfifthavenue.com");
		headers.put("Cookie", "optimizelyEndUserId=oeu1477904586127r0.4722357489711111; __cmbU=ABJeb19YyzYkAHHKueaXZJWQAOqjyTfKpW3k45e1yABqLEiAEJWC08CaYj8O9Dkz_P_Njl-IQ_Kzw0dt4ufRTXOJdSDzOYQ-3w; EndecaNumberOfItems=LOW; Cart=2253999461424099; optimizelySegments=%7B%225678030366%22%3A%22none%22%2C%225681280435%22%3A%22search%22%2C%225663642021%22%3A%22false%22%2C%225639301526%22%3A%22gc%22%7D; optimizelyBuckets=%7B%7D; v11=%5B%5B%27json.cn%27%2C%271486363445828%27%5D%5D; v55=%5B%5B%2706FEB2017%253A02%253A44%253A02%27%2C%271486363445830%27%5D%5D; v50=%5B%5B%27referral%27%2C%271486363445831%27%5D%5D; _ga=GA1.2.81197088.1477904585; s_fid=686B1B9E843FAB5C-0D59EA839643B59B; s_vi=[CS]v1|2C0B8465852AA260-40000301E0005B1D[CE]; sr_browser_id=44afcd15-0fae-4a50-a839-4500290d5600; fsr.r=%7B%22d%22%3A90%2C%22i%22%3A%22d55c592-103974256-e7d9-bf20-e57fc%22%2C%22e%22%3A1499670107660%7D; SA74=3ZzljFTW8-DeUHZE3UCINE9bhWJ-YBuoeTxJ_xHsNjU53FzG1EhJdGw; TS54c0f7=a3e866dce34682a32d7008699710427c41a5b7281182679d59aa4e09556278103fe4df8f286023f23262eb1a60ac0ec5453a7e37866d39d0b12f6551077623711809f75107601fb840b6a4ae5283d66affffffff2396c135d4d3511daf1a552c2b359f937d8b0044d4d3511d48de5bd52b359f93dcfc69801f6193d3; saksBrowserWarnings=true; sr_cr4_exp=2; _abck=87540AEE9AB7BD7F0F53D27E36D712B1A5FE9CC628400000094EAA594A19B16C~0~wC1cPc+DTCguff2RTqtO7okL2B43kXouvQyI2TcY5bE=~-1~-1; AMCVS_5B7B123F5245ADFC0A490D45%40AdobeOrg=1; bm_sz=0C2C99B14366DC382F8984328615E8EA~QAAQdUYRYHgslJpeAQAAkPZpp3Hv+f2bRezjFoRv0dPgfg+JBGPwpVRDAJ8UDRckPs47amOgRto96m45S8lM8lR8z4ROH8rdosei5wPb+tMPA7CoIrA1jajJJ1MF/JQEr7V2cuIVDY8JOD3FHZhb4xvqABDpf2nZ0wa/WnPq7l3Y2SN9W7e8GSAuxWNYfzQYMK5WuqTdKAs=; ak_bmsc=3407BFFBF0583185F9AFD843E7525E013FEB153D3C5300003E75C459AC07BB65~plrzYDgD9DiaiFtiRkc2STthNndg/L/Zy53PASmu4WpxO1bNxXQYID5qZNC8kn+nWyHp2LoreUJ8v6w0plREnrJd0ItanpQ/P2KYI6OoRQeI5yGYdtOrbuGXZqmYo/bse1w3XSTJafDj5BR6wnI3YquZOyuqtk0a7aUzAvCYZ52uWYo3jXQj4XVEv/An7a1pFem6DSjUIhfsWveMowQWFKJv7sr1mT+wYHo1yIef7qyuM2lE5iRUe45JNgkF8hLdtS; TLTSID=AFED50B09F3D109F11B8952B7F1D91E2; JSESSIONID=pcTSZG1f7X5ryGt5H1TTg9Q9g2Mr4tkKy5ygfv2ZK5wThXw9p2GG!-1243242591; saksBagNumberOfItems=0; E4X_COUNTRY=US; E4X_CURRENCY=USD; PSC=null; VIP_PRICE=OFF; PS=DEFAULT; PS_EXP=2017-09-24T22:28:15; PSSALE=DEFAULT; PSSALE_EXP=2017-09-24T22:28:15; BROWSER_GUID=6fe3867a-2d43-472f-b7ab-60c95aa8de96; AMCV_5B7B123F5245ADFC0A490D45%40AdobeOrg=2096510701%7CMCAID%7C2C0B8465852AA260-40000301E0005B1D%7CMCIDTS%7C17432%7CMCMID%7C28690333666309034504203295349042846609%7CMCAAMLH-1506652111%7C9%7CMCAAMB-1506652111%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1506054511s%7CNONE%7CMCSYNCSOP%7C411-17439%7CvVersion%7C2.0.0; cache_buster_cookie=1506047313402; sakscsakey=5fadc27bf674435087b5f0293a42de19; sakscsauvt=23b7aca1e82c450fbb0d3d4e21746cb1_1477904591569_368205535_1506047314524_102; s_tbm14=1; TT3bl=false; TURNTO_VISITOR_SESSION=1; rr_rcs=eF5j4cotK8lM4bEw1TXUNWQpTfZIs0xNMzdNTNI1N7Mw1DUxMDXRNUpKBRLJpqZpSamJZompKQCBUg6W; TURNTO_TEASER_SHOWN=1506048618018; _sp_id.7a58=2277e271-0230-4a27-a9ae-1ebea8091409.1477904796.86.1506048719.1498817158.8612c7a3-d9da-4fbb-b519-7fed12634ec7; sf_wdt_sidebar_store=phoenix-store; cm=undefinedSaks%20Networkundefined; bm_sv=479C9C8B0A5E26A32CBD570CB6D2279B~3jgL0POkMX0wdf7sAeahrpWmabx93tpmOg2HVkmF5ZJlpH2XyXSrZjfgTHtKWRF14BgZidhyG+9zvnC56WNXl5YLdqmKp2fPBJumlhHBOakqj+x58mDkbIRmXug81mlVvamANzQ3GWyhgS541Nxsd9JwhKJmHsHDRmqlKdv2Tjg=; TLTHID=E3A0346E9F46109F0E89EAA1F95C1443; mbox=PC#1477904582897-122024.28_87#1507260848|session#01c67d7616734c1bb796247a12607748#1506053108|check#true#1506051308; EML1145A=TRUE; sessionID=1506047306579iDDDGEzfUfsfLZCTTRVT8pu-yZED6v6Rr6TIsZ9LrqWCrQa9ZI2kE1a6; _bcvm_vid_4405020731204665754=718356838093738099TCFBA498F172651207731666A7A8ACFFC9F87C0F577C5C99AA5844B729DF1557E62211CE9193C9340AAC282FBB125F2C983B4B441C5349C4ED9720ED6D4530761; _bcvm_vrid_4405020731204665754=718075410698619881TF1109F0F7041C22A23079B330DE2A9D175CAAC5DD0C424E72D3D353413CC4CD542D330D75990CD896FA0FB321661414F6B693ABD33F22233524847FC8768EF36; _br_uid_2=uid%3D7414203368134%3Av%3D12.0%3Ats%3D1477904590058%3Ahc%3D768; c38=saks.com%3Athemensstore%3Aaccessories%3Apensdeskaccessories; pn=6; v0=1; s_sq=%5B%5BB%5D%5D; s_cc=true; _mibhv=anon-1489396153255-4826432379_3292; usy46gabsosd=sakscsa__368205535_1506051252845_1506047314524_1072; sakscsaDBID=13_null; sf_siterefer=salesfloor; sf_wdt_session_expiration_session=1506051257604; sf_wdt_customer_id=z9x2j5fs8; sf_wdt_fingerprint=99516111153704; _loop_ga=GA1.2.7273c09a-511c-47cb-84e2-61ff17b16364; _loop_ga_gid=GA1.2.1027809603.1506047342; _gat_loop_tracker=1; s_ppvl=saks.com%253Aproduct%2520detail%253Agucci%253Adragon%2520mules%2520with%2520crystal%2520heels%2C11%2C11%2C335%2C1301%2C229%2C1366%2C768%2C1%2CP; s_ppv=saks.com%253Athemensstore%253Aaccessories%253Apensdeskaccessories%2C2%2C2%2C229%2C1301%2C229%2C1366%2C768%2C1%2CP; bc_pv_end=718356877557666479T5BAA2C9431BFC374735808199C6B05602A824D21FD7E472AA57068F8159EE27C43EF96C43AABED25821C2B0F0745B41DAC4F4C9841440FA81489232DC558476F; _4c_=jVNdT%2BNIEPwrp3ngKY7n%2ByMSWjnArThBgL3VvkZje4ytOHZ2xkmIEP%2BdHkNW5IRuNy%2Be7ip3asrVz2hfuw7NiMASC0Kl0IZO0ModApo9I9%2BU8bFDM2S55FhbVXCrJNa8zKtcYlqJglhdUoMm6CnOIZRgw5iSVL9MUNmUN%2F3j3%2FfHKeSUxgkTBmjF5p3wjLa%2BBV49DJswS9P9fj8NdhWqphpqu3Pd1k2Lfp3eui7NisKF0PvGhfTedSGxXZlcurBKPiKh7jfpMl0kgq70zzpduES2uy4HIaDs0ZXXcH1U2TY4aN350vnlYrvOnYc2HlkH2w4HqKC4t4e164ZbN9Q9WDMSBufX4FaEc9%2Fvw%2FjmRe37tftLaOj2Eb1puu0TFN5VzvuRA1VoBgenT64IYNGXESRmyqY02U0Z9CpwCtELPNccvpWgWUYlTjiGH8PkCh5iTi7jTW6%2Ff1vOr7KLu8UHP0P8w2JoD2H41NccGGkEoGibnUtJ%2Bs%2B%2FCZnKNGDJuRBGc04EE1%2Byh%2Fk5OVs35TnV0mDGmJSSYThxgTnFjBrBuIEjKJXYnNlI%2FZ3ws%2Bzh6jyGZBPTEs1r%2B8K20QaI6QR9zZbXl1AoqliBjU0EIUXCVZEnmjuaSFJVROVEMsmPJvwY3%2FgTz8oYhdJVdtsOCGJ5mlRJoRV1jUnV%2F4UVA%2FgtAP9DEnEGJHyEyTsMuyeJ4kRJAXEaACZ8lIfjCrnqnQ6L%2BTaNaaowE9LEaVH023IdlZ%2FSDFZxE5tfi3yCU6bUuIJ%2BfxT1EfiE%2BvLyCg%3D%3D; sf_change_page=true");
		return headers;
	}
}
