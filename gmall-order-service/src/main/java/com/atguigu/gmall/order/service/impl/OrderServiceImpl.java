package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.order.mapper.OmsOrderItemMapper;
import com.atguigu.gmall.order.mapper.OmsOrderMapper;
import com.atguigu.gmall.payment.service.CartService;
import com.atguigu.gmall.payment.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Reference
    CartService cartService;

    /**
     * 在提交订单时检查交易码，然后销毁
     * @param memberId
     * @param tradeCode:页面传来的tradeCode
     * @return
     */
    @Override
    public String checkTradeCode(String memberId,String tradeCode) {

        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            String tradeKey = "user:" + memberId + ":tradeCode";

            //服务器上存储的tradeCode
//            String tradeCodeFromCache = jedis.get(tradeKey); //使用lua脚本在发现key的同时将key删除，防止并发订单攻击

            //使用lua脚本，防止高并发，对比防止重删
            String script = "if redis.call('get',KBYS[1]) == ARGB[1] then return redis.call('del',KEYS[1] else return 0 end)";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey),Collections.singletonList(tradeCode));

            /**
             * 选择要购买商品完成时，第一次点击结算按钮，会生成一个tradeCode，然后去redis比对，比对通过的同时使用lua脚本删除tradeCode，返回success，，
             * 就算用户回退到结算按钮页，此时已经没有了tradeCode，返回fail，不能在点击支付了
             */
            if (eval != null && eval != 0) {
//                jedis.del(tradeKey); //使用lua脚本在发现key的同时将key删除，防止并发订单攻击
                return "success";
            } else {
                return "fail";
            }
        }finally {
            jedis.close();
        }

    }

    /**
     * 根据memberID生成交易码
     * @param memberId
     * @return
     */
    @Override
    public String getTradeCode(String memberId) {

        Jedis jedis = redisUtil.getJedis();

        String tradeKey = "user:"+memberId+":tradeCode";

        String tradeCode = UUID.randomUUID().toString();

        jedis.setex(tradeKey,60*15,tradeCode); //在redis中15分钟后过期

        jedis.close();

        return tradeCode;
    }

    /**
     *将订单和订单详情写入数据库,删除购物车商品
     * @param omsOrder
     */
    @Override
    public void saveOrder(OmsOrder omsOrder) {

        //保存订单表
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();


        //保存订单详情表
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems(); //取出订单详情
        //循环保存
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            //删除购物车商品

        }
    }


    /**
     * 获得一个订单对象
     * @param outTradeNo
     * @return
     */
    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
//        OmsOrderItem omsOrder1 = omsOrderItemMapper.selectOne("omsOrder");
//        return omsOrder1;
        return null;
    }


}































