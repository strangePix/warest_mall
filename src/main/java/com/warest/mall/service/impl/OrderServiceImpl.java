package com.warest.mall.service.impl;

/*import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;*/

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.dao.*;
import com.warest.mall.domain.*;
import com.warest.mall.service.IOrderService;
import com.warest.mall.util.BigDecimalUtil;
import com.warest.mall.util.DateTimeUtil;
import com.warest.mall.util.FTPUtil;
import com.warest.mall.util.PropertiesUtil;
import com.warest.mall.vo.OrderItemVo;
import com.warest.mall.vo.OrderProductVo;
import com.warest.mall.vo.OrderVo;
import com.warest.mall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by geely
 */
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {


    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         **/
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         **/
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayInfoMapper payInfoMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ShippingMapper shippingMapper;


    /**
     * 创建订单
     * @param userId 用户id
     * @param shippingId
     * @return
     */
    public ResponseEntity createOrder(Integer userId, Integer shippingId) {

        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);

        //计算这个订单的总价
        ResponseEntity serverResponse = this.getCartOrderItem(userId, cartList);
        //如果有错就报错
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        //取出商品列表
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        //计算总价
        BigDecimal payment = this.getOrderTotalPrice(orderItemList);


        //生成订单
        Order order = this.assembleOrder(userId, shippingId, payment);
        if (order == null) {
            return ResponseEntity.createByErrorMessage("生成订单错误");
        }
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ResponseEntity.createByErrorMessage("购物车为空");
        }
        //配置订单号
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }
        //mybatis 批量插入
        orderItemMapper.batchInsert(orderItemList);

        //生成成功,我们要减少我们产品的库存
        this.reduceProductStock(orderItemList);
        //清空一下购物车
        this.cleanCart(cartList);

        //返回给前端数据
        OrderVo orderVo = assembleOrderVo(order, orderItemList);
        return ResponseEntity.createBySuccess(orderVo);
    }


    /**
     * 拼装返回数据
     * @param order
     * @param orderItemList
     * @return
     */
    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());

        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf(order.getStatus()).getValue());

        orderVo.setShippingId(order.getShippingId());
        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(assembleShippingVo(shipping));
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));


        //orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));


        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = assembleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        orderVo.setOrderItemVoList(orderItemVoList);
        return orderVo;
    }


    private OrderItemVo assembleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());

        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));
        return orderItemVo;
    }


    /**
     * 拼装快递vo
     * @param shipping
     * @return
     */
    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        shippingVo.setReceiverPhone(shippingVo.getReceiverPhone());
        return shippingVo;
    }

    /**
     * 清空购物车
     * @param cartList
     */
    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }


    /**
     * 减少库存，根据订单
     * @param orderItemList
     */
    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }


    /**
     * 组装订单
     * @param userId
     * @param shippingId
     * @param payment
     * @return
     */
    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payment) {
        Order order = new Order();
        //生成订单号
        long orderNo = this.generateOrderNo();
        order.setOrderNo(orderNo);
        //状态未付款
        order.setStatus(Const.OrderStatusEnum.NO_PAY.getCode());
        //运费为0
        order.setPostage(0);
        //支付方式
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        //总价
        order.setPayment(payment);

        order.setUserId(userId);
        order.setShippingId(shippingId);
        //todo 发货时间等等
        //todo 付款时间等等
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }


    /**
     * 生成订单号
     * @return
     */
    private long generateOrderNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime+currentTime%10 + new Random().nextInt(100);
    }

    public static void main(String[] args) {

        System.out.println(new OrderServiceImpl().generateOrderNo());
    }


    /**
     * 计算总价
     * @param orderItemList
     * @return
     */
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        // BigDecimal payment = new BigDecimal("0");
        // for (OrderItem orderItem : orderItemList) {
        //     payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
        // }
        BigDecimal payment = orderItemList.stream().map(e->e.getTotalPrice()).reduce(BigDecimal.ONE,BigDecimal::add);
        return payment;
    }

    /*public static void main(String[] args) {
        List<BigDecimal> list = Arrays.asList(new BigDecimal(1),new BigDecimal(2),new BigDecimal(3),new BigDecimal(4));
        BigDecimal bigDecimal = list.stream().reduce(BigDecimal.ONE,BigDecimal::multiply);
        System.out.println(bigDecimal.intValue());
    }*/

    /**
     * 计算订单总价
     * @param userId
     * @param cartList
     * @return
     */
    private ResponseEntity getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ResponseEntity.createByErrorMessage("购物车为空");
        }

        //校验购物车的数据,包括产品的状态和数量
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ResponseEntity.createByErrorMessage("产品" + product.getName() + "不是在线售卖状态");
            }

            //校验库存
            if (cartItem.getQuantity() > product.getStock()) {
                return ResponseEntity.createByErrorMessage("产品" + product.getName() + "库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductImage(product.getMainImage());
            //单价
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            //单品价格  价格*数量
            orderItem.setTotalPrice(BigDecimalUtil.multiply(product.getPrice(), cartItem.getQuantity()));
            orderItemList.add(orderItem);
        }
        return ResponseEntity.createBySuccess(orderItemList);
    }


    public ResponseEntity<String> cancel(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ResponseEntity.createByErrorMessage("该用户此订单不存在");
        }
        if (order.getStatus() != Const.OrderStatusEnum.NO_PAY.getCode()) {
            return ResponseEntity.createByErrorMessage("已付款,无法取消订单");
        }
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(Const.OrderStatusEnum.CANCELED.getCode());

        int row = orderMapper.updateByPrimaryKeySelective(updateOrder);
        if (row > 0) {
            return ResponseEntity.createBySuccess();
        }
        return ResponseEntity.createByError();
    }


    public ResponseEntity getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据

        List<Cart> cartList = cartMapper.selectCheckedCartByUserId(userId);
        ResponseEntity serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();

        BigDecimal payment = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderItem.getTotalPrice().doubleValue());
            orderItemVoList.add(assembleOrderItemVo(orderItem));
        }
        orderProductVo.setProductTotalPrice(payment);
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ResponseEntity.createBySuccess(orderProductVo);
    }


    public ResponseEntity<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo, userId);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ResponseEntity.createBySuccess(orderVo);
        }
        return ResponseEntity.createByErrorMessage("没有找到该订单");
    }


    public ResponseEntity<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = assembleOrderVoList(orderList, userId);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ResponseEntity.createBySuccess(pageResult);
    }


    private List<OrderVo> assembleOrderVoList(List<Order> orderList, Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //todo 管理员查询的时候 不需要传userId
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());
            } else {
                orderItemList = orderItemMapper.getByOrderNoUserId(order.getOrderNo(), userId);
            }
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }


    public ResponseEntity pay(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        //查询对应用户是否有相应订单
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);

        if (order == null) {
            return ResponseEntity.createByErrorMessage("用户没有该订单");
        }
        // 确认无误放入map
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        //组装支付宝订单

        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = new StringBuilder().append("mmal扫码支付/订单号").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "用户自己的支付宝付款码"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        //        String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0.0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品3件共20.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("总价").append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        //String providerId = "2088100200300400500";
        ExtendParams extendParams = new ExtendParams();
        //extendParams.setSysServiceProviderId(providerId);

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        // GoodsDetail goods1 = GoodsDetail.newInstance("goods_id001", "xxx面包", 1000, 1);
        // // 创建好一个商品后添加至商品明细列表
        // goodsDetailList.add(goods1);
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(orderNo,userId);
        orderItemList.stream().forEach(e->goodsDetailList.add(GoodsDetail.newInstance(e.getProductId().toString(),e.getProductName(),
                BigDecimalUtil.multiply(e.getCurrentUnitPrice().doubleValue(),new Double(100).doubleValue()).longValue(),
                e.getQuantity())));


        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);

        //参数组装完逼

        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                //创建文件夹路径
                File file = new File(path);
                if(!file.exists()){
                    file.setWritable(true);
                    file.mkdirs();
                }

                // 根据订单号生成路径
                String filePath = String.format(path+"/qr-%s.png",response.getOutTradeNo());
                String fileName = String.format("qr-%s.png", response.getOutTradeNo());
                //工具类生成二维码   包含文件名
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, filePath);
                //上传到ftp服务器
                try {
                    File targetFile = new File(path,fileName);
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));

                    String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFile.getName();
                    resultMap.put("qrUrl",qrUrl);
                } catch (IOException e) {
                    logger.info("二维码上传异常:", e);
                    e.printStackTrace();
                }


                return ResponseEntity.createBySuccess(resultMap);

            case FAILED:
                return ResponseEntity.createByErrorMessage("支付宝预下单失败");
            case UNKNOWN:
                //logger.error("系统异常，预下单状态未知!!!");
                return ResponseEntity.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                //logger.error("不支持的交易状态，交易返回异常!!!");
                return ResponseEntity.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答响应
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }


    /**
     * 回调数据验证
     * @param params
     * @return
     */
    public ResponseEntity aliCallback(Map<String, String> params) {
        //订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //支付宝交易号
        String tradeNo = params.get("trade_no");
        //交易状态
        String tradeStatus = params.get("trade_status");
        //查询订单
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseEntity.createByErrorMessage("非本商城的订单,回调忽略");
        }
        //判断订单状态，是否已支付  大于则无需处理
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            //正常返回，避免再次回调
            return ResponseEntity.createBySuccess("支付宝重复调用");
        }
        //判断支付宝交易状态  是代付款还是已支付
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            //更新付款时间  格式为yyyy-MM-dd HH:mm:ss
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));

            order.setStatus(Const.OrderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        //支付平台  用枚举
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        //支付宝交易号
        payInfo.setPlatformNumber(tradeNo);
        // 支付宝交易状态
        payInfo.setPlatformStatus(tradeStatus);

        payInfoMapper.insert(payInfo);

        return ResponseEntity.createBySuccess();
    }


    /**
     * 查询支付宝订单状态
     * @param userId
     * @param orderNo
     * @return 是否已支付
     */
    public ResponseEntity queryOrderPayStatus(Integer userId, Long orderNo) {
        //查询订单是否存在
        Order order = orderMapper.selectByUserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ResponseEntity.createByErrorMessage("用户没有该订单");
        }
        //查询订单状态是否为未支付状态
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            return ResponseEntity.createBySuccess();
        }
        return ResponseEntity.createByError();
    }


    //backend

    public ResponseEntity<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orderList = orderMapper.selectAllOrder();
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, null);
        PageInfo pageResult = new PageInfo(orderList);
        pageResult.setList(orderVoList);
        return ResponseEntity.createBySuccess(pageResult);
    }


    public ResponseEntity<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);
            return ResponseEntity.createBySuccess(orderVo);
        }
        return ResponseEntity.createByErrorMessage("订单不存在");
    }


    public ResponseEntity<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = assembleOrderVo(order, orderItemList);

            PageInfo pageResult = new PageInfo(Lists.newArrayList(order));
            pageResult.setList(Lists.newArrayList(orderVo));
            return ResponseEntity.createBySuccess(pageResult);
        }
        return ResponseEntity.createByErrorMessage("订单不存在");
    }


    public ResponseEntity<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (order.getStatus() == Const.OrderStatusEnum.PAID.getCode()) {
                order.setStatus(Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                orderMapper.updateByPrimaryKeySelective(order);
                return ResponseEntity.createBySuccess("发货成功");
            }
        }
        return ResponseEntity.createByErrorMessage("订单不存在");
    }


}
