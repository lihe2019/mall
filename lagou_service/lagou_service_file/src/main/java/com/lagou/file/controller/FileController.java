package com.lagou.file.controller;

import com.lagou.file.pojo.FastDFSFile;
import com.lagou.file.service.FileService;
import com.lagou.file.util.FastDFSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 文件上传
     *
     * @param file
     * @return 可以访问的URL地址
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        return fileService.upload(file);
    }
}
