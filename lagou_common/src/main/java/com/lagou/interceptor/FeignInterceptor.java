package com.lagou.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

/**
 * @author lihe
 * @Version 1.0
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        //将用户请求对象中所有的请求头放入RequestTemplate的请求头中
        //1.获取到用户请求中所有的请求头
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return;
        }
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();
        //2.放入
        while (headerNames.hasMoreElements()) {
            //key
            String key = headerNames.nextElement();
            String value = requestAttributes.getRequest().getHeader(key);
            template.header(key, value);
        }
    }
}
