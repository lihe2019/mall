package com.lagou.seckill.service.impl;

import com.lagou.seckill.pojo.SeckillGoods;
import com.lagou.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lihe
 * @Version 1.0
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SECKILL_KEY = "SeckillGoods_";

    /**
     * 通过时间项获取秒杀商品列表
     *
     * @param time
     * @return
     */
    @Override
    public List<SeckillGoods> list(String time) {
        return redisTemplate.boundHashOps(SECKILL_KEY+time).values();
    }

    /**
     * 获取商品详情信息
     *
     * @param time
     * @param id
     * @return
     */
    @Override
    public SeckillGoods select(String time, Long id) {
        return (SeckillGoods)redisTemplate.boundHashOps(SECKILL_KEY+time).get(id);
    }
}
