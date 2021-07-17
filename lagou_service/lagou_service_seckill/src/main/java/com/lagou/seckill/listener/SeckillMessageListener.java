package com.lagou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.lagou.seckill.dao.SecKillOrderMapper;
import com.lagou.seckill.pojo.SeckillOrder;
import com.lagou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 监听seckill_queue
 *
 * @author lihe
 * @Version 1.0
 */
@Component
@RabbitListener(queues = "seckill_queue")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 当秒杀订单支付成功之后
     *
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message) {
        System.out.println("监听秒杀队列的消息:" + message);
        String username = "yuanjing";
        //将message转换为Map
        Map<String,String> map = JSON.parseObject(message, Map.class);
        seckillOrderService.updateSeckillOrderStatus(username,map);
    }

}









