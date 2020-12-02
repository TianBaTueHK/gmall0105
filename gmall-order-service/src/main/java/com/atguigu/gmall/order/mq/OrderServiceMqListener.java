package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.payment.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.MapMessage;

public class OrderServiceMqListener {

    @Autowired
    OrderService orderService;

    @JmsListener(destination = "P",containerFactory = "jmsQueueListener")
    public void consumePaymentResult(MapMessage mapMessage){

        //更新订单状态



    }


}



































