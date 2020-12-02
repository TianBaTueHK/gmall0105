package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.atguigu.gamll.annotations.LoginRequired;
import com.atguigu.gamll.util.CookieUtil;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.payment.service.CartService;
import com.atguigu.gmall.payment.service.SkuService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Controller
public class CartController {

    @Reference
    SkuService skuService;
    @Reference
    CartService cartService; //购物车服务

    /**
     * 加入购物车后，结算功能（业务）
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequired(loginSuccess = false) //自定义的注解，有:说明该方法拦截 ,false:拦截认证失败后，也可以继续访问
    public String cartList(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        List<OmsCartItem> omsCartItems = new ArrayList<>();//购物车数据集合的这个变量

//        String memberId = ""; //用户登录判断
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");

        if (StringUtils.isNotBlank(memberId)){ //如果用户登录了才去查询

            //如果已经登录，从购物车服务中查询
            omsCartItems = cartService.cartList(memberId); //去缓存查询，
        }else {
            //如果没登录，查询cookie
            Cookie cartListCookie = CookieUtil.getCookieByName(request,"cartListCookie",true);
            if (StringUtils.isNotBlank((CharSequence) cartListCookie)){
//                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);
            }
        }

        modelMap.put("cartList",omsCartItems);

        //被勾选商品的总额
        BigDecimal totalAmount = getTotalAmount(omsCartItems);
        modelMap.put("totalAmount",totalAmount);

        return "cartList";
    }

    /**
     * 被勾选商品的总额
     * @param omsCartItems
     * @return
     */
    private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItems) {

        BigDecimal totalAmount = new BigDecimal("0");

        for (OmsCartItem omsCartItem : omsCartItems) { //遍历商品集合
            BigDecimal totalPrice = omsCartItem.getPrice(); //得到每个商品的价格

            //如果商品是被选中
            if (omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }


    /**
     * 重定向
     *
     * 将商品加入购物车：没登录的情况，登录的情况；查询购物车的，更新购物车的功能
     *
     * @return
     */
    @RequestMapping("addToCart")
    public String addToCart(String skuId, long quantity, HttpServletRequest request, HttpServletResponse response) {

        /**
         * 用户未登录和登录了都需要购物车数据集合的这个变量，所以可以将其放入外面，公用，
         */
        List<OmsCartItem> omsCartItems = new ArrayList<>();

        /**
         * 将商品加入购物车的代码功能
         */
        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId, "");

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setDeleteStatus("0");
        omsCartItem.setProductAtt("颜色");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
//        omsCartItem.setQuantity("24"); //添加的商品数量

        //判断用户是否登录
//        String memberId = "1"; //request.getAttribute("memberId");
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");

        //用户没有登录
        if (StringUtils.isBlank(memberId)) {
            //用户没有登录 , 操作cookie，对cookie增删改查
//            List<OmsCartItem> omsCartItems = new ArrayList<>(); //购物车数据集合的变量

            //cookie里原有的购物车数据(cookie中的购物车数据)
            Cookie cartListCookie = CookieUtil.getCookieByName(request, "cartListCookie",true);
            if (StringUtils.isBlank((CharSequence) cartListCookie)) { //cookie里没有的购物车数据
                //cookie为空
                omsCartItems.add(omsCartItem);
            } else {
                //cookie不为空
                //omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class);有，但不知道为什么爆红
//                omsCartItems = JSON.parseArray(cartListCookie,OmsCartItem.class); //.parseArray(cartListCookie, OmsCartItem.class);

                //判断添加的购物车数据在cookie是否存在
                boolean exist = if_cart_exist(omsCartItems, omsCartItem);
                if (exist) {
                    //之前添加过，更新购物车数量
                    for (OmsCartItem cartItem : omsCartItems) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            //更新购物车数量
//                           cartItem.setQuantity(cartItem.getQuantity() + omsCartItem.getQuantity());
                            //更新价格
//                            cartItem.setPrice(cartItem.getPrice() + omsCartItem.getPrice());
                        }
                    }
                } else {
                    //之前没有添加，新增当前的购物车
                    omsCartItems.add(omsCartItem);
                }
                //更新cookie
                CookieUtil.getCookieByName(request, response, "cartListCookie", JSON.toJSONString(omsCartItems), 60 * 60 * 72, true);

            }
        } else {
            /**
             * 用户未登录和登录了都需要购物车数据集合的这个变量，所以可以将其放入外面，公用，
             */
            //用户登录了 Db+cache(redis)
//            List<OmsCartItem> omsCartItems = new ArrayList<>();
            //从db中查出购物车数据
            OmsCartItem omsCartItemFromDb = cartService.ifCartExistByUser(memberId, skuId);
//            if (omsCartItems == null){
//                //db为空
//            }else {
//                //db不为空
//                //判断是否重复
//            }
            //该用户没有添加过当前商品
            if (omsCartItem == null) {
                //当前商品的信息
                omsCartItem.setMemberId(memberId); //当前用户
                omsCartItem.setProductAtt("lh");
                //添加
                cartService.addCart(omsCartItem);
            } else {
                //该用户添加过当前商品
                //改为当前商品到的数量
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity());//是omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity())
                cartService.updateCart(omsCartItemFromDb);
            }

            //同步缓存,从mysql查出之后，同步到缓存中去
            cartService.flushCartCache(memberId);

        }


        //购物车是写的操作，必须为重定向
        return "redirect:/success.html";
    }

    /**
     * 判断添加的购物车数据在cookie中是否存在
     *
     * @param omsCartItems
     * @param omsCartItem
     * @return
     */
    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {

        boolean b = false;

        for (OmsCartItem cartItem : omsCartItems) {
            //根据当前被添加到购物车的sku商品判断
            String productSkuId = cartItem.getProductSkuId();
            if (productSkuId.equals(omsCartItem.getProductSkuId())) {
                b = true;
            }
        }

        return b;
    }
}































