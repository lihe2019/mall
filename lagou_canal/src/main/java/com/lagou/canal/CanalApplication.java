package com.lagou.canal;

import com.xpand.starter.canal.annotation.EnableCanalClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author lihe
 * @Version 1.0
 */
@SpringBootApplication
//开启canalClient
@EnableCanalClient
public class CanalApplication {
    public static void main(String[] args) {
        SpringApplication.run(CanalApplication.class,args);
    }
}
