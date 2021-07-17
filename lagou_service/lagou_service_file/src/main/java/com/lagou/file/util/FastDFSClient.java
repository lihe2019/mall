package com.lagou.file.util;

import com.lagou.file.pojo.FastDFSFile;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author lihe
 * @Version 1.0
 */
public class FastDFSClient {

    private static Logger logger = LoggerFactory.getLogger(FastDFSClient.class);

    /**
     * 初始化加载FastDFS的TrackerServer配置信息
     */
    static {
        try {
            String filePath = new ClassPathResource("fdfs_client.conf").getFile().getAbsolutePath();
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 文件上传
     * 返回：group、remoteFileName
     */
    public static String[] upload(FastDFSFile file) {
        //文件元数据
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("author", file.getAuthor());
        //文件上传
        StorageClient storageClient = null;
        //返回结果
        String[] results = null;
        try {
            storageClient = getStorageClient();
            results = storageClient.upload_file(file.getContent(), file.getExt(), meta_list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("上传文件时发送异常:" + file.getName());
        }
        return results;
    }

    /**
     * 获取文件信息
     */
    public static FileInfo getFileInfo(String group, String remoteFileName) {
        StorageClient storageClient = null;
        FileInfo info = null;
        try {
            storageClient = getStorageClient();
            info = storageClient.get_file_info(group, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取文件信息时发生异常:" + group + "," + remoteFileName);
        }
        return info;
    }

    /**
     * 文件下载
     *
     * @param group
     * @param remoteFileName
     * @return
     */
    public static InputStream downFile(String group, String remoteFileName) {
        try {

            StorageClient storageClient = getStorageClient();
            byte[] bytes = storageClient.download_file(group, remoteFileName);
            InputStream inputStream = new ByteArrayInputStream(bytes);
            return inputStream;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("下载文件时发生异常:"+group+","+remoteFileName);
        }
        return null;
    }

    /**
     * 删除文件
     * @param group
     * @param remoteFileName
     */
    public static void deleteFile(String group, String remoteFileName){
        try {
            StorageClient storageClient = getStorageClient();
            storageClient.delete_file(group, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("删除文件时发生异常:"+group+","+remoteFileName);
        }
    }

    /**
     * 获得TrackerServer
     *
     * @return
     */
    public static TrackerServer getTrackerServer() throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerServer;
    }

    /**
     * 获取StorageClient
     *
     * @return
     * @throws IOException
     */
    public static StorageClient getStorageClient() throws IOException {
        TrackerServer trackerServer = getTrackerServer();
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

    /**
     * 获取TrackerURL
     *
     * @return
     * @throws IOException
     */
    public static String getTrackerURL() throws IOException {
        return "http://" + getTrackerServer().getInetSocketAddress().getHostString() + ":" + ClientGlobal.getG_tracker_http_port() + "/";
    }

    /**
     * 获得存储组信息
     */
    public static StorageServer[] getStorageServer(String groupName) throws IOException {
        //创建TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //获得TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取组
        return trackerClient.getStoreStorages(trackerServer, groupName);
    }

    /**
     * 获得存储服务器的信息，IP和端口
     *
     * @param groupName
     * @param remoteFileName
     * @return
     * @throws IOException
     */
    public static ServerInfo[] getFetchStorage(String groupName, String remoteFileName) throws IOException {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
    }

}
