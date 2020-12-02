package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.mq.ActiveMQUtil;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    ActiveMQUtil activeMQUtil;


    /**
     *保存用户的支付信息
     * @param paymentInfo
     */
    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {

        paymentInfoMapper.insertSelective(paymentInfo);
    }

    /**
     * 更新到数据库中,更新用户的支付状态
     * 支付成功后，引起的系统服务-》订单服务的更新-》库存服务-》物流服务
     * 可能要开启分布式事务：使用消息队列
     * @param paymentInfo
     */
    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        String orderSn = paymentInfo.getOrderSn();

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderSn",orderSn);

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection(); //开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED); //创建session
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            paymentInfoMapper.updateByExampleSelective(paymentInfo , example);
            //支付成功后，引起的系统服务-》订单服务的更新-》库存服务-》物流服务
            //调用mq发送支付成功的信息
            Queue payhment_success_queue = session.createQueue("PAYHMENT_SUCCESS_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);

            TextMessage textMessage = new ActiveMQTextMessage();//字符串文本

            MapMessage mapMessage = new ActiveMQMapMessage(); //hash结构
            mapMessage.setString("out_trade_no",paymentInfo.getOrderSn());

            producer.send(mapMessage);
            session.commit();
        }catch (Exception e){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *向消息中间件发送一个检查支付状态（支付服务消费）的延迟消息队列
     * @param outTradeNo
     */
    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo) {

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection(); //开启事务
            session = connection.createSession(true, Session.SESSION_TRANSACTED); //创建session
        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {

            Queue payhment_success_queue = session.createQueue("PAYHMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payhment_success_queue);


            MapMessage mapMessage = new ActiveMQMapMessage(); //hash结构
            mapMessage.setString("out_trade_no",outTradeNo);

            //加入延迟时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*30);

            producer.send(mapMessage);
            session.commit();
        }catch (Exception e){
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

}
























