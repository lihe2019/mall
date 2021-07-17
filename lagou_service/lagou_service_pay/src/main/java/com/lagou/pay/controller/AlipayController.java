package com.lagou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lagou.entity.Result;
import com.lagou.entity.StatusCode;
import com.lagou.order.feign.OrderFeign;
import com.lagou.order.pojo.Order;
import com.lagou.pay.config.AlipayConfig;
import com.lagou.pay.util.MatrixToImageWriter;
import com.lagou.seckill.pojo.SeckillOrder;
import com.netflix.discovery.converters.Auto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author lihe
 * @Version 1.0
 */
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    @Autowired
    private OrderFeign orderFeign;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private AlipayConfig alipayConfig;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 统一收单线下交易预创建
     *
     * @param orderId  订单ID(out_trade_no)
     * @param exchange 申请二维码的时候区分是普通订单还是秒杀订单 普通订单：order_exchange ; 秒杀订单：seckill_exchange
     */
    @RequestMapping("/qrCode")
    public Result preCreate(@RequestParam String orderId, @RequestParam String exchange) throws Exception {
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        request.setNotifyUrl("http://yuanjing.frpgz1.idcfengye.com/alipay/notify");//设置notifyUrl
        //创建预处理业务模型
        createPrecreateModel(orderId, request, exchange);
        AlipayTradePrecreateResponse response = alipayClient.execute(request);
        if (response.isSuccess() && "10000".equals(response.getCode())) {
            //5.通过二维码链接生成收款二维码
            createQrCode(orderId, response);
        }
        return new Result(true, StatusCode.OK, "交易预创建成功");
    }

    /**
     * 手动查询用户的支付结果
     * alipay.trade.query(统一收单线下交易查询)
     *
     * @return
     */
    @GetMapping("/queryStatus")
    public String query(@RequestParam String out_trade_no) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        //设置订单ID
        model.setOutTradeNo(out_trade_no);
        request.setBizModel(model);
        //执行查询
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        //判断订单状态
        String result = checkTradeStatus(response);
        return result;
    }

    /**
     * 支付宝服务器异步通知URL
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping("/notify")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        //一、获取并转换支付宝请求中参数
        Map<String, String> params = parseAlipayResultToMap(request);
        //二、验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipay_public_key(),
                alipayConfig.getCharset(), alipayConfig.getSigntype()); //调用SDK验证签名
        //签名验证成功 & 用户已经成功支付
        if (signVerified && "TRADE_SUCCESS".equals(params.get("trade_status"))) {
            //三、将数据发送MQ
            String message = prepareMQData(params);
            String exchange = params.get("body");
            rabbitTemplate.convertAndSend(exchange, "", message);
            return "success";
        } else {
            return "fail";
        }
    }

    /**
     * 关闭支付宝服务器的交易
     *
     * @param orderId
     * @return
     * @throws AlipayApiException
     */
    @RequestMapping("/close")
    public Result close(@RequestParam String orderId) throws AlipayApiException {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(orderId);
        request.setBizModel(model);
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        if (response.isSuccess() && "10000".equals(response.getCode())) {
            return new Result(true, StatusCode.OK, "操作成功");
        } else {
            return new Result(false, StatusCode.ERROR, "操作失败");
        }
    }

    /**
     * 校验订单状态
     * 响应正常->交易状态
     * 响应异常->错误原因
     * 其他问题->返回响应body
     *
     * @param response
     * @return
     */
    private String checkTradeStatus(AlipayTradeQueryResponse response) {
        String result = response.getBody();
        if (response.isSuccess() && "10000".equals(response.getCode())) {
            //返回订单交易状态
            result = response.getTradeStatus();
        } else {
            String subCode = response.getSubCode();
            if ("ACQ.SYSTEM_ERROR".equals(subCode)) {
                result = "系统错误,重新发起请求";
            }
            if ("ACQ.INVALID_PARAMETER".equals(subCode)) {
                result = "参数无效,检查请求参数，修改后重新发起请求";
            }
            if ("ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                result = "查询的交易不存在,检查传入的交易号是否正确，修改后重新发起请求";
            }
        }
        return result;
    }

    /**
     * 通过二维码链接生成收款二维码
     *
     * @param orderId
     * @param response
     * @throws WriterException
     * @throws IOException
     */
    private void createQrCode(@RequestParam String orderId, AlipayTradePrecreateResponse response) throws WriterException, IOException {
        String qrCode = response.getQrCode(); //获得二维码链接
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bt = writer.encode(qrCode, BarcodeFormat.QR_CODE, 300, 300); //绘制二维码
        //生成二维码,将二维码写到输出流,返回到页面
        //MatrixToImageWriter.writeToStream(bt, "jpg", httpResponse.getOutputStream());
        //将二维码写入到磁盘
        File file = new File("D:\\Develop\\IdeaProjects\\lagou_parent\\lagou_service\\lagou_service_pay\\src\\main\\resources\\qrcodes", orderId + ".jpg");
        MatrixToImageWriter.writeToFile(bt, "jpg", file);
    }

    /**
     * 创建预处理模型
     *
     * @param orderId
     * @param request
     * @param exchange
     */
    private void createPrecreateModel(@RequestParam String orderId, AlipayTradePrecreateRequest request, String exchange) {
        //1.获得订单对象，判断支付状态
        //如果是普通订单去订单微服务获取订单对象,如果是秒杀订单去redis中获取订单对象SeckillOrder
        String totalMoney = null;
        //普通订单
        if ("order_exchange".equals(exchange)) {
            totalMoney = orderFeign.findById(orderId).getData().getTotalMoney().toString();
        }
        //秒杀订单
        if ("seckill_exchange".equals(exchange)) {
            totalMoney = ((SeckillOrder) redisTemplate.boundHashOps("SeckillOrder_").get("yuanjing")).getMoney().toString();
        }
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        //设置商户订单号
        model.setOutTradeNo(orderId);
        //卖家支付宝用户ID
        model.setSellerId("2088621955637013");
        //设置支付金额
        model.setTotalAmount(totalMoney);
        //设置body(区分普通订单还是秒杀订单)
        model.setBody(exchange);
        //商品的标题/交易标题/订单标题/订单关键字等。
        model.setSubject("拉勾商城-订单支付");
        //将model放入到请求中
        request.setBizModel(model);
    }

    /**
     * 将阿里服务器请求中的数据转换为Map
     *
     * @param request
     * @return
     */
    private Map<String, String> parseAlipayResultToMap(HttpServletRequest request) {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        //支付宝请求中的参数
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return params;
    }

    /**
     * 准备要发送到MQ中的数据
     *
     * @param params
     * @return
     */
    private String prepareMQData(Map<String, String> params) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("out_trade_no", params.get("out_trade_no"));
        messageMap.put("trade_no", params.get("trade_no"));
        messageMap.put("total_amount", params.get("total_amount"));
        //  yyyy-MM-dd HH:mm:ss
        messageMap.put("gmt_payment", params.get("gmt_payment"));
        //发动到MQ中
        return JSON.toJSONString(messageMap);
    }

}
