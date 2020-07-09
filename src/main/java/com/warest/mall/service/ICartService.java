package com.warest.mall.service;

import com.warest.mall.common.ResponseEntity;
import com.warest.mall.vo.CartVo;

/**
 * Created by geely
 */
public interface ICartService {
    ResponseEntity<CartVo> add(Integer userId, Integer productId, Integer count);
    ResponseEntity<CartVo> update(Integer userId, Integer productId, Integer count);
    ResponseEntity<CartVo> deleteProduct(Integer userId, String productIds);

    ResponseEntity<CartVo> list (Integer userId);
    ResponseEntity<CartVo> selectOrUnSelect (Integer userId, Integer productId, Integer checked);
    ResponseEntity<Integer> getCartProductCount(Integer userId);
}
