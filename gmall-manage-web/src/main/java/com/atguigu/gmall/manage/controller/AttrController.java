package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.payment.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**attr ：属性值，属性名称
 *
 * 触发（三级分类catalog3Id下）商品平台属性的管理功能(属性的增删改查)
 */
@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

    /**
     * 查询出销售属性名称（衣服颜色，尺码，版本.....）
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> pmsBaseSaleAttrs = attrService.baseSaleAttrList();
        return pmsBaseSaleAttrs;
    }

    /**
     * 选择要删除的属性
     *      删除成功后，返回三级分类的属性下attrInfoList?catalog3Id=61，刷新页面，显示出来
     * @param id
     * @return
     */
    @RequestMapping("deleteAttrInfo")
    @ResponseBody
    public String deleteAttrInfo(@RequestBody String id){

        String success = attrService.deleteAttrInfo(id);

        return success;
    }

    /**
     * 查询出属性值后，添加要保存的属性，或者根据id修改属性和属性值
     *      保存成功后，返回三级分类的属性下attrInfoList?catalog3Id=61，刷新页面，显示出来
     * @param pmsBaseAttrInfo
     * @return
     */
    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){

        //success ：保存返回值，是否保存或修改成功
        String success = attrService.saveAttrInfo(pmsBaseAttrInfo);
        return success;
    }

    /**
     * 根据三级查询下的 catalog3Id 查询平台属性和平台属性值（平台属性 + 平台属性值双层集合）
     * @param catalog3Id
     * @return
     */
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){

        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.attrInfoList(catalog3Id);
        return pmsBaseAttrInfos;
    }

    /**
     * 根据id 为修改页面查询一个属性值得集合，修改属性值后，点击保存，触发保存的方法saveAttrInfo（有id就修改，没有id就增加）
     * @param attrId
     * @return
     */
    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){

        List<PmsBaseAttrValue> pmsBaseAttrValues = attrService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }
}






























