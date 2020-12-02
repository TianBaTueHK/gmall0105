package com.atguigu.gmall.payment.mq;

import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Map;

@Component
public class PaymentServiceMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYHMENT_CHECK_QUEUE" ,containerFactory = "jmsQueueListener")
    public void consumePaymentCheckResult(MapMessage mapMessage) throws JMSException{

        String out_trade_no = mapMessage.getString("trade_no");

//        Map<String,Object> resultMap = paymentService.checkAlipaPayment(out_trade_no);



    }


}






















