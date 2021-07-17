package com.lagou.oauth.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @author lihe
 */
public class URLParamEncoder {
    //将原始的没有经过编码的url地址编码后返回

    //而且 只编码 参数的值的
    //例如 http://www.foo.com?key1=小户&key2=YuanJing
    //编码完的结果变成了http://www.foo.com?key1=%E5%B0%8F%E6%88%B7&key2=%E8%B5%B5%E5%81%A5
    //也就是只是将参数的中文值部分给编码了
    public static String encoding(String source){
        assert source!=null;
        //先以?切分来 注意? 必须加转义字符 因为split方法要求传入的正则语法  而在正则表达式中?是有特殊用途的 所以这里要转义符
        String[] split = source.split("\\?");
        //第一部分是原来的 路径部分
        String path=split[0];
        //第二部分是参数部分
        String paramsOld = split[1];
        StringBuffer paramsNew=new StringBuffer();
        //开始处理参数部分  以&切分开来 遍历
        //结果:[key1=小户,key2=lihe]
        String[] paramArr = paramsOld.split("&");
        for (String s : paramArr) {
            //切出来的部分 一个键值对参数
            //再以=号切分 比如遍历第一个 key1=小户
            //结果[key1,小户]
            String[] kv = s.split("=");
            //开始重新拼接字符串
            //s="key1"
            paramsNew.append(kv[0]);
            //s="key1="
            paramsNew.append("=");
            //进行编码
            try {
                //s="key1=%E5%B0%8F%E6%88%B7"
                paramsNew.append(URLEncoder.encode(kv[1], "utf-8"));
            } catch (UnsupportedEncodingException e) {
                //ignore
            }

            //最后拼接一个 &
            //s="key1=%E5%B0%8F%E6%88%B7&"
            paramsNew.append("&");
        }
        //将最后一个&去掉
        String s = paramsNew.substring(0, paramsNew.length() - 1);
        //最后将路径和 参数重新拼接好 中间是?号
        return path+"?"+s;
    }

    public static void main(String[] args) {
        String s="http://www.foo.com?key1=小户&key2=lihe";
        String encoding = encoding(s);
        System.out.println(encoding);


    }
}
