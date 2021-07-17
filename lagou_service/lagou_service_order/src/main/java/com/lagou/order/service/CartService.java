package com.lagou.order.service;

import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
public interface CartService {
    void add(String id, Integer num, String userName);

    Map list(String userName);

    void delete(String skuId, String userName);

    void updateCheckedStatus(String skuId, Boolean checked, String userName);
}
