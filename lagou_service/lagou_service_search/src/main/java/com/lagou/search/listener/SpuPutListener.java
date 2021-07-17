package com.lagou.search.listener;

import com.lagou.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lihe
 * @Version 1.0
 */
@Component
@RabbitListener(queues = "search_add_queue")
public class SpuPutListener {

    @Autowired
    private SearchService searchService;

    @RabbitHandler
    public void addDataToES(String spuId){
        //通过id查询skuList保存到索引库
        searchService.importDataToES(spuId);
    }

}
