package com.warest.mall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.dao.CartMapper;
import com.warest.mall.dao.ProductMapper;
import com.warest.mall.domain.Cart;
import com.warest.mall.domain.Product;
import com.warest.mall.service.ICartService;
import com.warest.mall.util.BigDecimalUtil;
import com.warest.mall.vo.CartProductVo;
import com.warest.mall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by geely
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;

    // 查询当前购物车信息
    public ResponseEntity<CartVo> list (Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);
        return ResponseEntity.createBySuccess(cartVo);
    }

    /**
     * 添加购物车
     * @param userId 用户id
     * @param productId 商品id
     * @param count 数量
     * @return
     */
    public ResponseEntity<CartVo> add(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // TODO: 2020/9/1  判断商品是否下架状态，下架不能添加  暂时不提示
        if(!Integer.valueOf(Const.ProductStatusEnum.ON_SALE.getCode()).equals(productMapper.getProductStatus(productId))){
            return this.list(userId);
        }
        // 先查购物车有没有这个商品（sku）
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart == null){
            //这个产品不在这个购物车里,需要新增一个这个产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            // 购物车选中状态
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{
            //这个产品已经在购物车里了.
            //如果产品已存在,数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        //库存数量校验放在list函数中统一修正
        return this.list(userId);
    }

    /**
     * 更新购物车商品数
     * 没有的商品不会添加，不会更新数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    public ResponseEntity<CartVo> update(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    /**
     * 删除商品
     * @param userId
     * @param productIds 字符串形式显示的删除商品id列表，格式是 xx,xx,xx 需要分割
     * @return
     */
    public ResponseEntity<CartVo> deleteProduct(Integer userId, String productIds){
        // 工具类方法 字符串切割，拼成集合
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ResponseEntity.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }


    /**
     * 全选或者全反选或单选或单反选
     * @param userId
     * @param productId 为null时即为全选/全反选
     * @param checked Const.Cart.CHECKED / Const.Cart.UN_CHECKED
     * @return
     */
    public ResponseEntity<CartVo> selectOrUnSelect (Integer userId, Integer productId, Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    /**
     * 查询商品总数
     * @param userId
     * @return
     */
    public ResponseEntity<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ResponseEntity.createBySuccess(0);
        }
        return ResponseEntity.createBySuccess(cartMapper.selectCartProductCount(userId));
    }


    /**
     * 查询购物车，封装为vo
     * 里面包含一个商品集合 cartproductvo
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();
        // 查购物车商品集合
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();

        // 初始化购物车总价
        // 优先使用string构建初始值，用double或者int会出现精度上的误差
        BigDecimal cartTotalPrice = new BigDecimal("0");

        boolean isAllChecked = true;

        // 购物车不为空则进行封装
        if(CollectionUtils.isNotEmpty(cartList)){
            for(Cart cartItem : cartList){
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());

                // 根据购物车商品查询商品
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                if(product != null){
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock()); //商品库存，不是用户购买的数量
                    //判断库存
                    int buyLimitCount = 0;
                    if(product.getStock() >= cartItem.getQuantity()){
                        //如果库存充足（库存比购买数多），则保留购买数，标注购买数未超限
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    }else{
                        //否则库存不足，将购买数设置为最大库存，标注购买数超限，进行限制
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新购买商品数
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    //vo也更新购买数
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算总价=单价*数量
                    // cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.multiply(product.getPrice(),cartProductVo.getQuantity()));
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                // 计算购物车总价，如果勾选了，则添加到总价中
                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice,cartProductVo.getProductTotalPrice());
                }else{
                    isAllChecked = false;
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        // cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setAllChecked(isAllChecked);
        // cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;

    }


























}
