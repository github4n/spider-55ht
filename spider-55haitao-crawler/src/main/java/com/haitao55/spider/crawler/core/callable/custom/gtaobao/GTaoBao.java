package com.haitao55.spider.crawler.core.callable.custom.gtaobao;


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.haitao55.spider.common.gson.bean.taobao.TBRetBody;
import com.haitao55.spider.common.gson.bean.taobao.TBType;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * G_淘宝全球购收录
 * date : 2017-3-9
 * @author denghuan
 *
 */
public class GTaoBao extends AbstractSelect{

	private static final String domain = "g.taobao.com";
	
	@SuppressWarnings("deprecation")
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		String url = StringUtils.substringBefore(context.getCurrentUrl(),"&");
		String cateName = StringUtils.substringAfter(context.getCurrentUrl(),"cateName=");
		TBRetBody tbRetBody = new TBRetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String itemId = StringUtils.substringBetween(content, "itemId : '", "'");
			String title = doc.select(".tb-main-title").text();
			String price = doc.select("#J_StrPrice").text();
			String attr = doc.select("ul.attributes-list li").text();
			
			if(StringUtils.isBlank(title) && 
					StringUtils.isBlank(price)){
				return;
			}
			
			List<Image> imgList = new ArrayList<>();
			Elements  es = doc.select("ul#J_UlThumb li a img");
			if(es != null && es.size() > 0){
				for(Element e : es){
					String image = e.attr("data-src");
					if(StringUtils.isNotBlank(image)){
						image = image.replace("50x50", "600x600");
						imgList.add(new Image("http:"+image));
					}
				}
			}
			context.getUrl().getImages().put(itemId, imgList);
			
			if(StringUtils.isNotBlank(itemId)){
				tbRetBody.setDOCID(SpiderStringUtil.md5Encode(domain+itemId));
			}else{
				tbRetBody.setDOCID(SpiderStringUtil.md5Encode(url));
			}
			
			String cate = URLDecoder.decode(cateName);
			if(StringUtils.isNotBlank(cate)){
				tbRetBody.setCategory(cate);
			}
			tbRetBody.setProductUrl(url);
			tbRetBody.setTitle(title);
			tbRetBody.setSalePrice(price);
			tbRetBody.setDescription(attr);
			tbRetBody.setType(new TBType("1"));
			setOutput(context, tbRetBody.parseTo());
		}
	}
}
