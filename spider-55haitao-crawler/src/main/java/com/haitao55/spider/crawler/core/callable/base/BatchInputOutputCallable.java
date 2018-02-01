package com.haitao55.spider.crawler.core.callable.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.haitao55.spider.crawler.core.callable.context.Context;

/**
 * 
 * 功能：抽象的Callalbe接口实现,为子类提供批量处理功能
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:28:44
 * @version 1.0
 */
public abstract class BatchInputOutputCallable extends AbstractCallable {

	@SuppressWarnings("unchecked")
	public void invoke(Context context) throws Exception {

		Object obj = getInputObject(context);
		if (obj == null) {
			return;
		}

		Set<Object> set = new LinkedHashSet<Object>();
		if (obj instanceof Collection) {// 集合
			Collection<Object> collection = (Collection<Object>) obj;
			Object[] array = collection.toArray(new Object[collection.size()]);
			set.addAll(invoke(array));
		} else if (obj.getClass().isArray()) {// 数组
			Object[] array = (Object[]) obj;
			set.addAll(invoke(array));
		} else {// 单值
			set.addAll(invoke(obj));
		}

		if (CollectionUtils.isEmpty(set)) {
			return;
		}

		if (set.size() == 1) {
			context.put(getOutput(), set.iterator().next());
		} else {
			context.put(getOutput(), set);
		}
	}

	private Set<Object> invoke(Object[] array) throws Exception {
		if (ArrayUtils.isEmpty(array)) {
			return Collections.emptySet();
		}

		Set<Object> set = new LinkedHashSet<Object>();
		for (Object obj : array) {
			if (obj == null) {
				continue;
			}
			set.addAll(invoke(obj));
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	private Set<Object> invoke(Object input) throws Exception {

		Object result = process(input);
		if (result == null) {
			return Collections.emptySet();
		}

		Set<Object> set = new LinkedHashSet<Object>();// 单个输入值，经过处理后的输出结果，也有可能为多个
		if (result instanceof Collection) {
			set.addAll((Collection<Object>) result);
		} else if (result.getClass().isArray()) {
			set.addAll(Arrays.asList((Object[]) result));
		} else {
			set.add(result);
		}
		return set;
	}

	/**
	 * 单个输入值元素的处理罗辑，在子类中实现
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	protected abstract Object process(Object input) throws Exception;
}