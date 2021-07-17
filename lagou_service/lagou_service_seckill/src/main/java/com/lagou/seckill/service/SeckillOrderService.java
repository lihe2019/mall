package com.lagou.seckill.service;

import com.lagou.entity.SeckillStatus;
import com.lagou.seckill.pojo.SeckillGoods;

import java.util.List;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
public interface SeckillOrderService {

    /**
     * 下单
     * @param time
     * @param id
     * @param username
     */
    void add(String time, Long id, String username);

    SeckillStatus queryStatus(String username);


    /**
     * 修改订单状态
     * @param username
     * @param map
     */
    public void updateSeckillOrderStatus(String username, Map<String,String> map);
}
