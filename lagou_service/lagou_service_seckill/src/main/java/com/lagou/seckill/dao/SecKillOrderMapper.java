package com.lagou.seckill.dao;

import com.lagou.seckill.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SecKillOrderMapper extends Mapper<SeckillOrder> {

}
