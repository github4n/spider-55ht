package com.haitao55.spider.crawler.core.callable.custom.finishline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.HttpUtils;

public class FinishlineHandler {
	private static final int threadCount = 3;

	private ExecutorService executorService;

	public FinishlineHandler() {
		executorService = Executors.newFixedThreadPool(threadCount);
	}

	public JSONObject process(List<String> list, Proxy proxy) {
		JSONObject resultJsonObject = new JSONObject();
		if (null == list || list.size() == 0) {
			return resultJsonObject;
		}

		List<Callable<JSONObject>> calls = new ArrayList<Callable<JSONObject>>();

		for (String param : list) {
			calls.add(new FinishlineCallaber(proxy, param, resultJsonObject));
		}

		try {
			List<Future<JSONObject>> futures = executorService.invokeAll(calls);
			try {
				for (Future<JSONObject> future : futures) {
					future.get(HttpUtils.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {

				e.printStackTrace();
			}
		} catch (InterruptedException e) {

		} finally {
			executorService.shutdownNow();
		}
		return resultJsonObject;

	}

}
