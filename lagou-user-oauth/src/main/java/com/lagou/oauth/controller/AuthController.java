package com.lagou.oauth.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.oauth.service.AuthService;
import com.lagou.oauth.util.AuthToken;
import com.lagou.oauth.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/user")
public class AuthController {

    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Autowired
    private AuthService authService;

    /**
     * 用户登录/认证 客户端ID、客户端密钥
     *
     * @param name
     * @param password
     * @return 成功/失败,将令牌信息写入Cookie中
     */
    @PostMapping("/login")
    public Result login(String name, String password) {
        AuthToken authToken = authService.login(name, password, clientId, clientSecret);
        if (authToken != null) {
            String token = authToken.getAccessToken();
            writeCookie(token);
            return new Result(true, StatusCode.OK, "登录成功", authToken.getAccessToken());
        } else {
            return new Result(false, StatusCode.ERROR, "登录失败");
        }
    }

    private void writeCookie(String token){
        HttpServletResponse response =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","Authorization",token,cookieMaxAge,false);
    }

}
