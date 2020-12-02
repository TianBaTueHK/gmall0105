package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {

    void insertSelective(PmsBaseAttrValue pmsBaseAttrValue);

    List<PmsBaseAttrInfo> selectAttrValueListByValueId(String valueIdStr);
}
























