package com.haitao55.spider.crawler.thrift.pool;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.ThriftService.Client;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：Thrift的可池化对象工厂
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午11:48:15
 * @version 1.0
 */
public class ThriftPoolableObjectFactory implements PoolableObjectFactory {
    public static final Logger logger = LoggerFactory
            .getLogger(Constants.LOGGER_NAME_SYSTEM);

    /**
     * 服务器的IP地址
     */
    private String serviceIP;
    /**
     * 服务的端口
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
     * 构造方法
     * 
     * @param serviceIP
     * @param servicePort
     * @param timeOut
     */
    public ThriftPoolableObjectFactory(String serviceIP, int servicePort,
            int connectTimeOut, int initialBufferCapacity, int maxLength) {
        this.serviceIP = serviceIP;
        this.servicePort = servicePort;
        this.connectTimeOut = connectTimeOut;
        this.initialBufferCapacity = initialBufferCapacity;
        this.maxLength = maxLength;
    }

    @Override
    public Object makeObject() throws Exception {
        try {
            TSocket tSocket = new TSocket(this.serviceIP, this.servicePort,
                    this.connectTimeOut);
            TFastFramedTransport framedTransport = new TFastFramedTransport(
                    tSocket, initialBufferCapacity, maxLength);
            TBinaryProtocol protocol = new TBinaryProtocol(framedTransport);
            Client client = new Client(protocol);
            
            tSocket.open();

            return client;
        } catch (Exception e) {
            logger.error("error ThriftPoolableObjectFactory()", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroyObject(Object obj) throws Exception {
        if (obj instanceof Client) {
            TTransport socket = ((Client) obj).getInputProtocol()
                    .getTransport();
            if (socket.isOpen()) {
                socket.close();
            }
        }
    }

    @Override
    public boolean validateObject(Object obj) {
        try {
            if (obj instanceof Client) {
                TTransport thriftSocket = ((Client) obj).getInputProtocol()
                        .getTransport();
                if (thriftSocket.isOpen()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void passivateObject(Object obj) throws Exception {
        // do nothing
    }

    @Override
    public void activateObject(Object obj) throws Exception {
        // do nothing
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
}