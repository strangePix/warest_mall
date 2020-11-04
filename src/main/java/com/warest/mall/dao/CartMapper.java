package com.warest.mall.dao;


import com.warest.mall.domain.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    //两个同类型参数需要注解区分
    Cart selectCartByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);
    // Cart selectCartByUserIdProductId(Integer userId, Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectCartProductCheckedStatusByUserId(Integer userId);

    int deleteByUserIdProductIds(@Param("userId") Integer userId, @Param("productIdList") List<String> productIdList);


    int checkedOrUncheckedProduct(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("checked") Integer checked);

    int selectCartProductCount(Integer userId);


    /**
     * 根据用户id查询勾选的购物车信息
     * @param userId
     * @return
     */
    List<Cart> selectCheckedCartByUserId(Integer userId);


}
