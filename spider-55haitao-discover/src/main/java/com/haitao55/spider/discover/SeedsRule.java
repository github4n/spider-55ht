package com.haitao55.spider.discover;

/**
 * 
 * 功能：代表一个种子抓取的配置内容
 * 
 * @author Arthur.Liu
 * @time 2016年6月2日 下午2:09:20
 * @version 1.0
 */
public class SeedsRule {
    /**
     * 抓取操作等待时间；单位：秒
     */
    private int timeOutInSecond;
    /**
     * 抓取深度；目前支持2和3和4，这能满足“绝大多数”情况
     */
    private int hopCount;
    /**
     * 开始url；从这里开始抓取，算第一级
     */
    private String baseUrl;
    /**
     * 用来在第一级抓取结果上选取第二级链接的css选择器
     */
    private String baseCss;
    /**
     * 是否只抓取第一页
     */
    private boolean firstPageOnly = false;
    /**
     * 用来在第二级抓取结果上选取第三级链接的css选择器
     */
    private String secondaryCss;
    /**
     * 用来在第三级抓取结果上选取第四级链接的css选择器
     */
    private String tertiaryCss;
    /**
     * “下一页”链接元素的css选择器
     */
    private String nextCss;
    /**
     * “总页数”元素的css选择器
     */
    private String totalCountCss;
    /**
     * “总商品数”元素的css选择器
     */
    private String totalProductCountCss;
    /**
     * “每页商品数”，配合“总商品数”用以计算“总页数”
     */
    private int productCountPerPage;
    /**
     * 翻页规则配置
     */
    private Replacement replacement;
    /**
     * 抓取和更新行为控制
     */
    private Control control;

    public int getTimeOutInSecond() {
        return timeOutInSecond;
    }

    public void setTimeOutInSecond(int timeOutInSecond) {
        this.timeOutInSecond = timeOutInSecond;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseCss() {
        return baseCss;
    }

    public void setBaseCss(String baseCss) {
        this.baseCss = baseCss;
    }

    public boolean isFirstPageOnly() {
        return firstPageOnly;
    }

    public void setFirstPageOnly(boolean firstPageOnly) {
        this.firstPageOnly = firstPageOnly;
    }

    public String getSecondaryCss() {
        return secondaryCss;
    }

    public void setSecondaryCss(String secondaryCss) {
        this.secondaryCss = secondaryCss;
    }

    public String getTertiaryCss() {
        return tertiaryCss;
    }

    public void setTertiaryCss(String tertiaryCss) {
        this.tertiaryCss = tertiaryCss;
    }

    public String getNextCss() {
        return nextCss;
    }

    public void setNextCss(String nextCss) {
        this.nextCss = nextCss;
    }

    public String getTotalCountCss() {
        return totalCountCss;
    }

    public void setTotalCountCss(String totalCountCss) {
        this.totalCountCss = totalCountCss;
    }

    public String getTotalProductCountCss() {
        return totalProductCountCss;
    }

    public void setTotalProductCountCss(String totalProductCountCss) {
        this.totalProductCountCss = totalProductCountCss;
    }

    public int getProductCountPerPage() {
        return productCountPerPage;
    }

    public void setProductCountPerPage(int productCountPerPage) {
        this.productCountPerPage = productCountPerPage;
    }

    public Replacement getReplacement() {
        return replacement;
    }

    public void setReplacement(Replacement replacement) {
        this.replacement = replacement;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    /**
     * 
     * 功能：用来根据“下一页”和“总页数”做替换以生成翻页种子链接使用
     * 
     * @author Arthur.Liu
     * @data 2014-8-13 下午5:03:31
     * @version 1.0
     * 
     */
    public class Replacement {
        /**
         * “下一页”链接url必须符合这里定义的正则表达式
         */
        private String nextUrlRegex;
        /**
         * 种子链接url（pageLinkUrl）的编号从哪里算起，0还是1（从首页开始算起：首页是0，第二页就是1；首页是1，第二页就是2）
         */
        private int replaceStartIndex;
        /**
         * 需要替换的“源”正则表达式
         */
        private String replaceRegex;
        /**
         * 需要替换的“目标”正则表达式
         */
        private String replaceFormat;

        public String getNextUrlRegex() {
            return nextUrlRegex;
        }

        public void setNextUrlRegex(String nextUrlRegex) {
            this.nextUrlRegex = nextUrlRegex;
        }

        public int getReplaceStartIndex() {
            return replaceStartIndex;
        }

        public void setReplaceStartIndex(int replaceStartIndex) {
            this.replaceStartIndex = replaceStartIndex;
        }

        public String getReplaceRegex() {
            return replaceRegex;
        }

        public void setReplaceRegex(String replaceRegex) {
            this.replaceRegex = replaceRegex;
        }

        public String getReplaceFormat() {
            return replaceFormat;
        }

        public void setReplaceFormat(String replaceFormat) {
            this.replaceFormat = replaceFormat;
        }
    }

    /**
     * 
     * 功能：用来通过页面上的配置以控制种子链接数据的抓取和更新的行为
     * 
     * @author Arthur.Liu
     * @data 2014-8-15 下午3:04:14
     * @version 1.0
     * 
     */
    public class Control {
        /**
         * 是否将本次抓取到的种子链接与数据库中已经存在的种子链接做合并
         */
        private boolean mergedb;
        /**
         * 种子链接抓取时需要启动的线程数
         */
        private int threadsCount;
        /**
         * 是否启用JS
         */
        private boolean enableJs;

        public boolean isMergedb() {
            return mergedb;
        }

        public void setMergedb(boolean mergedb) {
            this.mergedb = mergedb;
        }

        public int getThreadsCount() {
            return threadsCount;
        }

        public void setThreadsCount(int threadsCount) {
            this.threadsCount = threadsCount;
        }

        public boolean isEnableJs() {
            return enableJs;
        }

        public void setEnableJs(boolean enableJs) {
            this.enableJs = enableJs;
        }
    }
}