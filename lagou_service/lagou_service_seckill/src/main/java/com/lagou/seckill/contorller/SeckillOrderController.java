package com.lagou.seckill.contorller;

import com.lagou.entity.Result;
import com.lagou.entity.SeckillStatus;
import com.lagou.entity.StatusCode;
import com.lagou.seckill.pojo.SeckillGoods;
import com.lagou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/order")
public class SeckillOrderController {


    @Autowired
    private SeckillOrderService orderService;

    /**
     * 秒杀下单
     *
     * @param time 时间项
     * @param id   秒杀商品id
     * @return
     */
    @GetMapping("/add")
    public Result<SeckillGoods> add(String time, Long id,String username) {
        //String username = "yuanjing";
        orderService.add(time, id, username);
        return new Result(true, StatusCode.OK, "正在排队.....");
    }


    @GetMapping("/query")
    public Result<SeckillStatus> queryStatus() {
        String username = "yuanjing";
        SeckillStatus seckillStatus = orderService.queryStatus(username);
        if (seckillStatus != null) {
            return new Result<>(true, StatusCode.OK, "success", seckillStatus);
        }
        return new Result<>(false, StatusCode.ERROR, "抢单失败");
    }


}
