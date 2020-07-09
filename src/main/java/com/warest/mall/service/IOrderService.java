package com.warest.mall.service;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.vo.OrderVo;

import java.util.Map;

/**
 * Created by geely
 */
public interface IOrderService {
    ResponseEntity pay(Long orderNo, Integer userId, String path);
    ResponseEntity aliCallback(Map<String,String> params);
    ResponseEntity queryOrderPayStatus(Integer userId, Long orderNo);
    ResponseEntity createOrder(Integer userId, Integer shippingId);
    ResponseEntity<String> cancel(Integer userId, Long orderNo);
    ResponseEntity getOrderCartProduct(Integer userId);
    ResponseEntity<OrderVo> getOrderDetail(Integer userId, Long orderNo);
    ResponseEntity<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize);



    //backend
    ResponseEntity<PageInfo> manageList(int pageNum, int pageSize);
    ResponseEntity<OrderVo> manageDetail(Long orderNo);
    ResponseEntity<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);
    ResponseEntity<String> manageSendGoods(Long orderNo);


}
