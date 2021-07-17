package com.lagou.order.controller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.order.service.CartService;
import com.lagou.order.util.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    //private String userName = "lagou_user";

    @GetMapping("/add")
    public Result add(String id,Integer num){
        String userName = TokenDecode.getUserInfo().get("username");
        cartService.add(id,num,userName);
        return new Result(true, StatusCode.OK,"添加成功");
    }

    @GetMapping("/list")
    public Map list(){
        String userName = TokenDecode.getUserInfo().get("username");
        return cartService.list(userName);
    }

    @DeleteMapping
    public Result delete(@RequestParam(name="skuId") String skuId){
        String userName = TokenDecode.getUserInfo().get("username");
        cartService.delete(skuId,userName);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    @PutMapping
    public Result updateChecked(@RequestParam(name = "skuId")String skuId,@RequestParam(name = "checked")Boolean checked){
        String userName = TokenDecode.getUserInfo().get("username");
        cartService.updateCheckedStatus(skuId,checked,userName);
        return new Result(true,StatusCode.OK,"操作成功");
    }

}
