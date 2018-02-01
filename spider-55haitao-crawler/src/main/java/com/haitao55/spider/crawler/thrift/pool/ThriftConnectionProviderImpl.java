package com.haitao55.spider.crawler.thrift.pool;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：对象池接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:48:35
 * @version 1.0
 */
public class ThriftConnectionProviderImpl implements ThriftConnectionProvider,
        InitializingBean, DisposableBean {
    public static final Logger logger = LoggerFactory
            .getLogger(Constants.LOGGER_NAME_SYSTEM);
    /**
     * 服务器的IP地址
     */
    private String serviceIP;
    /**
     * 服务器的端口
     */
    private int servicePort;
    /**
     * 连接超时时间
     */
    private int connectTimeOut;
    /**
     * 初始化缓冲区容量
     */
    private int initialBufferCapacity;
    /**
     * 最大数据长度
     */
    private int maxLength;
    /**
     * 可以从缓存池中分配对象的最大数量
     */
    private int maxActive = GenericObjectPool.DEFAULT_MAX_ACTIVE;
    /**
     * 缓存池中最大空闲对象数量
     */
    private int maxIdle = GenericObjectPool.DEFAULT_MAX_IDLE;
    /**
     * 缓存池中最小空闲对象数量
     */
    private int minIdle = GenericObjectPool.DEFAULT_MIN_IDLE;
    /**
     * 阻塞的最大数量
     */
    private long maxWait = GenericObjectPool.DEFAULT_MAX_WAIT;
    /**
     * 从缓存池中分配对象，是否执行PoolableObjectFactory.validateObject方法
     */
    private boolean testOnBorrow = GenericObjectPool.DEFAULT_TEST_ON_BORROW;
    private boolean testOnReturn = GenericObjectPool.DEFAULT_TEST_ON_RETURN;
    private boolean testWhileIdle = GenericObjectPool.DEFAULT_TEST_WHILE_IDLE;
    /**
     * 对象缓存池
     */
    private ObjectPool objectPool = null;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化对象池
        objectPool = new GenericObjectPool();
        ((GenericObjectPool) objectPool).setMaxActive(maxActive);
        ((GenericObjectPool) objectPool).setMaxIdle(maxIdle);
        ((GenericObjectPool) objectPool).setMinIdle(minIdle);
        ((GenericObjectPool) objectPool).setMaxWait(maxWait);
        ((GenericObjectPool) objectPool).setTestOnBorrow(testOnBorrow);
        ((GenericObjectPool) objectPool).setTestOnReturn(testOnReturn);
        ((GenericObjectPool) objectPool).setTestWhileIdle(testWhileIdle);
        ((GenericObjectPool) objectPool)
                .setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        // 初始化并设置factory
        ThriftPoolableObjectFactory thriftPoolableObjectFactory = new ThriftPoolableObjectFactory(
                serviceIP, servicePort, connectTimeOut, initialBufferCapacity,
                maxLength);
        objectPool.setFactory(thriftPoolableObjectFactory);
        logger.info("objectPool:["+objectPool+"],serviceIP:["+serviceIP+"],servicePort:["+servicePort+"]");
    }

    @Override
    
    public Client getObject() {
        try {
            Client obj = (Client) objectPool.borrowObject();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("error getObject()", e);
        }
    }

    @Override
    public void invalidateObject(Client obj) {
        try {
            obj.getInputProtocol().getTransport().close();
            objectPool.invalidateObject(obj);
        } catch (Exception e) {
            throw new RuntimeException("error invalidateObject()", e);
        }
    }

    @Override
    public void returnObject(Client obj) {
        try {
            if (obj != null) {
                objectPool.returnObject(obj);
            }
        } catch (Exception e) {
            throw new RuntimeException("error returnObject()", e);
        }
    }

    @Override
    public void destroy() {
        try {
            objectPool.close();
        } catch (Exception e) {
            throw new RuntimeException("erorr destroy()", e);
        }
    }

    public String getServiceIP() {
        return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
        this.serviceIP = serviceIP;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public int getInitialBufferCapacity() {
        return initialBufferCapacity;
    }

    public void setInitialBufferCapacity(int initialBufferCapacity) {
        this.initialBufferCapacity = initialBufferCapacity;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public ObjectPool getObjectPool() {
        return objectPool;
    }

    public void setObjectPool(ObjectPool objectPool) {
        this.objectPool = objectPool;
    }
}