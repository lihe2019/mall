package com.lagou.page;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author lihe
 * @Version 1.0
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.lagou.page.feign")
public class PageApplication {

    public static void main(String[] args) {
        SpringApplication.run(PageApplication.class,args);
    }

}
