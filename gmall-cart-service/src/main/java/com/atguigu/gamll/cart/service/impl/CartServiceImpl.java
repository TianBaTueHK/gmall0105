package com.atguigu.gamll.cart.service.impl;

import com.alibaba.fastjson.JSON;

import com.atguigu.gamll.cart.mapper.OmsCartItemMapper;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.payment.service.CartService;
import com.atguigu.gmall.util.RedisUtil;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CartServiceImpl implements CartService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    /**
     * 从db中查出购物车中的数据，是否存在
     *
     * @param memberId
     * @param skuId
     * @return
     */
    @Override
    public OmsCartItem ifCartExistByUser(String memberId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        //同一个用户对同一个商品在购物车中只能添加一次
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);
        return omsCartItem1;
    }

    /**
     * 该用户没有添加过当前商品，现在就是要添加该商品
     *
     * @param omsCartItem
     */
    @Override
    public void addCart(OmsCartItem omsCartItem) {

        if (StringUtils.isNotBlank(omsCartItem.getMemberId())) { //用户id不为空
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    /**
     * 更新数据库中的购物车商品数量，根据id去更新
     *
     * @param omsCartItemFromDb
     */
    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {

        Example e = new Example(OmsCartItem.class);
        e.createCriteria().andEqualTo("id", omsCartItemFromDb.getId());

        //updateByExampleSelective: 为null的字段不更新，不为空的字段更新
        omsCartItemMapper.updateByExampleSelective(omsCartItemFromDb, e); //原始数据，当前数据
    }

    /**
     * 数据库中购物车数量发生变化，同步缓存,从mysql查出之后，同步到缓存中去
     * 但：购物车是经常会发生变化的，那么去访问数据库的次数就多了
     * 关于写入的操作，尽量少访问mysql数据库
     *
     * @param memberId
     */
    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);

        //同步到redis缓存中
        Jedis jedis = redisUtil.getJedis();
        /**
         * 设计数据如何存储在redis缓存中
         */
        Map<String, String> map = new HashMap<>();
        for (OmsCartItem cartItem : omsCartItems) {
            map.put(cartItem.getProductSkuId(), JSON.toJSONString(cartItem));
        }
        //删除之前的数据
        jedis.del("user:" + memberId + ":cart");
        //插入新的数据
        jedis.hmset("user:" + memberId + ":cart", map);


        jedis.close();
    }


    /**
     * 如果已经登录，从购物车服务中查询
     * 去缓存查询
     * (skuServiceImpl):缓存+Db---》kv的数据结构
     * cartList : 缓存+Db---》hash的数据结构
     *
     * @param userId
     * @return
     */
    @Override
    public List<OmsCartItem> cartList(String userId) {

        Jedis jedis = null;
        //购物车集合
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        try {
            jedis = redisUtil.getJedis();
            //返回的是字符串集合
            List<String> hvals = jedis.hvals("user:"+ userId +":cart");

            for (String hval : hvals) {
                OmsCartItem omsCartItem = JSON.parseObject(hval,OmsCartItem.class);
                omsCartItems.add(omsCartItem);
            }

        }catch (Exception e){
            //异常处理，记录系统日志
            e.printStackTrace();
            String message = e.getMessage();
            //logService.addErrLog(message);
            return null;
        }finally {
            jedis.close();
        }

        return omsCartItems;
    }


}





















