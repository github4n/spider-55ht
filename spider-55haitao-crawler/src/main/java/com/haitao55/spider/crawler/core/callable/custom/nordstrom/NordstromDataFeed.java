package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;

public class NordstromDataFeed extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "shop.nordstrom.com";
	private static final String remoteDir = "/1237/";
	private static final String ftp_user = "55haitao";
	private static final String ftp_pass = "bvBWzfeb";
	
	@Override
	public void invoke(Context context) throws Exception {
		boolean isSuc = downloadFiles();
		if(isSuc){
			
		}
	}

	public static boolean downloadFiles() {
		FTPClient ftp = new FTPClient();
		try {
			ftp.setDefaultTimeout(60000);
			ftp.connect("aftp.linksynergy.com");
			ftp.setSoTimeout(60000);
			logger.info("Connected to aftp.linksynergy.com");
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				logger.error("FTP server refused connection.");
			}
			ftp.login(ftp_user, ftp_pass);
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
			
			for (FTPFile f : ftp.listFiles(remoteDir)) {
				OutputStream output = null;
				InputStream inputStream = null;
				for(int i =0; i < 3; i++){
					try{
						logger.info("fileName : "+f.getName());
						String remote = remoteDir + f.getName();
						inputStream = ftp.retrieveFileStream(remote);
						ftp.setDataTimeout(60000);
						byte[] bytesArray = new byte[4096];
						if(StringUtils.endsWith(remote, ".gz")){
							String localName = StringUtils.substringBefore(f.getName(), ".gz");
							output = new BufferedOutputStream(new FileOutputStream(new File("/data/nordstrom/"+ localName)));
							InputStream gis = new GZIPInputStream(inputStream);
							int bytesRead = -1;
							while ((bytesRead = gis.read(bytesArray)) != -1) {
								output.write(bytesArray, 0, bytesRead);
							}
							IOUtils.closeQuietly(gis);
						} else {
							String localName = f.getName();
							output = new BufferedOutputStream(new FileOutputStream(new File("/data/nordstrom/"+ localName)));
							int bytesRead = -1;
							while ((bytesRead = inputStream.read(bytesArray)) != -1) {
								output.write(bytesArray, 0, bytesRead);
							}
						}
						boolean success = ftp.completePendingCommand();
						if (success) {
							logger.info("File {} has been downloaded successfully.",remote);
							break;
						}
					}catch(IOException e){
						e.printStackTrace();
					}finally {
						IOUtils.closeQuietly(inputStream);
						IOUtils.closeQuietly(output);
					}
				}
				System.out.println("==========================================");
			}
			return true;
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			logger.error("Could not connect to server.");
			e.printStackTrace();
		}
		return false;
		
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// GzipCompress.decompress(new FileInputStream(new
		// File("/data/1237_2927537_100399121_cmp.xml.gz")), new
		// FileOutputStream(new File("/data/nordstrom_datafeed_test")));
		// /1237

		FTPClient ftp = new FTPClient();

		try {
			int reply;
			ftp.connect("aftp.linksynergy.com");
			System.out.println("Connected to aftp.linksynergy.com");
			// After connection attempt, you should check the reply code to
			// verify
			// success.
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
				System.exit(1);
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
			System.err.println("Could not connect to server.");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			String remoteDir = "/1237/";
			ftp.login("55haitao", "bvBWzfeb");
			boolean bin = ftp.setFileType(FTP.BINARY_FILE_TYPE);
			System.out.println("bin:"+bin);
			ftp.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
			for (FTPFile f : ftp.listFiles(remoteDir)) {
				System.out.println(f.getName());
				String remote = remoteDir + f.getName();
				InputStream inputStream = ftp.retrieveFileStream(remote);
				ftp.setDataTimeout(60000);
				byte[] bytesArray = new byte[4096];
				OutputStream output = null;
				if(StringUtils.endsWith(remote, ".gz")){
					String localName = StringUtils.substringBefore(f.getName(), ".gz");
					output = new BufferedOutputStream(new FileOutputStream(new File("/data/nordstrom/"+ localName)));
					
					InputStream gis = new GZIPInputStream(inputStream);
					int bytesRead = -1;
					while ((bytesRead = gis.read(bytesArray)) != -1) {
						output.write(bytesArray, 0, bytesRead);
					}
					boolean success = ftp.completePendingCommand();
					if (success) {
						System.out.println("File #2 has been downloaded successfully.");
					}
					gis.close();
				} else {
					String localName = f.getName();
					output = new BufferedOutputStream(new FileOutputStream(new File("/data/nordstrom/"+ localName)));
					int bytesRead = -1;
					while ((bytesRead = inputStream.read(bytesArray)) != -1) {
						output.write(bytesArray, 0, bytesRead);
					}
				}
				output.flush();
				output.close();
				inputStream.close();
				System.out.println("==========================================");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("FTP客户端出错！", e);
		} finally {
			// IOUtils.closeQuietly(fos);
			try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("关闭FTP连接发生异常！", e);
			}
		}
	}
}
