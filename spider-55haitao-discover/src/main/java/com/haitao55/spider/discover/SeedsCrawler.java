package com.haitao55.spider.discover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.discover.SeedsRule.Replacement;


/**
 * 
 * 功能：执行任务种子抓取功能的主要类
 * 
 * @author Arthur.Liu
 * @time 2016年6月2日 下午2:26:03
 * @version 1.0
 */
public class SeedsCrawler {
    private static final Logger logger = LoggerFactory
            .getLogger(SeedsCrawler.class);
    
    private static final int CRAWL_RETRY_TIMES = 3;
    
    private static final String PAGE_URL_REPLACE_VALUE = "\\{\\}";

    /**
     * 
     * 功能：执行种子抓取的入口
     * 
     * @param jobId
     *            任务ID
     * @param seedsRule
     *            任务种子抓取配置规则
     */
    public Set<UrlDO> crawl(long jobId, SeedsRule seedsRule) {
        int timeOut = seedsRule.getTimeOutInSecond() * 1000;// 这里有个“秒”与“毫秒”的转换
        int hopCount = seedsRule.getHopCount();// 抓取深度
        boolean enableJs = seedsRule.getControl().isEnableJs();
        
        Set<String> finalUrls = null;// 代表最终抓取到的种子链接，以用来在其上再执行一次抓取类目名称、类目url、翻页url的操作
        
        // 1.由一级种子链接抓取二级种子链接
        String baseUrl = seedsRule.getBaseUrl();
        String baseCss = seedsRule.getBaseCss();
        Set<String> secondaryUrls = crawlBase(baseUrl, baseCss, timeOut, jobId, enableJs);
        finalUrls = secondaryUrls;
        
        if(hopCount > 2 && !StringUtils.isBlank(seedsRule.getSecondaryCss())){// 使用secondaryUrls再抓取一次，并将结果赋值给finalUrls
            String secondaryCss = seedsRule.getSecondaryCss();
            Set<String> tertiaryUrls = crawlDescendant(secondaryUrls, secondaryCss, timeOut, jobId, enableJs);
            finalUrls = tertiaryUrls;
            
            if(hopCount > 3 && !StringUtils.isBlank(seedsRule.getTertiaryCss())){// 使用tertiaryUrls再抓取一次，并将结果赋值给finalUrls
                String tertiaryCss = seedsRule.getTertiaryCss();
                Set<String> fourthUrls = crawlDescendant(tertiaryUrls, tertiaryCss, timeOut, jobId, enableJs);
                finalUrls = fourthUrls;
              
//              if(hopCount > 4 /*&& !StringUtils.isBlank(seedsRule.getSecondaryCss())*/){// 使用xxxUrls再抓取一次，并将结果赋值给finalUrls
//                  // .........
//              }
          }
        }
        
        Set<UrlDO> crawledSeedsRst = null;
        if(seedsRule.isFirstPageOnly()){// 仅抓取列表页的首页，可能是因为后续页码抓取不到
            crawledSeedsRst = fillFinalUrls(finalUrls);
        }else{
            crawledSeedsRst = crawlerSeedsFromFinalUrls(finalUrls, seedsRule, jobId, timeOut, CRAWL_RETRY_TIMES, enableJs);
        }
        
        return crawledSeedsRst;
    }
    
    private Set<UrlDO> fillFinalUrls(Set<String> finalUrls){
        Set<UrlDO> rst = new HashSet<UrlDO>();
        
        if(finalUrls == null || finalUrls.isEmpty()){
            return rst;
        }
        
        for(String finalUrl : finalUrls){
        	UrlDO seed = new UrlDO();
            seed.setValue(finalUrl);
            rst.add(seed);
        }
        
        return rst;
    }
    
    /**
     * 
     * 功能：由finalUrls执行抓取，构建出翻页种子链接集合，组合成最终需要保存到DB的格式；这里启动多个线程来执行抓取操作，并由这个方法来统一管理这些线程的运行与数据
     * @param finalUrls 每一个最小类目列表的首页url
     * @param seedsRule 预先配置的抓取规则
     * @param jobId 任务ID
     * @param timeOut 等待请求响应返回的时间限制
     * @param retryTimes 抓取重试次数
     * @return 最终可以保存到DB中的符合格式的Seed集合
     */
    private Set<UrlDO> crawlerSeedsFromFinalUrls(Set<String> finalUrls, SeedsRule seedsRule, long jobId, int timeOut, int retryTimes, boolean enableJs){
        Set<UrlDO> rstSeeds = new HashSet<UrlDO>();
        
        Queue<String> finalFirstUrls = new ConcurrentLinkedQueue<String>(finalUrls);// 由多个线程共享
        Queue<UrlDO> rstSeedsQueue = new ConcurrentLinkedQueue<UrlDO>();// 由多个线程共享
        
        List<SeedsCrawlerTask> tasks = new ArrayList<SeedsCrawlerTask>();
        
        int threadsCount = seedsRule.getControl().getThreadsCount();// 前端配置的线程数量
        for(int i = 1; i <= threadsCount; i++){// 启动多个线程
            SeedsCrawlerTask task = new SeedsCrawlerTask(finalFirstUrls, rstSeedsQueue, seedsRule, jobId, timeOut, retryTimes, enableJs);
            tasks.add(task);
            Thread t = new Thread(task);
            t.start();// 启动线程
        }
        
        while (true) {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
            }

            if (isAllTasksFinished(tasks)) {
                break;
            }
        }
        
        rstSeeds.addAll(rstSeedsQueue);// 当所有线程都完成任务之后，统一收集最终结果数据
        return rstSeeds;
    }
    
    /**
     * 
     * 功能：检查是否所有线程都已经执行完毕
     * @param tasks 需要检查的线程列表
     * @return
     */
    private boolean isAllTasksFinished(List<SeedsCrawlerTask> tasks){
        for(SeedsCrawlerTask task : tasks){
            if(!task.isFinished()){
                return false;
            }
        }
        
        return true;
    }

    /**
     * 
     * 功能：由某级种子链接抓取下一级种子链接
     * @param descendantUrls 某级种子链接的集合
     * @param descendantCss 用来在某级种子链接url抓取结果页面上选取下一级种子链接url的css选择器
     * @param timeOut 等待请求返回时间限制
     * @param jobId 当前处理的任务ID
     * @return 下一级种子链接的集合
     */
    private Set<String> crawlDescendant(Set<String> descendantUrls, String descendantCss, int timeOut, long jobId, boolean enableJs){
        Set<String> rst = new HashSet<String>();
        
        for(String descendantUrl : descendantUrls){
            try {
                Set<String> urls = crawlAndSelectUrls(descendantUrl, descendantCss, timeOut, CRAWL_RETRY_TIMES, enableJs);;
                rst.addAll(urls);
            }  catch (Exception e) {
                logger.error("Error occured while crawling seeds(descendantUrls)::jodId:{};descendantCss:{};e:{}", jobId, descendantCss, e);
            }
        }
        
        return rst;
    }
    
    /**
     * 
     * 功能：由一级种子链接抓取二级种子链接
     * @param baseUrl 一级种子链接url
     * @param baseCss 用来在一级种子链接url抓取结果页面上选取二级种子链接url的css选择器
     * @param timeOut 等待请求返回时间限制
     * @param jobId 当前处理的任务ID
     * @return 二级种子链接的集合
     */
    private Set<String> crawlBase(String baseUrl, String baseCss, int timeOut, long jobId, boolean enableJs){
        Set<String> rst = new HashSet<String>();
        
        try {
            Set<String> urls = crawlAndSelectUrls(baseUrl, baseCss, timeOut, CRAWL_RETRY_TIMES, enableJs);
            rst.addAll(urls);
        }  catch (Exception e) {
            logger.error("Error occured while crawling seeds(baseUrl)::jodId:{};e:{}", jobId, e);
        }
        
        return rst;
    }
    
    /**
     * 
     * 功能：执行抓取并在抓取结果源码页面上根据css选择器选取想要的urls
     * @param url 用来执行抓取的url
     * @param css 用来在抓取结果页面上执行选取的css选择器
     * @param timeOut 等待请求响应返回时间限制
     * @param retryTimes 重试次数
     * @return 抓取并选取的url集合
     * @throws ClientProtocolException
     * @throws HttpException
     * @throws IOException
     */
    private Set<String> crawlAndSelectUrls(String url, String css, int timeOut, int retryTimes, boolean enableJs) throws ClientProtocolException, HttpException, IOException{
        Set<String> urls = new HashSet<String>();
        
        String html = "";
        if(!enableJs){// 不启用JS的情况
            html = Crawler.create().timeOut(timeOut).retry(retryTimes).url(url).resultAsString();
        }else{// 启用JS的情况
            html = SeedsUtils.crawlViaHtmlunit(url, timeOut, enableJs);
        }
        Document document = Jsoup.parse(html, url);
        Elements elements = document.select(css);
        for(int i = 0; i < elements.size(); i++){
            Element element = elements.get(i);
            String href = element.attr("abs:href");
            urls.add(href);
        }
        
        return urls;
    }
    
    /**
     * 
     * 功能：执行抓取功能的线程
     * @author Arthur.Liu
     * @data 2014-8-18 下午2:16:20
     * @version 1.0
     *
     */
    private class SeedsCrawlerTask implements Runnable{
        private Queue<String> finalFirstUrls = null;
        private Queue<UrlDO> rstSeedsQueue = null;
        private SeedsRule seedsRule;
        private long jobId;
        private int timeOut;
        private int retryTimes;
        private boolean enableJs;
        
        private boolean finished = false;// 标识本线程是否执行完毕，供外界做监控与管理使用
        
        public boolean isFinished() {
            return finished;
        }

        public SeedsCrawlerTask(Queue<String> finalFirstUrls, Queue<UrlDO> rstSeedsQueue, SeedsRule seedsRule, long jobId, int timeOut, int retryTimes, boolean enableJs){
            this.finalFirstUrls = finalFirstUrls;
            this.rstSeedsQueue = rstSeedsQueue;
            this.seedsRule = seedsRule;
            this.jobId = jobId;
            this.timeOut = timeOut;
            this.retryTimes = retryTimes;
            this.enableJs = enableJs;
            
            finished = false;
        }
        
        @Override
        public void run() {
            finished = false;
            
            String finalFirstUrl;
            while((finalFirstUrl = this.finalFirstUrls.poll()) != null){
                String finalFirstUrl4Crawling = SeedsUtils.handleSpecialCharInUrl(finalFirstUrl);// 处理url中的特殊字符
                try {
                    String html = "";
                    if(!this.enableJs){// 不启用JS的情况
                        html = Crawler.create().timeOut(timeOut).retry(retryTimes).url(finalFirstUrl4Crawling).resultAsString();
                    }else{// 启用JS的情况
                        html = SeedsUtils.crawlViaHtmlunit(finalFirstUrl4Crawling, timeOut, enableJs);
                    }
                    Document document = Jsoup.parse(html, finalFirstUrl);
                    
                    UrlDO firstPageSeed = new UrlDO();// 首页种子链接
                    firstPageSeed.setValue(finalFirstUrl);
                    
                    rstSeedsQueue.add(firstPageSeed);// 首页种子对象加入结果集合
                    
                    // 以下处理翻页逻辑
                    Set<UrlDO> pagedSeeds = buildNextPageUrls(document, seedsRule.getReplacement(), jobId);
                    rstSeedsQueue.addAll(pagedSeeds);
                } catch (Exception e) {
                    logger.error("Error occured while crawling seeds(finalUrl)::jodId:{};e:{}", jobId, e);
                }
            }
            
            finished = true;
        }

        /**
         * 
         * 功能：由一个首页url，处理生成翻页url的集合
         * @param nextPageUrlElement “下一页”的html页面元素
         * @param totalCountElement “总页数”的html页面元素
         * @param replacement 预先配置的翻页处理规则
         * @return 从第二页开始直到最后一页的种子
         */
        private Set<UrlDO> buildNextPageUrls(Document document, Replacement replacement, long jobId){
            Set<UrlDO> pagedSeeds = new HashSet<UrlDO>();// 从第二页开始直到最后一页的种子
            
            String nextCss = seedsRule.getNextCss();
            Element nextPageUrlElement = document.select(nextCss).get(0);
            String nextPageUrl = nextPageUrlElement.attr("abs:href");
            
            String totalPageCountCss = seedsRule.getTotalCountCss();
            String totalProductCountCss = seedsRule.getTotalProductCountCss();
            int productCountPerPage = seedsRule.getProductCountPerPage();
            int totalPageCountInt = 1;// 最终要得出的int类型的“总页数”
            if(StringUtils.isNotBlank(totalPageCountCss)){// 配置了“总页数”的情况
                Element totalPageCountElement = document.select(totalPageCountCss).get(0);
                String totalPageCount = totalPageCountElement.text();
                totalPageCount = handleCount(totalPageCount);
                if ("1".equals(totalPageCount)) {// 配置了“总商品数”的情况
                    return pagedSeeds;// 整个列表就只有一页的情况
                }
                
                totalPageCountInt = Integer.parseInt(totalPageCount);
            }else if(StringUtils.isNotBlank(totalProductCountCss) && productCountPerPage > 0){
                Element totalProductCountElement = document.select(totalProductCountCss).get(0);
                String totalProductCount = totalProductCountElement.text();
                totalProductCount = handleCount(totalProductCount);
                int totalProductCountInt = Integer.parseInt(totalProductCount);
                if(totalProductCountInt <= productCountPerPage){
                    return pagedSeeds;// 整个列表就只有一页的情况
                }
                
                totalPageCountInt = (totalProductCountInt / productCountPerPage) + 1;
            }else{// 什么都没配置的情况，这算不正常的情况
                return pagedSeeds;
            }
            
            int pageUrlStartIndex = replacement.getReplaceStartIndex();// 首页的index
            String nextUrlRegex = replacement.getNextUrlRegex();// pageUrl必须符合的正则表达式
            String replaceRegex = replacement.getReplaceRegex();// 用来寻找要替换的子串的正则表达式
            String replaceFormat = replacement.getReplaceFormat();// 要替换成个什么样子
            
            // 1.当前的“下一页”url值要符合预先配置的正则表达式，以排除选错的可能性
            Pattern p = Pattern.compile(nextUrlRegex);
            Matcher m = p.matcher(nextPageUrl);
            if(!m.find()){// 如果“下一页”链接url值不符合预先配置的规则，说明找的不正确，略过
                logger.error("Current 'nextPageUrl' do not match the configured 'nextUrlRegex'::nextPageUrl:{};nextUrlRegex:{}", nextPageUrl, nextUrlRegex);
                return pagedSeeds;
            }
            
            // 2.对当前的“下一页”url值，根据预先配置的replaceRegex和replaceFormat作替换
            String replacedNextPageUrl = nextPageUrl.replaceAll(replaceRegex, replaceFormat);
            
            // 4.循环生成翻页种子链接
            for(int i = pageUrlStartIndex + 1; i <= totalPageCountInt; i++){
                String pageUrl = replacedNextPageUrl.replaceAll(PAGE_URL_REPLACE_VALUE, String.valueOf(i));
                
                UrlDO seed = new UrlDO();
                seed.setValue(pageUrl);
                
                pagedSeeds.add(seed);
            }
            
            return pagedSeeds;
        }
        
        /**
         * 
         * 功能：处理totalCount数值，比如“2/11”分隔符形式
         * @param totalPageCount
         * @return
         */
        private String handleCount(String totalPageCount){
            if (totalPageCount.indexOf("/") != -1) {
                String[] c_pages = totalPageCount.trim().split("/");
                if (c_pages.length == 2) {
                    totalPageCount = c_pages[1].trim();
                }
            }
            totalPageCount = totalPageCount.replaceAll("[^\\d]", "");
            
            return totalPageCount;
        }
    }
    
}