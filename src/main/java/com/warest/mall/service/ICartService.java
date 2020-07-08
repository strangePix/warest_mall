package com.warest.mall.service;

import com.warest.mall.common.ServerResponse;
import com.warest.mall.vo.CartVo;

/**
 * Created by geely
 */
public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> deleteProduct(Integer userId,String productIds);

    ServerResponse<CartVo> list (Integer userId);
    ServerResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked);
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
