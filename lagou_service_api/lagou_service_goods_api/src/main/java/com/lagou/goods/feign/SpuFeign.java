package com.lagou.goods.feign;

import com.lagou.entity.Result;
import com.lagou.goods.pojo.Sku;
import com.lagou.goods.pojo.Spu;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lihe
 * @Version 1.0
 */
@FeignClient(name = "goods")
@RequestMapping("/spu")
public interface SpuFeign {

    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable String id);
}
