namespace java com.haitao55.spider.common.thrift

struct HeartbeatModel{
    1: required i64 time,// 心跳发生时间
    2: required string ip,// 客户端机器ip
    3: required string procId,// 客户端进程id
    4: required i32 threadCount,// 客户端进程中爬虫线程数量
}

struct TaskModel{
    1: required i64 id,// 任务id
    2: required string name,// 任务名称
    3: required string status,// 状态
    4: required string config,// 任务配置信息
    5: required string siteRegion,// 目标网站所在地区
    6: required string proxyRegionId,// 抓取目标网站需要使用哪个地区的代理IP
    7: required i32 limit,// 单位时间内分发待抓取的urls的数量上限
    8: required i32 weight,// 分发待抓取的urls时,用来计算每个任务该分发多少urls的权重
}

struct ProxyModel{
	1: required i64 id,// 主键ID号
	2: required string regionId,// 区域代码
	3: required string regionName,// 区域名称
	4: required string ip,// IP值
	5: required i32 port,// 端口号
}

enum UrlStatusModel{
	NEWCOME,// 从互联网上刚被发现到
	CRAWLING,// 从底层controller中刚传递上来,等待执行抓取
	CRAWLED_OK,// 已经执行过抓取操作,执行抓取成功
	CRAWLED_ERROR,// 已经执行过抓取操作,执行抓取错误
	DELETING,// 从底层controller中刚传递上来,等待执行删除
	DELETED_OK,// 已经执行过删除操作,执行删除成功
	DELETED_ERROR,// 已经执行过删除操作,执行删除错误
	UNKNOWN,// 未知状态
}

enum UrlTypeModel{
	LINK,// 链接类型
	ITEM,// 商品类型
}

struct UrlModel{
    1: required string id,// Url在底层数据库中存放时的主键ID号
    2: required string value,// Url的实际字符串值
    3: required i32 grade,// Url的级别
    4: required UrlTypeModel type,// Url的类型
    5: required i64 taskId,// Url所属task的taskID号
    6: required i64 lastCrawledTime,// 最近一次这条Url被执行抓取的时间
    7: required string lastCrawledIP,// 最近执行这条Url抓取的爬虫机器IP地址
    8: required UrlStatusModel urlStatusModel,// Url实例状态
    9: required string latelyErrorCode,// 抓取失败时的错误代码
    10: required i32 latelyFailedCount,// 最近连续抓取失败次数
    11: required i64 createTime,// 创建时间
}

struct ItemModel{
	1: required string id,// 商品数据ID
	2: required string value,// 商品数据本身
}

service ThriftService{
    bool heartbeat(1: HeartbeatModel heartbeatModel);

    map<i64, TaskModel> fetchHotTasks();
    
    map<i64, ProxyModel> fetchProxies();

    TaskModel fetchTask(1: i64 taskId);

    bool noticeTaskConfigError(1: i64 taskId);

    list<UrlModel> fetchUrls(1: i32 limit);
    
    bool updelUrls(1: list<UrlModel> urls);
    
    bool upsertUrls(1: list<UrlModel> urls);
    
    bool upsertItems(1: map<i64, list<ItemModel>> itemMap);
}