package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

public interface PmsSkuSaleAttrValueMapper extends Mapper<PmsSkuSaleAttrValue> {
    int insertSelective(PmsSkuAttrValue pmsSkuAttrValue);
}
