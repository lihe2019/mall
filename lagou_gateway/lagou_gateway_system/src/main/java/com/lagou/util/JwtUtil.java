package com.lagou.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * @author lihe
 * @Version 1.0
 */
public class JwtUtil {

    //有效期一个月
    public static final Long JWT_TTL = 1000L * 60 * 60 * 24 * 30;
    //密钥明文
    private static final String JWT_KEY = "lagou";

    /**
     * 解析Token
     * 当token不合法时解析会发生异常
     * @param token
     * @return
     */
    public static Claims parseJWT(String token) throws Exception{
        SecretKey secretKey = generalKey();
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    /**
     * 生成密钥密文
     *
     * @return
     */
    public static SecretKey generalKey() {
        byte[] bytes = Base64.getEncoder().encode(JwtUtil.JWT_KEY.getBytes());
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return secretKey;
    }


    public static void main(String[] args) {
        System.out.println(UUID.randomUUID());
        System.out.println(UUID.randomUUID());
        System.out.println(UUID.randomUUID());
        System.out.println(UUID.randomUUID());
    }

}

