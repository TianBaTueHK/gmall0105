package com.atguigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;


/**
 * 图片上传的工具类
 */
public class PmsUploadUtil {

    public static String uploadImage(MultipartFile multipartFile) {

        String imgUrl = "http://192.168.222.20";

        //上传图片服务器
        //配置fdfs的全局连接地址
        String tracker = PmsUploadUtil.class.getResource("").getPath();//获得配置文件的路径

        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        //获得一个trackerServer的实例
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //通过tracker获得一个Storage连接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);

        try {

            byte[] bytes = multipartFile.getBytes(); //获得上传的二进制对象

            //获得图片文件后缀名
            String originalFilename = multipartFile.getOriginalFilename(); // a.jpg
            int i = originalFilename.lastIndexOf(".");
            String extName = originalFilename.substring(i + 1);

            String[] uploadInfos = storageClient.upload_file(bytes,"jpg",null);


            for (String uploadInfo : uploadInfos){
                imgUrl += "/" + uploadInfo ;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return imgUrl;
    }
}



























