package com.lagou.pojo;

import com.lagou.file.util.FastDFSClient;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import org.junit.Test;

/**
 * @author lihe
 * @Version 1.0
 */
public class FastDFSClientTest {

    @Test
    public void test1() throws  Exception{
        System.out.println(FastDFSClient.getTrackerServer());
        System.out.println(FastDFSClient.getStorageServer("group1"));
    }

}
