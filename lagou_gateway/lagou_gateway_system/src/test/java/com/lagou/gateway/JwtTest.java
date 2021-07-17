package com.lagou.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;

/**
 * @author lihe
 * @Version 1.0
 */
public class JwtTest {

    //创建简单的JWT
    @Test
    public void test1() {
        //构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop").setIssuedAt(new Date()).signWith(SignatureAlgorithm.HS256, "lagou");
        //创建
        String token = jwtBuilder.compact();
        //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTI3Iiwic3ViIjoibGFnb3Vfc2hvcCIsImlhdCI6MTYxNjU3MDkxMH0.esiJA-5PzPTQ3MKJaL99ySAnVaPs82cbKwLuJ-8Bkxg
        //eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTI3Iiwic3ViIjoibGFnb3Vfc2hvcCIsImlhdCI6MTYxNjU3MDk2N30.268xSXWSYwwYA8tM-LDbydnWRR--MhKVH39ZIAOReto
        System.out.println(token);
    }

    //验证令牌
    @Test
    public void test2() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI5NTI3Iiwic3ViIjoibGFnb3Vfc2hvcCIsImlhdCI6MTYxNjU3NTMyOSwiZXhwIjoxNjE2NTc1MzI5fQ.ht309uTeEegxgV4iBK-8ipFI1ojwtiDeMVeDsHlYfAQ";
        Claims claims = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(claims);
    }

    //创建简单的JWT
    @Test
    public void test3() {
        //指定令牌的有效期1个月
        long time = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
        Date expirationDate = new Date(time);
        //构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop").setIssuedAt(new Date()).setExpiration(expirationDate).signWith(SignatureAlgorithm.HS256, "lagou");
        //创建
        String token = jwtBuilder.compact();
        System.out.println(token);
    }

    @Test
    public void test4() {
        //指定令牌的有效期1个月
        long time = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
        Date expirationDate = new Date(time);
        //构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop").setIssuedAt(new Date()).setExpiration(new Date()).signWith(SignatureAlgorithm.HS256, "lagou");
        //创建
        String token = jwtBuilder.compact();
        System.out.println(token);
    }

    //自定义载荷
    @Test
    public void test5() {
        //指定令牌的有效期1个月
        long time = System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30;
        Date expirationDate = new Date(time);
        //构建一个jwtBuilder对象
        JwtBuilder jwtBuilder = Jwts.builder().setId("9527").setSubject("lagou_shop").setIssuedAt(new Date())
                .setExpiration(expirationDate).claim("role","admin")
                .signWith(SignatureAlgorithm.HS256, "lagou");
        //创建
        String token = jwtBuilder.compact();
        System.out.println(token);
        System.out.println("---------------------------");
        Claims claims = Jwts.parser().setSigningKey("lagou").parseClaimsJws(token).getBody();
        System.out.println(claims);
    }

}
