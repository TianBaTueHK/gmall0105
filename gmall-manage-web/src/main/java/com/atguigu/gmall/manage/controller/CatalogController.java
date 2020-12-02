package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.payment.service.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 前端和后端因为来自不同的网域，所以在http的安全协议策略下，不信任，所以后端的数据传输不到前端页面
 *  解决方案：在springmvc的controller层加入一个@CrossOrigin跨域访问的注解
 */
@Controller
@CrossOrigin
public class CatalogController {

    @Reference
    CatalogService catalogService;


    /**
     * 二级分类查询完，马上触发三级分类（post请求）;查询完三级分类，触发（三级分类catalog3Id下）商品平台属性的管理功能(增删改查)
     * @return
     */
    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id){

        List<PmsBaseCatalog3> getCatalog3s = catalogService.getCatalog3(catalog2Id);
        return getCatalog3s;
    }

    /**
     * 一级分类查询完，马上触发二级分类（post请求）
     * @return
     */
    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id){

        List<PmsBaseCatalog2> getCatalog2s = catalogService.getCatalog2(catalog1Id);
        return getCatalog2s;
    }

    /**
     * 商品一级分类的查询，一级分类查询完，马上触发二级分类（post请求），接着三级分类
     * @return
     */
    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<PmsBaseCatalog1> getCatalog1(){

        List<PmsBaseCatalog1> catalog1s = catalogService.getCatalog1();
        return catalog1s;
    }

}














































