package com.lagou.order.service.impl;

import com.lagou.entity.Result;
import com.lagou.goods.feign.SkuFeign;
import com.lagou.order.dao.OrderItemMapper;
import com.lagou.order.dao.OrderLogMapper;
import com.lagou.order.dao.OrderMapper;
import com.lagou.order.pojo.OrderItem;
import com.lagou.order.pojo.OrderLog;
import com.lagou.order.service.CartService;
import com.lagou.order.service.OrderService;
import com.lagou.order.pojo.Order;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.lagou.order.util.AdminToken;
import com.lagou.user.feign.UserFeign;
import com.lagou.util.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private UserFeign userFeign;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * ??????????????????
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * ??????ID??????
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }


    /**
     * ??????
     *
     * @param order
     */
    @Override
    public void add(Order order) {
        //?????????????????????
        Map cartMap = cartService.list(order.getUsername());
        List<OrderItem> orderItemList = (List<OrderItem>) cartMap.get("orderItemList");
        //???????????????????????????
        //?????????????????????????????????????????????????????? to do
        order.setId(String.valueOf(idWorker.nextId()));
        //????????????
        int totalNum = 0;
        int totalPrice = 0;
        for (OrderItem orderItem : orderItemList) {
            //??????????????????
            if (orderItem.isChecked()) {
                totalNum += orderItem.getNum();
                totalPrice += orderItem.getMoney();
            }
        }
        order.setTotalNum(totalNum);
        order.setTotalMoney(totalPrice);
        order.setPayMoney(totalPrice);

        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setSourceType("1");//????????????
        order.setBuyerRate("0");//????????????
        order.setOrderStatus("0");//????????????
        order.setPayStatus("0");//????????????
        order.setConsignStatus("0"); //?????????
        order.setIsDelete("0");//?????????
        orderMapper.insertSelective(order);

        //???????????????????????????????????????????????????
        skuFeign.changeInventoryAndSaleNumber(order.getUsername());

        //????????????
        //userFeign.addPoints(order.getPayMoney()/10);
        //userFeign.addPoints(10);
        //?????????????????????mq????????????????????? username,point

        //?????????????????????????????????
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.isChecked()) {
                //??????id
                orderItem.setId(String.valueOf(idWorker.nextId()));
                orderItem.setOrderId(order.getId());
                orderItem.setIsReturn("0");//????????????
                orderItemMapper.insertSelective(orderItem);
                //?????????????????????: isChecked = true
                cartService.delete(orderItem.getSkuId(), order.getUsername());
            }
        }
        //????????????????????????ordercreate_queue???
        rabbitTemplate.convertAndSend("", "ordercreate_queue", order.getId());
    }


    /**
     * ??????
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * ??????
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }


    /**
     * ????????????
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * ????????????
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Order>) orderMapper.selectAll();
    }

    /**
     * ??????+????????????
     *
     * @param searchMap ????????????
     * @param page      ??????
     * @param size      ?????????
     * @return ????????????
     */
    @Override
    public Page<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Order>) orderMapper.selectByExample(example);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param map
     */
    @Override
    public void changeOrderStatusAndOrderLog(Map<String, String> map) {
        //??????????????????
        Order order = orderMapper.selectByPrimaryKey(map.get("out_trade_no"));
        //???????????????????????????
        if (order != null && "0".equals(order.getPayStatus())) {
            order.setPayStatus("1");//?????????
            order.setOrderStatus("1");//?????????
            //????????????????????????
            order.setTransactionId(map.get("trade_no"));
            order.setUpdateTime(new Date());
            //??????????????????
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                order.setPayTime(format.parse(map.get("gmt_payment")));
            } catch (ParseException e) {
                order.setPayTime(new Date());
                e.printStackTrace();
            }
            //????????????
            orderMapper.updateByPrimaryKeySelective(order);
            //????????????????????????
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setOperater("system");
            orderLog.setOrderId(order.getId());
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("1");
            orderLog.setPayStatus("1");
            orderLog.setRemarks("Alipay??????:" + map.get("trade_no"));
            orderLogMapper.insert(orderLog);
        }
    }

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private RestTemplate restTemplate;
    /**
     * ????????????
     *
     * @param orderId
     */
    @Override
    public void close(String orderId) {
        //????????????
        Order order = orderMapper.selectByPrimaryKey(orderId);
        order.setUpdateTime(new Date());//????????????
        order.setCloseTime(new Date());//????????????
        order.setOrderStatus("4");//????????????
        orderMapper.updateByPrimaryKeySelective(order);

        //??????????????????
        OrderLog orderLog = new OrderLog();
        orderLog.setRemarks(orderId + "???????????????");
        orderLog.setOrderStatus("4");
        orderLog.setOperateTime(new Date());
        orderLog.setOperater("system");
        orderLog.setId(idWorker.nextId() + "");
        orderLogMapper.insert(orderLog);

        //????????????&??????
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        List<OrderItem> orderItems = orderItemMapper.select(orderItem);
        for (OrderItem orderItem_ : orderItems) {
            //?????????????????????
            //skuFeign.resumeStockNum(orderItem_.getSkuId(),orderItem_.getNum());
            ServiceInstance serviceInstance = loadBalancerClient.choose("goods");
            //2.??????????????????
            String path = serviceInstance.getUri().toString()+"/sku/resumeStockNum";
            //3.????????????
            MultiValueMap<String,String> formData = new LinkedMultiValueMap<>();
            formData.add("skuId",orderItem_.getSkuId());
            formData.add("num",orderItem_.getNum()+"");
            //??????header
            MultiValueMap<String,String> header = new LinkedMultiValueMap<>();
            //value : Basic base64(clientid:clientSecret)
            header.add("Authorization","bearer "+ AdminToken.create());
            //????????????
            Result result = null;
            try {
                ResponseEntity<Result> mapResponseEntity =
                        restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(formData, header), Result.class);
                result = mapResponseEntity.getBody();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private String httpBasic(String clientId,String clientSecret){
        String idAndSecret = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(idAndSecret.getBytes());
        return "Basic "+new String(encode);
    }

    /**
     * ??????????????????
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // ??????id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // ???????????????1??????????????????0 ????????????
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andEqualTo("payType", searchMap.get("payType"));
            }
            // ????????????
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // ????????????
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // ????????????
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // ????????????
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // ????????????
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // ?????????
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // ???????????????
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // ???????????????
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // ???????????????1:web???2???app???3?????????????????????4??????????????????  5 H5????????????
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andEqualTo("sourceType", searchMap.get("sourceType"));
            }
            // ???????????????
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // ????????????
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andEqualTo("orderStatus", searchMap.get("orderStatus"));
            }
            // ????????????
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andEqualTo("payStatus", searchMap.get("payStatus"));
            }
            // ????????????
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andEqualTo("consignStatus", searchMap.get("consignStatus"));
            }
            // ????????????
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }

            // ????????????
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // ????????????
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // ????????????
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // ??????
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // ????????????
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
