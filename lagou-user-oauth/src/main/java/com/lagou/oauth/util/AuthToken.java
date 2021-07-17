package com.lagou.oauth.util;

import java.io.Serializable;

/**
 * 用户令牌封装
 **/
public class AuthToken implements Serializable{

    //令牌信息
    String accessToken;

    //刷新token(refresh_token)
    String refreshToken;

    //jwt短令牌
    String jti;


    public AuthToken() {
    }

    public AuthToken(String accessToken, String refreshToken, String jti) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.jti = jti;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }
}