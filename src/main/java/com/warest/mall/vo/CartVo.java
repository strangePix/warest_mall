package com.warest.mall.vo;

import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车vo
 */
public class CartVo {

    // 购物车商品集合
    private List<CartProductVo> cartProductVoList;
    //总价
    private BigDecimal cartTotalPrice;
    //全选
    private Boolean allChecked;
    //购物车图片
    @Value("${ftp.server.http.prefix:http://img.happymmall.com/}")
    private String imageHost;

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
