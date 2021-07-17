package com.lagou;

import com.lagou.util.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.lagou.goods.dao"})
public class GoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run( GoodsApplication.class,args);
    }


    @Value("${workerId}")
    private Integer workerId;
    @Value("${datacenterId}")
    private Integer datacenterId;

    @Bean
    public IdWorker idWorker(){
        return new IdWorker(workerId,datacenterId);
    }
}
