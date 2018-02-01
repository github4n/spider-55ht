package com.haitao55.spider.crawler.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.ItemModel;
import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.OutputObject;
import com.haitao55.spider.crawler.service.AbstractOutputService;
import com.haitao55.spider.crawler.service.OutputService;
import com.haitao55.spider.crawler.thrift.pool.ThriftConnectionProvider;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：结果数据输出的Service接口实现，以调用Controller接口的方式输出结果数据
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午6:31:39
 * @version 1.0
 */
public class OutputServiceController extends AbstractOutputService implements OutputService {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_OUTPUT);
	private ThriftConnectionProvider thriftConnectionProvider;

	public ThriftConnectionProvider getThriftConnectionProvider() {
		return thriftConnectionProvider;
	}

	public void setThriftConnectionProvider(ThriftConnectionProvider thriftConnectionProvider) {
		this.thriftConnectionProvider = thriftConnectionProvider;
	}

	public void write(OutputObject oo) {
		Client client = this.thriftConnectionProvider.getObject();
		try {
			HashMap<Long, List<ItemModel>> items = this.buildItems(oo);
			client.upsertItems(items);
		} catch (Exception e) {
			this.thriftConnectionProvider.invalidateObject(client);
			client = null;
			logger.error("Error occurred while save newurls to controller via thrift!{}", e);
		} finally {
			this.thriftConnectionProvider.returnObject(client);// 及时释放连接对象
		}
	}

	@SuppressWarnings("serial")
	private HashMap<Long, List<ItemModel>> buildItems(OutputObject oo) {
		HashMap<Long, List<ItemModel>> result = new HashMap<Long, List<ItemModel>>() {
			{
				put(Long.parseLong(oo.getTaskId()), new ArrayList<ItemModel>() {
					{
						add(new ItemModel() {
							{
								setId(SpiderStringUtil.md5Encode(oo.convertItem2Json()));
								setValue(oo.convertItem2Json());
							}
						});
					}
				});
			}
		};

		return result;
	}

	@Override
	public boolean existInRepertory(Image image) {
		return false;
	}

	@Override
	public void uploadImage(Image image,OutputObject oo) {
	}

	@Override
	public void createImageRepertoryUrl(Image image,OutputObject oo) {
		
	}
}