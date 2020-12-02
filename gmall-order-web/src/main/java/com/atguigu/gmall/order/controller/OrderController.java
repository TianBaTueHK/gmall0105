package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gamll.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.payment.service.CartService;
import com.atguigu.gmall.payment.service.OrderService;
import com.atguigu.gmall.payment.service.SkuService;
import com.atguigu.gmall.payment.service.UserServer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    CartService cartService;

    @Reference
    UserServer userServer;

    @Reference
    OrderService orderService;

    @Reference
    SkuService skuService;


    /**
     * 点击提交订单（付款）的按钮
     * @param receiveAddressId:收货人收货地址id
     * @param totalAmount：付款总金额
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("submitOrder")
    @LoginRequired(loginSuccess = true) //自定义的注解，有:说明该方法拦截
    public ModelAndView submitOrder(String receiveAddressId, BigDecimal totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){
        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");

//        点击结算

        //检查交易码，保证订单提交的唯一性
        String success = orderService.checkTradeCode(memberId,tradeCode);

        if (success.equals("success")){
            List<OmsOrderItem> omsOrderItems = new ArrayList<>();
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setCreateTime(new Date()); //订单生成时间
            omsOrder.setMemberUsername(nickName);
            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();//将毫秒时间戳拼接到外部订单号
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + sdf.format(new Date());
            omsOrder.setOrderSn(outTradeNo); //订单号

            //获得收货地址
            UmsMemberReceiveAddress umsMemberReceiveAddress = userServer.getReceiveAddressById(receiveAddressId);


            //1 根据用户id获得要购买的商品列表和总价格
            List<OmsCartItem> omsCartItems = cartService.cartList(memberId); //先将商品从缓存中查出来
            //循环购物车
            for (OmsCartItem omsCartItem : omsCartItems) {
                if (omsCartItem.getIsChecked().equals("1")){
                    //每获得一个要购买的商品，就获得订单详情列表
                    OmsOrderItem omsOrderItem = new OmsOrderItem(); //然后封装为订单对象，每封装一个商品就要验价验库存
                    //2 验价，
                    boolean b = skuService.checkPrice(omsCartItem.getProductSkuId(),omsCartItem.getPrice());
                    if (b==false){
                        ModelAndView mv = new ModelAndView("tradeFail");
                        return mv;
                    }
                    //验库存，要远程调用库存系统 ,封装订单详情对象
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setOrderSn(outTradeNo); //外部订单号，用来和其他系统进行交互，防止重复
                    omsOrderItem.setProductPrice(omsCartItem.getPrice());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());

                    omsOrderItems.add(omsOrderItem);
                }
            }
            omsOrder.setOmsOrderItems(omsOrderItems);


            //3 将订单和订单详情写入数据库
            //  删除购物车商品
            orderService.saveOrder(omsOrder);


            //3 重定向到支付系统
            ModelAndView mv = new ModelAndView("redirect:http:payment.gmall.com:8087/index");
            mv.addObject("outTradeNo",outTradeNo);
            return mv;
        }else {
            ModelAndView mv = new ModelAndView("tradeFail");
            return mv;
        }
    }

    /**
     *结算后，封装订单详情表，上一个功能在CartController
     * 订单结算页面
     * @param request
     * @param response
     * @param session
     * @param modelMap
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequired(loginSuccess = true) //自定义的注解，有:说明该方法拦截
    public String toTrade(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");

        //收货人地址信息
        List<UmsMemberReceiveAddress> umsMemberReceiveAddress = userServer.getReceiveAddressByMemberId(memberId);//用户地址

        List<OmsCartItem> omsCartItems = cartService.cartList(memberId); //购物车表

        List<OmsOrderItem> omsOrderItems = new ArrayList<>(); //订单详情表

        //将购物车集合转化为页面结算清单集合
        for (OmsCartItem omsCartItem : omsCartItems) {
            //将选中商品的才封装到表中
            if (omsCartItem.getIsChecked().equals("1")){
                //每次循环一个购物车对象，就封装一个商品的详情到OmsOrderItem
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                //前端需要商品的什么信息，就set什么
                omsOrderItem.setProductName(omsCartItem.getProductName());
                omsOrderItem.setProductPic(omsCartItem.getProductPic());
                omsOrderItems.add(omsOrderItem);
            }
        }
        //前端需要的，返回给前端的信息值
        modelMap.put("omsOrderItems",omsOrderItems);
        modelMap.put("userAddressList",umsMemberReceiveAddress);
        modelMap.put("totalAmount",getTotalAmount(omsCartItems));


        //生成交易码，为了在提交订单时做交易码的校验，保证订单提交的唯一性
        String tradCode = orderService.getTradeCode(memberId); //根据用户id生成交易码，还可以根据商品id
        modelMap.put("tradCode",tradCode);

        //订单结算页面
        return "trade";
    }

    /**
     * 商品总金额
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
}


























