//package com.haitao55.spider.discover;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.haitao55.spider.common.bean.Seed;
//import com.haitao55.spider.common.bean.SeedsRuleDO;
//import com.haitao55.spider.common.bean.UrlInfoBean;
//import com.haitao55.spider.common.dao.SeedsRuleDAO;
//import com.haitao55.spider.common.dao.UrlDao;
//import com.haitao55.spider.common.dao.constants.Constants;
//
//
//
///**
// * 
// * 功能：种子链接(URLs)管理，现在主要是抓取
// * 
// * @author arthur
// * @time 2016年2月17日 下午2:43:28
// * @version 1.0
// */
//public class SeedsManager {
//    @SuppressWarnings("unused")
//    private static final Logger logger = LoggerFactory.getLogger("system");
//    
//    private static final String SEEDS_RULE_COLLECTIONS_NAME = "seeds_rule";
//    
//    private SeedsRuleDAO seedsRuleDAO = null;
//    private UrlDao urlDAO = null;
//    
//    public void init(){
//        
//    }
//
//    /**
//     * 
//     * 功能：执行种子链接数据的抓取操作
//     * <p>
//     * 这个方法是执行种子链接数据抓取操作的入口方法，可能会被Web-UI中的手动操作使用，也可能会被定时程序调用
//     * </p>
//     * 
//     * @param jobId 要执行种子链接数据抓取操作的任务的ID
//     * @return 本次抓取操作获取到的种子数量
//     * @throws Exception
//     */
//    public int crawlSeeds(long jobId) throws Exception {
//        SeedsRuleDO rule = this.seedsRuleDAO.getByJobId(jobId, SEEDS_RULE_COLLECTIONS_NAME);// 得到任务的种子抓取配置规则
//        String ruleContent = SeedsUtils.getRuleContentFromWrappedRuleContent(rule);
//        SeedsRule seedsRule = SeedsUtils.buildSeedsRuleFromRuleContent(ruleContent);// 根据配置的规则来抓取
//        
//        if(seedsRule == null){
//            throw new Exception("Error occured while building SeedsRule instance before crawl seeds");
//        }
//        
//        SeedsCrawler sc = new SeedsCrawler();
//        Set<Seed> crawledSeeds = sc.crawl(jobId, seedsRule);
//        handleSpecialCharInUrls(crawledSeeds);
//        
//        Set<UrlInfoBean> mergedSeeds = new HashSet<UrlInfoBean>();
//        if(crawledSeeds != null && !crawledSeeds.isEmpty()){
//            for(Seed seed : crawledSeeds){
//                UrlInfoBean urlBean = new UrlInfoBean();
//                urlBean.setValue(seed.getPageUrl());
//                urlBean.setErrorCode("");
//                urlBean.setFialCount(0);
//                urlBean.setId(null);
//                urlBean.setLastCrawleIp("");
//                urlBean.setLastCrawleTime(0L);
//                urlBean.setStatus("I");
//                urlBean.setCreateTime(System.currentTimeMillis());
//                
//                mergedSeeds.add(urlBean);
//            }
//        }
//        
//        // 因为每一次抓取都不一定能抓取到目标网站的全部链接，所以最好还是先与DB中现存的种子链接数据合并之后，再存到DB中去，而不是先删完DB中现有的而只保存这次抓取到的——这个行为可以通过页面进行配置
//        if(seedsRule.getControl().isMergedb()){
//            @SuppressWarnings("serial")
//            List<String> status = new ArrayList<String>(){{
//                add(Constants.URL_STATUS.INIT.value());
//                add(Constants.URL_STATUS.ACTIVE.value());
//                add(Constants.URL_STATUS.FINISH.value());
//                add(Constants.URL_STATUS.PAUSE.value());
//                add(Constants.URL_STATUS.ERROR.value());
//            }};
//            List<UrlInfoBean> existSeedsFromDB = this.urlDAO.queryUrl(jobId, status, Integer.MAX_VALUE);// 最后要与现有的种子执行一个合并操作
//            mergedSeeds.addAll(existSeedsFromDB);
//        }
//        
//        // 严格上来说，这两句代码需要在一个事务中执行，先这样
//        this.urlDAO.deleteExpiredUrls(jobId, System.currentTimeMillis());// 将当前时间之前的URLs全部删除
//        this.urlDAO.insertUrls(jobId, new ArrayList<UrlInfoBean>(mergedSeeds));
//        
//        return mergedSeeds.size();
//    }
//    
//    /**
//     * 
//     * 功能：处理url中的特殊字符
//     * @param seeds
//     */
//    private void handleSpecialCharInUrls(Set<Seed> seeds){
//        if(seeds == null || seeds.isEmpty()){
//            return;
//        }
//        
//        for(Seed seed : seeds){
//            seed.setPageUrl(SeedsUtils.handleSpecialCharInUrl(seed.getPageUrl()));// 处理url中的特殊字符，如‘|’等
//        }
//    }
//
//    public SeedsRuleDAO getSeedsRuleDAO() {
//        return seedsRuleDAO;
//    }
//
//    public void setSeedsRuleDAO(SeedsRuleDAO seedsRuleDAO) {
//        this.seedsRuleDAO = seedsRuleDAO;
//    }
//
//    public UrlDao getUrlDAO() {
//        return urlDAO;
//    }
//
//    public void setUrlDAO(UrlDao urlDAO) {
//        this.urlDAO = urlDAO;
//    }
//}