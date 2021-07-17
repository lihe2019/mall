package com.lagou.page.listener;

import com.lagou.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author lihe
 * @Version 1.0
 */
@Component
@RabbitListener(queues = "page_create_queue")
public class PageQueueListener {

    @Autowired
    private PageService pageService;

    @RabbitHandler
    public void createPage(String spuId){
        pageService.createHtml(spuId);
    }

}
