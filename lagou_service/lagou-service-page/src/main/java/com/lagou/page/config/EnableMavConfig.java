package com.lagou.page.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lihe
 * @Version 1.0
 */
@ControllerAdvice
@Configuration
public class EnableMavConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/items/**").addResourceLocations("classpath:/templates/items/");
    }
}
