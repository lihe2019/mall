package com.lagou.seckill.service.impl;

import com.lagou.entity.SeckillStatus;
import com.lagou.seckill.dao.SecKillGoodsMapper;
import com.lagou.seckill.dao.SecKillOrderMapper;
import com.lagou.seckill.pojo.SeckillOrder;
import com.lagou.seckill.service.SeckillOrderService;
import com.lagou.seckill.task.MultiThreadCreateOrderTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private static final String SECKILL_KEY = "SeckillGoods_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecKillGoodsMapper goodsMapper;

    @Autowired
    private MultiThreadCreateOrderTask threadCreateOrderTask;

    /**
     * 下单
     *
     * @param time
     * @param id
     * @param username
     */
    @Override
    public void add(String time, Long id, String username) {
        /** 解决重复抢单问题 */
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if (userQueueCount > 1) {
            throw new RuntimeException("重复抢单！！！");
        }
        //1.创建排队对象
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);
        //存入redis中,list排队
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);
        //用户抢单状态-->用于用户查询
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        //2.多线程异步执行创建订单的方法
        threadCreateOrderTask.createOrder();
    }

    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }


    @Autowired
    private SecKillOrderMapper secKillOrderMapper;

    /**
     * 修改订单状态
     *
     * @param username
     * @param map
     */
    @Override
    public void updateSeckillOrderStatus(String username, Map<String, String> map) {
        //1.修改订单状态(秒杀订单现在在redis中),将修改状态后的订单持久到数据库中
        //在此处是不能获取userName的,只能传过来 body(String)-->Json(exchange:exchange,username:username)
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder_").get(username);
        if (seckillOrder != null) {

            //修改状态信息
            seckillOrder.setStatus("1");
            seckillOrder.setTransactionId(map.get("trade_no"));
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                seckillOrder.setPayTime(format.parse(map.get("gmt_payment")));
            } catch (ParseException e) {
                seckillOrder.setPayTime(new Date());
                e.printStackTrace();
            }
            //保存到MySQL中
            secKillOrderMapper.insertSelective(seckillOrder);
            //2.删除redis中的订单信息
            redisTemplate.boundHashOps("SeckillOrder_").delete(username);
            //3.清除用户排队抢单的信息
            //清理掉排队信息
            redisTemplate.boundHashOps("UserQueueCount").delete(username);
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);
        }
    }
}
