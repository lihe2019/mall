package com.lagou.token;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
public class JwtTokenTest {

    //生成令牌
    @Test
    public void create(){
        //证书路径
        ClassPathResource resource = new ClassPathResource("lagou.jks");
        //创建密钥库工厂对象
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource,"edu.lagou".toCharArray());
        //读取密钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("lagou", "edu.lagou".toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //定义JWT的信息
        Map<String,String> tokenMap = new HashMap<>();
        tokenMap.put("id","9527");
        tokenMap.put("userName","YuanJing");
        //生成令牌
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(tokenMap), new RsaSigner(aPrivate));
        //取出令牌
        String jwtEncoded = jwt.getEncoded();
        //eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijk1MjciLCJ1c2VyTmFtZSI6Ill1YW5KaW5nIn0.JwbYsg5v7EGEYKRqjB_e-Q9ebO1SBcHeb3nsuHbmKQxW2SbxJcZ8IMRfI8l_xN0QaXc_l43We2_uZeE5-2Bi8rxFY5FIb0SdR5ZptW1WJTGAGLbMlcHaId0YDS_G93h9UPn2AGe8wGIU9Vu0CIVniGuLW9yZJ5Qbcyw67tbw9q7LhvZ0ZzbDKtqbq8gDwZeppDabHszbgB0W6O1_DYmmZW_GEAaagGwyFoMl_laFSBWjcC-MK6Dat2rmEe-MaGst4TV26ax32raoKMaFix3-g6GKQIBUljMoDqWRH3zv7l5Jd45VKzTKMEyEoRCrb6wMJXaVO1EnREkU48lv8STTDQ
        System.out.println(jwtEncoded);
    }

    //校验令牌
    @Test
    public void parseJwt(){
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJzY29wZSI6WyJhcHAiXSwibmFtZSI6bnVsbCwiaWQiOm51bGwsImV4cCI6MTYxODUxNzM1NSwiYXV0aG9yaXRpZXMiOlsic2Vja2lsbF9saXN0IiwiZ29vZHNfbGlzdCJdLCJqdGkiOiJkMWM0YjQ5Zi1jNTU1LTQ5NDYtOGYzYi1kODNmYjlmYWY2NzkiLCJjbGllbnRfaWQiOiJsYWdvdSIsInVzZXJuYW1lIjoiemhhbmdzYW4ifQ.CaniA5MhasdtnRsTHj-CmtE6vT1Ka1c5XX2ZEGZzKno4YhlK9Lx82bx_t028uHYN1AF5H9HxSdBt4Gn91zOU3GCWnmSaFwoeoaOKqub8WNvnfbiqF4qkr_4mreMoCfgYrhjLse-SiyQimdPVHz19PMQJR7Oko18vkWO6RjyhieiXJS8QlgXKpfw21eK0uIJqc96gUsiS7BmxvRVp9woaVSOdEi0tvLbWZz0aaaZMcassI9ajEkZuVkgZ4JpFGz06JgTAbl_EnCpxI1e2UDfbyASHhMw2Is5Eug4ok8UYmYHIZ2F1sWjb7h6EN58CYmnG6gzXdY8uU87F1mqv_h-VMA";
        //指定公钥
        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh9Tm8gKUrIlC2fFj8Kcb7SrGU+s4Nc9vlcfVL8DBrrc4JSZLCyJzxGr9I6qwo1eNyFwelRvPmZ58F2vsIbsJJrMQ1Y0e2IzglggAme/Nnf9vIcZaksNURkAo5+4mTFxljNBGZ8hL9tyaJkQSVVc08UWAukWVr3s5WycwMQe2RTbd3Sj1r+RTiKZy3owWqClVgzvka7235EspfFAR/1r5c8Nzoos3NM5lst0QoMaIAcxtnmw5SROrMIvm6tEKqtWTkbXoB/SfPhgO1riYM5WR7OUAy5EKFoArAqoz6fNDwmoc3r0I6fz/Tz+Cus9/jY6YHV2p9fj6z72S6ppD1GcPNwIDAQAB-----END PUBLIC KEY-----";
        //校验
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publicKey));
        //获取原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
    }

    @Test
    public void decodeString() throws UnsupportedEncodingException {
        String string = "dXNlcndlYjpsYWdvdQ==";
        byte[] decode = Base64.getDecoder().decode(string);
        String decodeString = new String(decode,"UTF-8");
        //userweb:lagou
        System.out.println(decodeString);
    }

}
