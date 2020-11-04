package com.warest.mall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 文件上传工具列
 */
@Component
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static final FTPUtil ftpUtil = new FTPUtil();

    private FTPUtil() {
    }

    private static String ip;

    private static int port;

    private static String user;

    private static String pwd;

    public static String FTP_PREFIX;

    @Value("${ftp.server.http.prefix}")
    public void setFtpPrefix(String ftpPrefix) {
        FTP_PREFIX = ftpPrefix;
    }

    @Value("${ftp.server.ip}")
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Value("${ftp.server.port}")
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${ftp.user}")
    public void setUser(String user) {
        this.user = user;
    }

    @Value("${ftp.pass}")
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    private FTPClient ftpClient;


    /**
     * 批量上传
     *
     * @param fileList
     * @return
     * @throws IOException
     */
    public static boolean uploadFile(List<File> fileList) throws IOException {
        // FTPUtil ftpUtil = FTPUtil.getInstance();
        logger.info("开始连接ftp服务器");
        //这个目录斜杠开头表示ftp服务器根目录开始
        boolean result = ftpUtil.uploadFile("/img", fileList);
        logger.info("结束上传,上传结果:{}",result);
        return result;
    }


    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException {
        boolean uploaded = true;
        //FileInputStream fis = null;
        //连接FTP服务器
        if (connectServer()) {
            try {
                //切换文件夹
                if(!existFile(remotePath)){
                    if(!makeDirectory(remotePath))return false;
                };
                boolean change = ftpClient.changeWorkingDirectory(remotePath);
                // logger.info("切换目录结果：{}", change);
                //设置缓冲区
                ftpClient.setBufferSize(1024);
                //设置编码
                // ftpClient.setControlEncoding("UTF-8");
                // 上传文件类型  二进制
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                // 因为ftp设置了被动模式，开放服务端口范围 这里打开本地被动模式
                ftpClient.enterLocalPassiveMode();

                // testFile();

                // 上传文件
                for (File fileItem : fileList) {
                    try (FileInputStream fis = new FileInputStream(fileItem)) {
                        uploaded &= ftpClient.storeFile(fileItem.getName(), fis);
                    }
                }
            } catch (IOException e) {
                logger.error("上传文件异常", e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                // 释放资源
                //fis.close();
                ftpClient.disconnect(); //存在异常，抛出
            }
        }
        return uploaded;
    }


    /**
     * 测试类
     * @throws IOException
     */
    private boolean existFile(String path) throws IOException {
        // System.out.println("img文件列表显示：");
        FTPFile[] ftpFileArr = ftpClient.listFiles(path);
        for (FTPFile ftpFile : ftpFileArr) {
            logger.info("文件：{}",ftpFile.getName());
        }
        return ftpFileArr.length > 0;
    }


    // 创建目录  目前仅考虑单层情况  实际只是为了创建img文件夹
    private boolean makeDirectory(String dir) throws UnsupportedEncodingException {
        dir = new String(dir.getBytes("GBK"), "iso-8859-1");  //不这么设也行，不过设了之后速度很快
        boolean flag = true;
        try {
            flag = ftpClient.makeDirectory(dir);
            logger.info("创建目录{}结果：{}", dir,flag);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("创建上传目录{}失败",dir);
        }
        return flag;
    }

        /**
         * 连接ftp客户端
         *
         * @return
         */
    private boolean connectServer() {

        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.setControlEncoding("utf-8");
            ftpClient.connect(ip,port);
            isSuccess = ftpClient.login(user, pwd)&& FTPReply.isPositiveCompletion(ftpClient.getReplyCode());
        } catch (IOException e) {
            logger.error("连接FTP服务器异常", e);
        }
        return isSuccess;
    }


}
