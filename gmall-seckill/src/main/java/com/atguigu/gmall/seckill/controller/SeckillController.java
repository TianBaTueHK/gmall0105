package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

/**
 * 秒杀
 */
@Controller
public class SeckillController {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;


    @RequestMapping("secKill")
    @ResponseBody
    public String secKill(){
        Jedis jedis = redisUtil.getJedis();

        RSemaphore semaphore = redissonClient.getSemaphore("106");
        boolean b = semaphore.tryAcquire();

        Integer stock = Integer.parseInt(jedis.get("106"));
        if (b){
            System.out.println("当前库存剩余数量"+stock+",某用户抢购成功，当前抢购人数："+(1000-stock));
            //用消息队列发出订单消息
        }else {
            System.out.println("当前库存剩余数量"+stock+",某用户抢购失败");
        }

        return "1";
    }


    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        Jedis jedis = redisUtil.getJedis();
        //开启商品的监控
        jedis.watch("106"); //"106"这件商品
        Integer stock = Integer.parseInt(jedis.get("106")); //查询这件商品的数量
        if (stock > 0){ //商品的数量>0
            Transaction multi = jedis.multi();//开事务抢
            multi.incrBy("106",-1);
            List<Object> exec = multi.exec();
            if (exec!=null && exec.size()>0){
                System.out.println("当前库存剩余数量"+stock+",某用户抢购成功，当前抢购人数："+(1000-stock));
                //用消息队列发出订单消息
            }else {
                System.out.println("当前库存剩余数量"+stock+",某用户抢购失败");
            }

        }
        jedis.close();
        return "1";
    }

}




































