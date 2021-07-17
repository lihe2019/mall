package com.lagou.goods.dao;

import com.lagou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SpecMapper extends Mapper<Spec> {


    @Select("SELECT * FROM tb_spec WHERE template_id IN (SELECT template_id FROM tb_category WHERE NAME=#{category}) ORDER BY seq")
    List<Spec> selectByCategoryName(@Param("category") String category);
}
