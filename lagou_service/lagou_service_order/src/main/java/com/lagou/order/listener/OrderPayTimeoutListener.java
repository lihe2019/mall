package com.lagou.order.listener;

import com.lagou.entity.Result;
import com.lagou.order.service.OrderService;
import com.lagou.pay.feign.AlipayFeign;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听ordertimeout_queue
 *
 * @author lihe
 * @Version 1.0
 */
@Component
@RabbitListener(queues = "ordertimeout_queue")
public class OrderPayTimeoutListener {

    @Autowired
    private AlipayFeign alipayFeign;

    @Autowired
    private OrderService orderService;

    /**
     * 1.不扫码,交易没有在支付宝服务器创建
     * 2.扫码不支付,交易已经创建,执行关闭
     *
     * @param orderId
     * @throws Exception
     */
    @RabbitHandler
    public void orderTimeoutHandler(String orderId) throws Exception {
        //1.去支付宝服务器查询该订单的支付状态，只有处于未支付状态（WAIT_BUYER_PAY）才关闭交易
        String tradeStatus = alipayFeign.query(orderId);
        //如果交易已经关闭 || 交易支付成功 || 交易结束，不可退款，那么无需处理
        if ("TRADE_CLOSED".equals(tradeStatus)
                || "TRADE_SUCCESS".equals(tradeStatus)
                || "TRADE_FINISHED".equals(tradeStatus)) {
            return;
        }
        //已经扫码了，但没有支付,在支付宝服务器交易已经创建创建了
        if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
            //2.在支付宝服务器关闭该交易
            Result result = alipayFeign.close(orderId);
        }
        //3.本地关闭订单&记录订单日志&回滚库存&回滚销量
        orderService.close(orderId);
    }

}
