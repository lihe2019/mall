package com.lagou.oauth.config;

import com.lagou.oauth.util.UserJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义用户认证转换器
 */
@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        LinkedHashMap response = new LinkedHashMap();
        //获取用户名
        String name = authentication.getName();
        response.put("username", name);

        Object principal = authentication.getPrincipal();

        //获取用户自定义JWT对象
        UserJwt userJwt = null;
        if(principal instanceof  UserJwt){
            userJwt = (UserJwt) principal;
        }else{
            //refresh_token默认不去调用userdetailService获取用户信息，这里我们手动去调用，得到 UserJwt
            UserDetails userDetails = userDetailsService.loadUserByUsername(name);
            userJwt = (UserJwt) userDetails;
        }

        //用户名放入响应
        response.put("name", userJwt.getName());
        //用户id放入响应
        response.put("id", userJwt.getId());
        //如果用户权限集合不为空, 则将权限集合放入响应
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

}
