package com.atguigu.gmall.payment.mapper;

import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import tk.mybatis.mapper.common.Mapper;

public interface PmsProductInfoMapper extends Mapper<PmsProductInfo> {

    void insertSelective(PmsProductSaleAttr pmsProductSaleAttr);

    void insertSelective(PmsProductImage pmsProductImage);

    void delete(PmsProductSaleAttr pmsProductSaleAttrDel);

    void delete(PmsProductImage pmsProductImageDel);
}



























