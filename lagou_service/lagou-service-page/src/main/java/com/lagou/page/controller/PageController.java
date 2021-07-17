package com.lagou.page.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.page.service.PageService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端传递过来一个spu的id
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired
    private PageService pageService;

    @RequestMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(name = "id") String id){
        pageService.createHtml(id);
        return new Result(true, StatusCode.OK,"success");
    }

}
