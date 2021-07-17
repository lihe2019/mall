package com.lagou.system.service.impl;

import com.lagou.system.dao.AdminMapper;
import com.lagou.system.pojo.Admin;
import com.lagou.system.service.AdminService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lagou.util.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Admin> findAll() {
        return adminMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param id
     * @return
     */
    @Override
    public Admin findById(Integer id) {
        return adminMapper.selectByPrimaryKey(id);
    }


    /**
     * 增加
     *
     * @param admin
     */
    @Override
    public void add(Admin admin) {
        //在插入前将密码加密
        String hashpw = BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt());
        admin.setPassword(hashpw);
        adminMapper.insert(admin);
    }


    /**
     * 修改
     *
     * @param admin
     */
    @Override
    public void update(Admin admin) {
        adminMapper.updateByPrimaryKey(admin);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Integer id) {
        adminMapper.deleteByPrimaryKey(id);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Admin> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return adminMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Admin> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Admin>) adminMapper.selectAll();
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
    public Page<Admin> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Admin>) adminMapper.selectByExample(example);
    }

    @Override
    public boolean login(Admin admin) {
        //根据登录名查询管理员
        Admin adminQuery = new Admin();
        adminQuery.setLoginName(admin.getLoginName());
        adminQuery.setStatus("1");
        //根据用户名和状态查询
        Admin adminResult = adminMapper.selectOne(adminQuery);
        //判断-验证
        if (adminResult == null){
            return false;
        }else {
            //验证密码
           return  BCrypt.checkpw(admin.getPassword(),adminResult.getPassword());
        }
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Admin.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 用户名
            if (searchMap.get("loginName") != null && !"".equals(searchMap.get("loginName"))) {
                criteria.andLike("loginName", "%" + searchMap.get("loginName") + "%");
            }
            // 密码
            if (searchMap.get("password") != null && !"".equals(searchMap.get("password"))) {
                criteria.andLike("password", "%" + searchMap.get("password") + "%");
            }
            // 状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // id
            if (searchMap.get("id") != null) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }

        }
        return example;
    }

}
