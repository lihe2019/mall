package com.lagou.goods.controller;
import com.lagou.entity.PageResult;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.goods.pojo.Goods;
import com.lagou.goods.pojo.Spu;
import com.lagou.goods.service.SpuService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/spu")
public class SpuController {


    @Autowired
    private SpuService spuService;

    /**
     * 查询全部数据
     * @return
     */
    @GetMapping
    public Result findAll(){
        List<Spu> spuList = spuService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",spuList) ;
    }

    /**
     * 商品的概念：1SPU + N * SKU
     */
    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable String id){
       Goods goods = spuService.findGoodsById(id);
       return new Result(true,StatusCode.OK,"查询成功",goods.getSpu());
    }

    /**
     * 商品审核
     * @param id
     * @return
     */
    @PutMapping("/audit/{id}")
    public Result audit(@PathVariable String id){
        spuService.audit(id);
        return new Result(true,StatusCode.OK,"审核成功");
    }

    /**
     * 商品上架
     */
    @PutMapping("/put/{id}")
    public Result put(@PathVariable String id){
        spuService.put(id);
        return new Result(true,StatusCode.OK,"上架成功");
    }
    /**
     * 商品下架
     */
    @PutMapping("/pull/{id}")
    public Result pull(@PathVariable String id){
        spuService.pull(id);
        return new Result(true,StatusCode.OK,"下架成功");
    }

    /***
     * 新增数据
     * @param goods
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Goods goods){
        spuService.add(goods);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody Spu spu,@PathVariable String id){
        spu.setId(id);
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除商品
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable String id){
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /**
     * 还原
     * @param id
     * @return
     */
    @PutMapping("/restore/{id}")
    public Result restore(@PathVariable String id){
        spuService.restore(id);
        return new Result(true,StatusCode.OK,"还原成功");
    }

    /***
     * 根据ID物理删除商品
     * @param id
     * @return
     */
    @DeleteMapping(value = "/realDelete/{id}" )
    public Result realDelete(@PathVariable String id){
        spuService.realDelete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }


    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<Spu> list = spuService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<Spu> pageList = spuService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }




}
