package org.spider.haitao55.realtime.service.handlers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.spider.haitao55.realtime.service.utils.RealTimeServiceConfig;


public abstract class AbstractHttpHandler extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    private GenericObjectPool pool; //Queue<ChromeDriver> queue = new ConcurrentLinkedQueue<ChromeDriver>();


    @Override
    public void init() throws ServletException {
        HaiTaoPoolableObjectFactory factory = new HaiTaoPoolableObjectFactory();  
        pool = new GenericObjectPool(factory);  
        pool.setMaxActive(5); // 能从池中借出的对象的最大数目  
        pool.setMaxIdle(5); // 池中可以空闲对象的最大数目  
        pool.setMaxWait(-1); // 对象池空时调用borrowObject方法，最多等待多少毫秒  
        pool.setTimeBetweenEvictionRunsMillis(600000);// 间隔每过多少毫秒进行一次后台对象清理的行动  
        pool.setNumTestsPerEvictionRun(-1);// －1表示清理时检查所有线程  
        pool.setMinEvictableIdleTimeMillis(600000);// 设定在进行后台对象清理时，休眠时间超过了3000毫秒的对象为过期
        pool.setTestOnBorrow(true);
    	//new Thread(new Moniter(queue)).start();
        initialize();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doTask(req, resp);
    }

    @Override
    public void destroy() {
        finalize();
    }

    public abstract void initialize();

    public abstract void finalize();

    public abstract void doTask(HttpServletRequest req, HttpServletResponse resp);

    public InputStream reqConverStream(HttpServletRequest req) throws IOException {
        return req.getInputStream();
    }

    public byte[] reqConverByte(InputStream is) throws IOException {
        return readStream(is);
    }

    public String reqConverString(byte[] bs, String charset) throws IOException {
        if (charset == null || charset.equals(""))
            charset = "UTF-8";
        return new String(bs, charset);
    }

    public String reqFastConverString(HttpServletRequest req) throws IOException {
        return reqConverString(reqConverByte(reqConverStream(req)), null);
    }

    public void output(String output, HttpServletResponse response) {
        output(output, response, "utf-8");
    }

    public void output(String output, HttpServletResponse response, String charset) {
        try {
        	response.setContentType("text/json;charset=" + charset);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(output);
			response.getWriter().flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	try {
				if(response.getWriter() != null){
					response.getWriter().close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    public byte[] readStream(InputStream is) throws IOException {
		ByteArrayOutputStream pos = new ByteArrayOutputStream();
		byte[] bf = new byte[1024];
		int ret = -1;
		while ((ret = is.read(bf)) != -1) {
			pos.write(bf, 0, ret);
		}
		pos.close();
		return pos.toByteArray();
	}
    
    protected ChromeDriver getDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("no-sandbox");
		options.addArguments("start-maximized");
		options.setBinary("/usr/bin/google-chrome-stable");
		options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
		//options.addExtensions(new File("/home/jerome/.config/google-chrome/Default/Extensions/padekgcemlokbadohgkifijomclgjgif/2.3.21_0.crx"));
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		ChromeDriver dr = new ChromeDriver(capabilities);
		dr.manage().window().maximize();
		return dr;
	}

	/*public Queue<ChromeDriver> getQueue() {
		return queue;
	}*/
    
    
	
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
	}
	
	
  public GenericObjectPool getPool() {
    return pool;
  }


  class Moniter implements Runnable {
		
		private Queue<ChromeDriver> queue;
		
		public Moniter(Queue<ChromeDriver> queue){
			this.queue = queue;
		}

		@Override
		public void run() {
			while(true){
				if(queue.size() < Integer.valueOf(RealTimeServiceConfig.CHROMEDRIVER_COUNT)){
					queue.offer(getDriver());
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
	}
    
}
