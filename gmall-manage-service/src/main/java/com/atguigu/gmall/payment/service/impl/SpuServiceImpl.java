package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsProductSaleAttrValue;
import com.atguigu.gmall.payment.mapper.PmsProductImageMapper;
import com.atguigu.gmall.payment.mapper.PmsProductInfoMapper;
import com.atguigu.gmall.payment.mapper.PmsProductSaleAttrMapper;
import com.atguigu.gmall.payment.mapper.PmsProductSaleAttrValueMapper;
import com.atguigu.gmall.payment.service.SpuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;


    /**
     * 三级分类下，根据catalog3Id查询商品属性的详细信息(序号，商品id，商品名称，商品描述)
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {

        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    /**
     * 添加完成，点击保存，触发保存saveSpuInfo方法，保存SpuInfo的属性(销售属性，图片地址列表)
     * @param pmsProductInfo
     * @return
     */
    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {

        String id = pmsProductInfo.getId();

        if (StringUtils.isBlank(id)){ //如果id为空

            //添加保存操作
            //保存属性, insertSelective:将值插入数据库，为null的不插；insert：全部都插入数据值，为null值也插入
            pmsProductInfoMapper.insertSelective(pmsProductInfo);

            //保存插入的属性值
            List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductInfo.getPmsProductSaleAttrList();
            for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrList){
                pmsProductInfo.setPmsId(pmsProductInfo.getId());

                pmsProductInfoMapper.insertSelective(pmsProductSaleAttr);
            }
            //保存插入的图片
            List<PmsProductImage> pmsProductImageList = pmsProductInfo.getPmsProductImageList();
            for (PmsProductImage pmsProductImage : pmsProductImageList){
                pmsProductInfo.setPmsId(pmsProductInfo.getId());

                pmsProductInfoMapper.insertSelective(pmsProductImage);
            }

        }else {

            //id不为空，修改操作

            //修改属性
            Example example = new Example(PmsProductInfo.class);
            example.createCriteria().andEqualTo("id",pmsProductInfo.getId());
            pmsProductInfoMapper.updateByExample(pmsProductInfo,example);

            //属性值修改
            //按照属性id删除所有属性值
            PmsProductSaleAttr pmsProductSaleAttrDel = new PmsProductSaleAttr();
            //设置属性值id
            pmsProductSaleAttrDel.setSaleAttrId(pmsProductInfo.getId());
            //根据id删除
            pmsProductInfoMapper.delete(pmsProductSaleAttrDel);

            //按照属性id删除图片
            PmsProductImage pmsProductImageDel = new PmsProductImage();
            pmsProductImageDel.setId(pmsProductInfo.getId());
            pmsProductInfoMapper.delete(pmsProductImageDel);

            //删除后，将新的属性值插入
            //保存插入的属性值
            List<PmsProductSaleAttr> pmsProductSaleAttrList = pmsProductInfo.getPmsProductSaleAttrList();
            for (PmsProductSaleAttr pmsProductSaleAttr : pmsProductSaleAttrList){
                pmsProductInfo.setPmsId(pmsProductInfo.getId());

                pmsProductInfoMapper.insertSelective(pmsProductSaleAttr);
            }
            //保存插入的图片
            List<PmsProductImage> pmsProductImageList = pmsProductInfo.getPmsProductImageList();
            for (PmsProductImage pmsProductImage : pmsProductImageList){
                pmsProductInfo.setPmsId(pmsProductInfo.getId());

                pmsProductInfoMapper.insertSelective(pmsProductImage);
            }

//=====================-------------------------------------------------
            //保存商品信息
            pmsProductInfoMapper.insertSelective(pmsProductInfo);
            //生成商品主键
            String productId = pmsProductInfo.getId();
            //保存商品图片信息
            List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
            for (PmsProductImage pmsProductImage : spuImageList) {
                pmsProductImage.setProduceId(productId);
                pmsProductImageMapper.insertSelective(pmsProductImage);
            }
            //保存销售属性的信息
            List<PmsProductSaleAttr> spuSaleAttList = pmsProductInfo.getSpuSaleAttrList();
            for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttList) {
                pmsProductSaleAttr.setProductId(productId);
                pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            }
            //保存销售属性值
            PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
            List<PmsProductSaleAttr> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttr pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(productId);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }

        }

        return "success";
    }

    /**
     * 查询spu销售属性列表
     * @param spuId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        //查询spu销售属性
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);

        //根据spu销售属性，查询spu销售属性值
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {

            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            //得到要查询spu销售属性值得到id
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId()); //不是getId（销售属性是电商平台定义的，不平台用户定义的）销售属性id用的是系统的字典表中id，不是销售属性表的主键

            //根据得到的id查询属性值
            List<PmsProductSaleAttr> productSaleAttrValues = pmsProductSaleAttrMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(productSaleAttrValues);
        }

        return pmsProductSaleAttrs;
    }

    /**
     * 根据id查询图片
     * @param spuId
     * @return
     */
    @Override
    public List<PmsProductImage> spuImageList(String spuId) {

        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;
    }


    /**
     * 根据id查询sku商品对象;商品图片集合;sku对象，销售属性列表，控制层返回给前端页面
     * @param productId
     * @param skuId
     * @return
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {

//        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
//        pmsProductSaleAttr.setProductId(productId);
//        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
//
//        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
//            String saleAttrId = productSaleAttr.getSaleAttrId();
//
//            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
//            pmsProductSaleAttrValue.setSaleAttrId(saleAttrId);
//            pmsProductSaleAttrValue.setProductId(productId);
//            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);

//            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);

            List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(productId,skuId);


        return pmsProductSaleAttrs;
    }

}






















