package com.lagou.seckill;

import com.lagou.util.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author lihe
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@MapperScan(basePackages = {"com.lagou.seckill.dao"})
@EnableAsync//开启异步执行
public class SecKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecKillApplication.class,args);
    }

    @Bean
    public IdWorker idWorker(){
        return new IdWorker(1,1);
    }
}
