package com.haitao55.spider.util;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.view.DomainCounter;

/**
 * 导出excel
 * @author denghuan
 * date : 2017-4-27
 */
public class ExportExcelToDisk {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);
	
	public static boolean exportExcel(String excelFilePath){
		logger.info("start Export Excel::");
		long startTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currTime = sdf.format(System.currentTimeMillis());
		boolean flag = false;
		try{ 
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet(Constants.EXCEL_NAME + currTime);
			sheet.setColumnWidth(0, 40 * 256);
			sheet.setColumnWidth(1, 30 * 256);
			sheet.setColumnWidth(2, 30 * 256);
			HSSFRow rowHeader = sheet.createRow(0);
	
			HSSFCell cell = rowHeader.createCell(0);
			cell.setCellValue("Site Domain");
			cell = rowHeader.createCell(1);
			cell.setCellValue("Items Count");
	
			List<DomainCounter> doMainLIst = CleaingFullUtil.getDomainCountList(Constants.CLEANING_FULL_ITEM_ONLINE_FIELD_PREFIX);
			Collections.sort(doMainLIst,Collections.reverseOrder());
			
			int size = doMainLIst.size();
			for (int i = 1; i <= size; i++) {// 第1行(下标为0)已经是标题行了，所以数据行从第2行(下标为1)开始
				DomainCounter domainCount = doMainLIst.get(i - 1);
				HSSFRow rowData = sheet.createRow(i);
				rowData.createCell(0).setCellValue(domainCount.getDomain());
				rowData.createCell(1).setCellValue(domainCount.getCount());
			}
			// 第六步，将文件存到指定位置  
			 FileOutputStream fout = new FileOutputStream(excelFilePath+Constants.EXCEL_NAME+currTime+Constants.EXPORT_EXCEL_FILE_SUFFIX);  
			 workbook.write(fout);  
			 fout.close();  
			 IOUtils.closeQuietly(workbook);
			 flag = true;
        }catch (Exception e){
        	flag = false;
            logger.error("Export Excel exception!!!!!!!!!!!!!");
            e.printStackTrace();
        }  
        logger.info("end Export Excel::curr_Time : {}",System.currentTimeMillis()-startTime);
        
        return flag;
	}
}
