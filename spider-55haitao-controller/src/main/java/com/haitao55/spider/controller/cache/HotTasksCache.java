package com.haitao55.spider.controller.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.TaskModel;

/**
 * 
 * 功能：
 * <p>
 * 这是一个单例类；
 * </p>
 * <p>
 * 维护着系统中所有"热门('运行中'和'丢弃中')"任务的配置数据；
 * </p>
 * <p>
 * 定时从数据库中获得更新；
 * </p>
 * 
 * @author Arthur.Liu
 * @time 2016年8月12日 下午2:20:38
 * @version 1.0
 */
public class HotTasksCache extends ConcurrentHashMap<Long, TaskModel> {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger("system");

	private HotTasksCache() {
		// nothing
	}

	private static class HotTasksCacheHolder {
		private static final HotTasksCache cache = new HotTasksCache();
	}

	public static HotTasksCache getInstance() {
		return HotTasksCacheHolder.cache;
	}

	public HotTasksCache deepClone() {
		HotTasksCache result = null;

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.flush();

			ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
			result = (HotTasksCache) ois.readObject();
		} catch (IOException e) {
			logger.error("Error while deel-clone HotTasksCache instance!", e);
		} catch (ClassNotFoundException e) {
			logger.error("Error while deel-clone HotTasksCache instance!", e);
		} finally {
			if (oos != null) {
				IOUtils.closeQuietly(oos);
			}
			if (ois != null) {
				IOUtils.closeQuietly(ois);
			}
		}

		return result;
	}
}