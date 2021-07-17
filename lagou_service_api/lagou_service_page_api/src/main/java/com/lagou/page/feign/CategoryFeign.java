package com.lagou.page.feign;

import com.lagou.entity.Result;
import com.lagou.goods.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author lihe
 * @Version 1.0
 */
@FeignClient("goods")
@RequestMapping("/category")
public interface CategoryFeign {
    /**
     * 通过分类ID查询分类对象
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Integer id);
}
