package com.lagou.seckill.contorller;

import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.seckill.pojo.SeckillGoods;
import com.lagou.seckill.service.SeckillService;
import com.lagou.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 获取秒杀商品详情信息
     * @param time 时间项
     * @param id 秒杀商品id
     * @return
     */
    @GetMapping("/one")
    public Result<SeckillGoods> queryByNamespaceAndKey(String time,Long id){
        SeckillGoods seckillGoods = seckillService.select(time,id);
        return new Result(true,StatusCode.OK,"success",seckillGoods);
    }

    /**
     * 时间菜单
     * @return
     */
    @GetMapping("/timeMenus")
    public Result dateMenus(){
        List<Date> dateList = DateUtil.getDateMenus();
        List<String> dateStringList = new ArrayList<>(dateList.size());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Date date : dateList) {
            String formatDate = simpleDateFormat.format(date);
            dateStringList.add(formatDate);
        }
        return new Result(true, StatusCode.OK,"success",dateStringList);
    }

    /**
     * 通过时间项获得秒杀商品列表
     * @param time  格式:yyyyMMddHH
     * @return
     */
    @GetMapping("/list")
    public Result<List<SeckillGoods>> list(@RequestParam(name = "time") String time){
        List<SeckillGoods> list = seckillService.list(time);
        return new Result<>(true,StatusCode.OK,"success",list);
    }

}
