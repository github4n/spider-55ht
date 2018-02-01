package com.haitao55.spider.data.service.service;

import java.io.BufferedWriter;
import java.io.IOException;

/** 
 * @Description: 海豚村Service接口
 * @author: zhoushuo
 * @date: 2017年3月7日 下午4:34:25  
 */
public interface HaituncunService {
	
	/**
	 * 将Mongodb中的所有数据按照分页的方式全部输出到客户端
	 * @param bw
	 * @param page
	 * @param pageSize
	 * @return 返回写入成功的数据条数
	 * @throws IOException
	 */
	long writeAllDataToClient(BufferedWriter bw, int page, int pageSize) throws IOException;
}
