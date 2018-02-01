package com.haitao55.spider.crawler.core.callable.custom.amazon.imp;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author jerome
 *
 * @param <T>
 */
public class HTQueue<T> {

	private Queue<T> buff ;
	private int capacity = 1000;
	private AtomicLong count = new AtomicLong();
	
	public HTQueue(){
		buff = new LinkedList<T>();
	}
	
	public void add(T e){
		synchronized (buff) {
			if (count.incrementAndGet() > this.capacity){ // 队列size大于设定的容量，当前队列开始等待
				try {
					buff.wait();//阻塞，释放对象锁
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			buff.offer(e);
			buff.notifyAll();
		}
	}
	
	
	public T get(){
		T v = null;
		synchronized (buff) {
			v = buff.poll();
			if (v == null) {
				try {
					buff.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				count.decrementAndGet();
			
			}
			buff.notifyAll();
		}
		return v;
	}
	
	public  void notified(){
		synchronized (buff) {
			buff.notifyAll();
		}
	}
	
	public long getTotalCount() {
		return count.get();
	}
}
