package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.manage.util.PmsUploadUtil;
import com.atguigu.gmall.payment.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Spu功能，商品属性Spu管理
 */
@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    /**
     * 根据id查询图片
     * @param spuId
     * @return
     */
    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }

    /**
     * 查询spu销售属性列表
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }


    /**
     * spu的添加功能（PmsProductImage表）
     *  1，spu信息：spu名称，spu的描述
     *  2，spu图片信息
     *          图片的对象数据保存在分布式的文件存储服务器上（fastdfs）
     *          图片的元数据信息保存在数据库中（两种方案，基本上选择b）：
     *                          a：用户在选择完图片后，我们把图片在用户提交时候和其他的商品spu信息一起提交到后台
     *                          b：我们在用户选择图片后，就将图片上传至服务器
     *  3，spu的销售属性信息
     *          商品的平台属性属于电商网站后台管理（整个商品平台的维度下的
     *          商品的销售属性（衣服颜色，尺码）属于在电商网站上卖商品的商家管理（属性某一件商品的维度下的
     *  4，销售属性字典表
     *          商家在添加spu商品信息时，需要添加销售属性（自定义）
     *          在添加spu的页面，商家先选择销售属性（平台后台定义的销售属性字典表），然后自定义当前商品的销售属性值
     */


    /**
     * 图片信息的处理
     *      <form method="post" enctype="multipart/Form-data">
     *          <input type="file"/>
     *      </form>
     * 用户点击上传图片后，后台将图片存储到服务器上，然后返回图片的访问路径给前端
     * @param multipartFile
     * @return
     */
    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        //将图片或者视频上传到分布式的文件存储系统（fastdfs）
        //将图片的存储路径返回给页面
        String imgUrl = PmsUploadUtil.uploadImage(multipartFile);//"https://m.360buyimg.com/babel/jfs/t5134/42525ef33.jpg";
        return imgUrl;
    }

    /**
     * 添加完成，点击保存，触发保存saveSpuInfo方法，保存SpuInfo的属性(销售属性，图片地址列表)
     * @param pmsProductInfo
     * @return
     */
    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        //success ：保存返回值，是否保存或修改成功
        String success = spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }

    /**
     *三级分类下，根据catalog3Id查询商品属性的详细信息(序号，商品id，商品名称，商品描述)
     * 数据列表查询
     * @param catalog3Id
     * @return
     */
    @RequestMapping("spuList") //spuList?catalog3Id=
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> productInfos = spuService.spuList(catalog3Id);
        return productInfos;
    }

}












