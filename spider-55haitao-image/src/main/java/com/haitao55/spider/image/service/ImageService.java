package com.haitao55.spider.image.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.dao.ImageDAO;
import com.haitao55.spider.common.dos.ImageDO;
import com.haitao55.spider.common.utils.SpiderStringUtil;


@Service("imageService")
public class ImageService {
	
	private ImageDAO imageDAO;
	
	
	public boolean isCdnKeyExist(long taskId,String cdn_key) throws Exception{
		if(StringUtils.isBlank(cdn_key)){
			return false;
		}
		ImageDO imageDo = new ImageDO();
		imageDo.setCdn_key(cdn_key);
		long count = imageDAO.countImagesBy(taskId, imageDo);
		if(count > 0){
			return true;
		}
		return false;
	}
	
	public boolean isSrcExist(long taskId,String src) throws Exception{
		if(StringUtils.isBlank(src)){
			return false;
		}
		String src_key = SpiderStringUtil.md5Encode(src);
		ImageDO imageDo = new ImageDO();
		imageDo.setSrc_key(src_key);
		long count = imageDAO.countImagesBy(taskId, imageDo);
		if(count > 0){
			return true;
		}
		return false;
	}
	
	
	public boolean saveImages(long taskId,List<ImageDO> imageDoList)throws Exception{
		imageDAO.insertImages(taskId, imageDoList);
		return true;
	}
	
	public boolean updateImages(long taskId,List<ImageDO> imageDoList) throws Exception{
		imageDAO.updateImages(taskId, imageDoList);
		return true;
	}
	

	public ImageDAO getImageDAO() {
		return imageDAO;
	}

	public void setImageDAO(ImageDAO imageDAO) {
		this.imageDAO = imageDAO;
	}
	

}
