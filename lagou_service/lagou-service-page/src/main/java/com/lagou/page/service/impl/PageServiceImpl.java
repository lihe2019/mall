package com.lagou.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.Result;
import com.lagou.goods.pojo.Category;
import com.lagou.goods.pojo.Sku;
import com.lagou.goods.pojo.Spu;
import com.lagou.page.feign.CategoryFeign;
import com.lagou.page.feign.SkuFeign;
import com.lagou.page.feign.SpuFeign;
import com.lagou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private SpuFeign spuFeign;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagepath}")
    private String pagepath;

    @Override
    public void createHtml(String spuId) {
        //存储所有生成静态页所需要的的数据
        Map<String,Object> resultMap = new HashMap<>();
        //一、调用商品微服务加载Spu对象、分类、Sku列表
        loadGoodsInfo(spuId, resultMap);
        //二、在指定的位置通过模板引擎生成静态页
        createStaticPage(spuId, resultMap);
    }

    private void createStaticPage(String spuId, Map<String, Object> resultMap) {
        //1.将数据放入上下文中
        Context context = new Context();
        //在模板中可以通过map的key获取value的值
        context.setVariables(resultMap);
        //2.设置输出目录
        File file = new File(pagepath);
        if(!file.exists()){
            file.mkdir();
        }
        //3.通过引擎对象生成静态页
        Writer writer = null;
        try {
            writer = new PrintWriter(file+"/"+spuId+".html");
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadGoodsInfo(String spuId, Map<String, Object> resultMap) {
        //1.加载spu对象
        Result result = spuFeign.findById(spuId);
        Spu spu = JSON.parseObject(JSON.toJSONString(result.getData()), Spu.class);
        resultMap.put("spu",spu);
        //List<String>
        // [
        //  {"color":"银白色","url":"//img14.360buyimg.com/n8/jfs/t20653/324/331963566/193551/a7b4378b/5b0a28b7N7549821d.jpg"}
        //  {"color":"珍珠白","url":"//img14.360buyimg.com/n8/jfs/t20653/324/331963566/193551/a7b4378b/5b0a28b7N7549821e.jpg"}
        // ]
        String images = spu.getImages();
        List<Map> maps = JSON.parseArray(images, Map.class);
        //2.图片列表
        List<String> imageList = new ArrayList<>();
        if (maps != null && maps.size() > 0) {
            for(Map map : maps){
                imageList.add(String.valueOf(map.get("url")));
            }
            resultMap.put("imageList",imageList);
        }
        //3.获得分类
        resultMap.put("category1", categoryFeign.findById(spu.getCategory1Id()).getData());
        resultMap.put("category2",categoryFeign.findById(spu.getCategory2Id()).getData());
        resultMap.put("category3",categoryFeign.findById(spu.getCategory3Id()).getData());
        //4.根据spu的id获得sku集合
        List<Sku> skuList = skuFeign.findListBySpuId(spuId);
        resultMap.put("skuList",skuList);
        //5.规格处理
        // {  "颜色":["紫色"],   "尺码":["250度","200度","100度","150度","300度"]  }
        String specItems = spu.getSpecItems();
        Map mapSpec = JSON.parseObject(specItems, Map.class);
        resultMap.put("specificationList",mapSpec);
    }
}








