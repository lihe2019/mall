package com.lagou.goods.service.impl;

import com.lagou.goods.dao.*;
import com.lagou.goods.pojo.*;
import com.lagou.goods.service.SpuService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lagou.util.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     * 需要保存一个SPU和多个SKU
     *
     * @param goods
     */
    @Transactional
    @Override
    public void add(Goods goods) {
        //生成spuId
        long spuId = idWorker.nextId();
        //保存SPU
        goods.getSpu().setId(String.valueOf(spuId));
        //设置商品状态信息
        goods.getSpu().setIsDelete("0");
        goods.getSpu().setStatus("0");
        goods.getSpu().setIsMarketable("0");
        spuMapper.insert(goods.getSpu());
        //保存SKU集合
        insertSkuList(goods);
    }

    /**
     * 保存SKUList集合
     *
     * @param goods
     */
    private void insertSkuList(Goods goods) {
        Spu spu = goods.getSpu();
        //当前日期
        Date date = new Date();
        //获得品牌对象
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        //获得分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        /**
         * 添加分类和品牌之间的关联
         * tb_category_brand
         */
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0) {
            categoryBrandMapper.insert(categoryBrand);
        }

        //获得SkuList
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            for (Sku sku : skuList) {
                //赋值
                sku.setId(String.valueOf(idWorker.nextId()));
                //外键
                sku.setSpuId(spu.getId());
                //设置规格
                if (StringUtils.isEmpty(sku.getSpec())) {
                    sku.setSpec("{}");
                }
                //设置sku名称
                sku.setName(spu.getName());
                //日期
                sku.setCreateTime(date);
                sku.setUpdateTime(date);
                //分类
                sku.setCategoryName(category.getName());
                sku.setBrandName(brand.getName());
                skuMapper.insert(sku);
            }
        }
    }


    /**
     * 修改
     *
     * @param spu
     */
    @Override
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 逻辑删除
     * 修改删除标记的值
     * @param id
     */
    @Override
    public void delete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!spu.getIsMarketable().equals("0")){
            throw new RuntimeException("该商品未下架，不能执行删除操作");
        }
        spu.setIsDelete("1");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Spu>) spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Spu>) spuMapper.selectByExample(example);
    }

    @Override
    public Goods findGoodsById(String id) {
        //查询到SPU对象
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //查询SKU列表
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        //查询
        List<Sku> skuList = skuMapper.selectByExample(example);
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    @Override
    public void audit(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setStatus("1");//修改为已审核
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Autowired
    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //只有status = 1，已审核
        if(!spu.getStatus().equals("1")){
            throw  new RuntimeException("为审核的商品不允许上架");
        }
        spu.setIsMarketable("1");//上架
        spuMapper.updateByPrimaryKeySelective(spu);//将商品上架状态持久化到数据库
        //将上架的商品id发送到MQ
        rabbitMessagingTemplate.convertAndSend("goods_up_exchange","",id);
    }

    @Override
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setIsMarketable("0");//下架
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //判断是否是删除状态
        if(!spu.getIsDelete().equals("1")){
            throw  new RuntimeException("该商品未删除");
        }
        spu.setIsDelete("0");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void realDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!spu.getIsDelete().equals("1")){
            throw new RuntimeException("该商品未执行逻辑删除");
        }
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andEqualTo("isMarketable", searchMap.get("isMarketable"));
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
