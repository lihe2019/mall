package com.lagou.order.listener;

import com.alibaba.fastjson.JSON;
import com.lagou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
@Component
@RabbitListener(queues = "order_queue")
public class OrderPayListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void receivePayStatus(String message){
        System.out.println("=======================监听order_queue=======================");
        //将message转换为Map
        Map<String,String> map = JSON.parseObject(message, Map.class);
        //变更订单状态&记录订单变动日志
        orderService.changeOrderStatusAndOrderLog(map);
    }

}
