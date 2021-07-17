package com.lagou.oauth.config;

import com.lagou.entity.Result;
import com.lagou.oauth.util.UserJwt;
import com.lagou.user.feign.UserFeign;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * 查询用户信息 及权限的 自定义实现类 可连接数据库 查询数据库中的用户信息 及授权信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private UserFeign userFeign;

    /****
     * 自定义授权认证
     * @param username  用户登录时输入的用户名
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //客户端信息-起
        //取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，
        //开始认证client_id和client_secret
        if (authentication == null) {
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                //秘钥
                String clientSecret = clientDetails.getClientSecret();
                //静态方式
                //return new User(username,new BCryptPasswordEncoder().encode(clientSecret), AuthorityUtils.commaSeparatedStringToAuthorityList(""));
                //数据库查找方式, 创建用户对象
                return new User(username, clientSecret, AuthorityUtils.commaSeparatedStringToAuthorityList(""));
            }
        }
        //客户端信息-止

        //如果用户没有输入用户名返回null
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        //从数据库中加载
        Result result = userFeign.findById(username);
        if (result == null || result.getData() == null) {
            return null;
        }
        //获取到Result中的User对象
        //在User对象在http传输过程中LinkedHashMap类型
        ObjectMapper objectMapper = new ObjectMapper();
        com.lagou.user.pojo.User user = objectMapper.convertValue(result.getData(), com.lagou.user.pojo.User.class);
        //根据用户名查询用户信息
        String pwd = user.getPassword();
        //创建权限字符串
        String permissions = "user,vip,admin";
        //创建用户JWT对象
        UserJwt userDetails = new UserJwt(username, pwd, AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));
        //返回用户JWT对象
        return userDetails;
    }
}
