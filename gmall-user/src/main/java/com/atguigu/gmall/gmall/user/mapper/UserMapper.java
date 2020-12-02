package com.atguigu.gmall.gmall.user.mapper;

import com.atguigu.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * dao层
 */

/**
 * Mapper:通用mapper,mapper中定义了单表的增删改查
 */
public interface UserMapper extends Mapper<UmsMember> {

    List<UmsMember> selectAllUser();


}
























