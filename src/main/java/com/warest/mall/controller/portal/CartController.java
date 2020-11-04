package com.warest.mall.controller.portal;

import com.warest.mall.common.Const;
import com.warest.mall.common.ResponseCode;
import com.warest.mall.common.ResponseEntity;
import com.warest.mall.domain.User;
import com.warest.mall.service.ICartService;
import com.warest.mall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 加入购物车
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService iCartService;

    //todo  登录拦截器
    private Integer checkLoginId(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return user==null?null:user.getId();
    }

    // 查询购物车
    @PostMapping("list")
    public ResponseEntity<CartVo> list(HttpSession session){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.list(userId);
    }

    /**
     * 加入购物车
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @PostMapping("add")
    public ResponseEntity<CartVo> add(HttpSession session, Integer count, Integer productId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.add(userId,productId,count);
    }


    /**
     * 修改购物车商品数量
     * 没有的商品不会添加
     * @param session
     * @param count
     * @param productId
     * @return
     */
    @PostMapping("update")
    public ResponseEntity<CartVo> update(HttpSession session, Integer count, Integer productId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.update(userId,productId,count);
    }

    /**
     * 删除商品
     * @param session
     * @param productIds
     * @return
     */
    @PostMapping("delete_product")
    public ResponseEntity<CartVo> deleteProduct(HttpSession session, String productIds){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.deleteProduct(userId,productIds);
    }


    /**
     * 全选
     * @param session
     * @return
     */
    @PostMapping("select_all")
    public ResponseEntity<CartVo> selectAll(HttpSession session){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.selectOrUnSelect(userId,null,Const.Cart.CHECKED);
    }

    /**
     * 全反选
     * @param session
     * @return
     */
    @PostMapping("un_select_all")
    public ResponseEntity<CartVo> unSelectAll(HttpSession session){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.selectOrUnSelect(userId,null,Const.Cart.UN_CHECKED);
    }


    /**
     * 单选
     * @param session
     * @param productId 根据的是商品id而不是购物车商品id
     * @return
     */
    @PostMapping("select")
    public ResponseEntity<CartVo> select(HttpSession session, Integer productId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.selectOrUnSelect(userId,productId,Const.Cart.CHECKED);
    }

    /**
     * 单选反选
     * @param session
     * @param productId
     * @return
     */
    @PostMapping("un_select")
    public ResponseEntity<CartVo> unSelect(HttpSession session, Integer productId){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc()):iCartService.selectOrUnSelect(userId,productId,Const.Cart.UN_CHECKED);
    }


    /**
     * 查询购物车不同规格商品数，数量之和
     * @param session
     * @return
     */
    @PostMapping("get_cart_product_count")
    public ResponseEntity<Integer> getCartProductCount(HttpSession session){
        Integer userId = this.checkLoginId(session);
        return userId==null?ResponseEntity.createBySuccess(0):iCartService.getCartProductCount(userId);

    }




    //全选
    //全反选

    //单独选
    //单独反选

    //查询当前用户的购物车里面的产品数量,如果一个产品有10个,那么数量就是10.




}
