package com.atguigu.gmall.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *连接redis缓存的工具类
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initPool(String host, String port, int database){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        //最大连接数
        poolConfig.setMaxTotal(200);
        //最小空闲连接数
        poolConfig.setMinIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        //当池内没有可用的连接时，最大等待时间
        poolConfig.setMaxWaitMillis(10*100);
        poolConfig.setTestOnBorrow(true);
        jedisPool = new JedisPool(poolConfig,host, Integer.parseInt(port),20*1000);
    }

    public Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
}






























