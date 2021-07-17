package com.lagou.goods.dao;

import com.lagou.goods.pojo.Sku;
import com.lagou.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {
    /**
     * 库存-num,销量+num
     * @param orderItem
     * @return
     */
    @Update("update tb_sku set num=num-#{num},sale_num=sale_num+#{num} where id=#{skuId} and num>=#{num}")
    int changeCount(OrderItem orderItem);
}
