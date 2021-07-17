package com.lagou.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;

/**
 * oauth2 核心配置文件
 */
@Configuration
//开启认证服务
@EnableAuthorizationServer
class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    //数据源，用于从数据库获取数据进行认证操作，测试可以从内存中获取
    @Autowired
    private DataSource dataSource;

    //jwt令牌转换器
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    //SpringSecurity 用户自定义授权认证类
    @Autowired
    private UserDetailsService userDetailsService;

    //授权认证管理器
    @Autowired
    private AuthenticationManager authenticationManager;

    //令牌持久化存储接口
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private CustomUserAuthenticationConverter customUserAuthenticationConverter;

    /***
     * 客户端信息配置
     * 拉勾用户中心
     *      校验:   畅购应用是否在数据库 oauth_client_details 表中有此用户
     *      方式:   数据库(持久化保存用户信息)
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).clients(clientDetails());
    }

    /***
     * 授权服务器端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.accessTokenConverter(jwtAccessTokenConverter)//token生成的方式
                .authenticationManager(authenticationManager) //认证管理器
                .tokenStore(tokenStore)                       //保存令牌
                .userDetailsService(userDetailsService);      //校验 消费者信息 并授权
    }

    /***
     * 授权服务器的安全配置
     * @param oauthServer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.allowFormAuthenticationForClients()
                .passwordEncoder(new BCryptPasswordEncoder())
                .tokenKeyAccess("permitAll()")
                //放开所有校验访问：permitAll()
                .checkTokenAccess("permitAll()");
                //.checkTokenAccess("isAuthenticated()");
    }


    //读取密钥的配置
    @Bean("keyProp")
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }


    @Resource(name = "keyProp")
    private KeyProperties keyProperties;

    //客户端配置
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 令牌保存
     * @return
     */
    @Bean
    @Autowired
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }


    /**
     * JWT令牌转换器
     * 生成使用RSA方式加密的 Jwt令牌
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(CustomUserAuthenticationConverter customUserAuthenticationConverter) {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        //钥匙对
        KeyPair keyPair = new KeyStoreKeyFactory(
                keyProperties.getKeyStore().getLocation(),                          //证书路径
                keyProperties.getKeyStore().getSecret().toCharArray())              //证书秘钥
                .getKeyPair(
                        keyProperties.getKeyStore().getAlias(),                     //证书别名
                        keyProperties.getKeyStore().getPassword().toCharArray());   //证书密码
        converter.setKeyPair(keyPair);

        //配置自定义的CustomUserAuthenticationConverter
        DefaultAccessTokenConverter accessTokenConverter = (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);
        return converter;
    }
}

