package com.atguigu.gmall.payment.service.impl;

import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.payment.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.payment.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.payment.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.payment.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.payment.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Service;
import com.alibaba.dubbo.config.annotation.Service;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @Configuration: 当一个类上面有了这个注解，说明这个类是spring的配置类了
 */
@Service
@Configuration
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        //插入skuInfo
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        String skuId = pmsSkuInfo.getId();

        //插入平台属性关联
        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            //循环插入的时候需要一个skuId
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }

        //插入销售属性关联
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
        //插入图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insertSelective(pmsSkuImage);
        }
    }


    public PmsSkuInfo getSkuByIdFromDb(String skuId){
        //sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //商品图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
        skuInfo.setSkuImageList(pmsSkuImages);//多张图片放入一个List中
        return skuInfo;
    }

    /**
     * 根据id查询商品属性，返回给控制层，控制层返回给前端页面
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuById(String skuId,String ip) {

//        获取ip地址
        System.out.println("ip:"+ ip + Thread.currentThread().getName());

        //sku商品对象
//        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
//        pmsSkuInfo.setId(skuId);
//        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //商品图片集合
//        PmsSkuImage pmsSkuImage = new PmsSkuImage();
//        pmsSkuImage.setSkuId(skuId);
//        List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
//        skuInfo.setSkuImageList(pmsSkuImages);//多张图片放入一个List中

//        从redis缓存中查询============================================================================================

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //链接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存(设计一个数据存储策略)
        String skuKey = "sku:"+skuId+"info";
        String skuJson = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJson)){ // skuJson!=null&&!skuJson.equals("")
            System.out.println("ip:"+ ip + Thread.currentThread().getName() +"从缓存中获取商品详情");
            pmsSkuInfo = com.alibaba.fastjson.JSON.parseObject(skuJson,PmsSkuInfo.class);
        }else {
            //如果缓存中没有，查询mysql--》这一步是应该受到保护的
//            pmsSkuInfo = getSkuByIdFromDb(skuId);

            System.out.println("ip:"+ ip + Thread.currentThread().getName() +"发现缓存中没有，申请缓存的分布式锁"+ "sku:"+skuId+":lock");
            //设置分布式锁  //是这个：(key:"sku:"+skuId+":lock", v:"1",nxxx:"nx",expx:"px",time:10);拿到锁的线程有10秒的过期时间,在10秒内这个锁有用
           //不同的线程拿到的key是一样的，但v是不一样的；key---v
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:"+skuId+":lock", "token"); //("sku:"+skuId+":lock", "token","nx","px",10);
            /**
             * 10秒之后这个锁过期，这个请求执行完毕后会回来删锁，
             * 但这可以会产生一个问题，锁10秒过期了，之后的请求便可以拿到锁去访问数据库，第一个请求在数据库取得数据之后
             * 会回来删锁，这时删的锁就是后面请求的锁了
             * 怎么办？--》不同的线程拿到的key是一样的，但v是不一样的，用token确认删除的是自己的sku的锁
             *          String token = UUID.randomUUID().toString(); v
             *          String lockToken = jedis.get("sku:"+skuId+":info"); key
             *          StringUtils.isNotBlank(lockToken)&&lockToken.equals(token))//用token确认删除的是自己的sku的锁
             */
            if (StringUtils.isNotBlank(OK)&&OK.equals("OK")){
                //设置成功（第一个请求拿到分布式锁）该请求可以去访问数据库取数据 ;//如果缓存中没有，查询mysql--》这一步是应该受到保护的
                System.out.println("ip:"+ ip + Thread.currentThread().getName() +"有权在10秒的过期时间内访问 数据库"+ "sku:"+skuId+":lock");
                //该请求可以去访问数据库取数据
                pmsSkuInfo = getSkuByIdFromDb(skuId);

                //第一个请求从数据库中拿到数据之后，等5秒归还锁（可以不用），会将数据存入缓存中
                //延迟等待时间，将数据放入缓存中
                try {
                    Thread.sleep(1000*5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //第一个请求去访问数据库取数据，取得的查询结果存入redis缓存中，之后的请求便去缓存中获取数据即可
                if (pmsSkuInfo != null){
                    //查询结果存入redis缓存中
                    jedis.set("sku:"+skuId+":info", com.alibaba.fastjson.JSON.toJSONString(pmsSkuInfo));
                }
                //防止缓存穿透
                else {
                    //数据库中不存在该sku
                    //为了防止穿透，null或者空字符串值设置给redis --》为了保护mysql，防止穿透
                    //一旦一个sku在数据库中不存在的话，会将null值设置为redis缓存，并且这个sku的过期时间设为3分钟。3分钟不会有sql打到mysql
                    jedis.setex("sku:"+skuId+":info",60*3, com.alibaba.fastjson.JSON.toJSONString(""));

                }

                System.out.println("ip:"+ ip + Thread.currentThread().getName() +"使用完毕，将锁归还："+ "sku:"+skuId+":lock");

                /**
                 * 删锁这还有一个问题，如果线程刚好走到get和if（177,178）行，准备删锁时，刚好锁过期了，这时一个请求获得了锁
                 *      还是删的是别的请求的锁
                 * 解决--》在get的时候就删了，没有任何时间差,使用lua脚本，防止高并发下的意外的发生
                 */
                //get回来的是key对应的v
                String lockToken = jedis.get("sku:"+skuId+":info");
                if (StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){ //用token确认删除的是自己的sku的锁
                    //在get的一瞬间
                    String script = "if redis.call('get',KEYS[1]==ARGV[1]then return redis.call('del',KEYS[1])" +
                                        "else return 0 eend)";
                    jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token)); //可以用lua脚本，在查询到key的同时删除

                    //在访问mysql后，将mysql的分布式锁释放，删锁
                    jedis.del("sku:"+skuId+":lock");
                }

            }else {
                //设置失败（没有拿到分布式锁），代表访问不了数据库，则 自旋（该线程在睡眠几秒后，重新尝试访问本方法）
                //自旋完毕之后，会从新执行原始的请求方法，这是redis缓存中已经有了数据，就直接从redis中获得
                System.out.println("ip:"+ ip + Thread.currentThread().getName() +"没有拿到锁，开始自旋");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuByIdFromDb(skuId); //return 防止孤儿线程

            }

        }
        jedis.close();

        return pmsSkuInfo;
    }



    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSku(String catalog3Id) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(select);

        }

        return pmsSkuInfos;
    }

    /**
     * 验价
     * @param productId
     * @param productPrice
     * @return
     */
    @Override
    public boolean checkPrice(String productId, BigDecimal productPrice) {

        boolean b = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productId);
        PmsSkuInfo pmsSkuInfo1 = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        String price = pmsSkuInfo1.getPrice();

        if (price.compareTo(String.valueOf(productPrice)) == 0){
            b = true;
        }

        return b;
    }
}























