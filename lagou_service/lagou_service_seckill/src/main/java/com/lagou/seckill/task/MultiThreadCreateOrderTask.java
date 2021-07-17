package com.lagou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.lagou.entity.SeckillStatus;
import com.lagou.seckill.dao.SecKillGoodsMapper;
import com.lagou.seckill.pojo.SeckillGoods;
import com.lagou.seckill.pojo.SeckillOrder;
import com.lagou.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author lihe
 * @Version 1.0
 */
@Component
public class MultiThreadCreateOrderTask {

    private static final String SECKILL_KEY = "SeckillGoods_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SecKillGoodsMapper goodsMapper;

    @Autowired
    private IdWorker idWorker;

    /**
     * 以多线程的方式执行该方法
     * 底层就是用线程池实现的
     */
    @Async
    public void createOrder() {
        try {
            System.out.println("MultiThreadCreateOrderTask....createOrder...start");
            Thread.sleep(10000);
            //获取Redis中排队用户信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            if (seckillStatus == null) {
                return;
            }
            //通过订单状态获取下单信息
            String username = seckillStatus.getUsername();
            String time = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();

            //先从SeckillGoodsQueue中获取商品的库存队列信息
            Object rightPop = redisTemplate.boundListOps("SeckillGoodsQueue_" + seckillStatus.getGoodsId()).rightPop();
            //如果没有获取到商品的库存队列,没有库存,清空排队信息,终止该方法
            if (rightPop == null) {
                //清理掉排队信息
                redisTemplate.boundHashOps("UserQueueCount").delete(username);
                redisTemplate.boundHashOps("UserQueueStatus").delete(username);
                return;
            }
            //从redis中检索秒杀对象，判断有没有库存
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_KEY + time).get(id);
            if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {
                throw new RuntimeException("卖完了!");
            }
            //创建订单信息
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            //未支付
            seckillOrder.setStatus("0");
            //保存订单到Redis,每人只能秒杀一次
            System.out.println("秒杀订单ID："+seckillOrder.getId());
            redisTemplate.boundHashOps("SeckillOrder_").put(username, seckillOrder);

            //修改用户抢单状态
            seckillStatus.setStatus(2);//待付款状态
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice().toString()));
            seckillStatus.setOrderId(seckillOrder.getId());
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            //库存递减
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);

            /** 库存不精确解决方案 */
            //获取到秒杀商品的库存队列,长度就是剩余库存数,最准确的库存数
            Long size = redisTemplate.boundListOps("SeckillGoodsQueue_" + seckillGoods.getGoodsId()).size();
            //1.当前购买的商品就是最后一件:redis中该商品记录移除
            if (size <= 0) {
                //同步库存数量
                seckillGoods.setStockCount(size.intValue());
                //将数据同步到Mysql中
                goodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //移除Redis中该商品的数据
                redisTemplate.boundHashOps(SECKILL_KEY + time).delete(id);
            } else {
                //将库存数据更新到Redis
                redisTemplate.boundHashOps(SECKILL_KEY + time).put(id, seckillGoods);
            }
            System.out.println("MultiThreadCreateOrderTask....createOrder...end...下单成功");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
