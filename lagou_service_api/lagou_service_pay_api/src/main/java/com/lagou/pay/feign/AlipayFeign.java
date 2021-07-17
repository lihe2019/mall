package com.lagou.pay.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author lihe
 * @Version 1.0
 */
@FeignClient(name = "pay")
@RequestMapping("/alipay")
public interface AlipayFeign {

    @GetMapping("/queryStatus")
    public String query(@RequestParam String out_trade_no) throws Exception;

    @RequestMapping("/close")
    public Result close(@RequestParam String orderId) throws Exception;


}
