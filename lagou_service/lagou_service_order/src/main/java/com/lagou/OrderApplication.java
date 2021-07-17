package com.lagou;

import com.lagou.interceptor.FeignInterceptor;
import com.lagou.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.lagou.goods.feign", "com.lagou.user.feign", "com.lagou.pay.feign"})
@MapperScan(basePackages = {"com.lagou.order.dao"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }


    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
