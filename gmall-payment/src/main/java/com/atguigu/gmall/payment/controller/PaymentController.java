package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gamll.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.payment.service.OrderService;
import com.atguigu.gmall.payment.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    PaymentService paymentService;

    @Reference
    OrderService orderService; //订单service

    /**
     * 支付宝扫码后就创建了这笔订单，返回的页面
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String callBackReturn(HttpServletRequest request, ModelMap modelMap){

        //回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no"); //订单编号
        String out_trade_no = request.getParameter("out_trade_no");
        String total_amount = request.getParameter("total_amount"); //金额
        String call_back_content = request.getQueryString();


        //通过支付宝的paramsMap进行签名验证，2.0版本到的接口将paramsMap参数去掉了，导致同步请求没法验证
        if (StringUtils.isNoneBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setCallbackTime(call_back_content);
//          paymentInfo.setCallbackTime(new Date());

            //更新到数据库中,更新用户的支付状态
            paymentService.updatePayment(paymentInfo);
        }

        //支付成功后，引起的系统服务-》订单服务的更新-》库存服务-》物流服务
        //调用mq发送支付成功的信息


        return "finish";
    }

    /**
     * 微信支付
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("mx/submit")
    @LoginRequired(loginSuccess = true)
    public String mx(String outTradeNo, String totalAmount, HttpServletRequest request, ModelMap modelMap){


        return null;
    }

    /**点击订单结算的submit按钮，
     * 支付宝还未支付
     * @param outTradeNo
     * @param totalAmount
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, String totalAmount, HttpServletRequest request, ModelMap modelMap){

        //获得一个支付宝请求的客户端（并不是一个链接，而是一个封装好的http的表单请求
        String form = null;
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();

        //回调函数地址（是相互回调的
        alipayTradePagePayRequest.getReturnUrl(); //要写地址
        alipayTradePagePayRequest.setNotifyUrl("http: / / /callback/notify");

        Map<String,Object> map = new HashMap<>();
        //4个参数为必写参数
        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",totalAmount);
        map.put("subject","商品标题");

        String param = JSON.toJSONString(map);

        alipayTradePagePayRequest.setBizContent(param);
        try {
            form = alipayClient.pageExecute(null).getBody(); //调用SDK生成表单
            System.out.println(form);
        }catch (AlipayApiException e){
            e.printStackTrace();
        }

        //付款后，生成并保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo); //获得一个订单对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderSn(omsOrder.getId());
        paymentInfo.setOutTradeNo(outTradeNo); //订单编号
        paymentInfo.setPaymentStatus("未付款"); //状态
        paymentInfo.setSubject("商品名称");
        paymentInfo.setTotalAmount("12345");
        paymentService.savePaymentInfo(paymentInfo);

        /**
         * 向消息中间件发送一个检查支付状态（支付服务消费）的延迟消息队列
         * 检查当前的订单号
         */
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo);


        //提交到请求到支付宝
        return form;
    }

    /**
     *点击支付按钮，跳转到支付系统的index页面
     * @param outTradeNo 订单编号
     * @param totalAmount 数量
     * @param request
     * @param modelMap
     * @return
     */
    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(String outTradeNo, String totalAmount, HttpServletRequest request, ModelMap modelMap){

        String memberId = (String)request.getAttribute("memberId");
        String nickName = (String)request.getAttribute("nickName");
        //返回给前端的值
        modelMap.put("nickName",nickName);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);

        return "index";
    }



}



























