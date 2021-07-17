package com.lagou.oauth.service;

import com.lagou.oauth.util.AuthToken;

/**
 * @author lihe
 * @Version 1.0
 */
public interface AuthService {
    /**
     * 用户认证的方法
     * @param name
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    AuthToken login(String name, String password, String clientId, String clientSecret);
}
