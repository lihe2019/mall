package com.lagou.page.feign;

import com.lagou.goods.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author lihe
 * @Version 1.0
 */
@FeignClient("goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 通过商品id返回库存列表
     */
    @GetMapping("/findListBySkuId/{spuId}")
    public List<Sku> findListBySpuId(@PathVariable String spuId);

}
