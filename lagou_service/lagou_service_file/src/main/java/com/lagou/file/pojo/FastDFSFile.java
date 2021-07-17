package com.lagou.file.pojo;

/**
 * @author lihe
 * @Version 1.0
 */
public class FastDFSFile {
    //文件名称
    private String name;
    //文件内容
    private byte[] content;
    //文件扩展名
    private String ext;
    //作者
    private String author;
    //md5
    private String md5;

    public FastDFSFile() {
    }

    public FastDFSFile(String name, byte[] content, String ext, String author) {
        this.name = name;
        this.content = content;
        this.ext = ext;
        this.author = author;
    }

    public FastDFSFile(String name, byte[] content, String ext) {
        this.name = name;
        this.content = content;
        this.ext = ext;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
