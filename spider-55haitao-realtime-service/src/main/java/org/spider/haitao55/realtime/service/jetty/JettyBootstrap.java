
package org.spider.haitao55.realtime.service.jetty;
import java.util.Map;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spider.haitao55.realtime.service.handlers.AbstractHttpHandler;
import org.spider.haitao55.realtime.service.handlers.HelloServlet;
import org.spider.haitao55.realtime.service.utils.RealTimeServiceConfig;
/**
 * jetty启动类
  * @ClassName: JettyBootstrap
  * @author songsong.xu
  * @date 2017年1月11日 上午11:17:43
  *
 */
public class JettyBootstrap {

    private static final Logger LOG = LoggerFactory.getLogger(JettyBootstrap.class);

    private int maxThreads = 50;
    private int timeOut = 60000;
    private Server server;
    private Map<String,AbstractHttpHandler> servlets;
    
    public JettyBootstrap() throws JettyBootstrapException{
    	this(null);
    }
    public JettyBootstrap(Map<String,AbstractHttpHandler> servlets) throws JettyBootstrapException {
    	this.servlets = servlets;
    	initServer();
    }

    /**
     * 
      * jetty 启动
      * @Title: startServer
      * @param @return
      * @param @throws JettyBootstrapException    设定文件
      * @return JettyBootstrap    返回类型
      * @throws
     */
    public JettyBootstrap startServer() throws JettyBootstrapException {
        LOG.info("Starting Server...");
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            throw new JettyBootstrapException(e);
        }
        
        return this;
    }


    /**
     * Return if server is started
     * 
     * @return if server is started
     */
    public boolean isServerStarted() {
        return (server != null && server.isStarted());
    }

    /**
      * 停止jetty server
      * @Title: stopServer
      * @param @return
      * @param @throws JettyBootstrapException    设定文件
      * @return JettyBootstrap    返回类型
      * @throws
     */
    public JettyBootstrap stopServer() throws JettyBootstrapException {
        LOG.info("Stopping Server...");
        try {
            if (isServerStarted()) {
                server.stop();
                LOG.info("Server stopped.");
            } else {
                LOG.warn("Can't stop server. Already stopped");
            }
        } catch (Exception e) {
            throw new JettyBootstrapException(e);
        }
        return this;
    }

   

   /**
     * 添加的请求处理类
     * @Title: addHandler
     * @param @param handler
     * @param @return
     * @param @throws JettyBootstrapException    设定文件
     * @return Handler    返回类型
     * @throws
    */
    public Handler addHandler(Handler handler) throws JettyBootstrapException {
        /*JettyHandler jettyHandler = new JettyHandler();
        jettyHandler.setHandler(handler);
        handlers.addHandler(handler);*/

        return handler;
    }

    /**
      * 获取jetty server的对象实例
      * @Title: getServer
      * @param @return
      * @param @throws JettyBootstrapException    设定文件
      * @return Server    返回类型
      * @throws
     */
    public Server getServer() throws JettyBootstrapException {
        return server;
    }

    /**
      * 初始化jetty server
      * @Title: initServer
      * @param @throws JettyBootstrapException    设定文件
      * @return void    返回类型
      * @throws
     */
    protected void initServer() throws JettyBootstrapException {
        if (server == null) {
            //server = createServer();
            LOG.trace("Create Jetty Server...");

            server = new Server(new QueuedThreadPool(maxThreads));//new QueuedThreadPool(maxThreads)
            ServerConnector connector = new ServerConnector(server);
            connector.setName("JettyConnection");
            connector.setAcceptQueueSize(50);
            connector.setIdleTimeout(timeOut);
            connector.setPort(Integer.valueOf(RealTimeServiceConfig.JETTY_PORT));
            server.addConnector(connector);
            server.setStopAtShutdown(false); 
            server.setStopTimeout(timeOut);
            
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[]{context, new DefaultHandler()});
            server.setHandler(handlers);
            ServletHolder servletHolder = new ServletHolder(new HelloServlet());
            servletHolder.setInitOrder(1);
            servletHolder.setAsyncSupported(true);
            context.addServlet(servletHolder,"/hello");
            
            if(servlets != null && servlets.size() > 0){
            	for(Map.Entry<String,AbstractHttpHandler> entry : servlets.entrySet() ){
            		servletHolder = new ServletHolder(entry.getValue());
                    servletHolder.setInitOrder(1);
                    servletHolder.setAsyncSupported(true);
            		context.addServlet(servletHolder, entry.getKey());
            	}
            }
            createShutdownHook();
        }
    }

    /**
      *  创建一个关闭jetty server的钩子
      * @Title: createShutdownHook
      * @param     设定文件
      * @return void    返回类型
      * @throws
     */
    private void createShutdownHook() {
        LOG.trace("Creating Jetty ShutdownHook...");

        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    LOG.debug("Shutting Down...");
                    stopServer();
                } catch (Exception e) {
                    LOG.error("Shutdown", e);
                }
            }
        });
    }
}
