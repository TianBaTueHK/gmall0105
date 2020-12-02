package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.payment.service.AttrService;
import com.atguigu.gmall.payment.service.SearchService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 搜索框的controller
 */
@Controller
public class SearchController {

    @Reference
    SearchService searchService;
    @Reference
    AttrService attrService; //属性的服务

    /**
     * 通过三级分类id，关键字，属性 进行查询
     *
     * @param pmsSearchParm
     * @return
     */
    @RequestMapping("list.html")
    public String list(PmsSearchParm pmsSearchParm, ModelMap modelMap) { //三级分类id，关键字，属性筛选

        //调用搜索服务，，返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfos = searchService.list(pmsSearchParm);
        modelMap.put("skuInfoList", pmsSearchSkuInfos);


        //抽取检索结果锁包含的平台属性集合
        Set<String> valueIdSet = new HashSet<>();
        /**
         * 使用java代码抽取平台属性(将pmsSearchSkuInfos表中包含的平台属性值集合 去重之后取出)
         *      使用了list，set集合的功能，不可重复
         *      先取id，再通过id进行查询
         */
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                //获取不重复的id，取id
                String valueId = pmsSkuAttrValue.getValueId();
                //获取平台属性的id
                valueIdSet.add(valueId);
            }
        }
        //根据valueId将属性列表的值查询出来 （前端要的属性值在pmsBaseAttrInfo，pmsBaseAttrValue表中）
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrService.getAttrValueListByValueId(valueIdSet);
        modelMap.put("attrList", pmsBaseAttrInfos); //页面上的名称是attrList

//        ----------------------------------------------------------------------------------------------
        /**
         * 当点击了一个属性后，点击的这个属性的这一行会消失
         * 对平台属性集合进一步处理，去掉当前条件中valueId所在的属性组
         * 解决：在集合中判断(集合删除太慢)，迭代器
         */
        String[] delValueIds = pmsSearchParm.getValueId();

//      if (delValueIds != null){
//        for (PmsBaseAttrInfo pmsBaseAttrInfo : pmsBaseAttrInfos) {
//            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
//            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                String valueId = pmsBaseAttrValue.getId();
//                for (String delValueId : delValueIds) {
//                    if (valueId.equals(delValueId)){
//                        //删除当前valueId所在的属性值 （数组删除效率极低，删除一个，数组索引会变更，）
//                    }
//                }
//            }
//        }
//      }

        /**
         * 单独删除的操作
         */
        //迭代器 适合做检查（警察）式的删除
//        if (delValueIds != null){
//            Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
//            while (iterator.hasNext()){
//                PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
//                List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
//
//                for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                    String valueId = pmsBaseAttrValue.getId();
//
//                    for (String delValueId : delValueIds) {
//                        if (delValueId.equals(valueId)){
//                            //删除该属性值删除的属性组
//                            iterator.remove();
//                        }
//                    }
//                }
//            }
//        }

        /**
         * 迭代器 适合做检查（警察）式的删除 和 面包屑 代码有相似的地方，可以整合
         *
         * 删除和面包屑的整合
         */
        if (delValueIds != null) { //只有在平台属性id有值得时候，才能确定属性列表有值要删除和说明才有面包屑生成
            //面包屑  面包屑-》用户所点击过的平台属性 , 当前请求 - 当前面包屑的属性 = 新url
            //pmsSearchParam
            //delValueIds
            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();

            /**
             * for (String delValueId : delValueIds) 放在这的原因？
             *  因为最外层的循环次数，正好是你要生成面包屑的个数也是你要删除平台属性的的个数
             */
            for (String delValueId : delValueIds) { //合成一件事，就必须需要valueId
                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator(); //平台属性集合

                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb(); //每次循环，都会new一个面包屑生成
                //生成面包屑的参数（id，url），这两个参数在delValueIds都有，但唯独没有属性值的名称
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrilParamForCrumb(pmsSearchParm, delValueId));
                /**
                 *
                 * iterator.hasNext()，对迭代器的循环，可是迭代器只循环一遍就没了，
                 * 所以要将Iterator放入for循环里：Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                 */
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) { //查找到对应的属性值
                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            /**
                             * 即将要删除的平台属性，正好就是要设置的面包屑名称
                             */
                            //查找面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());//属性值的名称
                            //删除该属性值删除的属性组
                            iterator.remove();
                        }
                    }
                }

                pmsSearchCrumbs.add(pmsSearchCrumb);
            }

            modelMap.put("attrValueSelectedList",pmsSearchCrumbs );
        }


        //传来的pmsSearchParm包含了哪些参数，当前urlParam就包含了哪些参数
        String urlParam = getUrilParam(pmsSearchParm); //获取当前url

        //当前请求，去到属性链接的的请求
        modelMap.put("urlParam", urlParam);
        String keyWord = pmsSearchParm.getKeyWord();
        if (StringUtils.isNotBlank(keyWord)) {
            modelMap.put("keyWord", keyWord);
        }

        /**
         * 单独要增加面包屑的操作
         */
        //面包屑  面包屑-》用户所点击过的平台属性 , 当前请求 - 当前面包屑的属性 = 新url
        //pmsSearchParam
        //delValueIds
//        List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
//        if (delValueIds != null) {
            //如果valueId参数不为空，说明当前请求中包含属性的参数，每一个属性的参数，都会生成一个面包屑
//            for (String delValueId : delValueIds) {
//                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
//                //生成面包屑的参数
//                pmsSearchCrumb.setValueId(delValueId);
//                pmsSearchCrumb.setValueName(delValueId);
//                pmsSearchCrumb.setUrlParam(getUrilParamForCrumb(pmsSearchParm, delValueId));
//                pmsSearchCrumbs.add(pmsSearchCrumb);
//            }
//        }

        return "list";
    }


    //拼接url请求路径的方法
    private String getUrilParamForCrumb(PmsSearchParm pmsSearchParm, String delValueId) {

        //keyWord和catalog3Id至少包含了一项，因为点击了这个才可以进去
        String keyWord = pmsSearchParm.getKeyWord();
        String catalog3Id = pmsSearchParm.getCatalog3Id();
        /**
         * 报错：skuAttrValueList没有封装上参数
         * 解决：分类属性值列表，封装为数组
         */
        String[] skuAttrValueList = pmsSearchParm.getValueId(); //分类属性值列表

        String urlParam = "";

        if (StringUtils.isNotBlank(keyWord)) {
            /**
             * 如果keyWord和catalog3Id前面没有任何参数的时候，当前参数不应该有&符号。所以加个判断
             */
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&"; //前面有参数
            }
            urlParam = urlParam + "keyWord" + keyWord; //没参数
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id" + catalog3Id;
        }

        /**
         * 因为skuAttrValueList没有封装上参数，当前url拼接不了参数
         */
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                if (!pmsSkuAttrValue.equals(delValueId)) {  //两个方法可以合并成一个方法，加入可变形参
                    urlParam = urlParam + "&valueId" + pmsSkuAttrValue;
                }
            }
        }

        return urlParam;
    }


    //拼接url请求路径的方法
    private String getUrilParam(PmsSearchParm pmsSearchParm, String... delValueId) { //可变形参

        //keyWord和catalog3Id至少包含了一项，因为点击了这个才可以进去
        String keyWord = pmsSearchParm.getKeyWord();
        String catalog3Id = pmsSearchParm.getCatalog3Id();
        /**
         * 报错：skuAttrValueList没有封装上参数
         * 解决：分类属性值列表，封装为数组
         */
        String[] skuAttrValueList = pmsSearchParm.getValueId(); //分类属性值列表

        String urlParam = "";

        if (StringUtils.isNotBlank(keyWord)) {
            /**
             * 如果keyWord和catalog3Id前面没有任何参数的时候，当前参数不应该有&符号。所以加个判断
             */
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&"; //前面有参数
            }
            urlParam = urlParam + "keyWord" + keyWord; //没参数
        }
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id" + catalog3Id;
        }

        /**
         * 因为skuAttrValueList没有封装上参数，当前url拼接不了参数
         */
        if (skuAttrValueList != null) {
            for (String pmsSkuAttrValue : skuAttrValueList) {
                urlParam = urlParam + "&valueId" + pmsSkuAttrValue;
            }
        }

        return urlParam;
    }


}





















