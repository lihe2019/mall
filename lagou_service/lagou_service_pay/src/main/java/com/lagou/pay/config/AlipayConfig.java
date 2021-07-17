package com.lagou.pay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author lihe
 * @Version 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@PropertySource("classpath:alipay.properties")
@Data
public class AlipayConfig {

    private String app_id;
    private String app_private_key;
    private String url;
    private String charset;
    private String format;
    private String alipay_public_key;
    private String log_path;
    private String signtype;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(url, app_id, app_private_key, format, charset, alipay_public_key, signtype);
    }

}

