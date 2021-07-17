package com.lagou.seckill.service;

import com.lagou.seckill.pojo.SeckillGoods;

import java.util.List;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
public interface SeckillService {
    /**
     * 通过时间项获取秒杀商品列表
     * @param time
     * @return
     */
    List<SeckillGoods> list(String time);

    /**
     * 获取商品详情信息
     * @param time
     * @param id
     * @return
     */
    SeckillGoods select(String time, Long id);
}
