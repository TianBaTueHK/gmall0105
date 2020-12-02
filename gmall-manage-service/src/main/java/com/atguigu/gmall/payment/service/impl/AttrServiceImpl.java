package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.payment.mapper.PmsBaseAttrInfoMapper;
import com.atguigu.gmall.payment.mapper.PmsBaseAttrValueMapper;
import com.atguigu.gmall.payment.mapper.PmsBaseSaleAttrMapper;
import com.atguigu.gmall.payment.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;


    /**
     * 根据三级查询下的 catalog3Id 查询平台属性和平台属性值（平台属性 + 平台属性值双层集合）
     * @param catalog3Id
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {

        //查询平台属性（电脑）
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        //查询平台属性值（电脑下的属性值，颜色；内存；版本......）
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {

            List<PmsBaseAttrValue> pmsBaseAttrValues = new ArrayList<>();

            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId()); //有了平台属性id，去平台属性值表查询属性值
            pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);

        }
        return pmsBaseAttrInfos;
    }


    /**
     *保存和修改操作
     *
     * 修改操作：
     *          1，根据属性id判断，有id是修改操作，没有id是添加操作
     *          2，修改操作
     *             a，先修改属性
     *             b，修改属性值
     *
     * @param pmsBaseAttrInfo
     * @return
     */
    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();
        if (StringUtils.isBlank(id)){ //StringUtils.isBlank(id):是否为空null
            //id为空，保存操作

            //保存属性, insertSelective:将值插入数据库，为null的不插；insert：全部都插入数据值，为null值也插入
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            //保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrInfo.setAttrId(pmsBaseAttrInfo.getId());

                pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrValue);
            }
        }else {
            //id不为空，修改

            //属性修改
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());//修改条件表达式
            pmsBaseAttrInfoMapper.updateByExample(pmsBaseAttrInfo,example); //pmsBaseAttrInfo:原始数据；example：修改的数据

            //属性值修改
            //按照属性id删除所有属性值
            PmsBaseAttrValue pmsBaseAttrValueDel = new PmsBaseAttrValue();
            //设置一个属性值id
            pmsBaseAttrValueDel.setAttrId(pmsBaseAttrInfo.getId());
            //删除
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValueDel);


            //删除后，将新的属性值插入
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }

        return "success";
    }


    /**
     * 删除属性
     * @param id
     * @return
     */
    @Override
    public String deleteAttrInfo(String id) {

        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.getCatalog3Id(id);

        pmsBaseAttrInfoMapper.delete(pmsBaseAttrInfo);
        return "s";
    }

    /**
     * 查询出属性值，修改属性值后，点击保存，触发保存的方法saveAttrInfo
     * @param attrId
     * @return
     */
    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;
    }


    /**
     * 查询出销售属性名称（衣服颜色，尺码，版本.....）
     * @return
     */
    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {

        return pmsBaseSaleAttrMapper.selectAll();
    }

    /**
     * 根据valueId将属性列表的值查询出来 （前端要的属性值在pmsBaseAttrInfo，pmsBaseAttrValue表中）
     * @param valueIdSet
     * @return
     */
    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> valueIdSet) {

        String valueIdStr = StringUtils.join(valueIdSet, ","); //id
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.selectAttrValueListByValueId(valueIdStr);

        return pmsBaseAttrInfos;

    }


}


































