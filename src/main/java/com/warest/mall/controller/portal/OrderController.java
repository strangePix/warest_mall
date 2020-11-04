package com.warest.mall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.User;
import com.warest.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Created by geely
 */

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;


    /**
     * 创建订单
     * @param session
     * @param shippingId  收货地址id
     * @return
     */
    @PostMapping("create")
    public ResponseEntity create(HttpSession session, Integer shippingId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(), shippingId);
    }


    @PostMapping("cancel")
    public ResponseEntity cancel(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(), orderNo);
    }


    /**
     * 获取订单商品信息
     * @param session
     * @return
     */
    @PostMapping("get_order_cart_product")
    public ResponseEntity getOrderCartProduct(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }


    @PostMapping("detail")
    public ResponseEntity detail(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    @PostMapping("list")
    public ResponseEntity list(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(), pageNum, pageSize);
    }





    /**
     * 支付
     *
     * @param session
     * @param orderNo 订单号
     * @param request 用于获取upload文件夹，返回图片地址
     * @return
     */
    @PostMapping("pay")
    public ResponseEntity pay(HttpSession session, Long orderNo, HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }
        //获取上传文件夹在服务器地址
        String path = request.getSession().getServletContext().getRealPath("upload");
        return iOrderService.pay(orderNo, user.getId(), path);
    }

    /**
     * 接收支付宝支付异步回调
     * @param request
     * @return  根据ali要求返回    需要返回success阿里才会停止通知  fail消息获取失败
     */
    @PostMapping("alipay_callback")
    public Object alipayCallback(HttpServletRequest request) {

        logger.info("alipay_callback");
        Map<String, String> params = Maps.newHashMap();

        // 从request中提取全部参数，组装到map中
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> stringEntry : requestParams.entrySet()) {
            String name = stringEntry.getKey();
            String[] values = stringEntry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr += (i == values.length - 1) ? values[i] : values[i] + ",";
            }
            params.put(name, valueStr);
        }

        /*for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for(int i = 0 ; i <values.length;i++){

                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i]+",";
            }
            params.put(name,valueStr);
        }*/
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}", new String[]{params.get("sign"), params.get("trade_status"), params.toString()});

        //非常重要,验证回调的正确性,是不是支付宝发的.并且呢还要避免重复通知.

        params.remove("sign_type");  //内部方法已经排除sign，这里只需要排除sign_type
        try {
            //boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());


            if (!alipayRSACheckedV2) {
                return ResponseEntity.createByErrorMessage("非法请求,验证不通过");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常", e);
        }
        //todo 除了验证支付宝数据外，接收到的通知还需验证是否来自本商城


        //业务逻辑
        ResponseEntity serverResponse = iOrderService.aliCallback(params);
        if (serverResponse.isSuccess()) {
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }


    /**
     * 前台轮询查询订单状态  用于采取响应行动
     * @param session
     * @param orderNo
     * @return
     */
    @PostMapping("query_order_pay_status")
    public ResponseEntity<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
        }

        ResponseEntity serverResponse = iOrderService.queryOrderPayStatus(user.getId(), orderNo);
        if (serverResponse.isSuccess()) {
            return ResponseEntity.createBySuccess(true);
        }
        return ResponseEntity.createBySuccess(false);
    }


}
