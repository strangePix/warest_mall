package com.warest.mall.service;

import com.github.pagehelper.PageInfo;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.Shipping;

/**
 * Created by geely
 */
public interface IShippingService {

    ResponseEntity add(Integer userId, Shipping shipping);
    ResponseEntity<String> del(Integer userId, Integer shippingId);
    ResponseEntity update(Integer userId, Shipping shipping);
    ResponseEntity<Shipping> select(Integer userId, Integer shippingId);
    ResponseEntity<PageInfo> list(Integer userId, int pageNum, int pageSize);

}
