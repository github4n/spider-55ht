package com.haitao55.spider.crawler.core.callable.custom.yslbeautyus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * @ClassName: Yslbeautyus @Description: detail page parser
 *
 * @author songsong.xu
 * @date 2016年11月28日 下午6:13:06
 */
public class Yslbeautyus extends AbstractSelect {
  private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
  private static final String domain = "www.yslbeautyus.com";

  @Override
  public void invoke(Context context) throws Exception {
    String content = super.getInputString(context);
    /*String content =
        Crawler.create()
            .timeOut(60000)
            .retry(3) .proxy(true).proxyAddress("104.196.30.199").proxyPort(3128)
            .url(context.getUrl().toString())
            .resultAsString();*/
    String url = context.getUrl().getValue();
    RetBody rebody = new RetBody();
    //<meta itemprop="productId" content="9081YSL">
    Pattern p = Pattern.compile("<meta[^>]*itemprop=\"productId\"[^>]*content=\"(.*?)\"[^>]*>");
    Matcher m = p.matcher(content);
    if (!m.find()) {
      throw new ParseException(
          CrawlerExceptionCode.PARSE_ERROR,
          "yslbeautyus.com itemUrl: " + context.getUrl().toString() + " parse error");
    }

    String pageContextObject =
        StringUtils.substringBetween(content, "app.pageContextObject", "customLoginParams");
    Map<String, YslSku> skuMap = new HashMap<>();
    List<String> skuIds = new ArrayList<>();
    String defaultSkuId = StringUtils.EMPTY;
    String defaultUnit = StringUtils.EMPTY;
    if (StringUtils.isNotBlank(pageContextObject)) {
      String[] skuArr = StringUtils.substringsBetween(pageContextObject, "{\"id\":", "}");
      if (skuArr != null && skuArr.length > 0) {
        Arrays.asList(skuArr)
            .parallelStream()
            .forEachOrdered(
                skuItem -> {
                  String skuId = StringUtils.substringBetween(skuItem, "\"", "\",");
                  String price = StringUtils.substringBetween(skuItem, "\"priceValue\":", ",");
                  String stock = StringUtils.substringBetween(skuItem, "\"availability\":", ",");
                  String color = StringUtils.substringBetween(skuItem, "\"color\":\"", "\",");
                  String size = StringUtils.substringBetween(skuItem, "\"size\":\"", "\",");
                  String imgUrl = StringUtils.substringBetween(skuItem, "\"imageUrl\":\"", "\",");
                  skuMap.put(skuId, new YslSku(skuId, price, stock, color, size, imgUrl));
                });
      }
      String skus =
          StringUtils.substringBetween(
              pageContextObject, "\"variants\":", ",\"defaultVariationID\"");
      if (StringUtils.isNotBlank(skus)) {
        JsonArray arr = JsonUtils.json2bean(skus, JsonArray.class);
        arr.forEach(
            json -> {
              String skuId = json.getAsJsonPrimitive().getAsString();
              skuIds.add(skuId);
            });
      }
      defaultSkuId =
          StringUtils.substringBetween(pageContextObject, "\"defaultVariationID\":\"", "\"");
      defaultUnit = StringUtils.substringBetween(pageContextObject, "\"currencyCode\":\"", "\",");
    }
    logger.info("url {}, skuIds {},defaultSkuId {}",url,skuIds,defaultSkuId);

    Document d = JsoupUtils.parse(content);
    Elements es = d.select("div#product_content > div.product-variations");
    d = JsoupUtils.parse(content);
    es =
        d.select(
            "#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > h1");
    String title = JsoupUtils.text(es);
    //#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta
    es =
        d.select(
            "#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta");
    String brand = JsoupUtils.attr(es, "content");
    //#product-content > div.product-variations > ul > li.attribute.clearfix
    es = d.select("div#product_content > div.product-variations > ul > li");

    boolean stockFlag = false;
    Sku sku = new Sku();
    List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
    List<LStyleList> l_style_list = new ArrayList<LStyleList>();
    float defaultOrig = 0;
    float defaultSale = 0;
    for (String skuId : skuIds) {
      LSelectionList lSelectionList = new LSelectionList();
      LStyleList lStyleList = new LStyleList();
      List<Selection> selections = new ArrayList<Selection>();
      YslSku yslSku = skuMap.get(skuId);
      if (yslSku != null) {
        lSelectionList.setGoods_id(skuId);
        lStyleList.setGood_id(skuId);

        lStyleList.setStyle_switch_img("");
        lStyleList.setStyle_cate_id(0);

        if (StringUtils.isNotBlank(yslSku.getColor())) {
          //selectlist
          lSelectionList.setStyle_id(yslSku.getColor());
          if (StringUtils.isNotBlank(yslSku.getSize())) {
            Selection selection = new Selection();
            selection.setSelect_id(0);
            selection.setSelect_name("Size");
            selection.setSelect_value(yslSku.getSize());
            selections.add(selection);
          }
          //stylelist
          lStyleList.setStyle_id(yslSku.getColor());
          lStyleList.setStyle_name(yslSku.getColor());
          lStyleList.setStyle_cate_name("Color");
        } else {
          //selectlist
          lSelectionList.setStyle_id(yslSku.getSize());
          //stylelist
          lStyleList.setStyle_id(yslSku.getSize());
          lStyleList.setStyle_name(yslSku.getSize());
          lStyleList.setStyle_cate_name("Size");
        }
        l_style_list.add(lStyleList);

        //sku  stock price
        String stock = yslSku.getStock();
        int stockStatus = 0;
        if (StringUtils.equals("true", stock)) {
          stockStatus = 1;
          stockFlag = stockFlag || true;
        }
        float sale = 0;
        float orig = 0;
        String unit = defaultUnit;
        if (StringUtils.isNotBlank(yslSku.getPrice())) {
          sale = Float.valueOf(yslSku.getPrice());
          orig = sale;
        }
        lSelectionList.setSale_price(sale);
        lSelectionList.setOrig_price(orig);
        lSelectionList.setPrice_unit(unit);
        lSelectionList.setStock_status(stockStatus);
        lSelectionList.setStock_number(0);
        lSelectionList.setSelections(selections);
        l_selection_list.add(lSelectionList);

        List<Image> pics = new ArrayList<Image>();
        pics.add(new Image(yslSku.getImgUrl()));
        context.getUrl().getImages().put(skuId, pics); // picture download
        if (StringUtils.equals(skuId, defaultSkuId)) {
          lStyleList.setDisplay(true);
          defaultOrig = orig;
          defaultSale = sale;
        }
      }
    }
    sku.setL_selection_list(l_selection_list);
    sku.setL_style_list(l_style_list);
    // full doc info
    String docid = SpiderStringUtil.md5Encode(url);
    String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
    rebody.setDOCID(docid);
    rebody.setSite(new Site(domain));
    rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
    //title
    rebody.setTitle(new Title(title, "", "", ""));
    // price
    if (defaultOrig == 0 || defaultSale == 0) {
      logger.error(
          "Error while fetching spuprice from yslbeautyus.com's detail page,url {}",
          context.getUrl().toString());
      throw new ParseException(
          CrawlerExceptionCode.PARSE_ERROR,
          "yslbeautyus.com category url :" + context.getUrl().toString() + " spuprice is null.");
    }
    int save = Math.round((1 - defaultSale / defaultOrig) * 100); // discount
    rebody.setPrice(new Price(defaultOrig, save, defaultSale, defaultUnit));
    // stock
    int stockStatus = 0;
    if (stockFlag) {
      stockStatus = 1;
    }
    rebody.setStock(new Stock(stockStatus));
    // images l_image_list
    // rebody.setImage(new LImageList(pics));
    // brand
    rebody.setBrand(new Brand(brand, "", "", ""));
    // Category
    //body > div.main.skincare_or_rouge > div > ul.breadcrumb > li > a > span
    es = d.select("ul.breadcrumb > li");
    List<String> cats = new ArrayList<String>();
    List<String> breads = new ArrayList<String>();
    if (es != null && es.size() > 0) {
      for (Element ele : es) {
        String prop = JsoupUtils.attr(ele, "itemprop");
        if (!StringUtils.contains(prop, "itemListElement")) {
          continue;
        }
        String text = JsoupUtils.text(ele.select("a > span"));
        if (StringUtils.isBlank(text)) {
          continue;
        }
        cats.add(text);
        breads.add(text);
      }
    }
    rebody.setCategory(cats);
    // BreadCrumb
    rebody.setBreadCrumb(breads);
    // description
    Map<String, Object> descMap = new HashMap<String, Object>();
    es = d.select("div.product_detail_description");
    StringBuilder sb = new StringBuilder();
    if (es != null && es.size() > 0) {
      for (Element ele : es) {
        String text = JsoupUtils.text(ele);
        if (StringUtils.isBlank(text)) {
          continue;
        }
        sb.append(text);
      }
    }
    descMap.put("en", sb.toString());
    rebody.setDescription(descMap);
    //feature
    Map<String, Object> featureMap = new HashMap<String, Object>();
    rebody.setFeatureList(featureMap);

    Map<String, Object> propMap = new HashMap<String, Object>();
    propMap.put("s_gender", "women");
    rebody.setProperties(propMap);
    rebody.setSku(sku);
    //System.out.println(rebody.parseTo());
    setOutput(context, rebody);
  }

  class YslSku {
    private String skuId;
    private String price;
    private String stock;
    private String color;
    private String size;
    private String imgUrl;

    public YslSku() {}

    public YslSku(
        String skuId, String price, String stock, String color, String size, String imgUrl) {
      this.skuId = skuId;
      this.price = price;
      this.stock = stock;
      this.color = color;
      this.size = size;
      this.imgUrl = imgUrl;
    }

    public String getSkuId() {
      return skuId;
    }

    public void setSkuId(String skuId) {
      this.skuId = skuId;
    }

    public String getPrice() {
      return price;
    }

    public void setPrice(String price) {
      this.price = price;
    }

    public String getStock() {
      return stock;
    }

    public void setStock(String stock) {
      this.stock = stock;
    }

    public String getColor() {
      return color;
    }

    public void setColor(String color) {
      this.color = color;
    }

    public String getSize() {
      return size;
    }

    public void setSize(String size) {
      this.size = size;
    }

    public String getImgUrl() {
      return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
      this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
      return "YslSku [skuId="
          + skuId
          + ", price="
          + price
          + ", stock="
          + stock
          + ", color="
          + color
          + ", size="
          + size
          + ", imgUrl="
          + imgUrl
          + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((color == null) ? 0 : color.hashCode());
      result = prime * result + ((imgUrl == null) ? 0 : imgUrl.hashCode());
      result = prime * result + ((price == null) ? 0 : price.hashCode());
      result = prime * result + ((size == null) ? 0 : size.hashCode());
      result = prime * result + ((skuId == null) ? 0 : skuId.hashCode());
      result = prime * result + ((stock == null) ? 0 : stock.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      YslSku other = (YslSku) obj;
      if (!getOuterType().equals(other.getOuterType())) return false;
      if (color == null) {
        if (other.color != null) return false;
      } else if (!color.equals(other.color)) return false;
      if (imgUrl == null) {
        if (other.imgUrl != null) return false;
      } else if (!imgUrl.equals(other.imgUrl)) return false;
      if (price == null) {
        if (other.price != null) return false;
      } else if (!price.equals(other.price)) return false;
      if (size == null) {
        if (other.size != null) return false;
      } else if (!size.equals(other.size)) return false;
      if (skuId == null) {
        if (other.skuId != null) return false;
      } else if (!skuId.equals(other.skuId)) return false;
      if (stock == null) {
        if (other.stock != null) return false;
      } else if (!stock.equals(other.stock)) return false;
      return true;
    }

    private Yslbeautyus getOuterType() {
      return Yslbeautyus.this;
    }
  }

  public static void main(String[] args) throws Exception {
    Yslbeautyus shan = new Yslbeautyus();
    Context context = new Context();
    //http://www.yslbeautyus.com/or-rouge-cleansing-cream/490YSL.html
    //http://www.yslbeautyus.com/rouge-volupte-shine-oil-in-stick-holiday-2016/912YSL.html
    //http://www.yslbeautyus.com/mon-paris-body-lotion/4781YSL.html
    //http://www.yslbeautyus.com/pour-homme/537YSL.html
    context.setUrl(
        new Url("http://www.yslbeautyus.com/fusion-ink-foundation/351YSL.html"));
    context.setCurrentUrl("http://www.yslbeautyus.com/fusion-ink-foundation/351YSL.html");
    //http://www.yslbeautyus.com/forever-light-creator-cc-primer/890YSL.html
    shan.invoke(context);
    //variation-select
    //String url = "http://www.yslbeautyus.com/on/demandware.store/Sites-ysl-us-Site/en_US/Product-Variation?pid=490YSL&dwvar_490YSL_size=5%2e0%20oz%2f150%20ML&quantity=1&action=variationChanged&format=ajax";
    /*String url = "http://www.yslbeautyus.com/or-rouge-cleansing-cream/490YSL.html";
    Map<String,Object> headers = new HashMap<String,Object>();

    String content = Crawler.create().method("get").header(headers).timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
    Document d = JsoupUtils.parse(content);
    Elements es = d.select("#pdpMain > div.section_scroll.section_about.clearfix > div.pdp_top_content_wrapper > meta");
    String brand = JsoupUtils.attr(es, "content");
    System.out.println(brand);*/

    /*if(StringUtils.isNotBlank(content)){
    	content = StringUtils.replacePattern(content, "\\\r|\\\n|\\\t", "");
    }
    Pattern p = Pattern.compile("<div[^>]*class=\"jcarousel-clip jcarousel-clip-horizontal[^>]*>[^<]*<ul[^>]*class=\"contentcarousel_list\">[^<]*<li[^>]*class=\"thumb[^>]*selected[^>]*>[^<]*<a[^>]*href=(.*?) target[^>]*>");
    Matcher m = p.matcher(content);
    if(m.find()){
    	System.out.println(m.group(1));
    } */
    //                                    5%2e0%20oz%2f150%20ML
    /*System.out.println(URLDecoder.decode("5%2e0%20oz%2f150%20ML"));
    System.out.println(escapeURIPathParam("5.0 oz/150 ML"));
    System.out.println(0xFF);
    System.out.println(Integer.toBinaryString(0xFF));
    System.out.println((int)'a' );
    System.out.println(Integer.parseUnsignedInt("11111111111111111111111111111110", 2));
    System.out.println('5'/ 16);
    System.out.println(toHex('5'/ 16));
    System.out.println(0x0F);*/

    //System.out.println(BCConvert.bj2qj("¥").equals("￥"));
    /*String url = "http://www.yslbeautyus.com/top-secrets-all-in-one-bb-cream/312YSL.html";

    String html = Crawler.create().timeOut(40000).retry(3).url(url
    		).resultAsString();
    Document d = JsoupUtils.parse(html);
    Elements es = d.select("#thumbnails");
    System.out.println(es.html());
    es.forEach( ele -> {
        System.out.println("href:"+ele.attr("href"));
    });*/
    /*Pattern p = Pattern.compile("Your Search For(.*?)Found 0 Results");
    Matcher m = p.matcher(html);
    if(m.find()){
    	String itemId = m.group(1);
    	System.out.println(itemId);
    }*/
    /*String url = "http://www.yslbeautyus.com/on/demandware.store/Sites-ysl-us-Site/en_US/Product-Variation?pid=1006YSL&dwvar_1006YSL_color=Indiscreet+Purple&quantity=1&action=variationChanged&format=ajax";
    String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).resultAsString();
    Document d = JsoupUtils.parse(content);
    //#main-image-container > div.carousel.contentcarousel.horizontal_carousel > div > ul.contentcarousel_list > li.image contentcarousel_list_item > a
    Elements es = d.select("ul.contentcarousel_list > li.image.contentcarousel_list_item > a");
    System.out.println(es.html());
    for(Element e : es){
    	System.out.println(JsoupUtils.attr(e, "data-tablet-src"));
    }*/

    /*Pattern p = Pattern.compile("<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*>[^<]*In Stock[^<]*<|<p[^>]*class=\"availability_value[^>]*\">[^<]*<span[^>]*class=\"product_limit_threshold\">[^<]*Only \\d+ available[^<]*<");
    Matcher m = p.matcher(content);
    if(m.find()){
    	System.out.println(m.group());
    }*/
    //out of stock

    /*p = Pattern.compile("<div[^>]*class=\"availability-msg\">[^<]*<p[^>]*>[^<]*out of stock[^<]*<");
    m = p.matcher(content);
    if(m.find()){
    	System.out.println("stock=0");
    } else {
    	System.out.println("stock=2");
    }
    //<li class="thumb
    p = Pattern.compile("<li[^>]*class=\"thumb[^>]*>[^<]*<a[^>]*href=\"(.*?)\"");
    m = p.matcher(content);
    if(m.find()){
    	System.out.println(m.group(1));
    }*/
  }
}
