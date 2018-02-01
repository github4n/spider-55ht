package com.haitao55.spider.crawler.core.callable.custom.nordstrom;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompress {

	private static final int BUFFER = 1024 * 4;

	public static void compress(InputStream is, OutputStream os)
			throws Exception {
		GZIPOutputStream gos = new GZIPOutputStream(os);
		try {
			int count;
			byte data[] = new byte[BUFFER];
			while ((count = is.read(data, 0, BUFFER)) != -1) {
				gos.write(data, 0, count);
			}
		} finally {
			gos.finish();
			gos.flush();
			if(gos != null){
				gos.close();
			}
		}

	}

	public static void decompress(InputStream in, OutputStream out)
			throws IOException {
		InputStream gis = new GZIPInputStream(in);
		try{
			byte[] b = new byte[BUFFER];
			int r = gis.read(b);
			while (r >= 0) {
				out.write(b, 0, r);
				r = gis.read(b);
			}
		}finally{
			if(gis != null){
				gis.close();
			}
		}
		
	}
	
	/*public static void doDecompress(File srcFile, String destDir) throws IOException {  
        InputStream is = null;  
        OutputStream os = null;  
        try {  
            File destFile = new File(destDir, FilenameUtils.getBaseName(srcFile.toString()));  
            is = new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(srcFile)));  
            os = new BufferedOutputStream(new FileOutputStream(destDir,true));  
            decompress(); 
        } finally {  
            IOUtils.closeQuietly(is);  
            IOUtils.closeQuietly(os);  
        }  
    }  */
	
	public static void main(String[] args) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File("/data/nordstrom/1237_2927537_100399121_cmp.xml.gz"))); 
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("/data/1237_2927537_100399121_cmp.xml",true)); 
		decompress( bis, bos);
	}

}
