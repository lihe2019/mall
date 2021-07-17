package com.lagou.file.service.impl;

import com.lagou.file.pojo.FastDFSFile;
import com.lagou.file.service.FileService;
import com.lagou.file.util.FastDFSClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author lihe
 * @Version 1.0
 */
@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        String url = null;
        try {

            FastDFSFile fastDFSFile = new FastDFSFile();
            //获取文件名称
            String filename = file.getOriginalFilename();
            //文件内容
            byte[] content = file.getBytes();
            //获得文件扩展名
            String ext = filename.substring(filename.lastIndexOf("."));
            fastDFSFile.setContent(content);
            fastDFSFile.setName(filename);
            fastDFSFile.setExt(ext);
            String[] results = FastDFSClient.upload(fastDFSFile);
            //group remoteFileName
            url = FastDFSClient.getTrackerURL()+results[0]+"/"+results[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return url;
    }
}
