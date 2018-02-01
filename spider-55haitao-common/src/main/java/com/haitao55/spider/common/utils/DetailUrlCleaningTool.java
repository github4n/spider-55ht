package com.haitao55.spider.common.utils;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * 
 * 功能：工具类,用来清理商品详情页链接功能的
 * 
 * @author Arthur.Liu
 * @time 2016年11月28日 上午11:01:19
 * @version 1.0
 */
public class DetailUrlCleaningTool {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);
	private static final String HOST_SEPARATE = "?";
	private static final String PARAMS_SEPARATE = "&";
	private static final String PROD = "/prod/pgeproduct.aspx";
	private static final String AMAZON_STANDARD_URL = "https://www.amazon.com/dp/{0}/";
	

	private static class Holder {
		private static DetailUrlCleaningTool instance = new DetailUrlCleaningTool();
	}

	public static DetailUrlCleaningTool getInstance() {
		return Holder.instance;
	}

	// <Pattern, methodName>
	private Map<Pattern, String> PATTERN_CACHE = new ConcurrentHashMap<Pattern, String>();
	// <domain, Method>
	private Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<String, Method>();

	/**
	 * 方法功能:通过反射调用方法, 达到清洗商品链接格式的目的
	 * 
	 * @param originalUrl
	 * @return
	 */
	public String cleanDetailUrl(String originalUrl) {
		String temp = StringUtils.substringAfter(originalUrl, "//");
		String domain = StringUtils.substringBefore(temp, "/");
		Method method = METHOD_CACHE.get(domain);

		if (!Objects.isNull(method)) {
			try {
				Object rst = method.invoke(this, originalUrl);
				return String.valueOf(rst);
			} catch (Exception e) {
				logger.error("", e);
			}
		}

		for (Entry<Pattern, String> entry : PATTERN_CACHE.entrySet()) {
			Pattern key = entry.getKey();
			String value = entry.getValue();

			if (key.matcher(domain).find()) {
				try {
					Method meth = this.getClass().getDeclaredMethod(value, String.class);

					METHOD_CACHE.put(domain, meth);

					Object rst = meth.invoke(this, originalUrl);
					return String.valueOf(rst);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}

		return originalUrl;
	}

	/**
	 * 测试方法
	 */
	public static void main(String... args) {
		DetailUrlCleaningTool tool = DetailUrlCleaningTool.getInstance();
		/*String src = "http://www1.macys.com/shop/product/bar-iii-zip-front-fit-flare-scuba-dress-only-at-macys?ID=2673388&CategoryID=5449#fn=sp%3D1%26spc%3D3004%26ruleId%3D77|BS|BA%26slotId%3D5";
		String rst = tool.cleanDetailUrl(src);
		System.out.println(rst);

		src = "http://shop.samsonite.com/business/samsonite-business-womens-spinner-mobile-office/57475XXXX.html?dwvar_57475XXXX_color=Black&cgidmaster=blaz-bl060";
		rst = tool.cleanDetailUrl(src);
		System.out.println(rst);

		src = "http://www.6pm.com/nine-west-toujours-dark-wine-dark-wine";
		rst = tool.cleanDetailUrl(src);
		System.out.println(rst);
		
		
	    src = "http://cn.feelunique.com/1130126.html/asdasd";
		rst = tool.cleanCnFeelunique(src);
		System.out.println(rst);
		
		src = "http://www.ralphlauren.com/product/index.jsp?productId=108780816&cp=1760782?.1760895&ab=tn_women_cs_sweaters&parentPage=family";
		rst = tool.cleanDetailUrl(src);
		System.out.println(rst);*/
		
		/*String src = "https://www.amazon.com/Amazon-Fire-7-Inch-Tablet-8GB/dp/B00TSUGXKE/ref=gbps_tit_s-3_bb19_f0b839ae?smid=ATVPDKIKX0DER&pf_rd_p=41fd713f-6bfe-4299-a021-d2b94872bb19&pf_rd_s=slot-3&pf_rd_t=701&pf_rd_i=gb_main&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=W7AWMJF54CVE6K8ZWP91";
		src = "https://www.amazon.com/Nickelodeon-TMN4008-Teenage-Mutant-Turtles/dp/B00H33C5ZK/ref=gbps_tit_s-3_bb19_d2d8faab?smid=ATVPDKIKX0DER&pf_rd_p=41fd713f-6bfe-4299-a021-d2b94872bb19&pf_rd_s=slot-3&pf_rd_t=701&pf_rd_i=gb_main&pf_rd_m=ATVPDKIKX0DER&pf_rd_r=KFZ9B6Q1DX998405SM3T";
	    src = "https://www.amazon.com/gp/product/B01N00GQIY";*/
	    //String src = "https://www.amazon.com/dp/B01N00GQIY";
	    //String src = "https://www.amazon.com/Aldo-51136426-Grawn-Shoulder-Handbag/dp/B01N00GQIY";
		//(.*/dp/)
	    //System.out.println(tool.cleanDetailUrl(src) ); 
		/*Pattern pattern = Pattern.compile("(https://|http://)www.amazon.com/(.*dp/|.*product/)([a-zA-Z0-9]*)[/]{0,1}");
		Matcher matcher = pattern.matcher(src);
		if(matcher.find()){
		    System.out.println(matcher.group(3));
		}*/
		/*src = "https://cn.shopbop.com/vintage-organdy-mini-dress-nicholas/vp/v=1/1524920713.htm?folderID=2534374302155172&fm=other-shopbysize-viewall&os=false&colorId=12926";
		System.out.println(src);
		System.out.println(tool.cleanDetailUrl(src));
		
		src = "http://www.asos.com/chi-chi-london/chi-chi-london-satin-2-in-1-printed-midi-dress/prd/7618851?iid=7618851&clr=Pinkmulti&cid=19316&pgesize=36&pge=0&totalstyles=212&gridsize=3&gridrow=1&gridcolumn=1";
		System.out.println(src);
		System.out.println(tool.cleanDetailUrl(src));
		
		src = "http://www.michaelkors.com/crystal-embroidered-floral-lace-flared-jumpsuit/_/R-US_465RKH084?No=-1&color=0001";
		System.out.println(src);
		System.out.println(tool.cleanDetailUrl(src));*/
		String url = "http://www.asos.com/asos/asos-jumper-in-oversized-with-crew-neck/prd/8240169/?cid=111";
		//System.out.println(tool.cleanDetailUrl(url));
		System.out.println(tool.cleanAsos(url));
//		String src = "http://www.saksfifthavenue.com/main/ProductDetail.jsp?PRODUCT%3C%3Eprd_id=845524447088557";
//		System.out.println(tool.cleanSaksFifthAvenue(src));
	}
	// =============================================================================================//
	// ====================================这个文件,只需要修改这块区域之下的内容===========================//
	// =============================================================================================//
	private DetailUrlCleaningTool() {// 私有构造方法
		// ***********************************************************************************//
		// *****重要提示::这里的key是正则表达式,value是方法名称；方法内容一旦写完确定,以后便不能再改变了*****//
		// ***********************************************************************************//
		PATTERN_CACHE.put(Pattern.compile(".*www1.macys.com.*"), "cleanMacys");
//		PATTERN_CACHE.put(Pattern.compile(".*cn.feelunique.com.*"), "cleanCnFeelunique");
		PATTERN_CACHE.put(Pattern.compile(".*www.jomashop.com.*"), "cleanJomaShop");
		PATTERN_CACHE.put(Pattern.compile(".*www.finishline.com.*"), "cleanFinishline");
//		PATTERN_CACHE.put(Pattern.compile(".*www.c21stores.com.*"), "cleanC21stores");
		PATTERN_CACHE.put(Pattern.compile(".*www.lookfantastic.com.*"), "cleanLookfantastic");
		PATTERN_CACHE.put(Pattern.compile(".*www.selfridges.com.*"), "cleanSelfridges");
		PATTERN_CACHE.put(Pattern.compile(".*www.mikihouse.jp.*"), "cleanMikiHouse");
		PATTERN_CACHE.put(Pattern.compile(".*www.beautybay.com.*"), "cleanBeautybay");
		PATTERN_CACHE.put(Pattern.compile(".*www.dinos.co.jp.*"), "cleanDinos");
		PATTERN_CACHE.put(Pattern.compile(".*www.lordandtaylor.com.*"), "cleanLordAndTaylor");
		PATTERN_CACHE.put(Pattern.compile(".*www.michaelkors.com.*"), "cleanMichaelkors");
		PATTERN_CACHE.put(Pattern.compile(".*www.feelunique.com.com.*"), "cleanWwwFeelunique");
		PATTERN_CACHE.put(Pattern.compile(".*row.feelunique.com.*"), "cleanRowFeelunique");
		PATTERN_CACHE.put(Pattern.compile(".*cn.feelunique.com.*"), "cleanCnFeelunique");
		PATTERN_CACHE.put(Pattern.compile(".*zh.ashford.com.*"), "cleanAshford");
		PATTERN_CACHE.put(Pattern.compile(".*www.cosme-de.com.*"), "cleanCosmeDe");
		PATTERN_CACHE.put(Pattern.compile(".*www.kohls.com.*"), "cleanKohls");
		PATTERN_CACHE.put(Pattern.compile(".*www.ssense.com.*"), "cleanSsense");
		PATTERN_CACHE.put(Pattern.compile(".*www.famousfootwear.com.*"), "cleanFamousfootwear");
		PATTERN_CACHE.put(Pattern.compile(".*www.topshop.com.*"), "cleanTopshop");
		PATTERN_CACHE.put(Pattern.compile(".*www.kipling.com.*"), "cleanKipling");
		PATTERN_CACHE.put(Pattern.compile(".*www.nordstromrack.com.*"), "cleanNordstromrack");
		PATTERN_CACHE.put(Pattern.compile(".*www.swarovski.com.*"), "cleanSwarovski");
		PATTERN_CACHE.put(Pattern.compile(".*www.harrods.com.*"), "cleanHarrods");
		PATTERN_CACHE.put(Pattern.compile(".*www.backcountry.com.*"), "cleanBackcountry");
		PATTERN_CACHE.put(Pattern.compile(".*www.mybag.com.*"), "cleanMybag");
		PATTERN_CACHE.put(Pattern.compile(".*www.everlane.com.*"), "cleanEverlane");
		PATTERN_CACHE.put(Pattern.compile(".*www.abercrombie.com.*"), "cleanAbercrombie");
		PATTERN_CACHE.put(Pattern.compile(".*www.ninewest.com.*"), "cleanNinewest");
		PATTERN_CACHE.put(Pattern.compile(".*www.sneakersnstuff.com.*"), "cleanSneakersnstuff");
		PATTERN_CACHE.put(Pattern.compile(".*www.perfumania.com.*"), "cleanPerfumania");
		PATTERN_CACHE.put(Pattern.compile(".*www.neimanmarcus.com.*"), "cleanNeimanmarcus");
		PATTERN_CACHE.put(Pattern.compile(".*www.yoox.com.*"), "cleanYooxcom");
		PATTERN_CACHE.put(Pattern.compile(".*www.yoox.cn.*"), "cleanYooxcn");
		PATTERN_CACHE.put(Pattern.compile(".*www.skinstore.com.*"), "cleanSkinstore");
		PATTERN_CACHE.put(Pattern.compile(".*www.maccosmetics.com.*"), "cleanMaccosmetics");
		PATTERN_CACHE.put(Pattern.compile(".*surprise.katespade.com.*"), "cleanSurpriseKatespade");
		PATTERN_CACHE.put(Pattern.compile(".*www.madewell.com.*"), "cleanMadewell");
		PATTERN_CACHE.put(Pattern.compile(".*www.spacenk.com.*"), "cleanSpacenk");
		PATTERN_CACHE.put(Pattern.compile(".*askderm.com.*"), "cleanAskderm");
		PATTERN_CACHE.put(Pattern.compile(".*www.shoebuy.com.*"), "cleanShoebuy");
		PATTERN_CACHE.put(Pattern.compile(".*www.gymboree.com.*"), "cleanGymboree");
		PATTERN_CACHE.put(Pattern.compile(".*www.hqhair.com.*"), "cleanHqhair");
		PATTERN_CACHE.put(Pattern.compile(".*www.disneystore.com.*"), "cleanDisneystore");
		PATTERN_CACHE.put(Pattern.compile(".*www.storets.com.*"), "cleanStorets");
		PATTERN_CACHE.put(Pattern.compile(".*www.nautica.com.*"), "cleanNautica");
		PATTERN_CACHE.put(Pattern.compile(".*www.columbia.com.*"), "cleanColumbia");
		PATTERN_CACHE.put(Pattern.compile(".*www.urbanoutfitters.com.*"), "cleanUrbanoutfitters");
		
		PATTERN_CACHE.put(Pattern.compile(".*www.ralphlauren.com.*"), "cleanRalphlauren");
		PATTERN_CACHE.put(Pattern.compile(".*www.escentual.com.*"), "cleanEscentual");
		PATTERN_CACHE.put(Pattern.compile(".*www.bluefly.com.*"), "cleanBlueFly");
		PATTERN_CACHE.put(Pattern.compile(".*www.rebeccaminkoff.com.*"), "cleanRebeccaMinkoff");
		PATTERN_CACHE.put(Pattern.compile(".*www.mankind.co.uk.*"), "cleanMankind");
		PATTERN_CACHE.put(Pattern.compile(".*www.groupon.com.*"), "cleanGroupOn");
		PATTERN_CACHE.put(Pattern.compile(".*www.joesnewbalanceoutlet.com.*"), "cleanJoesNewBalanceOutlet");
		PATTERN_CACHE.put(Pattern.compile(".*www.colehaan.com.*"), "cleanColeHaan");
		PATTERN_CACHE.put(Pattern.compile(".*www.skincarerx.com.*"), "cleanSkincarerx");
		PATTERN_CACHE.put(Pattern.compile(".*www.pixiemarket.com.*"), "cleanPixiemarket");
		PATTERN_CACHE.put(Pattern.compile(".*www.sportsdirect.com.*"), "cleanSportsdirect");
		
		PATTERN_CACHE.put(Pattern.compile(".*www.levi.com.*"), "cleanLevi");
		
		PATTERN_CACHE.put(Pattern.compile(".*www.6pm.com.*"), "clean_6pm");
		PATTERN_CACHE.put(Pattern.compile(".*shop.nordstrom.com.*"), "cleanNordstrom"); 
		PATTERN_CACHE.put(Pattern.compile(".*www.victoriassecret.com.*"), "cleanVictoriassecret");
		PATTERN_CACHE.put(Pattern.compile(".*www.katespade.com.*"), "cleanKatespade");
		PATTERN_CACHE.put(Pattern.compile(".*www.marcjacobs.com.*"), "cleanMarcjacobs");
		PATTERN_CACHE.put(Pattern.compile(".*www.yslbeautyus.com.*"), "cleanYslBeautyus");
		PATTERN_CACHE.put(Pattern.compile(".*www.55shantao.com.*"), "clean55shantao");
		PATTERN_CACHE.put(Pattern.compile(".*www.amazon.com.*"), "cleanUsAmazon");
				
		PATTERN_CACHE.put(Pattern.compile(".*www.zappos.com.*"), "cleanZappos");
		PATTERN_CACHE.put(Pattern.compile(".*www.sephora.com.*"), "cleanSephora");
		PATTERN_CACHE.put(Pattern.compile(".*shop.samsonite.com.*"), "cleanSamsonite");
		PATTERN_CACHE.put(Pattern.compile(".*www.saksfifthavenue.com.*"), "cleanSaksFifthAvenue");
		PATTERN_CACHE.put(Pattern.compile(".*www.mytheresa.com.*"), "cleanMytheresa");
		PATTERN_CACHE.put(Pattern.compile(".*www.foreo.com.*"), "cleanForeo");
		PATTERN_CACHE.put(Pattern.compile(".*www.forzieri.com.*"), "cleanForzieri");
		PATTERN_CACHE.put(Pattern.compile(".*tommy.com.*"), "cleanTommy");
		PATTERN_CACHE.put(Pattern.compile(".*www.allbeauty.com.*"), "cleanAllbeauty");
		PATTERN_CACHE.put(Pattern.compile(".*www.superdrug.com.*"), "cleanSuperdrug");
		PATTERN_CACHE.put(Pattern.compile(".*cn.shopbop.com.*"), "cleanShopbop");
		PATTERN_CACHE.put(Pattern.compile(".*www1.bloomingdales.com.*"), "cleanBloomingdales");
		PATTERN_CACHE.put(Pattern.compile(".*www.onlineshoes.com.*"), "cleanOnlineshoes");
		PATTERN_CACHE.put(Pattern.compile(".*www.matchesfashion.com.*"), "cleanMatchesfashion");
		PATTERN_CACHE.put(Pattern.compile(".*www.asos.com.*"), "cleanAsos");
		PATTERN_CACHE.put(Pattern.compile(".*www.ebags.com.*"), "cleanEbags");
		PATTERN_CACHE.put(Pattern.compile(".*www.jomalone.com.*"), "cleanJomalone");
		PATTERN_CACHE.put(Pattern.compile(".*www.juicycouture.com.*"), "cleanJuicycouture");
		PATTERN_CACHE.put(Pattern.compile(".*www.unineed.com.*"), "cleanUnineed");
		PATTERN_CACHE.put(Pattern.compile(".*www.esteelauder.com.*"), "cleanEsteelauder");
		PATTERN_CACHE.put(Pattern.compile(".*www.toryburch.com.*"), "cleanToryburch");
		PATTERN_CACHE.put(Pattern.compile(".*www.shopspring.com.*"), "cleanShopspring");
		PATTERN_CACHE.put(Pattern.compile(".*www.bergdorfgoodman.com.*"), "cleanBergdorfgoodman");
		PATTERN_CACHE.put(Pattern.compile(".*www.houseoffraser.co.uk.*"), "cleanHouseoffraser");
		PATTERN_CACHE.put(Pattern.compile(".*www.sierratradingpost.com.*"), "cleanSierratradingpost");
		PATTERN_CACHE.put(Pattern.compile(".*www.clinique.com.*"), "cleanClinique");
		PATTERN_CACHE.put(Pattern.compile(".*www.farfetch.com.*"), "cleanFarfetch");
		PATTERN_CACHE.put(Pattern.compile(".*www.lastcall.com.*"), "cleanLastcall");
		PATTERN_CACHE.put(Pattern.compile(".*www.b-glowing.com.*"), "cleanBglowing");
		PATTERN_CACHE.put(Pattern.compile(".*www.saksoff5th.com.*"), "cleanSaksoff5th");
		PATTERN_CACHE.put(Pattern.compile(".*www.jimmyjazz.com.*"), "cleanJimmyjazz");
		PATTERN_CACHE.put(Pattern.compile(".*www.giorgioarmanibeauty-usa.com.*"), "cleanGiorgioarmanibeauty");
		PATTERN_CACHE.put(Pattern.compile(".*www.glamglow.com.*"), "cleanGlamglow");
		PATTERN_CACHE.put(Pattern.compile(".*www.narscosmetics.com.*"), "cleanNarscosmetics");
		PATTERN_CACHE.put(Pattern.compile(".*www.footlocker.com.*"), "cleanFootlocker");
		PATTERN_CACHE.put(Pattern.compile(".*www.coach.com.*"), "cleanCoach");
		PATTERN_CACHE.put(Pattern.compile(".*www.theory.com.*"), "cleanTheory");
		
		//h5清洗规则
		PATTERN_CACHE.put(Pattern.compile(".*m.6pm.com.*"), "clean_6pm_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.lookfantastic.com.*"), "clean_lookfantastic_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.skinstore.com.*"), "clean_skinstore_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.disneystore.com.*"), "clean_disneystore_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.zappos.com.*"), "clean_zappos_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.ralphlauren.com.*"), "clean_ralphlauren_h5");
		PATTERN_CACHE.put(Pattern.compile(".*m.katespade.com.*"), "clean_katespade_h5");
		
	}
	
	@SuppressWarnings("unused")
	private String cleanTheory(String originalUrl){
		String sourceUrl = originalUrl.replaceAll("http:", "https:");
		if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "&")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("&"));
		}else if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "?")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanCoach(String originalUrl){
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanFootlocker(String originalUrl){
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String clean_6pm_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.6pm.com", "www.6pm.com").replace("https://", "http://");
		return clean_6pm(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_lookfantastic_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.lookfantastic.com", "www.lookfantastic.com");
		return cleanLookfantastic(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_skinstore_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.skinstore.com", "www.skinstore.com");
		return cleanSkinstore(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_disneystore_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.disneystore.com", "www.disneystore.com");
		return cleanDisneystore(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_zappos_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.zappos.com", "www.zappos.com");
		return cleanZappos(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_ralphlauren_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.ralphlauren.com", "www.ralphlauren.com");
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String clean_katespade_h5(String originalUrl) {
		originalUrl = StringUtils.replace(originalUrl, "m.katespade.com", "www.katespade.com");
		return cleanKatespade(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanNarscosmetics(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanGlamglow(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanGiorgioarmanibeauty(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanJimmyjazz(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanSaksoff5th(String originalUrl) {
		return recombineSaksFifthAvenueUrl(originalUrl, "prd_id");
	}
	@SuppressWarnings("unused")
	private String cleanBglowing(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanLastcall(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanFarfetch(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanClinique(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanSierratradingpost(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanHouseoffraser(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanBergdorfgoodman(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanShopspring(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanToryburch(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "?")){
			return originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanEsteelauder(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanUnineed(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanJuicycouture(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanJomalone(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanEbags(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanAsos(String originalUrl) {
		if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "?")){
			String proUrl = StringUtils.substring(originalUrl, 0, originalUrl.indexOf("?"));
			String cid = asosPattern(originalUrl);
			if(StringUtils.isNotBlank(cid)){
				return proUrl+"?"+cid;
			}
		}
		return originalUrl;
		
	}
	@SuppressWarnings("unused")
	private String cleanMatchesfashion(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanOnlineshoes(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanForzieri(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanTommy(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanAllbeauty(String originalUrl) {
		return originalUrl;
	}	
	@SuppressWarnings("unused")
	private String cleanSuperdrug(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanShopbop(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanBloomingdales(String originalUrl) {
		return recombineUrl(originalUrl, "ID");
	}
	
	@SuppressWarnings("unused")
	private String cleanMacys(String originalUrl) {
		String rst=StringUtils.replacePattern(originalUrl, "&CategoryID=.*", "");
		return rst;
	}

	private String cleanCnFeelunique(String originalUrl) {
		String rst=StringUtils.replacePattern(originalUrl, "(?<=.html)(.*)", "");
		return rst;
	}
	
	@SuppressWarnings("unused")
	private String cleanRalphlauren(String originalUrl) {
		originalUrl = originalUrl.replace("https://", "http://");
		String rst = recombineUrl(originalUrl, "productId");
		return rst;
	}
	
	@SuppressWarnings("unused")
	private String cleanEscentual(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanSkincarerx(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanBlueFly(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanRebeccaMinkoff(String originalUrl) {
		String url = getMainUrl(originalUrl);//此处得到的是该网站干净的url
		//由于在爬取的时候未清洗url，但是该网站已经上线，不便再做更改，经观察测试发现未清洗的url只是在干净的url后面
		//多了一个参数?src=catalog，为了能匹配查询，故而将其加上。
		return url+"?src=catalog";
	}
	
	@SuppressWarnings("unused")
	private String cleanMankind(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanPixiemarket(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean_6pm(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanNordstrom(String originalUrl) {
		if(StringUtils.isBlank(originalUrl)){
			return null;
		}
		String sourceUrl = originalUrl.replaceAll("http:", "https:");
		return getMainUrl(sourceUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanJomaShop(String originalUrl) {
		if(StringUtils.isBlank(originalUrl)){
			return null;
		}
		String sourceUrl = originalUrl.replaceAll("http:", "https:");
		if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "&")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("&"));
		}else if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "?")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanFinishline(String originalUrl) {
		if(StringUtils.isBlank(originalUrl)){
			return null;
		}
		String sourceUrl = originalUrl.replaceAll("http:", "https:");
		if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "&")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("&"));
		}else if(StringUtils.isNotBlank(originalUrl) && 
				StringUtils.containsIgnoreCase(originalUrl, "?")){
			originalUrl = originalUrl.substring(0, originalUrl.indexOf("?"));
		}
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanZappos(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanSephora(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
//	@SuppressWarnings("unused")
//	private String cleanC21stores(String originalUrl) {
//		return getMainUrl(originalUrl);
//	}
	
	@SuppressWarnings("unused")
	private String cleanSamsonite(String originalUrl) {
		String url = recombineUrl(originalUrl, "color");
		return url.replaceAll("\"", "");
	}
	
	@SuppressWarnings("unused")
	private String cleanVictoriassecret(String originalUrl) {
	    String trimUrl = StringUtils.trim(originalUrl);
        if(StringUtils.isBlank(trimUrl)){
            return null;
        }
        String sourceUrl = trimUrl.replaceAll("http:", "https:");
	    String mainUrl = StringUtils.substringBefore(sourceUrl, HOST_SEPARATE);
        Pattern pattern = Pattern.compile(".*ProductID=(\\d+)[&]{0,1}.*");
        Matcher matcher = pattern.matcher(sourceUrl);
        String paramPattern = "ProductID={0}&CatalogueType=OLS";
        if(matcher.find()){
            String productId = matcher.group(1);
            String params = MessageFormat.format(paramPattern, productId);
           return Joiner.on("?").join(mainUrl,params);
        }
        return null;
	}
	
	private String recombineSaksFifthAvenueUrl(final String url, String... keyParams){
		String trimUrl = StringUtils.trim(url);
		
		if(StringUtils.isBlank(trimUrl)){
		    return null;
		}
		String mainUrl = StringUtils.substringBefore(trimUrl, HOST_SEPARATE);
		String attr = StringUtils.substringAfter(trimUrl, HOST_SEPARATE);
		
		
		String[] params = StringUtils.split(attr, PARAMS_SEPARATE);
		StringBuffer buff = new StringBuffer();
		if(params != null){
			int len = keyParams.length;
			for (int i=0; i<len; i++) {
				for (String str : params) {
					if(str.contains(keyParams[i])){
						if(i==0)//调用该方法时要注意传入关键参数的顺序
							buff.append(HOST_SEPARATE).append(str);
						else
							buff.append(PARAMS_SEPARATE).append(str);
						break;
					}
				}
			}
		}
		if(StringUtils.isNotBlank(buff.toString()))
			trimUrl = mainUrl+buff.toString();
		
		return trimUrl;
		
	}
	
	private String cleanSaksFifthAvenue(String originalUrl) {
		return recombineSaksFifthAvenueUrl(originalUrl, "prd_id");
	}
	
	@SuppressWarnings("unused")
	private String cleanLookfantastic(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanSelfridges(String originalUrl) {
		originalUrl = StringUtils.substringBefore(originalUrl, "&");
		if(StringUtils.contains(originalUrl, "_$ja")){
			originalUrl = getMainUrl(originalUrl);
		}
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanForeo(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanMytheresa(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanMikiHouse(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanBeautybay(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanDinos(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanLordAndTaylor(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanMichaelkors(String originalUrl) {
		String url = StringUtils.replacePattern(originalUrl, "&isTrends.*", "");
		url = StringUtils.replacePattern(url, "No=\\d+", "");
		return url;
	}
	@SuppressWarnings("unused")
	private String cleanWwwFeelunique(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanRowFeelunique(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanAshford(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanCosmeDe(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanKohls(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanSsense(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanFamousfootwear(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanTopshop(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanKipling(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanNordstromrack(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanSwarovski(String originalUrl) {
		if(StringUtils.containsIgnoreCase(originalUrl, "Mob_US")){
			originalUrl = originalUrl.replace("Mob_US", "Web_US");
		}
		originalUrl = originalUrl.replace("http://", "https://");
		String newUrl = getMainUrl(originalUrl);
		if(StringUtils.isNotBlank(newUrl)){
			newUrl = newUrl.replaceAll("  ", "");
			return newUrl;
		}
		 return null;
	}
	@SuppressWarnings("unused")
	private String cleanHarrods(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanBackcountry(String originalUrl) {
		String url = StringUtils.replacePattern(originalUrl, "&ti.*", "");
		return url;
	}
	@SuppressWarnings("unused")
	private String cleanMybag(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanEverlane(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanAbercrombie(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanNinewest(String originalUrl) {
		String url = StringUtils.replacePattern(originalUrl, "&cgid.*", "");
		return url;
	}
	@SuppressWarnings("unused")
	private String cleanSneakersnstuff(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanPerfumania(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	
	@SuppressWarnings("unused")
	private String cleanNeimanmarcus(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanYooxcom(String originalUrl) {
		String url = StringUtils.replacePattern(originalUrl, "#.*", "");
		return url;
	}
	@SuppressWarnings("unused")
	private String cleanYooxcn(String originalUrl) {
		String url = StringUtils.replacePattern(originalUrl, "#.*", "");
		return url;
	}
	@SuppressWarnings("unused")
	private String cleanSkinstore(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanMaccosmetics(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanSurpriseKatespade(String originalUrl) {
		return originalUrl;
	}
	
	
	@SuppressWarnings("unused")
	private String cleanMadewell(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanSpacenk(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanAskderm(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanShoebuy(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanGymboree(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanHqhair(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanDisneystore(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanStorets(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	@SuppressWarnings("unused")
	private String cleanNautica(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanColumbia(String originalUrl) {
		return originalUrl;
	}
	@SuppressWarnings("unused")
	private String cleanUrbanoutfitters(String originalUrl) {
		return originalUrl;
	}
	
	@SuppressWarnings("unused")
	private String cleanKatespade(String originalUrl) {
		originalUrl = originalUrl.replace("http://", "https://");
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanMarcjacobs(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanYslBeautyus(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String clean55shantao(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanSportsdirect(String originalUrl) {
		return recombineUrl(originalUrl, "colcode");
	}
	
	@SuppressWarnings("unused")
	private String cleanLevi(String originalUrl) {
		originalUrl = getMainUrl(originalUrl);
		if(!originalUrl.contains("www.levi.com//"))
			originalUrl = StringUtils.replace(originalUrl, "www.levi.com/", "www.levi.com//");
		return originalUrl;
	}
	
	private String cleanUsAmazon(String originalUrl) {
	    Pattern pattern = Pattern.compile("(https://|http://)www.amazon.com/(.*dp/|.*product/|.*/aw/d/)([a-zA-Z0-9]*)[/]{0,1}");
        Matcher matcher = pattern.matcher(originalUrl);
		if(matcher.find()){
		    String asinId = matcher.group(3);
		    if(StringUtils.isNotBlank(asinId)){
		        return MessageFormat.format(AMAZON_STANDARD_URL, asinId);
		    }
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private String cleanGroupOn(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	
	@SuppressWarnings("unused")
	private String cleanJoesNewBalanceOutlet(String originalUrl) {
		String url = getMainUrl(originalUrl);
		if(!StringUtils.containsIgnoreCase(url, "product"))
			return url;
		String preffix = StringUtils.substringBeforeLast(url, "product");
		url = preffix + "product" + StringUtils.upperCase(StringUtils.substringAfterLast(url, "product"));
		return url;
	}
	
	@SuppressWarnings("unused")
	private String cleanColeHaan(String originalUrl) {
		return getMainUrl(originalUrl);
	}
	// =============================================================================================//
	// ====================================这个文件,只需要修改这块区域之上的内容========================//
	// =============================================================================================//


	/**
	 * @description 根据必需的参数重组Url
	 * @param url 原url
	 * @param keyParams 确定该商品详情页必须要的参数
	 * @return
	 */
	private String recombineUrl(final String url, String... keyParams){
		String trimUrl = StringUtils.trim(url);
		if(StringUtils.isBlank(trimUrl)){
		    return null;
		}
		String mainUrl = StringUtils.substringBefore(trimUrl, HOST_SEPARATE);
		String params = StringUtils.substringAfter(trimUrl, HOST_SEPARATE);
		List<String> paramList = Lists.newArrayList(Splitter.on(PARAMS_SEPARATE).omitEmptyStrings().trimResults().split(params));
		Map<String,String> kv = new HashMap<>();
		paramList.forEach( param  -> {
		    String key = StringUtils.substringBefore(param, "=");
		    String value = StringUtils.substringAfter(param, "=");
		    kv.put(key, value);
		});
		StringBuffer sb = new StringBuffer();
		sb.append(mainUrl).append(HOST_SEPARATE);
		Arrays.asList(keyParams).forEach( kp -> {
		    String value = kv.get(kp);
		    if(StringUtils.isNotBlank(value)){
		        sb.append(kp)
		          .append("=")
		          .append(value)
		          .append(PARAMS_SEPARATE);
		    }
		} );
		return sb.deleteCharAt(sb.length()-1).toString();
		/*String[] params = StringUtils.split(paramsString, PARAMS_SEPARATE);
		StringBuffer buff = new StringBuffer();
		if(params != null){
			int len = keyParams.length;
			for (int i=0; i<len; i++) {
				for (String str : params) {
					if(str.contains(keyParams[i])){
						if(i==0)//调用该方法时要注意传入关键参数的顺序
							buff.append(HOST_SEPARATE).append(str);
						else
							buff.append(PARAMS_SEPARATE).append(str);
						break;
					}
				}
			}
		}
		if(StringUtils.isNotBlank(buff.toString()))
			url = mainUrl+buff.toString();*/
	}
	
	private String getMainUrl(String url){
		url = StringUtils.trim(url);
		String mainUrl = StringUtils.substringBefore(url, HOST_SEPARATE);
		if(StringUtils.isNotBlank(mainUrl))
			return mainUrl.trim();
		
		return url;
	}
	private  String pattern(String pageCount){
		Pattern pattern = Pattern.compile("(/prd/\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	private  String asosPattern(String pageCount){
		Pattern pattern = Pattern.compile("(cid=\\d+)");
		Matcher matcher = pattern.matcher(pageCount);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
}