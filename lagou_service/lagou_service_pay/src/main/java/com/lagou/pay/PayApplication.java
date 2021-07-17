package com.lagou.pay;

import com.lagou.interceptor.FeignInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;

/**
 * @author lihe
 * @Version 1.0
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.lagou.order.feign")
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }

    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
