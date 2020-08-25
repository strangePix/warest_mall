package com.warest.mall.service.impl;

import com.google.common.collect.Lists;
import com.warest.mall.service.IFileService;
import com.warest.mall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件处理用service
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);


    public static void main(String[] args) {
        String str = "abc.345.def";
        System.out.println(str.substring(str.lastIndexOf(".")+1));
    }

    /**
     * 上传文件
     * @param file 文件
     * @param path 上传路径
     * @return
     */
    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename(); //文件名
        //扩展名
        //abc.jpg
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1); //文件名最后一个点后的内容
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        //todo logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",new Object[]{fileName,path,uploadFileName});

        File fileDir = new File(path); //上传文件夹
        if(!fileDir.exists()){
            fileDir.setWritable(true); //赋予权限可写  保证可以创建
            fileDir.mkdirs(); //有可能是嵌套文件夹，用mkdirs复数可以一并创建
        }
        // 创建文件，存放路径和文件名
        File targetFile = new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            //文件已经上传成功了

            //将文件上传到ftp服务器，可以传多个，但目前只穿了一个？
            if(!FTPUtil.uploadFile(Lists.newArrayList(targetFile))){
                logger.error("上传文件失败");
                return null;
            }
            //上传成功，删除网页服务器文件

            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        //A:abc.jpg
        //B:abc.jpg
        //返回目标文件文件名
        return targetFile.getName();
    }

}
