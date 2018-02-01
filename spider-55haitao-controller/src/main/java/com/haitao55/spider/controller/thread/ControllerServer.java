package com.haitao55.spider.controller.thread;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.thrift.ThriftService;

/**
 * 
 * 功能：Controller模块的Server类,在一个新线程中启动和管理ThriftServer类
 * 
 * @author Arthur.Liu
 * @time 2016年7月28日 上午10:27:10
 * @version 1.0
 */
public class ControllerServer extends Thread {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private TServer thriftServer = null;

	private boolean isRunning = false;

	public ControllerServer(ThriftService.Iface service, int port, int workerNum, int selectorNum, int acceptQueueSize,
			int maxReadBufferSize, int clientTimeout) {
		try {
			logger.info("Create thrift-server....");
			this.thriftServer = this.createThriftServer(service, port, clientTimeout, workerNum, selectorNum,
					acceptQueueSize, maxReadBufferSize);
			logger.info("Create thrift-server successfully!");
		} catch (TTransportException e) {
			logger.error("Create thrift-server failed!", e);
		}
	}

	private TServer createThriftServer(ThriftService.Iface service, int port, int clientTimeout, int workerNum,
			int selectorNum, int acceptQueueSize, int maxReadBufferSize) throws TTransportException {
		TBinaryProtocol.Factory protFactory = new TBinaryProtocol.Factory();
		ThriftService.Processor<ThriftService.Iface> processor = new ThriftService.Processor<ThriftService.Iface>(
				service);

		TNonblockingServerTransport nioTransport = new TNonblockingServerSocket(port, clientTimeout);
		TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(nioTransport);
		args.protocolFactory(protFactory);
		args.processor(processor);
		args.workerThreads(workerNum);
		args.selectorThreads(selectorNum);
		args.acceptQueueSizePerThread(acceptQueueSize);
		args.maxReadBufferBytes = maxReadBufferSize;

		TServer thriftServer = new TThreadedSelectorServer(args);
		return thriftServer;
	}

	@Override
	public void run() {
		if (this.thriftServer != null) {
			logger.info("Start thrift-server....");

			this.thriftServer.serve();
			this.isRunning = true;

			logger.info("Start thrift-server successfully!");
		} else {
			logger.error("thrift-server is null so cann't to be started, please check!!!");
		}
	}

	public synchronized void stopServer() {
		if (this.thriftServer != null) {
			logger.info("Stop thrift-server....");

			this.thriftServer.stop();
			this.isRunning = false;

			logger.info("Stop thrift-server successfully!");
		}
	}

	public boolean isRunning() {
		return this.isRunning;
	}
}