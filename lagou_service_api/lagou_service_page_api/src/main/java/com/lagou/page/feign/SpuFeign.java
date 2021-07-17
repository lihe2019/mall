package com.lagou.page.feign;

import com.lagou.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lihe
 * @Version 1.0
 */
@FeignClient("goods")
@RequestMapping("/spu")
public interface SpuFeign {
    /**
     * 通过商品id查询商品对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id);

}
