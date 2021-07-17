package com.lagou.user.dao;

import com.lagou.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {
    /**
     * 根据用户名修改积分
     * @param username
     * @param points
     */
    @Update("update tb_user set points=points+#{points} where username=#{username}")
    int addPoints(@Param("username") String username, @Param("points") Integer points);
}
